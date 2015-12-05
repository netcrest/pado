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

import java.io.Serializable;

import com.netcrest.pado.context.IUserInfo;

/**
 * IUserContext provides user context information necessary for the server to
 * determine and apply roles and rules to the user requests.
 * 
 * @author dpark
 * 
 */
public interface IUserContext extends Serializable
{
	/**
	 * Initializes the user context object. This method is invoked internally
	 * from the client-side just before executing an IBiz method. Invoking this
	 * method by application has no effect.
	 * 
	 * @param gridService
	 *            Grid service
	 */
	void initialize(IGridService gridService);

	/**
	 * Returns the session token obtained from login.
	 */
	Object getToken();

	/**
	 * Returns the user name.
	 */
	String getUsername();

	/**
	 * Resets the user context information. This method typically clears user
	 * context or reinstates it to the original state.
	 */
	void reset();
	
	/**
	 * Returns user information
	 */
	IUserInfo getUserInfo();
}
