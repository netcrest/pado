package com.netcrest.pado.rpc.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.rpc.mqtt.ReplyKey;
import com.netcrest.pado.rpc.mqtt.RequestKey;
import com.netcrest.pado.rpc.mqtt.client.MqttJsonRpcClient;
import com.netcrest.pado.rpc.util.RpcUtil;

/**
 * RpcMain is a client program for running in a data node where local server
 * service invocation is desired. It uses the Pado's RPC mechanism to locally
 * communicate with the data node server for accessing data and grid services.
 * It supports two different running modes as described below:
 * <p>
 * <b>Daemon (default):</b>
 * <p>
 * In the deamon mode, RpcMain runs in the main thread awaiting for the RPC
 * method invocation to complete. Upon completion, it sends the result to the
 * data node server (the caller) via the RPC mechanics. Note that because a
 * method may take a long time to complete, the default timeout is enforced by
 * the caller to protect itself from runaway RPC calls. If a method call times
 * out then the caller returns the result as null.
 * <p>
 * <b>Non-deamon:</b> Instead of running it in the main thread, in this mode,
 * the request is handled by a dedicated thread in which the method call is
 * made. Upond thread start, it immediately sends a result as null allowing the
 * caller to continue. This mode is desirable if the method takes a long time to
 * complete or runs indefinitely.
 * <p>
 * To specify non-daemon, set the "daemon" parameter in the request to false.
 * If this parameter is not specified then it is assigned to true by default.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RpcMain
{
	private static void writeLine()
	{
		System.out.println();
	}

	private static void writeLine(String line)
	{
		System.out.println(line);
	}

	private static void usage()
	{
		writeLine();
		writeLine("Usage:");
		writeLine("   RpcMain <server-id> <request> [-reply] [-?] ");
		writeLine();
		writeLine("   RpcMain takes an extended JSON-RPC 2.0 request and invokes the specified");
		writeLine("   class' method.");
		writeLine();
		writeLine("      <server-id> Unique Pado server ID representing the data node.");
		writeLine("      <request> Pado extended JSON-RPC 2.0. Must be enclosed in double quotes.");
		writeLine("      -reply If specified, then it prints the reply.");
		writeLine();
		writeLine("   Default: RpcMain <server-id> <request>");
		writeLine();
		System.exit(0);
	}
	
	public static void main(String[] args)
	{

		if (args == null || args.length == 0) {
			return;
		}

		String arg;
		boolean isReply = false;
		String serverId = null;
		String request = "";
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-reply")) {
				isReply = true;
			} else if (serverId == null) {
				serverId = arg;
			} else {
				request = arg;
			}
		}

		if (serverId == null) {
			System.err.println("Server ID not specified. Aborted.");
			System.exit(-1);
		}
		request = request.trim();
		if (request.startsWith("{") == false || request.endsWith("}") == false) {
			System.err.println("Valid request must be specified. Aborted.");
			System.exit(-2);
		}

		// Initializer the MQTT mechanics
		MqttJsonRpcClient.initialize(serverId);
		
		RpcUtil.processRequest(request, isReply);

		// Close MQTT
		MqttJsonRpcClient.getRpcClient().close();
		System.exit(0);
	}
}
