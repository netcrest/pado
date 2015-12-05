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

/**
 * EntryEvent is fired when an entry in the grid path has been created, updated,
 * removed or invalidated.
 * 
 * @author dpark
 * 
 * @param <K> Key object
 * @param <V> Value object
 */
public abstract class EntryEvent<K, V>
{
	/**
	 * Returns the ID of the grid that triggered this event.
	 */
	public abstract String getGridId();
	
	/**
	 * Returns the key that caused this event.
	 */
	public abstract K getKey();

	/**
	 * Returns the value associated with the key.
	 */
	public abstract V getValue();

	/**
	 * Returns the old key in the map before the new key was issued. This method
	 * may always return null if the underlying data grid has no support for old
	 * keys.
	 */
	public abstract K getOldKey();

	/**
	 * Returns the old value if exists. It may return null if the old value does
	 * not exist or the old value must be retrieved from the remote cache.
	 */
	public abstract V getOldValue();

}
