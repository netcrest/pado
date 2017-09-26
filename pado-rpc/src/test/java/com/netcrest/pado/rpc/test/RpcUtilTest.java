package com.netcrest.pado.rpc.test;

import org.junit.Assert;
import org.junit.Test;

import com.netcrest.pado.rpc.util.RpcUtil;


public class RpcUtilTest
{
	@Test
	public void testGetBizClassName()
	{
		String bizImplClassName = "com.netcrest.pado.rpc.client.biz.impl.gemfire.PathRpcBizImpl";
		String actual = RpcUtil.getBizClassName(bizImplClassName);
		Assert.assertEquals("com.netcrest.pado.rpc.client.biz.PathRpcBiz", actual);
		
		bizImplClassName = "impl.gemfire.PathRpcBizImpl";
		actual = RpcUtil.getBizClassName(bizImplClassName);
		Assert.assertEquals("PathRpcBiz", actual);
	}

	
	@Test
	public void testGetBizImplClassName()
	{
		String bizClassName = "com.netcrest.pado.rpc.client.biz.PathRpcBiz";
		String actual = RpcUtil.getBizImplClassName(bizClassName);
		Assert.assertEquals("com.netcrest.pado.rpc.client.biz.impl.gemfire.PathRpcBizImpl", actual);
		
		bizClassName = "PathRpcBiz";
		actual = RpcUtil.getBizImplClassName(bizClassName);
		Assert.assertEquals("impl.gemfire.PathRpcBizImpl", actual);
	}
}
