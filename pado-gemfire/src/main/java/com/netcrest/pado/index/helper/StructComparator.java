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

import com.gemstone.gemfire.cache.query.Struct;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class StructComparator extends DynamicComparator
{
	/**
	 * For GemFire Struct objects.
	 * 
	 * @param fieldName
	 * @param sortAsc
	 */
	public StructComparator(String fieldName, boolean sortAsc, Struct struct)
	{
		this.fieldName = fieldName;
		this.sortAsc = sortAsc;
		if (struct != null) {
			Object value = struct.get(fieldName);
			if (value != null) {
				returnType = value.getClass();
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
		return compareStruct((Struct) o1, (Struct) o2);
	}
	
	protected int compareStruct(Struct struct1, Struct struct2)
	{
		if (struct1 == null && struct2 == null) {
			return EQUAL;
		} else if (struct1 == null) {
			return GREATER_THAN;
		} else if (struct2 == null) {
			return LESS_THAN;
		}
		Object result1 = struct1.get(fieldName);
		Object result2 = struct2.get(fieldName);
		return compareResults(result1, result2);
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