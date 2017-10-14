package com.netcrest.pado.test.rpc.client;

import java.util.List;

import org.junit.Test;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.client.biz.PathBiz;
import com.netcrest.pado.rpc.client.dna.RpcInvokerDna;
import com.netcrest.pado.rpc.mqtt.RequestKey;

/**
 * PathBiz1Put tests the RPC {@link PathBiz} methods via {@link RpcInvokerDna}. This
 * test case requires the "nw" data distributed with Pado. Make sure to first
 * load that set of data in the "mygrid" environment.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PathBiz1Put extends AbstractTest
{
	private final String gridPath = "test1";
	
	public PathBiz1Put(String lang)
	{
		super(lang);
	}
	
	public void testPut()
	{
		System.out.println("PathBiz1PutTest.testPut()");
		System.out.println("-------------------------");
		JsonLite request = createBizRequest(PathBiz.class.getName(), "put");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite value1 = new JsonLite();
		value1.put("Name", "Joe Smith");
		value1.put("Address", "100 Somewhere Street");
		JsonLite rpcParams = createRpcParams(gridPath, "key1", value1);
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		JsonLite reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	@Test
	public void testPutAll()
	{
		System.out.println("PathBiz1PutTest.testPutAll()");
		System.out.println("----------------------------");
		JsonLite request = createBizRequest(PathBiz.class.getName(), "putAll");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite value2 = new JsonLite();
		value2.put("Name", "Foo Yong");
		value2.put("Address", "1 Farway Street");
		JsonLite value3 = new JsonLite();
		value3.put("Name", "Jane Doe");
		value3.put("Address", "1 Main Street");
		JsonLite entryMap = new JsonLite();
		entryMap.put("key2", value2);
		entryMap.put("key3", value3);
		JsonLite rpcParams = createRpcParams(gridPath, entryMap);
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		JsonLite reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	private JsonLite createRpcParams(String gridPath, String key, JsonLite value)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("key", key);
		rpcParams.put("value", value);
		return rpcParams;
	}

	private JsonLite createRpcParams(String gridPath, JsonLite entryMap)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("entryMap", entryMap);
		return rpcParams;
	}
}
