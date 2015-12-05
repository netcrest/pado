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

import com.netcrest.pado.index.exception.GridQueryException;

public interface IGridQueryService
{
	/**
	 * Executes the specified query and returns the result set. The result
	 * set is not ordered and the fetch size is determined by the underlying
	 * algorithm based on the total number of rows.
	 * 
	 * @param queryString OQL query string
	 * @return Scrollable result set
	 */
	IScrollableResultSet<?> query(GridQuery criteria) throws GridQueryException;

}
