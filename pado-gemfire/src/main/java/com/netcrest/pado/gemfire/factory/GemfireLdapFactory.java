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
package com.netcrest.pado.gemfire.factory;

import java.util.Set;

import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.gemfire.security.GemfireLdapUserPrincipal;
import com.netcrest.pado.gemfire.security.GemfireUserPrincipal;
import com.netcrest.pado.internal.factory.LdapFactory;

/**
 * GemfireLdapFactory derives LdapFactory following the static delegation
 * pattern.
 * 
 * @author dpark
 * 
 */
public class GemfireLdapFactory extends LdapFactory
{
	static {
		ldapFactory = new GemfireLdapFactory();
	}

	/**
	 * Constructs a user principal object for the specified user.
	 * 
	 * @param domain
	 *            Domain name
	 * @param username
	 *            User name
	 * @param userDn
	 *            User distinguished name
	 * @param memberOfSet
	 *            Member of set
	 */
	public IUserPrincipal createUserPrincipal(String domain, Object username, String userDn, Set<String> memberOfSet)
	{
		return new GemfireLdapUserPrincipal(domain, username, userDn, memberOfSet);
	}

	/**
	 * Constructs a user principal object that always permits the specified user
	 * to login to the grid.
	 * 
	 * @param domain
	 *            Domain name
	 * @param username
	 *            User name
	 */
	public IUserPrincipal createNoAuthenticationUserPrincipal(String domain, Object username)
	{
		return new GemfireUserPrincipal(domain, username, null);
	}
}
