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
import java.util.Random;

import com.gemstone.gemfire.cache.CacheException;
import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.demo.bank.market.data.Level2Data;
import com.netcrest.pado.demo.cache.CacheBase;

/**
 * Publisher publishes Level2 objects as fast as possible.
 */
public class Level2CannedPublisher extends CacheBase
{
	protected static Random random = new Random();

	protected static int messagesToPublish = Integer.MAX_VALUE;

	protected static int totalKeys = 1000;

	protected static boolean useMessagePool = false;

	protected static int rateMessageCount = 10000;

	protected static String keyPrefix = "";

	private Region region;
	private Level2Data level2DataPool[];

	public Level2CannedPublisher() throws CacheException, IOException
	{
		init();

		// Initialize the cache
		initializeCache();

		// Retrieve the region
		String level2Region = System.getProperty("level2Region", "/level2");
		region = cache.getRegion(level2Region);
		
		printConfig();
	}

	private void init()
	{
		useMessagePool = Boolean.getBoolean("useMessagePool");
		rateMessageCount = Integer.getInteger("rateMessageCount",
				rateMessageCount).intValue();
		keyPrefix = System.getProperty("keyPrefix", keyPrefix);

		totalKeys = Integer.getInteger("totalKeys", totalKeys).intValue();
	}
	
	
	protected void printConfig()
	{
		System.out.println("Publisher Data Feed Configuration");
		System.out.println("================================");
		System.out.println();
		System.out.println("      useMessagePool = " + useMessagePool);
		System.out.println("    rateMessageCount = " + rateMessageCount);
		System.out.println("           keyPrefix = " + keyPrefix);
		System.out.println("           totalKeys = " + totalKeys);
		System.out.println("   messagesToPublish = " + messagesToPublish);
		System.out.println();
	}
	
	protected void publishMessages() throws Exception
	{
		System.out.println("Publishing data...");
		System.out.println();
		if (useMessagePool) {
			createLevel2DataPool();
			publishMessages_pool();
		} else {
			publishMessages_nopool();
		}
	}

	protected void publishMessages_pool()
	{
		int count = 0;
		Level2Data level2Data;
		long rate;
		long endTime = 0;
		long startTime = System.currentTimeMillis();
		while (count < messagesToPublish) {
			level2Data = getLevel2DataFromPool();
			level2Data.setSeqNum(count);
			
			// Publish it into the region
			region.put(level2Data.getSymbol(), level2Data);

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
		Level2Data level2Data;
		long rate;
		long endTime = 0;
		long startTime = System.currentTimeMillis();
		while (count < messagesToPublish) {
			level2Data = createLevel2Data();
			level2Data.setSeqNum(count);

			// Publish it into the region
			String key = getKey(getIntKey());
			level2Data.setSymbol(key);
			region.put(level2Data.getSymbol(), level2Data);

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

	public static Level2Data createLevel2Data()
	{
		Level2Data level2Data = new Level2Data();

		return level2Data;
	}
	
	private void createLevel2DataPool()
	{
		level2DataPool = new Level2Data[totalKeys];
		for (int i = 0; i < totalKeys; i++) {
			level2DataPool[i] = createLevel2Data();
			level2DataPool[i].setSymbol(getKey(i));
		}
	}
	
	private Level2Data getLevel2DataFromPool()
	{
		int intKey = getIntKey();
		Level2Data level2Data = level2DataPool[intKey];
		return level2Data;
	}

	public static void main(String[] args) throws Exception
	{
		Level2CannedPublisher feeder = new Level2CannedPublisher();
		feeder.publishMessages();
		feeder.waitForever();
	}
}