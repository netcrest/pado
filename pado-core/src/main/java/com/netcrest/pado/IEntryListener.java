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
 * IEntryListener listens on inserts, updates and deletes made to the grid path.
 * 
 * @author dpark
 * 
 */
public interface IEntryListener<K, V>
{
	/**
	 * Invoked when a new entry is created in the grid path.
	 * @param event EntryEvent containing the created entry
	 */
	void onCreate(EntryEvent<K, V> event);

	/**
	 * Invoked when an entry is updated in the grid path.
	 * @param event EntryEvent containing the updated entry
	 */
	void onUpdate(EntryEvent<K, V> event);

	/**
	 * Invoked when an entry is removed from the grid path.
	 * @param event EntryEvent containing the removed entry
	 */
	void onRemove(EntryEvent<K, V> event);

	/**
	 * Inovked when an entry is invalidated from the grid path.
	 * @param event EntryEvent containing the invalidated entry
	 */
	void onInvalidate(EntryEvent<K, V> event);
}
