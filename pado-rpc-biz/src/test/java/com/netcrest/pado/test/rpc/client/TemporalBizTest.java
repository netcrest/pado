package com.netcrest.pado.test.rpc.client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.Test;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.client.biz.TemporalBiz;
import com.netcrest.pado.rpc.mqtt.RequestKey;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalBizTest extends AbstractTest
{
	@Test
	public void testGetAllEntries()
	{
		JsonLite request = createBizRequest("java", TemporalBiz.class.getName(), "getAllEntries");
		JsonLite params = (JsonLite)request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams("jde/sales", "02.115.251-US.sjde_US_99999", -1, -1);
		params.put(RequestKey.params.name(), rpcParams);
		
		printRequest(request);
		
		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}
	
	@Test
	public void testGetEntry()
	{
		JsonLite request = createBizRequest("java", TemporalBiz.class.getName(), "getEntry");
		JsonLite params = (JsonLite)request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams("jde/sales", "02.115.251-US.sjde_US_99999", -1, -1);
		params.put(RequestKey.params.name(), rpcParams);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}
	
	@Test
	public void testGet()
	{
		JsonLite request = createBizRequest("java", TemporalBiz.class.getName(), "get");
		JsonLite params = (JsonLite)request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams("jde/sales", "02.115.251-US.sjde_US_99999", -1, -1);
		params.put(RequestKey.params.name(), rpcParams);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}
	
	@Test
	public void testSize()
	{
		JsonLite request = createBizRequest("java", TemporalBiz.class.getName(), "size");
		JsonLite params = (JsonLite)request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams("jde/sales", -1, -1);
		params.put(RequestKey.params.name(), rpcParams);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}
	
	@Test
	public void testGetTemporalListCount()
	{
		JsonLite request = createBizRequest("java", TemporalBiz.class.getName(), "getTemporalListCount");
		JsonLite params = (JsonLite)request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams("jde/sales");
		params.put(RequestKey.params.name(), rpcParams);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}
	
	@Test
	public void testGetEntryHistoryWrittenTimeRangeList() throws ParseException
	{
		JsonLite request = createBizRequest("java", TemporalBiz.class.getName(), "getEntryHistoryWrittenTimeRangeList");
		JsonLite params = (JsonLite)request.get(RequestKey.params.name());
		SimpleDateFormat format = new SimpleDateFormat("yyyy/mm/dd");
		long fromWrittenTime = format.parse("2000/01/01").getTime();		
		long toWrittenTime = System.currentTimeMillis();
		JsonLite rpcParams = createRpcParams("jde/sales?02.100.014-US", -1, fromWrittenTime, toWrittenTime);
		params.put(RequestKey.params.name(), rpcParams);

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
	
	private JsonLite createRpcParams(String gridPath, long validAtTime, long asOfTime)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("validAtTime", validAtTime);
		rpcParams.put("asOfTime",  asOfTime);
		return rpcParams;
	}
	
	private JsonLite createRpcParams(String gridPath, String identityKey, long validAtTime, long asOfTime)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("identityKey", identityKey);
		rpcParams.put("validAtTime", validAtTime);
		rpcParams.put("asOfTime",  asOfTime);
		return rpcParams;
	}
	
	private JsonLite createRpcParams(String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("queryStatement", queryStatement);
		rpcParams.put("validAtTime", validAtTime);
		rpcParams.put("fromWrittenTime",  fromWrittenTime);
		rpcParams.put("toWrittenTime",  toWrittenTime);
		return rpcParams;
	}
}
