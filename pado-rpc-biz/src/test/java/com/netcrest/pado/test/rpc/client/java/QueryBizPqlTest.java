package com.netcrest.pado.test.rpc.client.java;

import org.junit.Test;

import com.netcrest.pado.rpc.client.biz.QueryBiz;
import com.netcrest.pado.rpc.client.dna.RpcInvokerDna;
import com.netcrest.pado.test.rpc.client.QueryBizPql;

/**
 * QueryBizPqlTest tests the RPC {@link QueryBiz} "executePql" method via
 * {@link RpcInvokerDna}. This test case requires the "nw" data distributed with
 * Pado. Make sure to first load that set of data in the "mygrid" environment.
 * 
 * @author dpark
 *
 */
public class QueryBizPqlTest extends QueryBizPql
{
	public QueryBizPqlTest()
	{
		super("java");
	}
	@Test
	public void testExecutePql()
	{
		super.testExecutePql();
	}
}
