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

import java.util.Properties;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.internal.util.QueueDispatcher;
import com.netcrest.pado.internal.util.QueueDispatcherListener;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;

/**
 * GridInfoCacheListenerImpl is registered to this grid to listen on child
 * GridInfos. Upon receipt of child GridInfo, this grid updates its child list
 * by opening grid-to-grid communication channels via the child pool information
 * provided by GridInfo. It also updates AppInfo and GridRouterInfo.
 * 
 * @author dpark
 * 
 */
public class GridInfoCacheListenerImpl extends CacheListenerAdapter<String, GridInfo> implements Declarable
{
	private static boolean isLogGridInfoReceived = PadoUtil.getBoolean("log.gridInfo", true);
	
	private QueueDispatcher queueDispatcher = new QueueDispatcher();

	public GridInfoCacheListenerImpl()
	{
		queueDispatcher.setQueueDispatcherListener(new QueueDispatcherListener() {

			@Override
			public void objectDispatched(Object obj)
			{
				GridInfo gridInfo = null;
				try {
					Cache cache = null;
					try {
						cache = CacheFactory.getAnyInstance();
					} catch (Exception ex) {
						// ignore
					}
					if (cache != null && cache.isClosed() == false) {
						gridInfo = (GridInfo)obj;
						update(gridInfo);
					}
				} catch (Throwable th) {
					Logger.warning("Error occurred while updating GridInfo. This maybe due to a startup or shutdown race condition. If so, please ignore this message. The next update will perform properly. " + th.getMessage(), th);
					String gridId = PadoServerManager.getPadoServerManager().getGridId();
					if (gridInfo != null && gridId.equals(gridInfo.getGridId()) == false) {
						// Remove it so that the next event can be served
						// from scratch. Only child supported.
						PadoServerManager.getPadoServerManager().removeGrid(gridInfo, false);
					}
				}
			}

		});
		queueDispatcher.start();
	}

	@Override
	public void init(Properties props)
	{
	}

	/**
	 * Updates all AppInfo objects found in the app region that have the
	 * matching received grid Id. Region: &lt;gridId, GridInfo&gt;
	 * 
	 * @param event
	 */
	private synchronized void update(GridInfo gridInfo)
	{
		// Drop events generated from this grid
//		if (gridInfo.getGridId().equals(PadoServerManager.getPadoServerManager().getGridId())) {
//			return;
//		}

		// Log GridInfo only from the grids other than this one.
		if (gridInfo.getGridId().equals(PadoServerManager.getPadoServerManager().getGridId()) == false) {
			if (isLogGridInfoReceived) {
				Logger.config("GridInfo received: gridId = " + gridInfo.getGridId());
			}
		}

		// Update PadoManager with the new GridInfo received. This
		// applies the update only if GridInfo differs from PadoManager's
		// grid info. Supports only child for now.
		GemfirePadoServerManager.getPadoServerManager().updateGrid(gridInfo, false);
		if (Logger.isFineEnabled()) {
			Logger.fine("GridInfo received and child grid regions, AppInfo and GridRouterInfo updated. gridId="
					+ gridInfo.getGridId());
		}
	}
	
	public static void setLogGridInfoReceived(boolean isLogGridInfoReceived)
	{
		GridInfoCacheListenerImpl.isLogGridInfoReceived = isLogGridInfoReceived;
	}
	
	public static boolean isLogGridInfoReceived()
	{
		return isLogGridInfoReceived;
	}

	@Override
	public void afterCreate(EntryEvent<String, GridInfo> event)
	{
		queueDispatcher.enqueue(event.getNewValue());
		// update(event);
	}

	@Override
	public void afterUpdate(EntryEvent<String, GridInfo> event)
	{
		try {
			queueDispatcher.enqueue(event.getNewValue());
			// update(event);
		} catch (Throwable th) {
			// The following exception was seen during failover:
			// com.gemstone.gemfire.SerializationException: An IOException was
			// thrown while deserializing
			// What's causing it? Ignore it for now. Since it continues to
			// receive GridInfo periodically, not an issue at this time.
			Logger.warning(th);
		}
	}

	/**
	 * This method is invoked upon expiration. It cleans up all relevant pado
	 * regions.
	 */
	@Override
	public void afterDestroy(EntryEvent<String, GridInfo> event)
	{
		GridInfo gridInfo = event.getOldValue();

		// Remove the grid. Supports only child for now.
		PadoServerManager.getPadoServerManager().removeGrid(gridInfo, false);
	}

	@Override
	public void close()
	{
		queueDispatcher.stop();
	}
}
