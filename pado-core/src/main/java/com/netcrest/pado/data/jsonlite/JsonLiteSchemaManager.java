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
package com.netcrest.pado.data.jsonlite;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.annotation.JsonLiteType;

public class JsonLiteSchemaManager
{
	private static String JSONLITE_CONFIG_FILE_PATH = System.getProperty("pado.jsonlite.config.file",
			"etc/jsonlite.json");
	private static Map<String, Object> levelMap = new HashMap();

	private static String[] getArray(String key, JsonLite jl)
	{
		Object val = jl.get(key);
		String[] strArray;
		if (val instanceof String) {
			strArray = val.toString().split(",");
		} else {
			Object[] objArray = (Object[]) val;
			strArray = new String[objArray.length];
			System.arraycopy(objArray, 0, strArray, 0, objArray.length);
		}
		return strArray;
	}

	public static void initialize()
	{
		try {
			JsonLite jl = new JsonLite(new JsonLiteTokenizer(new FileReader(JSONLITE_CONFIG_FILE_PATH), null), null);
			Set<Map.Entry<String, ?>> col = jl.entrySet();
			for (Map.Entry<String, ?> entry : col) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (key.equals("__auto")) {
					// if (value instanceof JsonLite) {
					// JsonLite jl2 = (JsonLite) value;
					// String[] jarArray = getArray("jars", jl2);
					// URL urls[] = new URL[jarArray.length];
					// for (int i = 0; i < jarArray.length; i++) {
					// File file = new File(jarArray[i]);
					// urls[i] = file.toURI().toURL();
					// }
					// JarIntrospector jarInstrospector = new
					// JarIntrospector(urls, "org net com");
					// String[] keyTypes = jarInstrospector.getKeyTypes();
					// for (String kt : keyTypes) {
					// if (kt.indexOf("_v") != -1) {
					// Class clazz = Class.forName(kt);
					// Object[] enums = clazz.getEnumConstants();
					// for (Object e : enums) {
					// KeyType keyType = (KeyType)e;
					// Class clazz2 = keyType.getType();
					// if (clazz2 == JsonLite.class) {
					// levelMap.put(keyType.getName(), ?);
					// }
					// }
					// }
					// }
					// }
				} else {
					String root = null;
					if (value instanceof String) {
						root = (String) value;
					} else {
						JsonLite jl2 = (JsonLite) value;
						Set<Map.Entry<String, String>> set2 = jl2.entrySet();
						HashMap map2 = new HashMap();
						for (Map.Entry<String, String> entry2 : set2) {
							String key2 = entry2.getKey();
							String value2 = entry2.getValue().trim();
							if (key2.equals("__root")) {
								root = value2;
							} else {
								if (value2.startsWith("${")) {
									map2.put(key2, value2);
								} else {
									Class clazz = Class.forName(value2);
									map2.put(key2, clazz);
								}
							}
						}
						if (map2.size() > 0) {
							if (root != null) {
								Class clazz = Class.forName(root);
								map2.put("__root", clazz);
							}
							levelMap.put(key, map2);
						}
					}
					if (root == null) {
						throw new Exception("__root key undefined for " + key);
					}
					if (levelMap.containsKey(key) == false) {
						if (root.startsWith("${")) {
							levelMap.put(key, root);
						} else {
							Class clazz = Class.forName(root);
							levelMap.put(key, clazz);
						}
					}
				}

			}

			// Scan both maps to resolve all aliases.
			Set<Map.Entry<String, Object>> set = levelMap.entrySet();
			for (Map.Entry<String, Object> entry : set) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof String) {
					String tmp = (String) value;
					if (tmp.startsWith("${") && tmp.endsWith("}")) {
						String alias = tmp.substring(2, tmp.indexOf('}'));
						Object aliasValue = levelMap.get(alias);
						if (aliasValue != null) {
							levelMap.put(key, aliasValue);
						}
					}
				} else if (value instanceof Map) {
					Map map = (Map) value;
					Set<Map.Entry<String, Object>> set2 = map.entrySet();
					for (Map.Entry<String, Object> entry2 : set2) {
						String key2 = entry2.getKey();
						Object value2 = entry2.getValue();
						if (value2 instanceof String) {
							String tmp = (String) value2;
							if (tmp.startsWith("${") && tmp.endsWith("}")) {
								String alias = tmp.substring(2, tmp.indexOf('}'));
								Object aliasValue = levelMap.get(alias);
								if (aliasValue != null) {
									map.put(key2, aliasValue);
								}
							}
						}
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Object getSchemaMap(String schemaName)
	{
		return levelMap.get(schemaName);
	}

	/**
	 * JsonLiteWrapper class, Map(KeyType name, KeyInfo)
	 */
	private static Map<Class<?>, Map<String, KeyInfo>> wrapperMap = new HashMap();
	
	public static Map<String, KeyInfo> getSchemaMap(KeyType keyType)
	{
		if (keyType == null) {
			return null;
		}
		return getSchemaMap(keyType.getDomainClass());
	}
	
	public static Map<String, KeyInfo> getSchemaMap(IJsonLiteWrapper<?> wrapper) throws ClassNotFoundException
	{
		if (wrapper == null) {
			return null;
		}
		return getSchemaMap(wrapper.getClass());
	}

	/**
	 * Returns a map of KeyType name and KeyInfo paired entries for the
	 * specified JsonLite wrapper. The returned map contains pairs for only
	 * those wrapper "get" methods that have the return type of JsonLiteWrapper.
	 * 
	 * @param wrapper
	 *            Returns null if the specified wrapper is null. Otherwise, it
	 *            always returns a non-null Map which may contain 0 or more
	 *            entries.
	 * @throws JsonLiteException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, KeyInfo> getSchemaMap(Class<?> wrapperClass) throws JsonLiteException
	{
		if (wrapperClass == null || wrapperClass.isAssignableFrom(IJsonLiteWrapper.class)) {
			return null;
		}
		try {
			Map<String, KeyInfo> wrapperInfoMap = wrapperMap.get(wrapperClass);
			if (wrapperInfoMap == null) {
				wrapperInfoMap = new HashMap();
				KeyInfo rootInfo = new KeyInfo();
				IJsonLiteWrapper wrapper = (IJsonLiteWrapper) wrapperClass.newInstance();
				rootInfo.returnType = wrapperClass;
				rootInfo.keyType = wrapper.toJsonLite().getKeyType();
				wrapperInfoMap.put("__root", rootInfo);
				Method[] methods = wrapper.getClass().getMethods();
				for (Method method : methods) {
					if (method.getName().length() > 3 && method.getName().startsWith("get")
							&& method.getParameterTypes().length == 0 && method.isVarArgs() == false
							&& Modifier.isStatic(method.getModifiers()) == false) {
						String key = method.getName().substring(3);
						KeyInfo keyInfo = new KeyInfo();
						// "get" method

						JsonLiteType parser = method.getAnnotation(JsonLiteType.class);
						Class returnClass = null;
						Class parserClass = null;
						if (parser != null) {
							String retType = parser.returnType().trim();
							if (retType.length() != 0) {
								returnClass = Class.forName(retType);
							}
							String compType = parser.componentType().trim();
							if (compType.length() != 0) {
								parserClass = Class.forName(compType);
							}
						}

						if (returnClass == null) {
							keyInfo.returnType = method.getReturnType();
						} else {
							keyInfo.returnType = returnClass;
						}
						keyInfo.componentType = keyInfo.returnType.getComponentType();
						if (Collection.class.isAssignableFrom(keyInfo.returnType)) {
							keyInfo.componentType = parserClass;
						}
						KeyType keyType = null;
						if (keyInfo.returnType != null && IJsonLiteWrapper.class.isAssignableFrom(keyInfo.returnType)) {
							IJsonLiteWrapper wrapper2 = (IJsonLiteWrapper) keyInfo.returnType.newInstance();
							keyInfo.keyType = wrapper2.toJsonLite().getKeyType();
						} else if (keyInfo.componentType != null
								&& IJsonLiteWrapper.class.isAssignableFrom(keyInfo.componentType)) {
							IJsonLiteWrapper wrapper2 = (IJsonLiteWrapper) keyInfo.componentType.newInstance();
							keyInfo.keyType = wrapper2.toJsonLite().getKeyType();
						}
						wrapperInfoMap.put(key, keyInfo);
					}
				}
				wrapperMap.put(wrapper.getClass(), wrapperInfoMap);
			}
			return wrapperInfoMap;
		} catch (Exception ex) {
			throw new JsonLiteException(ex);
		}
	}

	public static class KeyInfo
	{
		/**
		 * keyType represents returnType or componentType but not both. Only one
		 * of them can yield KeyType.
		 */
		public KeyType keyType;

		/**
		 * returnType is the return type of the POJO method.
		 * {@link JsonLiteType#returnType()} overrides the method return type.
		 */
		public Class returnType;

		/**
		 * component type of array if returnType is array, i.e., Position[].
		 * generic type of class or the component type of array if
		 * {@link JsonLiteType#componentType()} is defined.
		 */
		public Class componentType;
	}
}
