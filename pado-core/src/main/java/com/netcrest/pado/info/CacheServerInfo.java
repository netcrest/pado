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
 * CacheServerInfo provides server transport layer information such as port
 * number, bind address, etc.
 * 
 * @author dpark
 * 
 */
public abstract class CacheServerInfo
{
	/**
	 * Cache server port number
	 */
	protected int port;
	
	/**
	 * Cache server bind address
	 */
	protected String bindAddress;
	
	/**
	 * Host name used by the clients to connect to the cache server
	 */
	protected String hostnameForClients;

	/**
	 * Returns the bind address to which the server listens on client.
	 */
	public String getBindAddress()
	{
		return bindAddress;
	}

	/**
	 * Sets the bind address to which the server listens on clients.
	 * 
	 * @param bindAddress
	 *            Bind address
	 */
	public void setBindAddress(String bindAddress)
	{
		this.bindAddress = bindAddress;
	}

	/**
	 * Returns the host name or IP address for clients to connect.
	 */
	public String getHostnameForClients()
	{
		return hostnameForClients;
	}

	/**
	 * Sets the host name or IP address to which clients to connect.
	 * 
	 * @param hostnameForClients
	 *            Host name for clients to connect
	 */
	public void setHostnameForClients(String hostnameForClients)
	{
		this.hostnameForClients = hostnameForClients;
	}

	/**
	 * Returns the port number to which clients to connect.
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * Sets the port number to which clients to connect.
	 * 
	 * @param port
	 *            Port number
	 */
	public void setPort(int port)
	{
		this.port = port;
	}
}
