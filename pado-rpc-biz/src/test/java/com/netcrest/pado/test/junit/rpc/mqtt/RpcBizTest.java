package com.netcrest.pado.test.junit.rpc.mqtt;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IRpcBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.rpc.mqtt.RequestKey;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class RpcBizTest
{
	private static IPado pado;
	private static IRpcBiz rpcBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		if (Pado.isClosed()) {
			Pado.connect("ubuntu1:20000", true);
		}
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		rpcBiz = catalog.newInstance(IRpcBiz.class);
	}

	@Test
	public void testBroadcastPython()
	{
		System.out.println("RpcBizTest.testBroadcastPython()");
		System.out.println("--------------------------------");
		JsonLite request = createRequestPython();
		System.out.println("Request:");
		String arg = request.toJsonString();
		arg = arg.replaceAll("\"", "\\\\\"");
		arg = "\"" + arg + "\"";
		System.out.println(arg);
		System.out.println(request.toJsonString());
		System.out.println(request.toString(4, false, false));
		List<JsonLite> reply = rpcBiz.broadcast(request);
		System.out.println("Reply:");
		System.out.println(reply);
		System.out.println();
	}
	
	@Test
	public void testOnServerPython()
	{
		System.out.println("RpcBizTest.testOnServerPython()");
		System.out.println("-------------------------------");
		JsonLite request = createRequestPython();
		System.out.println("Request:");
		System.out.println(request.toString(4, false, false));
		JsonLite reply = rpcBiz.executeOnServer(request);
		System.out.println("Reply:");
		System.out.println(reply);
		System.out.println();
	}
	
	@Test
	public void testOnPathPython()
	{
		System.out.println("RpcBizTest.testOnPathPython()");
		System.out.println("-----------------------------");
		JsonLite request = createRequestPython();
		System.out.println("Request:");
		System.out.println(request.toString(4, false, false));
		rpcBiz.getBizContext().getGridContextClient().setGridPath("test1");
		List<JsonLite> reply = rpcBiz.executeOnPath(request);
		System.out.println("Reply:");
		System.out.println(reply);
		System.out.println();
	}
	
	@Test
	public void testBroadcastJava()
	{
		System.out.println("RpcBizTest.testBroadcastJava()");
		System.out.println("------------------------------");
		JsonLite request = createRequestJava();
		request.put(RequestKey.lang.name(), "java");
		System.out.println("Request:");
		System.out.println(request.toString(4, false, false));
		List<JsonLite> reply = rpcBiz.broadcast(request);
		System.out.println("Reply:");
		System.out.println(reply);
		System.out.println();
	}
	
	
	@Test
	public void testOnServerJava()
	{
		System.out.println("RpcBizTest.testOnServerJava()");
		System.out.println("-----------------------------");
		JsonLite request = createRequestJava();
		request.put(RequestKey.lang.name(), "java");
		System.out.println("Request:");
		System.out.println(request.toString(4, false, false));
		JsonLite reply = rpcBiz.executeOnServer(request);
		System.out.println("Reply:");
		System.out.println(reply);
		System.out.println();
	}
	
	@Test
	public void testOnPathJava()
	{
		System.out.println("RpcBizTest.testOnPathJava()");
		System.out.println("----------------------------");
		JsonLite request = createRequestJava();
		request.put(RequestKey.lang.name(), "java");
		System.out.println("Request:");
		System.out.println(request.toString(4, false, false));
		rpcBiz.getBizContext().getGridContextClient().setGridPath("test1");
		List<JsonLite> reply = rpcBiz.executeOnPath(request);
		System.out.println("Reply:");
		System.out.println(reply);
		System.out.println();
	}
	
	private static JsonLite createRequestPython()
	{
		JsonLite request = new JsonLite();
		request.put(RequestKey.lang.name(), "python");
		request.put(RequestKey.classname.name(), "com.netcrest.pado.test.rpc.ml_demo.MlDemo");
		request.put(RequestKey.method.name(), "execute");
		request.put(RequestKey.id.name(), System.nanoTime() + "");
//		request.put(RequestKey.daemon.name(), true);
		JsonLite params = new JsonLite();
		// params.put("path", "/mygrid/test1");
		params.put("queryPredicate", "get('Name')='Park'");
		request.put("params", params);
		request.put(RequestKey.jsonrpc.name(), "2.0");
		return request;
	}

	private static JsonLite createRequestJava()
	{
		JsonLite request = new JsonLite();
		request.put(RequestKey.lang.name(), "java");
		request.put(RequestKey.classname.name(), "com.netcrest.pado.test.rpc.MlDemo");
		request.put(RequestKey.method.name(), "execute");
		request.put(RequestKey.id.name(), System.nanoTime() + "");
//		request.put(RequestKey.daemon.name(), true);
		JsonLite params = new JsonLite();
		// params.put("path", "/mygrid/test1");
		params.put("queryPredicate", "get('Name')='Park'");
		request.put("params", params);
		request.put(RequestKey.jsonrpc.name(), "2.0");
		return request;
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

}
