package com.netcrest.pado.rpc.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.internal.util.QueueDispatcherListener;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.rpc.RpcManager;

/**
 * MqttJsonRpcListenerImpl listens on {@link MqttClientThread} dispatched
 * "request" and "result" messages in the form of MqttClientThread.Message.
 * 
 * @author dpark
 *
 */
@SuppressWarnings("rawtypes")
public class MqttJsonRpcListenerImpl implements QueueDispatcherListener, Constants
{
	private static MqttJsonRpcListenerImpl listenerImpl;

	private MqttClientThread mqttClientThread;

	public static final synchronized MqttJsonRpcListenerImpl initialize(MqttClientThread mqttClientThread)
	{
		if (listenerImpl == null) {
			listenerImpl = new MqttJsonRpcListenerImpl(mqttClientThread);
		}
		return listenerImpl;
	}

	public static final MqttJsonRpcListenerImpl getMqttJsonRpcListenerImpl()
	{
		return listenerImpl;
	}

	private MqttJsonRpcListenerImpl(MqttClientThread mqttClientThread)
	{
		this.mqttClientThread = mqttClientThread;
		init();
	}

	private void init()
	{
	}

	@Override
	public void objectDispatched(Object obj)
	{
		MqttClientThread.Message message = (MqttClientThread.Message) obj;
		String topic = message.getTopic();
		byte[] payload = message.getMqttMessage().getPayload();
		boolean isRequest = topic.startsWith(TOPIC_REQUEST_PREFIX);

		JsonLite jl = null;
		try {
			jl = new JsonLite(new String(payload));
		} catch (Exception ex) {
			// parser error
			Logger.error("Error occurred while parsing JSON request. " + ex.getMessage() + " " + new String(payload));
			return;
		}

		String jsonrpc = jl.getString(RequestKey.jsonrpc.name(), DEFAULT_JSON_RPC_VERSION);
		if (jsonrpc.equals(DEFAULT_JSON_RPC_VERSION) == false) {
			Logger.warning("Unsupported JSON RPC request received. Aborted. " + jl);
		}
		String id = jl.getString(RequestKey.id.name(), null);
		if (isRequest) {
			// Request
			JsonLite reply = RpcManager.getRpcManager().invoke(jl);
			if (reply != null) {
				if (id != null) {
					try {
						reply(jl, reply);
					} catch (MqttException e) {
						Logger.error("JSON RPC reply error. " + e.getMessage());
					}
				}
			}
		} else {
			// Result
			MqttJsonRpcOsClient.getRpcClient().notifyReply(id, jl);
		}
	}

	public void publish(String topic, JsonLite message) throws MqttPersistenceException, MqttException
	{
		if (message != null) {
			mqttClientThread.publish(topic, message);
		}
	}

	public void reply(JsonLite request, JsonLite reply) throws MqttPersistenceException, MqttException
	{
		String replyTopic = request.getString(RequestKey.replytopic.name(), TOPIC_REPLY);
		mqttClientThread.publish(replyTopic, reply);
	}

}
