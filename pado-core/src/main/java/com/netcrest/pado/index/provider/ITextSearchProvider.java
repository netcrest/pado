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
package com.netcrest.pado.index.provider;

import java.util.List;

import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.temporal.TemporalEntry;

/**
 * The Provider provides text mining specific algorithm to integrate with
 * <code>IIndexMatrixProvider</code>
 * 
 * @see IIndexMatrixProvider
 * 
 */
@SuppressWarnings({ "rawtypes" })
public interface ITextSearchProvider
{

	/**
	 * Returns a list of temporal entries.
	 * 
	 * @param criteria
	 *            Index matrix query criteria
	 */
	List<TemporalEntry> searchTemporal(GridQuery criteria);

	/**
	 * Combine the results from distributed servers and sort for TopN, the
	 * element on both the entities and return is
	 * <code>com.netcrest.pado.index.result.IndexableResult</code>
	 * 
	 * @param criteria
	 *            Index matrix query criteria
	 * @param entities
	 *            Entites to sort
	 * @param isMember
	 *            True if the list is member result, false if the list is merged
	 *            result
	 */
	List<?> combineAndSort(List<?> entities, GridQuery criteria, boolean isMember);

}
