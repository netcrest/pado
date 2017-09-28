package com.netcrest.pado.rpc.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.rpc.mqtt.Constants;
import com.netcrest.pado.rpc.mqtt.ReplyKey;
import com.netcrest.pado.rpc.mqtt.RequestKey;
import com.netcrest.pado.rpc.mqtt.client.MqttJsonRpcClient;

/**
 * RpcUtil provides convenience methods for handling Pado-extended JSON RPC 2.0
 * messages.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RpcUtil
{
	public final static String PROP_RPC_PROPERTIES_FILE = "rpcPropertyFile";
	public final static String DEFAULT_RPC_PROPERTIES_FILE_PATH = "etc/rpc.properties";

	/**
	 * Returns the agent request request topic.
	 * @param serverName Unique server name
	 */
	public final static String getAgentRequestTopic(String lang, String serverName)
	{
		return Constants.TOPIC_REQUEST_PREFIX + "/agent/" + lang + "/" + serverName;
	}
	
	/**
	 * Creates a request object for the specified function and parameters.
	 * 
	 * @param functionName
	 *            Function name
	 * @param params
	 *            Function parameters
	 * @return Request object
	 */
	public final static JsonLite createRequest(String functionName, JsonLite params)
	{
		return createRequest(null, functionName, params);
	}

	/**
	 * Creates a request object for the specified class' method with the
	 * specified parameters.
	 * 
	 * @param className
	 *            Class name.
	 * @param methodName
	 *            Method name. If className is null, then this argument is
	 *            equivalent to a function name.
	 * @param params Method or function parameters
	 */
	public final static JsonLite createRequest(String className, String methodName, JsonLite params)
	{
		JsonLite request = new JsonLite();
		request.put(RequestKey.id.name(), Long.toString(System.nanoTime()));
		request.put(RequestKey.jsonrpc.name(), "2.0");
		if (className != null) {
			request.put(RequestKey.classname.name(), className);
		}
		request.put(RequestKey.method.name(), methodName);
		if (params != null) {
			request.put(RequestKey.params.name(), params);
		}
		return request;
	}

	/**
	 * Creates a reply object with the specified "error" object conforming to
	 * JSON RPC 2.0.
	 * 
	 * @param request
	 *            JSON RPC 2.0 request
	 * @param error
	 *            Error to be included in the reply object
	 */
	public final static JsonLite createReplyError(JsonLite request, JsonLite error)
	{
		JsonLite reply = new JsonLite();
		reply.put(ReplyKey.id.name(), request.getString(RequestKey.id.name(), null));
		reply.put(ReplyKey.jsonrpc.name(), request.get(RequestKey.jsonrpc.name()));
		if (error != null) {
			reply.put(ReplyKey.error.name(), error);
		}
		return reply;
	}

	/**
	 * Creates a reply object with the specified "result" object conforming to
	 * JSON RPC 2.0.
	 * 
	 * @param request
	 *            JSON RPC 2.0 request
	 * @param result
	 *            Result to be included in the reply object.
	 */
	public final static JsonLite createReplyResult(JsonLite request, Object result)
	{
		JsonLite reply = new JsonLite();
		reply.put(ReplyKey.id.name(), request.getString(RequestKey.id.name(), null));
		reply.put(ReplyKey.jsonrpc.name(), request.get(RequestKey.jsonrpc.name()));
		if (result != null) {
			reply.put(ReplyKey.result.name(), result);
		}
		return reply;
	}

	/**
	 * Creates a reply object conforming to JSON RPC 2.0.
	 * 
	 * @param request
	 *            JSON RPC 2.0 request
	 * @param errorCode
	 *            Error code
	 * @param errorMessage
	 *            Error message
	 * @param errorData
	 *            Optional error data
	 */
	public final static JsonLite createReply(JsonLite request, int errorCode, String errorMessage, Object errorData)
	{
		JsonLite reply = new JsonLite();
		reply.put(ReplyKey.id.name(), request.getString(RequestKey.id.name(), null));
		reply.put(ReplyKey.jsonrpc.name(), request.get(RequestKey.jsonrpc.name()));
		JsonLite error = new JsonLite();
		error.put(ReplyKey.code.name(), errorCode);
		error.put(ReplyKey.message.name(), errorMessage);
		error.put(ReplyKey.data.name(), errorData);
		reply.put(ReplyKey.error.name(), error);
		return reply;
	}

	/**
	 * Creates an error object wrapper for client business objects. All client
	 * business objects must invoke this method to return an error message
	 * (instead of a result message) to the application.
	 * 
	 * @param errorCode
	 *            Error code, a negative number
	 * @param errorMessage
	 *            Error message
	 * @param errorData
	 *            Error data associated with the error message
	 * @return An error object in an internal format. The returned object is not
	 *         suitable for direct inclusion in JSON RPC 2.0 reply messages. The
	 *         returned object should be returned from a client business object.
	 */
	public final static JsonLite createErrorWrap(int errorCode, String errorMessage, Object errorData)
	{
		JsonLite retval = new JsonLite();
		JsonLite error = new JsonLite();
		error.put(ReplyKey.code.name(), errorCode);
		if (errorMessage != null) {
			error.put(ReplyKey.message.name(), errorMessage);
		}
		if (errorData != null) {
			error.put(ReplyKey.data.name(), errorData);
		}
		retval.put(ReplyKey.__error.name(), error);
		return retval;
	}

	/**
	 * Creates an error object wrapper for client business objects. All client
	 * business objects must invoke this method to return an error message
	 * (instead of a result message) to the application.
	 * 
	 * @param errorCode
	 *            Error code, a negative number
	 * @return An error object in an internal format. The returned object is not
	 *         suitable for direct inclusion in JSON RPC 2.0 reply messages. The
	 *         returned object should be returned from a client business object.
	 */
	public final static JsonLite createErrorWrap(int errorCode)
	{
		return createErrorWrap(errorCode, null, null);
	}

	/**
	 * Creates an error object wrapper for client business objects. All client
	 * business objects must invoke this method to return an error message
	 * (instead of a result message) to the application.
	 * 
	 * @param errorCode
	 *            Error code, a negative number
	 * @param errorMessage
	 *            Error message
	 * @return An error object in an internal format. The returned object is not
	 *         suitable for direct inclusion in JSON RPC 2.0 reply messages. The
	 *         returned object should be returned from a client business object.
	 */
	public final static JsonLite createErrorWrap(int errorCode, String errorMessage)
	{
		return createErrorWrap(errorCode, errorMessage, null);
	}

	/**
	 * Creates an error object wrapper for client business objects. All client
	 * business objects must invoke this method to return an error message
	 * (instead of a result message) to the application.
	 * 
	 * @param error
	 *            Error object containing the JSON RPC 2.0 error parameters
	 * @return An error object in an internal format. The returned object is not
	 *         suitable for direct inclusion in JSON RPC 2.0 reply messages. The
	 *         returned object should be returned from a client business object.
	 */
	public final static JsonLite createErrorWrap(JsonLite error)
	{
		JsonLite retval = new JsonLite();
		retval.put(ReplyKey.__error.name(), error);
		return retval;
	}

	/**
	 * Invokes the method specified in the specified request. If the method is
	 * not found in the request then it returns null.
	 * 
	 * @param obj
	 *            Object that has the method to invoke
	 * @param request
	 *            JSON RPC 2.0 request
	 * @return return value of the method call or null if the method is
	 *         undefined or has the void return type.
	 * @throws NoSuchMethodException
	 *             Thrown if the method is not defined in the object
	 * @throws SecurityException
	 *             Thrown if a security error occurs while invoking the method
	 * @throws IllegalAccessException
	 *             Thrown if the method cannot be accessed
	 * @throws IllegalArgumentException
	 *             Thrown if the parameters in the request do not match the
	 *             method argument list.
	 * @throws InvocationTargetException
	 *             Thrown if the method call throws an exception
	 */
	public final static Object invoke(Object obj, JsonLite request) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		if (obj == null) {
			return null;
		}
		String methodName = request.getString(RequestKey.method.name(), null);
		Method method = obj.getClass().getMethod(methodName, JsonLite.class);
		if (method == null) {
			return null;
		}
		Object retObj = method.invoke(obj, request.get(RequestKey.params.name()));
		return retObj;
	}

	/**
	 * Returns the RPC businesss object implementation class name for the
	 * specified client object class name.
	 * 
	 * @param bizClassName
	 * @return null if the specified class name is null or empty.
	 */
	public final static String getBizImplClassName(String bizClassName)
	{
		if (bizClassName == null) {
			return null;
		}
		bizClassName = bizClassName.trim();
		if (bizClassName.length() == 0) {
			return null;
		} else {
			String simpleName;
			String packageName;
			int index = bizClassName.lastIndexOf(".");
			if (index != -1) {
				packageName = bizClassName.substring(0, index) + ".impl.gemfire.";
				simpleName = bizClassName.substring(index + 1);
			} else {
				packageName = "impl.gemfire.";
				simpleName = bizClassName;
			}
			return packageName + simpleName + "Impl";
		}
	}

	/**
	 * Returns the RPC client object class name that for the specified
	 * implementation class name.
	 * 
	 * @param bizImplClassName
	 *            RPC business object implementation class name
	 * @return null if the specified implementation class name is a
	 *         non-conforming name.
	 */
	public final static String getBizClassName(String bizImplClassName)
	{
		if (bizImplClassName == null) {
			return null;
		}
		bizImplClassName = bizImplClassName.trim();
		if (bizImplClassName.length() == 0) {
			return null;
		} else {
			String simpleName;
			String packageName;
			int index = bizImplClassName.lastIndexOf(".impl.");
			if (index != -1) {
				packageName = bizImplClassName.substring(0, index) + ".";
			} else {
				index = bizImplClassName.lastIndexOf("impl.");
				if (index == -1) {
					return null;
				}
				packageName = "";
			}
			index = bizImplClassName.lastIndexOf(".");
			simpleName = bizImplClassName.substring(index + 1);
			index = simpleName.lastIndexOf("Impl");
			simpleName = simpleName.substring(0, index);
			return packageName + simpleName;
		}
	}

	/**
	 * Reads the rpc.properties file and sets system properties. This method
	 * should be used by rpc-client and invoked only once during startup to
	 * initialize the RPC mechanics.
	 */
	public final static Properties readRpcProperties()
	{
		String propertiesFilePath = System.getProperty(PROP_RPC_PROPERTIES_FILE, DEFAULT_RPC_PROPERTIES_FILE_PATH);
		Properties props = new Properties();
		File file = new File(propertiesFilePath);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			props.load(reader);
			// Retrieve all "javax" and "java" system properties. These are for
			// configuring SSL.
			Set<Map.Entry<Object, Object>> set = props.entrySet();
			for (Map.Entry<Object, Object> entry : set) {
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				if (key.startsWith("javax.") || key.startsWith("java.") || key.startsWith("pado.")) {
					System.setProperty(key, value);
				}
				// System.out.println(key + "=" + value);
			}
		} catch (FileNotFoundException e1) {
			System.err.println("rpc-client rpc.properties file not found: " + file.getAbsolutePath()
					+ ". Using defaults properties.");
		} catch (IOException e1) {
			System.err.println("IOExceptioh while reading rpc.properties file: " + file.getAbsolutePath()
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

		return props;
	}
	
	public final static void processRequest(String request, boolean isReply)
	{
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
	}

}
