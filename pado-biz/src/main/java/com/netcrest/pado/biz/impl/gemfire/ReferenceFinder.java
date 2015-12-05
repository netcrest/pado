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
package com.netcrest.pado.biz.impl.gemfire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.PadoQueryParser;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.AttachmentSetFactory;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;

public class ReferenceFinder<K, V>
{
	/**
	 * Searches all references defined in KeyMap's KeyType.
	 * 
	 * @param keyMap
	 *            KeyMap object
	 * @param depth
	 *            Reference depth -1, to start search from the beginning, 0 to
	 *            stop search, >=1 to search depth.
	 * @param validAtTime
	 *            If -1 and asOfTime is -1, then eternal.
	 * @param asOfTime
	 *            If -1 and validAtTime is -1, then eternal.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public KeyMap getReferences(KeyMap keyMap, int depth, long validAtTime, long asOfTime, Object keyMapReferenceId)
	{
		KeyType keyType = keyMap.getKeyType();
		KeyType refKeyTypes[] = keyType.getReferences();
		if (refKeyTypes != null) {
			ExecutorService es = ExecutorServiceThreadPool.getExecutorService();
			ArrayList<Future<?>> futureList = new ArrayList();
			for (KeyType kt : refKeyTypes) {
				try {
					final int innerDepth;
					if (depth < 0) {
						innerDepth = kt.getDepth() - 1;
					} else {
						innerDepth = depth - 1;
					}
					if (innerDepth < 0) {
						continue;
					}
					Future<?> future = es.submit(new SingleLookup(keyMap, kt, innerDepth, validAtTime, asOfTime,
							keyMapReferenceId));
					futureList.add(future);

				} catch (Exception ex) {
					Logger.error("Error occurred while finding KeyMap references", ex);
				}
			}
			for (Future<?> future : futureList) {
				try {
					future.get();
				} catch (InterruptedException e) {
					Logger.error(e);
				} catch (ExecutionException e) {
					Logger.error(e);
				}
			}
		}

		return keyMap;
	}

	@SuppressWarnings("rawtypes")
	public Collection<TemporalEntry<K, V>> getCollectionReferences(Collection<TemporalEntry<K, V>> collection,
			int depth, long validAtTime, long asOfTime, Object keyMapReferenceId)
	{
		if (depth == 0) {
			return collection;
		}

		ArrayList<KeyMap> keyMapList = new ArrayList<KeyMap>(collection.size());
		Iterator<TemporalEntry<K, V>> iterator = collection.iterator();
		while (iterator.hasNext()) {
			TemporalEntry<K, V> entry = iterator.next();
			ITemporalData data = entry.getTemporalData();
			Object value;
			if (data instanceof GemfireTemporalData) {
				value = ((GemfireTemporalData) data).getValue();
			} else {
				value = data;
			}
			if (value instanceof KeyMap) {
				keyMapList.add((KeyMap) value);
			}
		}

		getKeyMapCollectionReferences(keyMapList, depth, validAtTime, asOfTime, keyMapReferenceId);

		return collection;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<V> getCollectionValueReferences(Collection<V> collection, int depth, long validAtTime,
			long asOfTime, Object keyMapReferenceId)
	{
		if (depth == 0) {
			return collection;
		}
		for (V v : collection) {
			if (v instanceof KeyMap == false) {
				return collection;
			}
		}
		getKeyMapCollectionReferences((Collection<KeyMap>) collection, depth, validAtTime, asOfTime, keyMapReferenceId);
		return collection;
	}

	@SuppressWarnings("rawtypes")
	public Map<ITemporalKey<K>, ITemporalData<K>> getMapReferences(Map<ITemporalKey<K>, ITemporalData<K>> map,
			int depth, long validAtTime, long asOfTime, Object keyMapReferenceId)
	{
		if (depth == 0 || map == null) {
			return map;
		}

		ArrayList<KeyMap> keyMapList = new ArrayList<KeyMap>(map.size());
		Iterator<ITemporalData<K>> iterator = map.values().iterator();
		while (iterator.hasNext()) {
			ITemporalData<K> data = iterator.next();
			Object value;
			if (data instanceof GemfireTemporalData) {
				value = ((GemfireTemporalData) data).getValue();
			} else {
				value = data;
			}
			if (value instanceof KeyMap) {
				keyMapList.add((KeyMap) value);
			}
		}

		getKeyMapCollectionReferences(keyMapList, depth, validAtTime, asOfTime, keyMapReferenceId);

		return map;
	}

	class SingleLookup implements Runnable
	{
		@SuppressWarnings("rawtypes")
		KeyMap keyMap;
		KeyType kt;
		int depth;
		long validAtTime;
		long asOfTime;
		Object keyMapReferenceId;

		@SuppressWarnings("rawtypes")
		SingleLookup(KeyMap keyMap, KeyType kt, int depth, long validAtTime, long asOfTime, Object keyMapReferenceId)
		{
			this.keyMap = keyMap;
			this.kt = kt;
			this.depth = depth;
			this.validAtTime = validAtTime;
			this.asOfTime = asOfTime;
			this.keyMapReferenceId = keyMapReferenceId;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void run()
		{
			CompiledUnit cu = PadoQueryParser.getCompiledUnit(kt);
			if (cu == null) {
				return;
			}

			// One or more path should always be defined by the compiled unit.
			String[] paths = cu.getPaths();
			if (paths == null || paths.length == 0) {
				return;
			}

			// Multiple paths support for co-located paths only
			// Pass in the first path in the list
			ITemporalBizLink temporalBiz = PadoServerManager.getPadoServerManager().getCatalog()
					.newInstance(ITemporalBiz.class, paths[0]);
			temporalBiz.setReference(true);
			temporalBiz.setDepth(depth);
			temporalBiz.getBizContext().getGridContextClient().setTransientData(keyMapReferenceId);
			if (cu.isPathOnly()) {
				Object id = keyMap.get(kt);
				if (id instanceof Collection) {
					Collection col = (Collection) id;
					if (col.isEmpty() == false) {
						HashSet set = new HashSet(col);
						AttachmentSetFactory factory = new AttachmentSetFactory();
						AttachmentSet as = factory.createAttachmentSet(set);
						as.setGridPath(paths[0]);
						List list = temporalBiz.getAttachments(as, validAtTime, asOfTime);
						keyMap.putReference(kt, list, keyMapReferenceId);
					}
				} else {
					Object value = temporalBiz.get(id, validAtTime, asOfTime);
					keyMap.putReference(kt, value, keyMapReferenceId);
				}
			} else {
				switch (cu.getQueryLanguage()) {
				case LUCENE:
				case OQL:
					String identityKeyQueryStatement = cu.getTemporalIdentityQuery(keyMap, validAtTime, asOfTime);
					List list = temporalBiz.getQueryValues(identityKeyQueryStatement, validAtTime, asOfTime);
					keyMap.putReference(kt, list, keyMapReferenceId);
					break;

				default:
					break;
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<KeyMap> getKeyMapCollectionReferences(Collection<KeyMap> keyMapCollection, int depth,
			long validAtTime, long asOfTime, Object keyMapReferenceId)
	{
		if (keyMapCollection == null || keyMapCollection.size() == 0) {
			return keyMapCollection;
		}

		KeyMap keyMap = keyMapCollection.iterator().next();
		KeyType keyType = keyMap.getKeyType();
		KeyType refKeyTypes[] = keyType.getReferences();
		if (refKeyTypes != null) {
			ArrayList<Future<?>> futureList = new ArrayList(refKeyTypes.length);
			for (KeyType kt : refKeyTypes) {
				try {
					int innerDepth;
					if (depth < 0) {
						innerDepth = kt.getDepth() - 1;
					} else {
						innerDepth = depth - 1;
					}
					if (innerDepth < 0) {
						continue;
					}

					ExecutorService es = ExecutorServiceThreadPool.getExecutorService();
					Future future = es.submit(new CollectionLookup(keyMapCollection, keyMap, kt, innerDepth,
							validAtTime, asOfTime, keyMapReferenceId));
					futureList.add(future);
				} catch (Exception ex) {
					Logger.error("Error occurred while finding KeyMap references", ex);
				}
			}
			for (Future<?> future : futureList) {
				try {
					future.get();
				} catch (InterruptedException e) {
					Logger.error(e);
				} catch (ExecutionException e) {
					Logger.error(e);
				}
			}
		}
		return keyMapCollection;
	}

	class CollectionLookup implements Runnable
	{
		@SuppressWarnings("rawtypes")
		Collection<KeyMap> keyMapCollection;
		KeyMap keyMap;
		KeyType kt;
		String path;
		int depth;
		long validAtTime;
		long asOfTime;
		Object keyMapReferenceId;

		@SuppressWarnings("rawtypes")
		CollectionLookup(Collection<KeyMap> keyMapCollection, KeyMap keyMap, KeyType kt, int depth, long validAtTime,
				long asOfTime, Object keyMapReferenceId)
		{
			this.keyMapCollection = keyMapCollection;
			this.keyMap = keyMap;
			this.kt = kt;
			this.depth = depth;
			this.validAtTime = validAtTime;
			this.asOfTime = asOfTime;
			this.keyMapReferenceId = keyMapReferenceId;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void run()
		{
			try {

				CompiledUnit cu = PadoQueryParser.getCompiledUnit(kt);
				if (cu == null) {
					return;
				}

				// One or more path should always be defined by the compiled
				// unit.
				String[] paths = cu.getPaths();
				if (paths == null || paths.length == 0) {
					return;
				}
				path = paths[0];
				ITemporalBizLink temporalBiz = PadoServerManager.getPadoServerManager().getCatalog()
						.newInstance(ITemporalBiz.class, path);
				temporalBiz.setReference(true);
				temporalBiz.setDepth(depth);
				temporalBiz.getBizContext().getGridContextClient().setTransientData(keyMapReferenceId);

				if (cu.isPathOnly()) {
					Object id = keyMap.get(kt);
					if (id instanceof Collection) {

						// many child objects per parent (many-to-many),
						// i.e., each position in Positions has child objects.

						// Place all identity keys from each collection
						// of keyMapList into one attachment set to
						// perform a single look up.

						// kmListMap holds all KeyMap objects out of
						// the entire collections. set holds all of
						// identity keys.
						Map<Object, List> kmListMap = new HashMap();
						HashSet set = new HashSet();
						for (KeyMap km : keyMapCollection) {
							Collection col2 = (Collection) km.get(kt);
							for (Object id2 : col2) {
								List kmList = kmListMap.get(id2);
								if (kmList == null) {
									kmList = new ArrayList();
									kmListMap.put(id2, kmList);
								}
								kmList.add(km);
								set.add(id2);
							}
						}

						if (kmListMap.isEmpty() == false) {

							// Get attachments with a single lookup and
							// assign the results to individual KeyMap objects
							// stored in kmListMap.
							AttachmentSetFactory factory = new AttachmentSetFactory();
							AttachmentSet as = factory.createAttachmentSet(set);
							as.setGridPath(path);
							Map<ITemporalKey<?>, ITemporalData<?>> map = temporalBiz.getAttachmentsEntries(as,
									validAtTime, asOfTime);
							Set<Map.Entry<ITemporalKey<?>, ITemporalData<?>>> mapSet = map.entrySet();
							for (Map.Entry<ITemporalKey<?>, ITemporalData<?>> entry : mapSet) {
								List<KeyMap> list = kmListMap.get(entry.getKey().getIdentityKey());
								if (list != null) {
									ITemporalData data = entry.getValue();
									if (data instanceof GemfireTemporalData) {
										for (KeyMap km : list) {
											Collection refCol = (Collection) km.getReference(kt);
											if (refCol == null) {
												refCol = (Collection) id.getClass().newInstance();
												km.putReference(kt, refCol, keyMapReferenceId);
											}
											refCol.add(((GemfireTemporalData) data).getValue());
										}
									} else {
										for (KeyMap km : list) {
											Collection refCol = (Collection) km.getReference(kt);
											if (refCol == null) {
												refCol = (Collection) id.getClass().newInstance();
												km.putReference(kt, refCol, keyMapReferenceId);
											}
											refCol.add(data);
										}
									}
								}
							}
						}

					} else {

						// Single child per parent (one-to-many), i.e.,
						// Portfolio has many Positions.

						// kmListMap holds KeyMap objects so that
						// they can be updated with the temporal
						// results received.
						Map<Object, KeyMap> kmListMap = new HashMap();
						for (KeyMap km : keyMapCollection) {
							Object identityKey = km.get(kt);
							if (identityKey != null) {
								kmListMap.put(identityKey, km);
							}
						}
						if (kmListMap.size() == 0) {
							return;
						}

						Set idSet = kmListMap.keySet();
						AttachmentSetFactory factory = new AttachmentSetFactory();
						AttachmentSet as = factory.createAttachmentSet(idSet);
						as.setGridPath(path);
						Map<ITemporalKey<?>, ITemporalData<?>> map = temporalBiz.getAttachmentsEntries(as, validAtTime,
								asOfTime);
						Set<Map.Entry<ITemporalKey<?>, ITemporalData<?>>> mapSet = map.entrySet();

						List<KeyMap> keyMapList2 = new ArrayList<KeyMap>();
						for (Map.Entry<ITemporalKey<?>, ITemporalData<?>> entry : mapSet) {
							KeyMap km = kmListMap.get(entry.getKey().getIdentityKey());
							if (km != null) {
								ITemporalData data = entry.getValue();
								Object value;
								if (data instanceof GemfireTemporalData) {
									value = ((GemfireTemporalData) data).getValue();
									km.putReference(kt, value, keyMapReferenceId);
								} else {
									value = data;
									km.putReference(kt, data, keyMapReferenceId);
								}
								if (depth > 0 && value instanceof KeyMap) {
									keyMapList2.add((KeyMap) value);
								}
							}
						}

						// Handle nested KeyMap objects
						if (depth > 0 && keyMapList2.size() > 0) {
							getKeyMapCollectionReferences(keyMapList2, depth, validAtTime, asOfTime, keyMapReferenceId);
						}
					}
				} else {
					switch (cu.getQueryLanguage()) {
					case LUCENE:
					case OQL:
						for (KeyMap km : keyMapCollection) {
							String identityKeyQueryStatement = cu.getTemporalIdentityQuery(km, validAtTime, asOfTime);
							List list = temporalBiz.getQueryValues(identityKeyQueryStatement, validAtTime, asOfTime);
							km.putReference(kt, list, keyMapReferenceId);
						}
						break;

					default:
						break;
					}
				}

			} catch (Exception ex) {
				Logger.error("Error occurred while finding KeyMap references", ex);
			}
		}

	}
}
