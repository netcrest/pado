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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OnServer annotates an IBiz method to execute on a single server or all
 * servers in a single grid or multiple grids.
 * 
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OnServer
{

	/**
	 * Returns the name of the connection to the grid.
	 */
	String connectionName() default "";

	/**
	 * Returns true if method should be invoked on all servers in the grid,
	 * false if executed on a single server.The default is false.
	 */
	boolean broadcast() default false;

	/**
	 * Returns true if method should be invoked on all grids, false if executed
	 * on a single grid or specified grids. The default is false.
	 */
	boolean broadcastGrids() default false;

	/**
	 * Returns the grid router type for determining the routing method from the
	 * routing table. The default is {@link RouterType#COST}.
	 */
	RouterType routerType() default RouterType.COST;

}
