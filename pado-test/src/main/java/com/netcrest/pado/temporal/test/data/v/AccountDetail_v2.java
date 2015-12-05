package com.netcrest.pado.temporal.test.data.v;

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
 * @pado 2:-1:0 3694110525529999128:-5438543347399740348
 * @version 2 dpark:06/17/14.08.25.15.EDT
 * @padocodegen Updated Tue Jun 17 08:25:15 EDT 2014
 */
public enum AccountDetail_v2 implements KeyType {
	/**
	 * AccountId: <b>String</b>
	 */
	KAccountId("AccountId", String.class, false, false, "account", 2),
	/**
	 * Portfolios: <b>String</b>
	 */
	KPortfolios("Portfolios", String.class, false, false,
			"portfolio?AccountId:${AccountId}", 4),
	/**
	 * Description: <b>String</b>
	 */
	KDescription("Description", String.class, false, true, "", 0),
	/**
	 * OqlPortfolios: <b>String</b>
	 */
	KOqlPortfolios("OqlPortfolios", String.class, false, false,
			"portfolio.get('AccountId')=${AccountId}", 4);

	private static final Object ID = new UUID(3694110525529999128L,
			-5438543347399740348L);
	private static final int VERSION = 2;
	private static int keyIndex;
	private static boolean payloadKeepSerialized;
	private static Class<?> domainClass;
	private static KeyType references[] = new KeyType[] { KAccountId,
			KPortfolios, KOqlPortfolios };

	static {
		try {
			domainClass = Class
					.forName("com.netcrest.pado.temporal.test.data.domain.AccountDetail");
		} catch (ClassNotFoundException ex) {
			domainClass = null;
		}
	}

	private static int getNextIndex() {
		keyIndex++;
		return keyIndex - 1;
	}

	private AccountDetail_v2(String name, Class type, boolean isDeprecated,
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
