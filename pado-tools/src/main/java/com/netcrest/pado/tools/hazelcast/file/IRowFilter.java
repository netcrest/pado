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
package com.netcrest.pado.tools.hazelcast.file;

/**
 * IRowFilter filters row tokens constructed by the file loader which first
 * invokes filterKey() followed by filterValue() per row of data. Each method's
 * returned object is used as the respective key or value into the grid. If an
 * implementation class of this interface is specified in the schema file then 
 * the file loader will not create keys and values of its own and defers that
 * duty to the implementation class.
 * 
 * @author dpark
 * 
 */
public interface IRowFilter
{
	/**
	 * Filters the specified tokens and returns a key object to be put into the
	 * grid. This method is always invoked first before
	 * {@link #filterValue(String[])}.
	 * 
	 * @param tokens
	 *            An array of tokens representing a row read from the data file.
	 * @return A Serializable key object. If null, then this key is
	 *         discarded (it will not be put into the grid.)
	 */
	Object filterKey(String[] tokens);

	/**
	 * Filters the specified tokens and returns a value object to be put into the
	 * grid. This method is invoked only if {@link #filterKey(String[])} returns a
	 * non-null object.
	 * 
	 * @param tokens
	 *            An array of tokens representing a row read from the data file.
	 * @return A Serializable value object. If null, then this value is
	 *         discarded (it will not be put into the grid.)
	 */
	Object filterValue(String[] tokens);
}