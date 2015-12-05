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
package com.netcrest.pado.index.gemfire.function;

import java.util.List;

import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.service.GridQuery;

/**
 * An interface required by any EntitySearchFunction that caches the resultSets and returns
 * the first batch to GridIndexServer
 *
 */
public interface IEntitySearchFunction  {
	
	/**
	 * Return all matched results, if none is found, an empty list is returned
	 * @param criteria
	 * @return a list of objects that are found, the objects are not necessary
	 * the objects returned to GridIndexServer
	 */
	List queryLocal (GridQuery criteria, FunctionContext context) throws GridQueryException;	
	
	/**
	 * Sort the list, called after <code>queryLocal</code> and passing the result of <code>queryLocal</code> 
	 * @param criteria
	 * @param list
	 * @throws GridQueryException
	 */
	void sort (GridQuery criteria, List list)  throws GridQueryException;	

	
	/**
	 * Save the resultSet in cache, called after sort.
	 * @param criteria
	 * @param list	The sorted list of results
	 */	
	void cacheResultSet (GridQuery criteria, List list);
	
	
	/**
	 * Get cached resultSet that is saved in <code>cacheResultSet</code>, 
	 * based on the criteria's fetch size and start index
	 * @param criteria
	 * @return
	 */
	List getCachedResultSet (GridQuery criteria);	
	
	/**
	 * Called on server side to transform the server data model to client data before sending to Index server or client
	 * @param fromData Server model 
	 * @return
	 */
	Object transformEntity (Object fromData);	
	
}
