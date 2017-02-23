package com.netcrest.pado.demo.nw.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;

/**
 * @pado 1:-1:1 534019392027837140:-8143058111814274128
 * @version 1 pi:01/22/17.14.47.24.EST
 * @padocodegen Updated Sun Jan 22 14:47:24 EST 2017
 */
public enum OrderKey implements KeyType 
{
	/**
	 * <b>String</b>
	 */
	KOrderId("OrderId", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KCustomerId("CustomerId", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KEmployeeId("EmployeeId", String.class, false, true, "", 0),
	/**
	 * <b>Date</b>
	 */
	KOrderDate("OrderDate", Date.class, false, true, "", 0),
	/**
	 * <b>Date</b>
	 */
	KRequiredDate("RequiredDate", Date.class, false, true, "", 0),
	/**
	 * <b>Date</b>
	 */
	KShippedDate("ShippedDate", Date.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KShipVia("ShipVia", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KFreight("Freight", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KShipName("ShipName", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KShipAddress("ShipAddress", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KShipCity("ShipCity", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KShipRegion("ShipRegion", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KShipPostalCode("ShipPostalCode", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KShipCountry("ShipCountry", String.class, false, true, "", 0);

	private static final Object ID = new UUID(534019392027837140L, -8143058111814274128L);
	private static final int VERSION = 1;
	private static int keyIndex;
	private static boolean payloadKeepSerialized;
	private static Class<?> domainClass;
	private static KeyType references[] = new KeyType[] {};

	static {
		try {
			domainClass = Class
					.forName("com.netcrest.pado.demo.nw.data.domain.Order");
		} catch (ClassNotFoundException ex) {
			domainClass = null;
		}
	}

	private static int getNextIndex() {
		keyIndex++;
		return keyIndex - 1;
	}

	private OrderKey(String name, Class<?> type, boolean isDeprecated,
			boolean keyKeepSerialized, String query, int depth) {
		this.index = getNextIndex();
		this.name = name;
		this.type = type;
		this.isDeprecated = isDeprecated;
		this.keyKeepSerialized = keyKeepSerialized;
		this.query = query;
		this.depth = depth;
	}

	private int index;
	private String name;
	private Class<?> type;
	private boolean isDeprecated;
	private boolean keyKeepSerialized;
	private String query;
	private int depth;

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	private static final Map<String, KeyType> keyNameMap;
	private static final int[] deprecatedIndexes;

	static {
		KeyType values[] = values();
		HashMap<String, KeyType> map = new HashMap<String, KeyType>(
				values.length + 1, 1f);
		List<Integer> list = new ArrayList<Integer>(values.length);
		for (int i = 0; i < values.length; i++) {
			map.put(values[i].getName(), values[i]);
			if (values[i].isKeyKeepSerialized()) {
				payloadKeepSerialized = true;
			}
			if (values[i].isDeprecated()) {
				list.add(i);
			}
		}
		keyNameMap = Collections.unmodifiableMap(map);
		deprecatedIndexes = new int[list.size()];
		for (int i = 0; i < deprecatedIndexes.length; i++) {
			deprecatedIndexes[i] = list.get(i);
		}
	}

	public Object getId() {
		return ID;
	}

	public int getMergePoint() {
		return +1;
	}

	public int getVersion() {
		return VERSION;
	}

	public int getKeyCount() {
		return values().length;
	}

	public KeyType[] getValues(int version) {
		return KeyTypeManager.getValues(this, version);
	}

	public static KeyType getKeyType() {
		return values()[0];
	}

	public KeyType getKeyType(String name) {
		return keyNameMap.get(name);
	}

	public KeyType[] getValues() {
		return values();
	}

	public boolean isDeltaEnabled() {
		return false;
	}

	public boolean isDeprecated() {
		return isDeprecated;
	}

	public int[] getDeprecatedIndexes() {
		return deprecatedIndexes;
	}

	public boolean isKeyKeepSerialized() {
		return keyKeepSerialized;
	}

	public boolean isCompressionEnabled() {
		return false;
	}

	public boolean isPayloadKeepSerialized() {
		return payloadKeepSerialized;
	}

	public Set<String> getNameSet() {
		return keyNameMap.keySet();
	}

	public boolean containsKey(String name) {
		return keyNameMap.containsKey(name);
	}

	public Class<?> getDomainClass() {
		return domainClass;
	}

	public KeyType[] getReferences() {
		return references;
	}
	
	public void setReferences(KeyType[] ref) {
		if (ref == null) {
			references = new KeyType[0];
		} else {
			references = ref;
		}
	}

	public boolean isReference() {
		return query != null && query.length() > 0;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
}
