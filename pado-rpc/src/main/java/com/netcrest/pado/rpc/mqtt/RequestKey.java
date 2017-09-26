package com.netcrest.pado.rpc.mqtt;

import com.netcrest.pado.data.jsonlite.JsonLite;

/**
 * Request parameters supported by JSON RPC 2.0 and the Pado extension.
 * 
 * @author dpark
 *
 */
public enum RequestKey {
	jsonrpc, method, params(JsonLite.class), id, classname, replytopic, lang, daemon(boolean.class), timeout(int.class);

	private RequestKey()
	{
		this.type = String.class;
	}

	private RequestKey(String name)
	{
		this(String.class);
	}

	private RequestKey(Class<?> type)
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
