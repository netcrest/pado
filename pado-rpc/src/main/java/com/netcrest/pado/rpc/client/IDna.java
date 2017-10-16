package com.netcrest.pado.rpc.client;

import com.netcrest.pado.rpc.client.IRpcContext;

/**
 * IDna must be implemented by all DNA implementation classes that are to be
 * executed in data nodes. Note that IDna is for client apps and {@link IRpc} is
 * for server plugins.
 * 
 * @author dpark
 *
 */
public interface IDna
{
	/**
	 * Invoked when the DNA object is created by the underlying client RPC
	 * mechanism.
	 * 
	 * @param rpcContext
	 *            RPC context object required when communicating with the data
	 *            node (server).
	 */
	void init(IRpcContext rpcContext);
}
