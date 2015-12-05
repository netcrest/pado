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
package com.netcrest.pado.internal.server.impl;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IMessageListener;
import com.netcrest.pado.IPado;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.server.PadoServerManager;

public class PadoServerImpl implements IPado
{
	private String username;
	private Object token;
	private ICatalog catalog;
	private Object userData;

	public PadoServerImpl(String username, Object token, ICatalog catalog)
	{
		this.username = username;
		this.token = token;
		this.catalog = catalog;
	}

	/**
	 * Returns the server-side catalog.
	 */
	@Override
	public ICatalog getCatalog()
	{
		return catalog;
	}

	/**
	 * Returns the user name.
	 */
	@Override
	public String getUsername()
	{
		return username;
	}

	/**
	 * This method has no effect. The server-side is not allowed to logout.
	 */
	@Override
	public void logout()
	{
		this.userData = null;
	}

	/**
	 * Always returns null. Token is not supported in the server side.
	 */
	@Override
	public Object getToken()
	{
		return token;
	}

	/**
	 * Returns the app ID.
	 */
	@Override
	public String getAppId()
	{
		if (catalog == null) {
			return null;
		}
		return catalog.getAppId();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getGridId()
	{
		return PadoServerManager.getPadoServerManager().getGridId();
	}

	/**
	 * Refreshes the server-side pado client interface.
	 */
	@Override
	public void refresh()
	{
	}

	/**
	 * Not supported in server
	 */
	@Override
	public void addMessageListener(IMessageListener listener)
	{
	}

	/**
	 * Not supported in server
	 */
	@Override
	public void removeMessageListener(IMessageListener listener)
	{
	}

	/**
	 * Always returns true.
	 */
	@Override
	public boolean isLoggedOut()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isVirtualPath(String virtualPath)
	{
		if (virtualPath == null) {
			return false;
		}
		return KeyTypeManager.isVirtualPathDefinition(virtualPath);
	}

	/**
	 * This method is not so useful for the server since there is only one
	 * instance of IPado in the server.
	 * @param userData Session specific user data.
	 */
	@Override
	public void setUserData(Object userData)
	{
		this.userData = userData;
	}

	/**
	 * This method is not so useful for the server since there is only one
	 * instance of IPado in the server.
	 * @return User data. Null if not set.
	 */
	@Override
	public Object getUserData()
	{
		return this.userData;
	}

	/**
	 * Not supported in server
	 */
	@Override
	public void resetSessionIdleTimeout()
	{
	}
}
