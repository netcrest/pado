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

import java.lang.reflect.Method;
import java.util.Map;

import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.temporal.TemporalEntry;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class TemporalEntryComparator extends DynamicComparator
{
	/**
	 * For POJO objects.
	 * @param method
	 * @param sortAsc
	 */
	public TemporalEntryComparator(Method method, boolean sortAsc)
	{
		this.method = method;
		this.sortAsc = sortAsc;
		if (method != null) {
			returnType = method.getReturnType();
		}
	}
	
	/**
	 * For Map objects.
	 * 
	 * @param fieldName
	 * @param sortAsc
	 */
	public TemporalEntryComparator(String fieldName, boolean sortAsc, TemporalEntry te, boolean sortKey)
	{
		this.fieldName = fieldName;
		this.sortAsc = sortAsc;
		this.sortKey = sortKey;
		
		if (sortKey) {
			if (te.getTemporalKey().getIdentityKey() != null) {
				if (fieldName.equals("WrittenTime") || fieldName.equals("EndWrittenTime") || fieldName.equals("StartValidTime") || fieldName.equals("EndValidTime")) {
					returnType = Long.class;
				} else if (fieldName.equals("Username")) {
					returnType = String.class;
				} else {
					returnType = te.getTemporalKey().getIdentityKey().getClass();
				}
			}
		} else {
			Object obj = null;
			if (te.getTemporalData() != null) {
				obj = te.getTemporalData().getValue();
				if (obj instanceof KeyMap) {
					KeyType keyType = ((KeyMap)obj).getKeyType();
					if (keyType != null) {
						keyType = keyType.getKeyType(fieldName);
					}
					if (keyType != null) {
						returnType = keyType.getType();
					}
				}
				if (returnType == null) {
					if (obj instanceof Map) {
						Object value = ((Map)obj).get(fieldName);
						if (value == null) {
							if ("IdentityKey".equals(fieldName)) {
								returnType = te.getTemporalKey().getIdentityKey().getClass();
							}
						} else {
							returnType = value.getClass();
						}
					} else {
						this.method = BaseComparatorFactory.getMethod(obj.getClass(), fieldName);
					}
				}
			}
		}
	}

	public int compare(Object o1, Object o2)
	{
		return super.compareTemporalEntry((TemporalEntry) o1, (TemporalEntry) (o2));
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