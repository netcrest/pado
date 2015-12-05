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
package com.netcrest.pado;

import java.util.Properties;

import javax.naming.AuthenticationException;

/**
 * IUserAuthentication is a plug-in interface for authenticating users during
 * the user login time. By default, Pado allows all users to login with full
 * permissions to perform all grid operations. By implementing this interface,
 * users can be properly authenticated and user roles can be assigned. For
 * ecample, Pado provides a simple LDAP implementation,
 * {@link LdapUserAuthentication}, which performs authentication and provides
 * user role information.
 * 
 * @author dpark
 * 
 */
public interface IUserAuthentication
{
	/**
	 * Authenticates the specified user name and password.
	 * 
	 * @param appId
	 *            Pado application ID.
	 * @param domain
	 *            Pado login domain name. This is typically optional.
	 * @param username
	 *            Pado login user name.
	 * @param password
	 *            Pado login user password.
	 * @param props
	 *            Additional security properties provided by the client and may
	 *            be required by the implementation class. For example, it may
	 *            contain a security signature for verification. Note that it
	 *            maybe null or empty if there are no additional properties
	 *            supplied by the client.
	 * @return IUserPrincipal object that provides user role information.
	 * @throws AuthenticationException
	 */
	IUserPrincipal authenticate(String appId, String domain, String username, char[] password, Properties props)
			throws AuthenticationException;
}
