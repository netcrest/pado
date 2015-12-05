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
 * IDataContext provides data specific context information that both client and
 * server may construct during each IBiz call for applying compliance and
 * privacy rules within Pado. Each application is responsible for implementing
 * this interface and provide necessary data specific information upon
 * encountering privacy sensitive data. IDataContext commonly provides
 * information needed in performing data filtering, masking, blocking, and etc.
 * 
 * @author dpark
 * 
 */
public interface IDataContext extends Serializable
{
	/**
	 * Initializes the data context object. This is method is invoked internally
	 * from the client-side just before executing an IBiz method. Invoking this
	 * method by application has no effect.
	 * 
	 * @param gridService
	 *            Grid service
	 */
	void initialize(IGridService gridService);

	/**
	 * Resets the data context information. This method typically clears data
	 * context or reinstates it to the original state.
	 */
	void reset();
}
