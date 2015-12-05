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
package com.netcrest.pado.context;

import com.netcrest.pado.IUserPrincipal;

/**
 * IUserInfo provides user information required by the grid to validate access
 * rights.
 * 
 * @author dpark
 * 
 */
public interface IUserInfo
{
	/**
	 * Clears all attributes.
	 */
	public void reset();

	/**
	 * Returns the value of the specified attribute key.
	 * 
	 * @param key
	 *            Attribute name
	 * @return Returns null if the specified key is not defined.
	 */
	public Object getAttribute(String key);

	/**
	 * Sets the specified key/value entry.
	 * 
	 * @param key
	 *            Attribute name
	 * @param value
	 *            Attribute value
	 */
	public void setAttribute(String key, Object value);
	
	/**
	 * Returns the IUserPrincial object that provides a list of allowed
	 * grid operations for this user. This method returns null if client or
	 * authentication is disabled.
	 * 
	 * @return
	 */
	IUserPrincipal getUserPrincipal();
	
	/** 
	 * Sets the IUserPrincial objects that provides a list of allowed
	 * grid operations for this user. This method should never be invoked
	 * directly by the application.
	 * @param userPrincipal User principal.
	 */
	void setUserPrincipal(IUserPrincipal userPrincipal);
}
