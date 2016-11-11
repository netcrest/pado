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
package com.netcrest.pado.index.helper;

import java.util.Map;

import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.temporal.ITemporalData;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class TemporalDataComparator extends DynamicComparator
{
	/**
	 * For Map objects.
	 * 
	 * @param fieldName
	 * @param sortAsc
	 */
	public TemporalDataComparator(String fieldName, boolean sortAsc, ITemporalData data)
	{
		this.fieldName = fieldName;
		this.sortAsc = sortAsc;
		if (data != null) {
			Object value = data.getValue();
			if (value instanceof KeyMap) {
				KeyType keyType = ((KeyMap) value).getKeyType();
				if (keyType != null) {
					keyType = keyType.getKeyType(fieldName);
				}
				if (keyType != null) {
					returnType = keyType.getType();
				}
			}
			if (returnType == null && value instanceof Map) {
				value = ((Map) value).get(fieldName);
				if (value != null) {
					returnType = value.getClass();
				}
			}
		}
	}

	public int compare(Object o1, Object o2)
	{
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 == null) {
			return 1;
		} else if (o2 == null) {
			return -1;
		}
		return super.compareTemporalData((ITemporalData) o1, (ITemporalData) o2);
	}

	/**
	 * Not used for sorting. Only here to meet the requirements of the
	 * Comparator interface.
	 * 
	 * @param o
	 *            The object for comparison
	 * @return boolean
	 */
	public boolean equals(Object o)
	{
		return true;
	}
}