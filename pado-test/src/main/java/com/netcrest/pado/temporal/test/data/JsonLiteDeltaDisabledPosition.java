package com.netcrest.pado.temporal.test.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.data.jsonlite.annotation.KeyReference;

/**
 * @pado 1:-1:0 3056322075758971134:-5711361224371045994
 * @version 1 dpark:12/22/13.09.58.42.EST
 * @padocodegen Updated Sun Dec 22 09:58:42 EST 2013
 */
public enum JsonLiteDeltaDisabledPosition implements KeyType {
	/**
	 * AccountCd: <b>String</b>
	 */
	KAccountCd("AccountCd", String.class, false, true),
	/**
	 * AccountId: <b>String</b>
	 */
	KAccountId("AccountId", Long.class, false, true),
	/**
	 * AccrualAm: <b>Double</b>
	 */
	KAccrualAm("AccrualAm", Double.class, false, true),
	/**
	 * AsOfDt: <b>Date</b>
	 */
	KAsOfDt("AsOfDt", Date.class, false, true),
	/**
	 * BvAm: <b>Double</b>
	 */
	KBvAm("BvAm", Double.class, false, true),
	/**
	 * CurrFaceAm: <b>Double</b>
	 */
	KCurrFaceAm("CurrFaceAm", Double.class, false, true),
	/**
	 * FiImntId: <b>Long</b>
	 */
	KFiImntId("FiImntId", Long.class, false, true),
	/**
	 * ImntAltCd: <b>String</b>
	 */
	KImntAltCd("ImntAltCd", String.class, false, true),
	/**
	 * MkPr: <b>Double</b>
	 */
	KMkPr("MkPr", Double.class, false, true),
	/**
	 * MvAm: <b>Double</b>
	 */
	KMvAm("MvAm", Double.class, false, true),
	/**
	 * NavAm: <b>Double</b>
	 */
	KNavAm("NavAm", Double.class, false, true),
	/**
	 * OrgFaceAm: <b>Double</b>
	 */
	KOrgFaceAm("OrgFaceAm", Double.class, false, true),
	/**
	 * ParAm: <b>Double</b>
	 */
	KParAm("ParAm", Double.class, false, true),
	/**
	 * PositionCd: <b>String</b>
	 */
	KPositionCd("PositionCd", String.class, false, true),
	/**
	 * TavAm: <b>Double</b>
	 */
	KTavAm("TavAm", Double.class, false, true),
	/**
	 * Uuid: <b>String</b>
	 */
	KUuid("Uuid", String.class, false, true);

	private static final Object ID = new UUID(3056322075758971134L,
			-5711361224371045994L);
	private static final int VERSION = 1;
	private static int keyIndex;
	private static boolean payloadKeepSerialized;

	private static int getNextIndex() {
		keyIndex++;
		return keyIndex - 1;
	}

	private JsonLiteDeltaDisabledPosition(String name, Class type,
			boolean isDeprecated, boolean keyKeepSerialized) {
		this.index = getNextIndex();
		this.name = name;
		this.type = type;
		this.isDeprecated = isDeprecated;
		this.keyKeepSerialized = keyKeepSerialized;
		try {
			Field field = this.getClass().getField("K" + name);
			Annotation annotations[] = field.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == KeyReference.class) {
					isReference = true;
					break;
				}
			}
		} catch (Exception e) {
			// ignore
		}
	}

	private int index;
	private String name;
	private Class type;
	private boolean isDeprecated;
	private boolean keyKeepSerialized;
	private boolean isReference;

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

	@Override
	public Class getDomainClass()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KeyType[] getReferences()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReference()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getQuery()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setQuery(String path)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDepth()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDepth(int depth)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReferences(KeyType[] references)
	{
		// TODO Auto-generated method stub
		
	}

}
