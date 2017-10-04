package com.netcrest.pado.test.rpc.client;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IRpcBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.client.dna.RpcInvoker;
import com.netcrest.pado.rpc.mqtt.RequestKey;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AbstractTest
{
	protected static IRpcBiz rpcBiz;

	protected static IPado pado;
	protected static String locators = "ubuntu1:20000";
	protected String lang;
	
	public AbstractTest(String lang)
	{
		this.lang = lang;
	}
	
	@BeforeClass
	public static void beforeStart()
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		if (Pado.isClosed()) {
			Pado.connect(locators, true);
		}
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		rpcBiz = pado.getCatalog().newInstance(IRpcBiz.class);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	protected JsonLite createBizRequest(String className, String methodName)
	{
		JsonLite request;
		if (lang.equals("java")) {
			request = createRequest(RpcInvoker.class.getName(), "invoke");
		} else {
			request = createRequest("com.netcrest.pado.rpc.client.dna.rpc_invoker.RpcInvoker", "invoke");
		}
		JsonLite params = new JsonLite();
		params.put(RequestKey.classname.name(), className);
		params.put(RequestKey.method.name(), methodName);
		request.put(RequestKey.params.name(), params);
		return request;
	}
	
	protected JsonLite createRequest(String className, String methodName)
	{
		JsonLite request = new JsonLite();
		request.put(RequestKey.lang.name(), lang);
		request.put(RequestKey.classname.name(), className);
		request.put(RequestKey.method.name(), methodName);
		request.put(RequestKey.id.name(), System.nanoTime() + "");
		request.put(RequestKey.agent.name(), true);
		request.put(RequestKey.jsonrpc.name(), "2.0");
		return request;
	}
	
	protected void printRequest(JsonLite request)
	{
		System.out.println("Request:");
		String arg = request.toJsonString();
		arg = arg.replaceAll("\"", "\\\\\"");
		arg = "\"" + arg + "\"";
		System.out.println(arg);
		System.out.println(request.toJsonString());
		System.out.println(request.toString(4, false, false));
	}
	
	protected void printReply(JsonLite reply)
	{
		System.out.println(reply.toString(4, false, false));
		System.out.println();
	}
	
	protected void printReply(List<JsonLite> reply)
	{
		int j = 0;
		for (JsonLite jl : reply) {
			System.out.println("[" + (j++) + "]" + jl.toString(4, false, false));
		}
		System.out.println();
	}
}
