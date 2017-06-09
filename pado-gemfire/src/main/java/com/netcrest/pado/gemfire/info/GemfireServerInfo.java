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
package com.netcrest.pado.gemfire.info;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.info.BucketInfo;
import com.netcrest.pado.info.ServerInfo;

/**
 * ServerInfo contains server specific information obtained from CacheInfo,
 * CacheServerInfo, and RegionInfo. It provides server info for a given region.
 * 
 * @author dpark
 *
 */
public class GemfireServerInfo extends ServerInfo implements DataSerializable {
	private static final long serialVersionUID = 1L;

	// CacheInfo
	private int searchTimeout;
	private int lockTimeout;
	private int lockLease;
	private int messageSyncInterval;

	// CacheServerInfo
	private String[] groups;
	private String bindAddress;
	private String hostnameForClients;
	private int maxThreads;

	// RegionInfo
	private GemfireRegionInfo regionInfo;
	private String regionPath;

	// BucketInfo
	private int routingBucketId = -1;

	public GemfireServerInfo() {
	}

	/**
	 * Constructs a ServerInfo object for use in the client side. This
	 * constructor is locally invoked by clients after CacheInfo is retrieved
	 * from the grid. ServerInfo provides process-level details of a single
	 * server.
	 * 
	 * @param gridInfo
	 * @param cacheInfo
	 * @param cacheServerInfo
	 * @param fullPath
	 */
	public GemfireServerInfo(GemfireGridInfo gridInfo, GemfireCacheInfo cacheInfo,
			GemfireCacheServerInfo cacheServerInfo, String fullPath) {
		// GridInfo
		gridId = gridInfo.getGridId();

		// CacheInfo
		name = cacheInfo.getName();
		id = cacheInfo.getId();
		host = cacheInfo.getHost();
		processId = cacheInfo.getProcessId();
		searchTimeout = cacheInfo.getSearchTimeout();
		lockTimeout = cacheInfo.getLockTimeout();
		lockLease = cacheInfo.getLockLease();
		messageSyncInterval = cacheInfo.getMessageSyncInterval();

		// CacheServerInfo
		groups = cacheServerInfo.getGroups();
		port = cacheServerInfo.getPort();
		bindAddress = cacheServerInfo.getBindAddress();
		hostnameForClients = cacheServerInfo.getHostnameForClients();
		maxThreads = cacheServerInfo.getMaxThreads();

		// Additional info from the host
		try {
			InetAddress ip = InetAddress.getByName(host);
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			byte[] mac = network.getHardwareAddress();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
			}
			macAddress = sb.toString();
			hostAddress = ip.getHostAddress();
			canonicalHostName = ip.getCanonicalHostName();
			networkInterfaceName = network.getName();
			networkInterfaceDisplayName = network.getDisplayName();
			mtu = network.getMTU();
			Enumeration<InetAddress> en = network.getInetAddresses();
			while (en.hasMoreElements()) {
				InetAddress ia = en.nextElement();
				if (ia instanceof Inet6Address) {
					ipv6Address = ia.getHostAddress();
					break;
				}
			}
		} catch (Exception ex) {
			// ignore
		}

		// RegionInfo
		this.regionPath = fullPath;
		regionInfo = (GemfireRegionInfo) cacheInfo.getPathInfo(fullPath);
		// RegionInfo can be null if the top level is selected in UI, i.e.,
		// CacheInfo itself.
		if (regionInfo != null) {
			Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
			if (region != null) {
				int size;
				if (region instanceof PartitionedRegion) {
					size = PartitionRegionHelper.getLocalPrimaryData(region).size();
				} else {
					size = region.size();
				}
				regionInfo.setSize(size);
			}
		}

