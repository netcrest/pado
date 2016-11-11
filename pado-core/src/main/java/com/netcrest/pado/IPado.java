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

import com.netcrest.pado.info.GridInfo;

/**
 * IPado is the client API for accessing the grid that the user has successfully
 * logged in by invoking {@link Pado#login(String, String, String, char[])}.
 * Note that the user must pass in an app ID and a user name in order to login.
 * Once an instance of IPado is obtained from the login invocation, the user has
 * access to the IBiz catalog which contains app and user specific IBiz classes.
 * 
 * @author dpark
 * 
 */
public interface IPado
{
	/**
	 * Returns the catalog of IGo classes and app specifics.
	 */
	public ICatalog getCatalog();

	/**
	 * Returns the user name.
	 */
	public String getUsername();

	/**
	 * Logs out the user. The Pado instance is no longer useful after this call.
	 */
	public void logout();

	/**
	 * Returns true if logged out. A logged out IPado instance is invalid and
	 * non-operational.
	 */
	public boolean isLoggedOut();

	/**
	 * Returns the token used by the grid to manage the login session. This
	 * token uniquely represents the Pado instance.
	 */
	public Object getToken();

	/**
	 * Returns the app ID provided during login.
	 */
	public String getAppId();

	/**
	 * Returns the logged-on grid ID.
	 */
	public String getGridId();

	/**
	 * Returns a GridInfo object of the specified grid ID.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public GridInfo getGridInfo(String gridId);

	/**
	 * Returns the number of running servers in the specified grid.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public int getServerCount(String gridId);

	/**
	 * Returns all server IDs that uniquely identify the servers in the
	 * specified grid.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public Object[] getServerIds(String gridId);

	/**
	 * Refreshes the IPado instance for synchronizing internals. The Pado grid
	 * automatically pushes out grid updates that are non-disruptive to clients.
	 * For disruptive changes, it is client's responsibility to sync up with the
	 * grid by invoking this method. Disruptive changes may involve IBiz hot
	 * deployment, reconfiguration of grid paths, etc.
	 */
	public void refresh();

	/**
	 * Adds a listener to listen on Pado messages. Each grid may send
	 * system-level messages such as alerts and announcements that may be of
	 * importance to clients.
	 * 
	 * @param listener
	 *            A Pado message listener
	 */
	public void addMessageListener(IMessageListener listener);

	/**
	 * Removes the specified listener from the message listener list. The
	 * removed listener no longer receives Pado messages after this call.
	 * 
	 * @param listener
	 *            The Pado message listener to be removed.
	 */
	public void removeMessageListener(IMessageListener listener);

	/**
	 * Returns true if the specified path is a virtual path.
	 * 
	 * @param virtualPath
	 *            Virtual path
	 */
	public boolean isVirtualPath(String virtualPath);

	/**
	 * Sets the specified user data that belongs to this Pado session. User data
	 * is automatically removed from this IPado instance when the session
	 * terminates due to logout or expiration. This frees the application from
	 * cleaning up session-specific data upon session termination. To remove
	 * user data, pass in null.
	 * 
	 * @param userData
	 *            Any session specific user data.
	 */
	public void setUserData(Object userData);

	/**
	 * Returns user data. Null if not set.
	 */
	public Object getUserData();

	/**
	 * Resets the session expiration (idle) timeout. IPado instances expire
	 * after the idle time period configurable by the grid. The default timeout
	 * is 15 min. If no activities occur within the idle timeout period then the
	 * grid automatically terminates the session, effectively invalidating the
	 * corresponding IPado instance. Applications can keep sessions alive by
	 * invoking this method.
	 */
	public void resetSessionIdleTimeout();
}
