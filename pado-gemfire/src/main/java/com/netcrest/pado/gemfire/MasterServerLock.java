/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.gemfire;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import com.gemstone.gemfire.admin.AdminDistributedSystem;
import com.gemstone.gemfire.admin.AdminDistributedSystemFactory;
import com.gemstone.gemfire.admin.DistributedSystemConfig;
import com.gemstone.gemfire.admin.SystemMembershipEvent;
import com.gemstone.gemfire.admin.SystemMembershipListener;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.netcrest.pado.info.ServerInfo;
import com.netcrest.pado.info.message.GridStatusInfo;
import com.netcrest.pado.info.message.GridStatusInfo.Status;
import com.netcrest.pado.info.message.MessageType;
import com.netcrest.pado.internal.factory.InfoFactory;
import com.netcrest.pado.link.IGridBizLink;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.MasterFailoverListener;
import com.netcrest.pado.server.PadoServerManager;

/**
 * MasterServerLock uses a global lock to determine master and slave servers.
 * This singleton class enables one and only one master server per grid. All
 * non-master servers are labeled "slaves". Upon termination of the master
 * server, it automatically selects the first slave that detects the termination
 * as the new master. The key "masterId" maps the master member ID in the global
 * region, "__pado/system".
 * 
 * @author dpark
 * 
 */
public class MasterServerLock
{
	/**
	 * The master ID lock name.
	 */
	protected final static String LOCK_NAME_FAILOVER = "masterId";

	private static MasterServerLock masterServerLock;

	private String masterId = null;

	private AdminDistributedSystem adminDS;

	/**
	 * Do not use this member directly. Always use isMasterEnabled(), instead.
	 * Whoever comes up first is the master. Subsequent caches are automatically
	 * set to slaves.
	 */
	private boolean masterEnabled = true;

	/**
	 * The system region is a global region for obtaining lock services and
	 * publishing system-wide event notifications.
	 */
	protected Region systemRegion;
	
	private final Set<MasterFailoverListener> masterFailoverListenerSet = Collections.synchronizedSet(new HashSet(2));
	
	public static MasterServerLock getMasterServerLock()
	{
		return masterServerLock;
	}

	public static synchronized void initialize(Region systemRegion)
	{
		if (masterServerLock == null) {
			masterServerLock = new MasterServerLock(systemRegion);
		}
	}

	private MasterServerLock(Region systemRegion)
	{
		this.systemRegion = systemRegion;
		init();
	}

	/**
	 * Initializes the system region, registers a command manager, adds a
	 * membership event listener.
	 */
	private void init()
	{
		Cache cache = CacheFactory.getAnyInstance();
		Lock lock = systemRegion.getDistributedLock(LOCK_NAME_FAILOVER);
		lock.lock();
		try {
			masterId = (String) systemRegion.get(LOCK_NAME_FAILOVER);
			if (masterId == null && masterEnabled) {
				this.masterEnabled = true;
				masterId = cache.getDistributedSystem().getDistributedMember().getId();
				systemRegion.put(LOCK_NAME_FAILOVER, masterId);
			} else {
				masterEnabled = false;
			}
		} finally {
			lock.unlock();
		}

		// Setup failover listener
		try {
			DistributedSystemConfig config = AdminDistributedSystemFactory.defineDistributedSystem(
					cache.getDistributedSystem(), null);
			adminDS = AdminDistributedSystemFactory.getDistributedSystem(config);
			adminDS.connect();
			adminDS.addMembershipListener(new SystemMembershipListenerImpl());
		} catch (Exception ex) {
			Logger.error(ex);
		}
		logCacheServerState();
	}

	/**
	 * Turns this cache into master. Returns true if this cache became a new
	 * master.
	 */
	public boolean enableMaster(String oldMasterId)
	{
		boolean newMaster = false;
		if (systemRegion != null) {
			Lock lock = systemRegion.getDistributedLock(LOCK_NAME_FAILOVER);
			lock.lock();
			try {
				String systemId = (String) systemRegion.get(LOCK_NAME_FAILOVER);
				String cacheId = CacheFactory.getAnyInstance().getDistributedSystem().getDistributedMember().getId();
				if (systemId != null) {
					if (systemId.equals(oldMasterId)) {
						masterId = cacheId;
						systemRegion.put(LOCK_NAME_FAILOVER, masterId);
						this.masterEnabled = true;
						newMaster = true;
					} else if (systemId.equals(cacheId)) {
						newMaster = true;
						this.masterEnabled = true;
					}
				}
			} finally {
				lock.unlock();
			}
		}

		return newMaster;
	}

