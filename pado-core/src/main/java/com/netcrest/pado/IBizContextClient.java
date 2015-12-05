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

import com.netcrest.pado.biz.IBizStatistics;
import com.netcrest.pado.internal.impl.GridService;

/**
 * IBizContextClient provides biz context information relevant to
 * client-side operations.
 */
public interface IBizContextClient extends IBizContext
{
	/**
	 * Returns the grid context for setting grid attributes
	 * prior to invoking IBiz methods.
	 */
	IGridContextClient getGridContextClient();
	
	/**
	 * Returns the grid service that contains all of grids that span
	 * within this biz context.
	 */
	GridService getGridService();
	
	/**
	 * Returns the IBiz statistics object for Local implementation to record
	 * statistics.
	 */
	IBizStatistics getBizStatistics();
	
	/**
	 * Resets the entire biz context. It clears all of the context
	 * objects to revert to the original state defined by the application.
	 * Note that if reset() is not invoked, then the previous settings
	 * are carried over and applied to IBiz method invocation. This method
	 * invokes the reset methods listed under the "See Also:" tag.
	 * 
	 * @see IGridContextClient#reset()
	 * @see IUserContext#reset()
	 * @see IDataContext#reset()
	 */
	void reset();
}
