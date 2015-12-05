/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.demo.bank.market;

import java.io.IOException;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheException;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.query.CqAttributes;
import com.gemstone.gemfire.cache.query.CqAttributesFactory;
import com.gemstone.gemfire.cache.query.CqEvent;
import com.gemstone.gemfire.cache.query.CqListener;
import com.gemstone.gemfire.cache.query.CqQuery;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;

public class CqConsumer extends ClientBase
{
	public CqConsumer() throws Exception
	{
		// Login to pado
		super.login();

		// Initialize CQ
		initCq();
	}

	public void initCq() throws Exception
	{
		// The parameter itself needs to be a collection, and the query engine
		// allows arrays where it expects a collection.
		String queryStringKeyMap = "select * from /equity/level2/keymap where get('Date') <= TO_DATE('2008-06-08','yyyy-mm-dd')";
		String queryStringPojo = "select * from /equity/level2/pojo where \"date\" <= TO_DATE('2008-06-08','yyyy-mm-dd')";
		
		String queryStringOrderKeyMap = "select * from /equity/order/keymap";
		String queryStringOrderPojo = "select * from /equity/order/pojo";

		// CQ is not currently supported by IBiz. So let'st just access the
		// GemFire API directly.
		Cache cache = CacheFactory.getAnyInstance();
		QueryService queryService = cache.getQueryService();
		System.out.println();
		System.out.println(queryStringKeyMap);
		System.out.println(queryStringPojo);
		System.out.println(queryStringOrderKeyMap);
		System.out.println(queryStringOrderPojo);
		System.out.println();

		CqAttributesFactory factory = new CqAttributesFactory();
		factory.addCqListener(new CqListener() {
			public void onError(CqEvent cqEvent)
			{
				System.out.println(cqEvent);
			}

			public void onEvent(CqEvent cqEvent)
			{
				System.out.println(cqEvent.getNewValue());
			}

			public void close()
			{
				System.out.println("CqListener.close()");
			}
		});
		CqAttributes cqAttr = factory.create();
		CqQuery cqQuery = queryService.newCq(queryStringKeyMap, cqAttr);
		SelectResults sr = cqQuery.executeWithInitialResults();
		System.out.println(sr);
		cqQuery = queryService.newCq(queryStringPojo, cqAttr);
		sr = cqQuery.executeWithInitialResults();
		System.out.println(sr);
		cqQuery = queryService.newCq(queryStringOrderKeyMap, cqAttr);
		sr = cqQuery.executeWithInitialResults();
		System.out.println(sr);
		cqQuery = queryService.newCq(queryStringOrderPojo, cqAttr);
		sr = cqQuery.executeWithInitialResults();
		System.out.println(sr);
	}

	public static void main(String args[]) throws Exception
	{
		CqConsumer consumer;
		try {
			consumer = new CqConsumer();
			System.out.println();
			System.out.println("Market Data CqConsumer Ready.");
			System.out.println();
			consumer.waitForever();
		} catch (CacheException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
