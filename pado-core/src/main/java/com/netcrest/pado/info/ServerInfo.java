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
	 * MAC (hardware) address
	 */
	protected String macAddress;

	/**
	 * Host IPv4 address
	 */
	protected String hostAddress;

	/**
	 * Canonical host name, i.e., the fully qualified host name that includes
	 * the domain name
	 */
	protected String canonicalHostName;

	/**
	 * Network interface name
	 */
	protected String networkInterfaceName;

	/**
	 * Network interface display name
	 */
	protected String networkInterfaceDisplayName;

	/**
	 * Maximum Transmission Unit (MTU) for this network interface
	 */
	protected int mtu;

	/**
	 * IPv6 address
	 */
	protected String ipv6Address;

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
	 * Returns the MAC address
	 */
	public String getMacAddress()
	{
		return macAddress;
	}

	/**
	 * Sets the MAC address
	 * 
	 * @param macAddress
	 *            MAC address
	 */
	public void setMacAddress(String macAddress)
	{
		this.macAddress = macAddress;
	}

	/**
	 * Returns the IPv4 host address
	 */
	public String getHostAddress()
	{
		return hostAddress;
	}

	/**
	 * Sets the IPv4 host address
	 * 
	 * @param hostAddress
	 *            host address
	 */
	public void setHostAddress(String hostAddress)
	{
		this.hostAddress = hostAddress;
	}

	/**
	 * Returns the canonical host name, i.e., the fully qualified name that
	 * includes the domain name. Note that if the domain name is not included
	 * then the domain name is not defined for the host.
	 */
	public String getCanonicalHostName()
	{
		return canonicalHostName;
	}

	/**
	 * Sets the canonical host name.
	 * 
	 * @param canonicalHostName
	 *            Canonical host name
	 */
	public void setCanonicalHostName(String canonicalHostName)
	{
		this.canonicalHostName = canonicalHostName;
	}

	/**
	 * Returns the network interface name
	 * 
	 * @return
	 */
	public String getNetworkInterfaceName()
	{
		return networkInterfaceName;
	}

	/**
	 * Sets the network interface name
	 * 
	 * @param networkInterfaceName
	 */
	public void setNetworkInterfaceName(String networkInterfaceName)
	{
		this.networkInterfaceName = networkInterfaceName;
	}

	/**
	 * Returns the network interface display name
	 */
	public String getNetworkInterfaceDisplayName()
	{
		return networkInterfaceDisplayName;
	}

	/**
	 * Sets the network interface display name
	 * 
	 * @param networkInterfaceDisplayName
	 *            Network interface display name
	 */
	public void setNetworkInterfaceDisplayName(String networkInterfaceDisplayName)
	{
		this.networkInterfaceDisplayName = networkInterfaceDisplayName;
	}

	/**
	 * Returns the Maximum Transmission Unit (MTU) for this network interface
	 */
	public int getMtu()
	{
		return mtu;
	}

	/**
	 * Sets the Maximum Transmission Unit (MTU) for this network interface
	 * 
	 * @param mtu
	 *            Maximum Transmission Unit
	 */
	public void setMtu(int mtu)
	{
		this.mtu = mtu;
	}

	/**
	 * Returns the IPv6 address
	 */
	public String getIpv6Address()
	{
		return ipv6Address;
	}

	/**
	 * Sets the IPv6 address
	 * 
	 * @param ipv6Address
	 *            IPv6 address
	 */
	public void setIpv6Address(String ipv6Address)
	{
		this.ipv6Address = ipv6Address;
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
