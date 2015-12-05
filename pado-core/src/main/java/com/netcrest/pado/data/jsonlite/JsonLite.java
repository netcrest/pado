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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.Delta;
import com.gemstone.gemfire.InvalidDeltaException;
import com.gemstone.gemfire.internal.HeapDataOutputStream;
import com.netcrest.pado.data.InvalidKeyException;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.data.jsonlite.JsonLiteSchemaManager.KeyInfo;
import com.netcrest.pado.data.jsonlite.internal.JsonLiteHelper;
import com.netcrest.pado.data.jsonlite.internal.JsonLiteSerializer;
import com.netcrest.pado.gemfire.factory.GemfireVersionSpecifics;
import com.netcrest.pado.gemfire.util.DataSerializerEx;

/**
 * JsonLite is a data class for efficiently storing and retrieving data to/from
 * Pado grids. It is a lightweight JSON class designed for delivering
 * self-describing messages over the network without the cost of embedded keys
 * in the wire format. JsonLite achieves this by predefining the keys in the
 * form of enum (and String) constants and deploying them as part of the
 * application binary classes. The enum key classes are automatically versioned
 * and generated using the provided IDE plug-in, ensuring full compatibility and
 * coexistence with other versions.
 * <p>
 * In addition to the code generator, JsonLite includes the following Pado
 * optimization and operational features while maintaining the same level of
 * self-describing message accessibility.
 * <p>
 * <ul>
 * <li>JsonLite is fully JSON compliant.</li>
 * <li>JsonLite implements {@link java.util.Map}.</li>
 * <li>JsonLite is in general significantly lighter than JSON, POJO, and
 * {@link HashMap}. Its wire format is compact and does not require class
 * reflection.</li>
 * <li>JsonLite is faster than JSON, POJO and HashMap. Its smaller payload size
 * means it is serialized and delivered faster.</li>
 * <li>JsonLite lookup is faster than HashMap and JSON. JsonLite keeps values
 * internally indexed in an array for faster access.</li>
 * <li>JsonLite supports delta propagation.
 * <li>JsonLite fully supports selective key inflation (SKI). With SKI, the
 * underlying JsonLite mechanism inflates only the values that are accessed by
 * the application. The rest of the values are kept deflated until they are
 * accessed. This reduces the memory footprint and eliminates the unnecessary
 * latency overhead introduced by the serialization and deserialization
 * operations.</li>
 * <li>JsonLite fully supports the underlying data grid query service such as
 * GemFire's OQL.</li>
 * <li>JsonLite is fully integrated with the key class versioning mechanism,
 * which enables multiple versions of JsonLite key sets to coexist in the
 * fabric. All versioned key classes are fully forward and backward compatible.</li>
 * <li>JsonLite key classes are universally unique across space and time.</li>
 * <li>JsonLite is language neutral.</li>
 * </ul>
 * <p>
 * <h3>Lighter and Faster</h3>
 * JsonLite, in general, is significantly lighter and faster than HashMap and
 * JSON. The size of a typical serialized JsonLite object is considerably
 * smaller than the counterpart HashMap and JSON objects. Enum
 * {@link #get(KeyType)} calls are faster than {@link HashMap#get(Object)}
 * because the values are indexed in an array, circumventing the more expensive
 * hash lookup operation.
 * 
 * <h3>Map with enum KeyType Keys</h3>
 * JsonLite implements Map and therefore has the same Map methods and behaves
 * exactly like Map. Unlike HashMap which also implements Map, a JsonLite object
 * is restricted to a fixed set of predefined keys in an enum class that
 * implements the interface KeyType. This restriction effectively makes JsonLite
 * lighter, faster, and more acquiescent than HashMap and JSON. It removes the
 * keys from the wire format and provides a valid key list for strict allowed
 * key and type checking.
 * 
 * <h3>Code Generator</h3>
 * Editing keys, although it can be done manually, is done via the provided IDE
 * plug-in which automatically generates a new version of the enum class. The
 * built-in versioning mechanism allows the new versioned enum class to be
 * deployed to the servers and clients during runtime without the requirement of
 * restarting them. The servers automatically load the new versioned class
 * making it immediately available to the application along with the previous
 * versions.
 * 
 * <h3>String Keys</h3>
 * In addition to the enum keys, JsonLite also supports String keys. String keys
 * are costlier than enum keys but comparable to HashMap and JSON in terms of
 * the put and get speeds. One of the benefits of using String keys is the
 * flexibility of executing ad hoc queries. JsonLite is fully compliant with the
 * GemFire query service, making it ideal for object-relational mapping.
 * 
 * <p>
 * <h3>Using JsonLite</h3>
 * <ol>
 * <li>
 * Create a <code>{@link KeyType}</code> enum class using the code generator.</li>
 * <li>
 * Register the new <code>KeyType</code> enum class using
 * <code>{@link KeyTypeManager}</code>.</li>
 * <li>
 * Use <code>KeyType</code> to create <code>JsonLite</code> objects. Always use
 * {@link #JsonLite(KeyType)} to create <code>JsonLite</code> objects.</li>
 * <li>
 * Put the JsonLite objects into cache regions</li>
 * <li>
 * Get the JsonLite objects from cache regions</li>
 * <li>
 * Get values from the objects using <code>KeyType</code> or <code>String</code>
 * keys</li>
 * </ol>
 * 
 * <h3>Examples</h3>
 * 
 * <pre>
 * import com.netcrest.pado.data.KeyTypeManager;
 * import com.netcrest.pado.data.jsonlite.JsonLite;
 * import jsonlite.examples.model.Dummy;
 * import jsonlite.examples.model.v.Dummy_v1;
 * import jsonlite.examples.model.v.Dummy_v2;
 * 
 * . . .
 *  
 * // Register the Dummy key type. This also registers all of
 * // the versions in the sub-package named v. This registration
 * // call is not required if Pado is used.
 * KeyTypeManager.registerKeyType(Dummy.getKeyType());
 * 
 * // Create a JsonLite object using the latest Dummy version.
 * // In our examples, Dummy is equivalent to Dummy_v2 if
 * // Dummy_v2 is the latest version.
 * JsonLite jl = new JsonLite(Dummy.getKeyType());
 * 
 * // Put data using the Dummy.KMessage key (enum constant).
 * // Note that all KeyType constant names are prefixed with the letter 'K'
 * // to the string key names.
 * jl.put(Dummy.KMessage, "Hello, world.");
 * 
 * // Put data using the string key "Message" which is equivalent to
 * // Dummy.KMessage.
 * jl.put("Message", "Hello, world.");
 * 
 * // Get the value using the Dummy.Message key (enum constant)
 * String message = (String) jl.get(Dummy.KMessage);
 * 
 * // Get the value using the versioned KeyType enum class Dummy_v2 which is
 * // equivalent to Dummy if Dummy_v2 is the latest version.
 * message = (String) jl.get(Dummy_v2.KMessage);
 * 
 * // Get the value using the first version, Dummy_v1.
 * message = (String) jl.get(Dummy_v1.KMessage);
 * ,
 * // Get the value using the string key "Message".
 * message = (String) jl.get("Message");
 * </pre>
 * 
 * @author dpark
 * 
 */
public class JsonLite<V> implements KeyMap<V>, Externalizable, Cloneable, DataSerializable, Delta
{
	private static final long serialVersionUID = 1L;

	private static boolean isGemfire = true;

	private static final byte FLAG_KEY_TYPE = 0;

	private static final int BIT_MASK_SIZE = 32; // int type

	static {
		String val = System.getProperty("pado.jsonlite.type", "gemfire");
		if (val.equalsIgnoreCase("gemfire")) {
			isGemfire = true;
		} else {
			isGemfire = false;
		}
	}

	private KeyType keyType;
	private Object[] values;
	private transient Map<String, Object> referenceMap;

	// threadReferenceMap is used by the server to serialize the reference map
	// belonging to the specified thread.
	private transient Map<Object, Map<String, Object>> threadReferenceMap;

	/**
	 * If JsonLite is not initialized with KeyType then valueMap is used instead
	 * of values to store key/value pairs. If KeyType is defined, then valueMap
	 * is always empty and never used.
	 */
	Map<String, V> valueMap;
	private int keyVersion;
	private int[] dirtyFlags;
	private byte internalFlag;

	// serialized values
	private byte[] serializedBytes;

	/**
	 * <font COLOR="#ff0000"><strong>The use of this constructor is strongly
	 * discouraged. For best performance, always use {@link #JsonLite(KeyType)}
	 * wherever possible. If KeyType is not initially provided then JsonLite
	 * honors only String keys.</strong></font>
	 * <p>
	 * The default constructor creates a new JsonLite object with KeyType
	 * undefined. Undefined KeyType may lead to undesired effects. The following
	 * restriction applies if this constructor is invoked and KeyType is used
	 * thereafter:
	 * 
	 * <blockquote><i> {@link #put(KeyType, Object)} or {@link #get(KeyType)}
	 * must be invoked once with a {@link KeyType} enum constant before the
	 * JsonLite object can be used. These methods implicitly initialize the
	 * JsonLite object with the specified key type. JsonLite ignores String keys
	 * until the key type has been assigned. </i></blockquote>
	 * 
	 * An exception to the above restriction is {@link #putAll(Map)} with the
	 * argument type of JsonLite. If JsonLite is passed in, then putAll()
	 * transforms this <i>empty</i> JsonLite object into the passed-in JsonLite
	 * key type.
	 * <p>
	 * It is recommended that the overloaded constructor
	 * {@link #JsonLite(KeyType)} should always be used wherever possible. This
	 * default constructor is primarily for satisfying Java serialization
	 * restrictions in addition to handling special operations such as putAll().
	 * 
	 */
	public JsonLite()
	{
	}

