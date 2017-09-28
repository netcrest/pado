package com.netcrest.pado.rpc.mqtt;

/**
 * Constants contains constant values used by the MQTT plugin.
 * 
 * @author dpark
 *
 */
public interface Constants
{
	public final static int CONNECTION_RETRY_TIME_IN_MSEC = 10000;

	/**
	 * Default iot-agent heartbeat topic. A hearbeat message is published every
	 * {@link #DEFAULT_HEARTBEAT_INTERVAL_IN_MSEC} milliseconds on this topic.
	 */
	public final static String DEFAULT_HEARTBEAT_TOPIC = "/__pado/heartbeat";

	public final static String TOPIC_REQUEST_PREFIX = "/__pado/request";
	public final static String TOPIC_REPLY = "/__pado/reply";
	public final static String TOPIC_RESULT = "/__pado/result";
	public final static String TOPIC_LISTENER = "/__pado/listener";


	public final static int DEFAULT_QOS = 2;
	public final static int DEFAULT_HEARTBEAT_INTERVAL_IN_MSEC = 60000;

	public final static String PROP_MQTT_URL = "mqtt.all.url";
	public final static String PROP_HEARTBEAT_INTERVAL_IN_MSEC = "mqtt.heartbeat.interval";

	/**
	 * Boolean property. true to enable debug info in log. Default: false
	 */
	public final static String PROP_MQTT_DEBUG = "mqtt.debug";
	
	public final static String DEFAULT_JSON_RPC_VERSION = "2.0";
	
	public final static String DEFAULT_MQTT_URL= "tcp://localhost:1883";
	public final static int DEFAULT_TIMEOUT_IN_MSEC = 10000;
}
