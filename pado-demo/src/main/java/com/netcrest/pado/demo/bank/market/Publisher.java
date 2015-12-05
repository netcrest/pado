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

import java.io.File;

import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.demo.bank.market.data.CompanyInfo;
import com.netcrest.pado.demo.bank.market.data.Level2Data;
import com.netcrest.pado.demo.bank.market.data.Level2KeyType;
import com.netcrest.pado.demo.bank.market.data.Level2Publisher;
import com.netcrest.pado.internal.util.QueueDispatcherListener;

public class Publisher extends ClientBase implements QueueDispatcherListener
{
	private Level2Publisher publisher;
	private int objectDispatchedCount = 0;
	private IGridMapBiz level2MapBiz;

	public Publisher() throws Exception
	{
		init();
	}

	private void init() throws Exception
	{
		boolean keyMapPublisherEnabled = Boolean.getBoolean("mapLitePublisherEnabled");
		if (keyMapPublisherEnabled) {
			publisher = new Level2KeyMapPublisher();
		} else {
			publisher = new Level2Publisher();
		}
		publisher.setQueueDispatchListener(this);

		String companyInfoPath = System.getProperty("companyInfo", "data/demo/SnP500CompanyInfo.txt");
		File companyInfoFile = new File(System.getProperty("user.dir"), companyInfoPath);

		// Login to pado
		login();

		IGridMapBiz companyMapBiz = (IGridMapBiz) pado.getCatalog().newInstance(IGridMapBiz.class);

		if (keyMapPublisherEnabled) {
			companyMapBiz.setGridPath("company/keymap");
			KeyMap keyMaps[] = CompanyInfo.loadCompnayInfoKeyMap(companyInfoFile, companyMapBiz);
		} else {
			companyMapBiz.setGridPath("company/pojo");
			CompanyInfo companyInfo[] = CompanyInfo.loadCompnayInfo(companyInfoFile, companyMapBiz);
		}

		level2MapBiz = (IGridMapBiz) pado.getCatalog().newInstance(IGridMapBiz.class);
		level2MapBiz.setGridPath("level2");
		if (keyMapPublisherEnabled) {
			level2MapBiz.setGridPath("level2/keymap");
		} else {
			level2MapBiz.setGridPath("level2/pojo");
		}
	}

	public void start() throws Exception
	{
		publisher.start();
	}

	/**
	 * Receives all Level1Data objects dispatched by the publisher. It feeds the
	 * Pado grid with the level2 data.
	 */
	public void objectDispatched(Object obj)
	{
		if (obj instanceof Level2Data) {
			Level2Data level2Data = (Level2Data) obj;
			level2MapBiz.put(level2Data.getSymbol(), level2Data);
		} else {
			KeyMap keyMap = (KeyMap) obj;
			level2MapBiz.put(keyMap.get(Level2KeyType.KSymbol), keyMap);
		}
		objectDispatchedCount++;
		if (objectDispatchedCount % 1000 == 0) {
			if (obj instanceof Level2Data) {
				System.out.println("Level2Data count = " + objectDispatchedCount);
			} else {
				System.out.println("KeyMap count = " + objectDispatchedCount);
			}
		}
	}

	public static void main(String args[]) throws Exception
	{
		Publisher publisher = new Publisher();
		publisher.start();
	}
}