	/**
	 * Creates a new JsonLite object with the specified key type. Once created,
	 * all subsequent operations must use the same <code>KeyType</code> enum
	 * class. The key type is obtained from the <i>no-arg</i> static method,
	 * <code>getKeyType()</code>, included in the generated key type class. For
	 * example,
	 * 
	 * <pre>
	 * JsonLite jl = new JsonLite(Dummy.getKeyType());
	 * </pre>
	 * 
	 * @param keyType
	 *            The key type enum constant to assign to JsonLite.
	 */
	public JsonLite(KeyType keyType)
	{
		init(keyType);
	}

	/**
	 * Creates a new JsonLite object read from the specified file.
	 * 
	 * @param file
	 *            File that contains a Json string representation.
	 * @throws FileNotFoundException
	 *             Thrown if the file is not found.
	 * @throws IOException
	 *             Thrown if the file cannot be read.
	 */
	public JsonLite(File file) throws FileNotFoundException, IOException
	{
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			JsonLiteTokenizer jt = new JsonLiteTokenizer(reader, null);
			init(jt.getKeyType());
			init(jt, null);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Creates a new JsonLite object with the specified JSON string and the
	 * wrapper object. The wrapper object provides the schema information needed
	 * to construct the matching JsonLite object.
	 * 
	 * @param jsonString
	 *            JSON string conforming to the JSON language spec and
	 *            optionally the JsonLite serialization spec. The wrapper object
	 *            overrides the JsonLite specifics in the string if any.
	 * @param wrapper
	 *            JsonLite wrapper that contains schema info.
	 */
	@SuppressWarnings("rawtypes")
	public JsonLite(String jsonString, IJsonLiteWrapper wrapper)
	{
		this(wrapper.toJsonLite().getKeyType());
		try {
			init(new JsonLiteTokenizer(jsonString, JsonLiteSchemaManager.getSchemaMap(wrapper)), null);
		} catch (ClassNotFoundException e) {
			throw new JsonLiteException(e);
		}
	}

	/**
	 * Creates a new JsonLite object with the specified JSON string and the
	 * wrapper class. The wrapper class provides the schema information needed
	 * to construct the matching JsonLite object.
	 * 
	 * @param jsonString
	 *            JSON string conforming to the JSON language spec and
	 *            optionally the JsonLite serialization spec. The wrapper object
	 *            overrides the JsonLite specifics in the string if any.
	 * @param wrapper
	 *            JsonLite wrapper class that contains schema info.
	 */
	@SuppressWarnings("rawtypes")
	public JsonLite(String jsonString, Class<IJsonLiteWrapper> wrapperClass)
	{
		Map<String, KeyInfo> map = JsonLiteSchemaManager.getSchemaMap(wrapperClass);
		if (map != null) {
			KeyInfo keyInfo = map.get("__root");
			if (keyInfo != null) {
				init(keyInfo.keyType);
			}
		}
		init(new JsonLiteTokenizer(jsonString, map), null);
	}

	/**
	 * Creates a new JsonLite object with the specified JSON string. If the JSON
	 * string is the standard JSON representation without JsonLite specifics
	 * then the JsonLite object will be backed by a Map object.
	 * 
	 * @param jsonString
	 *            JSON string conforming to the JSON language spec and
	 *            optionally the JsonLite serialization spec.
	 */
	public JsonLite(String jsonString)
	{
		this(jsonString, (Map<String, JsonLiteSchemaManager.KeyInfo>) null);
	}

	/**
	 * Creates a new JsonLite object with the specified JSON string and map of
	 * (string key, KeyType) pairs.
	 * 
	 * @param jsonString
	 *            JSON string conforming to the JSON language spec and
	 *            optionally the JsonLite serialization spec.
	 * @param schemaTypeMap
	 *            Map of (string key, KeyType) pairs for deserializing the
	 *            specified JSON string.
	 */
	public JsonLite(String jsonString, Map<String, JsonLiteSchemaManager.KeyInfo> schemaTypeMap)
	{
		this(new JsonLiteTokenizer(jsonString, schemaTypeMap), null);
	}

	/**
	 * Creates a new JsonLite object with the specified JSON string and schema
	 * name.
	 * 
	 * @param jsonString
	 *            JSON string conforming to the JSON language spec and
	 *            optionally the JsonLite serialization spec.
	 * @param schemaName
	 *            Name of the schema for parsing the JSON string.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JsonLite(String jsonString, String schemaName)
	{
		Object obj = JsonLiteSchemaManager.getSchemaMap(schemaName);
		if (obj instanceof Map) {
			init(new JsonLiteTokenizer(jsonString, (Map) obj), null);
		} else if (KeyType.class.isAssignableFrom((Class) obj)) {
			// KeyType
			Class clazz = (Class) obj;
			Object[] enums = clazz.getEnumConstants();
			if (enums != null && enums.length > 0) {
				keyType = (KeyType) enums[0];
			} else {
				throw new JsonLiteException("Invalid KeyType enum class: " + clazz.getCanonicalName());
			}
			init(keyType);
			init(new JsonLiteTokenizer(jsonString, keyType), null);
		} else {
			init(new JsonLiteTokenizer(jsonString, (Class) obj), null);
		}
	}

	/**
	 * Creates a new JsonLite object with the specified tokenizer.
	 * 
	 * @param tokenizer
	 *            JsonLite tokenizer.
	 */
	public JsonLite(JsonLiteTokenizer tokenizer, KeyType mappedKeyType)
	{
		if (tokenizer != null) {
			init(tokenizer.getKeyType());
		}
		init(tokenizer, mappedKeyType);
	}

	public JsonLite(Map<String, V> map)
	{
		if (map != null) {
			valueMap = new HashMap<String, V>(map);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void init(JsonLiteTokenizer tokenizer, KeyType mappedKeyType)
	{
		// keyType = mappedKeyType;
		keyType = tokenizer.getKeyType();

		char c;
		String prevKey;
		String key = null;
		Class beanClass = null;
		Object bean = null;
		Map<String, Method> methodMap = null;
		c = tokenizer.nextClean();

		if (c != '{') {
			throw tokenizer.syntaxError("A JsonLite text must begin with '{'");
		}
		for (;;) {
			c = tokenizer.nextClean();
			switch (c) {
			case 0:
				throw tokenizer.syntaxError("A JsonLite text must end with '}'");
			case '}':
				return;
			default:
				tokenizer.back();
				prevKey = key;
				key = tokenizer.nextValue(null, null).toString();
			}

			// The key is followed by ':'.

			c = tokenizer.nextClean();
			if (c != ':') {
				throw tokenizer.syntaxError("Expected a ':' after a key");
			}
			Object value;
			if (key.equals("__h")) {
				value = tokenizer.getHeaderValue();
				keyType = tokenizer.getKeyType();
				beanClass = tokenizer.getBeanClass();
				init(keyType);
				if (beanClass != null) {
					try {
						bean = beanClass.newInstance();
						// get all setters
						Method methods[] = bean.getClass().getMethods();
						methodMap = new HashMap(methods.length, 1f);
						for (int i = 0; i < methods.length; i++) {
							if (methods[i].getName().startsWith("set") && methods[i].getParameterTypes().length == 1) {
								methodMap.put(methods[i].getName(), methods[i]);
							}
						}
					} catch (Exception ex) {
						throw new JsonLiteException(ex);
					}
				}
				if (value instanceof Map) {
					valueMap = (Map) value;
				}
				if (keyType == null && beanClass == null && valueMap != value && prevKey != null) {
					valueMap = createValueMapIfNotExist();
					valueMap.put(prevKey, (V) value);
				}
			} else {
				if (keyType != null) {
					KeyType kt = keyType.getKeyType(key);
					if (kt == null) {
						throw new JsonLiteException("Invalid key: " + key + ". It does not exist in "
								+ keyType.getClass().getCanonicalName());
					}
					Class ktType = kt.getType();
					value = tokenizer.nextValue(key, JsonLiteTokenizer.getTypeName(ktType));
				} else {
					keyType = tokenizer.getKeyType();
					if (keyType != null) {
						KeyType kt = keyType.getKeyType(key);
						if (kt == null) {
							throw new JsonLiteException("Invalid key: " + key + ". It does not exist in "
									+ keyType.getClass().getCanonicalName());
						}
						Class ktType = kt.getType();
						value = tokenizer.nextValue(key, JsonLiteTokenizer.getTypeName(ktType));
					} else {
						value = tokenizer.nextValue(key, null);
					}
				}
				if (bean != null) {
					Method method = methodMap.get("set" + key);
					if (method == null) {
						throw new JsonLiteException("Undefined setter method: " + bean.getClass().getCanonicalName()
								+ ".set" + key);
					}
					try {
						Class argType = method.getParameterTypes()[0];
						if (argType == Byte.class || argType == byte.class) {
							if (value.getClass().isPrimitive()) {
								method.invoke(bean, (Byte) value);
							} else if (value instanceof Integer) {
								method.invoke(bean, ((Integer) value).byteValue());
							} else {
								method.invoke(bean, value);
							}
						} else if (argType == Date.class) {
							Date date = JsonLiteHelper.parseDate(value.toString());
							method.invoke(bean, date);
						} else {
							method.invoke(bean, value);
						}
					} catch (Exception ex) {
						throw new JsonLiteException("Invalid key for the setter method: " + method + " - "
								+ bean.getClass().getCanonicalName() + ".set" + key + ", " + ex + ", value=" + value);
					}
				} else {
					put(key, (V) value);
				}
			}

			// Pairs are separated by ','.
			c = tokenizer.nextClean();
			switch (c) {
			case ';':
			case ',':
				if (tokenizer.nextClean() == '}') {
					return;
				}
				tokenizer.back();
				break;
			case '}':
				if (keyType == null && valueMap != null && key == null) {
					tokenizer.nonJsonLiteValue = value;
				} else if (bean != null) {
					tokenizer.nonJsonLiteValue = bean;
				} else if (value != null && (value instanceof Collection || value.getClass().isArray())) {
					tokenizer.nonJsonLiteValue = value;
				}
				return;
			default:
				throw tokenizer.syntaxError("Expected a ',' or '}'");
			}
		}
	}
	
	private Map<String, V> createValueMapIfNotExist()
	{
		if (valueMap == null) {
			valueMap = new HashMap<String, V>(32);
		}
		return valueMap;
	}
	
	private Map<Object, Map<String, Object>> createThreadReferenceMapIfNotExist()
	{
		if (threadReferenceMap == null) {
			threadReferenceMap = new HashMap<Object, Map<String, Object>>(4);
		}
		return threadReferenceMap;
	}
	

	/**
	 * Initializes the JsonLite object by creating data structures for the
	 * specified key type.
	 * 
	 * @param keyType
	 *            The key type enum constant to assign to JsonLite.
	 */
	private void init(KeyType keyType)
	{
		if (keyType == null) {
			return;
		}
		this.keyType = keyType;
		this.keyVersion = keyType.getVersion();
		int count = keyType.getKeyCount();
		this.values = new Object[count];
		int dirtyFlagCount = calculateDirtyFlagCount();
		this.dirtyFlags = new int[dirtyFlagCount];

		if (KeyTypeManager.isRegistered(keyType) == false) {
			KeyTypeManager.registerKeyType(keyType);
		}
	}

	/**
	 * Calculates the dirty flag count. The dirty flags are kept in an array of
	 * integers. Each integer value represents 32 dirty flags.
	 * 
	 * @return Returns the dirty flag count.
	 */
	private int calculateDirtyFlagCount()
	{
		int count = keyType.getKeyCount();
		int dirtyFlagCount = count / BIT_MASK_SIZE;
		int reminder = count % BIT_MASK_SIZE;
		if (reminder > 0) {
			dirtyFlagCount++;
		}
		return dirtyFlagCount;
	}

	/**
	 * Marks all keys dirty.
	 */
	private void dirtyAllKeys()
	{
		if (dirtyFlags != null) {
			for (int i = 0; i < dirtyFlags.length; i++) {
				dirtyFlags[i] = 0xFFFFFFFF;
			}
		}
	}

	/**
	 * Clears the entire dirty flags.
	 */
	private void clearDirty()
	{
		if (dirtyFlags != null) {
			for (int i = 0; i < dirtyFlags.length; i++) {
				dirtyFlags[i] = 0x0;
			}
		}
	}

	/**
	 * Returns the key type constant used to initialize this object.
	 */
	@Override
	public KeyType getKeyType()
	{
		return keyType;
	}

	/**
	 * Returns the value of the specified key type. If the default constructor
	 * {@link #JsonLite()} is used to create this object then this method
	 * implicitly initializes itself with the specified key type if it has not
	 * been initialized previously.
	 * 
	 * @param keyType
	 *            The key type constant to lookup the mapped value.
	 * @return Returns the mapped value. It returns null if the value does not
	 *         exist or it was explicitly set to null.
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public V get(KeyType keyType)
	{
		if ((keyType = validateKey(keyType)) == null) {
			return null;
		}

		// Initialization is not thread safe.
		// It allows the use of the default constructor but at
		// the expense of the lack of thread safety.
		// if (values == null) {
		// init(keyType);
		// }

		if (keyType.isKeyKeepSerialized()) {
			deserialize();
		}
		// KeyType translatedKeyType =
		// KeyTypeManager.translateKeyTypeVersion(keyType, this.keyType);
		// if (translatedKeyType == null) {
		// return null;
		// }
		return (V) values[keyType.getIndex()];
	}

	/**
	 * 
	 * @param keyType
	 * @param wrapper
	 * @return
	 */
	// public V get(KeyType keyType, boolean returnWrapper)
	// {
	// if ((keyType = validateKey(keyType)) == null) {
	// return null;
	// }
	//
	// // Initialization is not thread safe.
	// // It allows the use of the default constructor but at
	// // the expense of the lack of thread safety.
	// // if (values == null) {
	// // init(keyType);
	// // }
	//
	// deserialize();
	// // KeyType translatedKeyType =
	// // KeyTypeManager.translateKeyTypeVersion(keyType, this.keyType);
	// // if (translatedKeyType == null) {
	// // return null;
	// // }
	// Object val = values[keyType.getIndex()];
	// if (returnWrapper == false && val instanceof IJsonLiteWrapper) {
	// return (V)((IJsonLiteWrapper)val).toJsonLite();
	// }
	// return (V)val;
	// }

	private KeyType validateKey(KeyType keyType) throws InvalidKeyException
	{
		if (keyType == null) {
			return null;
		}

		// If the JsonLite object has not been initialized then use the
		// passed-in key type to initialize it.
		// IMPORTANT: Note that initialization is not thread safe.
		// It allows the use of the default constructor but at
		// the expense of the lack of thread safety.
		// To avoid this, always create JsonLite using the JsonLite(KeyType)
		// constructor.
		if (this.keyType == null) {
			init(keyType);
		}
		return keyType;

		// TODO: The following validation is costly. Need to optimize it.
		// No detailed validation for now.

		// if (this.keyType.getId().equals(keyType.getId()) == false) {
		// throw new
		// InvalidKeyException("The passed-in KeyType does not match. [this key type: "
		// + this.keyType.getClass().getName() + ", passed-in invalid type: " +
		// keyType.getClass().getName()
		// + "]");
		// }
		//
		// KeyType translatedKeyType =
		// KeyTypeManager.translateKeyTypeVersion(keyType, this.keyType);
		// return translatedKeyType;
	}

	/**
	 * Puts the specified value mapped by the specified key type into this
	 * object. If the default constructor {@link #JsonLite()} is used to create
	 * this object then this method implicitly initializes itself with the
	 * specified key type if it has not been initialized previously.
	 * <p>
	 * Note that for JsonLite to be language neutral, the value type must be a
	 * valid type. It must be strictly enforced by the application. For Java
	 * only applications, any Serializable objects are valid.
	 * 
	 * @param keyType
	 *            The key type constant to lookup the mapped value.
	 * @param value
	 *            The value to put into the JsonLite object.
	 * @return Returns the old value. It returns null if the old value does not
	 *         exist or has been explicitly set to null.
	 * @throws InvalidKeyException
	 *             A runtime exception thrown if the passed in value type does
	 *             not match the key type.
	 */
	@SuppressWarnings({ "unchecked" })
	private V put_org(KeyType keyType, V value) throws InvalidKeyException
	{
		if ((keyType = validateKey(keyType)) == null) {
			return null;
		}

		V oldVal = (V) values[keyType.getIndex()];
		try {
			// values[keyType.getIndex()] = keyType.getType().cast(value);
			values[keyType.getIndex()] = value;
		} catch (ClassCastException ex) {
			throw new InvalidKeyException("Invalid value class " + value.getClass().getName() + ". Expected "
					+ keyType.getType().getName() + ".", ex);
		}

		setDirty(keyType, dirtyFlags);
		return oldVal;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public V put(KeyType keyType, V value) throws InvalidKeyException
	{
		if ((keyType = validateKey(keyType)) == null) {
			return null;
		}

		if (value != null && value instanceof IJsonLiteWrapper) {
			if (((IJsonLiteWrapper) value).toJsonLite().getKeyType().getClass().isAssignableFrom(keyType.getType())) {
				throw new InvalidKeyException("Invalid value type: KeyType=" + keyType + ", value=" + value.getClass()
						+ "(KeyType="
						+ ((IJsonLiteWrapper) value).toJsonLite().getKeyType().getType().getCanonicalName() + ")");
			}
		}

		if (keyType.isKeyKeepSerialized()) {
			deserialize();
		}

		V oldVal = ((V) values[keyType.getIndex()]);
		values[keyType.getIndex()] = value;

		setDirty(keyType, dirtyFlags);
		return oldVal;
	}

	/**
	 * Returns the mapped value for the specified key. It uses the String value
	 * of the key, i.e., key.toString(), to lookup the mapped value.
	 * 
	 * @param key
	 *            The key object.
	 */
	@Override
	public V get(Object key)
	{
		if (key == null) {
			return null;
		}
		if (keyType == null) {
			if (valueMap == null) {
				return null;
			} else {
				return valueMap.get(key);
			}
		}
		KeyType keyType = this.keyType.getKeyType(key.toString());
		if (keyType == null) {
			return null;
		}
		return get(keyType);
	}

	// private Map getMap(JsonLite jl, Map map)
	// {
	// Set<Map.Entry> set = jl.entrySet();
	// for (Entry entry : set) {
	// map.put(entry.getKey(), entry.getValue());
	// }
	// }

	// public Object get(String key, Class returnClass)
	// {
	// Object val = get(key);
	// if (val == null) {
	// return null;
	// }
	// if (returnClass == null) {
	// return val;
	// }
	// try {
	// if (returnClass == Date.class) {
	// return iso8601DateFormat.parse(val.toString());
	// } else if (returnClass == BigDecimal.class) {
	// return new BigDecimal((Double) val);
	// } else if (returnClass == BigInteger.class){
	// return new BigInteger(val.toString());
	// // } else if (returnClass == HashMap.class) {
	// // JsonLite jl = (JsonLite)val;
	// // HashMap map = new HashMap(jl.size(), 1f);
	// // Set<Map.Entry> set = jl.entrySet();
	// // for (Entry entry : set) {
	// // map.put(entry.getKey(), entry.getValue());
	// // }
	// // return map;
	// } else {
	// return val;
	// }
	// } catch (Exception ex) {
	// throw new JsonLiteException(ex);
	// }
	// }

	/**
	 * Puts the specified value mapped by the specified key into this object.
	 * Unlike {@link #put(KeyType, Object)}, this method will not implicitly
	 * initialize this object if the default constructor is used. If this object
	 * has not been initialized with KeyType, then it is backed by Map<String,
	 * V>.
	 * 
	 * @param key
	 *            The key object.
	 * @param value
	 *            The value to put into the JsonLite object.
	 * @return Returns the old value.
	 */
	@Override
	public V put(String key, V value) throws InvalidKeyException
	{
		if (keyType == null) {
			valueMap = createValueMapIfNotExist();
			return valueMap.put(key, value);
		}
		KeyType keyType = this.keyType.getKeyType(key);
		if (keyType == null) {
			return null;
		}
		return put(keyType, value);
	}

	@Override
	public void putReference(String key, Object value, Object keyMapReferenceId) throws InvalidKeyException
	{
		if (key == null) {
			return;
		}
		threadReferenceMap = createThreadReferenceMapIfNotExist();
		Map<String, Object> rm = threadReferenceMap.get(keyMapReferenceId);
		if (rm == null) {
			rm = new HashMap<String, Object>(8);
			threadReferenceMap.put(keyMapReferenceId, rm);
		}
		rm.put(key, value);
	}

	@Override
	public void putReference(KeyType refKeyType, Object value, Object keyMapReferenceId)
	{
		if (keyType == null) {
			return;
		}
		if (keyType.isKeyKeepSerialized()) {
			deserialize();
		}
		putReference(refKeyType.getName(), value, keyMapReferenceId);
	}

	public Object getReference(KeyType refKeyType)
	{
		if (refKeyType == null) {
			return null;
		}
		return getReference(refKeyType.getName());
	}

	public Object getReference(String key)
	{
		if (referenceMap == null || key == null) {
			return null;
		}
		return referenceMap.get(key);
	}

	/**
	 * Returns true if this object has KeyType defined. If it returns false then
	 * the entries are stored in Map<String, V>.
	 */
	public boolean isKeyType()
	{
		return keyType != null;
	}

	/**
	 * Returns true if delta propagation is enabled and there are changes in
	 * values.
	 */
	@Override
	public boolean hasDelta()
	{
		if (keyType != null && keyType.isDeltaEnabled()) {
			return isDirty();
		}
		return false;
	}

	/**
	 * Returns true if there are changes made in values.
	 */
	@Override
	public boolean isDirty()
	{
		for (int i = 0; i < dirtyFlags.length; i++) {
			if ((dirtyFlags[i] & 0xFFFFFFFF) != 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the bit of the specified flag.
	 * 
	 * @param bit
	 *            Bit position starting from 0.
	 * @param flag
	 *            Flag to set
	 * @return Flag with the specified bit set.
	 */
	private int setDirty(int bit, int flag)
	{
		return flag | 1 << bit;
	}

	private int unsetDirty(int bit, int flag)
	{
		return flag & ~(1 << bit);
	}

	/**
	 * Sets the specified key type dirty.
	 * 
	 * @param keyType
	 *            The key type to set dirty.
	 * @param flags
	 *            The flags that contain the key type.
	 */
	private void setDirty(KeyType keyType, int flags[])
	{
		int index = keyType.getIndex();
		setDirty(index, flags);
	}

	/**
	 * Sets the specified contiguous bit of the flags. A contiguous bit is the
	 * bit number of the contiguous array integers. For example, if the flags
	 * array size is 2 then the contiguous bit of 32 represents the first bit of
	 * the flags[1] integer, 33 represents the second bit, and etc.
	 * 
	 * @param contiguousBit
	 *            The contiguous bit position.
	 * @param flags
	 *            The bit flags.
	 */
	private void setDirty(int contiguousBit, int flags[])
	{
		int dirtyFlagsIndex = contiguousBit / BIT_MASK_SIZE;
		int bit = contiguousBit % BIT_MASK_SIZE;
		flags[dirtyFlagsIndex] |= 1 << bit;
	}

	/**
	 * Returns true if the specified key type is dirty.
	 * 
	 * @param keyType
	 *            The key type to check.
	 * @param flags
	 *            The flags that contain the key type.
	 */
	private boolean isBitDirty(KeyType keyType, int flags[])
	{
		int index = keyType.getIndex();
		int dirtyFlagsIndex = index / BIT_MASK_SIZE;
		int bit = index % BIT_MASK_SIZE;
		return isBitDirty(flags[dirtyFlagsIndex], bit);
	}

	/**
	 * Returns true if the specified contiguous bit of the flags is set. A
	 * contiguous bit the bit number of the contiguous array integers. For
	 * example, if the flags array size is 2 then the contiguous bit of 32
	 * represents the first bit of the flags[1] integer, 33 represents the
	 * second bit, and etc.
	 * 
	 * @param contiguousBit
	 *            The contiguous bit position
	 * @param flags
	 *            The bit flags
	 */
	@SuppressWarnings("unused")
	private boolean isDirty(int contiguousBit, int flags[])
	{
		int dirtyFlagsIndex = contiguousBit / BIT_MASK_SIZE;
		int bit = contiguousBit % BIT_MASK_SIZE;
		return isBitDirty(flags[dirtyFlagsIndex], bit);
	}

	/**
	 * Returns true if the specified flag bit is dirty.
	 * 
	 * @param flag
	 *            The flag to check.
	 * @param bit
	 *            The bit to compare.
	 * @return
	 */
	private boolean isBitDirty(int flag, int bit)
	{
		return ((flag >> bit) & 1) == 1;
	}

	/**
	 * Returns true if the any of the flag bits is dirty.
	 * 
	 * @param flag
	 *            The flag to check.
	 */
	private boolean isDirty(int flag)
	{
		return (flag & 0xFFFFFFFF) != 0;
	}

	/**
	 * Deserializes (inflates) the serialized bytes if has not been done.
	 */
	void deserialize()
	{
		byte[] byteArray = serializedBytes;
		if (byteArray != null) {
			KeyType[] keyTypeValues = keyType.getValues(keyVersion);
			ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
			DataInputStream dis = new DataInputStream(bais);

			try {
				if (isGemfire) {
					// ObjectInputStream dis = new ObjectInputStream(bais);
					for (int i = 0; i < keyTypeValues.length; i++) {
						if (keyTypeValues[i].isKeyKeepSerialized()) {
							// deserialized values
							try {
								values[i] = readValueGemfire(keyTypeValues, i, dis);
							} catch (Exception ex) {
								throw new RuntimeException("Desserialization error: [KeyTypeClass="
										+ keyTypeValues[i].getClass().getName() + ", keyType="
										+ keyTypeValues[i].getName() + ", index=" + i + "]", ex);
							}
						}
					}
				} else {
					// ObjectInputStream dis = new ObjectInputStream(bais);
					for (int i = 0; i < keyTypeValues.length; i++) {
						if (keyTypeValues[i].isKeyKeepSerialized()) {
							// deserialized values
							values[i] = readValue(keyTypeValues, i, dis);
						}
					}
				}
				dis.close();
				serializedBytes = null;
			} catch (Exception e) {
				throw new JsonLiteException("Deserialization failed: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Reads the value at for the specified key type index.
	 * 
	 * @param keyTypes
	 *            The entire key types that represent the JsonLite values.
	 * @param index
	 *            The index of the key to read.
	 * @param input
	 *            The input stream.
	 * @return Returns the read value.
	 * @throws IOException
	 *             Thrown if an IO error encountered.
	 * @throws ClassNotFoundException
	 */
	private Object readValue(KeyType[] keyTypes, int index, DataInput input) throws IOException, ClassNotFoundException
	{
		return JsonLiteSerializer.read(keyTypes[index].getType(), input);
	}

	private Object readValueGemfire(KeyType[] keyTypes, int index, DataInput input) throws IOException,
			ClassNotFoundException
	{
		return DataSerializerEx.read(keyTypes[index].getType(), input);
	}

	/**
	 * Upgrades this object to the latest KeyType version found in the class
	 * loader.
	 */
	private void upgrade()
	{
		if (keyType == null) {
			return;
		}
		KeyType latestKeyType = KeyTypeManager.getLatestKeyTypeVersion(keyType);
		merge(latestKeyType);
	}

	/**
	 * Merges this object to the specified key type version. Note that for both
	 * upgrade and downgrade drop attributes that are not part of the specified
	 * key type.
	 * 
	 * @param toKeyType
	 *            Key type to merge to.
	 */
	@Override
	public void merge(KeyType toKeyType)
	{
		if (keyType == null || toKeyType == null) {
			return;
		}
		if (this.keyType.getId().equals(toKeyType.getId()) == false) {
			return;
		}
		if (keyType.getVersion() == toKeyType.getVersion()) {
			return;
		}

		if (keyType.getVersion() < toKeyType.getVersion()) {
			upgrade(toKeyType);
		} else {
			downgrade(toKeyType);
		}
	}

	/**
	 * Upgrades this object to the specified key type.
	 * 
	 * @param toKeyType
	 *            Key type to upgrade.
	 */
	private void upgrade(KeyType toKeyType)
	{
		if (keyType == null) {
			return;
		}

		// take care of serializedBytes first
		deserialize();

		// convert
		// TODO: need to handle race conditions
		// int version = KeyTypeManager.getNextMergePoint(keyType);
		Object[] newValues;
		// handle merge points
		Object[] startValues = values;
		// for each subsequent merge point, remove all deprecated
		// keys up to the new values sizes
		KeyType fromKeyType = this.keyType;
		KeyType keyType = KeyTypeManager.getNextMergePointKeyType(fromKeyType);
		while (keyType != null && keyType.getVersion() < toKeyType.getVersion()) {
			KeyType keyTypes[] = keyType.getValues();
			newValues = new Object[keyTypes.length - keyType.getDeprecatedIndexes().length];
			int j = 0;
			for (int i = 0; i < startValues.length; i++) {
				KeyType kt = keyTypes[i];
				if (kt.isDeprecated() == false) {
					newValues[j++] = startValues[i];
				}
			}
			startValues = newValues;
			keyType = KeyTypeManager.getNextMergePointKeyType(keyType.getValues(keyType.getVersion() + 1)[0]);
		}
		newValues = new Object[toKeyType.getKeyCount()];
		System.arraycopy(startValues, 0, newValues, 0, startValues.length);
		this.keyType = toKeyType;
		this.keyVersion = toKeyType.getVersion();
		this.values = newValues;
	}

	/**
	 * Downgrades this object to the specified key type.
	 * 
	 * @param toKeyType
	 *            Key type to downgrade.
	 */
	private void downgrade(KeyType toKeyType)
	{
		if (keyType == null) {
			return;
		}

		// take care of serializedBytes first
		deserialize();

		// convert
		// TODO: need to handle race conditions
		Object newValues[] = new Object[toKeyType.getKeyCount()];
		KeyType fromKeyTypes[] = keyType.getValues();
		for (KeyType fromKeyType : fromKeyTypes) {
			KeyType matchingToKeyType = toKeyType.getKeyType(fromKeyType.getName());
			if (matchingToKeyType != null && matchingToKeyType.getType() == fromKeyType.getType()) {
				newValues[matchingToKeyType.getIndex()] = get(fromKeyType);
			}
		}
		this.keyType = toKeyType;
		this.keyVersion = toKeyType.getVersion();
		this.values = newValues;
	}

	/**
	 * Swaps the contents of the specified JsonLite object. After this method
	 * call, this JsonLite object contains the specified object contents and the
	 * specified object contains this object's contents. This method allows the
	 * JsonLite object to change contents, i.e., the version while preserving
	 * the object reference.
	 * 
	 * @param jsonLite
	 *            The JsonLite object to swap. If it is null then swap is not
	 *            performed.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void swap(JsonLite jsonLite)
	{
		if (jsonLite == null) {
			return;
		}

		KeyType keyType = jsonLite.keyType;
		int keyVersion = jsonLite.keyVersion;
		Object[] values = jsonLite.values;
		Map<String, V> valueMap = jsonLite.valueMap;
		int[] dirtyFlags = jsonLite.dirtyFlags;
		byte flags = jsonLite.internalFlag;
		byte[] serializedBytes = jsonLite.serializedBytes;

		jsonLite.keyType = this.keyType;
		jsonLite.keyVersion = this.keyVersion;
		jsonLite.values = this.values;
		jsonLite.valueMap = this.valueMap;
		jsonLite.dirtyFlags = this.dirtyFlags;
		jsonLite.internalFlag = this.internalFlag;
		jsonLite.serializedBytes = this.serializedBytes;

		this.keyType = keyType;
		this.keyVersion = keyVersion;
		this.values = values;
		this.valueMap = valueMap;
		this.dirtyFlags = dirtyFlags;
		this.internalFlag = flags;
		this.serializedBytes = serializedBytes;
	}

	/**
	 * Writes the value of the specified index to the output stream.
	 * 
	 * @param keyTypes
	 *            The entire key types that represent the JsonLite values.
	 * @param index
	 *            The index of the key to write.
	 * @param output
	 *            The output stream.
	 * @return Returns the read value.
	 * @throws IOException
	 *             Thrown if an IO error encountered.
	 */
	private void writeValue(KeyType[] keyTypes, int index, DataOutput output) throws IOException
	{
		try {
			JsonLiteSerializer.write(keyTypes[index].getType(), values[index], output);
		} catch (Exception ex) {
			throw new InvalidKeyException(
					ex.getMessage() + keyTypes.getClass() + " index=" + keyTypes[index].getName(), ex);
		}
	}

	private void writeValueGemfire(KeyType[] keyTypes, int index, DataOutput output) throws IOException
	{
		try {
			DataSerializerEx.write(keyTypes[index].getType(), values[index], output);
		} catch (Exception ex) {
			throw new InvalidKeyException(
					ex.getMessage() + keyTypes.getClass() + " index=" + keyTypes[index].getName(), ex);
		}
	}

	/**
	 * Returns the key type ID that is universally unique. This call is
	 * equivalent to <code>getKeyType().getId()</code>.
	 */
	@Override
	public Object getId()
	{
		if (keyType == null) {
			return null;
		}
		return keyType.getId();
	}

	/**
	 * Returns the key type version. There are one or more key type versions per
	 * ID. This method call is equivalent to invoking
	 * <code>getKeyType().getVersion()</code>.
	 */
	@Override
	public int getKeyTypeVersion()
	{
		if (keyType == null) {
			return 0;
		}
		return keyType.getVersion();
	}

	/**
	 * Returns the simple (short) class name of the key type. It returns null if
	 * the key type is not defined.
	 */
	@Override
	public String getName()
	{
		if (keyType == null) {
			return null;
		}
		return keyType.getClass().getSimpleName();
	}

	/**
	 * Returns the fully qualified class name of the key type. It returns null
	 * if the key type is not defined.
	 */
	@Override
	public String getKeyTypeName()
	{
		if (keyType == null) {
			return null;
		}
		return keyType.getClass().getName();
	}

	/**
	 * Returns all of the keys.
	 * 
	 * @return Unmodifiable key set.
	 */
	@Override
	public Set<String> keySet()
	{
		Set<String> keys;
		if (keyType == null) {
			if (valueMap == null) {
				keys = Collections.unmodifiableSet(new HashSet<String>(0, 1f));
			} else {
				keys = Collections.unmodifiableSet(valueMap.keySet());
			}
		} else {
			keys = Collections.unmodifiableSet(keyType.getNameSet());
		}
		return keys;
	}

	/**
	 * Returns the entire collection of non-null values.
	 * 
	 * @return Unmodifiable value collection.
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<V> values()
	{
		Collection col;
		if (keyType == null) {
			if (valueMap == null) {
				col = Collections.unmodifiableCollection(new HashSet(0, 1f));
			} else {
				col = Collections.unmodifiableCollection(valueMap.values());
			}
		} else {
			ArrayList list = new ArrayList(values.length + 1);
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null) {
					list.add(values[i]);
				}
			}
			col = Collections.unmodifiableCollection(list);
		}
		return col;
	}

	/**
	 * Returns the (string key, value) paired entry set that contains only
	 * non-null values.
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<Map.Entry<String, V>> entrySet()
	{
		Set<Map.Entry<String, V>> entries;
		if (keyType == null) {
			if (valueMap == null) {
				entries = Collections.unmodifiableSet(new HashSet<Map.Entry<String, V>>(0, 1f));
			} else {
				entries = Collections.unmodifiableSet(valueMap.entrySet());
			}
		} else {
			HashMap<String, V> map = new HashMap(keyType.getKeyCount() + 1, 1f);
			for (KeyType ft : keyType.getValues()) {
				Object value = get(ft);
				if (value != null) {
					map.put(ft.getName(), get(ft));
				}
			}
			entries = Collections.unmodifiableSet(map.entrySet());
		}
		return entries;
	}

	/**
	 * Clears the JsonLite values. All non-null values are set to null and
	 * dirty.
	 */
	@Override
	public void clear()
	{
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null) {
					setDirty(i, dirtyFlags);
				}
			}
			values = new Object[values.length];
		}
		if (valueMap != null) {
			valueMap.clear();
		}
	}

	/**
	 * Returns true if the specified key maps a non-null value. It uses
	 * key.toString() to search the key.
	 * 
	 * @param key
	 *            The key to check.
	 */
	@Override
	public boolean containsKey(Object key)
	{
		if (keyType == null) {
			if (valueMap == null) {
				return false;
			} else {
				return valueMap.containsKey(key);
			}
		}
		return get(key) != null;
	}

	/**
	 * Returns true if the specified value exists in this object. It returns
	 * null if the specified value is null and the JsonLite object contains one
	 * or more null values.
	 * 
	 * @param value
	 *            The value to search.
	 */
	@Override
	public boolean containsValue(Object value)
	{
		if (keyType == null) {
			if (valueMap == null) {
				return false;
			} else {
				return valueMap.containsValue(value);
			}
		}
		for (int i = 0; i < values.length; i++) {
			if (values[i] == value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if there are no values stored in this object. A null value
	 * is considered no value.
	 */
	@Override
	public boolean isEmpty()
	{
		if (keyType == null || values == null) {
			return true;
		}
		if (keyType == null) {
			if (valueMap == null) {
				return true;
			} else {
				return valueMap.isEmpty();
			}
		} else if (values == null) {
			return false;
		} else {
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null) {
					return false;
				}
			}
			return false;
		}
	}

	/**
	 * Puts all entries found in the specified map into this JsonLite object.
	 * The specified map is handled based on its type as follows:
	 * 
	 * <ul>
	 * <li>If the specified map is null then it is ignored.</li>
	 * 
	 * <li>If the specified map is JsonLite and this object has not assigned
	 * KeyType then this method shallow-copies the entire map image into this
	 * object. As a result, JsonLite object effectively becomes a clone of the
	 * specified map with all of the keys marked dirty.</li>
	 * 
	 * <li>If the specified map is JsonLite and this object has the same KeyType
	 * as the map then the above bullet also applies.</li>
	 * 
	 * <li>If the specified map is Map or JsonLite with a KeyType that is
	 * different from this object then this method shallow-copies only the valid
	 * keys and values. All invalid keys and values are ignored. The valid keys
	 * must have the same key names defined in this object's KeyType. Similarly,
	 * the valid values must have the same types defined in this object's
	 * KeyType. All valid keys are marked dirty.</li>
	 * </ul>
	 * <p>
	 * Note that the last bullet transforms any Map objects into JsonLite
	 * objects.
	 * 
	 * @param map
	 *            Mappings to be stored in this JsonLite object. If it is null
	 *            then it is silently ignored.
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void putAll(Map<? extends String, ? extends V> map)
	{
		if (map == null) {
			return;
		}

		// If key type is not defined
		if (keyType == null) {
			if (map instanceof JsonLite) {
				JsonLite jl = (JsonLite) map;
				if (jl.getKeyType() == null) {
					// Both use valueMap.
					if (jl.valueMap == null) {
						valueMap = null;
					} else {
						valueMap = createValueMapIfNotExist();
						valueMap.putAll(jl.valueMap);
					}
				} else {
					init(jl.getKeyType());
					System.arraycopy(jl.values, 0, values, 0, values.length);
					dirtyAllKeys();
				}
			} else {
				// This object uses valueMap and the passed-in map is a Map.
				valueMap = createValueMapIfNotExist();
				valueMap.putAll(map);
			}

		} else {

			// If key type is defined
			if (map instanceof JsonLite) {
				JsonLite jl = (JsonLite) map;
				if (keyType == jl.getKeyType()) {
					System.arraycopy(jl.values, 0, values, 0, values.length);
					dirtyAllKeys();
					return;
				}
			}

			// If Map or JsonLite with a different KeyType - key must be string
			Set<? extends Map.Entry<? extends String, ? extends V>> set = map.entrySet();
			for (Map.Entry<? extends String, ? extends V> entry : set) {
				KeyType keyType = this.keyType.getKeyType(entry.getKey());
				if (keyType == null) {
					continue;
				}
				if (entry.getValue() != null && keyType.getType() != entry.getValue().getClass()) {
					continue;
				}
				put(keyType, entry.getValue());
			}
		}
	}

	/**
	 * Removes the specified key's value. This method removes only the value
	 * that the key maps to. The keys are never removed.
	 * 
	 * @param key
	 *            The key of the value to remove.
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public V remove(Object key)
	{
		if (key == null) {
			return null;
		}

		V oldVal;
		if (keyType == null) {
			if (valueMap == null) {
				oldVal = null;
			} else {
				oldVal = valueMap.remove(key);
			}
			
		} else {
			KeyType keyType = this.keyType.getKeyType(key.toString());
			if (keyType == null) {
				return null;
			}

			oldVal = (V) values[keyType.getIndex()];

			if (oldVal != null) {
				// if (this.keyType.isDeltaEnabled()) {
				setDirty(keyType, dirtyFlags);
				// }
			}
			// TODO: take care of initial values for primitives
			values[keyType.getIndex()] = null;
		}
		return oldVal;
	}

	/**
	 * Returns the count of the non-null values.
	 */
	@Override
	public int size()
	{
		int count;
		if (keyType == null) {
			if (valueMap == null) {
				count = 0;
			} else {
				count = valueMap.size();
			}
		} else {
			count = 0;
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Returns the count of all keys. If KeyType is defined, then this method
	 * call is equivalent to invoking
	 * <code>{@link #getKeyType()}.getKeyCount()</code>.
	 */
	@Override
	public int getKeyCount()
	{
		if (keyType == null) {
			if (valueMap == null) {
				return 0;
			} else {
				return valueMap.size();
			}
		}
		return keyType.getKeyCount();
	}

	/**
	 * Clones this object by shallow-copying values. The returned object is the
	 * exact image of this object including deltas and serialized values.
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object clone()
	{
		JsonLite jl;
		if (keyType == null) {
			jl = new JsonLite(this.valueMap);
		} else {
			jl = new JsonLite(keyType);
			System.arraycopy(values, 0, jl.values, 0, values.length);
			System.arraycopy(dirtyFlags, 0, jl.dirtyFlags, 0, dirtyFlags.length);
			jl.serializedBytes = serializedBytes;
//			if (serializedBytes != null) {
//				System.arraycopy(serializedBytes, 0, jl.serializedBytes, 0, serializedBytes.length);
//			}
		}
		return jl;
	}

	/**
	 * Reads JsonLite contents in the specified input stream.
	 * 
	 * @param input
	 *            The input stream.
	 * @throws IOException
	 *             Thrown if an IO error encountered.
	 * @throws ClassNotFoundException
	 *             Thrown if the input stream contains the wrong class type.
	 *             This should never occur with JsonLite.
	 */
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException
	{
		readData(input);
	}

	@SuppressWarnings("unchecked")
	public void readData(DataInput input) throws IOException, ClassNotFoundException
	{
		internalFlag = JsonLiteSerializer.readByte(input);
		if (isBitDirty(internalFlag, FLAG_KEY_TYPE) == false) {
			valueMap = (HashMap<String, V>) JsonLiteSerializer.readHashMap(input);
		} else {
			long mostSigBits = input.readLong();
			long leastSigBits = input.readLong();
			keyVersion = JsonLiteSerializer.readUnsignedShort(input);
			referenceMap = (HashMap<String, Object>) JsonLiteSerializer.readHashMap(input);
			keyType = KeyTypeManager.getKeyType(mostSigBits, leastSigBits, keyVersion);

			init(keyType);
			values = new Object[keyType.getKeyCount()];
			KeyType[] keyTypeValues = keyType.getValues(keyVersion);
			if (keyType.isPayloadKeepSerialized()) {
				// need not to lock since readData is invoked only once
				serializedBytes = JsonLiteSerializer.readByteArray(input);
				byte[] deserializedBytes = JsonLiteSerializer.readByteArray(input);
				ByteArrayInputStream bais = new ByteArrayInputStream(deserializedBytes);
				DataInputStream dis = new DataInputStream(bais);
				for (int i = 0; i < keyTypeValues.length; i++) {
					if (keyTypeValues[i].isKeyKeepSerialized() == false) {
						// deserialized values
						values[i] = readValue(keyTypeValues, i, dis);
					}
				}
				dis.close();
			} else {
				for (int i = 0; i < keyTypeValues.length; i++) {
					values[i] = readValue(keyTypeValues, i, input);
				}
			}

			// To support auto upgrade a different version, invoke upgrade()
			// from
			// here
			// TODO: Preserve previous versions so that the versions
			// can be properly resolved.
			// upgrade();
		}
	}

	/**
	 * Reads deltas from the specified input stream.
	 * 
	 * @param input
	 *            The input stream.
	 * @throws IOException
	 *             Thrown if an IO error encountered.
	 */
	public void readDelta(DataInput input) throws IOException
	{
		KeyType[] keyTypeValues = keyType.getValues();
		int bitCount = keyTypeValues.length;
		int dirtyFlagCount = dirtyFlags.length;

		int dirtyFlagsToApply[] = new int[dirtyFlagCount];
		for (int i = 0; i < dirtyFlagCount; i++) {
			dirtyFlagsToApply[i] = input.readInt();
		}

		try {
			int count = BIT_MASK_SIZE; // int
			for (int i = 0; i < dirtyFlagsToApply.length; i++) {
				int dirty = dirtyFlagsToApply[i]; // received dirty
				int userDirty = dirtyFlags[i]; // app dirty
				if (i == dirtyFlagsToApply.length - 1) {
					count = bitCount % BIT_MASK_SIZE;
					if (count == 0 && bitCount != 0) {
						count = BIT_MASK_SIZE;
					}
				}

				// Compare both the current bit and the received bit.
				// The app might be modifying the object. If so, keep the
				// user modified data and discard the change received.
				int startIndex = i * BIT_MASK_SIZE;
				for (int j = 0; j < count; j++) {
					if (isBitDirty(dirty, j)) {
						int index = startIndex + j;
						Object value = JsonLiteSerializer.readObject(input);
						// Set the new value only if the app has not set the
						// value
						if (isBitDirty(userDirty, j) == false) {
							values[index] = value;
						}
					}
				}
			}
		} catch (ClassNotFoundException ex) {
			// ignore
		}
	}

	/**
	 * Writes the JsonLite contents to the specified output stream.
	 * 
	 * @param output
	 *            The output stream.
	 * @throws IOException
	 *             Thrown if an IO error encountered.
	 */
	@Override
	public void writeExternal(ObjectOutput output) throws IOException
	{
		writeData(output);
	}

	// public void writeData_org(DataOutput output) throws IOException
	// {
	// if (keyType == null) {
	// internalFlag = (byte) unsetDirty(FLAG_KEY_TYPE, internalFlag);
	// JsonLiteSerializer.writeByte(internalFlag, output);
	// JsonLiteSerializer.writeHashMap((HashMap<String, ?>) valueMap, output);
	// } else {
	// internalFlag = (byte) setDirty(FLAG_KEY_TYPE, internalFlag);
	// JsonLiteSerializer.writeByte(internalFlag, output);
	// output.writeLong(((UUID) keyType.getId()).getMostSignificantBits());
	// output.writeLong(((UUID) keyType.getId()).getLeastSignificantBits());
	// JsonLiteSerializer.writeUnsignedShort(keyType.getVersion(), output);
	// KeyType[] keyTypeValues = keyType.getValues(keyVersion);
	// if (keyType.isPayloadKeepSerialized()) {
	// // assign byteArray to serializedBytes beforehand to
	// // handle race condition
	// byte[] byteArray = serializedBytes;
	// if (byteArray != null) {
	// JsonLiteSerializer.writeByteArray(byteArray, output);
	// // HeapDataOutputStream hdos2 = new HeapDataOutputStream();
	// ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
	// DataOutputStream hdos2 = new DataOutputStream(baos2);
	// for (int i = 0; i < keyTypeValues.length; i++) {
	// if (keyTypeValues[i].isKeyKeepSerialized() == false) {
	// // keep it separate in deserialized array.
	// // this array is always deserialized
	// writeValue(keyTypeValues, i, hdos2);
	// }
	// }
	// hdos2.close();
	// JsonLiteSerializer.writeByteArray(baos2.toByteArray(), output);
	// } else {
	// // HeapDataOutputStream hdos = new HeapDataOutputStream();
	// // HeapDataOutputStream hdos2 = new HeapDataOutputStream();
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
	// DataOutputStream hdos = new DataOutputStream(baos);
	// DataOutputStream hdos2 = new DataOutputStream(baos2);
	//
	// for (int i = 0; i < keyTypeValues.length; i++) {
	// if (keyTypeValues[i].isKeyKeepSerialized()) {
	// // serialize in the normal array
	// // the normal array is deserialized only when the
	// // one of its keys is accessed.
	// writeValue(keyTypeValues, i, hdos);
	// } else {
	// // keep it separate in deserialized array.
	// // this array is always deserialized
	// writeValue(keyTypeValues, i, hdos2);
	// }
	// }
	// hdos.close();
	// hdos2.close();
	// // JsonLiteSerializer.writeByteArray(hdos.toByteArray(),
	// // output);
	// // JsonLiteSerializer.writeByteArray(hdos2.toByteArray(),
	// // output);
	// JsonLiteSerializer.writeByteArray(baos.toByteArray(), output);
	// JsonLiteSerializer.writeByteArray(baos2.toByteArray(), output);
	// }
	// } else {
	// for (int i = 0; i < keyTypeValues.length; i++) {
	// writeValue(keyTypeValues, i, output);
	// }
	// }
	// clearDirty();
	// }
	// }

	public void writeData(DataOutput output) throws IOException
	{
		if (keyType == null) {
			internalFlag = (byte) unsetDirty(FLAG_KEY_TYPE, internalFlag);
			JsonLiteSerializer.writeByte(internalFlag, output);
			JsonLiteSerializer.writeHashMap((HashMap<String, ?>) valueMap, output);
		} else {
			internalFlag = (byte) setDirty(FLAG_KEY_TYPE, internalFlag);
			JsonLiteSerializer.writeByte(internalFlag, output);
			output.writeLong(((UUID) keyType.getId()).getMostSignificantBits());
			output.writeLong(((UUID) keyType.getId()).getLeastSignificantBits());
			JsonLiteSerializer.writeUnsignedShort(keyType.getVersion(), output);
			// Remove the reference map when serializing. We don't need the
			// reference map once serialized and delivered to the client. This
			// is for server-side operation.
			Map<String, Object> refMap = null;
			if (threadReferenceMap != null) {
				refMap = threadReferenceMap.remove(Thread.currentThread().getId());
			}
			JsonLiteSerializer.writeHashMap((HashMap<String, Object>) refMap, output);
			KeyType[] keyTypeValues = keyType.getValues(keyVersion);
			if (keyType.isPayloadKeepSerialized()) {
				// assign byteArray to serializedBytes beforehand to
				// handle race condition
				byte[] byteArray = serializedBytes;
				if (byteArray != null) {
					JsonLiteSerializer.writeByteArray(byteArray, output);
					// HeapDataOutputStream hdos2 = new HeapDataOutputStream();
					ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
					DataOutputStream hdos2 = new DataOutputStream(baos2);
					for (int i = 0; i < keyTypeValues.length; i++) {
						if (keyTypeValues[i].isKeyKeepSerialized() == false) {
							// keep it separate in deserialized array.
							// this array is always deserialized
							writeValue(keyTypeValues, i, hdos2);
						}
					}
					hdos2.close();
					JsonLiteSerializer.writeByteArray(baos2.toByteArray(), output);
				} else {
					// HeapDataOutputStream hdos = new HeapDataOutputStream();
					// HeapDataOutputStream hdos2 = new HeapDataOutputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
					DataOutputStream hdos = new DataOutputStream(baos);
					DataOutputStream hdos2 = new DataOutputStream(baos2);

					for (int i = 0; i < keyTypeValues.length; i++) {
						if (keyTypeValues[i].isKeyKeepSerialized()) {
							// serialize in the normal array
							// the normal array is deserialized only when
							// one of its keys is accessed.
							writeValue(keyTypeValues, i, hdos);
						} else {
							// keep it separate in deserialized array.
							// this array is always deserialized
							writeValue(keyTypeValues, i, hdos2);
						}
					}
					hdos.close();
					hdos2.close();
					// JsonLiteSerializer.writeByteArray(hdos.toByteArray(),
					// output);
					// JsonLiteSerializer.writeByteArray(hdos2.toByteArray(),
					// output);
					JsonLiteSerializer.writeByteArray(baos.toByteArray(), output);
					JsonLiteSerializer.writeByteArray(baos2.toByteArray(), output);
				}
			} else {
				for (int i = 0; i < keyTypeValues.length; i++) {
					writeValue(keyTypeValues, i, output);
				}
			}
			clearDirty();
		}
	}

	/**
	 * Writes deltas to the specified output stream.
	 * 
	 * @param output
	 *            The output stream.
	 * @throws IOException
	 *             Thrown if an IO error encountered.
	 */
	public void writeDelta(DataOutput output) throws IOException
	{
		KeyType[] keyTypeValues = keyType.getValues();
		int bitCount = keyTypeValues.length;

		for (int i = 0; i < dirtyFlags.length; i++) {
			output.writeInt(dirtyFlags[i]);
		}

		int count = BIT_MASK_SIZE;

		for (int i = 0; i < dirtyFlags.length; i++) {
			int dirty = dirtyFlags[i];
			if (isDirty(dirty) == false) {
				continue;
			}
			if (i == dirtyFlags.length - 1) {
				count = bitCount % BIT_MASK_SIZE;
				if (count == 0 && bitCount != 0) {
					count = BIT_MASK_SIZE;
				}
			}
			int startIndex = i * BIT_MASK_SIZE;
			for (int j = 0; j < count; j++) {
				if (isBitDirty(dirty, j)) {
					int index = startIndex + j;
					// JsonLiteSerializer.writeObject(values[index], output);
					writeValue(keyTypeValues, index, output);
				}
			}
		}
		clearDirty();
	}

	/**
	 * Returns raw (unindented) JSON string representation including the header
	 * information. This method call is equivalent to
	 * <code>toJsonString(true);</code>
	 */
	public String toJsonString()
	{
		return toString(0, false, false);
	}

	/**
	 * Returns raw (unindented) JSON string representation.
	 * 
	 * @param isReference
	 *            true to include reference objects.
	 * @param isHeader
	 *            true to include header, false to exclude header.
	 */
	public String toJsonString(boolean isReference, boolean isHeader)
	{
		return toString(0, isReference, isHeader);
	}

	/**
	 * Returns raw (unindented) JSON string representation. This call is
	 * equivalent to <code>toString(0, true);</code>
	 */
	@Override
	public String toString()
	{
		return toString(0, false, false);
	}

	/**
	 * Returns indented string representation of JsonLite for display purposes.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @return a printable, displayable, portable, transmittable representation
	 *         of the object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JsonLiteException
	 *             If the object contains an invalid number.
	 */
	public String toString(int indentFactor, boolean isReference, boolean isHeader) throws JsonLiteException
	{
		deserialize();
		JsonLiteStringWriter<V> jlsw = new JsonLiteStringWriter<V>();
		StringWriter w = new StringWriter();
		return jlsw.write(w, this, indentFactor, 0, isReference, isHeader).toString();
	}

	@Override
	public void toDelta(DataOutput output) throws IOException
	{
		KeyType[] keyTypeValues = keyType.getValues();
		int bitCount = keyTypeValues.length;

		for (int i = 0; i < dirtyFlags.length; i++) {
			output.writeInt(dirtyFlags[i]);
		}

		int count = BIT_MASK_SIZE;

		for (int i = 0; i < dirtyFlags.length; i++) {
			int dirty = dirtyFlags[i];
			if (isDirty(dirty) == false) {
				continue;
			}
			if (i == dirtyFlags.length - 1) {
				count = bitCount % BIT_MASK_SIZE;
				if (count == 0 && bitCount != 0) {
					count = BIT_MASK_SIZE;
				}
			}
			int startIndex = i * BIT_MASK_SIZE;
			for (int j = 0; j < count; j++) {
				if (isBitDirty(dirty, j)) {
					int index = startIndex + j;
					DataSerializer.writeObject(values[index], output);
				}
			}
		}
		clearDirty();
	}

	@Override
	public void fromDelta(DataInput input) throws IOException, InvalidDeltaException
	{
		KeyType[] keyTypeValues = keyType.getValues();
		int bitCount = keyTypeValues.length;
		int dirtyFlagCount = dirtyFlags.length;

		int dirtyFlagsToApply[] = new int[dirtyFlagCount];
		for (int i = 0; i < dirtyFlagCount; i++) {
			dirtyFlagsToApply[i] = input.readInt();
		}

		try {
			int count = BIT_MASK_SIZE; // int
			for (int i = 0; i < dirtyFlagsToApply.length; i++) {
				int dirty = dirtyFlagsToApply[i]; // received dirty
				int userDirty = dirtyFlags[i]; // app dirty
				if (i == dirtyFlagsToApply.length - 1) {
					count = bitCount % BIT_MASK_SIZE;
					if (count == 0 && bitCount != 0) {
						count = BIT_MASK_SIZE;
					}
				}

				// Compare both the current bit and the received bit.
				// The app might be modifying the object. If so, keep the
				// user modified data and discard the change received.
				int startIndex = i * BIT_MASK_SIZE;
				for (int j = 0; j < count; j++) {
					if (isBitDirty(dirty, j)) {
						int index = startIndex + j;
						Object value = DataSerializer.readObject(input);
						// Set the new value only if the app has not set the
						// value
						if (isBitDirty(userDirty, j) == false) {
							values[index] = value;
						}
					}
				}
			}
		} catch (ClassNotFoundException ex) {
			// ignore
		}
	}

	@Override
	public void toData(DataOutput output) throws IOException
	{
		if (keyType == null) {
			internalFlag = (byte) unsetDirty(FLAG_KEY_TYPE, internalFlag);
			DataSerializerEx.writeByte(internalFlag, output);
			DataSerializer.writeHashMap((HashMap<String, V>) valueMap, output);
		} else {
			internalFlag = (byte) setDirty(FLAG_KEY_TYPE, internalFlag);
			DataSerializerEx.writeByte(internalFlag, output);
			output.writeLong(((UUID) keyType.getId()).getMostSignificantBits());
			output.writeLong(((UUID) keyType.getId()).getLeastSignificantBits());
			DataSerializer.writeUnsignedShort(keyType.getVersion(), output);
			// Remove the reference map when serializing. We don't need the
			// reference map once serialized and delivered to the client. This
			// is for server-side operation.
			Map<String, Object> refMap = null;
			if (threadReferenceMap != null) {
				refMap = threadReferenceMap.remove(Thread.currentThread().getId());
			}
			DataSerializer.writeHashMap((HashMap<String, Object>) refMap, output);
			KeyType[] keyTypeValues = keyType.getValues(keyVersion);
			if (keyType.isPayloadKeepSerialized()) {
				// assign byteArray to serializedBytes beforehand to
				// handle race conditions
				byte[] byteArray = serializedBytes;
				if (byteArray != null) {
					DataSerializer.writeByteArray(byteArray, output);
					HeapDataOutputStream hdos2 = GemfireVersionSpecifics.getGemfireVersionSpecifics().createHeapDataOutpuStream();
					for (int i = 0; i < keyTypeValues.length; i++) {
						if (keyTypeValues[i].isKeyKeepSerialized() == false) {
							// keep it separate in deserialized array.
							// this array is always deserialized
							writeValueGemfire(keyTypeValues, i, hdos2);
						}
					}
					DataSerializer.writeByteArray(hdos2.toByteArray(), output);
					hdos2.close();
				} else {
					HeapDataOutputStream hdos = GemfireVersionSpecifics.getGemfireVersionSpecifics().createHeapDataOutpuStream();
					HeapDataOutputStream hdos2 = GemfireVersionSpecifics.getGemfireVersionSpecifics().createHeapDataOutpuStream();
					for (int i = 0; i < keyTypeValues.length; i++) {
						if (keyTypeValues[i].isKeyKeepSerialized()) {
							// serialize in the normal array
							// the normal array is deserialized only when the
							// one of its keys is accessed.
							writeValueGemfire(keyTypeValues, i, hdos);
						} else {
							// keep it separate in deserialized array.
							// this array is always deserialized
							writeValueGemfire(keyTypeValues, i, hdos2);
						}
					}
					DataSerializer.writeByteArray(hdos.toByteArray(), output);
					DataSerializer.writeByteArray(hdos2.toByteArray(), output);
					hdos.close();
					hdos2.close();
				}
			} else {
				for (int i = 0; i < keyTypeValues.length; i++) {
					writeValueGemfire(keyTypeValues, i, output);
				}
			}
			clearDirty();
		}
	}

	@Override
	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		internalFlag = DataSerializer.readByte(input);
		if (isBitDirty(internalFlag, FLAG_KEY_TYPE) == false) {
			valueMap = DataSerializer.readHashMap(input);
		} else {
			long mostSigBits = input.readLong();
			long leastSigBits = input.readLong();
			keyVersion = DataSerializer.readUnsignedShort(input);
			keyType = KeyTypeManager.getKeyType(mostSigBits, leastSigBits, keyVersion);
			if (keyType == null) {
				throw new JsonLiteException("KeyType undefined. Make sure the KeyType class is versioned and included in the jar file.");
			}
			init(keyType);
			values = new Object[keyType.getKeyCount()];
			// read references after init()
			referenceMap = DataSerializer.readHashMap(input);
			KeyType[] keyTypeValues = keyType.getValues(keyVersion);
			if (keyType.isPayloadKeepSerialized()) {
				// need not to lock since readData is invoked only once
				serializedBytes = DataSerializer.readByteArray(input);
				byte[] deserializedBytes = DataSerializer.readByteArray(input);
				ByteArrayInputStream bais = new ByteArrayInputStream(deserializedBytes);
				DataInputStream dis = new DataInputStream(bais);
				for (int i = 0; i < keyTypeValues.length; i++) {
					if (keyTypeValues[i].isKeyKeepSerialized() == false) {
						// deserialized values
						values[i] = readValueGemfire(keyTypeValues, i, dis);
					}
				}
				dis.close();
			} else {
				for (int i = 0; i < keyTypeValues.length; i++) {
					values[i] = readValueGemfire(keyTypeValues, i, input);
				}
			}

			// To support auto upgrade a different version, invoke upgrade()
			// from
			// here
			// TODO: Preserve previous versions so that the versions
			// can be properly resolved.
			// upgrade();
		}
	}

	@Override
	public int hashCode()
	{
		int result = 1;
		if (values != null) {
			final int prime = 31;
			for (Object value : values) {
				result = prime * result + ((value == null) ? 0 : value.hashCode());
			}
		} else {
			if (valueMap != null) {
				result = valueMap.hashCode();
			}
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JsonLite other = (JsonLite) obj;
		if (keyType == null && other.keyType == null) {
			if (valueMap == null && other.valueMap == null) {
				return true;
			}
			if (valueMap != null) {
				return valueMap.equals(other.valueMap);
			} else {
				return false;
			}
		}
		if (keyType.getVersion() != other.getKeyType().getVersion()) {
			return false;
		}
		if (values != null && other.values != null) {
			deserialize();
			other.deserialize();
			for (int i = 0; i < values.length; i++) {
				if (values[i] == null && other.values[i] == null) {
					continue;
				}
				if (values[i] != null && values[i].equals(other.values[i]) == false) {
					return false;
				}
			}
		} else {
			if (valueMap != null && other.valueMap != null) {
				return valueMap.equals(other.valueMap);
			}
		}
		return true;
	}

	@Override
	public boolean hasReferences()
	{
		return referenceMap != null && referenceMap.size() > 0;
	}
}