	/**
	 * Performs failover tasks. This method is invoked when the server receives
	 * a member crashed or member left event.
	 * 
	 * @param oldMasterMemberId
	 *            The member that failed.
	 */
	void failover(String oldMasterMemberId)
	{
		Logger.info("Failover in progress... Failed cache member ID: " + oldMasterMemberId);
		final boolean isBeforeMaster = isMasterEnabled();
		enableMaster(oldMasterMemberId);
		if (isMasterEnabled()) {
			// report server failure to parents
			PadoServerManager sm = PadoServerManager.getPadoServerManager();
			GridStatusInfo gridStatusInfo = InfoFactory.getInfoFactory().createGridStatusInfo(Status.SERVER_TERMINATED,
					sm.getGridId(), masterId, oldMasterMemberId, "Server terminated and failover has occurred");
			if (sm.isParent()) {
				sm.putMessage(MessageType.GridStatus, gridStatusInfo);
			} else {
				sm.putMessageParents(MessageType.GridStatus, gridStatusInfo);
			}
		}
		fireMasterFailoverEvent(isBeforeMaster);
		logCacheServerState();
	}

	void newServerStarted(String newMemberId, DistributedMember newMember)
	{
		if (isMasterEnabled()) {
			PadoServerManager sm = PadoServerManager.getPadoServerManager();
			String masterId = (String) systemRegion.get(LOCK_NAME_FAILOVER);
			GridStatusInfo gridStatusInfo = InfoFactory.getInfoFactory().createGridStatusInfo(Status.SERVER_STARTED,
					sm.getGridId(), masterId, newMemberId, "New server joined the grid");
			gridStatusInfo.setServerInfoList(getServerInfo(newMember));
			if (sm.isParent()) {
				sm.putMessage(MessageType.GridStatus, gridStatusInfo);
			} else {
				sm.putMessageParents(MessageType.GridStatus, gridStatusInfo);
			}
		}
	}

	/**
	 * Returns the first ServerInfo object created using the cache server info
	 * list. TODO: Replace ServerInfo with something more appropriate.
	 * 
	 * @return
	 */
	private List<ServerInfo> getServerInfo(DistributedMember member)
	{
		PadoServerManager sm = PadoServerManager.getPadoServerManager();
		IGridBizLink gridBiz = sm.getGridBiz();
		gridBiz.getBizContext().reset();
		gridBiz.getBizContext().getGridContextClient().setGridIds(sm.getGridId());
		Set<DistributedMember> memberSet = Collections.singleton(member);
		gridBiz.getBizContext().getGridContextClient().setProductSpecificData(memberSet);
		return gridBiz.getServerInfoList(null);
	}

	/**
	 * Returns true if master.
	 */
	public boolean isMasterEnabled()
	{
		return masterEnabled;
	}
	
	public void addMasterFailoverListener(final MasterFailoverListener listener)
	{
		masterFailoverListenerSet.add(listener);
	}
	
	public void removeMasterFailoverListener(final MasterFailoverListener listener)
	{
		masterFailoverListenerSet.remove(listener);
	}
	
	protected void fireMasterFailoverEvent(final boolean isMasterBefore)
	{
		synchronized (masterFailoverListenerSet) {
			for (final MasterFailoverListener listener : masterFailoverListenerSet) {
				listener.failoverOccurred(isMasterBefore, isMasterEnabled());
			}
		}
	}
	
	/**
	 * Prints the server Primary/Secondary status.
	 */
	protected void logCacheServerState()
	{
		if (isMasterEnabled()) {
			Logger.info("Ready (Master).");
		} else {
			Logger.info("Ready (Slave).");
		}
	}

	class SystemMembershipListenerImpl implements SystemMembershipListener
	{
		public void memberJoined(SystemMembershipEvent event)
		{
			// TODO: newServerStarted() is not working properly
			// Throws NPE when invoking gridBiz.getServerInfoList() in getServerInfo().	
//			newServerStarted(event.getMemberId(), event.getDistributedMember());
		}

		public void memberCrashed(SystemMembershipEvent event)
		{
			failover(event.getMemberId());
		}

		public void memberLeft(SystemMembershipEvent event)
		{
			failover(event.getMemberId());
		}
	}
}
