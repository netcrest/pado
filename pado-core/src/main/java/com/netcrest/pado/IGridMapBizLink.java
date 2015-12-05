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
package com.netcrest.pado;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.exception.EntryExistsException;
import com.netcrest.pado.exception.GridNotAvailableException;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.util.IBulkLoader;

/**
 * IGridMapBizLink is the permanent link class that bridges the hot-deployed
 * IGridMap class with a detached class loader such as the system class loader
 * that the underlying data grid product may rely on. Such data grid products
 * must use IGridMapBizLink to access IGridMapBiz operations.
 * 
 * @author dpark
 * 
 * @param <K>
 *            Key type
 * @param <V>
 *            Value type
 */
public interface IGridMapBizLink<K, V> extends IBiz, Map<K, V>
{
	/**
	 * Sets the grid path. This method must be invoked once immediately after
	 * obtaining an IGridMap instance from {@link ICatalog}.
	 * 
	 * @param gridPath
	 *            grid path
	 */
	public void setGridPath(String gridPath);

	/**
	 * Returns the target path.
	 */
	public String getGridPath();

	/**
	 * Returns the number of entries in the local cache. This method call is
	 * analogous to invoking <code>size(false)</code>.
	 */
	public int size();

	/**
	 * Returns the number of entries in the local cache or the remote cache
	 * (grid). <code>size(false)</code> is analogous to invoking {@link #size()}
	 * .
	 * 
	 * @param fromRemote
	 *            true to return the remote cache size, false to return the
	 *            local cache size.
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public int size(boolean fromRemote) throws GridNotAvailableException;

	/**
	 * Returns true if the local cache is empty. This method call is analogous
	 * to invoking <code>isEmpty(false)</code>
	 */
	public boolean isEmpty();

	/**
	 * Returns true if the local cache or the remote cache (grid) is empty.
	 * <code>isEmpty(false)</code> is analogous to invoking {@link #isEmpty()}
	 * 
	 * @param fromRemote
	 *            true to check the remote cache (grid), false to check the
	 *            local cache
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public boolean isEmpty(boolean fromRemote) throws GridNotAvailableException;

	/**
	 * Returns true if the specified key exists in the local cache. This method
	 * call is analogous to invoking <code>containsKey(false)</code>.
	 * 
	 * @param key
	 *            Key object
	 */
	public boolean containsKey(Object key);

	/**
	 * Returns true if the specified key exists in the local cache or the remote
	 * cache (grid). <code>containsKey(key, false)</code> is analogous to
	 * invoking {@link #containsKey(Object)}
	 * 
	 * @param key
	 *            Key object
	 * @param fromRemote
	 *            true to check the remote cache (grid), false to check the
	 *            local cache
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public boolean containsKey(Object key, boolean fromRemote) throws GridNotAvailableException;

	/**
	 * Returns true if the specified value exists in the local cache. This
	 * method call is analogous to invoking <code>containsValue(false)</code>.
	 * 
	 * @param value
	 *            Value object
	 */
	public boolean containsValue(Object value);

	/**
	 * Returns true if the specified value exists in the local cache or the
	 * remote cache (grid). <code>containsValue(key, false)</code> is analogous
	 * to invoking {@link #containsValue(Object)}
	 * 
	 * 
	 * @param value
	 *            Value object
	 * @param fromRemote
	 *            true to check the remote cache (grid), false to check the
	 *            local cache
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public boolean containsValue(Object value, boolean fromRemote) throws GridNotAvailableException;

	/**
	 * Returns the value mapped by the specified key. Returns null if not found.
	 * 
	 * @param key
	 *            Key that maps value.
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public V get(Object key) throws GridNotAvailableException;

	/**
	 * Returns a map of entries that match the specified keys.
	 * 
	 * @param keyCollection
	 *            Collection of keys
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public Map<K, V> getAll(Collection<? extends K> keyCollection) throws GridNotAvailableException;

	/**
	 * Puts the specified key/value pair into the map in the local cache which
	 * in turn puts it in the remote cache. It overwrites the existing pair.
	 * 
	 * @param key
	 *            Key that maps value
	 * @param value
	 *            Value mapped by key
	 * @return The previous value associated with key. In some cases null may be
	 *         returned even if a previous value exists. The behavior depends on
	 *         the underlying data grid product.
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public V put(K key, V value) throws GridNotAvailableException;

	/**
	 * Puts the specified key/value pair into the map in the local cache or
	 * remote cache. It overwrites the existing pair.
	 * 
	 * @param key
	 *            Key object
	 * @param value
	 *            Value object
	 * @param fromRemote
	 *            true to put the pair from the remote cache, false to create
	 *            the pair from the local cache. In either case, the pair is put
	 *            in the remote cache. If true, the pair may be pushed to the
	 *            local cache if it is subscribed.
	 * @return Returns the old value from the local cache if it exists, null
	 *         otherwise.
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public V put(K key, V value, boolean fromRemote) throws GridNotAvailableException;

	/**
	 * Removes the specified key from the remote cache and returns the removed
	 * value from the local cache.
	 * 
	 * @param key
	 *            Key object
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public V remove(Object key) throws GridNotAvailableException;

	/**
	 * Removes all of the specified keys from the remote cache.
	 * 
	 * @param keyCollection
	 *            Collection of keys
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public void removeAll(Collection<? extends K> keyCollection) throws GridNotAvailableException;

	/**
	 * Creates the specified key/value pair in the map. It throws
	 * EntryExistsException if the pair exists in the local cache. To overwrite
	 * the existing entry, use put() instead. Note that this method is not part
	 * of {@link java.util.Map}.
	 * 
	 * @param key
	 *            Key object
	 * @param value
	 *            Value object
	 * @throws EntryExistsException
	 *             Thrown if the value exists
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public void create(K key, V value) throws EntryExistsException, GridNotAvailableException;

	/**
	 * Creates the specified key/value pair in the map. It throws
	 * EntryExistsException if the pair exists in the local cache or the remote
	 * cache.
	 * 
	 * @param key
	 *            Key object
	 * @param value
	 *            Value object
	 * @param fromRemote
	 *            true to create the pair from the remote cache, false to create
	 *            the pair from the local cache. In either case, the pair is
	 *            created in the remote cache if it doesn't exist. If true, the
	 *            pair may be pushed to the local cache if it is subscribed.
	 * @throws EntryExistsException
	 *             Thrown if fromRemote is true and the value exists in the
	 *             remote cache. Thrown if fromRemote is false and the value
	 *             exists in the local cache.
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public void create(K key, V value, boolean fromRemote) throws EntryExistsException, GridNotAvailableException;

	/**
	 * Invalidates the specified key.
	 * 
	 * @param key
	 *            Key object
	 */
	public void invalidate(K key);

