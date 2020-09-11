package com.netcrest.pado.tools.hazelcast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hazelcast.map.IMap;
import com.netcrest.pado.exception.PathUndefinedException;
import com.netcrest.pado.util.IBulkLoader;
import com.netcrest.pado.util.IBulkLoaderListener;

public class HazelcastBulkLoader <K, V> implements IBulkLoader<K, V>
{
	protected IMap<K, V> hzMap;
	protected String gridPath;
	protected int batchSize = 1000;
	protected HashMap<K, V> map = new HashMap<K, V>(batchSize, 1f);
	protected Set<IBulkLoaderListener> bulkLoaderListenerSet = new HashSet<IBulkLoaderListener>(3);

	
	public HazelcastBulkLoader(IMap<K, V> hzMap)
	{
		this.hzMap = hzMap;
	}
	
	@Override
	public void setPath(String gridPath) throws PathUndefinedException {
		// Not supported
	}

	@Override
	public String getPath() {
		if (hzMap == null) {
			return null;
		}
		return hzMap.getName();
	}

	@Override
	public void setBatchSize(int batchSize) {
		if (this.batchSize < batchSize) {
			this.map = new HashMap<K, V>(batchSize, 1f);
		}
		this.batchSize = batchSize;
	}

	@Override
	public int getBatchSize() {
		return batchSize;
	}

	@Override
	public void put(K key, V value) throws PathUndefinedException {
		map.put(key, value);
	}

	@Override
	public void remove(K key) throws PathUndefinedException {
		map.remove(key);
		if (hzMap != null) {
			hzMap.remove(key);
		}
	}

	@Override
	public void flush() {
		int count = map.size();
		if (count > 0 && hzMap != null) {
			hzMap.putAll(map);
			map.clear();
			synchronized (bulkLoaderListenerSet) {
				Iterator<IBulkLoaderListener> iterator = bulkLoaderListenerSet.iterator();
				while (iterator.hasNext()) {
					IBulkLoaderListener listener = iterator.next();
					listener.flushed(count);
				}
			}
		}
	}

	@Override
	public void addBulkLoaderListener(IBulkLoaderListener listener) {
		synchronized (bulkLoaderListenerSet) {
			bulkLoaderListenerSet.add(listener);
		}
	}

	@Override
	public void removeBulkLoaderListener(IBulkLoaderListener listener) {
		synchronized (bulkLoaderListenerSet) {
			bulkLoaderListenerSet.remove(listener);
		}
	}

}