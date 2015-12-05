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
package com.netcrest.pado.security.server;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import com.netcrest.pado.IUserPrincipal;

/**
 * UserPrincipal contains user principal and authorization logic. This object is
 * built by LdapUserAuthenticator upon successful LDAP authentication and used
 * by LdaUserAuthorization authorizing cache operations.
 * 
 * @author dpark
 * 
 */
public class UserPrincipal implements IUserPrincipal, Principal, Serializable
{
	private static final long serialVersionUID = 1L;

	protected String username;
	protected String domain;
	protected Object token;

	protected Set<String> memberOfSet;

	protected HashSet<String> readOnlyGridPathSet = new HashSet<String>();
	protected HashSet<String> readWriteGridPathSet = new HashSet<String>();

	public UserPrincipal()
	{	
	}
	
	public UserPrincipal(String domain, String username,
			Set<String> memberOfSet)
	{
		this.domain = domain;
		this.username = username;

		// Assume memberOfSet has the list of allowed regions
		this.memberOfSet = memberOfSet;

		// Index Matrix
		readWriteGridPathSet.add("__pado/index");

		// app
		readOnlyGridPathSet.add("__pado/app");

		// Iterate memberOfList to determine the allowed paths
		// example:
//		if (memberOfSet != null) {
//			for (String groupName : memberOfSet) {
//				if (groupName.indexOf("Admin") != -1) {
//					readWriteGridPathSet.add("*");
//				} else if (groupName.indexOf("Repair") != -1) {
//					readOnlyGridPathSet.add("");
//				} else if (groupName.indexOf("Warehouse") != -1) {
//					readOnlyGridPathSet.add("");
//				}
//			}
//		}
	}

	public UserPrincipal(String username, Object token,
			Set<String> memberOfList)
	{
		this(null, username, null);
		this.token = token;
	}
	
	@Override
	public String getName()
	{
		return username;
	}

	@Override
	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	@Override
	public Object getToken()
	{
		return token;
	}

	@Override
	public String toString()
	{
		return this.username;
	}

	/**
	 * Returns true if the user is a member of the specified group.
	 * 
	 * @param groupName
	 *            Group name. Case-sensitive. If null, then it returns false.
	 */
	@Override
	public boolean isMemberOf(String groupName)
	{
		if (memberOfSet == null) {
			return false;
		}
		return memberOfSet.contains(groupName);
	}

	public boolean isWriteAllowed(String fullPath)
	{
		// TODO: temporary...
		if (fullPath.indexOf("__pado") != -1) {
			return true;
		}
		if (true) {
			return true;
		}

		return readWriteGridPathSet.contains(fullPath);
	}

	public boolean isEntryWriteAllowed(String fullPath)
	{
		return true;
	}
}
