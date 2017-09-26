package com.netcrest.pado.rpc.mqtt.client;

/**
 * IRpcListener receives streamed messages from the data node.
 * 
 * @author dpark
 *
 */
public interface IRpcListener
{
	/**
	 * Invoked when a new message arrives from the data node.
	 * 
	 * @param message
	 *            Messages may come in different forms depending on listener
	 *            services. In most cases, the message has the JsonLite type.
	 */
	void messageReceived(Object message);
}
