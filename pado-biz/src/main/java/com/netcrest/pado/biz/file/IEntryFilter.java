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
package com.netcrest.pado.biz.file;

import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;

/**
 * IEntryFilter filters key/value entries created by the file loader which first
 * invokes filterKey() followed by filterValue() per row of data. Each method's
 * returned object is used as the respective key or value into the grid.
 * 
 * @author dpark
 * 
 */
public interface IEntryFilter
{
	/**
	 * Filters the specified entry and returns an entry object to be put into
	 * the grid. Typically, the returned entry is the same input entry with key
	 * and/or value filtered.
	 * 
	 * @param entry
	 *            Entry to filter. Both key and value are the actual objects to
	 *            be put into the grid. If temporal, then the key and value have
	 *            the types, {@link ITemporalKey} and {@link ITemporalData},
	 *            respectively.
	 * @return Final entry to be put into the grid. If any of entry, key, or
	 *         value is null, then the entry is discarded (it will not be put
	 *         into the grid.)
	 */
	IEntryFilter.Entry filterEntry(IEntryFilter.Entry entry);

	/**
	 * Filter entry key/value pairs
	 * @author dpark
	 *
	 */
	public static class Entry
	{
		private Object key;
		private Object value;

		public Entry(Object key, Object value)
		{
			this.key = key;
			this.value = value;
		}

		public Object getKey()
		{
			return key;
		}

		public void setKey(Object key)
		{
			this.key = key;
		}

		public Object getValue()
		{
			return value;
		}

		public void setValue(Object value)
		{
			this.value = value;
		}
	}
}