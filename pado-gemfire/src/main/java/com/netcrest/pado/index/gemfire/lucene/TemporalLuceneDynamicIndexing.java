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
package com.netcrest.pado.index.gemfire.lucene;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.store.MMapDirectory;

import com.gemstone.gemfire.cache.EntryEvent;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.index.provider.lucene.DateTool;
import com.netcrest.pado.index.provider.lucene.LuceneBuilder;
import com.netcrest.pado.index.provider.lucene.LuceneField;
import com.netcrest.pado.index.provider.lucene.LuceneSearch;
import com.netcrest.pado.index.provider.lucene.ReflectionHelper;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;

/**
 * LuceneDynamicIndexing dynamically appends Lucene indexes for {@link #CacheListener}
 * events for a single path.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalLuceneDynamicIndexing
{
	private String fullPath;
	private boolean isVerbose;
	private IndexWriter writer;
	private LuceneSearch luceneSearch;
	private StandardQueryParser parser;
	private boolean isClosed;

	public TemporalLuceneDynamicIndexing(String fullPath)
	{
		this.fullPath = fullPath;
		resetWriter();
	}

	/**
	 * Resets the writer that writes to the file system.
	 */
	private void resetWriter()
	{
		close();

		File file = new File("lucene" + fullPath);
		if (file.exists() == false) {
			file.mkdirs();
		}
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		try {
			Path path = Paths.get(file.getPath());
			MMapDirectory directory = new MMapDirectory(path);
			writer = new IndexWriter(directory, iwc);
			luceneSearch = LuceneSearch.getLuceneSearch(fullPath);
			parser = luceneSearch.createParser();
			isClosed = false;
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	/**
	 * Closes TemporalLuceneDynamicIndexing by freeing up system resources. Once
	 * closed, this object is no longer usable until {@link #open} is invoked.
	 */
	public synchronized void close()
	{
		if (writer != null) {
			try {
				writer.close();
				if (writer.getDirectory() != null) {
					writer.getDirectory().close();
				}
			} catch (CorruptIndexException e) {
				Logger.error(e);
			} catch (IOException e) {
				Logger.error(e);
			} finally {
				isClosed = true;
			}
		}
	}

	/**
	 * Returns true if closed. It is closed only if {@link #close()} has been
	 * invoked. To reopen, invoke {@link #open()}. The default is false, i.e.,
	 * this object is opened when initially created.
	 */
	public boolean isClosed()
	{
		return isClosed;
	}

	/**
	 * Opens this object. If it is already opened, then this call has no effect.
	 */
	public synchronized void open()
	{
		if (isClosed()) {
			resetWriter();
		}
	}

	/**
	 * Sets the verbose flag. If true, then additional information is logged.
	 * Default: false.
	 * 
	 * @param verbose
	 *            true to output additional information to the log file.
	 */
	public void setVerbose(boolean verbose)
	{
		this.isVerbose = verbose;
	}

	/**
	 * Returns true is the verbose flag has been enabled. Default: false.
	 */
	public boolean isVerbose()
	{
		return isVerbose;
	}

	/**
	 * Processes the specified CacheListener event by appending a corresponding
	 * Lucene index.
	 * 
	 * @param event
	 *            CacheListener temporal event.
	 */
	public synchronized void processEvent(EntryEvent<ITemporalKey, ITemporalData> event)
	{
		try {
			buildTemporalEntry(parser, event, writer);
			writer.commit();
		} catch (IOException ex) {
			Logger.error(ex);
		}

		if (isVerbose) {
			StringBuffer buffer = new StringBuffer(200);
			buffer.append("Lucene Dynamic Index Added (single event): ");
			buffer.append(fullPath);
			buffer.append(" IdentityKey [");
			ITemporalKey tk = event.getKey();
			Object identityKey = tk.getIdentityKey();
			identityKey = event.getKey().getIdentityKey();
			buffer.append(identityKey);
			buffer.append("]\n");
			Logger.info(buffer.toString());
		}
	}

	/**
	 * Processes the specified list of CacheListener events by appending
	 * corresponding Lucene indexes.
	 * 
	 * @param events
	 *            List of CacheListener temporal events
	 */
	public synchronized void processEvents(List<EntryEvent<ITemporalKey, ITemporalData>> events)
	{
		try {
			buildTemporalEntries(parser, events, writer);
			writer.commit();
		} catch (IOException ex) {
			Logger.error(ex);
		}

		if (isVerbose) {
			Logger.info("Lucene Dynamic Indexes Added: " + events.size());
		}
	}

	/**
	 * Builds Lucene indexes for the specified temporal events.
	 * 
	 * @param parser
	 *            Lucene parser
	 * @param events
	 *            Temporal events
	 * @param writer
	 *            Index writer
	 */
	private void buildTemporalEntries(StandardQueryParser parser, List<EntryEvent<ITemporalKey, ITemporalData>> events,
			IndexWriter writer)
	{
		LuceneBuilder luceneBuilder = LuceneBuilder.getLuceneBuilder();

		boolean isKeyMap = false;
		KeyType keyType = null;
		Set<Object> keySet = null;
		Object firstDataObject = null;
		Method[] attributeGetters = null;
		boolean isIdentityKeyPrimitive = false;
		for (EntryEvent<ITemporalKey, ITemporalData> event : events) {
			ITemporalKey tk = event.getKey();
			ITemporalData data = event.getNewValue();

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
			Object identityKey = tk.getIdentityKey();
			isIdentityKeyPrimitive = ReflectionHelper.isPrimitiveWrapper(identityKey.getClass());
			break;
		}

		LuceneField luceneField = new LuceneField();
		SimpleDateFormat format = (SimpleDateFormat) DateTool.Resolution.DAY.format.clone();
		List<Document> docList = new ArrayList<Document>();
		try {
			if (keyType != null) {
				for (EntryEvent<ITemporalKey, ITemporalData> event : events) {
					ITemporalKey tk = event.getKey();
					ITemporalData data = event.getNewValue();

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
					Document doc = luceneBuilder.createKeyMapDocument(parser, writer, tk, data, -1, luceneField,
							keyType, keyMap, nameSet, isIdentityKeyPrimitive, true, format);
					docList.add(doc);
				}
			} else if (keySet != null) {
				for (EntryEvent<ITemporalKey, ITemporalData> event : events) {
					ITemporalKey tk = event.getKey();
					ITemporalData data = event.getNewValue();

					Map dataMap;
					if (data instanceof GemfireTemporalData) {
						dataMap = (Map) ((GemfireTemporalData) data).getValue();
					} else {
						dataMap = (Map) data;
					}

					// TODO: See if we can support binary types
					// createDoc();
					Document doc = luceneBuilder.createMapDocument(parser, writer, tk, data, luceneField, dataMap,
							keySet, isIdentityKeyPrimitive, format);
					docList.add(doc);
				}
			} else {
				if (attributeGetters != null && attributeGetters.length > 0) {
					for (EntryEvent<ITemporalKey, ITemporalData> event : events) {
						ITemporalKey tk = event.getKey();
						ITemporalData data = event.getNewValue();

						Document doc = luceneBuilder.createPojoDocument(parser, writer, tk, data, -1l, luceneField,
								attributeGetters, isIdentityKeyPrimitive, true/* isNew */, format);
						docList.add(doc);
					}
				}
			}
		} catch (Exception ex) {
			Logger.error(ex);
		}
		try {
			writer.addDocuments(docList);
		} catch (Exception ex) {
			Logger.error(ex);
		}
	}

	/**
	 * Builds a single Lucene index for the specified temporal event.
	 * 
	 * @param parser
	 *            Lucene parser
	 * @param events
	 *            Temporal events
	 * @param writer
	 *            Index writer
	 */
	private void buildTemporalEntry(StandardQueryParser parser, EntryEvent<ITemporalKey, ITemporalData> event,
			IndexWriter writer)
	{
		LuceneBuilder luceneBuilder = LuceneBuilder.getLuceneBuilder();

		boolean isKeyMap = false;
		KeyType keyType = null;
		Set<Object> keySet = null;
		Object firstDataObject = null;
		Method[] attributeGetters = null;
		boolean isIdentityKeyPrimitive = false;

		// First, extract out the key type.
		ITemporalKey tk = event.getKey();
		ITemporalData data = event.getNewValue();

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
		Object identityKey = tk.getIdentityKey();
		isIdentityKeyPrimitive = ReflectionHelper.isPrimitiveWrapper(identityKey.getClass());

		// Next, create Lucene doc for the event
		LuceneField luceneField = new LuceneField();
		SimpleDateFormat format = (SimpleDateFormat) DateTool.Resolution.DAY.format.clone();
		Document doc = null;
		try {
			if (keyType != null) {
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
				doc = luceneBuilder.createKeyMapDocument(parser, writer, tk, data, -1, luceneField, keyType, keyMap,
						nameSet, isIdentityKeyPrimitive, true, format);
			} else if (keySet != null) {
				Map dataMap;
				if (data instanceof GemfireTemporalData) {
					dataMap = (Map) ((GemfireTemporalData) data).getValue();
				} else {
					dataMap = (Map) data;
				}

				// TODO: See if we can support binary types
				// createDoc();
				doc = luceneBuilder.createMapDocument(parser, writer, tk, data, luceneField, dataMap, keySet,
						isIdentityKeyPrimitive, format);
			} else {
				if (attributeGetters != null && attributeGetters.length > 0) {
					doc = luceneBuilder.createPojoDocument(parser, writer, tk, data, -1l, luceneField,
							attributeGetters, isIdentityKeyPrimitive, true/* isNew */, format);
				}
			}
		} catch (Exception ex) {
			Logger.error(ex);
		}

		// Append the newly created doc to Lucene
		if (doc != null) {
			try {
				writer.addDocument(doc);
			} catch (Exception ex) {
				Logger.error(ex);
			}
		}
	}
}
