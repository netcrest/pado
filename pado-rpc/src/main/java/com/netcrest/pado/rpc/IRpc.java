package com.netcrest.pado.rpc;

import com.netcrest.pado.rpc.client.IRpcContext;

/**
 * IRpc must be implemented by all RPC implementation classes that provide local
 * (server-side) services (aka plugins). All IRpc implementing classes are automatically
 * registered during server startup and hot-deployment.
 * 
 * @author dpark
 *
 */
public interface IRpc
{
	/**
	 * Invoked when an IRpc object is created by the underlying server RPC
	 * mechanism.
	 * 
	 * @param rpcContext
	 *            RPC context object required when communicating with the client
	 *            apps (DNAs).
	 */
	void init(IRpcContext rpcContext);
}
