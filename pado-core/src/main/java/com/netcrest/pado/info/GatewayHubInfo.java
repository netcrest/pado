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
package com.netcrest.pado.info;

import java.util.ArrayList;
import java.util.List;

/**
 * GatewayHubInfo provides information of a gateway hub that synchronizes data
 * between grids. This class applies to data grid products that support the WAN
 * topology.
 * 
 * @author dpark
 * 
 */
public abstract class GatewayHubInfo implements Comparable<GatewayHubInfo>
{
	/**
	 * Gateway bind address
	 */
	protected String bindAddress;
	
	/**
	 * Gateway hub ID
	 */
	protected String id;
	
	/**
	 * Gateway port number
	 */
	protected int port;
	
	/**
	 * List of GatewayInfo objects
	 */
	protected List<GatewayInfo> gatewayInfoList = new ArrayList<GatewayInfo>(4);

	/**
	 * Constructs an empty GatewayHubInfo object.
	 */
	public GatewayHubInfo()
	{
	}

	/**
	 * Returns the bind address to which the gateway hub listens on.
	 */
	public String getBindAddress()
	{
		return bindAddress;
	}

	/**
	 * Sets the bind address to which the gateway hub listens on.
	 * @param bindAddress
	 */
	public void setBindAddress(String bindAddress)
	{
		this.bindAddress = bindAddress;
	}

	/**
	 * Returns the gateway hub ID.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Sets the gateway hub ID.
	 * @param id Gateway hub ID
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * Returns the port number.
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * Sets the port number.
	 * @param port Port number
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * Returns the list of GatewayInfo objects.
	 */
	public List<GatewayInfo> getGatewayInfoList()
	{
		return gatewayInfoList;
	}

	/**
	 * Sets the list of GatewayInfo objects.
	 * @param gatewayInfoList
	 */
	public void setGatewayInfoList(List<GatewayInfo> gatewayInfoList)
	{
		this.gatewayInfoList = gatewayInfoList;
	}

	/**
	 * Compares {@link #getId()}.
	 */
	public int compareTo(GatewayHubInfo antoherGatewayHubInfo)
	{
		if (antoherGatewayHubInfo == null) {
			return 1;
		}
		if (id == null) {
			return -1;
		}
		return id.compareTo(antoherGatewayHubInfo.id);
	}
}
