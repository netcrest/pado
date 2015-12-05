package com.netcrest.pado.temporal.test.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;

/**
 * @pado 1:-1:0 -6575843401283452612:-8582819533056432217
 * @version 1 dpark:03/17/14.18.42.41.EDT
 * @padocodegen Updated Mon Mar 17 18:42:41 EDT 2014
 */
public enum Account implements KeyType {
	/**
	 * AccountId: <b>String</b>
	 */
	KAccountId("AccountId", String.class, false, true, "", 0),
	/**
	 * AccountName: <b>String</b>
	 */
	KAccountName("AccountName", String.class, false, true, "", 0),
	/**
	 * LastName: <b>String</b>
	 */
	KLastName("LastName", String.class, false, true, "", 0),
	/**
	 * FirstName: <b>String</b>
	 */
	KFirstName("FirstName", String.class, false, true, "", 0),
	/**
	 * Address: <b>String</b>
	 */
	KAddress("Address", String.class, false, true, "", 0),
	/**
	 * BankId: <b>String</b>
	 */
	KBankId("BankId", String.class, false, false, "bank", 1);

	private static final Object ID = new UUID(-6575843401283452612L,
			-8582819533056432217L);
	private static final int VERSION = 1;
	private static int keyIndex;
	private static boolean payloadKeepSerialized;
	private static Class<?> domainClass;
	private static KeyType references[] = new KeyType[] { KBankId };

	static {
		try {
			domainClass = Class
					.forName("com.netcrest.pado.temporal.test.data.domain.Account");
		} catch (ClassNotFoundException ex) {
			domainClass = null;
		}
	}

	private static int getNextIndex() {
		keyIndex++;
		return keyIndex - 1;
	}

	private Account(String name, Class type, boolean isDeprecated,
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