		// BucketInfo - use the server region (pr) for this purpose
		GemfireRegionInfo serverRegionInfo = (GemfireRegionInfo) cacheInfo
				.getPathInfo(gridInfo.getRootPathInfo().getFullPath() + "/__pado/server");
		if (serverRegionInfo.isDataPolicyPartitionedRegion(false)) {
			List<BucketInfo> list = serverRegionInfo.getPrimaryBucketInfoList();
			if (list.size() > 0) {
				this.routingBucketId = list.get(0).getBucketId();
			}
		} else {
			this.routingBucketId = -1;
		}
	}

	public int getSearchTimeout() {
		return searchTimeout;
	}

	public void setSearchTimeout(int searchTimeout) {
		this.searchTimeout = searchTimeout;
	}

	public int getLockTimeout() {
		return lockTimeout;
	}

	public void setLockTimeout(int lockTimeout) {
		this.lockTimeout = lockTimeout;
	}

	public int getLockLease() {
		return lockLease;
	}

	public void setLockLease(int lockLease) {
		this.lockLease = lockLease;
	}

	public int getMessageSyncInterval() {
		return messageSyncInterval;
	}

	public void setMessageSyncInterval(int messageSyncInterval) {
		this.messageSyncInterval = messageSyncInterval;
	}

	public String[] getGroups() {
		return groups;
	}

	public void setGroups(String[] groups) {
		this.groups = groups;
	}

	public String getBindAddress() {
		return bindAddress;
	}

	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
	}

	public String getHostnameForClients() {
		return hostnameForClients;
	}

	public void setHostnameForClients(String hostnameForClients) {
		this.hostnameForClients = hostnameForClients;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public int getRegionSize() {
		if (regionInfo != null) {
			return regionInfo.getSize();
		} else {
			return 0;
		}
	}

	public String getRegionPath() {
		return regionPath;
	}

	public String getRegionScope() {
		if (regionInfo != null) {
			return ((GemfireRegionAttributeInfo) regionInfo.getAttrInfo())
					.getAttribute(GemfireRegionAttributeInfo.SCOPE);
		} else {
			return "Undefined";
		}
	}

	public String getRegionDataPolicy() {
		if (regionInfo != null) {
			return ((GemfireRegionAttributeInfo) regionInfo.getAttrInfo())
					.getAttribute(GemfireRegionAttributeInfo.DATA_POLICY);
		} else {
			return "Undefined";
		}
	}

	public GemfireRegionInfo getRegionInfo() {
		return regionInfo;
	}

	public int getRoutingBucketId() {
		return routingBucketId;
	}

	public void setRoutingBucketId(int routingBucketId) {
		this.routingBucketId = routingBucketId;
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void fromData(DataInput input) throws IOException, ClassNotFoundException {
		gridId = DataSerializer.readString(input);
		name = DataSerializer.readString(input);
		id = DataSerializer.readObject(input);
		host = DataSerializer.readString(input);
		macAddress = DataSerializer.readString(input);
		hostAddress = DataSerializer.readString(input);
		canonicalHostName = DataSerializer.readString(input);
		networkInterfaceName = DataSerializer.readString(input);
		networkInterfaceDisplayName = DataSerializer.readString(input);
		ipv6Address = DataSerializer.readString(input);
		mtu = DataSerializer.readPrimitiveInt(input);
		processId = DataSerializer.readPrimitiveInt(input);
		searchTimeout = DataSerializer.readPrimitiveInt(input);
		lockTimeout = DataSerializer.readPrimitiveInt(input);
		lockLease = DataSerializer.readPrimitiveInt(input);
		messageSyncInterval = DataSerializer.readPrimitiveInt(input);
		groups = DataSerializer.readStringArray(input);
		port = DataSerializer.readPrimitiveInt(input);
		bindAddress = DataSerializer.readString(input);
		hostnameForClients = DataSerializer.readString(input);
		maxThreads = DataSerializer.readPrimitiveInt(input);
		regionInfo = DataSerializer.readObject(input);
		regionPath = DataSerializer.readString(input);
		routingBucketId = DataSerializer.readPrimitiveInt(input);
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void toData(DataOutput output) throws IOException {
		DataSerializer.writeString(gridId, output);
		DataSerializer.writeString(name, output);
		DataSerializer.writeObject(id, output);
		DataSerializer.writeString(host, output);
		DataSerializer.writeString(macAddress, output);
		DataSerializer.writeString(hostAddress, output);
		DataSerializer.writeString(canonicalHostName, output);
		DataSerializer.writeString(networkInterfaceName, output);
		DataSerializer.writeString(networkInterfaceDisplayName, output);
		DataSerializer.writeString(ipv6Address, output);
		DataSerializer.writePrimitiveInt(mtu, output);
		DataSerializer.writePrimitiveInt(processId, output);
		DataSerializer.writePrimitiveInt(searchTimeout, output);
		DataSerializer.writePrimitiveInt(lockTimeout, output);
		DataSerializer.writePrimitiveInt(lockLease, output);
		DataSerializer.writePrimitiveInt(messageSyncInterval, output);
		DataSerializer.writeStringArray(groups, output);
		DataSerializer.writePrimitiveInt(port, output);
		DataSerializer.writeString(bindAddress, output);
		DataSerializer.writeString(hostnameForClients, output);
		DataSerializer.writePrimitiveInt(maxThreads, output);
		DataSerializer.writeObject(regionInfo, output);
		DataSerializer.writeString(regionPath, output);
		DataSerializer.writePrimitiveInt(routingBucketId, output);
	}
}
