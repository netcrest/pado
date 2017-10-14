package com.netcrest.pado.test.rpc.client.python;

import org.junit.Test;

import com.netcrest.pado.rpc.client.biz.PathBiz;
import com.netcrest.pado.rpc.client.dna.RpcInvokerDna;
import com.netcrest.pado.test.rpc.client.PathBiz1Put;

/**
 * PathBiz1PutTest tests the RPC {@link PathBiz} methods via {@link RpcInvokerDna}. This
 * test case requires the "nw" data distributed with Pado. Make sure to first
 * load that set of data in the "mygrid" environment.
 * 
 * @author dpark
 *
 */
public class PathBiz1PutTest extends PathBiz1Put
{	
	
	public PathBiz1PutTest()
	{
		super("python");
	}
	
	@Test
	public void testPut()
	{
		super.testPut();
	}

	@Test
	public void testPutAll()
	{
		super.testPutAll();
	}
}
