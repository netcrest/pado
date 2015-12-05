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
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.query.Struct;
import com.netcrest.pado.index.result.ResultItem;
import com.netcrest.pado.index.result.ValueInfo;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

/**
 * The ComparatorFactory configured at Cache startup time The Method to get the
 * property for comparison is configured as <class.fieldName, methodName>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ComparatorFactory implements Declarable
{

	private static ConcurrentHashMap<String, Method> methodsMap = new ConcurrentHashMap<String, Method>();
	private static LogWriter logWriter = CacheFactory.getAnyInstance().getLogger();

	public ComparatorFactory()
	{

	}

	@Override
	public void init(Properties prop)
	{
	}

	static Method getMethod(Class compClass, String field)
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
				logWriter.warning("Misconfigured Comparator, no method " + classMethod, ex);
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
				logWriter.warning("Misconfigured Comparator", ex);
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
		if (value instanceof ResultItem) {
			ResultItem ri = (ResultItem)compObject;
			if (ri.getItem() != null) {
				value = ((ValueInfo)ri.getItem()).getValue();
			}
		}
		
		if (value instanceof TemporalEntry) {
			comparator = new TemporalEntryComparator(field, sortAsc, (TemporalEntry) value, sortKey);
		} else if (value instanceof ITemporalKey) {
			comparator = new TemporalKeyComparator(sortAsc, (ITemporalKey) value);
		} else if (value instanceof ITemporalData) {
			comparator = new TemporalDataComparator(field, sortAsc, (ITemporalData) value);
		} else if (value instanceof MapComparator || value instanceof Map) {
			comparator = new MapComparator(field, sortAsc, (Map) value);
		} else if (value instanceof Struct) {
			comparator = new StructComparator(field, sortAsc, (Struct) value);
		} else {
			Class compClass = value.getClass();
			Method method = getMethod(compClass, field);
			if (method != null)
				comparator = new DynamicComparator(method, sortAsc);
		}
		if (comparator != null && comparator.getReturnType() != null) {
			if (compObject instanceof ResultItem) {
				comparator = new ResultItemComparator(comparator);
			}
			return comparator;
		} else {
			return null;
		}
	}

}
