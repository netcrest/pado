package com.netcrest.pado.temporal.test.data.v;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.ArrayList;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.data.jsonlite.annotation.KeyReference;

/**
 * @pado 6:5:0 758480909569445530:-7425363208675110297
 * @version 6 dpark:06/14/12.13.37.59.EDT
 * @padocodegen Updated Thu Jun 14 13:37:59 EDT 2012
 */
public enum Position_v6 implements KeyType {
	/**
	 * SecId: <b>String</b>
	 */
	KSecId("SecId", String.class, false, false, null, 0),
	/**
	 * AccountId: <b>String</b>
	 */
	KAccountId("AccountId", String.class, false, false, "account", 2),
	/**
	 * Exposure1: <b>Double</b>
	 */
	KExposure1("Exposure1", Double.class, false, true, null, 0),
	/**
	 * OriginalCost: <b>Double</b>
	 */
	KOriginalCost("OriginalCost", Double.class, false, true, null, 0),
	/**
	 * OriginalFace: <b>Double</b>
	 */
	KOriginalFace("OriginalFace", Double.class, false, true, null, 0),
	/**
	 * SettlementDate: <b>Date</b>
	 */
	KSettlementDate("SettlementDate", Date.class, false, true, null, 0),
	/**
	 * Exposure2: <b>Double</b>
	 */
	KExposure2("Exposure2", Double.class, false, true, null, 0);

	private static final Object ID = new UUID(758480909569445530L,
			-7425363208675110297L);
	private static final int VERSION = 6;
	private static int keyIndex;
	private static boolean payloadKeepSerialized;
	private static KeyType references[] = new KeyType[] { KAccountId };

	private static int getNextIndex() {
		keyIndex++;
		return keyIndex - 1;
	}

	private Position_v6(String name, Class type, boolean isDeprecated,
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
		ArrayList<Integer> list = new ArrayList(values.length);
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
		return +5;
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

	@Override
	public Class getDomainClass()
	{
		// TODO Auto-generated method stub
		return null;
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
