package com.netcrest.pado.rpc.mqtt.client;

/**
 * ClientConstants contains MQTT constant values required by the RPC mechanics
 * that use MQTT communications.
 * 
 * @author dpark
 *
 */
public interface ClientConstants
{
	public final static String PROP_JSON_RPC_CLIENT_PROPERTIES_FILE = "rpcPropertyFile";
	public final static String PROP_HEARTBEAT_INTERVAL_IN_MSEC = "mqtt.heartbeat.interval";
	public final static String PROP_RPC_DEBUG = "mqtt.debug";
	public final static String PROP_MQTT_URL = "mqtt.url";

	public final String DEFAULT_JSON_RPC_CLIENT_PROPERTIES_FILE_PATH = "etc/rpc.properties";

	public final static int DEFAULT_QOS = 2;
	public final static int DEFAULT_HEARTBEAT_INTERVAL_IN_MSEC = 60000;
	public final static String DEFAULT_MQTT_URL = "tcp://localhost:1883";

	public final static String TOPIC_REQUEST = "/__pado/request";
	public final static String TOPIC_REPLY = "/__pado/reply";
	public final static String TOPIC_RESULT = "/__pado/result";
}
