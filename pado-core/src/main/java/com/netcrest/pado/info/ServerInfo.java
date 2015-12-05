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

/**
 * ServerInfo contains server specific information obtained from CacheInfo,
 * CacheServerInfo, and PathInfo. It provides server info for a given grid path.
 * 
 * @author dpark
 * 
 */
public abstract class ServerInfo implements Comparable<ServerInfo>
{
	/**
	 * ID of the grid to which this server is member of.
	 */
	protected String gridId;

	/*
	 * Name of this server. The server name may or may not be unique depending
	 * on how the underlying data grid product is configured.
	 */
	protected String name;

	/**
	 * ID of this server. This ID uniquely identifies the server.
	 */
	protected Object id;

	/**
	 * The host name or IP address.
	 */
	protected String host;

	/**
	 * Host port number.
	 */
	protected int port;

	/**
	 * Process ID of this server.
	 */
	protected int processId;

	/**
	 * Constructs an empty ServerInfo object.
	 */
	public ServerInfo()
	{
	}

	/**
	 * Returns the ID of grid to which this server is member of.
	 */
	public String getGridId()
	{
		return gridId;
	}

	/**
	 * Sets the grid ID.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public void setGridId(String gridId)
	{
		this.gridId = gridId;
	}

	/**
	 * Returns the name of this server. The server name may or may not be unique
	 * depending on how the underlying data grid product is configured.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the server name.
	 * 
	 * @param name
	 *            Server name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the ID of this server. The returned ID uniquely identifies the
	 * server.
	 */
	public Object getId()
	{
		return id;
	}

	/**
	 * Sets the server ID.
	 * 
	 * @param id
	 *            Server ID
	 */
	public void setId(Object id)
	{
		this.id = id;
	}

	/**
	 * Returns the host name or IP address.
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Sets the host name or IP address.
	 * 
	 * @param host
	 *            Host name or IP address
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * Returns the server port number.
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * Sets the server port number.
	 * 
	 * @param port
	 *            Port number
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * Returns the process ID.
	 */
	public int getProcessId()
	{
		return processId;
	}

	/**
	 * Sets the process ID.
	 * 
	 * @param processId
	 *            Process ID
	 */
	public void setProcessId(int processId)
	{
		this.processId = processId;
	}

	/**
	 * Compares the following attributes in the order listed: name, host and id.
	 */
	@Override
	public int compareTo(ServerInfo anotherServerInfo)
	{
		int compared = -1;
		if (name != null) {
			compared = name.compareTo(anotherServerInfo.name);
		}
		if (compared == 0) {
			if (host != null) {
				compared = host.compareTo(anotherServerInfo.host);
			}
			if (compared == 0) {
				if (id != null) {
					compared = id.toString().compareTo(anotherServerInfo.id.toString());
				}
			}
		}
		return compared;
	}

	/**
	 * Returns "[name, id]".
	 */
	public String toString()
	{
		return "[" + name + "," + id + "]";
	}
}
