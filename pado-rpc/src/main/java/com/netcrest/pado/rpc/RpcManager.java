package com.netcrest.pado.rpc;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.mqtt.RequestKey;
import com.netcrest.pado.rpc.util.RpcUtil;

/**
 * RpcManager manages {@link IRpc} classes by providing static methods to add a
 * new class, create a new instance, invoke a method specified a JSON object
 * that conforms to the Pado-extended JSON RPC 2.0 spec. IRpcBiz provides
 * details.
 * 
 * @author dpark
 *
 */
public class RpcManager
{
	private final static RpcManager manager = new RpcManager();

	private Map<String, IRpc> rpcMap = Collections.synchronizedMap(new HashMap<String, IRpc>(20));

	private Map<String, Class<?>> rpcClassMap = Collections.synchronizedMap(new HashMap<String, Class<?>>(20));

	private RpcManager()
	{
	}

	/**
	 * Returns the singleton RpcManager object.
	 */
	public final static RpcManager getRpcManager()
	{
		return manager;
	}

	public void addRpcClass(Class<?> clazz) throws InstantiationException, IllegalAccessException
	{
		if (clazz != null) {
			String rpcBizClassName = RpcUtil.getBizClassName(clazz.getName());
			rpcClassMap.put(rpcBizClassName, clazz);
			IRpc rpc = (IRpc) clazz.newInstance();
			rpcMap.put(rpcBizClassName, rpc);
		}
	}

	/**
	 * Returns a new instance of the specified IRpc class.
	 * 
	 * @param rpcClassName
	 *            IRpc implementation class with the default no-args
	 *            constructor.
	 * @return null if the specified class name is null
	 * @throws InstantiationException
	 *             Thrown if an error occurs while instantiating the class
	 * @throws IllegalAccessException
	 *             Thrown if a security error occurs
	 */
	public IRpc newInstance(String rpcClassName) throws InstantiationException, IllegalAccessException
	{
		if (rpcClassName != null) {
			Class<?> clazz = rpcClassMap.get(rpcClassName);
			if (clazz != null) {
				return (IRpc) clazz.newInstance();
			}
		}
		return null;
	}

	/**
	 * Invokes the IRpc class' method in the specified request.
	 * 
	 * @param request
	 *            Pado-extended JSON RPC 2.0 request.
	 * @return Pado-extended JSON RPC 2.0 reply that contains the "result"
	 *         parameter assigned to the method returned value. A reply may
	 *         contain the "error" parameter instead if the class instantiation
	 *         or method invokation fails.
	 */
	@SuppressWarnings("rawtypes")
	public JsonLite invoke(JsonLite request)
	{
		JsonLite reply = null;
		String className = request.getString(RequestKey.classname.name(), null);
		IRpc rpc = null;
		try {
			rpc = newInstance(className);
		} catch (Exception ex) {
			reply = RpcUtil.createReply(request, -1000, ex.getClass().getName() + ": " + ex.getMessage(), null);
		}

		if (rpc == null) {
			reply = RpcUtil.createReply(request, -1002, "Class undefined", null);
		} else {
			try {
				Object result = RpcUtil.invoke(rpc, request);
				reply = RpcUtil.createReplyResult(request, result);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				reply = RpcUtil.createReply(request, -1001, e.getClass().getName() + ": " + e.getMessage(), null);
			}
		}

		return reply;
	}
}
