package com.netcrest.pado.rpc.mqtt.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.internal.util.QueueDispatcherListener;
import com.netcrest.pado.rpc.client.IRpcContext;
import com.netcrest.pado.rpc.mqtt.Constants;
import com.netcrest.pado.rpc.mqtt.MqttClientThread;
import com.netcrest.pado.rpc.mqtt.ReplyKey;
import com.netcrest.pado.rpc.mqtt.RequestKey;
import com.netcrest.pado.rpc.util.RpcUtil;
import com.netcrest.pado.rpc.util.StringUtil;

/**
 * MqttJsonRpcClient connects to the MQTT broker and subscribes its "reply"
 * topic. It also provides methods to execute "request" services on the data
 * node and the "result" method to send the result to the data node that
 * originated the start of the client app which led to use of this class.
 * <p>
 * <b>IMPORTANT</b> <b> The {@link #initialize(String)} method must be invoked
 * first to create the singleton object.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MqttJsonRpcClient implements ClientConstants
{
	private static MqttJsonRpcClient rpcClient;

	private static Logger LOGGER = Logger.getLogger(MqttJsonRpcClient.class.getName());

	private MqttClientThread mqttClientThread;
	private int heartbeatIntervalInMsec;
	private HashMap<String, ThreadReply> idThreadMap = new HashMap<String, ThreadReply>(10);
	// <listenerName, IRpcListener>
	HashMap<String, List<IRpcListener>> idRpcListenerMap = new HashMap<String, List<IRpcListener>>(10);
	HashMap<String, List<IRpcListener>> liveIdRpcListenerMap = new HashMap<String, List<IRpcListener>>(10);
	private String requestTopic;
	private String replyTopic;
	private String agentRequestTopic;
	private boolean isAgent;
	private boolean isDebug;

	// agentExecutorService is created only if the isAgent is true
	private ExecutorService agentExecutorService;

	/**
	 * Constructs a private instance of MqttJsonRpcClient.
	 * 
	 * @param props
	 *            RPC specific properties typically read from the
	 *            "rpc.properties" file.
	 * @param serverId
	 *            Unique server ID representing the data node to communicate
	 *            with. This ID is used to create the agent request topic and/or
	 *            the reply topic.
	 */
	private MqttJsonRpcClient(Properties props, String serverId)
	{
		init(props, serverId);
	}

	/**
	 * Initializes the MQTT communications mechanism.
	 * 
	 * @param rpcProps
	 *            RPC specific properties typically read from the
	 *            "rpc.properties" file.
	 */
	private void init(Properties rpcProps, String serverId)
	{
		String hostName = null;
		try {
			InetAddress iAddress = InetAddress.getLocalHost();
			hostName = iAddress.getHostName();
		} catch (UnknownHostException e) {
			LOGGER.log(Level.SEVERE, "Unable get host name needed for MQTT client ID.", e);
		}

		String intervalStr = rpcProps.getProperty(PROP_HEARTBEAT_INTERVAL_IN_MSEC);
		heartbeatIntervalInMsec = DEFAULT_HEARTBEAT_INTERVAL_IN_MSEC;
		if (intervalStr != null) {
			try {
				heartbeatIntervalInMsec = Integer.parseInt(intervalStr);
			} catch (Exception ex) {
				// ignore
			}
		}
		String val = rpcProps.getProperty(PROP_RPC_AGENT_ENABLED, "false");
		isAgent = Boolean.valueOf(val);

		val = rpcProps.getProperty(PROP_RPC_DEBUG, "false");
		isDebug = Boolean.valueOf(val);

		String urls = rpcProps.getProperty(PROP_MQTT_URL);
		String[] urlArray = new String[] { DEFAULT_MQTT_URL };
		if (urls != null) {
			urlArray = StringUtil.string2Array(urls, ",");
		}
		requestTopic = TOPIC_REQUEST_PREFIX + "/" + serverId;
		UUID uuid = UUID.randomUUID();
		String clientId = "rpc-client-" + hostName + "-" + uuid.toString();
		replyTopic = TOPIC_REPLY_PREFIX + "/" + uuid.toString();
		String topics[];
		if (isAgent) {
			clientId = "rpc-agent-" + hostName + "-" + serverId;
			agentRequestTopic = RpcUtil.getAgentRequestTopic("java", serverId);
			topics = new String[] { agentRequestTopic, replyTopic };
			val = rpcProps.getProperty(PROP_RPC_AGENT_THREAD_POOL_SIZE, Integer.toString(DEFAULT_RPC_AGENT_POOL_SIZE));
			int agentThreadPoolSize;
			try {
				agentThreadPoolSize = Integer.parseInt(val);
			} catch (Exception ex) {
				agentThreadPoolSize = DEFAULT_RPC_AGENT_POOL_SIZE;
			}
			agentExecutorService = Executors.newFixedThreadPool(agentThreadPoolSize);
		} else {
			clientId = "rpc-client-" + hostName + "-" + uuid.toString();
			topics = new String[] { replyTopic };
		}
		mqttClientThread = new MqttClientThread("MqttJsonRpcThread", clientId, urlArray, topics,
				heartbeatIntervalInMsec);
		mqttClientThread.setDispatcherListener(new PayloadListenerImpl());
		mqttClientThread.connectNow();
		mqttClientThread.start();
	}

	/**
	 * Reads the rpc.properties file.
	 * 
	 * @return
	 */
	private static Properties readRpcProperties()
	{
		String propertiesFilePath = System.getProperty(PROP_JSON_RPC_CLIENT_PROPERTIES_FILE,
				DEFAULT_JSON_RPC_CLIENT_PROPERTIES_FILE_PATH);
		Properties props = new Properties();
		File file = new File(propertiesFilePath);
		if (file.exists()) {
			FileReader reader = null;
			try {
				reader = new FileReader(file);
				props.load(reader);
			} catch (FileNotFoundException e1) {
				LOGGER.log(Level.SEVERE,
						"rpc.properties file not found: " + file.getAbsolutePath() + ". Using defaults properties.");
			} catch (IOException e1) {
				LOGGER.log(Level.SEVERE, "IOException while reading gid.properties file: " + file.getAbsolutePath()
						+ ". Using default properties.");
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}

		return props;
	}

	/**
	 * Initializes MqttJsonRpcClient for the specified unique server ID.
	 * 
	 * @param serverId
	 *            Unique server ID (or name)
	 */
	public final static synchronized void initialize(String serverId)
	{
		if (rpcClient == null) {
			rpcClient = new MqttJsonRpcClient(readRpcProperties(), serverId);
		}
	}

	/**
	 * Returns the singleton instance of the MqttJsonRpcClient class.
	 * 
	 * @return
	 */
	public final static MqttJsonRpcClient getRpcClient()
	{
		return rpcClient;
	}

	/**
	 * Starts the MQTT client. This method must be invoked to enable the MQTT
	 * communications mechanism.
	 */
	public void start()
	{
		if (mqttClientThread.isAlive() == false) {
			mqttClientThread.start();
		}
	}

	/**
	 * Stops the MQTT communications mechanism. Once invoked, this object is no
	 * longer usable.
	 */
	public void stop()
	{
		mqttClientThread.terminate();
		mqttClientThread.interrupt();
	}

	/**
	 * Returns true if the this client has been stopped.
	 */
	public boolean isStopped()
	{
		return mqttClientThread.isTerminated();
	}

	/**
	 * Executes the specified function with the specified function parameters.
	 * The function name must be one of the built-in functions.
	 * 
	 * @param rpcContext
	 *            IRpcContext object
	 * @param functionName
	 *            Function name
	 * @param params
	 *            Function parameters
	 * @param timeout
	 *            0 or less for no timeout, a positive value for timeout in
	 *            msec.
	 * @return Function return value
	 */
	public JsonLite execute(IRpcContext rpcContext, String functionName, JsonLite params, int timeout)
	{
		return execute(rpcContext, null, functionName, params, timeout);
	}

	/**
	 * Executes the specified class' method with the specified method
	 * parameters.
	 * 
	 * @param rpcContext
	 *            IRpcContext object
	 * @param className
	 *            Class name.
	 * @param methodName
	 *            Method name. If className is null, then it is assumed a
	 *            function name.
	 * @param params
	 *            Method parameters. If className is null, then it is assumed
	 *            function parameters.
	 * @param timeout
	 *            0 or less for no timeout, a positive value for timeout in
	 *            msec.
	 * @return Method (or function) return value
	 */
	public JsonLite execute(IRpcContext rpcContext, String className, String methodName, JsonLite params, int timeout)
	{
		JsonLite request = RpcUtil.createRequest(rpcContext, className, methodName, params);
		return execute(request, timeout);
	}

	/**
	 * Executes the specified business object's method with the specified method
	 * parameters. The actual implementation class that hides the RPC
	 * communications details must be supplied in order for this method to work.
	 * The implementation class follows the Pado business object naming
	 * conventions as follows:
	 * <ul>
	 * <li>It must be instantiable by the default (no-args) constructor)</li>
	 * <li>Its package name must start with the package name of the specified
	 * business object and end with <i>.impl.&lt;platform&gt;</i>, where
	 * <i>&lt;platform;&gt;</i> is the data grid product name, i.e.,
	 * <i>gemfire</i>.</li>
	 * </ul>
	 * Its class name must start with the class name of the specified business
	 * object end with <i>Impl</i></li>
	 * </ul>
	 * <p>
	 * <b>Example:</b>
	 * <ul>
	 * <b>biz</b> com.netcrest.pado.rpc.client.biz.PathBiz
	 * </ul>
	 * <ul>
	 * <b>impl</b> com.netcrest.pado.rpc.client.biz.impl.gemfire.PathBizImpl
	 * </ul>
	 * 
	 * @param rpcContext
	 *            IRpcContext object
	 * @param biz
	 *            Business object.
	 * @param methodName
	 *            Method name. If biz is null, then it is assumed a function
	 *            name.
	 * @param params
	 *            Method parameters. If biz is null, then it is assumed function
	 *            parameters.
	 * @param timeout
	 *            0 or less for no timeout, a positive value for timeout in
	 *            msec.
	 * @return Method (or function) return value
	 */
	public JsonLite execute(IRpcContext rpcContext, Object biz, String methodName, JsonLite params, int timeout)
	{
		String className;
		if (biz == null) {
			className = null;
		} else {
			className = biz.getClass().getName();
		}
		JsonLite request = RpcUtil.createRequest(rpcContext, className, methodName, params);
		return execute(request, timeout);
	}

	/**
	 * Executes the specified request.
	 * 
	 * @param request
	 *            Request conforming to the Pado-extended JSON RPC 2.0 spec.
	 * @param timeout
	 *            0 or less for no timeout, a positive value for timeout in
	 *            msec.
	 * @return Results from executing the request. It returns null if the method
	 *         has the void type.
	 */
	public JsonLite execute(JsonLite request, int timeout)
	{
		JsonLite reply = null;

		String id = request.getString(RequestKey.id.name(), null);
		if (id != null) {
			try {
				ThreadReply threadReply = new ThreadReply(Thread.currentThread());
				idThreadMap.put(id, threadReply);
				request.put(RequestKey.replytopic.name(), replyTopic);
				System.out.println("MqttJsonRpcClient.execute(): " + request);
				mqttClientThread.publish(requestTopic, request);
				if (timeout < 0) {
					timeout = 0;
				}
				synchronized (threadReply) {
					threadReply.wait(timeout);
				}
				threadReply = idThreadMap.remove(id);
				if (threadReply != null) {
					reply = (JsonLite) threadReply.reply;
				}
			} catch (MqttException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				idThreadMap.remove(id);
			}
		}

		return reply;
	}

	/**
	 * Sends the specified reply that contains result to the data node.
	 * 
	 * @param reply
	 *            Reply object with the "result" parameter conforming to the
	 *            Pado-extended JSON RPC 2.0 spec.
	 */
	public void sendResult(JsonLite reply)
	{
		if (reply == null) {
			return;
		}
		try {
			mqttClientThread.publish(TOPIC_RESULT, reply, 2000);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addRpcListener(String listenerName, IRpcListener listener)
	{
		if (listenerName == null || listenerName.trim().length() == 0 || listener == null) {
			return;
		}
		String topic = Constants.TOPIC_LISTENER + "/" + listenerName.trim();
		List<IRpcListener> listenerList = idRpcListenerMap.get(topic);
		if (listenerList == null) {
			listenerList = new ArrayList<IRpcListener>(3);
			idRpcListenerMap.put(topic, listenerList);
		}
		if (listenerList.contains(listener)) {
			return;
		}
		mqttClientThread.subscribeTopic(topic);
		listenerList.add(listener);
		liveIdRpcListenerMap.put(topic, new ArrayList<IRpcListener>(listenerList));
	}

	public void removeRpcListener(String listenerName, IRpcListener listener)
	{
		if (listenerName == null || listenerName.trim().length() == 0) {
			return;
		}
		String topic = Constants.TOPIC_LISTENER + "/" + listenerName.trim();
		List<IRpcListener> listenerList = idRpcListenerMap.get(topic);
		if (listenerList != null) {
			if (listenerList.size() == 0) {
				mqttClientThread.unsubscribeTopic(topic);
			}
			listenerList.remove(listener);
			liveIdRpcListenerMap.put(topic, new ArrayList<IRpcListener>(listenerList));
		}
	}

	/**
	 * Returns the live list of listeners that is thread safe.
	 * 
	 * @param listenerName
	 *            IRpicListener name
	 * @return null if not found
	 */
	public List<IRpcListener> getLiveRpcListenerList(String topic)
	{
		if (topic == null || topic.trim().length() == 0) {
			return null;
		}
		return liveIdRpcListenerMap.get(topic);
	}

	public void close()
	{
		mqttClientThread.close();
	}

	class PayloadListenerImpl implements QueueDispatcherListener
	{
		@Override
		public void objectDispatched(Object obj)
		{
			MqttClientThread.Message message = (MqttClientThread.Message) obj;
			byte[] payload = message.getMqttMessage().getPayload();
			if (message.getTopic().equals(agentRequestTopic)) {
				// agent request
				final String request = new String(payload);
				agentExecutorService.execute(new Runnable() {

					@Override
					public void run()
					{
						RpcUtil.processRequest(request, false);
					}

				});

			} else {
				// reply
				JsonLite reply = new JsonLite(new String(payload));
				String id = reply.getString(ReplyKey.id.name(), null);
				if (id != null) {
					ThreadReply threadReply = idThreadMap.get(id);
					if (threadReply != null) {
						threadReply.reply = reply;
						synchronized (threadReply) {
							threadReply.notify();
						}
					}
				} else {
					List<IRpcListener> liveListenerList = getLiveRpcListenerList(message.getTopic());
					if (liveListenerList != null) {
						for (IRpcListener listener : liveListenerList) {
							listener.messageReceived(reply);
						}
					}
				}
			}
		}
	}

	class ThreadReply
	{
		ThreadReply(Thread thread)
		{
			this.thread = thread;
		}

		Thread thread;
		volatile Object reply;
	}
}
