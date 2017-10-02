package com.netcrest.pado.rpc.client.biz.impl.gemfire;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.netcrest.pado.biz.IPqlBiz;
import com.netcrest.pado.biz.IUtilBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.rpc.BizProxy;
import com.netcrest.pado.rpc.IRpc;
import com.netcrest.pado.rpc.mqtt.MqttJsonRpcListenerImpl;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.util.GridUtil;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PathBizImpl implements IRpc
{
	private static HashMap<String, CacheListener> cacheListenerMap = new HashMap<String, CacheListener>();

	public JsonLite put(JsonLite params) throws Exception
	{
		String gridPath = params.getString("gridPath", null);
		String key = params.getString("key", null);
		JsonLite value = (JsonLite)params.get("value", null);

		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		if (key == null) {
			throw new InvalidParameterException("key undefined");
		}
		if (value == null) {
			throw new InvalidParameterException("value undefined");
		}

		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			throw new InvalidParameterException("Invalid grid path: " + gridPath + " [fullPath=" + fullPath);
		}
		region.put(key, value);
		return value;
	}
	
	public void putAll(JsonLite params) throws Exception
	{
		String gridPath = params.getString("gridPath", null);
		JsonLite entryMap = (JsonLite)params.get("entryMap", null);

		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		if (entryMap == null) {
			throw new InvalidParameterException("entryMap undefined");
		}
	
		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			throw new InvalidParameterException("Invalid grid path: " + gridPath + " [fullPath=" + fullPath);
		}
		region.putAll(entryMap);
	}
	
	public JsonLite remove(JsonLite params) throws Exception
	{
		String gridPath = params.getString("gridPath", null);
		String key = params.getString("key", null);

		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		if (key == null) {
			throw new InvalidParameterException("key undefined");
		}

		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			throw new InvalidParameterException("Invalid grid path: " + gridPath + " [fullPath=" + fullPath);
		}
		return (JsonLite)region.remove(key);
	}
	
	public JsonLite get(JsonLite params) throws Exception
	{
		String gridPath = params.getString("gridPath", null);
		String key = params.getString("key", null);

		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		if (key == null) {
			throw new InvalidParameterException("key undefined");
		}

		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			throw new InvalidParameterException("Invalid grid path: " + gridPath + " [fullPath=" + fullPath);
		}
		return (JsonLite)region.get(key);
	}
	
	public JsonLite getAll(JsonLite params) throws Exception
	{
		String gridPath = params.getString("gridPath", null);
		Object[] keyArray = (Object[])params.getArray("keyArray");

		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		if (keyArray == null || keyArray.length == 0) {
			throw new InvalidParameterException("keyArray undefined");
		}

		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			throw new InvalidParameterException("Invalid grid path: " + gridPath + " [fullPath=" + fullPath);
		}
		Map<String, JsonLite> map = region.getAll(Arrays.asList(keyArray));
		return new JsonLite<>(map);
	}
	
	public List<JsonLite> query(JsonLite params) throws Exception
	{
		String gridPath = params.getString("gridPath", null);
		String queryPredicate = params.getString("queryPredicate", null);

		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		if (queryPredicate == null) {
			throw new InvalidParameterException("queryPredicate undefined");
		}

		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			throw new InvalidParameterException("Invalid grid path: " + gridPath + " [fullPath=" + fullPath);
		}
		SelectResults<JsonLite> sr = region.query(queryPredicate);
		List<JsonLite> list = sr.asList();
		return list;
	}
	
	public List<JsonLite> executePql(JsonLite params) throws Exception
	{
		String pql = params.getString("pql", null);
		IPqlBiz pqlBiz = BizProxy.newInstance(IPqlBiz.class);
		return (List<JsonLite>)pqlBiz.executePql(pql);
	}

	public String dumpGridPath(JsonLite params)
	{
		String gridPath = params.getString("gridPath", null);

		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}

		IUtilBiz utilBiz = BizProxy.newInstance(IUtilBiz.class);
		List<String> list = utilBiz.dumpServers(gridPath);
		if (list == null || list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}

	public int size(JsonLite params)
	{
		String gridPath = params.getString("gridPath", null);
		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}

		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			return 0;
		} else {
			return region.size();
		}
	}

	/**
	 * Adds a cache listener for the "name" parameter found in the specified
	 * params argument.
	 * 
	 * @param params
	 *            Parameters
	 */
	public void addListener(JsonLite params)
	{
		String gridPath = params.getString("gridPath", null);
		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			throw new InvalidParameterException("Invalid grid path: " + gridPath + " [fullPath=" + fullPath);
		}
		String name = params.getString("name", null);
		if (name == null) {
			throw new InvalidParameterException("name undefined");
		}
		CacheListener cacheListener = cacheListenerMap.get(name);
		if (cacheListener == null) {
			cacheListener = new CacheListenerImpl(name);
			region.getAttributesMutator().addCacheListener(cacheListener);
			cacheListenerMap.put(name, cacheListener);
		}
	}

	/**
	 * Removes the cache listener mapped by the "name" parameter found in the
	 * specified params argument.
	 * 
	 * @param params
	 *            Parameters
	 */
	public void removeListener(JsonLite params)
	{
		String gridPath = params.getString("gridPath", null);
		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			throw new InvalidParameterException("Invalid grid path: " + gridPath + " [fullPath=" + fullPath);
		}
		String name = params.getString("name", null);
		if (name == null) {
			throw new InvalidParameterException("name undefined");
		}
		CacheListener cacheListener = cacheListenerMap.remove(name);
		if (cacheListener != null) {
			region.getAttributesMutator().removeCacheListener(cacheListener);
		}
	}

	private class CacheListenerImpl extends CacheListenerAdapter
	{
		String name;
		String topic;

		CacheListenerImpl(String name)
		{
			this.name = name;
			this.topic = "/__pado/listener/" + name;
		}

		@Override
		public void afterCreate(EntryEvent event)
		{
			publish(event, "create");
		}

		@Override
		public void afterUpdate(EntryEvent event)
		{
			publish(event, "update");
		}

		@Override
		public void afterDestroy(EntryEvent event)
		{
			publish(event, "destroy");
		}

		private void publish(EntryEvent event, String type)
		{
			Object key = event.getKey();
			Object value = event.getNewValue();
			JsonLite jl = new JsonLite();
			jl.put("key", key);
			if (value instanceof ITemporalData) {
				ITemporalData td = (ITemporalData) value;
				value = td.getValue();
			}
			jl.put("value", value);
			jl.put("type", type);
			try {
				MqttJsonRpcListenerImpl.getMqttJsonRpcListenerImpl().publish(topic, jl);
			} catch (MqttException e) {
				Logger.error(e.getMessage());
			}
		}
	}
}
