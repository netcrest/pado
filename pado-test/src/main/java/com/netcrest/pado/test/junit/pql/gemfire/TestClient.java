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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;

public class TestClient {

	private GemFireCache cache;

	private Region customerRegion;

	private Region orderRegion;

	private static final Random RANDOM = new Random();

	private static final int NUM_CUSTOMERS = 1000;

	private static final int NUM_ORDERS_PER_CUSTOMER = 10;

	public static void main(String[] args) throws Exception {
		TestClient client = new TestClient();

		// Initialize the cache
		client.initializeClientCache();

		// Retrieve the regions
		client.initializeRegions();

		String operation = args[0];
		if (operation.equals("load")) {
			client.loadEntries(NUM_CUSTOMERS);
		} else if (operation.equals("query-function")) {
			client.executeQueryFunction(NUM_CUSTOMERS);
		} else if (operation.equals("query-function-repeat")) {
			while (true) {
				client.executeQueryFunction(NUM_CUSTOMERS);
				Thread.sleep(10);
			}
		}
	}

	protected void loadEntries(int numEntries) throws Exception {
		long start = 0, end = 0;
		start = System.currentTimeMillis();
		for (int i = 0; i < numEntries; i++) {
			String customerId = String.valueOf(i);
			this.customerRegion.put(customerId, new Customer(customerId, "name-" + customerId));
			for (int j = 0; j < NUM_ORDERS_PER_CUSTOMER; j++) {
				String orderId = customerId + "-" + String.valueOf(j);
				this.orderRegion.put(orderId, new Order(orderId, customerId));
			}
		}
		end = System.currentTimeMillis();
		System.out.println("Loaded " + numEntries + " customers in " + (end - start) + " ms");
	}

	private void executeQueryFunction(int numEntries) {
		String customerId = String.valueOf(RANDOM.nextInt(numEntries));
		Execution execution = FunctionService.onRegion(this.customerRegion)
				.withFilter(Collections.singleton(customerId));
		ResultCollector collector = execution.execute("query-function", true /* hasResult */, true /* isHA */,
				true /* isOptimizeForWrite */);
		// Get the results which is a list from each member. In this case, the
		// function only goes to one member.
		List allResults = (List) collector.getResult();
		List results = (List) allResults.get(0);
		System.out.println("Retrieved the following " + results.size() + " orders for customer: " + customerId);
		for (Iterator i = results.iterator(); i.hasNext();) {
			System.out.println("\t" + i.next());
		}
	}

	private void initializeRegions() {
		this.customerRegion = this.cache.getRegion("/mock/customer");
		System.out.println("Retrieved region: " + this.customerRegion);
		this.orderRegion = this.cache.getRegion("/mock/order");
		System.out.println("Retrieved region: " + this.orderRegion);
	}

	private void initializeClientCache() {
		this.cache = new ClientCacheFactory().create();
		System.out.println("Created " + this.cache);
	}
}