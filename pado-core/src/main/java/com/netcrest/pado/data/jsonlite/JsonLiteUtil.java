package com.netcrest.pado.data.jsonlite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.netcrest.pado.data.jsonlite.JsonLite;

/**
 * JsonLiteUtil provides JsonLite-specific convenience methods for manipulating
 * JsonLite object contents.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class JsonLiteUtil
{
	/**
	 * Sorts the specified list by the value of the specified order-by key.
	 * 
	 * @param list
	 *            List of JsonLte objects that contain the specified order-by
	 *            key.
	 * @param orderByKey
	 *            Order-by key to extract the mapped value from JsonLite.
	 * @return The specified list that has been sorted.
	 */
	private static final List<JsonLite> __getOrderBy(List<JsonLite> list, final String orderByKey)
	{
		if (list == null) {
			return null;
		}
		Collections.sort(list, new Comparator<JsonLite>() {
			@Override
			public int compare(JsonLite jl1, JsonLite jl2)
			{
				Comparable o1 = (Comparable) jl1.get(orderByKey);
				Comparable o2 = (Comparable) jl2.get(orderByKey);
				if (o1 == null) {
					return -1;
				} else if (o2 == null) {
					return 1;
				}
				return o1.compareTo(o2);
			}
		});
		return list;
	}

	/**
	 * Transfers first same values of the specified keys into a list. It removes
	 * matching values from the specified list.
	 * 
	 * @param list
	 *            List to search
	 * @param keys
	 *            Keys to extract the mapped values from JsonLite
	 * @return List containing same values of the specified keys.
	 */
	private static final List<JsonLite> transferEqualsList(List<JsonLite> list, String... keys)
	{
		ArrayList<JsonLite> list2 = new ArrayList<JsonLite>();
		Iterator<JsonLite> iterator = list.iterator();
		JsonLite prevJl = null;
		if (iterator.hasNext()) {
			prevJl = iterator.next();
			iterator.remove();
			list2.add(prevJl);
		}
		if (prevJl != null) {
			while (iterator.hasNext()) {
				JsonLite jl = iterator.next();
				if (isValueEqual(prevJl, jl, keys)) {
					iterator.remove();
					list2.add(jl);
				} else {
					break;
				}
			}
		}
		return list2;
	}

	/**
	 * Returns true if the specified JsonLite objects contain same values for
	 * the specified keys. It returns false if keys are not specified. It
	 * returns true if both JsonLite objects are null.
	 * 
	 * @param jl1
	 *            First JsonLite object to compare
	 * @param jl2
	 *            Second JsonLite object to comapre
	 * @param keys
	 *            Keys
	 */
	private static final boolean isValueEqual(JsonLite jl1, JsonLite jl2, String... keys)
	{
		if (keys == null) {
			return false;
		}
		boolean isEqual = true;
		if (jl1 != jl2) {
			for (String key : keys) {
				Object value1 = jl1.get(key);
				Object value2 = jl2.get(key);
				if (value1 == null) {
					if (value2 == null) {
						continue;
					} else {
						isEqual = false;
						break;
					}
				} else if (value1.equals(value2) == false) {
					isEqual = false;
					break;
				}
			}
		}
		return isEqual;
	}

	/**
	 * Sorts the specified list by the values of the specified order-by keys.
	 * 
	 * @param list
	 *            List of JsonLte objects that contain the specified order-by
	 *            key.
	 * @param orderByKeys
	 *            Order-by keys to extract the mapped values from JsonLite.
	 * @return The specified list that has been sorted.
	 */
	public static final List<JsonLite> getOrderBy(List<JsonLite> list, final String... orderByKeys)
	{
		String prevKey = null;
		if (orderByKeys == null) {
			return list;
		}
		if (orderByKeys.length > 0) {
			prevKey = orderByKeys[0];
			list = __getOrderBy(list, prevKey);
		}
		for (int i = 1; i < orderByKeys.length; i++) {
			String key = orderByKeys[i];
			ArrayList<JsonLite> list2 = new ArrayList<JsonLite>(list);
			list.clear();
			String[] keys = new String[i];
			for (int j = 0; j < keys.length; j++) {
				keys[j] = orderByKeys[j];
			}
			System.out.println();
			while (list2.isEmpty() == false) {
				List<JsonLite> transferredList = transferEqualsList(list2, keys);
				transferredList = __getOrderBy(transferredList, key);
				list.addAll(transferredList);
			}
			prevKey = key;
		}
		return list;
	}

}
