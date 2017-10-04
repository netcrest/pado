package com.netcrest.pado.test.rpc.client.java;

import org.junit.Test;

import com.netcrest.pado.rpc.client.biz.PathBiz;
import com.netcrest.pado.rpc.client.dna.RpcInvoker;
import com.netcrest.pado.test.rpc.client.PathBiz2Get;

/**
 * PathBiz2GetTest tests the RPC {@link PathBiz} "get", "query", "size", and "dump"
 * methods via {@link RpcInvoker}. This test case requires the "nw" data
 * distributed with Pado. Make sure to first load that set of data in the
 * "mygrid" environment.
 * 
 * @author dpark
 *
 */
public class PathBiz2GetTest extends PathBiz2Get
{
	public PathBiz2GetTest()
	{
		super("java");
	}
	
	@Test
	public void testGet()
	{
		super.testGet();
	}

	@Test
	public void testGetAll()
	{
		super.testGetAll();
	}

	@Test
	public void testQuery()
	{
		super.testQuery();
	}

	@Test
	public void testDump()
	{
		super.testDump();
	}

	@Test
	public void testSize()
	{
		super.testSize();
	}
}
