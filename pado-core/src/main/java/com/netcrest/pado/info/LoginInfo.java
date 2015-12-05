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

import java.util.Set;

import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.Pado;

/**
 * LoginInfo provides user login information. It is internally delivered to the
 * client during a call to {@link Pado#login()}. It contains login status and
 * login session data that Pado internally utilize to initialize the app and
 * user specific catalog service.
 * 
 * @author dpark
 * 
 */
public abstract class LoginInfo
{
	/**
	 * Login status
	 * 
	 * @author dpark
	 * 
	 */
	public enum LoginStatus
	{
		/**
		 * Login is successful
		 */
		SUCCESS,

		/**
		 * Login failed
		 */
		FAILED
	}

	/**
	 * App ID
	 */
	protected String appId;

	/**
	 * Domain name
	 */
	protected String domain;

	/**
	 * User name
	 */
	protected String username;

	/**
	 * User session token
	 */
	protected Object token;

	/**
	 * Set of BizInfo objects providing IBiz details
	 */
	protected Set<BizInfo> bizSet;

	/**
	 * Grid Id
	 */
	protected String gridId;

	/**
	 * Client connection name
	 */
	protected String connectionName;

	/**
	 * Client shared connection name for creating local cache if connectionName
	 * is for multi-user
	 */
	protected String sharedConnectionName;

	/**
	 * Client locators
	 */
	protected String locators;

	/**
	 * List of all child grid IDs that are available to this user
	 */
	protected String childGridIds[];

	/**
	 * User principal object that contains user role information obtained during
	 * user login.
	 */
	protected IUserPrincipal userPrincipal;

	/**
	 * Constructs an empty LoginInfo object.
	 */
	public LoginInfo()
	{
	}

	/**
	 * Constructs a LoginInfo object with the specified app ID, user name,
	 * session token, and the set of BizInfo objects accessible by the specified
	 * user. This constructor is invoked by a server during a client login.
	 * 
	 * @param appId
	 *            App ID
	 * @param domain
	 *            Optional domain name
	 * @param username
	 *            User name
	 * @param token
	 *            Session token
	 * @param bizSet
	 *            Set of BizInfo objects
	 */
	public LoginInfo(String appId, String domain, String username, Object token, Set<BizInfo> bizSet)
	{
		this.appId = appId;
		this.domain = domain;
		this.username = username;
		this.token = token;
		this.bizSet = bizSet;
	}

	/**
	 * Returns the app ID.
	 */
	public String getAppId()
	{
		return appId;
	}

	/**
	 * Sets the app ID.
	 * 
	 * @param appId
	 *            App ID
	 */
	public void setAppId(String appId)
	{
		this.appId = appId;
	}

	/**
	 * Returns the optional domain name.
	 */
	public String getDomain()
	{
		return domain;
	}

	/**
	 * Sets the optional domain name.
	 * 
	 * @param domain
	 *            Domain name
	 */
	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	/**
	 * Returns the user name.
	 */
	public String getUsername()
	{
		return username;
	}

	/**
	 * Sets the user name
	 * 
	 * @param username
	 *            User name
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}

	/**
	 * Returns the user session token. This token is typically created by Pado.
	 */
	public Object getToken()
	{
		return token;
	}

	/**
	 * Sets the user token.
	 * 
	 * @param token
	 *            Session token
	 */
	public void setToken(Object token)
	{
		this.token = token;
	}

	/**
	 * Returns the set of BizInfo objects representing the business objects that
	 * this user is allowed to access. Pado creates the IBiz catalog based on
	 * the returned set.
	 */
	public Set<BizInfo> getBizSet()
	{
		return bizSet;
	}

	/**
	 * Sets the set of BizInfo objects that make up the app and user specific
	 * IBiz catalog.
	 * 
	 * @param bizSet
	 *            Set of BizInfo objects
	 */
	public void setBizSet(Set<BizInfo> bizSet)
	{
		this.bizSet = bizSet;
	}

	/**
	 * Returns all IDs of the child grids that the app communicates with in
	 * executing the IBiz classes provided by the catalog service.
	 */
	public String[] getChildGridIds()
	{
		return childGridIds;
	}

	/**
	 * Sets the child grid IDs.
	 * 
	 * @param childGridIds
	 *            Child grid IDs
	 */
	public void setChildGridIds(String[] childGridIds)
	{
		this.childGridIds = childGridIds;
	}

	/**
	 * Returns the grid ID of the Pado grid in which the login took place.
	 */
	public String getGridId()
	{
		return gridId;
	}

	/**
	 * Sets the ID of the Pado grid in which the login took plac.e
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public void setGridId(String gridId)
	{
		this.gridId = gridId;
	}

	/**
	 * Returns the Pado's default connection name.
	 */
	public String getConnectionName()
	{
		return connectionName;
	}

	/**
	 * Sets the Pado's default connection name.
	 * 
	 * @param connectionName
	 *            Connection name
	 */
	public void setConnectionName(String connectionName)
	{
		this.connectionName = connectionName;
	}

	/**
	 * Returns the shared connection name. The shared connection is a special
	 * connection that can be shared across multiple Pado instances without any
	 * restrictions.
	 */
	public String getSharedConnectionName()
	{
		return sharedConnectionName;
	}

	/**
	 * Sets the shared connection name. The shared connection is a special
	 * connection that can be shared across multiple Pado instances without any
	 * restrictions.
	 * 
	 * @param sharedConnectionName
	 *            Shared connection name
	 */
	public void setSharedConnectionName(String sharedConnectionName)
	{
		this.sharedConnectionName = sharedConnectionName;
	}

	/**
	 * Returns the locators. A client must first connect once to Pado via
	 * locators before it can login to Pado.
	 */
	public String getLocators()
	{
		return locators;
	}

	/**
	 * Sets the locators.
	 * 
	 * @param locators
	 *            Locators
	 */
	public void setLocators(String locators)
	{
		this.locators = locators;
	}

	/**
	 * Sets the user principal object that contains user role information.
	 * 
	 * @param userPrincipal
	 *            User principal
	 */
	public void setUserPrincipal(IUserPrincipal userPrincipal)
	{
		this.userPrincipal = userPrincipal;
	}

	/**
	 * Returns the user principal object that contains user role information.
	 */
	public IUserPrincipal getUserPrincipal()
	{
		return userPrincipal;
	}
}
