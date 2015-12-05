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
package com.netcrest.pado.temporal.gemfire.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.NumberDateFormat;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.store.MMapDirectory;

import com.gemstone.gemfire.internal.util.BlobHelper;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.index.provider.lucene.LuceneField;
import com.netcrest.pado.index.provider.lucene.LuceneSearch;
import com.netcrest.pado.index.provider.lucene.ReflectionHelper;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.temporal.TemporalType;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class LuceneTemporalEntryBuilder
{
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	private final static NumberDateFormat DATE_FORMAT = new NumberDateFormat(dateFormat);
	private final static int PRECISION_STEP = 8;

	private IndexWriter writer = null;
	private MMapDirectory directory = null;
	private StandardQueryParser parser;
	private TemporalManager tm;
	
	Field tkFields[] = null;
	Method tkGetters[] = null;
	Class tkKeyType = null;
	boolean isIdentityKeyPrimitive;
	boolean isInitialized = false;

	public LuceneTemporalEntryBuilder(String fullPath)
	{
		try {
			File file = new File("lucene" + fullPath);
			if (file.exists() == false) {
				file.mkdirs();
			}
			Analyzer analyzer = new StandardAnalyzer(LuceneSearch.LUCENE_VERSION);
			IndexWriterConfig iwc = new IndexWriterConfig(LuceneSearch.LUCENE_VERSION, analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			directory = new MMapDirectory(file);
			writer = new IndexWriter(directory, iwc);
			tm = TemporalManager.getTemporalManager(fullPath);
			writer.commit();
			parser = LuceneSearch.getLuceneSearch(fullPath).createParser();
			configNumericType("StartValidTime", Date.class);
			configNumericType("EndValidTime", Date.class);
			configNumericType("StartWrittenTime", Date.class);
			configNumericType("EndWrittenTime", Date.class);
		} catch (IOException ex) {
			Logger.error(ex);
		}
	}

	public void close()
	{
		if (writer != null) {
			try {
				writer.close();
			} catch (CorruptIndexException e) {
				Logger.error(e);
			} catch (IOException e) {
				Logger.error(e);
			}
		}
		if (directory != null) {
			directory.close();
		}
	}
	
	private void initOnce(ITemporalKey tk)
	{
		if (isInitialized == false) {
			Object identityKey = tk.getIdentityKey();
			if (ReflectionHelper.isPrimitiveWrapper(identityKey.getClass())) {
				tkFields = null;
				tkGetters = null;
				tkKeyType = identityKey.getClass();
			} else {
				tkFields = ReflectionHelper.getPublicFields(identityKey.getClass());
				tkGetters = ReflectionHelper.getPublicGetters(identityKey.getClass());
			}
			isIdentityKeyPrimitive = tkKeyType != null;
			configNumericType("IdentityKey", tkKeyType);
			isInitialized = true;
		}
	}

	public void processTemporalEntry(ITemporalKey tk, ITemporalData data)
	{
		initOnce(tk);
		
		boolean isIdentityKeyPrimitive = buildTemporalKeys(tk);
		buildTemporalData(tk, data, isIdentityKeyPrimitive);
		try {
			writer.commit();
		} catch (CorruptIndexException e) {
			Logger.error(e);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void configNumericType(String fieldName, Class<?> fieldType)
	{
		// build the numeric config
		Map<String, NumericConfig> map = parser.getNumericConfigMap();
		if (map == null) {
			map = new HashMap<String, NumericConfig>();
			parser.setNumericConfigMap(map);
		}

		if (fieldType == Integer.class || fieldType.toString().equals("int")) {
			NumericConfig config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(), NumericType.INT);
			map.put(fieldName, config);
		} else if (fieldType == Long.class || fieldType.toString().equals("long")) {
			NumericConfig config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(), NumericType.LONG);
			map.put(fieldName, config);
		} else if (fieldType == Float.class || fieldType.toString().equals("float")) {
			NumericConfig config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(), NumericType.FLOAT);
			map.put(fieldName, config);
		} else if (fieldType == Double.class || fieldType.toString().equals("double")) {
			NumericConfig config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(), NumericType.DOUBLE);
			map.put(fieldName, config);
		} else if (fieldType == Date.class) {
			NumericConfig config = new NumericConfig(PRECISION_STEP, DATE_FORMAT, NumericType.LONG);
			map.put(fieldName, config);
		}
	}

	private String getPropertyName(Method getter)
	{
		String name = getter.getName();
		if (name.startsWith("is")) {
			return name.substring(2);
		} else {
			return name.substring(3);
		}
	}
	
	private boolean buildTemporalKeys(ITemporalKey tk)
	{
		// build the numeric config
		Map<String, NumericConfig> numericConfigMap = parser.getNumericConfigMap();
		if (numericConfigMap == null) {
			numericConfigMap = new HashMap<String, NumericConfig>();
			parser.setNumericConfigMap(numericConfigMap);
		}
		
		Object key = tk.getIdentityKey();
		LuceneField luceneField = new LuceneField();
		List<Document> docList = new ArrayList();
		Document doc = luceneField.createDocument();
		if (tkKeyType != null) {
			// primitive
			if (tkKeyType == Integer.class) {
				doc.add(luceneField.createField("IdentityKey", (Integer) key));
			} else if (tkKeyType == Long.class) {
				doc.add(luceneField.createField("IdentityKey", (Long) key));
			} else if (tkKeyType == Float.class) {
				doc.add(luceneField.createField("IdentityKey", (Float) key));
			} else if (tkKeyType == Double.class) {
				doc.add(luceneField.createField("IdentityKey", (Double) key));
			} else {
				// string for all others
				doc.add(luceneField.createField("IdentityKey", key.toString()));
			}
		} else {

			try {
				// fields
				if (tkFields != null && tkFields.length > 0) {

					// configure numeric types
					for (Field field : tkFields) {
						configNumericType(field.getName(), field.getType());
					}

					for (Field field : tkFields) {
						Object obj = field.get(key);
						Class fieldType = field.getType();
						if (fieldType == Integer.class || fieldType == int.class) {
							doc.add(luceneField.createField(field.getName(), (Integer) obj));
						} else if (fieldType == Long.class || fieldType == long.class) {
							doc.add(luceneField.createField(field.getName(), (Long) obj));
						} else if (fieldType == Float.class || fieldType == float.class) {
							doc.add(luceneField.createField(field.getName(), (Float) obj));
						} else if (fieldType == Double.class || fieldType == double.class) {
							doc.add(luceneField.createField(field.getName(), (Double) obj));
						} else if (fieldType == Date.class) {
							doc.add(luceneField.createField(field.getName(), ((Date) obj).getTime()));
						} else {
							// string for all others
							doc.add(luceneField.createField(field.getName(), obj.toString()));
						}
					}
				}

				// getters - methods
				if (tkGetters != null && tkGetters.length > 0) {
					for (Method method : tkGetters) {
						Object obj = method.invoke(key);
						Class<?> fieldType = method.getReturnType();
						if (fieldType == Integer.class || fieldType == int.class) {
							doc.add(luceneField.createField(getPropertyName(method), (Integer) obj));
						} else if (fieldType == Long.class || fieldType == long.class) {
							doc.add(luceneField.createField(getPropertyName(method), (Long) obj));
						} else if (fieldType == Float.class || fieldType == float.class) {
							doc.add(luceneField.createField(getPropertyName(method), (Float) obj));
						} else if (fieldType == Double.class || fieldType == double.class) {
							doc.add(luceneField.createField(getPropertyName(method), (Double) obj));
						} else if (fieldType == Date.class) {
							doc.add(luceneField.createField(getPropertyName(method), ((Date) obj).getTime()));
						} else {
							// string for all others
							doc.add(luceneField.createField(getPropertyName(method), obj.toString()));
						}
					}
				}

			} catch (Exception ex) {
				Logger.warning(ex);
			}
		}
		doc.add(luceneField.createField("StartValidTime", tk.getStartValidTime()));
		doc.add(luceneField.createField("EndValidTime", tk.getEndValidTime()));
		doc.add(luceneField.createField("Username", tk.getUsername()));
		try {
			byte[] blob = BlobHelper.serializeToBlob(tk);
			doc.add(luceneField.createField("TemporalKey", blob));
		} catch (IOException e) {
			Logger.warning(e);
		}
		docList.add(doc);

		try {
			writer.addDocuments(docList);
		} catch (Exception ex) {
			Logger.warning(ex);
		}

		return isIdentityKeyPrimitive;
	}

	@SuppressWarnings({"unused" })
	private void buildTemporalData(ITemporalKey tk, ITemporalData data, boolean isIdentityKeyPrimitive)
	{
		try {
			TemporalType[] temporalTypes = GemfireTemporalManager.getAllTemporalTypes();

			LuceneField luceneBuilder = new LuceneField();

			Method[] attributeGetters = null;
			try {
				// first, find the attribute getter methods
				boolean isKeyMap = false;
				KeyType keyType = null;

				Object value;
				if (data instanceof GemfireTemporalData) {
					value = ((GemfireTemporalData) data).getValue();
				} else {
					value = data;
				}
				isKeyMap = value instanceof KeyMap;
				if (isKeyMap == false) {
					attributeGetters = ReflectionHelper.getAttributeGetters(data.getClass());
				} else {
					keyType = ((KeyMap) value).getKeyType();
				}

				// build the numeric config
				Map<String, NumericConfig> numericConfigMap = parser.getNumericConfigMap();
				if (numericConfigMap == null) {
					numericConfigMap = new HashMap<String, NumericConfig>();
					parser.setNumericConfigMap(numericConfigMap);
				}
				List<Document> docList = new ArrayList<Document>();

				if (isKeyMap) {

					KeyType[] keyTypes = KeyTypeManager.getAllRegisteredVersions(keyType.getClass());
					for (KeyType kt : keyTypes) {
						Set<String> nameSet = kt.getNameSet();
						for (String name : nameSet) {
							if (numericConfigMap.containsKey(name)) {
								continue;
							}
							KeyType kt2 = kt.getKeyType(name);
							String fieldName = kt2.getName();
							Class<?> fieldType = kt2.getType();

							if (fieldType == Integer.class || fieldType == int.class) {
								NumericConfig config = new NumericConfig(PRECISION_STEP,
										NumberFormat.getNumberInstance(), NumericType.INT);
								numericConfigMap.put(fieldName, config);
							} else if (fieldType == Long.class || fieldType == long.class) {
								NumericConfig config = new NumericConfig(PRECISION_STEP,
										NumberFormat.getNumberInstance(), NumericType.LONG);
								numericConfigMap.put(fieldName, config);
							} else if (fieldType == Float.class || fieldType == float.class) {
								NumericConfig config = new NumericConfig(PRECISION_STEP,
										NumberFormat.getNumberInstance(), NumericType.FLOAT);
								numericConfigMap.put(fieldName, config);
							} else if (fieldType == Double.class || fieldType == double.class) {
								NumericConfig config = new NumericConfig(PRECISION_STEP,
										NumberFormat.getNumberInstance(), NumericType.DOUBLE);
								numericConfigMap.put(fieldName, config);
							} else if (fieldType == Date.class) {
								NumericConfig config = new NumericConfig(PRECISION_STEP, DATE_FORMAT, NumericType.LONG);
								numericConfigMap.put(fieldName, config);
							}
						}
					}

					Object identityKey = tk.getIdentityKey();
					KeyMap keyMap;
					if (data instanceof GemfireTemporalData) {
						keyMap = (KeyMap) ((GemfireTemporalData) data).getValue();
					} else {
						keyMap = (KeyMap) data;
					}
					keyType = keyMap.getKeyType();
					Set<String> nameSet = keyType.getNameSet();

					Document doc = luceneBuilder.createDocument();
					if (isIdentityKeyPrimitive) {
						doc.add(luceneBuilder.createField("IdentityKey", identityKey.toString()));
					} else {
						doc.add(luceneBuilder.createIdentityKeyField(identityKey));
					}
					for (String name : nameSet) {
						Object obj = keyMap.get(name);
						// obj can be null (e.g., version difference or
						// app defined)
						if (obj == null) {
							continue;
						}
						KeyType kt = keyType.getKeyType(name);
						Class fieldType = kt.getType();
						if (fieldType == String.class) {
							doc.add(luceneBuilder.createField(name, obj.toString()));
						} else if (fieldType == Integer.class || fieldType == int.class) {
							doc.add(luceneBuilder.createField(name, (Integer) obj));
						} else if (fieldType == Long.class || fieldType == long.class) {
							doc.add(luceneBuilder.createField(name, (Long) obj));
						} else if (fieldType == Float.class || fieldType == float.class) {
							doc.add(luceneBuilder.createField(name, (Float) obj));
						} else if (fieldType == Double.class || fieldType == double.class) {
							doc.add(luceneBuilder.createField(name, (Double) obj));
						} else if (fieldType == Date.class) {
							doc.add(luceneBuilder.createField(name, ((Date) obj).getTime()));
						} else {
							doc.add(luceneBuilder.createField(name, obj.toString()));
						}
					}
					docList.add(doc);

					try {
						writer.addDocuments(docList);
					} catch (Exception ex) {
						Logger.warning("KeyMap error", ex);
					}

				} else {
					for (Method method : attributeGetters) {
						Class fieldType = method.getReturnType();
						String fieldName = method.getName().substring(3);
						if (fieldType == Integer.class || fieldType == int.class) {
							NumericConfig config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(),
									NumericType.INT);
							numericConfigMap.put(fieldName, config);
						} else if (fieldType == Long.class || fieldType == long.class) {
							NumericConfig config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(),
									NumericType.LONG);
							numericConfigMap.put(fieldName, config);
						} else if (fieldType == Float.class || fieldType == float.class) {
							NumericConfig config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(),
									NumericType.FLOAT);
							numericConfigMap.put(fieldName, config);
						} else if (fieldType == Double.class || fieldType == double.class) {
							NumericConfig config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(),
									NumericType.DOUBLE);
							numericConfigMap.put(fieldName, config);
						} else if (fieldType == Date.class) {
							NumericConfig config = new NumericConfig(PRECISION_STEP, DATE_FORMAT, NumericType.LONG);
							numericConfigMap.put(fieldName, config);
						}
					}

					// build lucene for each attribute in the current
					// (latest) data
					if (attributeGetters != null && attributeGetters.length > 0) {
						Object identityKey = tk.getIdentityKey();
						Document doc = luceneBuilder.createDocument();
						if (isIdentityKeyPrimitive) {
							doc.add(luceneBuilder.createField("IdentityKey", identityKey.toString()));
						} else {
							doc.add(luceneBuilder.createIdentityKeyField(identityKey));
						}
						for (Method method : attributeGetters) {
							Object obj = method.invoke(data);
							Class fieldType = method.getReturnType();
							if (fieldType == String.class) {
								doc.add(luceneBuilder.createField(getPropertyName(method), obj.toString()));
							} else if (fieldType == Integer.class || fieldType == int.class) {
								doc.add(luceneBuilder.createField(getPropertyName(method), (Integer) obj));
							} else if (fieldType == Long.class || fieldType == long.class) {
								doc.add(luceneBuilder.createField(getPropertyName(method), (Long) obj));
							} else if (fieldType == Float.class || fieldType == float.class) {
								doc.add(luceneBuilder.createField(getPropertyName(method), (Float) obj));
							} else if (fieldType == Double.class || fieldType == double.class) {
								doc.add(luceneBuilder.createField(getPropertyName(method), (Double) obj));
							} else if (fieldType == Date.class) {
								doc.add(luceneBuilder.createField(getPropertyName(method), ((Date) obj).getTime()));
							} else {
								doc.add(luceneBuilder.createField(getPropertyName(method), obj.toString()));
							}
						}
						docList.add(doc);

						try {
							writer.addDocuments(docList);
						} catch (Exception ex) {
							Logger.warning("Non-KeyMap error", ex);
						}
					}
				}

				// TODO: support file system
				// place the RamDirectory in the lucene
				// region.put(type.getFullPath(), directory);

			} catch (Exception ex) {
				Logger.warning(ex);
			}

		} catch (Exception ex) {
			Logger.warning("Index builder aborted.", ex);
		}
	}
}
