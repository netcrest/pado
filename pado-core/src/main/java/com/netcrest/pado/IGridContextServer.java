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
 * IGridContextServer contains grid context information relevant to server-side
 * operations.
 * 
 * @author dpark
 * 
 */
public interface IGridContextServer
{
	/**
	 * Returns additional arguments supplied by the client. Because of the
	 * distributed nature of grid services, for some situations, the invoked
	 * IBiz method may require extraneous arguments in addition to the IBiz
	 * method arguments in order to complete the method execution. Because the
	 * additional arguments are not part of the method signature, they must be
	 * contractually well documented.
	 */
	Object[] getAdditionalArguments();
	
	/**
	 * Returns transient data that may be used within the JVM process space.
	 */
	Object[] getTransientData();
}
