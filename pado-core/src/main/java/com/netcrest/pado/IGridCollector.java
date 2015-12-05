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

import java.util.List;

/**
 * IGridCollector aggregates IBiz invocation results from one or more grids.
 * 
 * @author dpark
 * 
 * @param <T>
 *            Result received from a single grid.
 * @param <S>
 *            Aggregated result returned to the IBiz caller.
 */
public interface IGridCollector<T, S>
{
	/**
	 * Adds a single grid result. This typically comes in the form of a
	 * collection, i.e., {@link List}.
	 * 
	 * @param gridId ID of the grid that provided the result.
	 * @param result The grid result.
	 */
	void addResult(String gridId, T result);

	/**
	 * Returns the final aggregated result to the IBiz caller.
	 */
	S getResult();

	/**
	 * Invoked when the last grid result is received. This method is invoked
	 * after the last call to {@link #addResult(String, Object)} is made.
	 */
	void endResults();

	/**
	 * Clears the results.
	 */
	void clearResults();
}
