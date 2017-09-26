package com.netcrest.pado.test.junit.rpc.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.junit.Test;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.mqtt.Constants;
import com.netcrest.pado.rpc.mqtt.RequestKey;

public class RpcRequestTest extends AbstractTest implements Constants
{

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testRpcRequest() throws MqttPersistenceException, MqttException
	{
		JsonLite request = new JsonLite();
		request.put(RequestKey.id.name(), "10");
		request.put(RequestKey.jsonrpc.name(), "2.0");
		request.put(RequestKey.method.name(), "query");
		JsonLite params = new JsonLite();
		params.put("path", "/mygrid/test1");
		params.put("queryPredicate", "get('Name')='Park'");
		request.put(RequestKey.params.name(), params);
		MqttMessage message = new MqttMessage(request.toString().getBytes());
		mqttAsyncClient.publish(TOPIC_REQUEST, message);
	}

}
