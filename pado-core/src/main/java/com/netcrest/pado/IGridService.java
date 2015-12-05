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

/**
 * IGridService provides grid information needed by the underlying data grid
 * plug-in mechanism that provides the IBiz invocation facility.
 * 
 * @author dpark
 * 
 */
public interface IGridService
{
	/**
	 * Returns the ID of the target grid to which the IBiz method invocation
	 * should be made.
	 */
	String getGridId();

	/**
	 * Returns the app ID.
	 */
	String getAppId();

	/**
	 * Returns the session token that must be embedded in each IBiz method
	 * invocation request.
	 */
	Object getToken();

	/**
	 * Returns the user name.
	 */
	String getUsername();

//	/**
//	 * Returns true if this client is connected to the specified grid. false
//	 * means this client is currently disconnected from the specified grid
//	 * either because the grid is down or the grid has been detached from this
//	 * client.
//	 */
//	boolean isConnected(String gridId);
}
