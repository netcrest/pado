package com.netcrest.pado.rpc.client.biz;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.mqtt.client.IRpcListener;
import com.netcrest.pado.rpc.mqtt.client.MqttJsonRpcClient;

/**
 * PathBiz provides grid path methods
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PathBiz
{
	private String gridPath;

	/**
	 * Constructs a new instance of PathBiz.
	 * 
	 * @param gridPath
	 *            Grid path (not full path)
	 */
	public PathBiz(String gridPath)
	{
		this.gridPath = gridPath;
	}

	public String getGridPath()
	{
		return this.gridPath;
	}

	/**
	 * Puts the specified (key, value) pair in the grid path.
	 * 
	 * @param key
	 *            Key of string or numeric type.
	 * @param value
	 *            Value of string, numeric or JSON object (JSON array not
	 *            supported)
	 * @return Value put in the grid path
	 */
	public JsonLite put(Object key, Object value)
	{
		if (key == null || value == null) {
			return null;
		}
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("key", key);
		params.put("value", value);
		return MqttJsonRpcClient.getRpcClient().execute(this, "put", params, 0);
	}

	/**
	 * Puts the entries on the specified map the grid path.
	 * 
	 * @param entryMap
	 *            (key, value) entries.
	 */
	public void putAll(JsonLite entryMap)
	{
		if (entryMap == null) {
			return;
		}
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("entryMap", entryMap);
		MqttJsonRpcClient.getRpcClient().execute(this, "putAll", params, 0);
	}

	/**
	 * Removes the specified key from the grid path.
	 * 
	 * @param key
	 *            Key to remove. Must be of string or numeric type.
	 * @return Removed value.
	 */
	public Object remove(Object key)
	{
		if (key == null) {
			return null;
		}
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("key", key);
		return MqttJsonRpcClient.getRpcClient().execute(this, "remove", params, 0);
	}

	/**
	 * Gets the values of the specified keys from the grid path.
	 * 
	 * @param key
	 *            Key of string or numeric type.
	 * @return Key mapped value in the grid path.
	 */
	public Object get(Object key)
	{
		if (key == null) {
			return null;
		}
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("key", key);
		return MqttJsonRpcClient.getRpcClient().execute(this, "get", params, 0);
	}

	/**
	 * Gets the values of the specified keys from the grid path.
	 * 
	 * @param keyArray
	 *            Keys
	 * @return JSON object containing (key, value) pairs.
	 */
	public JsonLite getAll(Object... keyArray)
	{
		if (keyArray == null) {
			return null;
		}
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("keyArray", keyArray);
		return MqttJsonRpcClient.getRpcClient().execute(this, "getAll", params, 0);
	}

	/**
	 * Execute the specified query predicate on the grid path.
	 * 
	 * @param queryPredicate
	 *            Query predicate is a where clause with out the select
	 *            projection.
	 * @return Query results in JSON RPC reply form
	 */
	public JsonLite query(String queryPredicate)
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("queryPredicate", queryPredicate);
		return MqttJsonRpcClient.getRpcClient().execute(this, "query", params, 0);
	}

	/**
	 * Dump the grid path contents in the default data node dump directory.
	 * 
	 * @return Execution results in JSON.
	 */
	public JsonLite dump()
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		return MqttJsonRpcClient.getRpcClient().execute(this, "dumpGridPath", params, 0);
	}

	// Not implemented yet in the server
	public JsonLite dump(String queryPredicate, String filePath)
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("queryPredicate", queryPredicate);
		params.put("filePath", filePath);
		return MqttJsonRpcClient.getRpcClient().execute(this, "dump", params, 0);
	}

	/**
	 * Gets the size of the grid path in the data node.
	 * 
	 * @return Size in JSON RPC reply form.
	 */
	public JsonLite size()
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		return MqttJsonRpcClient.getRpcClient().execute(this, "size", params, 0);
	}

	/**
	 * Adds a listener to listen on data changes made in the grid path
	 * 
	 * @param listenerName
	 *            Unique name of the listener.
	 * @param listener
	 *            Listener function or class method with one parameter for
	 *            receiving JSON messages.
	 * @return Addition results in JSON RPC reply form.
	 */
	public JsonLite addListener(String listenerName, IRpcListener listener)
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("name", listenerName);

		MqttJsonRpcClient.getRpcClient().addRpcListener(listenerName, listener);
		return MqttJsonRpcClient.getRpcClient().execute(this, "addListener", params, 0);
	}

	/**
	 * Remove the specified listener
	 * 
	 * @param listenerName
	 *            Unique name of the listener.
	 * @param listener
	 *            Previously added Listener function or class method.
	 * @return Removal results in JSON RPC reply form.
	 */
	public JsonLite removeListener(String listenerName, IRpcListener listener)
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("name", listenerName);

		MqttJsonRpcClient.getRpcClient().removeRpcListener(listenerName, listener);
		return MqttJsonRpcClient.getRpcClient().execute(this, "removeListener", params, 0);
	}
}
