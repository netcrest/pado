package com.netcrest.pado.rpc.client.dna;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.IRpc;
import com.netcrest.pado.rpc.mqtt.ReplyKey;
import com.netcrest.pado.rpc.mqtt.RequestKey;
import com.netcrest.pado.rpc.mqtt.client.MqttJsonRpcClient;

/**
 * RpcInvoker directly invokes {@link IRpc} implementation class methods.
 * Typically, a biz wrapper class that hides the RPC details is used to invoke
 * {@link IRpc} objects, instead. Although this class can directly be used by
 * applications, it is more appropriate for remotely testing {@link IRpc}
 * classes.
 * 
 * @author dpark
 *
 */
public class RpcInvoker
{
	/**
	 * Invokes the IRpc method included in the specified parameters.
	 * 
	 * @param params
	 *            JSON object containing the following parameters:
	 *            <ul>
	 *            <li>classname: Fully-qualified IRpc implementation class
	 *            name</li>
	 *            <li>method: Name of the method to invoke</li>
	 *            <li>params: Parameters in JSON object</li>
	 *            <ul>
	 * @return Returned value of the IRpc method call, null if the class name or
	 *         method is not defined in the parameters, null if the method has
	 *         the return type of void.
	 */
	@SuppressWarnings("rawtypes")
	public Object invoke(JsonLite params)
	{
		System.out.println("RpcInvoker.invoke() entered");
		String bizImplClassName = params.getString(RequestKey.classname.name(), null);
		String method = params.getString(RequestKey.method.name(), null);
		JsonLite bizImplParams = (JsonLite) params.get(RequestKey.params.name());
		int timeout = params.getInt(RequestKey.timeout.name(), 1000);
		if (bizImplClassName == null) {
			return null;
		}
		if (method == null) {
			return null;
		}
		
		System.out.println("RpcInvoker.invoke() invoking RPC...");

		JsonLite jl = MqttJsonRpcClient.getRpcClient().execute(bizImplClassName, method, bizImplParams, timeout);
		System.out.println("RpcInvoker.invoke(): reply=" + jl);
		if (jl == null) {
			return null;
		}
		Object result = jl.get(ReplyKey.result.name());
		System.out.println("RpcInvoker.invoke(): result=" + result);
		return result;
	}
}
