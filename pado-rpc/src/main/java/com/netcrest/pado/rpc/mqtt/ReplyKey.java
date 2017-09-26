package com.netcrest.pado.rpc.mqtt;

import com.netcrest.pado.data.jsonlite.JsonLite;

/**
 * Reply parameters supported by JSON RPC 2.0 and the Pado extension.
 * 
 * @author dpark
 *
 */
public enum ReplyKey
{
	jsonrpc, result, error(JsonLite.class), __error(JsonLite.class), id, code(int.class), message, data, gid, sid;

	private ReplyKey()
	{
		this.type = String.class;
	}

	private ReplyKey(String name)
	{
		this(String.class);
	}

	private ReplyKey(Class<?> type)
	{
		this.type = type;
	}

	private Class<?> type;

	/**
	 * Returns the expected value type.
	 */
	public Class<?> type()
	{
		return type;
	}
}
