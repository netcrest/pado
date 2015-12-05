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
package com.netcrest.pado.index.service;

import com.netcrest.pado.index.gemfire.service.GemfireGridQuery;

/**
 * GridQueryFactory creates {@link GridQuery} objects specific to the underlying
 * data grid.
 * 
 * @author dpark
 * 
 */
public class GridQueryFactory
{
	/**
	 * Creates a new GridQuery object.
	 */
	public static GridQuery createGridQuery()
	{
		return new GemfireGridQuery();
	}
}
