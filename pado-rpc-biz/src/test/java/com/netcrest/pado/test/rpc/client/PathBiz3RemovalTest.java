package com.netcrest.pado.test.rpc.client;

import java.util.List;

import org.junit.Test;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.client.biz.PathBiz;
import com.netcrest.pado.rpc.client.dna.RpcInvoker;
import com.netcrest.pado.rpc.mqtt.RequestKey;

/**
 * PathBiz3RemovalTest tests the RPC {@link PathBiz} "remove" methods via
 * {@link RpcInvoker}. You must run {@link PathBiz1PutTest} first to insert data
 * into the grid.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PathBiz3RemovalTest extends AbstractTest
{
	private final String gridPath = "test1";

	/**
	 * Tests removal of all keys inserted by the test case
	 * {@link PathBiz1PutTest}.
	 */
	@Test
	public void testRemove()
	{
		System.out.println("PathBiz3RemovalTest.testRemove()");
		System.out.println("--------------------------------");
		JsonLite request = createBizRequest("java", PathBiz.class.getName(), "remove");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());

		for (int i = 1; i <= 3; i++) {
			String key = "key" + i;
			JsonLite rpcParams = createRpcParams(gridPath, key);
			params.put(RequestKey.params.name(), rpcParams);
			printRequest(request);
			List<JsonLite> reply = rpcBiz.broadcast(request);
			System.out.println("Reply:");
			printReply(reply);
		}
	}

	private JsonLite createRpcParams(String gridPath, String key)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("key", key);
		return rpcParams;
	}
}
