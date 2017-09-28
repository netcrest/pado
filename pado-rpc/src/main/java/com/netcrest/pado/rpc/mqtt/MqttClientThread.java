package com.netcrest.pado.rpc.mqtt;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.internal.util.QueueDispatcher;
import com.netcrest.pado.internal.util.QueueDispatcherListener;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.rpc.util.StringUtil;

/**
 * MqttClientThread connects to one of the specified MQTT URLs and listens on
 * the specified topics.
 * 
 * @author dpark
 *
 */
@SuppressWarnings("rawtypes")
public class MqttClientThread extends Thread implements IMqttMessageListener, Constants
{
	/**
	 * QOS of 2 for only once message delivery
	 */
	public final static int DEFAULT_QOS = 2;

	private MqttAsyncClient mqttAsyncClient;
	private MqttConnectOptions options = new MqttConnectOptions();
	private boolean isTerminated = false;

	private String clientId;

	private HashSet<String> topicSet = new HashSet<String>(10);
	private int heartbeatIntervalInMsec;

	private boolean isDebug = false;

	private QueueDispatcher dispatcher = new QueueDispatcher();

	/**
	 * Constructs a new MqttClientThread object. All messages are subscribed
	 * with QOS of {@link #DEFAULT_QOS}.
	 * 
	 * @param threadName
	 *            Thread name
	 * @param clientId
	 *            Client Id unique to the MQTT connection this thread makes
	 * @param mqttUrls
	 *            MQTT URLs. One of the URLs will be connected.
	 * @param topics
	 *            List of topics to subscribe
	 * @param heartbeatIntervalInMsec
	 *            Heartbeat interval in msec.
	 */
	public MqttClientThread(String threadName, String clientId, String[] mqttUrls, String[] topics,
			int heartbeatIntervalInMsec)
	{
		super(threadName);
		this.clientId = clientId;
		this.options.setServerURIs(mqttUrls);
		if (topics != null) {
			for (String topic : topics) {
				topicSet.add(topic);
			}
		}
		this.heartbeatIntervalInMsec = heartbeatIntervalInMsec;
		dispatcher.start();
		setDaemon(true);
		init();
	}

	/**
	 * Initializes MqttClientThread.
	 */
	private void init()
	{
		try {
			String firstUrl = null;
			String[] mqttUrls = options.getServerURIs();
			if (mqttUrls != null && mqttUrls.length > 0) {
				firstUrl = mqttUrls[0];
			}
			if (firstUrl == null) {
				return;
			}
			mqttAsyncClient = new MqttAsyncClient(firstUrl, clientId);
			Logger.config("MQTT client: [mqttUrl=" + firstUrl + ", clientId=" + clientId + "]");
		} catch (MqttException e) {
			Logger.severe("Unable to create MqttAsynClient", e);
		}
	}

	/**
	 * Sets the debug mode. If true, it logs additional information in the log
	 * file. Default is false.
	 * 
	 * @param isDebug
	 *            Debug mode
	 */
	public void setDebug(boolean isDebug)
	{
		this.isDebug = isDebug;
	}

	/**
	 * Returns true if in debug mode. If in debug, it logs additional
	 * information in the log file. Default is false.
	 * 
	 * @return
	 */
	public boolean isDebug()
	{
		return this.isDebug;
	}

	/**
	 * Terminates the thread after gracefully unsubscribing all topics.
	 * Termination is not immediate. It may wait up to the heartbeat interval
	 * before terminates. Invoke #close() if an immediate termination is
	 * desired.
	 */
	public void terminate()
	{
		isTerminated = true;
		unsubscribeTopics();
	}
	
	/**
	 * Returns true is terminated.
	 */
	public boolean isTerminated()
	{
		return isTerminated;
	}

	/**
	 * Connects to the specified broker. If the specified URL is null or same as
	 * the existing connection then it has no effect. Otherwise, it unsubscribes
	 * all topics from the existing connection and disconnects the existing
	 * connection. Note that this call does not make a connection to the new
	 * broker. Instead, a new connection is made during the periodic connection
	 * check performed in the thread loop.
	 * 
	 * @param mqttUrl
	 *            MQTT URL
	 */
	public void connect(String[] mqttUrls)
	{
		if (mqttUrls == null) {
			return;
		}
		String[] thisMqttUrls = options.getServerURIs();
		if (mqttUrls == thisMqttUrls) {
			if (mqttAsyncClient.isConnected()) {
				return;
			}
		}

		// If the specified URLs are same as the exsiting ones then do nothing
		// and return
		if (mqttUrls.length == thisMqttUrls.length) {
			boolean differ = false;
			for (int i = 0; i < mqttUrls.length; i++) {
				if (mqttUrls[i].equals(thisMqttUrls[i]) == false) {
					differ = true;
					break;
				}
			}
			if (differ == false) {
				return;
			}
		}

		options.setServerURIs(mqttUrls);
		HashSet<String> topicSet = this.topicSet;
		unsubscribeTopics();

		if (mqttAsyncClient.isConnected()) {
			disconnect();
		}
		init();
		this.topicSet = topicSet;
	}

