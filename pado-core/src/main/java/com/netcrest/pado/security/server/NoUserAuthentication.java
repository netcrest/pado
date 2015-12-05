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

import java.util.Properties;

import javax.naming.AuthenticationException;

import com.netcrest.pado.IUserAuthentication;
import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.internal.factory.LdapFactory;

/**
 * NoUserAuthentication is the default IUserAuthentication class that allows all
 * users to login to the grid. It provides a IUserPrincipal object that permits
 * open access to all grid operations.
 * <p>
 * By default this class is enabled. To explicitly enable it, add the following
 * as a Pado property preferably in <i>pado.properties</i>:
 * <pre>
 * class.userAuthentication=com.netcrest.pado.security.server.NoUserAuthentication
 * </pre>
 * 
 * @author dpark
 * 
 */
public class NoUserAuthentication implements IUserAuthentication
{
	public NoUserAuthentication()
	{
	}

	public IUserPrincipal authenticate(String appId, String domain, String username, char[] password, Properties props)
			throws AuthenticationException
	{
		if (username == null) {
			throw new AuthenticationException("User name cannot be null.");
		}
		return LdapFactory.getLdapFactory().createNoAuthenticationUserPrincipal(domain, username);
	}
}