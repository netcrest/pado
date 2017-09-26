package com.netcrest.pado.test.junit.rpc.mqtt;

import org.junit.AfterClass;
import org.junit.Test;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.client.biz.PathRpcBiz;
import com.netcrest.pado.rpc.mqtt.client.MqttJsonRpcClient;

@SuppressWarnings({ "rawtypes" })
public class RpcClientTest
{

	@Test
	public void testRpcClient()
	{
		PathRpcBiz pathBiz = new PathRpcBiz("/mygrid/test1");
		JsonLite result = pathBiz.query("get('Name')='Park'");

		System.out.println(result.toString(4, false, false));
	}

	@AfterClass
	public static void tearDown()
	{
		MqttJsonRpcClient.getRpcClient().stop();
	}
}
