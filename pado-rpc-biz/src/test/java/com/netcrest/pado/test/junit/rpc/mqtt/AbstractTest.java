package com.netcrest.pado.test.junit.rpc.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.mqtt.Constants;
import com.netcrest.pado.rpc.util.StringUtil;

@SuppressWarnings("rawtypes")
public class AbstractTest
{
	// Change the following values in src/main/resource/etc/test.properties
	public static String mqttUrls = "tcp://ubuntu1:1883";

	/**
	 * If isHeartbeatListener is true (default) then listens on heartbeats. Set
	 * it to false if heartbeats are not needed just prior to invoking
	 * {@link #beforeStart()}.
	 */
	public static boolean isHeartbeatListener = true;

	public static MqttAsyncClient mqttAsyncClient;
	public static String clientId = "pado-mqtt-test-" + System.currentTimeMillis();
	public final static int DEFAULT_QOS = 2;

	public static ReplyListener replyListener = new ReplyListener();

	@BeforeClass
	public static void beforeStart()
	{
		try {
			MqttConnectOptions mqttOptions = new MqttConnectOptions();
			String mqttUrlArray[] = StringUtil.string2Array(mqttUrls, ",");
			String firstUrl = null;
			if (mqttUrls != null && mqttUrlArray.length > 0) {
				firstUrl = mqttUrlArray[0];
			}
			if (firstUrl == null) {
				return;
			}
			mqttOptions.setServerURIs(mqttUrlArray);
			mqttAsyncClient = new MqttAsyncClient(firstUrl, clientId);
			IMqttToken token = mqttAsyncClient.connect(mqttOptions);
			token.waitForCompletion();
			
			mqttAsyncClient.subscribe(Constants.TOPIC_REPLY, Constants.DEFAULT_QOS, replyListener);
			token.waitForCompletion();
			System.out.println(
					"mqttAsyncClient.isConnected()=" + mqttAsyncClient.isConnected() + ", token=" + token);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * HeartbeatListener listens heartbeat messages and extracts GID, EID, and
	 * DID information from the heartbeat. It also makes a connection to the
	 * local broker if configured.
	 * 
	 * @author dpark
	 *
	 */
	static class ReplyListener implements IMqttMessageListener
	{
		private boolean heartbeatReceived = false;

		@Override
		public void messageArrived(String topic, MqttMessage message)
		{
			try {
				byte[] payload = message.getPayload();
				JsonLite reply = new JsonLite(new String(payload));
				System.out.println(reply.toString(4, false, false));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public boolean isHeartbeatReceived()
		{
			return heartbeatReceived;
		}
	}

	@AfterClass
	public static void tearDown() throws MqttException, InterruptedException
	{
		Thread.sleep(10000);
		if (mqttAsyncClient != null) {
			if (mqttAsyncClient.isConnected()) {
				mqttAsyncClient.disconnect();
			}
			mqttAsyncClient.close();
		}
	}
}
