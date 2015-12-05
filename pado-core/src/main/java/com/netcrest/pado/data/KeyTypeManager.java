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
package com.netcrest.pado.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.data.jsonlite.JsonLiteTokenizer;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.info.KeyTypeInfo;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.pql.PadoQueryParser;
import com.netcrest.pado.server.VirtualPathEngine;

/**
 * KeyTypeManager manages the key type classes for the process life time. Each
 * key type class requires registration through KeyTypeManager in order to
 * activate them within the process boundary. Using a key type without
 * registering may fail as the underlying marshaling mechanism is maybe unable
 * to properly map the key type.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
public class KeyTypeManager
{
	/**
	 * Map of key types. <UUID, Map<version, KeyType>>
	 */
	private static ConcurrentHashMap<UUID, Map<Integer, KeyType>> uuidMap = new ConcurrentHashMap<UUID, Map<Integer, KeyType>>();

	/**
	 * Map of query statements <UUID, Map<keyName, query>>
	 */
	private static ConcurrentHashMap<UUID, Map<String, QueryReference>> queryMap = new ConcurrentHashMap<UUID, Map<String, QueryReference>>();

	/**
	 * Map of KeyType arrays. Each array represents a list of defined
	 * references, i.e., QueryReferences.
	 */
	private static ConcurrentHashMap<UUID, KeyType[]> referenceMap = new ConcurrentHashMap<UUID, KeyType[]>();

	private static ConcurrentHashMap<String, KeyMap> virtualPathDefinitionMap = new ConcurrentHashMap<String, KeyMap>();

	// /**
	// * Map of VirtualCompiledUnit objects that define virtual paths.
	// * &lt;virtual-path, VCU&gt;
	// */
	// private static ConcurrentHashMap<String, VirtualCompiledUnit>
	// virtualCompiledUnitMap = new ConcurrentHashMap<String,
	// VirtualCompiledUnit>();

	/**
	 * Registers the specified key type. It assumes that all of the KeyType
	 * version classes are in the "v" sub-package as the specified KeyType.
	 * 
	 * @param keyTypeName
	 *            The fully qualified KeyType class name with or without the
	 *            version number suffix, i.e., both com.foo.orders.FOrder and
	 *            com.foo.orders.v.FOrder_v1 qualify.
	 */
	public static void registerKeyType(String keyTypeName)
	{
		if (keyTypeName == null) {
			return;
		}
		Class cls;
		try {
			cls = Class.forName(keyTypeName);
			Object[] objects = cls.getEnumConstants();
			if (objects != null && objects.length > 0) {
				KeyType keyType = (KeyType) objects[0];
				registerKeyType(keyType);
			}
		} catch (Exception e) {
			Logger.warning("KeyType registration failed: " + keyTypeName
					+ ". Make sure the key class is in this client's class path.", e.getCause());
		}
	}

	/**
	 * Returns all key type versions for the specified key type class without
	 * the main KeyType. Returns null if the specified class is not of KeyType.
	 * 
	 * @param keyTypeClass
	 *            The key type class.
	 */
	public static KeyType[] getAllRegisteredVersions(Class keyTypeClass)
	{
		return getAllRegisteredVersions(keyTypeClass, false);
	}

	/**
	 * Returns all key type versions for the specified key type class. Returns
	 * null if the specified class is not of KeyType.
	 * 
	 * @param keyTypeClass
	 *            The key type class.
	 * @param includeMainKeyType
	 *            true to include the main KeyType
	 */
	public static KeyType[] getAllRegisteredVersions(Class keyTypeClass, boolean includeMainKeyType)
	{
		if (keyTypeClass == null) {
			return null;
		}
		Object[] objects = keyTypeClass.getEnumConstants();
		KeyType keyType = null;
		if (objects != null && objects.length > 0) {
			if (objects[0] instanceof KeyType) {
				keyType = (KeyType) objects[0];
			}
		}
		if (keyType == null) {
			return null;
		}
		TreeMap<Integer, KeyType> map = (TreeMap<Integer, KeyType>) uuidMap.get(keyType.getId());
		if (includeMainKeyType) {
			KeyType mainKeyType = getMainKeyType(keyType);
			List<KeyType> list = new ArrayList(map.values());
			list.add(mainKeyType);
			return list.toArray(new KeyType[list.size()]);
		} else {
			return map.values().toArray(new KeyType[0]);
		}
	}

	/**
	 * Returns a list of all registered KeyType objects in the form of
	 * KeyTypeInfo. The returned map is sorted and has (last KeyType class name,
	 * KeyTypeInfo set) pairs. Returned object is never null.
	 */
	public static Map<String, Set<KeyTypeInfo>> getAllRegisteredKeyTypeInfos()
	{
		Collection<Map<Integer, KeyType>> col = uuidMap.values();
		TreeMap<String, Set<KeyTypeInfo>> keyTypeMap = new TreeMap<String, Set<KeyTypeInfo>>();
		for (Map<Integer, KeyType> map : col) {
			TreeSet<KeyTypeInfo> keyTypeSet = new TreeSet<KeyTypeInfo>();
			KeyTypeInfo info = null;
			String mainClassName = null;
			for (KeyType keyType : map.values()) {
				info = new KeyTypeInfo();
				if (keyType.getClass().getName().matches(".*_v\\d+$") == false) {
					mainClassName = keyType.getClass().getName();
				}
				info.setKeyTypeClassName(keyType.getClass().getName());
				info.setMergePoint(keyType.getMergePoint());
				info.setVersion(keyType.getVersion());
				keyTypeSet.add(info);
			}
			if (keyTypeSet.size() == 0) {
				continue;
			}
			if (mainClassName == null) {
				// pick the last one as the main class
				if (keyTypeSet.last() != null) {
					mainClassName = keyTypeSet.last().getKeyTypeClassName();
				}
				keyTypeMap.put(mainClassName, keyTypeSet);
			}
		}
		return keyTypeMap;
	}

	/**
	 * Returns all registered main key type names which do not include the
	 * version number. Always returns a non-null array.
	 */
	public static String[] getAllRegisteredMainKeyTypeNames()
	{
		Collection<Map<Integer, KeyType>> col = uuidMap.values();
		List<String> classNameList = new ArrayList<String>();
		for (Map<Integer, KeyType> map : col) {
			String className = null;
//			int version = -1;
			for (KeyType keyType : map.values()) {
				KeyType mainKeyType = getMainKeyType(keyType);
				if (mainKeyType != null) {
					className = mainKeyType.getClass().getName();
					break;
				}
//				if (keyType.getVersion() >= version) {
//					className = keyType.getClass().getName();
//					if (keyType.getClass().getName().matches(".*_v\\d+$") == false) {
//						break;
//					}
//					version = keyType.getVersion();
//				}
			}
			if (className != null) {
				classNameList.add(className);
			}
		}
		return classNameList.toArray(new String[0]);
	}

	/**
	 * Returns the main key type class of the specified key type. It returns
	 * null if the specified key type object is null or the main key type class
	 * cannot be found.
	 * 
	 * @param keyType
	 *            Key type object
	 */
	public static KeyType getMainKeyType(KeyType keyType)
	{
		if (keyType == null) {
			return null;
		}
		String simpleName = keyType.getClass().getSimpleName();
		if (simpleName.matches(".*_v\\d+$")) {
			simpleName = simpleName.substring(0, simpleName.lastIndexOf("_v"));
			String packageName = keyType.getClass().getPackage().getName();
			if (packageName.endsWith(".v")) {
				packageName = packageName.substring(0, packageName.lastIndexOf(".v"));
				String className = packageName + "." + simpleName;
				try {
					Class clazz = Class.forName(className);
					Object[] ec = clazz.getEnumConstants();
					if (ec != null) {
						keyType = (KeyType) ec[0];
					}
				} catch (ClassNotFoundException e) {
					// ignore
				}
			}
		}
		return keyType;
	}

	/**
	 * Registers the specified key type and all of the previous and next
	 * versions. All key types must be registered by invoking this method before
	 * they can be used by the application.
	 * 
	 * @param keyType
	 *            The key type to register.
	 */
	public static void registerKeyType(KeyType keyType)
	{
		if (keyType == null) {
			return;
		}

		registerSingleKeyType(keyType);

		Package pkg = keyType.getClass().getPackage();
		String simpleName = keyType.getClass().getSimpleName();
		String keyTypeName;

		// remove the version number suffix if exists
		int end = simpleName.lastIndexOf("_v");
		if (end != -1) {
			simpleName = simpleName.substring(0, end);
			if (pkg == null) {
				keyTypeName = simpleName;
			} else {
				keyTypeName = pkg.getName() + "." + simpleName;
			}
		} else {
			if (pkg == null) {
				keyTypeName = "v." + simpleName;
			} else {
				keyTypeName = pkg.getName() + ".v." + simpleName;
			}
		}

		// previous versions;
		int version = keyType.getVersion();
		do {
			try {
				Class cls = Class.forName(keyTypeName + "_v" + version);
				if (cls.isEnum() == false) {
					break;
				}
				Object[] consts = cls.getEnumConstants();
				if (consts != null && consts.length > 0 && consts[0] instanceof KeyType) {
					KeyType ft = (KeyType) consts[0];
					registerSingleKeyType(ft);
				}
			} catch (ClassNotFoundException e) {
				break;
			}
			version--;

		} while (true);

		// next versions
		version = keyType.getVersion();
		version++;
		do {
			try {
				Class cls = Class.forName(keyTypeName + "_v" + version);
				if (cls.isEnum() == false) {
					break;
				}
				Object[] consts = cls.getEnumConstants();
				if (consts != null && consts.length > 0 && consts[0] instanceof KeyType) {
					KeyType ft = (KeyType) consts[0];
					registerSingleKeyType(ft);
				}
			} catch (ClassNotFoundException e) {
				break;
			}
			version++;

		} while (true);
	}

	/**
	 * Registers the specified key type only. It will not register the previous
	 * versions.
	 * 
	 * @param keyType
	 *            The key type to register
	 */
	public static void registerSingleKeyType(KeyType keyType)
	{
		if (keyType == null) {
			return;
		}
		Map<Integer, KeyType> map = uuidMap.get(keyType.getId());
		if (map == null) {
			map = new TreeMap<Integer, KeyType>();
			uuidMap.put((UUID) keyType.getId(), map);
		}
		map.put(keyType.getVersion(), keyType);
	}

	/**
	 * Returns true if the specified key type has been registered previously.
	 * 
	 * @param keyType
	 *            The key type to check.
	 */
	public static boolean isRegistered(KeyType keyType)
	{
		if (keyType == null) {
			return false;
		}
		Map<Integer, KeyType> map = uuidMap.get(keyType.getId());
		if (map == null) {
			return false;
		}
		return map.get(keyType.getVersion()) != null;
	}

	/**
	 * Loads a new key type. The main key type points to this key type.
	 * Experimental - NOT USED
	 * 
	 * @param keyType
	 */
	private static void loadKeyType(KeyType keyType)
	{
		registerKeyType(keyType);
		Package pkg = keyType.getClass().getPackage();
		String keyTypeName = pkg.getName() + keyType.getClass().getSimpleName();
		try {
			Class cls = Class.forName(keyTypeName);
			Field field = cls.getField("VERSION");
			field.setInt(field, keyType.getVersion());
		} catch (ClassNotFoundException e) {
			return;
		} catch (NoSuchFieldException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		}
	}

	/**
	 * Returns the key type of the specified UUID most significant bits, least
	 * significant bits and version.
	 * 
	 * @param uuidMostSigBits
	 *            The most significant bits.
	 * @param uuidLeastSigBits
	 *            The least significant bits.
	 * @param version
	 *            The version number.
	 * @return Returns the key type of the specified UUID most significant bits,
	 *         least significant bits and version. It returns null if the key
	 *         type is not found.
	 */
	public static KeyType getKeyType(long uuidMostSigBits, long uuidLeastSigBits, int version)
	{
		return getKeyType(new UUID(uuidMostSigBits, uuidLeastSigBits), version);
	}

	/**
	 * Returns the latest version of the key type of the specified UUID most
	 * significant bits and least significant bits.
	 * 
	 * @param uuidMostSigBits
	 *            The most significant bits.
	 * @param uuidLeastSigBits
	 *            The least significant bits.
	 * @return Returns the key type of the specified UUID most significant bits,
	 *         least significant bits and version. It returns null if the key
	 *         type is not found.
	 */
	public static KeyType getKeyType(long uuidMostSigBits, long uuidLeastSigBits)
	{
		return getKeyType(new UUID(uuidMostSigBits, uuidLeastSigBits));
	}

	/**
	 * Returns the key type of the specified UUID and version.
	 * 
	 * @param uuid
	 *            The UUID representing the key type.
	 * @param version
	 *            The version number.
	 * @return Returns the key type of the specified UUID and version. It
	 *         returns null if the key type is not found.
	 */
	public static KeyType getKeyType(UUID uuid, int version)
	{
		Map<Integer, KeyType> map = uuidMap.get(uuid);
		if (map == null) {
			return null;
		}
		return map.get(version);
	}

	/**
	 * Returns the latest version of the key type of the specified UUID.
	 * 
	 * @param uuid
	 *            The UUID representing the key type.
	 * @return Returns the key type of the specified UUID and version. It
	 *         returns null if the key type is not found.
	 */
	public static KeyType getKeyType(UUID uuid)
	{
		TreeMap<Integer, KeyType> map = (TreeMap<Integer, KeyType>) uuidMap.get(uuid);
		if (map == null) {
			return null;
		}
		Map.Entry<Integer, KeyType> latest = map.lastEntry();
		if (latest == null) {
			return null;
		}
		return latest.getValue();
	}

	/**
	 * Returns the entire key type instances of the specified version.
	 * 
	 * @param keyType
	 *            The key type.
	 * @param version
	 *            The version number.
	 * @return Returns the entire key type instances of the specified version.
	 *         It returns null if the key types are not found.
	 */
	public static KeyType[] getValues(KeyType keyType, int version)
	{
		if (keyType == null) {
			return null;
		}
		KeyType ft = getKeyType((UUID) keyType.getId(), version);
		if (ft == null) {
			return null;
		}
		return ft.getValues();
	}

	/**
	 * Returns the latest version of the specified key type.
	 * 
	 * @param keyType
	 *            The key type
	 */
	public static KeyType getLatestKeyTypeVersion(KeyType keyType)
	{
		if (keyType == null) {
			return null;
		}
		TreeMap<Integer, KeyType> map = (TreeMap<Integer, KeyType>) uuidMap.get(keyType.getId());
		Map.Entry<Integer, KeyType> lastEntry = map.lastEntry();
		KeyType latestKeyType = lastEntry.getValue();
		return latestKeyType;
	}

	/**
	 * Translates the key type version. The specified key types must be of the
	 * same KeyType, otherwise it returns null. The returned key type is the
	 * model (actual) index to the value object array that KeyMap internally
	 * maintains.
	 * 
	 * @param fromKeyType
	 *            The key type to translate from.
	 * @param toKeyType
	 *            The key type to translate to. Note that toKeyType could have
	 *            any index. It's used as a base to translate the fromKeyType
	 *            index.
	 * @return Returns the translated key type of the toKeyType version.
	 *         <ul>
	 *         <li>Returns fromKeyType if fromKeyType and toKeyType are same.</li>
	 *         <li>Returns null if any of the specified key types is null.</li>
	 *         <li>Returns fromKeyType if toKeyType is of a different type,
	 *         i.e., fromKeyType.getId() != toKeyType.getId().</li>
	 *         <li>Returns fromKeyType if fromKeyType and toKeyType have the
	 *         same version.</li>
	 *         <li>Returns null if it is unable to translate the key as follows:
	 *         </li>
	 *         <ul>
	 *         <li><b>fromKeyType is older than toKeyType:</b> if there is a
	 *         merge point between the two keys and fromKeyType has been
	 *         deprecated.</li>
	 *         <li><b>fromKeyType is newer than toKeyType:</b> if fromKeyType is
	 *         a new addition that is not part of toKeyType.</li>
	 *         </ul>
	 *         </ul>
	 */
	public static KeyType translateKeyTypeVersion(KeyType fromKeyType, KeyType toKeyType)
	{
		if (fromKeyType == toKeyType) {
			return fromKeyType;
		} else if (fromKeyType == null || toKeyType == null) {
			return null;
		} else if (fromKeyType.getId().equals(toKeyType.getId()) == false) {
			return fromKeyType;
		}

		// If merger did not occur then translation is not necessary. Return
		// fromKeyType.
		if (fromKeyType.getMergePoint() == toKeyType.getMergePoint()) {
			// fromKeyType's index cannot be greater than the toKeyType key
			// count.
			if (fromKeyType.getIndex() >= toKeyType.getKeyCount()) {
				return null;
			}
			return fromKeyType;
		}

		if (fromKeyType.getVersion() == toKeyType.getVersion()) {
			return fromKeyType;
		}

		if (fromKeyType.getVersion() < toKeyType.getVersion()) {

			// from is older than to
			// read v6(to) with v3(from)

			if (fromKeyType.isDeprecated()) {
				return null;
			}

			int fromIndex = fromKeyType.getIndex();
			int index = fromIndex;
			KeyType keyType = getNextMergePointKeyType(fromKeyType);
			while (keyType != null && keyType.getVersion() < toKeyType.getVersion()) {
				// Decrement the from-index for the deprecated keys up to the
				// from key
				int[] deprecatedIndexes = keyType.getDeprecatedIndexes();
				for (int i = 0; i < deprecatedIndexes.length; i++) {
					if (fromIndex == deprecatedIndexes[i]) {
						// deprecated
						return null;
					} else if (fromIndex > deprecatedIndexes[i]) {
						index--;
					} else {
						break;
					}
				}
				// keyType = getNextMergePointKeyType(keyType);
				keyType = getNextMergePointKeyType(keyType.getValues(keyType.getVersion() + 1)[0]);
			}

			if (index >= toKeyType.getKeyCount()) {
				return null;
			}
			return toKeyType.getValues()[index];

		} else {

			// from is newer than to
			// read v3(to) with v6(from)

			int fromIndex = fromKeyType.getIndex();
			int index = fromIndex;
			KeyType keyType = getPreviousMergePointKeyType(fromKeyType);
			while (keyType != null && keyType.getVersion() < fromKeyType.getVersion()) {
				// Decrement the from-index for the deprecated keys up to the
				// from key
				int[] deprecatedIndexes = keyType.getDeprecatedIndexes();

				for (int i = 0; i < deprecatedIndexes.length; i++) {
					if (index >= deprecatedIndexes[i]) {
						index++;
					} else {
						break;
					}
				}
				keyType = getPreviousMergePointKeyType(keyType);
			}

			if (index >= toKeyType.getKeyCount()) {
				return null;
			}
			return toKeyType.getValues()[index];
		}
	}

	/**
	 * Returns the previous merge point index of the specified key type. It
	 * returns -1 if the previous merge point doesn't exist or the specified key
	 * type has not merged.
	 * 
	 * @param keyType
	 *            The key type to search from.
	 */
	public static int getPreviousMergePoint(KeyType keyType)
	{
		if (keyType == null || keyType.getMergePoint() == -1) {
			return -1;
		}
		KeyType kt = keyType;
		int mergePoint = kt.getMergePoint();
		KeyType types[] = kt.getValues(mergePoint);
		if (types == null || types.length == 0) {
			return -1;
		} else {
			return mergePoint;
		}
	}

	/**
	 * Returns the previous merge point key of the specified key type. It
	 * returns null if the previous merge point doesn't exist or the specified
	 * key type has not merged.
	 * 
	 * @param keyType
	 *            The key type to search from.
	 */
	public static KeyType getPreviousMergePointKeyType(KeyType keyType)
	{
		int version = getPreviousMergePoint(keyType);
		if (version == -1) {
			return null;
		}
		return getValues(keyType, version)[0];
	}

	/**
	 * Returns the next merge point index of the specified key type. It returns
	 * -1 if the next merge point is not found.
	 * 
	 * @param keyType
	 *            The key type to search from.
	 */
	public static int getNextMergePoint(KeyType keyType)
	{
		if (keyType == null) {
			return -1;
		}
		KeyType kt = keyType;
		int mergePoint = kt.getMergePoint();
		do {
			KeyType types[] = kt.getValues(kt.getVersion() + 1);
			if (types != null && types.length > 0) {
				kt = types[0];
			} else {
				kt = null;
			}
		} while (kt != null && kt.getMergePoint() == mergePoint);
		if (kt == null) {
			return -1;
		}
		return kt.getMergePoint();
	}

	/**
	 * Returns the next merge point key of the specified key type. It returns
	 * null if the next merge point is not found.
	 * 
	 * @param keyType
	 *            The key type to search from.
	 */
	public static KeyType getNextMergePointKeyType(KeyType keyType)
	{
		int version = getNextMergePoint(keyType);
		if (version == -1) {
			return null;
		}
		return getValues(keyType, version)[0];
	}

	// public static int getNextMergePoint(KeyType keyType)
	// {
	// if (keyType == null) {
	// return -1;
	// }
	// KeyType kt = keyType;
	// do {
	// kt = kt.getValues(keyType.getVersion() + 1)[0];
	// } while (kt != null && kt.getMergePoint() == -1);
	// if (kt == null) {
	// return -1;
	// }
	// return kt.getMergePoint();
	// }
	//
	// public static KeyType getNextMergePointKeyType(KeyType keyType)
	// {
	// int version = getNextMergePoint(keyType);
	// if (version == -1) {
	// return null;
	// }
	// return getValues(keyType, version)[0];
	// }

	/**
	 * Returns the query assigned to the specified key type. The returned query
	 * overrides the query defined in the KeyType object if not null.
	 * 
	 * @param keyType
	 *            KeyType Key type
	 * @return Null if not defined.
	 */
	public static String getQuery(KeyType keyType)
	{
		if (keyType == null) {
			return null;
		}
		Map<String, QueryReference> map = queryMap.get(keyType.getId());
		if (map == null) {
			return null;
		}
		QueryReference qr = map.get(keyType.getName());
		if (qr == null) {
			return null;
		}
		return qr.query;
	}

	public static int getDepth(KeyType keyType)
	{
		if (keyType == null) {
			return -1;
		}
		Map<String, QueryReference> map = queryMap.get(keyType.getId());
		if (map == null) {
			return -1;
		}
		QueryReference qr = map.get(keyType.getName());
		if (qr == null) {
			return -1;
		}
		return qr.depth;
	}

	/**
	 * Registers the entire key types contained in the specified JsonLite
	 * object. It overwrites the existing query references for the specified key
	 * type found in the JsonLite key type definition to all of its key type
	 * versioned classes.
	 * 
	 * @param dbDir
	 *            Data base root directory
	 * @param keyType
	 *            Key type
	 * @param keyTypeDefinition
	 *            JsonLite object with the following example format:
	 * 
	 *            <pre>
	 * {
	 *    "KeyType": "com.netcrest.pado.temporal.test.data.Portfolio",
	 *    "AccountId": {
	 *       "Query": "account",
	 *      "Depth": 2
	 *    },
	 *    "Positions": {
	 *       "Query": "position",
	 *       "Depth": 3
	 *    }
	 * }
	 * </pre>
	 * @param isPersist
	 *            True to persist to the DB, false to register in memory only.
	 * @throws IOException
	 *             Thrown if isPersist is true and persistence fails.
	 * @throws ClassNotFoundException
	 *             Thrown if the KeyType class name defined in the
	 *             dkeyTypeDefinition object is not found.
	 * @throws PadoException
	 *             Thrown if the KeyType class is not an enum class.
	 * @throws ClassCastException
	 *             Thrown if the class is not of KeyType.
	 */
	public static void registerQueryReferences(String dbDir, JsonLite keyTypeDefinition, boolean isPersist)
			throws IOException, ClassNotFoundException, PadoException, ClassCastException
	{
		if (keyTypeDefinition == null) {
			return;
		}
		JsonLite referenceDefinition = null;
		try {
			referenceDefinition = (JsonLite) keyTypeDefinition.get("Reference");
		} catch (Exception ex) {
			// ignore
		} finally {
			if (referenceDefinition == null) {
				return;
			}
		}

		String keyTypeClassName = (String) keyTypeDefinition.get("KeyType");
		Class claz = Class.forName(keyTypeClassName);
		KeyType[] allKeyTypeVersions = getAllRegisteredVersions(claz, true);
		if (allKeyTypeVersions == null || allKeyTypeVersions.length == 0) {
			return;
		}

		// First, clear all query references from the KeyType versions.
		// The specified KeyType definition overrides all existing query
		// references.
		for (KeyType keyType : allKeyTypeVersions) {
			KeyType[] keyTypes = keyType.getValues();
			for (KeyType kt : keyTypes) {
				kt.setDepth(0);
				kt.setQuery("");
				kt.setReferences(null);
			}
		}

		// Apply the new KeyType definition to all versions.
		KeyType latestKeyType = getLatestKeyTypeVersion(allKeyTypeVersions[0]);
		Map<String, QueryReference> map = new HashMap<String, QueryReference>(latestKeyType.getValues().length, 1f);
		// References for all versions, i.e., latest version
		ArrayList<KeyType> referenceKeyTypeList = new ArrayList<KeyType>(10);
		for (KeyType keyType : allKeyTypeVersions) {
			KeyType[] keyTypes = keyType.getValues();
			// References for this particular version.
			ArrayList<KeyType> referenceKeyTypeListForVersion = new ArrayList<KeyType>(10);
			for (KeyType kt : keyTypes) {
				JsonLite jl2 = (JsonLite) referenceDefinition.get(kt.getName());
				if (jl2 != null) {
					String query = (String) jl2.get("Query");
					Integer depth = (Integer) jl2.get("Depth");
					if (depth == null) {
						depth = 0;
					}
					if (query != null) {
						if (depth <= 0) {
							depth = -1;
						} else if (depth > 10) {
							depth = 10;
						}
						kt.setDepth(depth);
						kt.setQuery(query);
						map.put(kt.getName(), new QueryReference(query, depth));
						referenceKeyTypeList.add(kt);
						referenceKeyTypeListForVersion.add(kt);
					}
				}
			}

			// Set references found for this KeyType version.
			// This sets references to each versioned KeyType class.
			keyType.setReferences(referenceKeyTypeListForVersion.toArray(new KeyType[referenceKeyTypeListForVersion
					.size()]));
		}

		queryMap.put((UUID) latestKeyType.getId(), map);
		// Set references found from all versions. Same as the latest version.
		referenceMap.put((UUID) latestKeyType.getId(),
				referenceKeyTypeList.toArray(new KeyType[referenceKeyTypeList.size()]));

		// Remove all CUs for all key type version so that the new query
		// references can be compiled.
		for (KeyType kt2 : allKeyTypeVersions) {
			KeyType[] values = kt2.getValues();
			for (KeyType kt3 : values) {
				PadoQueryParser.removeCompiledUnit(kt3);
			}
		}

		if (isPersist) {
			String str = keyTypeDefinition.toString(3, false, false);
			File keyTypeDir = new File(dbDir, "keytype");
			File file = new File(keyTypeDir, keyTypeClassName + ".json");
			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
				writer.write(str);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}

		Logger.config("KeyTypeDefinition registered: " + keyTypeClassName);
	}

	public static void registerQueryReferences_old(String dbDir, JsonLite keyTypeDefinition, boolean isPersist)
			throws IOException, ClassNotFoundException, PadoException, ClassCastException
	{
		if (keyTypeDefinition == null) {
			return;
		}
		KeyType keyType = null;
		String keyTypeClassName = (String) keyTypeDefinition.get("KeyType");

		Class claz = Class.forName(keyTypeClassName);
		Object[] ec = claz.getEnumConstants();
		if (ec == null) {
			throw new PadoException("KeyType is not an enum class.");
		} else {
			keyType = (KeyType) ec[0];
		}

		ArrayList<KeyType> list = new ArrayList<KeyType>(10);
		Map<String, QueryReference> map = new HashMap<String, QueryReference>(keyType.getValues().length, 1f);
		KeyType[] keyTypes = keyType.getValues();
		for (KeyType kt : keyTypes) {
			JsonLite jl2 = (JsonLite) keyTypeDefinition.get(kt.getName());
			if (jl2 != null) {
				String query = (String) jl2.get("Query");
				Integer depth = (Integer) jl2.get("Depth");
				if (depth == null) {
					depth = 0;
				}
				if (query != null) {
					if (depth <= 0) {
						depth = -1;
					} else if (depth > 10) {
						depth = 10;
					}
					map.put(kt.getName(), new QueryReference(query, depth));
					list.add(kt);
				}
			}
		}
		queryMap.put((UUID) keyType.getId(), map);
		referenceMap.put((UUID) keyType.getId(), list.toArray(new KeyType[list.size()]));

		// Remove all CUs for all key type version so that the new query
		// references can be compiled.
		KeyType allVersions[] = getAllRegisteredVersions(keyType.getClass(), true);
		for (KeyType kt2 : allVersions) {
			KeyType[] values = kt2.getValues();
			for (KeyType kt3 : values) {
				PadoQueryParser.removeCompiledUnit(kt3);
			}
		}

		if (isPersist) {
			String str = keyTypeDefinition.toString(3, false, false);
			File keyTypeDir = new File(dbDir, "keytype");
			File file = new File(keyTypeDir, keyType.getClass().getName() + ".json");
			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
				writer.write(str);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}
	}

	/**
	 * Registers the specified virtual path definition in the specified DB
	 * directory under "vp".
	 * 
	 * @param dbDir
	 *            DB directory.
	 * @param virtualPathDefinition
	 *            Virtual path definition
	 * @throws IOException
	 *             Thrown if a DB error occurs.
	 */
	public static void registerVirtualPath(String dbDir, KeyMap virtualPathDefinition, boolean isPersist)
			throws IOException
	{
		if (virtualPathDefinition == null) {
			return;
		}
		String virtualPath = (String) virtualPathDefinition.get("VirtualPath");
		if (virtualPath == null) {
			return;
		}

		virtualPathDefinitionMap.put(virtualPath, virtualPathDefinition);

		if (isPersist) {
			String str = virtualPathDefinition.toString(3, false, false);
			File vpDir = new File(dbDir, "vp");
			String vpName = virtualPath.replaceAll("\\/", ".");
			File file = new File(vpDir, vpName + ".json");
			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
				writer.write(str);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}

		VirtualPathEngine.getVirtualPathEngine().addVirtualPathDefinition(virtualPathDefinition);
		Logger.config("VirtualPathDefinition registered: " + virtualPath);
	}

	/**
	 * Removes the specified virtual path. Once removed, the virtual path no
	 * longer exists. Its definition is removed and irrecoverable.
	 * 
	 * @param dbDir
	 *            Data base directory
	 * @param virtualPath
	 *            Virtual path
	 * @return true if the virtual path has successfully been removed. false, if
	 *         the removal of the virtual path failed due to a DB error.
	 */
	public static boolean removeVirtualPath(String dbDir, String virtualPath)
	{
		virtualPathDefinitionMap.remove(virtualPath);
		VirtualPathEngine.getVirtualPathEngine().removeVirtualPathDefinition(virtualPath);
		File vpDir = new File(dbDir, "vp");
		String vpName = virtualPath.replaceAll("\\/", ".");
		File file = new File(vpDir, vpName + ".json");
		if (file.exists()) {
			return file.delete();
		} else {
			return true;
		}
	}

	/**
	 * Returns the VPD for the specified virtual path. It returns null if not
	 * found.
	 * 
	 * @param virtualPath
	 *            Virtual path
	 */
	public static KeyMap getVirtualPathDefinition(String virtualPath)
	{
		return virtualPathDefinitionMap.get(virtualPath);
	}

	/**
	 * Returns true if the specified virtual path exists.
	 * 
	 * @param virtualPath
	 *            Virtual path
	 */
	public static boolean isVirtualPathDefinition(String virtualPath)
	{
		return virtualPathDefinitionMap.containsKey(virtualPath);
	}

	/**
	 * Returns a sorted array of all server registered virtual paths.
	 */
	public static String[] getAllVirtualPaths()
	{
		Set<String> set = virtualPathDefinitionMap.keySet();
		List<String> list = new ArrayList<String>(set);
		Collections.sort(list);
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Returns a map of all server-registered (virtual path, virtual definition)
	 * paired entries.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, KeyMap> getAllVirtualPathDefinitions()
	{
		return new HashMap(virtualPathDefinitionMap);
	}

	public static void resetDb(String dbDir)
	{
		if (dbDir == null) {
			return;
		}

		// Load "keytype" defintions
		resetKeyTypes(dbDir);

		// Load "vp" definitions
		resetVirtualPaths(dbDir);
	}

	private static void resetKeyTypes(String dbDir)
	{
		queryMap.clear();
		File keyTypeDir = new File(dbDir, "keytype");
		File[] jsonFiles = keyTypeDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".json");
			}

		});

		if (jsonFiles == null) {
			return;
		}
		for (File file : jsonFiles) {
			FileReader reader = null;
			String keyTypeClassName = null;
			try {
				reader = new FileReader(file);
				JsonLiteTokenizer jt = new JsonLiteTokenizer(reader, null);
				JsonLite jl = new JsonLite(jt, null);
				registerQueryReferences(dbDir, jl, false);
			} catch (PadoException e) {
				Logger.warning(e.getMessage() + " Query reference not registered. [file=" + file.getAbsolutePath()
						+ ", KeyType=" + keyTypeClassName + "]");
			} catch (FileNotFoundException e) {
				// ignore
			} catch (ClassNotFoundException e) {
				Logger.warning("KeyType class not found. Query reference not registered. [file="
						+ file.getAbsolutePath() + ", KeyType=" + keyTypeClassName + "]");
			} catch (ClassCastException e) {
				Logger.warning(e.getMessage() + " Query reference not registered. [file=" + file.getAbsolutePath()
						+ ", KeyType=" + keyTypeClassName + "]");
			} catch (IOException ex) {
				// ignore. Never thrown since registerQueryReferences() is
				// invoked with no persistence.
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ex) {
						// ignore
					}
				}
			}
		}

		// Reset the CUs so that the new query references can be compiled.
		PadoQueryParser.reset();
	}

	private static void resetVirtualPaths(String dbDir)
	{
		virtualPathDefinitionMap.clear();
		VirtualPathEngine.getVirtualPathEngine().clear();

		File keyTypeDir = new File(dbDir, "vp");
		File[] jsonFiles = keyTypeDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".json");
			}

		});

		if (jsonFiles == null) {
			return;
		}
		for (File file : jsonFiles) {
			try {
				JsonLite vpd = new JsonLite(file);
				registerVirtualPath(dbDir, vpd, false);
			} catch (Exception e) {
				Logger.error("Error reading file: " + file.getAbsolutePath(), e);
			}
		}
	}

	public static void dumpDb()
	{
		Set<Map.Entry<UUID, Map<String, QueryReference>>> set = queryMap.entrySet();
		int i = 0;
		for (Map.Entry<UUID, Map<String, QueryReference>> entry : set) {
			System.out.println(++i + ". " + entry.getKey());
			Map<String, QueryReference> map = entry.getValue();
			Set<Map.Entry<String, QueryReference>> set2 = map.entrySet();
			for (Map.Entry<String, QueryReference> entry2 : set2) {
				System.out.println("   " + entry2.getKey() + ": " + entry2.getValue());
			}
		}
	}

	public static KeyType[] getAllReferences(KeyType keyType)
	{
		if (keyType == null) {
			return null;
		}
		return referenceMap.get(keyType.getId());
		// KeyType[] keyTypes = referenceMap.get(keyType.getId());
		// if (keyTypes == null) {
		// keyTypes = keyType.getReferences();
		// }
		// return keyTypes;
	}

	public static KeyTypeManager.QueryReference getQueryReference(KeyType keyType)
	{
		if (keyType == null) {
			return null;
		}
		Map<String, QueryReference> map = queryMap.get(keyType.getId());
		if (map == null) {
			return null;
		}
		return map.get(keyType.getName());
	}

	public static boolean isReference(KeyType keyType)
	{
		if (keyType == null) {
			return false;
		}
		Map<String, QueryReference> map = queryMap.get(keyType.getId());
		if (map == null) {
			return false;
		}
		return map.containsKey(keyType.getName());
	}

	public static class QueryReference
	{
		String query;
		int depth;

		QueryReference(String query, int depth)
		{
			this.query = query;
			this.depth = depth;
		}

		public String getQuery()
		{
			return query;
		}

		public int getDepth()
		{
			return depth;
		}

		@Override
		public String toString()
		{
			return "QueryReference [query=" + query + ", depth=" + depth + "]";
		}
	}
}
