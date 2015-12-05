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

import java.util.Date;
import java.util.Random;

import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.demo.bank.market.data.OrderInfo;
import com.netcrest.pado.demo.bank.market.data.OrderInfoKeyType;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;

/**
 * Publisher publishes Message objects as fast as possible.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class OrderPublisher extends ClientBase
{
	protected static Random random = new Random();

	protected static int messagesToPublish = Integer.MAX_VALUE;

	protected static int totalKeys = 1000;

	protected static boolean keyMapPublisherEnabled = false;

	protected static boolean useMessagePool = false;

	protected static int rateMessageCount = 10000;

	protected static String keyPrefix = "";

	private IGridMapBiz orderMapBiz;
	private OrderInfo orderInfoPool[];
	private KeyMap orderInfoKeyMapPool[];

	public OrderPublisher() throws PadoException, PadoLoginException
	{
		init();

		// Login to pado
		login();

		// Retrieve the region
		orderMapBiz = (IGridMapBiz) pado.getCatalog().newInstance(IGridMapBiz.class);
		orderMapBiz.getBizContext().getGridContextClient().setGridIds(gridId);
		if (keyMapPublisherEnabled) {
			orderMapBiz.setGridPath("order/keymap");
		} else {
			orderMapBiz.setGridPath("order/pojo");
		}

		printConfig();
	}

	private void init()
	{
		String keyMapTypeProp = System.getProperty("keyMapType", "maplite");
		keyMapPublisherEnabled = Boolean.getBoolean("mapLitePublisherEnabled");
		useMessagePool = Boolean.getBoolean("useMessagePool");
		rateMessageCount = Integer.getInteger("rateMessageCount", rateMessageCount).intValue();
		keyPrefix = System.getProperty("keyPrefix", keyPrefix);

		totalKeys = Integer.getInteger("totalKeys", totalKeys).intValue();
	}

	protected void printConfig()
	{
		System.out.println();
		System.out.println("Publisher Data Feed Configuration");
		System.out.println("=================================");
		System.out.println();
		System.out.println("    keyMapPublisherEnabled = " + keyMapPublisherEnabled);
		System.out.println("            useMessagePool = " + useMessagePool);
		System.out.println("          rateMessageCount = " + rateMessageCount);
		System.out.println("                 keyPrefix = " + keyPrefix);
		System.out.println("                 totalKeys = " + totalKeys);
		System.out.println("         messagesToPublish = " + messagesToPublish);
		System.out.println();
	}

	protected void publishMessages() throws Exception
	{
		System.out.println("Publishing data...");
		System.out.println();
		if (keyMapPublisherEnabled) {
			if (useMessagePool) {
				createOrderInfoKeyMapPool();
				publishKeyMap_pool();
			} else {
				publishKeyMap_nopool();
			}

		} else {
			if (useMessagePool) {
				createOrderInfoPool();
				publishMessages_pool();
			} else {
				publishMessages_nopool();
			}
		}
	}

	protected void publishMessages_pool()
	{
		int count = 0;
		OrderInfo orderInfo;
		long rate;
		long endTime = 0;
		long startTime = System.currentTimeMillis();
		while (count < messagesToPublish) {
			orderInfo = getOrderInfoFromPool();
			orderInfo.setHostCounter(count);

			// Publish it into the region
			orderMapBiz.put(orderInfo.getClientOrdId(), orderInfo);

			count++;

			if (count % rateMessageCount == 0) {
				endTime = System.currentTimeMillis();
				rate = 1000 * rateMessageCount / (endTime - startTime);
				System.out.println("Publish Rate: " + rate + " msg/sec");
				startTime = System.currentTimeMillis();
			}
		}
	}

	protected void publishKeyMap_pool()
	{
		int count = 0;
		KeyMap orderInfo;
		long rate;
		long endTime = 0;
		long startTime = System.currentTimeMillis();
		while (count < messagesToPublish) {
			orderInfo = getOrderInfoKeyMapFromPool();
			orderInfo.put(OrderInfoKeyType.KHostCounter, count);

			// Publish it into the region
			orderMapBiz.put(orderInfo.get(OrderInfoKeyType.KClientOrdId), orderInfo);

			count++;

			if (count % rateMessageCount == 0) {
				endTime = System.currentTimeMillis();
				rate = 1000 * rateMessageCount / (endTime - startTime);
				System.out.println("Publish Rate: " + rate + " msg/sec");
				startTime = System.currentTimeMillis();
			}
		}
	}

	protected void publishMessages_nopool()
	{
		int count = 0;
		OrderInfo orderInfo;
		long rate;
		long endTime = 0;
		long startTime = System.currentTimeMillis();
		while (count < messagesToPublish) {
			orderInfo = createOrderInfo();
			orderInfo.setHostCounter(count);

			// Publish it into the region
			String key = getKey(getIntKey());
			orderInfo.setClientOrdId(key);
			orderMapBiz.put(orderInfo.getClientOrdId(), orderInfo);

			count++;

			if (count % rateMessageCount == 0) {
				endTime = System.currentTimeMillis();
				rate = 1000 * rateMessageCount / (endTime - startTime);
				System.out.println("Publish Rate: " + rate + " msg/sec");
				startTime = System.currentTimeMillis();
			}
		}
	}

	protected void publishKeyMap_nopool()
	{
		int count = 0;
		KeyMap orderInfo;
		long rate;
		long endTime = 0;
		long startTime = System.currentTimeMillis();
		while (count < messagesToPublish) {
			orderInfo = createOrderInfoKeyMap();
			orderInfo.put(OrderInfoKeyType.KHostCounter, count);

			// Publish it into the region
			String key = getKey(getIntKey());
			orderInfo.put(OrderInfoKeyType.KClientOrdId, key);
			orderMapBiz.put(orderInfo.get(OrderInfoKeyType.KClientOrdId), orderInfo);

			count++;

			if (count % rateMessageCount == 0) {
				endTime = System.currentTimeMillis();
				rate = 1000 * rateMessageCount / (endTime - startTime);
				System.out.println("Publish Rate: " + rate + " msg/sec");
				startTime = System.currentTimeMillis();
			}
		}
	}

	protected String getKey()
	{
		return keyPrefix + String.valueOf(random.nextInt(totalKeys));
	}

	protected String getKey(int key)
	{
		return keyPrefix + key;
	}

	protected int getIntKey()
	{
		return random.nextInt(totalKeys);
	}

	/**
	 * 
	 */
	private static int orderNum = 1;

	public static OrderInfo createOrderInfo()
	{
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setOrigGroupAcronym(getRandomString(10, 1));
		orderInfo.setConnectAcronym(getRandomString(4, 1));
		orderInfo.setTargetGroupAcronym(getRandomString(4, 1));
		orderInfo.setSymbol(getRandomString(5, 1).toUpperCase());
		orderInfo.setClientOrdId("" + (orderNum++));
		orderInfo.setHandleInst(random.nextInt(1000));
		orderInfo.setSide(random.nextInt(2));
		orderInfo.setOrdType(random.nextInt(5));
		orderInfo.setHostCounter(random.nextInt(10));
		orderInfo.setClientSessionId(random.nextInt(100000));
		orderInfo.setTimeStamp(new Date());
		orderInfo.setTransactionTime(new Date());

		return orderInfo;
	}

	public static KeyMap createOrderInfoKeyMap()
	{
		KeyMap orderInfo = new JsonLite(OrderInfoKeyType.getKeyType());
		orderInfo.put(OrderInfoKeyType.KOrigGroupAcronym, getRandomString(10, 1));
		orderInfo.put(OrderInfoKeyType.KConnectAcronym, getRandomString(4, 1));
		orderInfo.put(OrderInfoKeyType.KTargetGroupAcronym, getRandomString(4, 1));
		orderInfo.put(OrderInfoKeyType.KSymbol, getRandomString(5, 1).toUpperCase());
		orderInfo.put(OrderInfoKeyType.KClientOrdId, "" + (orderNum++));
		orderInfo.put(OrderInfoKeyType.KHandleInst, random.nextInt(1000));
		orderInfo.put(OrderInfoKeyType.KSide, random.nextInt(2));
		orderInfo.put(OrderInfoKeyType.KOrdType, random.nextInt(5));
		orderInfo.put(OrderInfoKeyType.KHostCounter, random.nextInt(10));
		orderInfo.put(OrderInfoKeyType.KClientSessionId, random.nextInt(100000));
		orderInfo.put(OrderInfoKeyType.KTimeStamp, new Date());
		orderInfo.put(OrderInfoKeyType.KTransactionTime, new Date());

		return orderInfo;
	}

	private static String getRandomString(int maxCharactersPerWord, int maxWords)
	{
		int stringLen = random.nextInt(maxWords) + 1;
		StringBuffer buffer = new StringBuffer(stringLen * 10);

		for (int i = 0; i < stringLen; i++) {
			int wordLen = random.nextInt(maxCharactersPerWord - 1) + 1;
			char[] word = new char[wordLen];
			for (int j = 0; j < wordLen; j++) {
				int val = random.nextInt(26) + 97; // a-z
				word[j] = (char) val;
			}
			buffer.append(word);
			buffer.append(" ");
		}
		return buffer.toString().trim();
	}

	private void createOrderInfoPool()
	{
		orderInfoPool = new OrderInfo[totalKeys];
		for (int i = 0; i < totalKeys; i++) {
			orderInfoPool[i] = createOrderInfo();
			orderInfoPool[i].setClientOrdId(getKey(i));
		}
	}

	private void createOrderInfoKeyMapPool()
	{
		orderInfoKeyMapPool = new KeyMap[totalKeys];
		for (int i = 0; i < totalKeys; i++) {
			orderInfoKeyMapPool[i] = createOrderInfoKeyMap();
			orderInfoKeyMapPool[i].put(OrderInfoKeyType.KClientSessionId, getKey(i));
		}
	}

	private OrderInfo getOrderInfoFromPool()
	{
		int intKey = getIntKey();
		OrderInfo orderInfo = orderInfoPool[intKey];
		return orderInfo;
	}

	private KeyMap getOrderInfoKeyMapFromPool()
	{
		int intKey = getIntKey();
		KeyMap orderInfo = orderInfoKeyMapPool[intKey];
		return orderInfo;
	}

	public static void main(String[] args) throws Exception
	{
		OrderPublisher feeder = new OrderPublisher();
		feeder.publishMessages();
		feeder.waitForever();
	}
}