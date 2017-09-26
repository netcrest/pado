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

	private static void write(String str)
	{
		System.out.print(str);
	}

	private static void usage()
	{
		writeLine();
		writeLine("Usage:");
		writeLine("   RpcMain [-reply] [-?] <Pado extended JSON-RPC 2.0 request>");
		writeLine();
		writeLine("   RpcMain takes an extended JSON-RPC 2.0 request and invokes the specified");
		writeLine("   class' method.");
		writeLine();
		writeLine("      -reply If specified, then it prints the reply.");
		writeLine();
		writeLine("   Default: RpcMain <Pado extended JSON-RPC 2.0 request>");
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
		String request = "";
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-reply")) {
				isReply = true;
			} else {
				request = arg;
			}
		}

		request = request.trim();
		if (request.startsWith("{") == false || request.endsWith("}") == false) {
			System.err.println("Valid request must be specified. Aborted.");
			System.exit(-1);
		}

		RpcUtil.readRpcProperties();

		final JsonLite jrequest = new JsonLite(request);
		System.out.println("Request:");
		System.out.println(jrequest.toString());
		System.out.println(jrequest.toString(4, false, false));

		String className = jrequest.getString(RequestKey.classname.name(), null);
		try {
			Class clazz = Class.forName(className);
			final Object obj = clazz.newInstance();
			String methodName = jrequest.getString(RequestKey.method.name(), null);
			final Method method = clazz.getMethod(methodName, JsonLite.class);
			if (method != null) {
				final JsonLite params = (JsonLite) jrequest.get(RequestKey.params.name());
				System.out.println("method=" + method + ", obj=" + obj + ", params=" + params);

				boolean isDaemon = jrequest.getBoolean(RequestKey.daemon.name(), true);

				if (isDaemon) {
					try {
						JsonLite result = (JsonLite) method.invoke(obj, params);
						JsonLite reply;
						if (result != null) {
							JsonLite error = (JsonLite) result.get(ReplyKey.__error.name());
							if (error != null) {
								reply = RpcUtil.createReplyError(jrequest, error);
							} else {
								reply = RpcUtil.createReplyResult(jrequest, result);
							}
						} else {
							reply = RpcUtil.createReplyResult(jrequest, null);
						}
						System.out.println("RpcMain.main(): before result call: size=" + result.size());
						MqttJsonRpcClient.getRpcClient().sendResult(reply);
						System.out.println("RpcMain.main(): after result call: size=" + result.size());
						if (isReply) {
							System.out.println(reply.toString());
						}
					} catch (InvocationTargetException e) {
						Logger.error(e);
					}
				} else {
					Future<?> future = Executors.newSingleThreadExecutor().submit(new Callable() {
						@Override
						public Object call()
						{
							JsonLite result = null;
							try {
								result = (JsonLite) method.invoke(obj, params);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								Logger.error(e);
							}
							return result;
						}
					});

					JsonLite reply = RpcUtil.createReplyResult(jrequest, null);
					MqttJsonRpcClient.getRpcClient().sendResult(reply);
					if (isReply) {
						System.out.println(reply.toString());
					}

					// Block until the thread completes
					try {
						Object result = future.get();
						if (result != null) {
							Logger.info(result.toString());
						}
					} catch (InterruptedException | ExecutionException e) {
						Logger.error(e);
					}
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
				| SecurityException | IllegalArgumentException e) {
			Logger.error(e);
		}

		MqttJsonRpcClient.getRpcClient().close();
		System.exit(0);
	}
}
