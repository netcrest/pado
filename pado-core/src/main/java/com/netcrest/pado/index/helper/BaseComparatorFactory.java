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
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

/**
 * The ComparatorFactory configured at Cache startup time The Method to get the
 * property for comparison is configured as <class.fieldName, methodName>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BaseComparatorFactory
{

	protected static ConcurrentHashMap<String, Method> methodsMap = new ConcurrentHashMap<String, Method>();

	public BaseComparatorFactory()
	{

	}

	protected static Method getMethod(Class compClass, String field)
	{
		if (compClass == null || field == null) {
			return null;
		}
		String fieldName = field;
		char chr = fieldName.charAt(0);
		boolean upper = Character.isUpperCase(chr);
		if (!upper) {
			chr = Character.toUpperCase(chr);
			if (fieldName.length() > 1) {
				fieldName = chr + fieldName.substring(1);
			} else {
				fieldName = String.valueOf(chr);
			}
		}
		String methodName = "get" + fieldName;
		String classMethod = compClass.getName() + "." + methodName;

		Method existing = methodsMap.get(classMethod);
		if (existing == null) {
			try {
				existing = compClass.getMethod(methodName);
			} catch (Exception ex) {
				Logger.warning("Misconfigured Comparator, no method " + classMethod, ex);
			}
			if (existing != null) {
				methodsMap.put(classMethod, existing);
			}
		}
		return existing;
	}

	public Object getField(Class compClass, String field, Object obj)
	{

		Method method = getMethod(compClass, field);
		Object resultField = null;
		if (method != null) {
			try {
				resultField = method.invoke(obj);
			} catch (Exception ex) {
				Logger.warning("Misconfigured Comparator", ex);
			}
		}
		return resultField;
	}

	public Comparator getComparator(Object compObject, String field, boolean sortAsc, boolean sortKey)
	{
		if (compObject == null) {
			return null;
		}
		DynamicComparator comparator = null;
		Object value = compObject;
		if (value instanceof TemporalEntry) {
			comparator = new TemporalEntryComparator(field, sortAsc, (TemporalEntry) value, sortKey);
		} else if (value instanceof ITemporalKey) {
			comparator = new TemporalKeyComparator(sortAsc, (ITemporalKey) value);
		} else if (value instanceof ITemporalData) {
			comparator = new TemporalDataComparator(field, sortAsc, (ITemporalData) value);
		} else if (value instanceof MapComparator || value instanceof Map) {
			comparator = new MapComparator(field, sortAsc, (Map) value);
			
		} else {
			Class compClass = value.getClass();
			Method method = getMethod(compClass, field);
			if (method != null)
				comparator = new DynamicComparator(method, sortAsc);
		}
		return comparator;
	}

}
