package com.netcrest.pado.rpc.mqtt;

import java.io.IOException;
import java.util.HashMap;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.rpc.mqtt.client.ClientConstants;
import com.netcrest.pado.rpc.util.OsUtil;
import com.netcrest.pado.rpc.util.RpcUtil;
import com.netcrest.pado.server.PadoServerManager;

/**
 * MqttJsonRpcOsClient is a singleton class that executes OS-level commands for
 * launching RPC enabled client apps in the server-side.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes" })
public class MqttJsonRpcOsClient implements ClientConstants
{
	private final String RPC_CLIENT_COMMAND = "./rpc_client";

	private final static MqttJsonRpcOsClient rpcClient = new MqttJsonRpcOsClient();

	private HashMap<String, ThreadReply> idThreadMap = new HashMap<String, ThreadReply>(10);

	MqttJsonRpcListenerImpl listener;

	private MqttJsonRpcOsClient()
	{
	}

	/**
	 * Returns the MqttJsonRpcOsClient singleton object.
	 * 
	 * @return
	 */
	public static MqttJsonRpcOsClient getRpcClient()
	{
		return rpcClient;
	}

	/**
	 * Returns the OS command for the specified request to execute.
	 * 
	 * @param request
	 *            Request conforming to the Pado-extended JSON RPC 2.0 spec.
	 * @return Always non-null string array
	 */
	public String[] getLangCommand(JsonLite request)
	{
		return new String[] { RPC_CLIENT_COMMAND, PadoServerManager.getPadoServerManager().getServerName(),
				request.toString() };
	}

	/**
	 * Returns the working directory of the specified request.
	 * 
	 * @param request
	 *            Request conforming to the Pado-extended JSON RPC 2.0 spec.
	 * @return If the "lang" parameter in the request is not specified then it
	 *         returns the default "java" working directory path.
	 */
	public String getWorkingDir(JsonLite request)
	{
		String lang = request.getString(RequestKey.lang.name(), "java");
		return PadoUtil.getProperty(com.netcrest.pado.internal.Constants.PROP_HOME_DIR) + "/lang/" + lang + "/bin_sh";
	}

	/**
	 * Executes the specified request. The "timeout" parameter in the request is
	 * handled as follows:
	 * <p>
	 * <ul>
	 * <b>"timeout"</b> 0 or less for no timeout, a positive value for timeout
	 * in msec, if not specified then {@link Constants#DEFAULT_TIMEOUT_IN_MSEC}.
	 * </ul>
	 * 
	 * @param token
	 *            User session token
	 * @param request
	 *            Request conforming to the Pado-extended JSON RPC 2.0 spec.
	 * 
	 * @return Reply of request execution
	 */
	@SuppressWarnings("unchecked")
	public JsonLite execute(Object token, JsonLite request)
	{
		if (request == null) {
			return null;
		}
		JsonLite reply = null;

		String idOriginal = request.getString(RequestKey.id.name(), null);
		if (idOriginal != null) {

			// A new id specific to this server must be used in case multiple
			// servers are running in the same machine.
			String idTarget = idOriginal + PadoServerManager.getPadoServerManager().getServerName();
			request.put(RequestKey.id.name(), idTarget);
			ThreadReply threadReply = new ThreadReply(Thread.currentThread());
			idThreadMap.put(idTarget, threadReply);
			boolean isAgent = request.getBoolean(RequestKey.agent.name(), true);

			// Add the token/username in the request
			request.put(RequestKey.token.name(), token);
			request.put(RequestKey.username.name(), PadoServerManager.getPadoServerManager().getUsername(token));

			try {
				if (isAgent) {
					// Agent requests go directly on the MQTT agent request
					// topic (from server to rpc_agent)
					String lang = request.getString(RequestKey.lang.name(), "java");
					String agentRequestTopic = RpcUtil.getAgentRequestTopic(lang,
							PadoServerManager.getPadoServerManager().getServerName());
					MqttJsonRpcListenerImpl.getMqttJsonRpcListenerImpl().publish(agentRequestTopic, request);

				} else {

					// Non-agent requests executes CLI
					OsUtil.CommandOutput output = null;
					try {
						String workingDir = getWorkingDir(request);
						String[] langCommand = getLangCommand(request);
						output = OsUtil.executeCommand(langCommand, workingDir, null, false);
					} catch (IOException | InterruptedException e) {
						Logger.error("Error occured while executing an OS command: request=" + request);
					}
					// Note that output is always null if the isOutput argument
					// of OsUtil.executeCommand() is false.
					if (output != null) {
						String result = output.getOutput();
						Logger.info(result);
					}
				}
				reply = (JsonLite) threadReply.reply;
				int timeout = request.getInt(RequestKey.timeout.name(), Constants.DEFAULT_TIMEOUT_IN_MSEC);

				synchronized (threadReply) {
					if (threadReply.reply == null) {
						if (timeout < 0) {
							timeout = 0;
						}
						threadReply.wait(timeout);
					}
					reply = (JsonLite) threadReply.reply;
				}
			} catch (Exception e) {
				Logger.warning(e);
				reply = RpcUtil.createReply(request, -1010,
						"Error occourred while executing JSON RPC request in data node: " + e.getMessage(), null);
			} finally {
				idThreadMap.remove(idTarget);
			}
		}

		if (reply == null) {
			reply = new JsonLite();
			reply.put(ReplyKey.id.name(), idOriginal);
		}
		reply.put(ReplyKey.gid.name(), PadoServerManager.getPadoServerManager().getGridId());
		reply.put(ReplyKey.sid.name(), PadoServerManager.getPadoServerManager().getServerName());
		return reply;
	}

	/**
	 * Notifies the request thread that awaits on the specified ID. This method
	 * should be invoked upon receiving results from the client app.
	 * 
	 * @param id
	 *            The "id" parameter of JSON RPC 2.0 request
	 * @param reply
	 *            Reply received from the client app which contains "result"
	 */
	public void notifyReply(String id, Object reply)
	{
		ThreadReply threadReply = idThreadMap.get(id);
		if (threadReply == null) {
			return;
		}
		synchronized (threadReply) {
			threadReply.reply = reply;
			threadReply.notify();
		}
	}

	/**
	 * ThreadReply holds a thread that initiated a request. It is used as a lock
	 * to wait for a reply notification raised when the client app sends a
	 * "result".
	 * 
	 * @author dpark
	 *
	 */
	class ThreadReply
	{
		ThreadReply(Thread thread)
		{
			this.thread = thread;
		}

		Thread thread;

		/**
		 * Reply set by the
		 * {@link MqttJsonRpcOsClient#notifyReply(String, Object)} method
		 * invoked by the RPC mechanism when it receives a "result" message. It
		 * is volatile to handle RPC's asynchronous messaging.
		 */
		volatile Object reply;
	}
}
