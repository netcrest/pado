package com.netcrest.pado.test.rpc.client;

import java.util.List;

import org.junit.Test;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.client.biz.TemporalBiz;
import com.netcrest.pado.rpc.client.dna.RpcInvokerDna;
import com.netcrest.pado.rpc.mqtt.RequestKey;

/**
 * TemporalBizDump tests the RPC {@link TemporalBiz} query methods via
 * {@link RpcInvokerDna}. This test case requires the "nw" data distributed with
 * Pado. Make sure to first load that set of data in the "mygrid" environment.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalBizTemporalList extends AbstractTest
{
	private final String gridPath = "nw/orders";

	public TemporalBizTemporalList(String lang)
	{
		super(lang);
	}
	
	@Test
	public void testGetTemporalLists()
	{
		System.out.println("TemporalBizDump.testGetTemporalLists()");
		System.out.println("--------------------------------------");
		JsonLite request = createBizRequest(TemporalBiz.class.getName(), "getTemporalList");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParamsIdentityKey(gridPath, null, "10251");
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		JsonLite reply = rpcBiz.broadcast(request);
		printReply(reply);
	}
	
	@Test
	public void testDumpTemporalLists()
	{
		System.out.println("TemporalBizDump.testDumpTemporalLists()");
		System.out.println("--------------------------------------");
		JsonLite request = createBizRequest(TemporalBiz.class.getName(), "dumpTemporalLists");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams(gridPath, null, "10251", "11070");
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		JsonLite reply = rpcBiz.broadcast(request);
		printReply(reply);
	}
	
	public void testDumpAllTemporalLists()
	{
		System.out.println("TemporalBizDump.testDumpAllTemporalLists()");
		System.out.println("------------------------------------------");
		JsonLite request = createBizRequest(TemporalBiz.class.getName(), "dumpAllTemporalLists");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams(gridPath, null);
		rpcParams.remove("identityKeys");
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		JsonLite reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	private JsonLite createRpcParamsIdentityKey(String gridPath, String dumpDir, String identityKey)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("dumpDir", dumpDir);
		rpcParams.put("identityKey", identityKey);
		return rpcParams;
	}
	
	private JsonLite createRpcParams(String gridPath, String dumpDir, String...identityKeys)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("dumpDir", dumpDir);
		rpcParams.put("identityKeys", identityKeys);
		return rpcParams;
	}
}