	/**
	 * Puts all of the entries in the specified map into the map.
	 * 
	 * @param map
	 *            Map of key/value pairs
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public void putAll(Map<? extends K, ? extends V> map) throws GridNotAvailableException;

	/**
	 * Clears the map. The behavior of this method is defined by the underlying
	 * data grid product. For example, GemFire clears only local cache.
	 * 
	 * @throws PadoException
	 *             Thrown if the underlying data grid does not support the clear
	 *             method.
	 */
	public void clear();

	/**
	 * Returns the key set.
	 */
	public Set<K> keySet();

	/**
	 * Returns all of the values in the local cache.
	 */
	public Collection<V> values();

	/**
	 * Returns all of the entries in the local cache.
	 */
	public Set<Map.Entry<K, V>> entrySet();

	/**
	 * Subscribes the specified key(s) to listen on entry events. To listen on
	 * the events, the caller must register an IEntryListener by invoking
	 * {@link #addEntryListener(IEntryListener)}.
	 * 
	 * @param keys
	 *            One or more keys to subscribe
	 * @see EntryEvent
	 */
	public void subscribeKeys(K... keys);

	/**
	 * Subscribes the specified regular expression to listen on entry events.
	 * Support for filter differs from data grid to data grid and therefore this
	 * method depends on the underlying data grid subscription model. For
	 * example, filter is key regular expression for GemFire.
	 * 
	 * @param filter
	 *            Filter for subscribing data
	 * @throws GridNotAvailableException
	 *             Thrown if the target grid cannot be reached. If partitioned
	 *             path then this exception thrown if the target grid is one of
	 *             the configured grids and it is unreachable. If
	 *             non-partitioned path then this exception may be thrown if
	 *             none of the grids is reachable.
	 */
	public void subscribeEntries(Object filter) throws GridNotAvailableException;

	/**
	 * Adds an entry listener. The caller must subscribe one or more keys to
	 * receive entry events.
	 * 
	 * @param entryListener
	 *            Entry listener to add
	 * @see EntryEvent
	 */
	public void addEntryListener(IEntryListener entryListener);

	/**
	 * Removes the specified entry listener.
	 * 
	 * @param entryListener
	 *            Entry listener to remove
	 */
	public void removeEntryListener(IEntryListener entryListener);

	/**
	 * Returns the bulk loader for this grid map. The returned bulk loader uses
	 * this grid map such that changing the grid path also affects the grid map.
	 * 
	 * @param batchSize
	 *            The batch size of the bulk loader. A bulk-put is done when it
	 *            reaches the batch size.
	 */
	public IBulkLoader<K, V> getBulkLoader(int batchSize);

	/**
	 * Returns the underlying data grid's native map object if supported. The
	 * returned map belongs to the first grid if there are more than one grids
	 * that the grid path spans.
	 */
	public Object getNativeMap();

	/**
	 * Returns a single random entry from the map. This method is useful for
	 * determining the key and value types.
	 * 
	 * @return null if this map is empty.
	 */
	public Map.Entry<K, V> getRandomEntry();
}
