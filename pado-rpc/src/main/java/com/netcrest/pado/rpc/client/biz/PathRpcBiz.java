package com.netcrest.pado.rpc.client.biz;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.mqtt.client.IRpcListener;
import com.netcrest.pado.rpc.mqtt.client.MqttJsonRpcClient;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PathRpcBiz
{
	private String gridPath;

	public PathRpcBiz(String gridPath)
	{
		this.gridPath = gridPath;
	}
	
	public String getGridPath()
	{
		return this.gridPath;
	}

	public JsonLite query(String queryPredicate)
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("queryPredicate", queryPredicate);
		return MqttJsonRpcClient.getRpcClient().execute(this, "query", params, 0);
	}
	
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
	
	public JsonLite size()
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		return MqttJsonRpcClient.getRpcClient().execute(this, "size", params, 0);
	}

	public JsonLite addListener(String listenerName, IRpcListener listener)
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("name", listenerName);
		
		MqttJsonRpcClient.getRpcClient().addRpcListener(listenerName, listener);
		return MqttJsonRpcClient.getRpcClient().execute(this, "addListener", params, 0);
	}
	
	public JsonLite removeListener(String listenerName, IRpcListener listener)
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("name", listenerName);

		MqttJsonRpcClient.getRpcClient().removeRpcListener(listenerName, listener);
		return MqttJsonRpcClient.getRpcClient().execute(this, "removeListener", params, 0);
	}	
}
