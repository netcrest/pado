package com.netcrest.pado.test.rpc.client;

import java.util.List;

import org.junit.Test;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.client.biz.PathBiz;
import com.netcrest.pado.rpc.client.dna.RpcInvoker;
import com.netcrest.pado.rpc.mqtt.RequestKey;

/**
 * PathBiz2Get tests the RPC {@link PathBiz} "get", "query", "size", and "dump"
 * methods via {@link RpcInvoker}. This test case requires the "nw" data
 * distributed with Pado. Make sure to first load that set of data in the
 * "mygrid" environment.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PathBiz2Get extends AbstractTest
{
	private final String gridPath = "test1";
	private final String gridPath2 = "nw/orders";
		
	public PathBiz2Get(String lang)
	{
		super(lang);
	}

	@Test
	public void testGet()
	{
		System.out.println("PathBiz2GetTest.testGet()");
		System.out.println("-------------------------");
		JsonLite request = createBizRequest(PathBiz.class.getName(), "get");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams(gridPath, "key1");
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	@Test
	public void testGetAll()
	{
		System.out.println("PathBiz2GetTest.testGetAll()");
		System.out.println("----------------------------");
		JsonLite request = createBizRequest(PathBiz.class.getName(), "getAll");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParamsKeys(gridPath, "key1", "key2", "key3");
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	@Test
	public void testQuery()
	{
		System.out.println("PathBiz2GetTest.testQuery()");
		System.out.println("---------------------------");
		JsonLite request = createBizRequest(PathBiz.class.getName(), "query");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParamsQuery(gridPath2, "value['CustomerId']='VICTE'");
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	@Test
	public void testDump()
	{
		System.out.println("PathBiz2GetTest.testDump()");
		System.out.println("--------------------------");
		JsonLite request = createBizRequest(PathBiz.class.getName(), "dumpGridPath");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams(gridPath2);
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	@Test
	public void testSize()
	{
		System.out.println("PathBiz2GetTest.testGetAll()");
		System.out.println("----------------------------");
		JsonLite request = createBizRequest(PathBiz.class.getName(), "size");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams(gridPath2);
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	private JsonLite createRpcParams(String gridPath)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		return rpcParams;
	}

	private JsonLite createRpcParams(String gridPath, String key)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("key", key);
		return rpcParams;
	}

	private JsonLite createRpcParamsKeys(String gridPath, String... keyArray)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("keyArray", keyArray);
		return rpcParams;
	}

	private JsonLite createRpcParamsQuery(String gridPath, String queryPredicate)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("queryPredicate", queryPredicate);
		return rpcParams;
	}
}
