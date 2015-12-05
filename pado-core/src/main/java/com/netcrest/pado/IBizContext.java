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

/**
 * IBizContext provides user and data context objects that contain IBiz method
 * level context information.
 * 
 * @author dpark
 * 
 */
public interface IBizContext extends Serializable
{
//	/**
//	 * Returns the context state that determines the workflow in the state
//	 * machine.
//	 */
//	ContextState getState();
//	
//	void nextState();

	/**
	 * Returns the user context object that contains user specific information
	 * such as user ID, roles, etc.
	 */
	IUserContext getUserContext();

	/**
	 * Returns the data context object that contains data specific information
	 * which is determined by the application.
	 */
	IDataContext getDataContext();
}
