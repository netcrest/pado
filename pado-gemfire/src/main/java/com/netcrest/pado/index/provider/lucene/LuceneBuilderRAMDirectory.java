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
package com.netcrest.pado.index.provider.lucene;

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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.NumberDateFormat;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.temporal.TemporalType;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;

public class LuceneBuilderRAMDirectory
{
	private static LuceneBuilderRAMDirectory manager = new LuceneBuilderRAMDirectory();

	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	private final static NumberDateFormat DATE_FORMAT = new NumberDateFormat(dateFormat);
	private final static int PRECISION_STEP = 8;

	public static LuceneBuilderRAMDirectory getLuceneBuilder()
	{
		return manager;
	}

	private LuceneBuilderRAMDirectory()
	{
	}

	private void configNumericType(StandardQueryParser parser, String fieldName, Class<?> fieldType)
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

	@SuppressWarnings({ "unchecked", "rawtypes", "resource" })
	public void buildTemporalKeys(boolean createNewDirectory)
	{
		Cache cache = CacheFactory.getAnyInstance();
		Region<String, RAMDirectory> region = cache
				.getRegion(IndexMatrixUtil.getProperty(Constants.PROP_REGION_LUCENE));
		TemporalType[] temporalTypes = GemfireTemporalManager.getAllTemporalTypes();
		
		for (TemporalType type : temporalTypes) {
			IndexWriter writer = null;
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			LuceneField luceneBuilder = new LuceneField();

			LuceneSearch ls = LuceneSearch.getLuceneSearch(type.getFullPath());
			StandardQueryParser parser = ls.createParser();
			TemporalManager tm = TemporalManager.getTemporalManager(type.getFullPath());
			try {
				List<?> identityKeyList = tm.getIdentityKeyList();
				if (identityKeyList.size() == 0) {
					continue;
				}
			
				RAMDirectory directory;
				if (createNewDirectory) {
					directory = new RAMDirectory();
				} else {
					directory = region.get(type.getFullPath());
					if (directory == null) {
						directory = new RAMDirectory();
					}
				}
				writer = new IndexWriter(directory, iwc);

				// determine the identity key type, public fields and getters
				Field fields[] = null;
				Method getters[] = null;
				Class keyType = null;
				for (Object key : identityKeyList) {
					if (ReflectionHelper.isPrimitiveWrapper(key.getClass())) {
						fields = null;
						getters = null;
						keyType = key.getClass();
					} else {
						fields = ReflectionHelper.getPublicFields(key.getClass());
						getters = ReflectionHelper.getPublicGetters(key.getClass());
					}
					break;
				}

				if (keyType != null) {

					configNumericType(parser, "IdentityKey", keyType);

					// primitive
					List<Document> docList = new ArrayList();
					if (keyType == String.class) {
						for (Object key : identityKeyList) {
							// TODO: do lucene here
							Document doc = luceneBuilder.createDocument();
							doc.add(luceneBuilder.createField("IdentityKey", key.toString()));
							docList.add(doc);
						}
					} else if (keyType == Integer.class) {
						for (Object key : identityKeyList) {
							// TODO: do lucene here
							Document doc = luceneBuilder.createDocument();
							doc.add(luceneBuilder.createField("IdentityKey", (Integer) key));
							docList.add(doc);
						}
					} else if (keyType == Long.class) {
						for (Object key : identityKeyList) {
							// TODO: do lucene here
							Document doc = luceneBuilder.createDocument();
							doc.add(luceneBuilder.createField("IdentityKey", (Long) key));
							docList.add(doc);
						}
					} else if (keyType == Float.class) {
						for (Object key : identityKeyList) {
							// TODO: do lucene here
							Document doc = luceneBuilder.createDocument();
							doc.add(luceneBuilder.createField("IdentityKey", (Float) key));
							docList.add(doc);
						}
					} else if (keyType == Double.class) {
						for (Object key : identityKeyList) {
							// TODO: do lucene here
							Document doc = luceneBuilder.createDocument();
							doc.add(luceneBuilder.createField("IdentityKey", (Double) key));
							docList.add(doc);
						}
					}
					try {
						writer.addDocuments(docList);
					} catch (Exception ex) {
						Logger.warning(ex);
					}
				} else {
					try {
						// fields
						if (fields != null && fields.length > 0) {

							// configure numeric types
							for (Field field : fields) {
								configNumericType(parser, field.getName(), field.getType());
							}

							List<Document> docList = new ArrayList();
							for (Object key : identityKeyList) {
								Document doc = luceneBuilder.createDocument();
								for (Field field : fields) {
									Object obj = field.get(key);
									Class fieldType = field.getType();
									if (fieldType == String.class) {
										doc.add(luceneBuilder.createField(field.getName(), obj.toString()));
									} else if (fieldType == Integer.class || fieldType == int.class) {
										doc.add(luceneBuilder.createField(field.getName(), (Integer) obj));
									} else if (fieldType == Long.class || fieldType == long.class) {
										doc.add(luceneBuilder.createField(field.getName(), (Long) obj));
									} else if (fieldType == Float.class || fieldType == float.class) {
										doc.add(luceneBuilder.createField(field.getName(), (Float) obj));
									} else if (fieldType == Double.class || fieldType == double.class) {
										doc.add(luceneBuilder.createField(field.getName(), (Double) obj));
									} else if (fieldType == Date.class) {
										doc.add(luceneBuilder.createField(field.getName(), ((Date) obj).getTime()));
									}
								}
								docList.add(doc);
							}
							try {
								writer.addDocuments(docList);
							} catch (Exception ex) {
								Logger.warning(ex);
							}
						}

						// getters - methods
						if (getters != null && getters.length > 0) {
							List<Document> docList = new ArrayList();
							for (Object key : identityKeyList) {
								Document doc = luceneBuilder.createDocument();
								for (Method method : getters) {
									// TODO: build lucene here
									Object obj = method.invoke(key);
									Class<?> fieldType = method.getReturnType();
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
										doc.add(luceneBuilder.createField(getPropertyName(method),
												((Date) obj).getTime()));
									}
								}
								docList.add(doc);
							}
							try {
								writer.addDocuments(docList);
							} catch (Exception ex) {
								Logger.warning(ex);
							}
						}

					} catch (Exception ex) {
						Logger.warning(ex);
					}
				}

				writer.commit();
				writer.close();

				// TODO: support file system
				// place the RamDirectory in lucene
				region.put(type.getFullPath(), directory);

			} catch (Exception ex) {
				Logger.error("Index builder aborted.", ex);
				return;
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "resource" })
	public void buildTemporalData(boolean createNewDirectory)
	{
		Cache cache = CacheFactory.getAnyInstance();
		Region<String, RAMDirectory> region = cache
				.getRegion(IndexMatrixUtil.getProperty(Constants.PROP_REGION_LUCENE));

		try {
			TemporalType[] temporalTypes = GemfireTemporalManager.getAllTemporalTypes();
			for (TemporalType type : temporalTypes) {
				IndexWriter writer = null;
				Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
				IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, analyzer);
				iwc.setOpenMode(OpenMode.CREATE);
				LuceneField luceneBuilder = new LuceneField();

				LuceneSearch ls = LuceneSearch.getLuceneSearch(type.getFullPath());
				StandardQueryParser parser = ls.createParser();
				TemporalManager tm = TemporalManager.getTemporalManager(type.getFullPath());
				Method[] attributeGetters = null;
				boolean isIdentityKeyPrimitive = false;
				try {
					List identityKeyList = tm.getIdentityKeyList();
					if (identityKeyList.size() == 0) {
						continue;
					}

					RAMDirectory directory;
					if (createNewDirectory) {
						directory = new RAMDirectory();
					} else {
						directory = region.get(type.getFullPath());
						if (directory == null) {
							directory = new RAMDirectory();
						}
					}
					writer = new IndexWriter(directory, iwc);

					// first, find the attribute getter methods
					boolean isKeyMap = false;
					KeyType keyType = null;
					for (Object identityKey : identityKeyList) {
						TemporalEntry entry = tm.getLastEntry(identityKey);
						ITemporalData data = entry.getTemporalData();
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
						isIdentityKeyPrimitive = ReflectionHelper.isPrimitiveWrapper(identityKey.getClass());
						break;
					}

					// build the numeric config
					Map<String, NumericConfig> map = parser.getNumericConfigMap();
					if (map == null) {
						map = new HashMap<String, NumericConfig>();
						parser.setNumericConfigMap(map);
					}

					if (isKeyMap) {

						KeyType[] keyTypes = KeyTypeManager.getAllRegisteredVersions(keyType.getClass());
						for (KeyType kt : keyTypes) {
							Set<String> nameSet = kt.getNameSet();
							for (String name : nameSet) {
								if (map.containsKey(name)) {
									continue;
								}
								KeyType kt2 = kt.getKeyType(name);
								String fieldName = kt2.getName();
								Class<?> fieldType = kt2.getType();

								if (fieldType == Integer.class || fieldType == int.class) {
									NumericConfig config = new NumericConfig(PRECISION_STEP,
											NumberFormat.getNumberInstance(), NumericType.INT);
									map.put(fieldName, config);
								} else if (fieldType == Long.class || fieldType == long.class) {
									NumericConfig config = new NumericConfig(PRECISION_STEP,
											NumberFormat.getNumberInstance(), NumericType.LONG);
									map.put(fieldName, config);
								} else if (fieldType == Float.class || fieldType == float.class) {
									NumericConfig config = new NumericConfig(PRECISION_STEP,
											NumberFormat.getNumberInstance(), NumericType.FLOAT);
									map.put(fieldName, config);
								} else if (fieldType == Double.class || fieldType == double.class) {
									NumericConfig config = new NumericConfig(PRECISION_STEP,
											NumberFormat.getNumberInstance(), NumericType.DOUBLE);
									map.put(fieldName, config);
								} else if (fieldType == Date.class) {
									NumericConfig config = new NumericConfig(PRECISION_STEP, DATE_FORMAT, NumericType.LONG);
									map.put(fieldName, config);
								}
							}
						}

						List<Document> docList = new ArrayList<Document>();

						for (Object identityKey : identityKeyList) {
							TemporalEntry entry = tm.getLastEntry(identityKey);
							ITemporalData data = entry.getTemporalData();
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
								}
							}
							docList.add(doc);
						}
						try {
							writer.addDocuments(docList);
						} catch (Exception ex) {
							Logger.warning("MapLite error", ex);
						}

					} else {
						for (Method method : attributeGetters) {
							Class fieldType = method.getReturnType();
							String fieldName = method.getName().substring(3);
							if (fieldType == Integer.class || fieldType == int.class) {
								NumericConfig config = new NumericConfig(PRECISION_STEP,
										NumberFormat.getNumberInstance(), NumericType.INT);
								map.put(fieldName, config);
							} else if (fieldType == Long.class || fieldType == long.class) {
								NumericConfig config = new NumericConfig(PRECISION_STEP,
										NumberFormat.getNumberInstance(), NumericType.LONG);
								map.put(fieldName, config);
							} else if (fieldType == Float.class || fieldType == float.class) {
								NumericConfig config = new NumericConfig(PRECISION_STEP,
										NumberFormat.getNumberInstance(), NumericType.FLOAT);
								map.put(fieldName, config);
							} else if (fieldType == Double.class || fieldType == double.class) {
								NumericConfig config = new NumericConfig(PRECISION_STEP,
										NumberFormat.getNumberInstance(), NumericType.DOUBLE);
								map.put(fieldName, config);
							} else if (fieldType == Date.class) {
								NumericConfig config = new NumericConfig(PRECISION_STEP, DATE_FORMAT, NumericType.LONG);
								map.put(fieldName, config);
							}
						}

						// build lucene for each attribute in the current
						// (latest)
						// data
						if (attributeGetters != null && attributeGetters.length > 0) {
							List<Document> docList = new ArrayList<Document>();

							for (Object identityKey : identityKeyList) {
								TemporalEntry entry = tm.getLastEntry(identityKey);
								ITemporalData data = entry.getTemporalData();
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
										doc.add(luceneBuilder.createField(getPropertyName(method),
												((Date) obj).getTime()));
									}
								}
								docList.add(doc);
							}
							try {
								writer.addDocuments(docList);
							} catch (Exception ex) {
								Logger.warning("Non-MapLite error", ex);
							}
						}
					}

					writer.commit();
					writer.close();

					// TODO: support file system
					// place the RamDirectory in the lucene
					region.put(type.getFullPath(), directory);

				} catch (Exception ex) {
					Logger.warning(ex);
				}
			}
		} catch (Exception ex) {
			Logger.warning("Index builder aborted.", ex);
		}
	}

	public void buildAll()
	{
		Logger.info("LuceneBuilder.buildAll() started");
		buildTemporalKeys(true);
		buildTemporalData(false);
		Logger.info("LuceneBuilder.buildAll() completed");
	}
}
