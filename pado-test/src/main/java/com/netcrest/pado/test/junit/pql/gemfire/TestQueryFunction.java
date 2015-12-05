/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Netcrest
 * Technologies, LLC. Your use of this software is strictly bounded to the terms
 * of the license agreement you established with Netcrest Technologies, LLC.
 * Redistribution and use in source and binary forms, with or without
 * modification, are strictly enforced by such a written license agreement,
 * which you must obtain from Netcrest Technologies, LLC prior to your action.
 */
package com.netcrest.pado.test.junit.pql.gemfire;

import java.util.Properties;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.query.QueryException;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;

public class TestQueryFunction implements Function, Declarable {

	private DefaultQuery query;

	private static final String ID = "query-function";

	private static final String QUERY = "SELECT o FROM /customer c, /order o WHERE c.id = o.customerId AND c.id = $1";

	public void execute(FunctionContext context) {
		RegionFunctionContext rfc = (RegionFunctionContext) context;

		// Get the customer id filter
		String customerId = (String) rfc.getFilter().iterator().next();

		// Execute the query and return the results
		try {
			SelectResults results = (SelectResults) this.query.execute(rfc, new String[] { customerId });
			context.getResultSender().lastResult(results.asList());
		} catch (QueryException e) {
			e.printStackTrace();
			context.getResultSender().sendException(e);
		}
	}

	public String getId() {
		return ID;
	}

	public boolean optimizeForWrite() {
		return true;
	}

	public boolean hasResult() {
		return true;
	}

	public boolean isHA() {
		return true;
	}

	public void init(Properties properties) {
		// Create the query
		QueryService queryService = CacheFactory.getAnyInstance().getQueryService();
		this.query = (DefaultQuery) queryService.newQuery(QUERY);
	}
}
