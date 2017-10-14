package com.netcrest.pado.rpc.client.impl;

import java.io.Serializable;

import com.netcrest.pado.rpc.client.IRpcContext;

public class RpcContextImpl implements IRpcContext, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private Object token;
	private String username;
	
	public RpcContextImpl(Object token, String username)
	{
		this.token = token;
		this.username = username;
	}
	
	@Override
	public Object getToken()
	{
		return this.token;
	}
	
	@Override
	public String getUsername()
	{
		return this.username;
	}
}
