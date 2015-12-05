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

import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;

public class Consumer extends ClientBase
{
	public Consumer() throws PadoException, PadoLoginException
	{
		// Login to pado
		login();

		// Start consumer
		start();
	}

	private void start()
	{
		IGridMapBiz gridMapBiz = (IGridMapBiz) pado.getCatalog().newInstance(IGridMapBiz.class);
		gridMapBiz.setGridPath("level2/keymap");
		gridMapBiz.addEntryListener(new ConsumerEntryListenerImpl());
		gridMapBiz.subscribeEntries(".*");
	
		gridMapBiz.setGridPath("level2/pojo");
		gridMapBiz.addEntryListener(new ConsumerEntryListenerImpl());
		gridMapBiz.subscribeEntries(".*");

		gridMapBiz.setGridPath("order/keymap");
		gridMapBiz.addEntryListener(new ConsumerEntryListenerImpl());
		gridMapBiz.subscribeEntries(".*");
		
		gridMapBiz.setGridPath("order/pojo");
		gridMapBiz.addEntryListener(new ConsumerEntryListenerImpl());
		gridMapBiz.subscribeEntries(".*");
		
		System.out.println();
		System.out.println("Market Data Consumer Ready.");
		System.out.println();
		waitForever();
	}

	public static void main(String args[]) throws Exception
	{
		new Consumer();
	}
}
