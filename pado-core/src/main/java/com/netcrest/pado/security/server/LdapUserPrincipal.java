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

import java.util.Set;

/**
 * LdapUserPrincipal extends UserPrincipal and contains LDAP specific info.
 * 
 * @author dpark
 * 
 */
public class LdapUserPrincipal extends UserPrincipal
{
	private static final long serialVersionUID = 1L;

	protected String userDn;

	public LdapUserPrincipal()
	{
	}

	public LdapUserPrincipal(String domain, String username, String userDn, Set<String> memberOfSet)
	{
		super(domain, username, memberOfSet);
		this.userDn = userDn;
	}

	public LdapUserPrincipal(String username, Object token, String userDn, Set<String> memberOfSet)
	{
		this(null, username, userDn, memberOfSet);
	}

	/**
	 * Returns the LDAP dn for this user.
	 */
	public String getUserDn()
	{
		return userDn;
	}

	@Override
	public String toString()
	{
		return this.userDn;
	}
}
