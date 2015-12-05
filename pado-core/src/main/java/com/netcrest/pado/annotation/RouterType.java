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
package com.netcrest.pado.annotation;

/**
 * RouterType defines the method of determining the target grid. Except for 
 * NORMAL, the router type always targets a single grid. NORMAL targets all
 * specified grids. The default is NORMAL. RouterType is typically used with 
 * {@link OnServer}.
 * @author dpark
 *
 */
public enum RouterType
{
	/**
	 * ALL targets all specified grids. 
	 */
	ALL,
	
	/**
	 * COST targets a single grid with the least latency cost. COST is default.
	 */
	COST, 
	
	/**
	 * LOCATION targets a single grid at a specified location.
	 */
	LOCATION, 
	
	/**
	 * LOAD targets a single grid with the least system/application load.
	 */
	LOAD;
}
