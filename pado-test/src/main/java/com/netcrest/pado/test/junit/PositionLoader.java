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
package com.netcrest.pado.test.junit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.temporal.ITemporalBulkLoader;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalValue;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalKey;
import com.netcrest.pado.temporal.test.data.v.Position_v1;
import com.netcrest.pado.temporal.test.data.v.Position_v2;
import com.netcrest.pado.temporal.test.data.v.Position_v3;
import com.netcrest.pado.temporal.test.data.v.Position_v4;
import com.netcrest.pado.temporal.test.data.v.Position_v5;
import com.netcrest.pado.temporal.test.data.v.Position_v6;
import com.netcrest.pado.temporal.test.data.v.Position_v7;
import com.netcrest.pado.temporal.test.data.v.Position_v8;
import com.netcrest.pado.temporal.test.data.v.Position_v9;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PositionLoader
{	
	private final Random RANDOM = new Random();

	public PositionLoader()
	{
	}
	
	public Set<ITemporalKey> loadPositions(IGridMapBiz gridMapBiz, int entryCount) throws Exception
	{
		System.out.println();
		System.out.println("Put: KeyMap Position objects");
		System.out.println("-----------------------------");
		System.out.println("   Entry count: " + entryCount);
		System.out.println();
		System.out.println("   Bulkload started. Please wait.");
		long start = 0, end = 0;
		int identityKeyBound = entryCount / 10;

		long oneMonthInMsec = 30l * 24l * 60l * 60l * 1000l;
		int oneYearInMonths = 12;
		int twoYearsInMonths = oneYearInMonths * 2;
		int fiveYearsInMonths = oneYearInMonths * 5;
		int tenYearsInMonths = oneYearInMonths * 10;

		Calendar calendar = Calendar.getInstance();
		Date today = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

		Date startDateBase = new Date(today.getTime() - fiveYearsInMonths * oneMonthInMsec);
		Date endDate = format.parse("12/31/2099");

		long startTimeBase = startDateBase.getTime();
		long startTimeMax = startTimeBase + System.currentTimeMillis() + oneMonthInMsec * twoYearsInMonths;
		int minimumValidMonths = 6;

		Set<ITemporalKey> temporalKeySet = new HashSet(entryCount + 1);
		start = System.currentTimeMillis();
		for (int i = 1; i <= entryCount; i++) {
			// Get a random identityKey and product
			String secId = getRandomString(5, 1).toUpperCase();
			String identityKey = secId;

			// Put it into the cache
			int startMonths = RANDOM.nextInt(twoYearsInMonths);
			long startValidTime = startTimeBase + startMonths * oneMonthInMsec;
			long endMonths = startMonths + RANDOM.nextInt(fiveYearsInMonths);
			long endValidTime = startValidTime + (endMonths + minimumValidMonths) * oneMonthInMsec;
			int sign = RANDOM.nextBoolean() ? 1 : -1;
			long writtenTime = startValidTime + sign * RANDOM.nextInt() + oneMonthInMsec * RANDOM.nextInt(3);

			KeyMap position;
			switch (i % 9) {
			case 0:
				position = createPosition1(secId, startValidTime, endValidTime);
				break;
			case 1:
				position = createPosition2(secId, startValidTime, endValidTime);
				break;
			case 2:
				position = createPosition3(secId, startValidTime, endValidTime);
				break;
			case 3:
				position = createPosition4(secId, startValidTime, endValidTime);
				break;
			case 4:
				position = createPosition5(secId, startValidTime, endValidTime);
				break;
			case 5:
				position = createPosition6(secId, startValidTime, endValidTime);
				break;
			case 6:
				position = createPosition7(secId, startValidTime, endValidTime);
				break;
			case 7:
				position = createPosition8(secId, startValidTime, endValidTime);
				break;
			case 8:
			default:
				position = createPosition9(secId, startValidTime, endValidTime);
				break;
			}
			ITemporalData data = putGridMap(gridMapBiz, identityKey, position, startValidTime, endValidTime, writtenTime);
			ITemporalValue tv = data.__getTemporalValue();
			temporalKeySet.add(tv.getTemporalKey());

		}

		end = System.currentTimeMillis();

		System.out.println();
		System.out.println("   Elapsed time: " + (end - start) / 1000 + " sec");
		System.out.println();
		System.out.println("   Put load complete.");
		System.out.println();
		
		return temporalKeySet;
	}

	@SuppressWarnings("unchecked")
	private ITemporalData putGridMap(IGridMapBiz gridMapBiz, Object identityKey, Object value, long startValidTime,
			long endValidTime, long writtenTime)
	{
		ITemporalKey tkey = new GemfireTemporalKey(identityKey, startValidTime, endValidTime, writtenTime, gridMapBiz.getBizContext().getUserContext().getUsername());
		ITemporalData data = new GemfireTemporalData(tkey, value);
		data.__getTemporalValue().setTemporalKey(tkey);
		gridMapBiz.put(tkey, data);
		return data;
	}
	
	@SuppressWarnings("unchecked")
	private ITemporalData putTemporalMap(IGridMapBiz gridMapBiz, Map<ITemporalKey, ITemporalData> map, Object identityKey, Object value, long startValidTime,
			long endValidTime, long writtenTime)
	{
		ITemporalKey tkey = new GemfireTemporalKey(identityKey, startValidTime, endValidTime, writtenTime, gridMapBiz.getBizContext().getUserContext().getUsername());
		ITemporalData data = new GemfireTemporalData(tkey, value);
		data.__getTemporalValue().setTemporalKey(tkey);
		map.put(tkey, data);
		return data;
	} 
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Set<ITemporalKey> bulkLoadPositions(IGridMapBiz gridMapBiz, int entryCount) throws Exception
	{
		System.out.println();
		System.out.println("PutAll: KeyMap Position objects");
		System.out.println("--------------------------------");
		System.out.println("   Entry count: " + entryCount);
		System.out.println();
		System.out.println("   Bulkload started. Please wait.");
		long start = 0, end = 0;
		int identityKeyBound = entryCount / 10;

		long oneMonthInMsec = 30l * 24l * 60l * 60l * 1000l;
		int oneYearInMonths = 12;
		int twoYearsInMonths = oneYearInMonths * 2;
		int fiveYearsInMonths = oneYearInMonths * 5;
		int tenYearsInMonths = oneYearInMonths * 10;

		Calendar calendar = Calendar.getInstance();
		Date today = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

		Date startDateBase = new Date(today.getTime() - fiveYearsInMonths * oneMonthInMsec);
		Date endDate = format.parse("12/31/2099");

		long startTimeBase = startDateBase.getTime();
		long startTimeMax = startTimeBase + System.currentTimeMillis() + oneMonthInMsec * twoYearsInMonths;
		int minimumValidMonths = 6;

		Set<ITemporalKey> temporalKeySet = new HashSet(entryCount + 1);
		Map<ITemporalKey, ITemporalData> map = new HashMap(1000, 1f);
		start = System.currentTimeMillis();
		for (int i = 1; i <= entryCount; i++) {
			// Get a random identityKey and product
			String secId = getRandomString(5, 1).toUpperCase();
			String identityKey = secId;

			// Put it into the cache
			int startMonths = RANDOM.nextInt(twoYearsInMonths);
			long startValidTime = startTimeBase + startMonths * oneMonthInMsec;
			long endMonths = startMonths + RANDOM.nextInt(fiveYearsInMonths);
			long endValidTime = startValidTime + (endMonths + minimumValidMonths) * oneMonthInMsec;
			int sign = RANDOM.nextBoolean() ? 1 : -1;
			long writtenTime = startValidTime + sign * RANDOM.nextInt() + oneMonthInMsec * RANDOM.nextInt(3);

			KeyMap position;
			switch (i % 9) {
			case 0:
				position = createPosition1(secId, startValidTime, endValidTime);
				break;
			case 1:
				position = createPosition2(secId, startValidTime, endValidTime);
				break;
			case 2:
				position = createPosition3(secId, startValidTime, endValidTime);
				break;
			case 3:
				position = createPosition4(secId, startValidTime, endValidTime);
				break;
			case 4:
				position = createPosition5(secId, startValidTime, endValidTime);
				break;
			case 5:
				position = createPosition6(secId, startValidTime, endValidTime);
				break;
			case 6:
				position = createPosition7(secId, startValidTime, endValidTime);
				break;
			case 7:
				position = createPosition8(secId, startValidTime, endValidTime);
				break;
			case 8:
			default:
				position = createPosition9(secId, startValidTime, endValidTime);
				break;
			}
			ITemporalData data = putTemporalMap(gridMapBiz, map, identityKey, position, startValidTime, endValidTime, writtenTime);
			ITemporalValue tv = data.__getTemporalValue();
			temporalKeySet.add(tv.getTemporalKey());
			
			if (map.size() % 1000 == 0) {
				gridMapBiz.putAll(map);
				map.clear();
			}
		}
		
		if (map.size() > 0) {
			gridMapBiz.putAll(map);
		}
		map.clear();

		end = System.currentTimeMillis();

		System.out.println();
		System.out.println("   Elapsed time: " + (end - start) / 1000 + " sec");
		System.out.println();
		System.out.println("   PutAll load complete.");
		System.out.println();
		
		return temporalKeySet;
	}

	public void runBulkLoadPositions(ITemporalBiz<String, KeyMap> temporalBiz, int entryCount, int batchSize) throws Exception
	{
		System.out.println();
		System.out.println("Bulkload KeyMap Position objects");
		System.out.println("---------------------------------");
		System.out.println("   Entry count: " + entryCount);
		System.out.println("    Batch size: " + batchSize);
		System.out.println();
		System.out.println("   Bulkload started. Please wait.");
		long start = 0, end = 0;
		ITemporalBulkLoader<String, KeyMap> loader = temporalBiz.getTemporalAdminBiz().createBulkLoader(batchSize);

		long oneMonthInMsec = 30l * 24l * 60l * 60l * 1000l;
		int oneYearInMonths = 12;
		int twoYearsInMonths = oneYearInMonths * 2;
		int fiveYearsInMonths = oneYearInMonths * 5;
		int tenYearsInMonths = oneYearInMonths * 10;

		Calendar calendar = Calendar.getInstance();
		Date today = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

		Date startDateBase = new Date(today.getTime() - fiveYearsInMonths * oneMonthInMsec);
		Date endDate = format.parse("12/31/2099");

		long startTimeBase = startDateBase.getTime();
		long startTimeMax = startTimeBase + System.currentTimeMillis() + oneMonthInMsec * twoYearsInMonths;
		int minimumValidMonths = 6;

		start = System.currentTimeMillis();
		for (int i = 1; i <= entryCount; i++) {
			// Get a random identityKey and product
			String secId = getRandomString(5, 1).toUpperCase();
			String identityKey = secId;

			// Put it into the cache
			int startMonths = RANDOM.nextInt(twoYearsInMonths);
			long startValidTime = startTimeBase + startMonths * oneMonthInMsec;
			long endMonths = startMonths + RANDOM.nextInt(fiveYearsInMonths);
			long endValidTime = startValidTime + (endMonths + minimumValidMonths) * oneMonthInMsec;
			int sign = RANDOM.nextBoolean() ? 1 : -1;
			long writtenTime = startValidTime + sign * RANDOM.nextInt() + oneMonthInMsec * RANDOM.nextInt(3);

			KeyMap position;
			switch (i % 9) {
			case 0:
				position = createPosition1(secId, startValidTime, endValidTime);
				break;
			case 1:
				position = createPosition2(secId, startValidTime, endValidTime);
				break;
			case 2:
				position = createPosition3(secId, startValidTime, endValidTime);
				break;
			case 3:
				position = createPosition4(secId, startValidTime, endValidTime);
				break;
			case 4:
				position = createPosition5(secId, startValidTime, endValidTime);
				break;
			case 5:
				position = createPosition6(secId, startValidTime, endValidTime);
				break;
			case 6:
				position = createPosition7(secId, startValidTime, endValidTime);
				break;
			case 7:
				position = createPosition8(secId, startValidTime, endValidTime);
				break;
			case 8:
			default:
				position = createPosition9(secId, startValidTime, endValidTime);
				break;
			}
			loader.put(identityKey, position, null, startValidTime, endValidTime, writtenTime, false);

			if (i % batchSize == 0) {
				System.out.print(i + " ");
			}
		}

		// Flushes the loader. This must be invoked at the end of the load
		// in order to ensure the remaining entries in the buffer is
		// flushed out to the cache.
		loader.flush();
		end = System.currentTimeMillis();

		if (entryCount % batchSize != 0) {
			System.out.println(entryCount);
		} else {
			System.out.println();
		}
		System.out.println();
		System.out.println("   Elapsed time: " + (end - start) / 1000 + " sec");
		System.out.println();
		System.out.println("   Bulkload complete.");
		System.out.println();
	}
	
//	public void runBulkLoad6(Temporal<String, KeyMap> temporal, int entryCount, int batchSize) throws Exception
//	{
//		System.out.println();
//		System.out.println("Bulkload KeyMap Position objects");
//		System.out.println("---------------------------------");
//		System.out.println("   Entry count: " + entryCount);
//		System.out.println("    Batch size: " + batchSize);
//		System.out.println();
//		System.out.println("   Bulkload started. Please wait.");
//		long start = 0, end = 0;
//		BulkLoader<String, KeyMap> loader = (BulkLoader<String, KeyMap>) temporal.getTemporalAdmin()
//				.createBulkLoader(batchSize);
//
//		long oneMonthInMsec = 30l * 24l * 60l * 60l * 1000l;
//		int oneYearInMonths = 12;
//		int twoYearsInMonths = oneYearInMonths * 2;
//		int fiveYearsInMonths = oneYearInMonths * 5;
//		int tenYearsInMonths = oneYearInMonths * 10;
//
//		Calendar calendar = Calendar.getInstance();
//		Date today = calendar.getTime();
//		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
//
//		Date startDateBase = new Date(today.getTime() - fiveYearsInMonths * oneMonthInMsec);
//		Date endDate = format.parse("12/31/2099");
//
//		long startTimeBase = startDateBase.getTime();
//		long startTimeMax = startTimeBase + System.currentTimeMillis() + oneMonthInMsec * twoYearsInMonths;
//		int minimumValidMonths = 6;
//
//		start = System.currentTimeMillis();
//		for (int i = 1; i <= entryCount; i++) {
//			// Get a random identityKey and product
//			String secId = getRandomString(5, 1).toUpperCase();
//			String identityKey = secId;
//
//			// Put it into the cache
//			int startMonths = RANDOM.nextInt(twoYearsInMonths);
//			long startValidTime = startTimeBase + startMonths * oneMonthInMsec;
//			long endMonths = startMonths + RANDOM.nextInt(fiveYearsInMonths);
//			long endValidTime = startValidTime + (endMonths + minimumValidMonths) * oneMonthInMsec;
//			int sign = RANDOM.nextBoolean() ? 1 : -1;
//			long writtenTime = startValidTime + sign * RANDOM.nextInt() + oneMonthInMsec * RANDOM.nextInt(3);
//
//			KeyMap position;
//			switch (i % 9) {
//			case 0:
//				position = createPosition1(secId, startValidTime, endValidTime);
//				break;
//			case 1:
//				position = createPosition2(secId, startValidTime, endValidTime);
//				break;
//			case 2:
//				position = createPosition3(secId, startValidTime, endValidTime);
//				break;
//			case 3:
//				position = createPosition4(secId, startValidTime, endValidTime);
//				break;
//			case 4:
//				position = createPosition5(secId, startValidTime, endValidTime);
//				break;
//			case 5:
//				position = createPosition6(secId, startValidTime, endValidTime);
//				break;
//			case 6:
//				position = createPosition7(secId, startValidTime, endValidTime);
//				break;
//			case 7:
//				position = createPosition8(secId, startValidTime, endValidTime);
//				break;
//			case 8:
//			default:
//				position = createPosition9(secId, startValidTime, endValidTime);
//				break;
//			}
//			loader.put(identityKey, position, null, startValidTime, endValidTime, writtenTime);
//
//			if (i % batchSize == 0) {
//				System.out.print(i + " ");
//			}
//		}
//
//		// Flushes the loader. This must be invoked at the end of the load
//		// in order to ensure the remaining entries in the buffer is
//		// flushed out to the cache.
//		loader.flush();
//		end = System.currentTimeMillis();
//
//		if (entryCount % batchSize != 0) {
//			System.out.println(entryCount);
//		} else {
//			System.out.println();
//		}
//		System.out.println();
//		System.out.println("   Elapsed time: " + (end - start) / 1000 + " sec");
//		System.out.println();
//		System.out.println("   Bulkload complete.");
//		System.out.println();
//	}

	private long getRandomLong(int maxNumDigits)
	{
		return Math.abs(RANDOM.nextLong() % maxNumDigits);
	}

	private double getRandomPrice(int maxNumWholeDigits)
	{
		return Math.abs((double) ((long) (RANDOM.nextDouble() * (maxNumWholeDigits + 2))) / 100d);
	}

	private KeyMap newPosition(KeyType keyType)
	{
		KeyMap position = new JsonLite(keyType);
		return position;
	}
	
	public KeyMap createPosition1(String secId, long startValidTime, long endValidTime)
	{
		KeyMap position = newPosition(Position_v1.getKeyType());
		position.put(Position_v1.KAccountId, getRandomString(6, 1));
		position.put(Position_v1.KExposure1, getRandomPrice(10000));
		position.put(Position_v1.KFiImntId, getRandomLong(10000));
		position.put(Position_v1.KMarketPlace, getRandomPrice(10000));
		position.put(Position_v1.KOriginalCost, getRandomPrice(100));
		position.put(Position_v1.KSecId, secId);
		return position;
	}

	private KeyMap createPosition2(String secId, long startValidTime, long endValidTime)
	{
		KeyMap position = newPosition(Position_v2.getKeyType());
		position.put(Position_v2.KAccountId, getRandomString(6, 1));
		position.put(Position_v2.KExposure1, getRandomPrice(10000));
		position.put(Position_v2.KFiImntId, getRandomLong(10000));
		position.put(Position_v2.KMarketPlace, getRandomPrice(10000));
		position.put(Position_v2.KOriginalCost, getRandomPrice(100));
		position.put(Position_v2.KOriginalFace, getRandomPrice(100));
		position.put(Position_v2.KSecId, secId);
		return position;
	}

	private KeyMap createPosition3(String secId, long startValidTime, long endValidTime)
	{
		KeyMap position = newPosition(Position_v3.getKeyType());
		position.put(Position_v3.KAccountId, getRandomString(6, 1));
		position.put(Position_v3.KExposure1, getRandomPrice(10000));
		position.put(Position_v3.KFiImntId, getRandomLong(10000));
		position.put(Position_v3.KMarketPlace, getRandomPrice(10000));
		position.put(Position_v3.KOriginalCost, getRandomPrice(100));
		position.put(Position_v3.KOriginalFace, getRandomPrice(100));
		position.put(Position_v3.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
		position.put(Position_v3.KSecId, secId);
		return position;
	}

	private KeyMap createPosition4(String secId, long startValidTime, long endValidTime)
	{
		KeyMap position = newPosition(Position_v4.getKeyType());
		position.put(Position_v4.KAccountId, getRandomString(6, 1));
		position.put(Position_v4.KExposure1, getRandomPrice(10000));
		// position.put(Position2_v4.FiImntId, getRandomLong(10000)); //
		// deprecated
		// position.put(Position2_v4.MarketPlace, getRandomPrice(10000)); //
		// deprecated
		position.put(Position_v4.KOriginalCost, getRandomPrice(100));
		position.put(Position_v4.KOriginalFace, getRandomPrice(100));
		position.put(Position_v4.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
		position.put(Position_v4.KSecId, secId);
		return position;
	}

	private KeyMap createPosition5(String secId, long startValidTime, long endValidTime)
	{
		KeyMap position = newPosition(Position_v5.getKeyType());
		position.put(Position_v5.KAccountId, getRandomString(6, 1));
		position.put(Position_v5.KExposure1, getRandomPrice(10000));
		position.put(Position_v5.KExposure2, getRandomPrice(10000));
		// position.put(Position2_v5.FiImntId, getRandomLong(10000)); //
		// deprecated
		// position.put(Position2_v5.MarketPlace, getRandomPrice(10000)); //
		// deprecated
		position.put(Position_v5.KOriginalCost, getRandomPrice(100));
		position.put(Position_v5.KOriginalFace, getRandomPrice(100));
		position.put(Position_v5.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
		position.put(Position_v5.KSecId, secId);
		return position;
	}

	private KeyMap createPosition6(String secId, long startValidTime, long endValidTime)
	{
		KeyMap position = newPosition(Position_v6.getKeyType());
		position.put(Position_v6.KAccountId, getRandomString(6, 1));
		position.put(Position_v6.KExposure1, getRandomPrice(10000));
		position.put(Position_v6.KExposure2, getRandomPrice(10000));
		// position.put(Position2_v6.FiImntId, getRandomLong(10000)); // merged
		// position.put(Position2_v6.MarketPlace, getRandomPrice(10000)); //
		// merged
		position.put(Position_v6.KOriginalCost, getRandomPrice(100));
		position.put(Position_v6.KOriginalFace, getRandomPrice(100));
		position.put(Position_v6.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
		position.put(Position_v6.KSecId, secId);
		return position;
	}

	private KeyMap createPosition7(String secId, long startValidTime, long endValidTime)
	{
		KeyMap position = newPosition(Position_v7.getKeyType());
		position.put(Position_v7.KAccountId, getRandomString(6, 1));
		position.put(Position_v7.KExposure1, getRandomPrice(10000));
		position.put(Position_v7.KExposure2, getRandomPrice(10000));
		// position.put(Position2_v7.FiImntId, getRandomLong(10000)); // merged
		// position.put(Position2_v7.MarketPlace, getRandomPrice(10000)); //
		// merged
		position.put(Position_v7.KOriginalCost, getRandomPrice(100));
		position.put(Position_v7.KOriginalFace, getRandomPrice(100));
		position.put(Position_v7.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
		position.put(Position_v7.KRiskFactor, getRandomPrice(100));
		position.put(Position_v7.KSecId, secId);
		return position;
	}

	private KeyMap createPosition8(String secId, long startValidTime, long endValidTime)
	{
		KeyMap position = newPosition(Position_v8.getKeyType());
		position.put(Position_v8.KAccountId, getRandomString(6, 1));
		position.put(Position_v8.KExposure1, getRandomPrice(10000));
		position.put(Position_v8.KExposure2, getRandomPrice(10000));
		// position.put(Position2_v8.FiImntId, getRandomLong(10000)); // merged
		// position.put(Position2_v8.MarketPlace, getRandomPrice(10000)); //
		// merged
		// position.put(Position2_v8.OriginalCost, getRandomPrice(100)); //
		// deprecated
		// position.put(Position2_v8.OriginalFace, getRandomPrice(100)); //
		// deprecated
		position.put(Position_v8.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
		position.put(Position_v8.KRiskFactor, getRandomPrice(100));
		position.put(Position_v8.KSecId, secId);
		return position;
	}

	private KeyMap createPosition9(String secId, long startValidTime, long endValidTime)
	{
		KeyMap position = newPosition(Position_v9.getKeyType());
		position.put(Position_v9.KAccountId, getRandomString(6, 1));
		position.put(Position_v9.KExposure1, getRandomPrice(10000));
		position.put(Position_v9.KExposure2, getRandomPrice(10000));
		// position.put(Position2_v9.FiImntId, getRandomLong(10000)); // merged
		// at 5
		// position.put(Position2_v9.MarketPlace, getRandomPrice(10000)); //
		// merged at 5
		// position.put(Position2_v9.OriginalCost, getRandomPrice(100)); //
		// merged at 8
		// position.put(Position2_v9.OriginalFace, getRandomPrice(100)); //
		// merged at 8
		position.put(Position_v9.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
		position.put(Position_v9.KRiskFactor, getRandomPrice(100));
		position.put(Position_v9.KSecId, secId);
		return position;
	}

	private String getRandomString(int maxCharactersPerWord, int maxWords)
	{
		int stringLen = RANDOM.nextInt(maxWords) + 1;
		StringBuffer buffer = new StringBuffer(stringLen * 10);

		for (int i = 0; i < stringLen; i++) {
			int wordLen = RANDOM.nextInt(maxCharactersPerWord - 1) + 1;
			char[] word = new char[wordLen];
			for (int j = 0; j < wordLen; j++) {
				int val = RANDOM.nextInt(26) + 97; // a-z
				word[j] = (char) val;
			}
			buffer.append(word);
			buffer.append(" ");
		}
		return buffer.toString().trim();
	}
}
