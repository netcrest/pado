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
package com.netcrest.pado.util;

import com.netcrest.pado.exception.PathUndefinedException;


/**
 * IBulkLoader loads data into the grid in batches. Loading data in batches is
 * generally faster than individual "put" operations.
 * 
 * @author dpark
 * 
 * @param <K>
 *            Key
 * @param <V>
 *            Value
 */
public interface IBulkLoader<K, V>
{
	/**
	 * Sets the grid path to which the file content to be loaded. Note that
	 * invoking this method while the bulk loader already has a grid path set
	 * will first flush the bulk loader before assigning the specified grid
	 * path.
	 * 
	 * @param gridPath
	 * @throws PathUndefinedException
	 *             Thrown if the specified path is not found. If this error
	 *             occurs then the bulk loader will not have the path set and
	 *             therefore the put operation will fail.
	 */
	void setPath(String gridPath) throws PathUndefinedException;

	/**
	 * Returns the grid path. It return null if the grid path is not defined.
	 */
	String getPath();

	/**
	 * Sets the batch size. The default size is 1000.
	 * 
	 * @param batchSize
	 *            Batch size
	 */
	void setBatchSize(int batchSize);

	/**
	 * Returns the batch size. The default size is 1000.
	 */
	int getBatchSize();

	/**
	 * Puts data into the underlying container. A bulk-put is done when it
	 * reaches the batch size.
	 * 
	 * @param key
	 *            The key that maps the specified value.
	 * @param value
	 *            The value to put into the container along with the key.
	 * @throws PathUndefinedException
	 *             Thrown if the grid path is not defined.
	 */
	void put(K key, V value) throws PathUndefinedException;

	/**
	 * Removes the specified key and the mapped value from the grid.
	 * 
	 * @param key
	 *            Key to delete
	 * @throws PathUndefinedException
	 *             Thrown if the grid path is not defined.
	 */
	void remove(K key) throws PathUndefinedException;

	/**
	 * Flushes the remaining batch. This method must be invoked at the end of
	 * the load.
	 */
	void flush();
	
	/**
	 * Adds a bulk loader listener that is invoked per flush() call.
	 */
	void addBulkLoaderListener(IBulkLoaderListener listener);
	
	/**
	 * Removes the specified bulk loader listener.
	 */
	void removeBulkLoaderListener(IBulkLoaderListener listener);
}
