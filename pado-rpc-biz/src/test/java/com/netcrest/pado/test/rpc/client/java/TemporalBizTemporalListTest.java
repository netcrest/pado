package com.netcrest.pado.test.rpc.client.java;

import org.junit.Test;

import com.netcrest.pado.rpc.client.biz.TemporalBiz;
import com.netcrest.pado.rpc.client.dna.RpcInvokerDna;
import com.netcrest.pado.test.rpc.client.TemporalBizTemporalList;

/**
 * TemporalBizTemporalListTest tests the RPC {@link TemporalBiz} query methods via
 * {@link RpcInvokerDna}. This test case requires the "nw" data distributed with
 * Pado. Make sure to first load that set of data in the "mygrid" environment.
 * 
 * @author dpark
 *
 */
public class TemporalBizTemporalListTest extends TemporalBizTemporalList
{
	public TemporalBizTemporalListTest()
	{
		super("java");
	}
	
	@Test
	public void testGetTemporalLists()
	{
		super.testGetTemporalLists();
	}
	
	@Test
	public void testDumpTemporalLists()
	{
		super.testDumpTemporalLists();
	}
	
	@Test
	public void testDumpAllTemporalLists()
	{
		super.testDumpAllTemporalLists();
	}
}
