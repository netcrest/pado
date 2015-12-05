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
package com.netcrest.pado.internal.factory;

import java.lang.reflect.Method;
import java.util.Set;

import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

/**
 * LdapFactory creates LDAP-related objects. It adheres to the static delegation
 * pattern.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class LdapFactory
{
	protected static LdapFactory ldapFactory;

	static {
		try {
			Class clazz = PadoUtil.getClass(Constants.PROP_CLASS_LDAP_FACTORY, Constants.DEFAULT_CLASS_LDAP_FACTORY);

			Method method = clazz.getMethod("getLdapFactory");
			try {
				ldapFactory = (LdapFactory) method.invoke(null);
			} catch (Exception e) {
				Logger.severe("LdapFactory creation error", e);
			}
		} catch (Exception e) {
			Logger.severe("LdapFactory creation error", e);
		}
	}

	/**
	 * Returns the singleton LdapFactory object that delegates static
	 * method calls to the underlying data grid factory implementation object.
	 */
	public static LdapFactory getLdapFactory()
	{
		return ldapFactory;
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
		return ldapFactory.createUserPrincipal(domain, username, userDn, memberOfSet);
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
		return ldapFactory.createNoAuthenticationUserPrincipal(domain, username);
	}
}
