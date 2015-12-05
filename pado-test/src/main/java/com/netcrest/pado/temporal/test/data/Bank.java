package com.netcrest.pado.temporal.test.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;

/**
 * @pado 1:-1:0 -4630091634444185827:-5808792127484104036
 * @version 1 dpark:03/10/14.13.31.50.EDT
 * @padocodegen Updated Mon Mar 10 13:31:50 EDT 2014
 */
public enum Bank implements KeyType {
	/**
	 * BankId: <b>Long</b>
	 */
	KBankId("BankId", Long.class, false, true, "", 0),
	/**
	 * BankName: <b>String</b>
	 */
	KBankName("BankName", String.class, false, true, "", 0),
	/**
	 * RoutingNumber: <b>String</b>
	 */
	KRoutingNumber("RoutingNumber", String.class, false, true, "", 0),
	/**
	 * AccountNumber: <b>String</b>
	 */
	KAccountNumber("AccountNumber", String.class, false, true, "", 0),
	/**
	 * Type: <b>Byte</b>
	 */
	KType("Type", Byte.class, false, true, "", 0);

	private static final Object ID = new UUID(-4630091634444185827L,
			-5808792127484104036L);
	private static final int VERSION = 1;
	private static int keyIndex;
	private static boolean payloadKeepSerialized;
	private static Class<?> domainClass;
	private static KeyType references[] = new KeyType[] {};

	static {
		try {
			domainClass = Class
					.forName("com.netcrest.pado.temporal.test.data.domain.Bank");
		} catch (ClassNotFoundException ex) {
			domainClass = null;
		}
	}

	private static int getNextIndex() {
		keyIndex++;
		return keyIndex - 1;
	}

	private Bank(String name, Class type, boolean isDeprecated,
			boolean keyKeepSerialized, String path, int depth) {
		this.index = getNextIndex();
		this.name = name;
		this.type = type;
		this.isDeprecated = isDeprecated;
		this.keyKeepSerialized = keyKeepSerialized;
		this.query = path;
		this.depth = depth;
	}

	private int index;
	private String name;
	private Class type;
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

	public Class getType() {
		return type;
	}

	private static final Map<String, KeyType> keyNameMap;
	private static final int[] deprecatedIndexes;

	static {
		KeyType values[] = values();
		HashMap<String, KeyType> map = new HashMap<String, KeyType>(
				values.length + 1, 1f);
		List<Integer> list = new ArrayList(values.length);
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
		return +-1;
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
		String query = KeyTypeManager.getQuery(this);
		if (query == null) {
			return this.query;
		} 
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getDepth() {
		int depth = KeyTypeManager.getDepth(this);
		if (depth == -1) {
			return this.depth;
		}
		return depth;
	}


	public void setDepth(int depth) {
		this.depth = depth;
	}

}
