/*
 * Copyright (c) 2013-2016 Netcrest Technologies, LLC. All rights reserved.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.VirtualCompiledUnit2;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.server.VirtualPathEngine;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.AttachmentSetFactory;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class VirtualPathEntityFinder<K, V>
{
	/**
	 * Searches all references defined in KeyMap's KeyType.
	 * 
	 * @param virtualPath
	 *            Virtual path
	 * @param keyMap
	 *            KeyMap object
	 * @param depth
	 *            Reference depth -1, to start search from the beginning, 0 to
	 *            stop search, >=1 to search depth.
	 * @param validAtTime
	 *            If -1 and asOfTime is -1, then eternal.
	 * @param asOfTime
	 *            If -1 and validAtTime is -1, then eternal.
	 * @param keyMapReferenceId
	 *            reference ID for identifying distributed queries
	 */
	public KeyMap getReferences(ICatalog catalog, String virtualPath, KeyMap keyMap, int depth, long validAtTime,
			long asOfTime, Object keyMapReferenceId)
	{
		VirtualCompiledUnit2 vcu = VirtualPathEngine.getVirtualPathEngine().getVirtualCompiledUnit(virtualPath);
		return getReferences(catalog, vcu, keyMap, depth, validAtTime, asOfTime, keyMapReferenceId);
	}

	public KeyMap getReferences(ICatalog catalog, KeyMap vpd, KeyMap keyMap, int depth, long validAtTime, long asOfTime,
			Object keyMapReferenceId)
	{
		VirtualCompiledUnit2 vcu = new VirtualCompiledUnit2(vpd);
		return getReferences(catalog, vcu, keyMap, depth, validAtTime, asOfTime, keyMapReferenceId);
	}

	private KeyMap getReferences(ICatalog catalog, VirtualCompiledUnit2 vcu, KeyMap keyMap, int depth, long validAtTime,
			long asOfTime, Object keyMapReferenceId)
	{
		if (vcu != null) {
			ExecutorService es = ExecutorServiceThreadPool.getExecutorService();
			VirtualCompiledUnit2.Argument args[] = vcu.getArguments();
			if (args != null) {
				ArrayList<Future<?>> futureList = new ArrayList<Future<?>>(args.length);
				for (VirtualCompiledUnit2.Argument arg : args) {
					try {
						final int innerDepth;
						if (depth < 0) {
							innerDepth = arg.getDepth() - 1;
						} else {
							innerDepth = depth - 1;
						}
						if (innerDepth < 0) {
							continue;
						}
						Future<?> future = es.submit(new SingleLookup(catalog, arg.getArgName(), arg.getCu(),
								arg.isOne(), keyMap, innerDepth, validAtTime, asOfTime, keyMapReferenceId));
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
		}

		return keyMap;
	}

	public Collection<TemporalEntry<K, V>> getCollectionReferences(ICatalog catalog, String virtualPath,
			Collection<TemporalEntry<K, V>> collection, int depth, long validAtTime, long asOfTime,
			Object keyMapReferenceId)
	{
		if (depth == 0) {
			return collection;
		}
		VirtualCompiledUnit2 vcu = VirtualPathEngine.getVirtualPathEngine().getVirtualCompiledUnit(virtualPath);
		return getCollectionReferences(catalog, vcu, collection, depth, validAtTime, asOfTime, keyMapReferenceId);
	}

	public Collection<TemporalEntry<K, V>> getCollectionReferences(ICatalog catalog, VirtualCompiledUnit2 vcu,
			Collection<TemporalEntry<K, V>> collection, int depth, long validAtTime, long asOfTime,
			Object keyMapReferenceId)
	{
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

		getKeyMapCollectionReferences(catalog, vcu, keyMapList, depth, validAtTime, asOfTime, keyMapReferenceId);

		return collection;
	}

	public Collection<KeyMap> getKeyMapCollectionReferences(ICatalog catalog, VirtualCompiledUnit2 vcu,
			Collection<KeyMap> keyMapCollection, int depth, long validAtTime, long asOfTime, Object keyMapReferenceId)
	{
		if (keyMapCollection == null || keyMapCollection.size() == 0) {
			return keyMapCollection;
		}

		VirtualCompiledUnit2.Argument args[] = vcu.getArguments();
		if (args != null) {
			ArrayList<Future<?>> futureList = new ArrayList<Future<?>>(args.length);
			for (VirtualCompiledUnit2.Argument arg : args) {
				try {
					final int innerDepth;
					if (depth < 0) {
						innerDepth = arg.getDepth() - 1;
					} else {
						innerDepth = depth - 1;
					}
					if (innerDepth < 0) {
						continue;
					}

					ExecutorService es = ExecutorServiceThreadPool.getExecutorService();
					Future future = es.submit(new CollectionLookup(catalog, vcu, arg, keyMapCollection, innerDepth,
							validAtTime, asOfTime, keyMapReferenceId));
					futureList.add(future);
				} catch (Exception ex) {
					Logger.error("Error occurred while finding KeyMap references", ex);
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
		}
		return keyMapCollection;
	}

	class SingleLookup implements Runnable
	{
		ICatalog catalog;
		KeyMap keyMap;
		String argName;
		CompiledUnit cu;
		boolean isOne;
		int depth;
		long validAtTime;
		long asOfTime;
		Object keyMapReferenceId;

		SingleLookup(ICatalog catalog, String argName, CompiledUnit cu, boolean isOne, KeyMap keyMap, int depth,
				long validAtTime, long asOfTime, Object keyMapReferenceId)
		{
			this.catalog = catalog;
			this.keyMap = keyMap;
			this.argName = argName;
			this.cu = cu;
			this.isOne = isOne;
			this.depth = depth;
			this.validAtTime = validAtTime;
			this.asOfTime = asOfTime;
			this.keyMapReferenceId = keyMapReferenceId;
		}

		@Override
		public void run()
		{
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
			String gridPath = paths[0];
			ITemporalBizLink temporalBiz = catalog.newInstance(ITemporalBiz.class, gridPath);
			temporalBiz.setDepth(depth);
			if (cu.isPathOnly()) {
				Object id = keyMap.get(argName);
				if (id instanceof Collection) {
					Collection col = (Collection) id;
					if (col.isEmpty() == false) {
						HashSet set = new HashSet(col);
						AttachmentSetFactory factory = new AttachmentSetFactory();
						AttachmentSet as = factory.createAttachmentSet(set);
						as.setGridPath(gridPath);
						List list = temporalBiz.getAttachments(as, validAtTime, asOfTime);
						if (isOne) {
							if (list != null && list.size() > 0) {
								keyMap.put(argName, list.get(0));
							} else {
								keyMap.put(argName, null);
							}
						} else {
							keyMap.put(argName, list);
						}
						// keyMap.putReference(kt, list, keyMapReferenceId);
					}
				} else {
					Object value = temporalBiz.get(id, validAtTime, asOfTime);
					// keyMap.putReference(kt, value, keyMapReferenceId);
					keyMap.put(argName, value);
				}
			} else if (cu.isFunction()) {
				if ("Identity".equals(cu.getFunctionName())) {
					if (VirtualPathEngine.getVirtualPathEngine().isVirtualPath(gridPath)) {
						Object attributes[] = (String[])cu.getAttributes();
						Object[] args = cu.getArgValues(keyMap);
						String[] argValues = null;
						String pql = gridPath + "?";
						if (args != null && args.length > 0) {
							argValues = new String[args.length];
							for (int i = 0; i < argValues.length; i++) {
								argValues[i] = args[i].toString();
								pql += attributes[i] + ":(\"" + argValues[i] + "\") ";
							}
						}
						List list = temporalBiz.getQueryValues(pql, validAtTime, asOfTime);
						if (isOne) {
							if (list == null || list.size() == 0) {
								keyMap.put(argName, null);
							} else {
								keyMap.put(argName, list.get(0));
							}
						} else {
							keyMap.put(argName, list);
						}
					} else {
						String identityKey = cu.getQuery(keyMap);
						Object value = temporalBiz.get(identityKey, validAtTime, asOfTime);
						if (isOne || value == null) {
							keyMap.put(argName, value);
						} else {
							keyMap.put(argName, Collections.singletonList(value));
						}
					}
				}
			} else {
				switch (cu.getQueryLanguage()) {
				case LUCENE:
				case OQL:
					String identityKeyQueryStatement = cu.getTemporalIdentityQuery(keyMap, validAtTime, asOfTime);
					List list = temporalBiz.getQueryValues(identityKeyQueryStatement, validAtTime, asOfTime);
					if (isOne) {
						if (list != null && list.size() > 0) {
							keyMap.put(argName, list.get(0));
						} else {
							keyMap.put(argName, null);
						}
					} else {
						keyMap.put(argName, list);
					}
					break;

				default:
					break;
				}
			}
		}

		public void run_old()
		{
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
			String gridPath = paths[0];
			String virtualEntityPath = null;
			if (VirtualPathEngine.getVirtualPathEngine().isVirtualPath(gridPath)) {
				virtualEntityPath = paths[0];
				VirtualCompiledUnit2 vcu = VirtualPathEngine.getVirtualPathEngine()
						.getVirtualCompiledUnit(virtualEntityPath);
				gridPath = vcu.getEntityGridPath();
			}
			ITemporalBizLink temporalBiz = catalog.newInstance(ITemporalBiz.class, gridPath);
			temporalBiz.setDepth(depth);
			temporalBiz.__setVirtualEntityPath(virtualEntityPath);
			if (cu.isPathOnly()) {
				Object id = keyMap.get(argName);
				if (id instanceof Collection) {
					Collection col = (Collection) id;
					if (col.isEmpty() == false) {
						HashSet set = new HashSet(col);
						AttachmentSetFactory factory = new AttachmentSetFactory();
						AttachmentSet as = factory.createAttachmentSet(set);
						as.setGridPath(gridPath);
						List list = temporalBiz.getAttachments(as, validAtTime, asOfTime);
						if (isOne) {
							if (list != null && list.size() > 0) {
								keyMap.put(argName, list.get(0));
							} else {
								keyMap.put(argName, null);
							}
						} else {
							keyMap.put(argName, list);
						}
						// keyMap.putReference(kt, list, keyMapReferenceId);
					}
				} else {
					Object value = temporalBiz.get(id, validAtTime, asOfTime);
					// keyMap.putReference(kt, value, keyMapReferenceId);
					keyMap.put(argName, value);
				}
			} else {
				switch (cu.getQueryLanguage()) {
				case LUCENE:
				case OQL:
					String identityKeyQueryStatement = cu.getTemporalIdentityQuery(keyMap, validAtTime, asOfTime);
					List list = temporalBiz.getQueryValues(identityKeyQueryStatement, validAtTime, asOfTime);
					if (isOne) {
						if (list != null && list.size() > 0) {
							keyMap.put(argName, list.get(0));
						} else {
							keyMap.put(argName, null);
						}
					} else {
						keyMap.put(argName, list);
					}
					break;

				default:
					break;
				}
			}
		}
	}

	class CollectionLookup implements Runnable
	{
		ICatalog catalog;
		VirtualCompiledUnit2 vcu;
		VirtualCompiledUnit2.Argument arg;
		Collection<KeyMap> keyMapCollection;
		String path;
		int depth;
		long validAtTime;
		long asOfTime;
		Object keyMapReferenceId;

		CollectionLookup(ICatalog catalog, VirtualCompiledUnit2 vcu, VirtualCompiledUnit2.Argument arg,
				Collection<KeyMap> keyMapCollection, int depth, long validAtTime, long asOfTime,
				Object keyMapReferenceId)
		{
			this.catalog = catalog;
			this.vcu = vcu;
			this.arg = arg;
			this.keyMapCollection = keyMapCollection;
			this.depth = depth;
			this.validAtTime = validAtTime;
			this.asOfTime = asOfTime;
			this.keyMapReferenceId = keyMapReferenceId;
		}

		@Override
		public void run()
		{
			try {
				// One or more path should always be defined by the compiled
				// unit.
				KeyMap keyMap = null;
				if (keyMapCollection.size() > 0) {
					keyMap = (KeyMap) keyMapCollection.iterator().next();
				} else {
					return;
				}
				CompiledUnit cu = arg.getCu();
				String[] paths = cu.getPaths();
				if (paths == null || paths.length == 0) {
					return;
				}
				path = paths[0];
				ITemporalBizLink temporalBiz = PadoServerManager.getPadoServerManager().getCatalog()
						.newInstance(ITemporalBiz.class, path);
				temporalBiz.setDepth(depth);
				temporalBiz.getBizContext().getGridContextClient().setTransientData(keyMapReferenceId);

				if (cu.isPathOnly()) {
					Object id = keyMap.get(arg.getArgName());
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
							Collection col2 = (Collection) km.get(arg.getArgName());
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
											Collection refCol = (Collection) km.getReference(arg.getArgName());
											if (refCol == null) {
												refCol = (Collection) id.getClass().newInstance();
												km.put(arg.getArgName(), refCol);
											}
											refCol.add(((GemfireTemporalData) data).getValue());
										}
									} else {
										for (KeyMap km : list) {
											Collection refCol = (Collection) km.getReference(arg.getArgName());
											if (refCol == null) {
												refCol = (Collection) id.getClass().newInstance();
												km.put(arg.getArgName(), refCol);
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
							Object identityKey = km.get(arg.getArgName());
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
									km.put(arg.getArgName(), value);
								} else {
									value = data;
									km.put(arg.getArgName(), data);
								}
								if (depth > 0 && value instanceof KeyMap) {
									keyMapList2.add((KeyMap) value);
								}
							}
						}

						// Handle nested KeyMap objects
						if (depth > 0 && keyMapList2.size() > 0) {
							getKeyMapCollectionReferences(catalog, vcu, keyMapList2, depth, validAtTime, asOfTime,
									keyMapReferenceId);
						}
					}
				} else if (cu.isFunction()) {
					if ("Identity".equals(cu.getFunctionName())) {
						if (VirtualPathEngine.getVirtualPathEngine().isVirtualPath(path)) {
							for (KeyMap km : keyMapCollection) {
								// TODO: Execute a single query that has all values
								Object attributes[] = (String[])cu.getAttributes();
								Object[] args = cu.getArgValues(km);
								String[] argValues = null;
								String pql = path + "?";
								if (args != null && args.length > 0) {
									argValues = new String[args.length];
									for (int i = 0; i < argValues.length; i++) {
										argValues[i] = args[i].toString();
										pql += attributes[i] + ":(\"" + argValues[i] + "\") ";
									}
								}
								List list = temporalBiz.getQueryValues(pql, validAtTime, asOfTime);
								if (arg.isOne()) {
									if (list == null || list.size() == 0) {
										km.put(arg.getArgName(), null);
									} else {
										km.put(arg.getArgName(), list.get(0));
									}
								} else {
									km.put(arg.getArgName(), list);
								}
							}
						} else {
							for (KeyMap km : keyMapCollection) {
								// TODO: Execute a single query that has all values
								String identityKey = cu.getQuery(km);
								Object value = temporalBiz.get(identityKey, validAtTime, asOfTime);
								if (arg.isOne() || value == null) {
									km.put(arg.getArgName(), value);
								} else {
									km.put(arg.getArgName(), Collections.singletonList(value));
								}
							}
						}
					}
				} else {
					switch (cu.getQueryLanguage()) {
					case LUCENE:
					case OQL:
						for (KeyMap km : keyMapCollection) {
							// TODO: Execute a single query that has all values
							String identityKeyQueryStatement = cu.getTemporalIdentityQuery(km, validAtTime, asOfTime);
							List list = temporalBiz.getQueryValues(identityKeyQueryStatement, validAtTime, asOfTime);
							if (arg.isOne()) {
								if (list == null || list.size() == 0) {
									km.put(arg.getArgName(), null);
								} else {
									km.put(arg.getArgName(), list.get(0));
								}
							} else {
								km.put(arg.getArgName(), list);
							}
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
