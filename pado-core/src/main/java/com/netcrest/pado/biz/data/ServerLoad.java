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
package com.netcrest.pado.biz.data;

import java.io.Serializable;

/**
 * ServerLoad contains a load snapshot of a server.
 * 
 * @author dpark
 * 
 */
public class ServerLoad implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String gridId;
	private String siteId;
	private String serverId;
	// memoryUsage in %
	private byte memoryUsage;
	// cpuUsage in %
	private byte cpuUsage;
	private double averageLoad;
	private long gcAverageTimeInMsec;
	private long gcPauses;

	private transient int usageLoad;

	/**
	 * Constructs an empty ServerLoad object. Required by serialization.
	 */
	public ServerLoad()
	{
	}

	/**
	 * Constructs a ServerLoad object with the specified parameters.
	 * @param gridId Grid ID
	 * @param siteId Site ID
	 * @param serverId Server ID
	 * @param memoryUsage Memory usage in %
	 * @param cpuUsage CPU usage in %
	 * @param averageLoad Average load
	 * @param gcAverageTimeInMsec GC average time in msec
	 * @param gcPauses Number of GC pauses that have occurred
	 */
	public ServerLoad(String gridId, String siteId, String serverId, byte memoryUsage, byte cpuUsage,
			double averageLoad, long gcAverageTimeInMsec, long gcPauses)
	{
		super();
		this.gridId = gridId;
		this.siteId = siteId;
		this.serverId = serverId;
		this.memoryUsage = memoryUsage;
		this.cpuUsage = cpuUsage;
		this.averageLoad = averageLoad;
		this.gcAverageTimeInMsec = gcAverageTimeInMsec;
		this.gcPauses = gcPauses;
		computeUsageLoad();
	}

	/**
	 * Returns the ID of grid in which the server runs.
	 */
	public String getGridId()
	{
		return gridId;
	}

	/**
	 * Returns the ID of site in which the server runs.
	 *
	 */
	public String getSiteId()
	{
		return siteId;
	}

	/**
	 * Returns the server ID.
	 */
	public String getServerId()
	{
		return serverId;
	}

	/**
	 * Returns the memory usage.
	 */
	public double getMemoryUsage()
	{
		return memoryUsage;
	}

	/**
	 * Returns the CPU usage.
	 */
	public byte getCpuUsage()
	{
		return cpuUsage;
	}

	/**
	 * Returns the average server load.
	 */
	public double getAverageLoad()
	{
		return averageLoad;
	}

	/**
	 * Returns the average time in msec that the server spent in GC.
	 */
	public long getGcAverageTimeInMsec()
	{
		return gcAverageTimeInMsec;
	}

	/**
	 * Returns the number of GC pauses has have occurred so far.
	 */
	public long getGcPauses()
	{
		return gcPauses;
	}

	/**
	 * Returns the usage load determined by adding the CPU and memory usages.
	 */
	public int getUsageLoad()
	{
		return usageLoad;
	}

	/**
	 * Computes the usage load.
	 */
	private void computeUsageLoad()
	{
		usageLoad = cpuUsage + memoryUsage;
	}
}
