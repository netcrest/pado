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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.Struct;
import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IEntryListener;
import com.netcrest.pado.IGridRouter;
import com.netcrest.pado.IGridRouter.Type;
import com.netcrest.pado.IPado;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.exception.EntryExistsException;
import com.netcrest.pado.exception.GridAccessNotAllowedException;
import com.netcrest.pado.exception.GridNotAvailableException;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoServerException;
import com.netcrest.pado.gemfire.GemfireEntryImpl;
import com.netcrest.pado.gemfire.GemfireGridService;
import com.netcrest.pado.internal.impl.PartitionedGridRouter;
import com.netcrest.pado.util.IBulkLoader;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GridMapBizImplLocal<K, V> implements IGridMapBiz<K, V>, IBizLocal
{
	@Resource
	IGridMapBiz<K, V> biz;

	private String gridPath;
	private GemfireGridService gridService;
	// <gridId, Region>
	private Map<String, Region> regionMap;
	private HashMap<IEntryListener, Map<String, GemfireEntryListenerImpl>> entryListenerMap = new HashMap(1, 1f);
	private IGridRouter gridRouter;
	private ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Pado-GridMapBizImplLocalCached");
            t.setDaemon(true);
            return t;
        }
    });

	private IBulkLoader<K, V> bulkLoader;

	public GridMapBizImplLocal()
	{
	}

	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		this.biz = (IGridMapBiz<K, V>) biz;
		if (args != null && args.length > 0) {
			setGridPath((String) args[0]);
		}
	}

	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	// @dpark - added to support grid paths. this needs to be checked.
	// it was working without this method prior to making grids independent.
	private void resetBizContext()
	{
		this.biz.getBizContext().reset();
		String gridIds[] = this.biz.getBizContext().getGridService().getGridIds(gridPath);
		this.biz.getBizContext().getGridContextClient().setGridIds(gridIds);
		this.biz.getBizContext().getGridContextClient().setAdditionalArguments(gridPath);
	}

	/**
	 * Throws a GridNotAvailableException with an appropriate message.
	 * 
	 * @param key
	 *            Key object
	 * @throws GridNotAvailableException
	 *             Always thrown
	 */
	private void throwGridNotAvailableException(Object key) throws GridNotAvailableException
	{
		String gridId = ((PartitionedGridRouter) gridRouter).findGridIdForPath(biz.getBizContext(), key);
		if (gridId == null) {
			throw new GridNotAvailableException("None of the grids is available for this operation");
		} else {
			throw new GridNotAvailableException("The target grid is not available: gridId=" + gridId);
		}
	}

	/**
	 * Throws a GridAccessNotAllowedException with an approprate message.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @throws GridAccessNotAllowedException
	 *             Always thrown
	 */
	private void throwGridAccessNotAllowedException(String gridId) throws GridAccessNotAllowedException
	{
		throw new GridAccessNotAllowedException("This app does not allow access to the grid."
				+ " The grid ID must be part of the allowed list in Pado. [appId=" + gridService.getAppId()
				+ ", gridId=" + gridId + "]");
	}

	@Override
	public void setGridPath(String gridPath)
	{
		this.gridPath = gridPath;
		gridService = (GemfireGridService) biz.getBizContext().getGridService();
		regionMap = gridService.getGridRegionMap(gridPath);
		gridRouter = gridService.getGridRouter(gridPath);
		resetBizContext();
	}

	@Override
	public String getGridPath()
	{
		return gridPath;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) throws GridNotAvailableException
	{
		if (gridPath == null || gridPath.length() == 0) {
			return;
		}
		if (gridRouter != null) {

			// first, create map per grid in keyMap
			// <gridId, Map<K, V>>
			Map<String, Map<K, V>> keyMap = new HashMap<String, Map<K, V>>(regionMap.size() + 1);
			Set<?> set = map.entrySet();
			for (Object obj : set) {
				Map.Entry<K, V> entry = (Map.Entry<K, V>) obj;
				String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), entry.getKey());
				if (gridId == null) {
					throwGridNotAvailableException(entry.getKey());
				}
				Map<K, V> partitionedMap = keyMap.get(gridId);
				if (partitionedMap == null) {
					partitionedMap = new HashMap<K, V>();
					keyMap.put(gridId, partitionedMap);
				}
				partitionedMap.put((K) entry.getKey(), (V) entry.getValue());
			}

			// make sure the grids are accessible
			Set<Map.Entry<String, Map<K, V>>> keySet = keyMap.entrySet();
			for (Map.Entry<String, Map<K, V>> entry : keySet) {
				Region<K, V> region = gridService.getRegion(entry.getKey(), gridPath);
				// region can be null if the router is partitioned and the grid
				// ID is not
				// in the allowed list.
				if (region == null) {
					throwGridAccessNotAllowedException(entry.getKey());
				}
			}

			// next, invoke putAll() per grid
			for (Map.Entry<String, Map<K, V>> entry : keySet) {
				Region<K, V> region = gridService.getRegion(entry.getKey(), gridPath);
				region.putAll(entry.getValue());
			}

		} else {

			String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
			if (gridIds == null || gridIds.length == 0) {
				throw new GridNotAvailableException("None of the grids is available for this operation");
			}
			if (gridIds.length == 1) {
				Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
				region.putAll(map);
			} else {
				for (int i = 0; i < gridIds.length; i++) {
					final String gridId = gridIds[i];
					final Map<? extends K, ? extends V> map2 = map;
					executorService.execute(new Runnable() {
						@Override
						public void run()
						{
							Region<K, V> region = gridService.getRegion(gridId, gridPath);
							region.putAll(map2);
						}
					});
				}
			}

		}
	}

	@Override
	public V put(final K key, final V value)
	{
		if (gridPath == null || gridPath.length() == 0) {
			return null;
		}
		if (gridRouter != null) {

			String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), key);
			if (gridId == null) {
				throwGridNotAvailableException(key);
			}
			Region<K, V> region = gridService.getRegion(gridId, gridPath);
			// region can be null if the router is partitioned and the grid ID
			// is not
			// in the allowed list.
			if (region == null) {
				throwGridAccessNotAllowedException(gridId);
			}
			return region.put(key, value);

		} else {

			String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
			if (gridIds == null || gridIds.length == 0) {
				throw new GridNotAvailableException("None of the grids is available for this operation");
			}
			if (gridIds.length == 1) {
				Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
				return region.put(key, value);
			} else {
				Future<V>[] futures = new Future[gridIds.length];
				for (int i = 0; i < gridIds.length; i++) {
					final String gridId = gridIds[i];
					futures[i] = executorService.submit(new Callable() {

						@Override
						public V call() throws Exception
						{
							Region<K, V> region = gridService.getRegion(gridId, gridPath);
							return region.put(key, value);
						}
					});
				}
				for (Future<V> future : futures) {
					try {
						return future.get();
					} catch (InterruptedException e) {
						// ignore
					} catch (ExecutionException e) {
						// ignore
					}
				}
			}
			return null;

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V put(K key, V value, boolean fromRemote)
	{
		if (fromRemote) {
			return biz.put(key, value);
		} else {
			return put(key, value);
		}
	}

	@Override
	public Map<K, V> getAll(Collection<? extends K> keyCollection) throws GridNotAvailableException
	{
		if (gridRouter != null) {

			// first, create key sets per grid in keyMap
			// <gridId, Set<K>>
			Map<String, Set<K>> keyMap = new HashMap(regionMap.size() + 1);
			for (K key : keyCollection) {
				String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), key);
				if (gridId == null) {
					throwGridNotAvailableException(key);
				}
				Set<K> partitionedSet = keyMap.get(gridId);
				if (partitionedSet == null) {
					partitionedSet = new HashSet();
					keyMap.put(gridId, partitionedSet);
				}
				partitionedSet.add(key);
			}

			// <gridId, Set<K>>
			Set<Map.Entry<String, Set<K>>> keySet = keyMap.entrySet();

			// make sure the grids are accessible
			for (Map.Entry<String, Set<K>> entry : keySet) {
				Region<K, V> region = gridService.getRegion(entry.getKey(), gridPath);
				// region can be null if the router is partitioned and the grid
				// ID is not
				// in the allowed list.
				if (region == null) {
					throwGridAccessNotAllowedException(entry.getKey());
				}
			}

			// next, invoke getAll() per grid
			Map<K, V> resultMap = new HashMap();
			for (Map.Entry<String, Set<K>> entry : keySet) {
				Region<K, V> region = gridService.getRegion(entry.getKey(), gridPath);
				Map<K, V> map = region.getAll(entry.getValue());
				resultMap.putAll(map);
			}
			return resultMap;

		} else {

			String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
			if (gridIds == null || gridIds.length == 0) {
				throw new GridNotAvailableException("None of the grids is available for this operation");
			}
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			return region.getAll(keyCollection);

		}
	}

	@Override
	public V get(Object key) throws GridNotAvailableException
	{
		if (gridRouter != null) {

			String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), key);
			if (gridId == null) {
				throwGridNotAvailableException(key);
			}
			Region<K, V> region = gridService.getRegion(gridId, gridPath);
			// region can be null if the router is partitioned and the grid ID
			// is not
			// in the allowed list.
			if (region == null) {
				throwGridAccessNotAllowedException(gridId);
			}
			return region.get(key);

		} else {

			String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
			if (gridIds == null || gridIds.length == 0) {
				throw new GridNotAvailableException("None of the grids is available for this operation");
			}
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			return region.get(key);

		}
	}

	@Override
	public int size()
	{
		String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length == 0) {
			throw new GridNotAvailableException("None of the grids is available for this operation");
		}
		int size = 0;
		if (gridRouter != null && gridRouter.getType() == Type.PARTITIONED) {
			for (int i = 0; i < gridIds.length; i++) {
				Region<K, V> region = gridService.getRegion(gridIds[i], gridPath);
				size += region.size();
			}
		} else {
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			size = region.size();
		}
		return size;
	}

	@Override
	public boolean isEmpty()
	{
		String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length == 0) {
			throw new GridNotAvailableException("None of the grids is available for this operation");
		}
		boolean isEmpty = true;
		if (gridRouter != null) {
			if (gridRouter.getType() == Type.PARTITIONED) {
				for (int i = 0; i < gridIds.length; i++) {
					Region<K, V> region = gridService.getRegion(gridIds[i], gridPath);
					isEmpty = region.isEmpty();
					if (isEmpty == false) {
						break;
					}
				}
			} else {
				String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), null);
				if (gridId == null) {
					throw new GridNotAvailableException("None of the grids is available for this operation");
				} else {
					Region<K, V> region = gridService.getRegion(gridId, gridPath);
					// region can be null if the router is partitioned and the
					// grid ID is not
					// in the allowed list.
					if (region == null) {
						throwGridAccessNotAllowedException(gridId);
					}
					isEmpty = region.isEmpty();
				}
			}
		} else {
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			isEmpty = region.isEmpty();
		}
		return isEmpty;
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (gridRouter != null) {

			String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), key);
			if (gridId == null) {
				throwGridNotAvailableException(key);
			}
			Region<K, V> region = gridService.getRegion(gridId, gridPath);
			// region can be null if the router is partitioned and the grid ID
			// is not
			// in the allowed list.
			if (region == null) {
				throwGridAccessNotAllowedException(gridId);
			}
			return region.containsKey(key);

		} else {

			String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
			if (gridIds == null || gridIds.length == 0) {
				throw new GridNotAvailableException("None of the grids is available for this operation");
			}
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			return region.containsKey(key);

		}
	}

	@Override
	public boolean containsValue(Object value)
	{
		String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length == 0) {
			throw new GridNotAvailableException("None of the grids is available for this operation");
		}
		boolean containsValue = false;
		if (gridRouter != null) {
			if (gridRouter.getType() == Type.PARTITIONED) {
				for (int i = 0; i < gridIds.length; i++) {
					Region<K, V> region = gridService.getRegion(gridIds[i], gridPath);
					containsValue = region.containsValue(value);
					if (containsValue) {
						break;
					}
				}
			} else {
				String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), null);
				if (gridId == null) {
					throw new GridNotAvailableException("None of the grids is available for this operation");
				} else {
					Region<K, V> region = gridService.getRegion(gridId, gridPath);
					// region can be null if the router is partitioned and the
					// grid ID is not
					// in the allowed list.
					if (region == null) {
						throwGridAccessNotAllowedException(gridId);
					}
					containsValue = region.containsValue(value);
				}
			}
		} else {
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			containsValue = region.containsValue(value);
		}
		return containsValue;
	}

	@Override
	public V remove(Object key)
	{
		if (gridRouter != null) {

			String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), key);
			if (gridId == null) {
				throwGridNotAvailableException(key);
			}
			Region<K, V> region = gridService.getRegion(gridId, gridPath);
			// region can be null if the router is partitioned and the grid ID
			// is not
			// in the allowed list.
			if (region == null) {
				throwGridAccessNotAllowedException(gridId);
			}
			return region.remove(key);

		} else {

			String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
			if (gridIds == null || gridIds.length == 0) {
				throw new GridNotAvailableException("None of the grids is available for this operation");
			}
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			return region.remove(key);

		}
	}

	@Override
	public void removeAll(Collection<? extends K> keyCollection) throws GridNotAvailableException
	{
		if (gridRouter != null) {

			// first, create key sets per grid in keyMap
			// <gridId, Set<K>>
			Map<String, Set<K>> keyMap = new HashMap(regionMap.size() + 1);
			for (K key : keyCollection) {
				String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), key);
				if (gridId == null) {
					throwGridNotAvailableException(key);
				}
				Set<K> partitionedSet = keyMap.get(gridId);
				if (partitionedSet == null) {
					partitionedSet = new HashSet();
					keyMap.put(gridId, partitionedSet);
				}
				partitionedSet.add(key);
			}

			// <gridId, Set<K>>
			Set<Map.Entry<String, Set<K>>> keySet = keyMap.entrySet();

			// make sure the grids are accessible
			for (Map.Entry<String, Set<K>> entry : keySet) {
				Region<K, V> region = gridService.getRegion(entry.getKey(), gridPath);
				// region can be null if the router is partitioned and the grid
				// ID is not
				// in the allowed list.
				if (region == null) {
					throwGridAccessNotAllowedException(entry.getKey());
				}
			}

			// next, invoke removeAll() per grid
			for (Map.Entry<String, Set<K>> entry : keySet) {
				String gridId = entry.getKey();
				biz.getBizContext().getGridContextClient().setGridIds(gridId);
				biz.removeAll(entry.getValue());
			}

		} else {

			String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
			if (gridIds == null || gridIds.length == 0) {
				throw new GridNotAvailableException("None of the grids is available for this operation");
			}

			biz.removeAll(keyCollection);
		}
	}

	@Override
	public void create(final K key, final V value)
	{
		if (gridRouter != null) {

			String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), key);
			if (gridId == null) {
				throwGridNotAvailableException(key);
			}
			Region<K, V> region = gridService.getRegion(gridId, gridPath);
			// region can be null if the router is partitioned and the grid ID
			// is not
			// in the allowed list.
			if (region == null) {
				throwGridAccessNotAllowedException(gridId);
			}
			try {
				region.create(key, value);
			} catch (com.gemstone.gemfire.cache.EntryExistsException ex) {
				throw new com.netcrest.pado.exception.EntryExistsException(ex);
			}

		} else {

			String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
			if (gridIds == null || gridIds.length == 0) {
				throw new GridNotAvailableException("None of the grids is available for this operation");
			}
			if (gridIds.length == 1) {
				Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
				region.create(key, value);
			} else {
				Future<V>[] futures = new Future[gridIds.length];
				for (int i = 0; i < gridIds.length; i++) {
					final String gridId = gridIds[i];
					futures[i] = executorService.submit(new Callable() {

						@Override
						public V call()
						{
							Region<K, V> region = gridService.getRegion(gridId, gridPath);
							region.create(key, value);
							return value;
						}
					});
				}
				for (Future<V> future : futures) {
					try {
						V val = future.get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						if (e.getCause() instanceof com.gemstone.gemfire.cache.EntryExistsException) {
							throw new com.netcrest.pado.exception.EntryExistsException(e.getCause());
						}
					}
				}
			}

		}
	}

	@Override
	public void invalidate(K key)
	{
		if (gridRouter != null) {

			String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), key);
			if (gridId == null) {
				throwGridNotAvailableException(key);
			}
			Region<K, V> region = gridService.getRegion(gridId, gridPath);
			// region can be null if the router is partitioned and the grid ID
			// is not
			// in the allowed list.
			if (region == null) {
				throwGridAccessNotAllowedException(gridId);
			}
			region.invalidate(key);

		} else {

			String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
			if (gridIds == null || gridIds.length == 0) {
				throw new GridNotAvailableException("None of the grids is available for this operation");
			}
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			region.invalidate(key);

		}
	}

	@Override
	public void clear()
	{
		String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length == 0) {
			throw new GridNotAvailableException("None of the grids is available for this operation");
		}
		if (gridRouter != null) {
			if (gridRouter.getType() == Type.PARTITIONED) {
				for (int i = 0; i < gridIds.length; i++) {
					Region<K, V> region = gridService.getRegion(gridIds[i], gridPath);
					region.clear();
				}
			} else {
				String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), null);
				if (gridId == null) {
					throw new GridNotAvailableException("None of the grids is available for this operation");
				} else {
					Region<K, V> region = gridService.getRegion(gridId, gridPath);
					// region can be null if the router is partitioned and the
					// grid ID is not
					// in the allowed list.
					if (region == null) {
						throwGridAccessNotAllowedException(gridId);
					}
					try {
						region.clear();
					} catch (Exception ex) {
						throw new PadoException(ex);
					}
				}
			}
		} else {
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			region.clear();
		}
	}

	@Override
	public Set<K> keySet()
	{
		String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length == 0) {
			throw new GridNotAvailableException("None of the grids is available for this operation");
		}
		Set<K> keySet = null;
		if (gridRouter != null) {
			if (gridRouter.getType() == Type.PARTITIONED) {
				for (int i = 0; i < gridIds.length; i++) {
					Region<K, V> region = gridService.getRegion(gridIds[i], gridPath);
					Set<K> set = region.keySet();
					if (gridIds.length > 1 && keySet == null) {
						keySet = new HashSet(set.size());
					}
					if (gridIds.length > 1) {
						keySet.addAll(set);
					} else {
						keySet = set;
					}
				}
				if (gridIds.length > 1) {
					keySet = Collections.unmodifiableSet(keySet);
				}
			} else {
				String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), null);
				if (gridId == null) {
					throw new GridNotAvailableException("None of the grids is available for this operation");
				} else {
					Region<K, V> region = gridService.getRegion(gridId, gridPath);
					// region can be null if the router is partitioned and the
					// grid ID is not
					// in the allowed list.
					if (region == null) {
						throwGridAccessNotAllowedException(gridId);
					}
					keySet = region.keySet();
				}
			}
		} else {
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			keySet = region.keySet();
		}
		return keySet;
	}

	@Override
	public Collection<V> values()
	{
		String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length == 0) {
			throw new GridNotAvailableException("None of the grids is available for this operation");
		}
		Collection<V> collection = null;
		if (gridRouter != null) {
			if (gridRouter.getType() == Type.PARTITIONED) {
				for (int i = 0; i < gridIds.length; i++) {
					Region<K, V> region = gridService.getRegion(gridIds[i], gridPath);
					Collection<V> col = region.values();
					if (gridIds.length > 1 && collection == null) {
						collection = new ArrayList(col.size());
					}
					if (gridIds.length > 1) {
						collection.addAll(col);
					} else {
						collection = col;
					}
				}
				if (gridIds.length > 1) {
					collection = Collections.unmodifiableCollection(collection);
				}
			} else {
				String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), null);
				if (gridId == null) {
					throw new GridNotAvailableException("None of the grids is available for this operation");
				} else {
					Region<K, V> region = gridService.getRegion(gridId, gridPath);
					// region can be null if the router is partitioned and the
					// grid ID is not
					// in the allowed list.
					if (region == null) {
						throwGridAccessNotAllowedException(gridId);
					}
					collection = region.values();
				}
			}
		} else {
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			collection = region.values();
		}
		return collection;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length == 0) {
			throw new GridNotAvailableException("None of the grids is available for this operation");
		}
		Set<java.util.Map.Entry<K, V>> entrySet = null;
		if (gridRouter != null) {
			if (gridRouter.getType() == Type.PARTITIONED) {
				for (int i = 0; i < gridIds.length; i++) {
					Region<K, V> region = gridService.getRegion(gridIds[i], gridPath);
					Set<java.util.Map.Entry<K, V>> set = region.entrySet();
					if (gridIds.length > 1 && entrySet == null) {
						entrySet = new HashSet(set.size());
					}
					if (gridIds.length > 1) {
						entrySet.addAll(set);
					} else {
						entrySet = set;
					}
				}
				if (gridIds.length > 1) {
					entrySet = Collections.unmodifiableSet(entrySet);
				}
			} else {
				String gridId = gridRouter.getReachableGridIdForPath(biz.getBizContext(), null);
				if (gridId == null) {
					throw new GridNotAvailableException("None of the grids is available for this operation");
				} else {
					Region<K, V> region = gridService.getRegion(gridId, gridPath);
					// region can be null if the router is partitioned and the
					// grid ID is not
					// in the allowed list.
					if (region == null) {
						throwGridAccessNotAllowedException(gridId);
					}
					entrySet = region.entrySet();
				}
			}
		} else {
			Region<K, V> region = gridService.getRegion(gridIds[0], gridPath);
			entrySet = region.entrySet();
		}
		return entrySet;
	}

	@Override
	public void create(K key, V value, boolean fromRemote)
	{
		if (fromRemote) {
			try {
				biz.create(key, value);
			} catch (com.gemstone.gemfire.cache.EntryExistsException ex) {
				throw new EntryExistsException(ex);
			} catch (PadoServerException ex) {
				if (ex.getCause() instanceof com.gemstone.gemfire.cache.EntryExistsException) {
					throw new EntryExistsException(ex);
				}
			}
		} else {
			create(key, value);
		}
	}

	@Override
	public boolean isEmpty(boolean fromRemote)
	{
		if (fromRemote) {
			return biz.isEmpty();
		} else {
			return isEmpty();
		}
	}

	@Override
	public boolean containsKey(Object key, boolean fromRemote)
	{
		if (fromRemote) {
			return biz.containsKey(key);
		} else {
			return containsKey(key);
		}
	}

	@Override
	public boolean containsValue(Object value, boolean fromRemote)
	{
		if (fromRemote) {
			return biz.containsValue(value);
		} else {
			return containsValue(value);
		}
	}

	@Override
	public int size(boolean fromRemote)
	{
		if (fromRemote) {
			return biz.size();
		} else {
			return size();
		}
	}

	@Override
	public void subscribeKeys(K... keys)
	{
		if (keys == null) {
			return;
		}
		String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length == 0) {
			throw new GridNotAvailableException("None of the grids is available for this operation");
		}
		for (int i = 0; i < gridIds.length; i++) {
			Region<K, V> region = gridService.getRegion(gridIds[i], gridPath);
			for (K key : keys) {
				region.registerInterest(key);
			}
		}
	}

	@Override
	public void subscribeEntries(Object filter)
	{
		if (filter == null) {
			return;
		}
		String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length == 0) {
			throw new GridNotAvailableException("None of the grids is available for this operation");
		}
		for (int i = 0; i < gridIds.length; i++) {
			Region<K, V> region = gridService.getRegion(gridIds[i], gridPath);
			region.registerInterestRegex(filter.toString());
		}
	}

	@Override
	public void addEntryListener(IEntryListener entryListener)
	{
		HashMap<String, GemfireEntryListenerImpl> cacheListenerMap = new HashMap(regionMap.size(), 1f);
		Set<Map.Entry<String, Region>> set = regionMap.entrySet();
		for (Map.Entry<String, Region> entry : set) {
			String gridId = entry.getKey();
			Region region = entry.getValue();
			GemfireEntryListenerImpl cacheListener = new GemfireEntryListenerImpl(entry.getKey(), entryListener);
			region.getAttributesMutator().addCacheListener(cacheListener);
			cacheListenerMap.put(gridId, cacheListener);
		}
		entryListenerMap.put(entryListener, cacheListenerMap);
	}

	@Override
	public void removeEntryListener(IEntryListener entryListener)
	{
		Map<String, GemfireEntryListenerImpl> cacheListenerMap = entryListenerMap.remove(entryListener);
		if (cacheListenerMap == null) {
			return;
		}
		for (Map.Entry<String, GemfireEntryListenerImpl> entry : cacheListenerMap.entrySet()) {
			String gridId = entry.getKey();
			GemfireEntryListenerImpl cacheListener = entry.getValue();
			Region region = regionMap.get(gridId);
			region.getAttributesMutator().removeCacheListener(cacheListener);
		}
	}

	@Override
	public synchronized IBulkLoader<K, V> getBulkLoader(int batchSize)
	{
		if (bulkLoader == null) {
			bulkLoader = new GridMapBulkLoaderImpl<K, V>(this, batchSize);
		}
		return bulkLoader;
	}

	@Override
	public Object getNativeMap()
	{
		String gridIds[] = biz.getBizContext().getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length == 0) {
			throw new GridNotAvailableException("None of the grids is available for this operation");
		}
		return gridService.getRegion(gridIds[0], gridPath);
	}
	
//	@Override
//	public Map.Entry<K, V> getRandomEntry()
//	{
//		return biz.getRandomEntry();
//	}

	public Map.Entry<K, V> getRandomEntry()
	{
		// Use query to get a single entry in the region.
		Map.Entry<K, V> entry = null;
		Region region = (Region) getNativeMap();
		String queryString = "select e.key, e.value from " + region.getFullPath() + ".entrySet e limit 1";
		Pool pool = PoolManager.find(region);
		if (pool != null) {
			QueryService qs = pool.getQueryService();
			Query query = qs.newQuery(queryString);
			try {
				SelectResults<Struct> sr = (SelectResults<Struct>) query.execute();
				if (sr != null) {
					for (Struct struct : sr) {
						entry = new GemfireEntryImpl((K)struct.getFieldValues()[0], (V)struct.getFieldValues()[1]);
						break;
					}
				}
			} catch (Exception ex) {
				throw new PadoException(ex);
			}
		} else {
			throw new PadoException("Pool undefined for the region path: " + region.getFullPath());
		}
		return entry;
	}
}