	/**
	 * Connects to one of the specified broker URLs. It retries connection every
	 * {@link CONNECTION_RETRY_TIME_IN_MSEC} msec until successful.
	 */
	private synchronized void connect()
	{
		boolean failed = false;
		while (mqttAsyncClient.isConnected() == false) {
			try {
				IMqttToken token = mqttAsyncClient.connect(options);
				token.waitForCompletion();
				Logger.info("MQTT broker connection successful: [connectedUrl=" + mqttAsyncClient.getCurrentServerURI()
						+ "] " + toString());
			} catch (MqttSecurityException e) {
				failed = true;
				Logger.warning("MqttSecurityException while making connection", e);
			} catch (MqttException e) {
				failed = true;
				String msg = "[Url=" + mqttAsyncClient.getCurrentServerURI() + "] " + toString()
						+ " Broker connection retry in " + CONNECTION_RETRY_TIME_IN_MSEC + " msec.";
				if (e.getCause() == null) {
					if (isDebug) {
						Logger.warning(e.getMessage() + msg, e);
					} else {
						Logger.warning(e.getMessage() + msg);
					}
				} else {
					if (isDebug) {
						Logger.warning(e.getMessage() + ". " + e.getCause().getMessage() + msg, e);
					} else {
						Logger.warning(e.getMessage() + ". " + e.getCause().getMessage() + msg);
					}
				}
			}
			if (failed) {
				try {
					Thread.sleep(CONNECTION_RETRY_TIME_IN_MSEC);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}

	/**
	 * Subscribes the specified topic if it has not already been subscribed.
	 * 
	 * @param topic
	 *            topic to subscribe
	 */
	public void subscribeTopic(String topic)
	{
		if (topicSet.contains(topic)) {
			return;
		}
		try {
			IMqttToken token = mqttAsyncClient.subscribe(topic, DEFAULT_QOS, this);
			token.waitForCompletion();
			topicSet.add(topic);
			Logger.info("[" + mqttAsyncClient.getCurrentServerURI()
					+ "] MQTT subscription successful and ready to receive data. TOPIC=" + topic);
		} catch (MqttException e) {
			Logger.severe("[" + mqttAsyncClient.getCurrentServerURI()
					+ "] MqttException raised while making subcription: TOPIC=" + topic, e);
		}
	}

	/**
	 * Subscribes all of the specified topics.
	 */
	private void subscribeTopics()
	{
		if (topicSet.size() == 0 || mqttAsyncClient == null || mqttAsyncClient.isConnected() == false) {
			return;
		}
		try {
			for (String topic : topicSet) {
				IMqttToken token = mqttAsyncClient.subscribe(topic, DEFAULT_QOS, this);
				token.waitForCompletion();
				Logger.info("[" + mqttAsyncClient.getCurrentServerURI()
						+ "] MQTT subscription successful and ready to receive data. TOPIC=" + topic);
			}
		} catch (MqttException e) {
			Logger.severe(
					"[" + mqttAsyncClient.getCurrentServerURI() + "] MqttException raised while making subcription", e);
		}

	}

	public void unsubscribeTopic(String topic)
	{
		if (topicSet.contains(topic) == false) {
			return;
		}
		try {
			IMqttToken token = mqttAsyncClient.unsubscribe(topic);
			token.waitForCompletion();
			topicSet.remove(topic);
			Logger.info("[" + mqttAsyncClient.getCurrentServerURI() + "] MQTT unscription successful. TOPIC=" + topic);
		} catch (MqttException e) {
			Logger.severe(
					"[" + mqttAsyncClient.getCurrentServerURI() + "] MqttException raised while making unsubcription: TOPIC=" + topic,
					e);
		}
	}

	/**
	 * Unsubscribes all subscribed topics. Upon calling this method, all
	 * subscriptions are lost. The {@link #subscribeTopics(String[])} must be
	 * invoked to make new subscriptions.
	 */
	public void unsubscribeTopics()
	{
		if (topicSet.size() == 0 || mqttAsyncClient == null || mqttAsyncClient.isConnected() == false) {
			return;
		}
		try {
			Iterator<String> iterator = topicSet.iterator();
			while (iterator.hasNext()) {
				String topic = iterator.next();
				IMqttToken token = mqttAsyncClient.unsubscribe(topic);
				token.waitForCompletion();
				iterator.remove();
				Logger.info(
						"[" + mqttAsyncClient.getCurrentServerURI() + "] MQTT unscription successful. TOPIC=" + topic);
			}
		} catch (MqttException e) {
			Logger.severe(
					"[" + mqttAsyncClient.getCurrentServerURI() + "] MqttException raised while making unsubcription",
					e);
		}
	}

	public void connectNow()
	{
		connect();
		subscribeTopics();
	}

	/**
	 * Invokes {@link #connect()} which periodically retries connection until
	 * successful. Once a connection is made, this method periodically generates
	 * heartbeats.
	 */
	private void loop()
	{
		connect();
		subscribeTopics();
		try {
			while (mqttAsyncClient.isConnected() && isTerminated == false) {
				try {
					pumpHearbeat();
					Thread.sleep(heartbeatIntervalInMsec);
				} catch (MqttException e) {
					// ignore
				}
			}
			System.out.println("out of loop");
		} catch (InterruptedException e) {
			Logger.info(getName() + " interrupt received. Terminating...");
			isTerminated = true;
		}
	}

	/**
	 * Disconnects the broker.
	 */
	private synchronized void disconnect()
	{
		if (mqttAsyncClient == null || mqttAsyncClient.isConnected() == false) {
			return;
		}
		try {
			IMqttToken token = mqttAsyncClient.disconnect();
			token.waitForCompletion();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Closes the broker connection and terminates the thread. It first
	 * unsubcribes all topics before closing the broker connection. If the
	 * broker connection cannot be closed in 2 seconds, then it forcibly
	 * disconnects the connection. This object is no longer valid after this
	 * call.
	 */
	public synchronized void close()
	{
		isTerminated = true;
		if (mqttAsyncClient.isConnected()) {
			unsubscribeTopics();
			try {
				IMqttToken token = mqttAsyncClient.disconnect();
				token.waitForCompletion(2000);
				if (token.isComplete() == false) {
					mqttAsyncClient.disconnectForcibly();
				}
				mqttAsyncClient.close();
				Logger.info(getName() + " terminated.");
			} catch (MqttException e) {
				Logger.severe("Error occurred while disconnecting MQTT client.", e);
			}
		}
	}

	/**
	 * Runs this thread.
	 */
	@Override
	public void run()
	{
		// Connect until successful. Exit only if terminated.
		while (isTerminated == false) {
			loop();
		}
		close();
//		mqttAsyncClient = null;
	}

	/**
	 * Returns this thread's configuration info in printable string.
	 * 
	 * @return
	 */
	public String toString()
	{
		String topicNames = StringUtil.arrayToString(topicSet.toArray(new String[topicSet.size()]), ',');
		String serverUrls = StringUtil.arrayToString(options.getServerURIs(), ',');
		StringBuffer buffer = new StringBuffer(100);
		buffer.append("[serverUrls=");
		buffer.append("[");
		buffer.append(serverUrls);
		buffer.append("]");
		buffer.append(", clientId=");
		buffer.append(clientId);
		buffer.append(", topics=[");
		buffer.append(topicNames);
		buffer.append("], isDebug=");
		buffer.append(isDebug);
		buffer.append("]");
		return buffer.toString();
	}

	/**
	 * MQTT listener callback method.
	 */
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception
	{
		dispatcher.enqueue(new Message(topic, message));
	}

	/**
	 * Sets the dispatcher's listener which receives MQTT payloads in byte
	 * arrays (byte[].
	 * 
	 * @param listener
	 *            MQTT message listener
	 */
	public void setDispatcherListener(QueueDispatcherListener listener)
	{
		dispatcher.setQueueDispatcherListener(listener);
	}

	public QueueDispatcherListener getDispatcherListener()
	{
		return dispatcher.getQueueDispatcherListener();
	}

	/**
	 * Generates a heartbeat.
	 * 
	 * @throws MqttPersistenceException
	 *             Thrown if an MQTT error occurs
	 * @throws MqttException
	 *             Thrown if an MQTT error occurs
	 */
	public void pumpHearbeat() throws MqttPersistenceException, MqttException
	{
		// do nothing for now
	}

	/**
	 * Returns the heartbeat interval in msec.
	 */
	public int getHearbeatIntervalInMsec()
	{
		return this.heartbeatIntervalInMsec;
	}

	/**
	 * Publishes the specified JSON object on the specified topic with the
	 * specified wait for completion timeout.
	 * 
	 * @param topic
	 *            MQTT topic
	 * @param jl
	 *            JSON object
	 * @param timeout
	 *            Wait for completion timeout in msec. If less than or equal to
	 *            0 then it waits for completion indefinitely.
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */
	public void publish(String topic, JsonLite jl, int timeout) throws MqttPersistenceException, MqttException
	{
		if (jl == null) {
			return;
		}
		IMqttDeliveryToken token = mqttAsyncClient.publish(topic, jl.toJsonString().getBytes(), DEFAULT_QOS, false);
		if (timeout > 0) {
			token.waitForCompletion(timeout);
		} else {
			token.waitForCompletion();
		}
	}

	/**
	 * Publishes the specified JSON object on the specified topic without
	 * waiting for completion.
	 * 
	 * @param topic
	 *            MQTT topic
	 * @param jl
	 *            JSON object
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */
	public void publish(String topic, JsonLite jl) throws MqttPersistenceException, MqttException
	{
		if (jl == null) {
			return;
		}
		IMqttDeliveryToken token = mqttAsyncClient.publish(topic, jl.toJsonString().getBytes(), DEFAULT_QOS, false);
		token.waitForCompletion();
	}

	public static class Message
	{
		private String topic;
		private MqttMessage mqttMessage;

		public Message(String topic, MqttMessage mqttMessage)
		{
			this.topic = topic;
			this.mqttMessage = mqttMessage;
		}

		public String getTopic()
		{
			return topic;
		}

		public MqttMessage getMqttMessage()
		{
			return mqttMessage;
		}
	}
}
