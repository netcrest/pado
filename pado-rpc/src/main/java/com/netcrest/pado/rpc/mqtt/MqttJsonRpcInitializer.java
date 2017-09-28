package com.netcrest.pado.rpc.mqtt;

import java.lang.reflect.Modifier;
import java.util.Properties;
import java.util.Set;

import org.reflections.Reflections;

import com.netcrest.pado.IBeanInitializable;
import com.netcrest.pado.internal.util.HotDeploymentBizClasses;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.rpc.RpcManager;
import com.netcrest.pado.server.DeploymentListener;
import com.netcrest.pado.server.PadoServerManager;

/**
 * MqttJsonRpcInitializer initializes the MQTT-based RPC mechanics for enabling
 * local (server-side) access by client apps running in the same data node.
 * 
 * @author dpark
 *
 */
public class MqttJsonRpcInitializer implements IBeanInitializable, DeploymentListener
{
	@Override
	public void init(Properties properties)
	{
		String clientId = PadoServerManager.getPadoServerManager().getServerName();
		String val = properties.getProperty(Constants.PROP_MQTT_URL, Constants.DEFAULT_MQTT_URL);
		String mqttUrls[] = val.split(",");
		int i = 0;
		for (String s : mqttUrls) {
			mqttUrls[i++] = s.trim();
		}
		String topics[] = new String[] { Constants.TOPIC_REQUEST_PREFIX + "/" + PadoServerManager.getPadoServerManager().getServerName(), Constants.TOPIC_RESULT };
		int heartbeatIntervalInMsec = 60000;
		MqttClientThread mqttClientThread = new MqttClientThread("MqttJsonRpcThread", clientId, mqttUrls, topics,
				heartbeatIntervalInMsec);
		MqttJsonRpcListenerImpl rpcListener = MqttJsonRpcListenerImpl.initialize(mqttClientThread);
		mqttClientThread.setDispatcherListener(rpcListener);
		mqttClientThread.start();

		PadoServerManager.getPadoServerManager().addDeploymentListener(this);
		Logger.config("MqttJsonRpcInitializer started.");
	}

	/**
	 * Invoked during startup and hot deployment to capture and register IRpc classes.
	 */
	@Override
	public void jarDeployed(HotDeploymentBizClasses deployment)
	{
		Reflections r = deployment.getReflections();
		Set<String> rpcClassNameSet = r.getStore().getSubTypesOf("com.netcrest.pado.rpc.IRpc");
		StringBuffer buffer = new StringBuffer(100);
		for (String rpcClassName : rpcClassNameSet) {
			try {
				ClassLoader classLoader = deployment.getClassLoader();
				Class<?> clazz;
				if (classLoader == null) {
					clazz = Class.forName(rpcClassName);
				} else {
					clazz = deployment.getClassLoader().loadClass(rpcClassName);
				}

				if (Modifier.isAbstract(clazz.getModifiers())) {
					continue;
				}
				RpcManager.getDeviceManager().addRpcClass(clazz);
				if (buffer.length() > 0) {
					buffer.append(", ");
				}
				buffer.append(clazz.getName());
			} catch (Exception e) {
				Logger.warning("Error occurred while instantiating " + rpcClassName + ". This class is ignored.", e);
			}
		}
		Logger.config("RPC classes extracted from jars and added to the rpc manager: [" + buffer.toString() + "]");
	}
}
