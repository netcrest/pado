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
package com.netcrest.pado.gemfire.security;

import java.util.Properties;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AuthInitialize;
import com.gemstone.gemfire.security.AuthenticationFailedException;

/**
 * UserPasswordAuthInit initializes user authentication information
 * (user name/password) passed to LadUserAuthenticator.
 * 
 * @author dpark
 *
 */
public class UserPasswordAuthInit implements AuthInitialize
{
	public static final String USER_NAME = "security-username";

	public static final String PASSWORD = "security-password";

	protected LogWriter securitylog;

	protected LogWriter systemlog;

	public static AuthInitialize create()
	{
		return new UserPasswordAuthInit();
	}

	public UserPasswordAuthInit()
	{
		CacheFactory.getAnyInstance().getLogger().info("UserPasswordAuthInit created");
	}

	public void init(LogWriter systemLogger, LogWriter securityLogger) throws AuthenticationFailedException
	{

		CacheFactory.getAnyInstance().getLogger().info("UserPasswordAuthInit.init()");
		this.systemlog = systemLogger;
		this.securitylog = securityLogger;
	}

	public Properties getCredentials(Properties props, DistributedMember server, boolean isPeer)
			throws AuthenticationFailedException
	{

		CacheFactory.getAnyInstance().getLogger().info("UserPasswordAuthInit.getCredentials()");
		Properties newProps = new Properties();
		String userName = props.getProperty(USER_NAME);
		if (userName == null) {
			throw new AuthenticationFailedException("UserPasswordAuthInit: user name property [" + USER_NAME
					+ "] not set.");
		}
		newProps.setProperty(USER_NAME, userName);
		String passwd = props.getProperty(PASSWORD);
		// If password is not provided then use empty string as the password.
		if (passwd == null) {
			passwd = "";
		}
		newProps.setProperty(PASSWORD, passwd);
		return newProps;
	}

	public void close()
	{
	}

}
