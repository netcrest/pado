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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.NumberDateFormat;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.RAMDirectory;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.internal.util.BlobHelper;
import com.netcrest.pado.IRoutingKey;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalList;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalKey;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.temporal.TemporalType;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;
import com.netcrest.pado.util.GridUtil;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LuceneBuilder
{
	private final static int PRECISION_STEP = 8;
	private final static boolean isRamDirectory = PadoUtil.getBoolean("lucene.ramDirectory", false);
	private final static int THREAD_COUNT = PadoUtil.getInteger("lucene.builder.thread.count", 4);
	
	private final static LuceneBuilder builder = new LuceneBuilder();
	
	private final ExecutorService executorService;

	// private final static boolean isColoate =
	// Boolean.getBoolean("pado.lucene.builder.coloate");

	public static LuceneBuilder getLuceneBuilder()
	{
		return builder;
	}

	private LuceneBuilder()
	{
		executorService = Executors.newFixedThreadPool(THREAD_COUNT, new ThreadFactory() {
			
			int threadNum = 1;

			@Override
			public Thread newThread(Runnable r)
			{
				Thread thread = new Thread(r);
				thread.setName("Pado-LuceneBuilder-" + threadNum++);
				thread.setDaemon(true);
				return thread;
			}
			
		});
	}

	private void configNumericType(StandardQueryParser parser, String fieldName, Class<?> fieldType,
			NumberDateFormat numericDateFormat)
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
			NumericConfig config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(),
					NumericType.FLOAT);
			map.put(fieldName, config);
		} else if (fieldType == Double.class || fieldType.toString().equals("double")) {
			NumericConfig config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(),
					NumericType.DOUBLE);
			map.put(fieldName, config);
		} else if (fieldType == Date.class) {
			NumericConfig config = new NumericConfig(PRECISION_STEP, numericDateFormat, NumericType.LONG);
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

	private void buildTemporalEntries(boolean createNewDirectory)
	{
		Cache cache = CacheFactory.getAnyInstance();
		Region<String, RAMDirectory> region = cache
				.getRegion(IndexMatrixUtil.getProperty(Constants.PROP_REGION_LUCENE));

		try {
			TemporalType[] temporalTypes = GemfireTemporalManager.getAllTemporalTypes();
			List<TemporalBuilderTask<Object>> taskList = new ArrayList(temporalTypes.length);
			for (TemporalType type : temporalTypes) {
				SimpleDateFormat format = (SimpleDateFormat) DateTool.Resolution.DAY.format.clone();
				taskList.add(new TemporalBuilderTask(createNewDirectory, region, type, format));
			}
			if (taskList.size() > 0) {
				executorService.invokeAll(taskList);
			}
		} catch (Exception ex) {
			Logger.warning("Index builder aborted.", ex);
		}
	}

	private void buildTemporalEntries(boolean createNewDirectory, String[] gridPaths)
	{
		Cache cache = CacheFactory.getAnyInstance();
		Region<String, RAMDirectory> region = cache
				.getRegion(IndexMatrixUtil.getProperty(Constants.PROP_REGION_LUCENE));

		try {
			TemporalType[] temporalTypes = GemfireTemporalManager.getAllTemporalTypes();
			List<TemporalBuilderTask<Object>> taskList = new ArrayList(temporalTypes.length);
			for (TemporalType type : temporalTypes) {
				String path = GridUtil.getChildPath(type.getFullPath());
				for (String gp : gridPaths) {
					if (path.equals(gp)) {
						SimpleDateFormat format = (SimpleDateFormat) DateTool.Resolution.DAY.format.clone();
						taskList.add(new TemporalBuilderTask(createNewDirectory, region, type, format));
						break;
					}
				}
			}
			if (taskList.size() > 0) {
				executorService.invokeAll(taskList);
			}
		} catch (Exception ex) {
			Logger.warning("Index builder aborted.", ex);
		}
	}

	private class TemporalBuilderTask<V> implements Callable<V>
	{
		boolean createNewDirectory;
		Region<String, RAMDirectory> region;
		TemporalType type;
		SimpleDateFormat format;

		TemporalBuilderTask(boolean createNewDirectory, Region<String, RAMDirectory> region, TemporalType type,
				SimpleDateFormat format)
		{
			this.createNewDirectory = createNewDirectory;
			this.region = region;
			this.type = type;
			this.format = format;
		}

		@Override
		public V call() throws Exception
		{

			// Index all keys and values in all temporal lists for this region.
			IndexWriter writer = null;
			IndexReader reader = null;
			Directory directory = null;
			Analyzer analyzer = new StandardAnalyzer(LuceneSearch.LUCENE_VERSION);
			IndexWriterConfig iwc = new IndexWriterConfig(LuceneSearch.LUCENE_VERSION, analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			LuceneField luceneField = new LuceneField();

			LuceneSearch ls = LuceneSearch.getLuceneSearch(type.getFullPath());
			StandardQueryParser parser = ls.createParser();

			Method[] attributeGetters = null;
			boolean isIdentityKeyPrimitive = false;

			// First, block updates from going into temporal lists by invoking
			// the temporal manager's pause()/flush() methods. The finally
			// block resumes the dispatcher.
			TemporalManager tm = TemporalManager.getTemporalManager(type.getFullPath());
			tm.pause();
			
			// flush() creates a race condition
			tm.flush();

			try {
				List identityKeyList = tm.getIdentityKeyList();
				if (isRamDirectory) {
					if (createNewDirectory) {
						directory = new RAMDirectory();
					} else {
						directory = region.get(type.getFullPath());
						if (directory == null) {
							directory = new RAMDirectory();
						}
					}
				} else {
					File file = new File("lucene" + type.getFullPath());
					// Delete the directory and create a new. This is not required 
					// but for troubleshooting, leave it for now.
					if (file.exists()) {
						FileUtils.forceDelete(file);
					}
					file.mkdirs();
					directory = new MMapDirectory(file);
				}

				writer = new IndexWriter(directory, iwc);
				writer.commit();

				try {
					reader = DirectoryReader.open(directory);
				} catch (CorruptIndexException e1) {
					Logger.error(e1);
					throw new RuntimeException(e1);
				} catch (IOException e1) {
					Logger.error(e1);
					throw new RuntimeException(e1);
				}

				// first, find the attribute getter methods
				boolean isKeyMap = false;
				KeyType keyType = null;
				Set<Object> keySet = null;
				Object firstDataObject = null;
				for (Object identityKey : identityKeyList) {
					TemporalEntry entry = tm.getLastEntry(identityKey);
					ITemporalData data = entry.getTemporalData();

					if (data instanceof GemfireTemporalData) {
						firstDataObject = ((GemfireTemporalData) data).getValue();
					} else {
						firstDataObject = data;
					}
					isKeyMap = firstDataObject instanceof KeyMap;
					if (isKeyMap == false) {
						if (firstDataObject instanceof Map) {
							keySet = ((Map) firstDataObject).keySet();
						} else {
							attributeGetters = ReflectionHelper.getAttributeGetters(data.getClass());
						}
					} else {
						keyType = ((KeyMap) firstDataObject).getKeyType();
						if (keyType == null) {
							keySet = ((Map) firstDataObject).keySet();
						}
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

				NumericConfig config;
				if (keyType != null) {

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
							config = createNumericMap(fieldName, fieldType);
							if (config != null) {
								map.put(fieldName, config);
							}
						}
					}

					List<Document> docList = new ArrayList<Document>();

					for (Object identityKey : identityKeyList) {
						ITemporalList tl = tm.getTemporalList(identityKey);

						// TODO: ITemporalList should also return the
						// internal list
						TemporalDataList tdl = tl.getTemporalDataList();
						List<TemporalEntry> list = tdl.getTemporalList();

						for (TemporalEntry entry : list) {

							ITemporalData data = entry.getTemporalData();
							KeyMap keyMap;
							if (data instanceof GemfireTemporalData) {
								keyMap = (KeyMap) ((GemfireTemporalData) data).getValue();
							} else {
								keyMap = (KeyMap) data;
							}
							keyType = keyMap.getKeyType();
							Set<String> nameSet = keyType.getNameSet();

							// TODO: See if we can support binary types
							// createDoc();

							Document doc = createKeyMapDocument(parser, writer, entry.getTemporalKey(),
									entry.getTemporalData(), -1, luceneField, keyType, keyMap, nameSet,
									isIdentityKeyPrimitive, true, format);
							docList.add(doc);
						}

						if (docList.size() > 1000) {
							writer.addDocuments(docList);
							docList.clear();
						}
					}
					try {
						if (docList.isEmpty() == false) {
							writer.addDocuments(docList);
							docList.clear();
						}

					} catch (Exception ex) {
						Logger.error("KeyMap error", ex);
					}

				} else if (keySet != null) {

					Map dataMap = (Map) firstDataObject;
					for (Object key : keySet) {
						Object value = dataMap.get(key);
						if (value != null) {
							String fieldName = key.toString();
							Class<?> fieldType = value.getClass();
							config = createNumericMap(fieldName, fieldType);
							if (config != null) {
								map.put(fieldName, config);
							}
						}
					}

					List<Document> docList = new ArrayList<Document>();
					for (Object identityKey : identityKeyList) {
						ITemporalList tl = tm.getTemporalList(identityKey);

						// TODO: ITemporalList should also return the
						// internal list
						TemporalDataList tdl = tl.getTemporalDataList();
						List<TemporalEntry> list = tdl.getTemporalList();

						for (TemporalEntry entry : list) {

							ITemporalData data = entry.getTemporalData();
							if (data instanceof GemfireTemporalData) {
								dataMap = (Map) ((GemfireTemporalData) data).getValue();
							} else {
								dataMap = (Map) data;
							}

							// TODO: See if we can support binary types
							// createDoc();

							Document doc = createMapDocument(parser, writer, entry.getTemporalKey(),
									entry.getTemporalData(), luceneField, dataMap, keySet, isIdentityKeyPrimitive,
									format);
							docList.add(doc);
						}

						if (docList.size() > 1000) {
							writer.addDocuments(docList);
							docList.clear();
						}
					}
					try {
						if (docList.isEmpty() == false) {
							writer.addDocuments(docList);
							docList.clear();
						}

					} catch (Exception ex) {
						Logger.error("Map error", ex);
					}

				} else {
					
					// build lucene for each attribute in the current
					// (latest) data
					if (attributeGetters != null && attributeGetters.length > 0) {
						for (Method method : attributeGetters) {
							Class fieldType = method.getReturnType();
							String fieldName = method.getName().substring(3);
							if (fieldType == Integer.class || fieldType == int.class) {
								config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(),
										NumericType.INT);
								map.put(fieldName, config);
							} else if (fieldType == Long.class || fieldType == long.class) {
								config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(),
										NumericType.LONG);
								map.put(fieldName, config);
							} else if (fieldType == Float.class || fieldType == float.class) {
								config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(),
										NumericType.FLOAT);
								map.put(fieldName, config);
							} else if (fieldType == Double.class || fieldType == double.class) {
								config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(),
										NumericType.DOUBLE);
								map.put(fieldName, config);
							} else if (fieldType == Date.class) {
								config = new NumericConfig(PRECISION_STEP, DateTool.Resolution.DAY.numericFormat,
										NumericType.LONG);
								map.put(fieldName, config);
							}
						}
					
						List<Document> docList = new ArrayList<Document>();

						for (Object identityKey : identityKeyList) {
							ITemporalList tl = tm.getTemporalList(identityKey);
							TemporalEntry entry = tm.getLastEntry(identityKey);
							// ITemporalData data = entry.getTemporalData();
							// Document doc = luceneField.createDocument();
							// if (isIdentityKeyPrimitive) {
							// doc.add(luceneField.createField("IdentityKey",
							// identityKey.toString()));
							// } else {
							// doc.add(luceneField.createIdentityKeyField(identityKey));
							// }
							// ITemporalList tl =
							// tm.getTemporalList(identityKey);
							// addTemporalKey(parser, searcher, writer, tl,
							// entry.getTemporalKey(), doc, luceneField);
							// for (Method method : attributeGetters) {
							// Object obj = method.invoke(data);
							// Class fieldType = method.getReturnType();
							// if (fieldType == String.class) {
							// doc.add(luceneField.createField(getPropertyName(method),
							// obj.toString()));
							// } else if (fieldType == Integer.class ||
							// fieldType == int.class) {
							// doc.add(luceneField.createField(getPropertyName(method),
							// (Integer) obj));
							// } else if (fieldType == Long.class ||
							// fieldType == long.class) {
							// doc.add(luceneField.createField(getPropertyName(method),
							// (Long) obj));
							// } else if (fieldType == Float.class ||
							// fieldType == float.class) {
							// doc.add(luceneField.createField(getPropertyName(method),
							// (Float) obj));
							// } else if (fieldType == Double.class ||
							// fieldType == double.class) {
							// doc.add(luceneField.createField(getPropertyName(method),
							// (Double) obj));
							// } else if (fieldType == Date.class) {
							// doc.add(luceneField.createField(getPropertyName(method),
							// ((Date) obj).getTime()));
							// }
							// }
							Document doc = createPojoDocument(parser, writer, entry.getTemporalKey(),
									entry.getTemporalData(), -1, luceneField, attributeGetters, isIdentityKeyPrimitive,
									true, format);
							docList.add(doc);
						}
						try {
							writer.addDocuments(docList);
						} catch (Exception ex) {
							Logger.error("POJO error", ex);
						}
					}
				}

				writer.commit();
				writer.close();

				// place the RamDirectory in the region
				if (isRamDirectory) {
					region.put(type.getFullPath(), (RAMDirectory) directory);
				} else {
					directory.close();
				}

			} catch (Exception ex) {
				Logger.warning(ex);
			} finally {
				
				// Resume the temporal manager event dispatcher.
				tm.resume();
				
				// Close all resources.
				if (writer != null) {
					try {
						writer.close();
					} finally {
						if (directory != null && IndexWriter.isLocked(directory)) {
							try {
								IndexWriter.unlock(directory);
								directory.close();
							} catch (IOException ex) {
								// ignore
							}
						}
					}
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ex) {
						// ignore
					}
				}
			}

			return null;
		}

	}

	private NumericConfig createNumericMap(String fieldName, Class<?> fieldType)
	{
		NumericConfig config = null;
		if (fieldType == Integer.class || fieldType == int.class) {
			config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(), NumericType.INT);
		} else if (fieldType == Long.class || fieldType == long.class) {
			config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(), NumericType.LONG);
		} else if (fieldType == Float.class || fieldType == float.class) {
			config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(), NumericType.FLOAT);
		} else if (fieldType == Double.class || fieldType == double.class) {
			config = new NumericConfig(PRECISION_STEP, NumberFormat.getNumberInstance(), NumericType.DOUBLE);
		} else if (fieldType == Date.class) {
			config = new NumericConfig(PRECISION_STEP, DateTool.Resolution.DAY.numericFormat, NumericType.LONG);
		}
		return config;
	}

	// private void createDoc()
	// {
	// Document doc = luceneField.createDocument();
	// if (isIdentityKeyPrimitive) {
	// doc.add(luceneField.createField("IdentityKey", identityKey.toString()));
	// } else {
	// doc.add(luceneField.createIdentityKeyField(identityKey));
	// }
	// addTemporalKey(parser, searcher, writer, tl, entry.getTemporalKey(), doc,
	// luceneField, true);
	// for (String name : nameSet) {
	// Object obj = keyMap.get(name);
	// // obj can be null (e.g., version difference
	// // or app defined)
	// if (obj == null) {
	// continue;
	// }
	// KeyType kt = keyType.getKeyType(name);
	// Class fieldType = kt.getType();
	// if (fieldType == String.class) {
	// doc.add(luceneField.createField(name, obj.toString()));
	// } else if (fieldType == Integer.class || fieldType == int.class) {
	// doc.add(luceneField.createField(name, (Integer) obj));
	// } else if (fieldType == Long.class || fieldType == long.class) {
	// doc.add(luceneField.createField(name, (Long) obj));
	// } else if (fieldType == Float.class || fieldType == float.class) {
	// doc.add(luceneField.createField(name, (Float) obj));
	// } else if (fieldType == Double.class || fieldType == double.class) {
	// doc.add(luceneField.createField(name, (Double) obj));
	// } else if (fieldType == Date.class) {
	// doc.add(luceneField.createField(name, ((Date) obj).getTime()));
	// }
	// }
	// }

	/**
	 * Returns concatenated String values of the specified key map. Concatenates
	 * nested KeyMap objects also.
	 * 
	 * @param keySet
	 *            Key names
	 * @param dataMap
	 *            Data map
	 * @param buffer
	 *            String buffer to append String values. If null one is created.
	 */
	private static StringBuffer getMapStringValue(Set<?> keySet, Map dataMap, StringBuffer buffer)
	{
		if (keySet == null || dataMap == null) {
			return null;
		}
		if (buffer == null) {
			buffer = new StringBuffer(200);
		}
		for (Object key : keySet) {
			Object value = dataMap.get(key);
			if (value instanceof String) {
				buffer.append((String) value);
				buffer.append(" ");
			} else if (value instanceof Map) {
				Map dataMap2 = (KeyMap) value;
				getMapStringValue(dataMap2.keySet(), dataMap2, buffer);
			}
		}
		return buffer;
	}

	public Document createKeyMapDocument(StandardQueryParser parser, IndexWriter writer, ITemporalKey tk,
			ITemporalData data, long endWrittenTime, LuceneField luceneField, KeyType keyType, KeyMap keyMap,
			Set<String> keyTypeNameSet, boolean isIdentityKeyPrimitive, boolean isNewDoc, SimpleDateFormat format)
			throws IOException
	{
		Document doc = luceneField.createDocument();

		// __doc is the default field that represents all of String values
		// in KeyMap including nested KeyMap objects.
		StringBuffer buffer = getMapStringValue(keyTypeNameSet, keyMap, null);
		doc.add(luceneField.createField("__doc", buffer.toString().toLowerCase()));
		Object identityKey = tk.getIdentityKey();
		if (identityKey instanceof IRoutingKey) {
			IRoutingKey routingKey = (IRoutingKey) identityKey;
			doc.add(luceneField.createField("RoutingKey", routingKey.getRoutingKey().toString().toLowerCase()));
		}
		if (isIdentityKeyPrimitive) {
			doc.add(luceneField.createField("IdentityKey", identityKey.toString()));
		} else {
			doc.add(luceneField.createIdentityKeyField(identityKey));
		}
		addTemporalKeyToDocument(tk, doc, luceneField, format);

		for (String fieldName : keyTypeNameSet) {
			Object obj = keyMap.get(fieldName);
			// obj can be null (e.g., version difference
			// or app defined)
			if (obj == null) {
				continue;
			}
			KeyType kt = keyType.getKeyType(fieldName);
			Class fieldType = kt.getType();
			IndexableField indexableField = createIndexableField(luceneField, fieldName, fieldType, obj);
			doc.add(indexableField);
		}
		return doc;
	}

	public Document createMapDocument(StandardQueryParser parser, IndexWriter writer, ITemporalKey tk,
			ITemporalData data, LuceneField luceneField, Map dataMap, Set<Object> keySet,
			boolean isIdentityKeyPrimitive, SimpleDateFormat format) throws IOException
	{
		Document doc = luceneField.createDocument();

		// __doc is the default field that represents all of String values
		// in KeyMap including nested KeyMap objects.
		StringBuffer buffer = getMapStringValue(keySet, dataMap, null);
		doc.add(luceneField.createField("__doc", buffer.toString().toLowerCase()));
		Object identityKey = tk.getIdentityKey();
		if (identityKey instanceof IRoutingKey) {
			IRoutingKey routingKey = (IRoutingKey) identityKey;
			doc.add(luceneField.createField("RoutingKey", routingKey.getRoutingKey().toString().toLowerCase()));
		}
		if (isIdentityKeyPrimitive) {
			doc.add(luceneField.createField("IdentityKey", identityKey.toString()));
		} else {
			doc.add(luceneField.createIdentityKeyField(identityKey));
		}
		addTemporalKeyToDocument(tk, doc, luceneField, format);

		for (Object key : keySet) {
			String fieldName = key.toString();
			Object obj = dataMap.get(fieldName);
			// obj can be null (e.g., version difference
			// or app defined)
			if (obj == null) {
				continue;
			}
			Class fieldType = obj.getClass();
			IndexableField indexableField = createIndexableField(luceneField, fieldName, fieldType, obj);
			doc.add(indexableField);
		}
		return doc;
	}

	private IndexableField createIndexableField(LuceneField luceneField, String fieldName, Class<?> fieldType,
			Object obj)
	{
		IndexableField indexableField;

		if (fieldType == String.class) {
			indexableField = luceneField.createField(fieldName, obj.toString().toLowerCase());
		} else if (fieldType == Integer.class || fieldType == int.class) {
			indexableField = luceneField.createField(fieldName, (Integer) obj);
		} else if (fieldType == Long.class || fieldType == long.class) {
			indexableField = luceneField.createField(fieldName, (Long) obj);
		} else if (fieldType == Float.class || fieldType == float.class) {
			indexableField = luceneField.createField(fieldName, (Float) obj);
		} else if (fieldType == Double.class || fieldType == double.class) {
			indexableField = luceneField.createField(fieldName, (Double) obj);
		} else if (fieldType == Date.class) {
			indexableField = luceneField.createField(fieldName, ((Date) obj).getTime());
		} else {
			indexableField = luceneField.createField(fieldName, obj.toString().toLowerCase());
		}
		return indexableField;
	}

	private void addTemporalKeyToDocument(ITemporalKey tk, Document doc, LuceneField luceneField,
			SimpleDateFormat format) throws IOException
	{
		doc.add(luceneField.createDateField("StartValidTime", tk.getStartValidTime(), format));
		doc.add(luceneField.createDateField("EndValidTime", tk.getEndValidTime(), format));
		doc.add(luceneField.createDateField("StartWrittenTime", tk.getWrittenTime(), format));
		doc.add(luceneField.createDateField("EndWrittenTime", ((TemporalKey) tk).getEndWrittenTime(), format));
		String username = tk.getUsername();
		if (username == null) {
			username = "";
		} else {
			username = username.toLowerCase();
		}
		doc.add(luceneField.createField("Username", username));

		try {
			byte[] blob = BlobHelper.serializeToBlob(tk);
			doc.add(luceneField.createField("TemporalKey", blob));
		} catch (IOException e) {
			Logger.warning(e);
		}

		// @debug
		// SimpleDateFormat dateFormat = DateTool.Resolution.DAY.format;
		// System.out.print(tk.getIdentityKey() + " " + dateFormat.format(new
		// Date(tk.getStartValidTime())) + " "
		// + dateFormat.format(new Date(tk.getEndValidTime())) + " "
		// + dateFormat.format(new Date(tk.getWrittenTime())) + " "
		// + dateFormat.format(((TemporalKey)tk).getEndWrittenTime()));
	}

	private void addTemporalKeyToKeyMapDocument_UpdateDocument(StandardQueryParser parser, IndexWriter writer,
			ITemporalList temporalList, ITemporalKey tk, long endWrittenTime, Document doc, LuceneField luceneField,
			KeyMap keyMap, Set<String> keyTypeNameSet, boolean isIdentityKeyPrimitive, boolean isNewDoc,
			SimpleDateFormat format) throws IOException
	{
		int index = temporalList.getIndex(tk);

		// Update the end written time of the previous tk with this tk's written
		// time.
		if (isNewDoc) {
			int prevIndex = index - 1;
			TemporalEntry prevEntry = temporalList.getTemporalEntry(prevIndex);
			if (prevEntry != null) {
				// the new tk's written time is the end written time of the
				// previous tk.
				long prevEndWrittenTime = tk.getWrittenTime();
				KeyMap prevKeyMap = (KeyMap) prevEntry.getValue();
				KeyType prevKeyType = prevKeyMap.getKeyType();
				updateKeyMapDocument(parser, writer, temporalList, prevEntry.getTemporalKey(),
						prevEntry.getTemporalData(), prevEndWrittenTime, luceneField, prevKeyType, prevKeyMap,
						prevKeyType.getNameSet(), isIdentityKeyPrimitive, format);
			}
		}

		// Next tk's written time is tk's end written time
		if (endWrittenTime == -1) {
			int nextIndex = index + 1;
			ITemporalKey nextTK = temporalList.getTemporalKey(nextIndex);
			if (nextTK != null) {
				endWrittenTime = nextTK.getWrittenTime();
			} else {
				endWrittenTime = LuceneSearch.MAX_TIME;
			}
		}

		doc.add(luceneField.createDateField("StartValidTime", tk.getStartValidTime(), format));
		doc.add(luceneField.createDateField("EndValidTime", tk.getEndValidTime(), format));
		doc.add(luceneField.createDateField("StartWrittenTime", tk.getWrittenTime(), format));
		doc.add(luceneField.createDateField("EndWrittenTime", endWrittenTime, format));
		doc.add(luceneField.createField("Username", tk.getUsername().toLowerCase()));
		try {
			byte[] blob = BlobHelper.serializeToBlob(tk);
			doc.add(luceneField.createField("TemporalKey", blob));
		} catch (IOException e) {
			Logger.warning(e);
		}

		// @debug
		// SimpleDateFormat dateFormat = DateTool.Resolution.DAY.format;
		// System.out.print(tk.getIdentityKey() + " " + dateFormat.format(new
		// Date(tk.getStartValidTime())) + " "
		// + dateFormat.format(new Date(tk.getEndValidTime())) + " "
		// + dateFormat.format(new Date(tk.getWrittenTime())) + " "
		// + dateFormat.format(endWrittenTime));
	}

	public Document createPojoDocument(StandardQueryParser parser, IndexWriter writer, ITemporalKey tk,
			ITemporalData data, long endWrittenTime, LuceneField luceneField, Method[] attributeGetters,
			boolean isIdentityKeyPrimitive, boolean isNewDoc, SimpleDateFormat format) throws IOException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Document doc = luceneField.createDocument();
		doc.add(luceneField.createField("__doc", data.toString().toLowerCase()));
		Object identityKey = tk.getIdentityKey();
		if (identityKey instanceof IRoutingKey) {
			IRoutingKey routingKey = (IRoutingKey) identityKey;

			doc.add(luceneField.createField("RoutingKey", routingKey.getRoutingKey().toString().toLowerCase()));
		}
		if (isIdentityKeyPrimitive) {
			doc.add(luceneField.createField("IdentityKey", identityKey.toString()));
		} else {
			doc.add(luceneField.createIdentityKeyField(identityKey));
		}
		addTemporalKeyToPojoDocument(parser, writer, tk, endWrittenTime, doc, luceneField, attributeGetters,
				isIdentityKeyPrimitive, isNewDoc, format);
		for (Method method : attributeGetters) {
			Object obj = method.invoke(data);
			if (obj == null) {
				continue;
			}
			Class fieldType = method.getReturnType();
			if (fieldType == String.class) {
				doc.add(luceneField.createField(getPropertyName(method), obj.toString().toLowerCase()));
			} else if (fieldType == Integer.class || fieldType == int.class) {
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
				doc.add(luceneField.createField(getPropertyName(method), obj.toString().toLowerCase()));
			}
		}

		return doc;
	}

	private void addTemporalKeyToPojoDocument(StandardQueryParser parser, IndexWriter writer, ITemporalKey tk,
			long endWrittenTime, Document doc, LuceneField luceneField, Method[] attributeGetters,
			boolean isIdentityKeyPrimitive, boolean isNewDoc, SimpleDateFormat format) throws IllegalArgumentException,
			IOException, IllegalAccessException, InvocationTargetException
	{
		doc.add(luceneField.createDateField("StartValidTime", tk.getStartValidTime(), format));
		doc.add(luceneField.createDateField("EndValidTime", tk.getEndValidTime(), format));
		doc.add(luceneField.createDateField("StartWrittenTime", tk.getWrittenTime(), format));
		doc.add(luceneField.createDateField("EndWrittenTime", ((TemporalKey) tk).getEndWrittenTime(), format));
		doc.add(luceneField.createField("Username", tk.getUsername()));
		try {
			byte[] blob = BlobHelper.serializeToBlob(tk);
			doc.add(luceneField.createField("TemporalKey", blob));
		} catch (IOException e) {
			Logger.warning(e);
		}
	}

	private void addTemporalKeyToPojoDocument_UpdateDocument(StandardQueryParser parser, IndexSearcher searcher,
			IndexWriter writer, ITemporalList tl, ITemporalKey tk, long endWrittenTime, Document doc,
			LuceneField luceneField, Method[] attributeGetters, boolean isIdentityKeyPrimitive, boolean isNewDoc,
			SimpleDateFormat format) throws IllegalArgumentException, IOException, IllegalAccessException,
			InvocationTargetException
	{
		int index = tl.getIndex(tk);
		if (isNewDoc) {
			int prevIndex = index - 1;
			TemporalEntry prevEntry = tl.getTemporalEntry(prevIndex);
			if (prevEntry != null) {
				long prevEndWrittenTime = tk.getWrittenTime();
				updatePojoDocument(parser, searcher, writer, prevEntry.getTemporalKey(), prevEntry.getTemporalData(),
						prevEndWrittenTime, luceneField, attributeGetters, isIdentityKeyPrimitive, format);
			}
		}

		if (endWrittenTime == -1) {
			int nextIndex = index + 1;
			ITemporalKey nextTK = tl.getTemporalKey(nextIndex);
			if (nextTK != null) {
				endWrittenTime = nextTK.getWrittenTime();
			} else {
				endWrittenTime = LuceneSearch.MAX_TIME;
			}
		}

		doc.add(luceneField.createDateField("StartValidTime", tk.getStartValidTime(), format));
		doc.add(luceneField.createDateField("EndValidTime", tk.getEndValidTime(), format));
		doc.add(luceneField.createDateField("StartWrittenTime", tk.getWrittenTime(), format));
		doc.add(luceneField.createDateField("EndWrittenTime", endWrittenTime, format));
		doc.add(luceneField.createField("Username", tk.getUsername()));
		try {
			byte[] blob = BlobHelper.serializeToBlob(tk);
			doc.add(luceneField.createField("TemporalKey", blob));
		} catch (IOException e) {
			Logger.warning(e);
		}
	}

	// private void updateDocTerm(StandardQueryParser parser, IndexSearcher
	// searcher, IndexWriter writer, ITemporalKey tk,
	// long endWrittenTime, LuceneField luceneField)
	// {
	// Term term = new Term("__sk", tk.hashCode());
	// TermQuery query = new TermQuery(term);
	// TermQueryBuilder builder = new TermQueryBuilder();
	//
	// // DirectoryReader reader;
	// // try {
	// // String queryString = String.format(TEMPORAL_KEY_QUERY_PREDICATE,
	// // tk.getIdentityKey(),
	// // tk.getStartValidTime(), tk.getEndValidTime(), tk.getWrittenTime());
	// // query = parser.parse(queryString, "IdentityKey");
	// // } catch (Exception ex) {
	// // // Lucene 4.7 bug, internal message not serializable
	// // // Send message instead of nesting the cause.
	// // throw new RuntimeException(ex.getMessage());
	// // }
	//
	// Document doc = null;
	// try {
	// TopDocs results = searcher.search(query, null, Integer.MAX_VALUE);
	// for (ScoreDoc hit : results.scoreDocs) {
	// doc = searcher.doc(hit.doc);
	// break;
	// }
	// } catch (IOException ex) {
	// Logger.error(ex);
	// return;
	// }
	//
	// if (doc == null) {
	// return;
	// }
	//
	// doc.removeField("EndWrittenTime");
	// doc.add(luceneField.createDateField("EndWrittenTime", endWrittenTime,
	// Resolution.DAY));
	//
	// // write the old document to the index with the modifications
	// writer.updateDocument(term, doc);
	// }

	private final static String TEMPORAL_KEY_QUERY_PREDICATE = "%s AND StartValidTime:%s AND EndValidTime: %s AND StartWrittenTime: %s";

	private void updateKeyMapDocument(StandardQueryParser parser, IndexWriter writer, ITemporalList tl,
			ITemporalKey tk, ITemporalData data, long endWrittenTime, LuceneField luceneField, KeyType keyType,
			KeyMap keyMap, Set<String> keyTypeNameSet, boolean isIdentityKeyPrimitive, SimpleDateFormat format)
			throws IOException
	{
		Query query = null;
		try {
			String queryString = String.format(TEMPORAL_KEY_QUERY_PREDICATE, tk.getIdentityKey(),
					tk.getStartValidTime(), tk.getEndValidTime(), tk.getWrittenTime());
			query = parser.parse(queryString, "__doc");
		} catch (Exception ex) {
			// Lucene 4.7 bug, internal message not serializable
			// Send message instead of nesting the cause.
			throw new RuntimeException(ex.getMessage());
		}

		writer.deleteDocuments(query);

		Document doc = createKeyMapDocument(parser, writer, tk, data, endWrittenTime, luceneField, keyType, keyMap,
				keyTypeNameSet, isIdentityKeyPrimitive, false, format);
		writer.addDocument(doc);
	}

	private void updatePojoDocument(StandardQueryParser parser, IndexSearcher searcher, IndexWriter writer,
			ITemporalKey tk, ITemporalData data, long endWrittenTime, LuceneField luceneField,
			Method[] attributeGetters, boolean isIdentityKeyPrimitive, SimpleDateFormat format) throws IOException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Query query = null;
		try {
			String queryString = String.format(TEMPORAL_KEY_QUERY_PREDICATE, tk.getIdentityKey(),
					tk.getStartValidTime(), tk.getEndValidTime(), tk.getWrittenTime());
			query = parser.parse(queryString, "__doc");
		} catch (Exception ex) {
			// Lucene 4.7 bug, internal message not serializable
			// Send message instead of nesting the cause.
			throw new RuntimeException(ex.getMessage());
		}

		writer.deleteDocuments(query);

		Document doc = createPojoDocument(parser, writer, tk, data, endWrittenTime, luceneField, attributeGetters,
				isIdentityKeyPrimitive, false, format);
		writer.addDocument(doc);
	}

	public void buildAll()
	{
		Logger.info("LuceneBuilder.buildAll() started");
		buildTemporalEntries(true);
		Logger.info("LuceneBuilder.buildAll() completed");
	}

	public void buildIndexes(String... gridPaths)
	{
		if (gridPaths == null || gridPaths.length == 0) {
			buildAll();
		} else {
			StringBuffer sb = new StringBuffer(100);
			sb.append("[");
			for (int i = 0; i < gridPaths.length; i++) {
				if (i > 0) {
					sb.append(" ");
				}
				sb.append(gridPaths[i]);
			}
			sb.append("]");
			String gridPathsStr = sb.toString();
			Logger.info("LuceneBuilder.buildIndexes() started: gridPaths=" + gridPathsStr);
			buildTemporalEntries(true, gridPaths);
			Logger.info("LuceneBuilder.buildIndexes() completed: gridPaths=" + gridPathsStr);
		}
	}
}
