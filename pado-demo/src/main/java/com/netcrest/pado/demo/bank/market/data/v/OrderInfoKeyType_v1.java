package com.netcrest.pado.demo.bank.market.data.v;

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
 * @pado 1:-1:0 -471832288139722198:-8753408166471510720
 * @version 1 dpark:10/04/12.13.09.12.EDT
 * @padocodegen Updated Thu Oct 04 13:09:12 EDT 2012
 */
public enum OrderInfoKeyType_v1 implements KeyType {
	/**
	 * OrigGroupAcronym: <b>String</b>
	 */
	KOrigGroupAcronym("OrigGroupAcronym", String.class, false, true),
	/**
	 * ConnectAcronym: <b>String</b>
	 */
	KConnectAcronym("ConnectAcronym", String.class, false, true),
	/**
	 * TargetGroupAcronym: <b>String</b>
	 */
	KTargetGroupAcronym("TargetGroupAcronym", String.class, false, true),
	/**
	 * Symbol: <b>String</b>
	 */
	KSymbol("Symbol", String.class, false, true),
	/**
	 * ClientOrdId: <b>String</b>
	 */
	KClientOrdId("ClientOrdId", String.class, false, true),
	/**
	 * HandleInst: <b>Integer</b>
	 */
	KHandleInst("HandleInst", Integer.class, false, true),
	/**
	 * Side: <b>Integer</b>
	 */
	KSide("Side", Integer.class, false, true),
	/**
	 * OrdType: <b>Integer</b>
	 */
	KOrdType("OrdType", Integer.class, false, true),
	/**
	 * HostCounter: <b>Integer</b>
	 */
	KHostCounter("HostCounter", Integer.class, false, true),
	/**
	 * ClientSessionId: <b>Integer</b>
	 */
	KClientSessionId("ClientSessionId", Integer.class, false, true),
	/**
	 * TimeStamp: <b>Date</b>
	 */
	KTimeStamp("TimeStamp", Date.class, false, true),
	/**
	 * TransactionTime: <b>Date</b>
	 */
	KTransactionTime("TransactionTime", Date.class, false, true);

	private static final Object ID = new UUID(-471832288139722198L,
			-8753408166471510720L);
	private static final int VERSION = 1;
	private static int keyIndex;
	private static boolean payloadKeepSerialized;

	private static int getNextIndex() {
		keyIndex++;
		return keyIndex - 1;
	}

	private OrderInfoKeyType_v1(String name, Class type, boolean isDeprecated,
			boolean keyKeepSerialized) {
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
