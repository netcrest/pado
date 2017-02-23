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
 * @pado 1:-1:1 2125699987479743435:-5530464836621178725
 * @version 1 pi:01/22/17.14.47.23.EST
 * @padocodegen Updated Sun Jan 22 14:47:23 EST 2017
 */
public enum EmployeeKey implements KeyType 
{
	/**
	 * <b>String</b>
	 */
	KEmployeeId("EmployeeId", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KLastName("LastName", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KFirstName("FirstName", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KTitle("Title", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KTitleOfCourtesy("TitleOfCourtesy", String.class, false, true, "", 0),
	/**
	 * <b>Date</b>
	 */
	KBirthDate("BirthDate", Date.class, false, true, "", 0),
	/**
	 * <b>Date</b>
	 */
	KHireDate("HireDate", Date.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KAddress("Address", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KCity("City", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KRegion("Region", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KPostalCode("PostalCode", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KCountry("Country", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KHomePhone("HomePhone", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KExtension("Extension", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KPhoto("Photo", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KNotes("Notes", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KReportsTo("ReportsTo", String.class, false, true, "", 0),
	/**
	 * <b>String</b>
	 */
	KPhotoPath("PhotoPath", String.class, false, true, "", 0);

	private static final Object ID = new UUID(2125699987479743435L, -5530464836621178725L);
	private static final int VERSION = 1;
	private static int keyIndex;
	private static boolean payloadKeepSerialized;
	private static Class<?> domainClass;
	private static KeyType references[] = new KeyType[] {};

	static {
		try {
			domainClass = Class
					.forName("com.netcrest.pado.demo.nw.data.domain.Employee");
		} catch (ClassNotFoundException ex) {
			domainClass = null;
		}
	}

	private static int getNextIndex() {
		keyIndex++;
		return keyIndex - 1;
	}

	private EmployeeKey(String name, Class<?> type, boolean isDeprecated,
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
