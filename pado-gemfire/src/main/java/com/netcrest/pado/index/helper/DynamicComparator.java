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

import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DynamicComparator<T> implements Comparator<T>
{
	// Class Members
	public static final int EQUAL = 0;
	public static final int LESS_THAN = -1;
	public static final int GREATER_THAN = 1;

	protected String fieldName;
	protected Class returnType;
	protected boolean sortKey;
	protected Method method;
	protected boolean sortAsc;
//	protected int sortBitValue = 1;

	public DynamicComparator()
	{
	}

	// Private Constructor
	public DynamicComparator(Method method, boolean sortAsc)
	{
		this.method = method;
		this.sortAsc = sortAsc;
//		sortBitValue = sortAsc ? 1 : -1;
	}
	
	public Class getReturnType()
	{
		return this.returnType;
	}

	protected int compareResults(Object result1, Object result2)
	{
		boolean r1Null = result1 == null;
		boolean r2Null = result2 == null;
		if (r1Null && r2Null)
			return EQUAL;
		if (r1Null)
			return GREATER_THAN;
		if (r2Null)
			return LESS_THAN;
		int rv = EQUAL;

		try {
			if (returnType.isAssignableFrom(Class.forName("java.util.Comparator"))) {
				java.util.Comparator c1 = (java.util.Comparator) result1;
				java.util.Comparator c2 = (java.util.Comparator) result2;
				rv = c1.compare(c1, c2);
			} else if (Class.forName("java.lang.Comparable").isAssignableFrom(returnType)) {
				java.lang.Comparable c1 = (java.lang.Comparable) result1;
				java.lang.Comparable c2 = (java.lang.Comparable) result2;
				rv = c1.compareTo(c2);
			} else if (returnType.isPrimitive()) {
				if (returnType.getName().equals("int") || returnType.getName().equals("char")) {
					int f1 = ((Number) result1).intValue();
					int f2 = ((Number) result2).intValue();
					if (f1 == f2)
						rv = EQUAL;
					else if (f1 < f2)
						rv = LESS_THAN;
					else if (f1 > f2)
						rv = GREATER_THAN;
				} else if (returnType.getName().equals("byte")) {
					byte f1 = ((Number) result1).byteValue();
					byte f2 = ((Number) result2).byteValue();
					if (f1 == f2)
						rv = EQUAL;
					else if (f1 < f2)
						rv = LESS_THAN;
					else if (f1 > f2)
						rv = GREATER_THAN;
				} else if (returnType.getName().equals("short")) {
					short f1 = ((Number) result1).shortValue();
					short f2 = ((Number) result2).shortValue();
					if (f1 == f2)
						rv = EQUAL;
					else if (f1 < f2)
						rv = LESS_THAN;
					else if (f1 > f2)
						rv = GREATER_THAN;
				} else if (returnType.getName().equals("long")) {
					long f1 = ((Number) result1).longValue();
					long f2 = ((Number) result2).longValue();
					if (f1 == f2)
						rv = EQUAL;
					else if (f1 < f2)
						rv = LESS_THAN;
					else if (f1 > f2)
						rv = GREATER_THAN;
				} else if (returnType.getName().equals("float")) {
					float f1 = ((Number) result1).floatValue();
					float f2 = ((Number) result2).floatValue();
					if (f1 == f2)
						rv = EQUAL;
					else if (f1 < f2)
						rv = LESS_THAN;
					else if (f1 > f2)
						rv = GREATER_THAN;
				} else if (returnType.getName().equals("double")) {
					double f1 = ((Number) result1).doubleValue();
					double f2 = ((Number) result2).doubleValue();
					if (f1 == f2)
						rv = EQUAL;
					else if (f1 < f2)
						rv = LESS_THAN;
					else if (f1 > f2)
						rv = GREATER_THAN;
				} else if (returnType.getName().equals("boolean")) {
					Boolean f1 = (Boolean) result1;
					Boolean f2 = (Boolean) result2;
					rv = f1.compareTo(f2);
				}
			} else {
				throw new RuntimeException("DynamicComparator does not currently support ''" + returnType.getName()
						+ "''!");
			}
		} catch (Exception nsme) {
			System.out.println("Error " + nsme);
		}
		return rv * getSortOrder();
	}

	protected int compareTemporalEntry(TemporalEntry te1, TemporalEntry te2)
	{
		if (te1 == null && te2 == null) {
			return EQUAL;
		} else if (te1 == null) {
			return GREATER_THAN;
		} else if (te2 == null) {
			return LESS_THAN;
		}
		if (sortKey) {
			return compareTemporalKey(te1.getTemporalKey(), te2.getTemporalKey());
		} else {
			return compareTemporalData(te1.getTemporalData(), te2.getTemporalData());
		}
	}

	protected int compareTemporalKey(ITemporalKey tk1, ITemporalKey tk2)
	{
		if (tk1 == null) {
			return LESS_THAN;
		} else if (tk2 == null) {
			return LESS_THAN;
		} else if (tk1 == tk2) {
			return EQUAL;
		} else {
			if (fieldName == null) {
				// IdentityKey
				return compareResults(tk1.getIdentityKey(), tk2.getIdentityKey());
			} else if (fieldName.equals("Username")) {
				return tk1.getUsername().compareTo(tk2.getUsername()) * getSortOrder();
			} else if (fieldName.equals("WrittenTime") || fieldName.equals("EndWrittenTime")) {
//				return compareLong(tk1.getWrittenTime(), tk2.getWrittenTime());
				return compareResults(tk1.getWrittenTime(), tk2.getWrittenTime());
			} else if (fieldName.equals("StartValidTime")) {
//				return compareLong(tk1.getStartValidTime(), tk2.getStartValidTime());
				return compareResults(tk1.getStartValidTime(), tk2.getStartValidTime());
			} else if (fieldName.equals("EndValidTime")) {
//				return compareLong(tk1.getEndValidTime(), tk2.getEndValidTime());
				return compareResults(tk1.getEndValidTime(), tk2.getEndValidTime());
			} else {
				// IdentityKey
				return compareResults(tk1.getIdentityKey(), tk2.getIdentityKey());
			}
		}
	}
	
	protected int compareLong(long l1, long l2)
	{
		if (l1 > l2) {
			return 1 * getSortOrder();
		} else if (l1 < l2) {
			return -1 * getSortOrder();
		} else {
			return 0;
		}
	}

	protected int compareTemporalData(ITemporalData td1, ITemporalData td2)
	{
		if (td1 == null && td2 == null) {
			return EQUAL;
		} else if (td1 == null) {
			return GREATER_THAN;
		} else if (td2 == null) {
			return LESS_THAN;
		}
		Object obj1 = null;
		Object obj2 = null;
		if (td1 instanceof GemfireTemporalData) {
			obj1 = ((GemfireTemporalData) td1).getValue();
		}
		if (td2 instanceof GemfireTemporalData) {
			obj2 = ((GemfireTemporalData) td2).getValue();
		}
		if (obj1 == null && obj2 == null) {
			return EQUAL;
		} else if (obj1 == null) {
			return GREATER_THAN;
		} else if (obj2 == null) {
			return LESS_THAN;
		}
		if (obj1 instanceof Map && obj2 instanceof Map) {
			return compareMap((Map) obj1, (Map) obj2);
		} else {
			return EQUAL;
		}
	}

	protected int compareMap(Map<String, Object> map1, Map<String, Object> map2)
	{
		if (map1 == null && map2 == null) {
			return EQUAL;
		} else if (map1 == null) {
			return GREATER_THAN;
		} else if (map2 == null) {
			return LESS_THAN;
		}
		Object result1 = map1.get(fieldName);
		Object result2 = map2.get(fieldName);
		return compareResults(result1, result2);
	}

	// // Sort invocation starts here
	// public static void sort(List list, String methodName, boolean sortAsc) {
	// Collections
	// .sort(list, new DynamicComparator(methodName, sortAsc));
	// }

	// Compare for Collections.sort here
	public int compare(T o1, T o2)
	{
		boolean o1Null = o1 == null;
		boolean o2Null = o2 == null;
		if (o1Null && o2Null)
			return EQUAL;
		if (o1Null)
			return GREATER_THAN;
		if (o2Null)
			return LESS_THAN;

		try {
			// Invoke method to gather two comparable objects
			java.lang.Object result1 = method.invoke(o1);
			java.lang.Object result2 = method.invoke(o2);
			return compareResults(result1, result2);
		} catch (Exception ex) {
			Logger.warning(ex.getMessage());
			return EQUAL;
		}
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

	// Sort order getter
	protected int getSortOrder()
	{
		return sortAsc ? GREATER_THAN : LESS_THAN;
	}
}