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
package com.netcrest.pado.temporal.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.info.GridRouterInfo;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.AttachmentSetFactory;
import com.netcrest.pado.temporal.ITemporalBulkLoader;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalValue;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalInternalFactory;
import com.netcrest.pado.temporal.TemporalUtil;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalKey;
import com.netcrest.pado.temporal.test.data.Account;
import com.netcrest.pado.temporal.test.data.AccountDetail;
import com.netcrest.pado.temporal.test.data.Bank;
import com.netcrest.pado.temporal.test.data.Portfolio;
import com.netcrest.pado.temporal.test.data.v.Position_v1;
import com.netcrest.pado.temporal.test.data.v.Position_v2;
import com.netcrest.pado.temporal.test.data.v.Position_v3;
import com.netcrest.pado.temporal.test.data.v.Position_v4;
import com.netcrest.pado.temporal.test.data.v.Position_v5;
import com.netcrest.pado.temporal.test.data.v.Position_v6;
import com.netcrest.pado.temporal.test.data.v.Position_v7;
import com.netcrest.pado.temporal.test.data.v.Position_v8;
import com.netcrest.pado.temporal.test.data.v.Position_v9;
import com.netcrest.pado.temporal.test.gemfire.PortfolioKey;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalLoader
{
	private static enum KeyMapType
	{
		MAPLITE, JSONLITE
	}

	static enum DataType
	{
		ACCOUNT, ACCOUNT_DETAIL, BANK, PORTFOLIO, POSITION
	}

	public final static String PATH_PORTFOLIO = "portfolio";
	public final static String PATH_BANK = "bank";
	public final static String PATH_ACCOUNT = "account";
	public final static String PATH_ACCOUNT_DETAIL = "account_detail";
	public final static String PATH_POSITION = "position";

	public final static String PREFIX_PORTFOLIO = "port_";
	public final static String PREFIX_BANK = "bank_";
	public final static String PREFIX_ACCOUNT = "acct_";
	public final static String PREFIX_POSITION = "pos_";

	private transient KeyMapType keyMapType = KeyMapType.JSONLITE;

	private final Random RANDOM = new Random();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	private Calendar calendar = Calendar.getInstance();

	/**
	 * Identity keys that have been put into Pado. This set is used to make sure
	 * this object always puts only unique identity keys for put calls.
	 */
	private List<String> positionIdList = new ArrayList();
	private List<String> accountIdList = new ArrayList();
	private List<String> bankIdList = new ArrayList();
	private List<String> portfolioIdList = new ArrayList();
	
	HashMap<String, String> colocatedPortfolioMap = new HashMap<String, String>();

	public TemporalLoader()
	{
		String keyMapTypeProp = System.getProperty("keyMapType", "jsonlite");
		keyMapType = keyMapTypeProp.equalsIgnoreCase("maplite") ? KeyMapType.MAPLITE : KeyMapType.JSONLITE;
		dateFormat.setTimeZone(TemporalUtil.TIME_ZONE);
		calendar.setTimeZone(TemporalUtil.TIME_ZONE);
	}

	public Set<ITemporalKey> loadPositionsByGridMap(IGridMapBiz gridMapBiz, int entryCount) throws Exception
	{
		if (entryCount > positionIdList.size()) {
			positionIdList = createIdentityKeyList(TemporalLoader.PREFIX_POSITION, entryCount, 5);
		}
		if (entryCount > accountIdList.size()) {
			accountIdList = createIdentityKeyList(TemporalLoader.PREFIX_ACCOUNT, entryCount, 10);
		}

		System.out.println();
		System.out.println("Put: KeyMap Position objects");
		System.out.println("----------------------------");
		System.out.println("   Entry count: " + entryCount);
		System.out.println();
		System.out.println("   Load started. Please wait.");
		long start = 0, end = 0;
		int identityKeyBound = entryCount / 10;

		long oneMonthInMsec = 30l * 24l * 60l * 60l * 1000l;
		int oneYearInMonths = 12;
		int twoYearsInMonths = oneYearInMonths * 2;
		int fiveYearsInMonths = oneYearInMonths * 5;
		int tenYearsInMonths = oneYearInMonths * 10;

		Date today = dateFormat.parse(dateFormat.format(calendar.getTime()));
		Date startDateBase = new Date(today.getTime() - fiveYearsInMonths * oneMonthInMsec);
		long endTime = TemporalUtil.MAX_TIME;

		long startTimeBase = startDateBase.getTime();
		long startTimeMax = startTimeBase + System.currentTimeMillis() + oneMonthInMsec * twoYearsInMonths;
		int minimumValidMonths = 6;

		Set<ITemporalKey> temporalKeySet = new HashSet(entryCount + 1);
		if (positionIdList.size() < entryCount) {
			positionIdList = createIdentityKeyList(TemporalLoader.PREFIX_POSITION, entryCount, 10);
		}
		int totalCount = 0;
		start = System.currentTimeMillis();
		for (int i = 1; i <= entryCount; i++) {
			// Get a random identityKey and product
			String secId = getRandomString(5, 1).toUpperCase();
			String identityKey = secId;

			// Put it into the cache
			int startMonths = RANDOM.nextInt(twoYearsInMonths);
			long startValidTime = startTimeBase + startMonths * oneMonthInMsec;

			int updateCount = RANDOM.nextInt(20);

			for (int j = 0; j < updateCount; j++) {
				long endMonths = startMonths + RANDOM.nextInt(fiveYearsInMonths);
				long endValidTime = startValidTime + (endMonths + minimumValidMonths) * oneMonthInMsec;
				int sign = RANDOM.nextBoolean() ? 1 : -1;
				long writtenTime = startValidTime + sign * RANDOM.nextInt() + oneMonthInMsec * RANDOM.nextInt(3);

				KeyMap position = createKeyMap(DataType.POSITION, i, secId, startValidTime, endValidTime, false);
				ITemporalData data = putGridMap(gridMapBiz, identityKey, position, startValidTime, endValidTime,
						writtenTime);
				ITemporalValue tv = data.__getTemporalValue();
				temporalKeySet.add(tv.getTemporalKey());
			}

			totalCount += updateCount;

		}

		end = System.currentTimeMillis();

		System.out.println();
		System.out.println("    Total count: " + totalCount);
		System.out.println("   Elapsed time: " + (end - start) / 1000 + " sec");
		System.out.println();
		System.out.println("   Put load complete.");
		System.out.println();

		return temporalKeySet;
	}

	private ITemporalData putGridMap(IGridMapBiz gridMapBiz, Object identityKey, Object value, long startValidTime,
			long endValidTime, long writtenTime)
	{
		ITemporalKey tkey = new GemfireTemporalKey(identityKey, startValidTime, endValidTime, writtenTime, gridMapBiz
				.getBizContext().getUserContext().getUsername());
		ITemporalData data = new GemfireTemporalData(tkey, value);
		data.__getTemporalValue().setTemporalKey(tkey);
		gridMapBiz.put(tkey, data);
		return data;
	}

	private ITemporalData putTemporalMap(IGridMapBiz gridMapBiz, Map<ITemporalKey, ITemporalData> map,
			Object identityKey, Object value, long startValidTime, long endValidTime, long writtenTime)
	{
		ITemporalKey tkey = new GemfireTemporalKey(identityKey, startValidTime, endValidTime, writtenTime, gridMapBiz
				.getBizContext().getUserContext().getUsername());
		ITemporalData data = new GemfireTemporalData(tkey, value);
		data.__getTemporalValue().setTemporalKey(tkey);
		map.put(tkey, data);
		return data;
	}

	public Set<ITemporalKey> loadPositionsByPutAll(IGridMapBiz gridMapBiz, int entryCount) throws Exception
	{
		if (entryCount > positionIdList.size()) {
			positionIdList = createIdentityKeyList(TemporalLoader.PREFIX_POSITION, entryCount, 5);
		}
		if (entryCount > accountIdList.size()) {
			accountIdList = createIdentityKeyList(TemporalLoader.PREFIX_ACCOUNT, entryCount, 10);
		}

		System.out.println();
		System.out.println("PutAll: KeyMap Position objects");
		System.out.println("-------------------------------");
		System.out.println("   Entry count: " + entryCount);
		System.out.println();
		System.out.println("   Bulkload started. Please wait.");
		long start = 0, end = 0;

		long oneMonthInMsec = 30l * 24l * 60l * 60l * 1000l;
		int oneYearInMonths = 12;
		int twoYearsInMonths = oneYearInMonths * 2;
		int fiveYearsInMonths = oneYearInMonths * 5;

		Date today = calendar.getTime();

		Date startDateBase = new Date(today.getTime() - fiveYearsInMonths * oneMonthInMsec);
		Date endDate = dateFormat.parse("12/31/2099");
		long endTime = endDate.getTime();

		long startTimeBase = startDateBase.getTime();
		int minimumValidMonths = 6;

		Set<ITemporalKey> temporalKeySet = new HashSet(entryCount + 1);
		Map<ITemporalKey, ITemporalData> map = new HashMap(1000, 1f);
		start = System.currentTimeMillis();
		for (int i = 1; i <= entryCount; i++) {
			// Get a random identityKey and product
			String secId = getRandomString(5, 1);
			String identityKey = secId;

			// Put it into the cache
			int startMonths = RANDOM.nextInt(twoYearsInMonths) + 1;
			long startValidTime = startTimeBase + startMonths * oneMonthInMsec;
			long endMonths = startMonths + RANDOM.nextInt(fiveYearsInMonths);
			long endValidTime = startValidTime + (endMonths + minimumValidMonths) * oneMonthInMsec;
			int sign = RANDOM.nextBoolean() ? 1 : -1;
			long writtenTime = startValidTime + sign * RANDOM.nextInt() + oneMonthInMsec * RANDOM.nextInt(3);

			KeyMap position = createKeyMap(DataType.POSITION, i, secId, startValidTime, endValidTime, false);
			ITemporalData data = putTemporalMap(gridMapBiz, map, identityKey, position, startValidTime, endValidTime,
					writtenTime);
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

	public Set<TemporalEntry<Object, KeyMap>> loadPositionsByPut(ITemporalBiz temporalBiz, int entryCount)
			throws Exception
	{
		if (entryCount > positionIdList.size()) {
			positionIdList = createIdentityKeyList(TemporalLoader.PREFIX_POSITION, entryCount, 5);
		}
		if (entryCount > accountIdList.size()) {
			accountIdList = createIdentityKeyList(TemporalLoader.PREFIX_ACCOUNT, entryCount, 10);
		}

		System.out.println();
		System.out.println("Temporal Put: KeyMap Position objects");
		System.out.println("--------------------------------------");
		System.out.println("   Entry count: " + entryCount);
		System.out.println();
		System.out.println("   Load started. Please wait.");
		long start = 0, end = 0;

		long oneMonthInMsec = 30l * 24l * 60l * 60l * 1000l;
		int oneYearInMonths = 12;
		int twoYearsInMonths = oneYearInMonths * 2;
		int fiveYearsInMonths = oneYearInMonths * 5;

		Date today = calendar.getTime();

		long todayTime = today.getTime();
		Date startDateBase = new Date(todayTime - fiveYearsInMonths * oneMonthInMsec);
		Date endDate = dateFormat.parse("12/31/2099");
		long endTime = endDate.getTime();

		long startTimeBase = startDateBase.getTime();
		long startTimeMax = startTimeBase + System.currentTimeMillis() + oneMonthInMsec * twoYearsInMonths;
		int minimumValidMonths = 6;
		int totalCount = 0;
		Set<TemporalEntry<Object, KeyMap>> temporalEntrySet = new HashSet(entryCount + 1);
		start = System.currentTimeMillis();
		for (int i = 1; i <= entryCount; i++) {
			// Get a random identityKey and product
			String secId = getRandomString(5, 1);
			while (positionIdList.contains(secId)) {
				secId = getRandomString(5, 1);
			}
			positionIdList.add(secId);
			String identityKey = secId;

			// Put it into the cache
			int startMonths = RANDOM.nextInt(twoYearsInMonths) + 1;
			long endValidTime = startTimeBase + startMonths * oneMonthInMsec;
			int updateCount = RANDOM.nextInt(20);

			for (int j = 0; j < updateCount; j++) {
				long startValidTime = endValidTime;
				endValidTime = startValidTime + (RANDOM.nextInt(oneYearInMonths) + 1) * oneMonthInMsec;
				long writtenTime = startValidTime;
				KeyMap position = createKeyMap(DataType.POSITION, i, secId, startValidTime, endValidTime, false);
				TemporalEntry entry = temporalBiz.put(identityKey, position, startValidTime, endValidTime, writtenTime,
						false);
				temporalEntrySet.add(entry);

				// Break if the end time is later than today so that we
				// can search now-relative data without specifying valid-at
				if (endValidTime > todayTime) {
					break;
				}
				totalCount += updateCount;
			}

		}

		end = System.currentTimeMillis();

		System.out.println();
		System.out.println("    Total count: " + totalCount);
		System.out.println("   Elapsed time: " + (end - start) / 1000 + " sec");
		System.out.println();
		System.out.println("   Temporal put load complete.");
		System.out.println();

		return temporalEntrySet;
	}

	public void bulkLoadAll(ICatalog catalog, int entryCount, int batchSize, boolean isLoadDelta) throws Exception
	{
		ITemporalBiz<Object, KeyMap> positionTemporalBiz = catalog.newInstance(ITemporalBiz.class, PATH_POSITION);
		ITemporalBiz<Object, KeyMap> accountTemporalBiz = catalog.newInstance(ITemporalBiz.class, PATH_ACCOUNT);
		ITemporalBiz<Object, KeyMap> portfolioTemporalBiz = catalog.newInstance(ITemporalBiz.class, PATH_PORTFOLIO);
		ITemporalBiz<Object, KeyMap> bankTemporalBiz = catalog.newInstance(ITemporalBiz.class, PATH_BANK);
		ITemporalBiz<Object, KeyMap> accountDetailTemporalBiz = catalog.newInstance(ITemporalBiz.class,
				PATH_ACCOUNT_DETAIL);

		positionIdList = createIdentityKeyList(TemporalLoader.PREFIX_POSITION, entryCount, 5);
		accountIdList = createIdentityKeyList(TemporalLoader.PREFIX_ACCOUNT, entryCount, 10);
		bankIdList = createIdentityKeyList(TemporalLoader.PREFIX_BANK, entryCount, 10);
		portfolioIdList = createIdentityKeyList(TemporalLoader.PREFIX_PORTFOLIO, entryCount, 6);

		bulkLoad(DataType.PORTFOLIO, portfolioIdList, portfolioTemporalBiz, batchSize, false, isLoadDelta);
		bulkLoad(DataType.POSITION, positionIdList, positionTemporalBiz, batchSize, false, isLoadDelta);
		bulkLoad(DataType.ACCOUNT, accountIdList, accountTemporalBiz, batchSize, false, isLoadDelta);
		bulkLoad(DataType.BANK, bankIdList, bankTemporalBiz, batchSize, false, isLoadDelta);
		bulkLoad(DataType.ACCOUNT_DETAIL, accountIdList, accountDetailTemporalBiz, batchSize, false, isLoadDelta);
		putMyPorfolio(portfolioTemporalBiz);
	}

	public Set<TemporalEntry<Object, KeyMap>> bulkLoadPositions(ITemporalBiz<Object, KeyMap> temporalBiz,
			int entryCount, int batchSize, boolean isReturnTemporalEntrySet) throws Exception
	{
		if (entryCount > positionIdList.size()) {
			positionIdList = createIdentityKeyList(TemporalLoader.PREFIX_ACCOUNT, entryCount, 5);
		}
		if (entryCount > accountIdList.size()) {
			accountIdList = createIdentityKeyList(TemporalLoader.PREFIX_ACCOUNT, entryCount, 10);
		}
		return bulkLoad(DataType.POSITION, positionIdList, temporalBiz, batchSize, isReturnTemporalEntrySet, false);
	}

	public Set<TemporalEntry<Object, KeyMap>> bulkLoadAccounts(ITemporalBiz<Object, KeyMap> temporalBiz,
			int entryCount, int batchSize, boolean isReturnTemporalEntrySet) throws Exception
	{
		if (entryCount > accountIdList.size()) {
			accountIdList = createIdentityKeyList(TemporalLoader.PREFIX_ACCOUNT, entryCount, 10);
		}
		if (entryCount > bankIdList.size()) {
			bankIdList = createIdentityKeyList(TemporalLoader.PREFIX_BANK, entryCount, 10);
		}
		return bulkLoad(DataType.ACCOUNT, accountIdList, temporalBiz, batchSize, isReturnTemporalEntrySet, false);
	}

	public Set<TemporalEntry<Object, KeyMap>> bulkLoadBanks(ITemporalBiz<Object, KeyMap> temporalBiz, int entryCount,
			int batchSize, boolean isReturnTemporalEntrySet) throws Exception
	{
		if (entryCount > bankIdList.size()) {
			bankIdList = createIdentityKeyList(TemporalLoader.PREFIX_BANK, entryCount, 10);
		}
		return bulkLoad(DataType.BANK, bankIdList, temporalBiz, batchSize, isReturnTemporalEntrySet, false);
	}

	public Set<TemporalEntry<Object, KeyMap>> bulkLoadPortfolios(ITemporalBiz<Object, KeyMap> temporalBiz,
			int entryCount, int batchSize, boolean isReturnTemporalEntrySet) throws Exception
	{
		if (entryCount > portfolioIdList.size()) {
			portfolioIdList = createIdentityKeyList(TemporalLoader.PREFIX_PORTFOLIO, entryCount, 10);
		}
		if (entryCount > accountIdList.size()) {
			accountIdList = createIdentityKeyList(TemporalLoader.PREFIX_ACCOUNT, entryCount, 10);
		}
		if (entryCount > positionIdList.size()) {
			positionIdList = createIdentityKeyList("pos", entryCount, 5);
		}
		return bulkLoad(DataType.PORTFOLIO, portfolioIdList, temporalBiz, batchSize, isReturnTemporalEntrySet, false);
	}

	public Set<TemporalEntry<Object, KeyMap>> bulkLoad(DataType dataType, List<String> idList,
			ITemporalBiz<Object, KeyMap> temporalBiz, int batchSize, boolean isReturnTemporalEntrySet,
			boolean isLoadDelta) throws Exception
	{
		int loopCount = idList.size();
		GridRouterInfo gri = temporalBiz.getBizContext().getGridService().getGridRouterInfo(temporalBiz.getGridPath());
		Set<String> gridIdSet;
		if (gri == null) {
			String gridIds[] =temporalBiz.getBizContext().getGridService().getGridIds(temporalBiz.getGridPath());
			gridIdSet = new HashSet();
			for (String gridId : gridIds) {
				gridIdSet.add(gridId);
			}
		} else {
			gridIdSet = gri.getGridRouter().getAllowedGridIdSet();
		}
		if (gridIdSet == null || gridIdSet.size() == 0) {
			System.err.println("Grid path undefined: " + temporalBiz.getGridPath());
			return null;
		}
		
		System.out.println();
		System.out.print("Bulkload KeyMap ");
		System.out.print(dataType);
		System.out.println(" objects using IBulkLoader");
		System.out.println("---------------------------------------------------");

		System.out.print("      Grid Ids: " + gridIdSet);
		
//		for (int i = 0; i < gridIds.length; i++) {
//			if (i < gridIds.length - 1) {
//				System.out.print(gridIds[i] + ", ");
//			} else {
//				System.out.print(gridIds[i]);
//			}
//		}
		System.out.println();
		System.out.println("     Grid path: " + temporalBiz.getGridPath());
		System.out.println("    Loop count: " + loopCount);
		System.out.println("    Batch size: " + batchSize);
		System.out.println();
		System.out.println("   Bulkload started. Please wait.");
		long start = 0, end = 0;
		ITemporalBulkLoader<Object, KeyMap> loader = temporalBiz.getTemporalAdminBiz().createBulkLoader(batchSize);
	
		long oneMonthInMsec = 30l * 24l * 60l * 60l * 1000l;
		int oneYearInMonths = 12;
		int twoYearsInMonths = oneYearInMonths * 2;
		int fiveYearsInMonths = oneYearInMonths * 5;

		long todayTime = TemporalUtil.round(System.currentTimeMillis(), TemporalUtil.Resolution.DAY);
		long startTimeBase = TemporalUtil.round(todayTime - fiveYearsInMonths * oneMonthInMsec,
				TemporalUtil.Resolution.DAY);
		long endTime = TemporalUtil.MAX_TIME;

		Set<TemporalEntry<Object, KeyMap>> temporalEntrySet = null;
		if (isReturnTemporalEntrySet) {
			temporalEntrySet = new HashSet(loopCount + 1);
		}

		int totalCount = 0;
		long startValidTime;
		long endValidTime;
		long writtenTime;
		start = System.currentTimeMillis();
		for (int i = 1; i <= loopCount; i++) {
			String id = idList.get(i - 1);

			int startMonths = RANDOM.nextInt(twoYearsInMonths) + 1;
			endValidTime = startTimeBase + startMonths * oneMonthInMsec;
			endValidTime = TemporalUtil.round(endValidTime, TemporalUtil.Resolution.DAY);

			int updateCount = RANDOM.nextInt(30);
			KeyMap keyMap;
			Map<String, AttachmentSet<Object>> attachmentMap;
			for (int j = 0; j < updateCount; j++) {
				startValidTime = endValidTime;
				endValidTime = startValidTime + (RANDOM.nextInt(oneYearInMonths) + 1) * oneMonthInMsec;
				endValidTime = TemporalUtil.round(endValidTime, TemporalUtil.Resolution.DAY);

				// System.out.println(dateFormat.format(new
				// Date(startValidTime)) + " " + dateFormat.format(new
				// Date(endValidTime)));

				writtenTime = startValidTime;
				boolean isDelta = isLoadDelta && (j % 10 != 0);
				keyMap = createKeyMap(dataType, i, id, startValidTime, endValidTime, isDelta);
				Object identityKey = createIdentityWithRoutingKey(dataType, id, keyMap);
				attachmentMap = createAttachmentMap(dataType);
				ITemporalData<Object> data = loader.put(identityKey, keyMap, attachmentMap, startValidTime,
						endValidTime, writtenTime, isDelta);

				if (temporalEntrySet != null) {
					GemfireTemporalKey<Object> key = new GemfireTemporalKey(identityKey, startValidTime, endValidTime,
							writtenTime, temporalBiz.getBizContext().getUserContext().getUsername());
					temporalEntrySet.add(TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(key,
							data));
				}

				// Break if the end time is later than today so that we
				// can search now-relative data without specifying valid-at
				if (endValidTime > todayTime) {
					break;
				}
			}

			if (endValidTime <= todayTime) {
				startValidTime = endValidTime;
				endValidTime = endTime;
				writtenTime = startValidTime;
				boolean isDelta = isLoadDelta && (updateCount % 10 != 0);
				updateCount++;
				keyMap = createKeyMap(dataType, i, id, startValidTime, endValidTime, isDelta);
				Object identityKey = createIdentityWithRoutingKey(dataType, id, keyMap);
				attachmentMap = createAttachmentMap(dataType);
				ITemporalData<Object> data = loader.put(identityKey, keyMap, attachmentMap, startValidTime,
						endValidTime, writtenTime, isDelta);
				if (temporalEntrySet != null) {
					GemfireTemporalKey<Object> key = new GemfireTemporalKey(identityKey, startValidTime, endValidTime,
							writtenTime, temporalBiz.getBizContext().getUserContext().getUsername());
					temporalEntrySet.add(TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(key,
							data));
				}
			}

			totalCount += updateCount;
			if (i % batchSize == 0) {
				System.out.print(totalCount + " ");
			}

		}

		// Flushes the loader. This must be invoked at the end of the load
		// in order to ensure the remaining entries in the buffer is
		// flushed out to the cache.
		loader.flush();
		end = System.currentTimeMillis();

		if (loopCount % batchSize != 0) {
			System.out.println(loopCount);
		} else {
			System.out.println();
		}
		System.out.println();
		System.out.println("    Total count: " + totalCount);
		System.out.println("   Elapsed time: " + (end - start) / 1000 + " sec");
		System.out.println();
		System.out.println("   Bulkload complete.");
		System.out.println();

		return temporalEntrySet;
	}

	private KeyMap createKeyMap(DataType dataType, int count, String id, long startValidTime, long endValidTime,
			boolean isDelta)
	{
		KeyMap keyMap;
		switch (dataType) {
		case ACCOUNT:
			keyMap = createAccount(id, isDelta);
			break;
		case BANK:
			keyMap = createBank(id, isDelta);
			break;
		case PORTFOLIO:
			keyMap = createPortfolio(id, isDelta);
			break;
		case ACCOUNT_DETAIL:
			keyMap = createAccountDetail(id);
			break;
		case POSITION:
		default:
			switch (count % 9) {
			case 0:
				keyMap = createPosition1(id, isDelta);
				break;
			case 1:
				keyMap = createPosition2(id, isDelta);
				break;
			case 2:
				keyMap = createPosition3(id, startValidTime, endValidTime, isDelta);
				break;
			case 3:
				keyMap = createPosition4(id, startValidTime, endValidTime, isDelta);
				break;
			case 4:
				keyMap = createPosition5(id, startValidTime, endValidTime, isDelta);
				break;
			case 5:
				keyMap = createPosition6(id, startValidTime, endValidTime, isDelta);
				break;
			case 6:
				keyMap = createPosition7(id, startValidTime, endValidTime, isDelta);
				break;
			case 7:
				keyMap = createPosition8(id, startValidTime, endValidTime, isDelta);
				break;
			case 8:
			default:
				keyMap = createPosition9(id, startValidTime, endValidTime, isDelta);
				break;
			}
			break;
		}
		return keyMap;
	}

	private Object createIdentityWithRoutingKey(DataType dataType, String id, KeyMap keyMap)
	{
		switch (dataType) {
		case PORTFOLIO:
			return new PortfolioKey(id, keyMap.get(Portfolio.KAccountId));
		default:
			return id;
		}
	}

	private Map<String, AttachmentSet<Object>> createAttachmentMap(DataType dataType)
	{
		Map<String, AttachmentSet<Object>> map;
		switch (dataType) {
		case PORTFOLIO:
			AttachmentSetFactory factory = new AttachmentSetFactory();

			// accounts
			HashSet set = new HashSet(10, 1f);
			int accountCount = RANDOM.nextInt(10);
			for (int i = 0; i < accountCount; i++) {
				set.add(accountIdList.get(RANDOM.nextInt(accountIdList.size())));
			}
			AttachmentSet as = factory.createAttachmentSet("BrokerageAccounts", set);
			as.setQueryStatement(PREFIX_ACCOUNT + getRandomString(2, 1) + "*");
			as.setGridPath(PATH_ACCOUNT);
			map = new HashMap(1, 3f);
			map.put("BrokerageAccounts", as);

			// positions
			HashSet set2 = new HashSet(10, 1f);
			int positionCount = RANDOM.nextInt(10);
			for (int i = 0; i < positionCount; i++) {
				set2.add(positionIdList.get(RANDOM.nextInt(positionIdList.size())));
			}
			AttachmentSet as2 = factory.createAttachmentSet("MyStocks", set2);
			as2.setQueryStatement(PREFIX_POSITION + getRandomString(2, 1) + "*");
			as2.setGridPath(PATH_POSITION);
			map.put("MyStocks", as2);

			// AccountByName - LastName
			AttachmentSet as3 = factory.createAttachmentSet("AccountByName", new HashSet(1, 1f));
			as3.setQueryStatement("LastName:" + getRandomString(2, 1) + "*");
			as3.setGridPath(PATH_ACCOUNT);
			map.put("AccountByName", as3);
			break;

		default:
			map = null;
			break;
		}
		return map;
	}

	private long getRandomLong(int max)
	{
		return Math.abs(RANDOM.nextLong() % max);
	}

	private double getRandomPrice(int maxNumWholeDigits)
	{
		return Math.abs((double) ((long) (RANDOM.nextDouble() * (maxNumWholeDigits + 2))) / 100d);
	}

	private KeyMap newKeyMap(KeyType keyType)
	{
		KeyMap keyMap = new JsonLite(keyType);
		return keyMap;
	}

	private void putMyPorfolio(ITemporalBiz<Object, KeyMap> temporalBiz)
	{
		String portfolioId = "MyPortfolio";
		long startTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 10;
		startTime = TemporalUtil.round(startTime, TemporalUtil.Resolution.DAY);
		long endTime = TemporalUtil.MAX_TIME;
		KeyMap portfolio = createPortfolio(portfolioId, false);

		// Add a large number of positions to see if they display properly
		// in the Grid Object popup frame
		List positionList = new ArrayList();
		int size = 1122;
		if (positionIdList.size() < size) {
			size = positionIdList.size();
		}
		for (int i = 0; i < size; i++) {
			positionList.add(positionIdList.get(i));
		}
		portfolio.put(Portfolio.KPositions, positionList);

		AttachmentSet as1 = new AttachmentSetFactory().createAttachmentSet();
		as1.setGridPath(PATH_POSITION);
		as1.setName("Tech");
		HashSet<String> techSet = new HashSet();
		techSet.add(positionIdList.get(RANDOM.nextInt(positionIdList.size())));
		techSet.add(positionIdList.get(RANDOM.nextInt(positionIdList.size())));
		as1.setAttachments(techSet);

		AttachmentSet as2 = new AttachmentSetFactory().createAttachmentSet();
		as2.setGridPath(PATH_POSITION);
		as2.setName("Health");
		HashSet<String> healthSet = new HashSet();
		healthSet.add(positionIdList.get(RANDOM.nextInt(positionIdList.size())));
		healthSet.add(positionIdList.get(RANDOM.nextInt(positionIdList.size())));
		as2.setAttachments(healthSet);

		AttachmentSet as3 = new AttachmentSetFactory().createAttachmentSet();
		as3.setGridPath(PATH_ACCOUNT);
		as3.setName("Accounts");
		HashSet<String> accountSet = new HashSet();
		accountSet.add(accountIdList.get(RANDOM.nextInt(accountIdList.size())));
		accountSet.add(accountIdList.get(RANDOM.nextInt(accountIdList.size())));
		as3.setAttachments(accountSet);
		
		String accountId = colocatedPortfolioMap.get(portfolioId);
		if (accountId == null) {
			accountId = accountIdList.get(RANDOM.nextInt(accountIdList.size()));
			colocatedPortfolioMap.put(portfolioId, accountId);
		}

		AttachmentSet attachmentSets[] = new AttachmentSet[] { as1, as2, as3 };

		temporalBiz.putAttachments(new PortfolioKey(portfolioId, accountId), portfolio, attachmentSets, startTime, endTime, startTime, false);

	}

	public void addBankId(String bankId)
	{
		bankIdList.add(bankId);
	}

	public KeyMap createAccount(String accountId, boolean isDelta)
	{
		KeyMap account = newKeyMap(Account.getKeyType());
		account.put(Account.KAccountId, accountId);

		if (isDelta) {
			int index = RANDOM.nextInt(bankIdList.size());
			account.put(Account.KBankId, bankIdList.get(index));
		} else {
			account.put(Account.KAccountName, getRandomString(10, 1));
			account.put(Account.KAddress, getRandomLong(1000) + " " + getRandomString(10, 2));
			int index = RANDOM.nextInt(bankIdList.size());
			account.put(Account.KBankId, bankIdList.get(index));
			account.put(Account.KFirstName, getRandomString(10, 1));
			account.put(Account.KLastName, getRandomString(10, 1));
		}
		return account;
	}

	public KeyMap createBank(String bankId, boolean isDelta)
	{
		KeyMap bank = newKeyMap(Bank.getKeyType());
		if (isDelta) {
			if (RANDOM.nextBoolean()) {
				bank.put(Bank.KAccountNumber, getRandomString(10, 1));
				bank.put(Bank.KType, (byte) RANDOM.nextInt(10));
			} else {
				bank.put(Bank.KAccountNumber, getRandomString(10, 1));
				bank.put(Bank.KBankName, getRandomString(10, 2));
				bank.put(Bank.KRoutingNumber, getRandomString(10, 1));
			}
		} else {
			bank.put(Bank.KBankId, bankId);
			bank.put(Bank.KAccountNumber, getRandomString(10, 1));
			bank.put(Bank.KBankName, getRandomString(10, 2));
			bank.put(Bank.KRoutingNumber, getRandomString(10, 1));
			bank.put(Bank.KType, (byte) RANDOM.nextInt(10));
		}
		return bank;
	}

	public KeyMap createPortfolio(String portfolioId, boolean isDelta)
	{
		KeyMap portfolio = newKeyMap(Portfolio.getKeyType());
		portfolio.put(Portfolio.KPortfolioId, portfolioId);

		String accountId = colocatedPortfolioMap.get(portfolioId);
		if (accountId == null) {
			accountId = accountIdList.get(RANDOM.nextInt(accountIdList.size()));
			colocatedPortfolioMap.put(portfolioId, accountId);
		}
		if (isDelta) {
			portfolio.put(Portfolio.KAccountId, accountId);
		} else {
			portfolio.put(Portfolio.KAccountId, accountId);
			portfolio.put(Portfolio.KPortfolioName, getRandomString(10, 2));
			portfolio.put(Portfolio.KDescription, getRandomString(7, 5));
		}

		int count = RANDOM.nextInt(20);
		List list = new ArrayList(count);
		for (int i = 0; i < count; i++) {
			int index = RANDOM.nextInt(positionIdList.size());
			list.add(positionIdList.get(index));
		}
		portfolio.put(Portfolio.KPositions, list);
		return portfolio;
	}

	public KeyMap createAccountDetail(String accountId)
	{
		KeyMap accountDetail = newKeyMap(AccountDetail.getKeyType());

		// KPortfolios is not needed as it is referenced in run-time.
		accountDetail.put(AccountDetail.KAccountId, accountId);
		accountDetail.put(AccountDetail.KDescription, getRandomString(10, 20));
		return accountDetail;
	}

	public KeyMap createPosition1(String secId, boolean isDelta)
	{
		KeyMap position = newKeyMap(Position_v1.getKeyType());
		int index = RANDOM.nextInt(accountIdList.size());

		if (isDelta) {
			int num = RANDOM.nextInt(5);
			switch (num) {
			case 0:
				position.put(Position_v1.KAccountId, accountIdList.get(index));
				break;
			case 1:
				position.put(Position_v1.KExposure1, getRandomPrice(10000));
				position.put(Position_v1.KFiImntId, getRandomLong(10000));
				break;
			default:
				position.put(Position_v1.KMarketPlace, getRandomPrice(10000));
				break;
			}
		} else {
			position.put(Position_v1.KAccountId, accountIdList.get(index));
			position.put(Position_v1.KExposure1, getRandomPrice(10000));
			position.put(Position_v1.KFiImntId, getRandomLong(10000));
			position.put(Position_v1.KMarketPlace, getRandomPrice(10000));
			position.put(Position_v1.KOriginalCost, getRandomPrice(100));
			position.put(Position_v1.KSecId, secId);
		}
		return position;
	}

	public KeyMap createPosition2(String secId, boolean isDelta)
	{
		KeyMap position = newKeyMap(Position_v2.getKeyType());
		int index = RANDOM.nextInt(accountIdList.size());

		if (isDelta) {
			int num = RANDOM.nextInt(5);
			switch (num) {
			case 0:
				position.put(Position_v2.KAccountId, accountIdList.get(index));
				break;
			case 1:
				position.put(Position_v2.KExposure1, getRandomPrice(10000));
				position.put(Position_v2.KFiImntId, getRandomLong(10000));
				break;
			default:
				position.put(Position_v2.KMarketPlace, getRandomPrice(10000));
				position.put(Position_v2.KOriginalFace, getRandomPrice(100));
				break;
			}
		} else {
			position.put(Position_v2.KAccountId, accountIdList.get(index));
			position.put(Position_v2.KExposure1, getRandomPrice(10000));
			position.put(Position_v2.KFiImntId, getRandomLong(10000));
			position.put(Position_v2.KMarketPlace, getRandomPrice(10000));
			position.put(Position_v2.KOriginalCost, getRandomPrice(100));
			position.put(Position_v2.KOriginalFace, getRandomPrice(100));
			position.put(Position_v2.KSecId, secId);
		}
		return position;
	}

	public KeyMap createPosition3(String secId, long startValidTime, long endValidTime, boolean isDelta)
	{
		KeyMap position = newKeyMap(Position_v3.getKeyType());
		int index = RANDOM.nextInt(accountIdList.size());

		if (isDelta) {
			int num = RANDOM.nextInt(5);
			switch (num) {
			case 0:
				position.put(Position_v3.KAccountId, accountIdList.get(index));
				break;
			case 1:
				position.put(Position_v3.KExposure1, getRandomPrice(10000));
				position.put(Position_v3.KFiImntId, getRandomLong(10000));
				break;
			default:
				position.put(Position_v3.KMarketPlace, getRandomPrice(10000));
				position.put(Position_v3.KOriginalFace, getRandomPrice(100));
				position.put(Position_v3.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
				break;
			}
		} else {
			position.put(Position_v3.KAccountId, accountIdList.get(index));
			position.put(Position_v3.KExposure1, getRandomPrice(10000));
			position.put(Position_v3.KFiImntId, getRandomLong(10000));
			position.put(Position_v3.KMarketPlace, getRandomPrice(10000));
			position.put(Position_v3.KOriginalCost, getRandomPrice(100));
			position.put(Position_v3.KOriginalFace, getRandomPrice(100));
			position.put(Position_v3.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
			position.put(Position_v3.KSecId, secId);
		}
		return position;
	}

	public KeyMap createPosition4(String secId, long startValidTime, long endValidTime, boolean isDelta)
	{
		KeyMap position = newKeyMap(Position_v4.getKeyType());
		int index = RANDOM.nextInt(accountIdList.size());

		if (isDelta) {
			int num = RANDOM.nextInt(5);
			switch (num) {
			case 0:
				position.put(Position_v4.KAccountId, accountIdList.get(index));
				break;
			case 1:
				position.put(Position_v4.KExposure1, getRandomPrice(10000));
				break;
			default:
				position.put(Position_v4.KOriginalFace, getRandomPrice(100));
				position.put(Position_v4.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
				break;
			}
		} else {
			position.put(Position_v4.KAccountId, accountIdList.get(index));
			position.put(Position_v4.KExposure1, getRandomPrice(10000));
			// position.put(Position2_v4.FiImntId, getRandomLong(10000)); //
			// deprecated
			// position.put(Position2_v4.MarketPlace, getRandomPrice(10000)); //
			// deprecated
			position.put(Position_v4.KOriginalCost, getRandomPrice(100));
			position.put(Position_v4.KOriginalFace, getRandomPrice(100));
			position.put(Position_v4.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
			position.put(Position_v4.KSecId, secId);
		}
		return position;
	}

	public KeyMap createPosition5(String secId, long startValidTime, long endValidTime, boolean isDelta)
	{
		KeyMap position = newKeyMap(Position_v5.getKeyType());
		int index = RANDOM.nextInt(accountIdList.size());

		if (isDelta) {
			int num = RANDOM.nextInt(5);
			switch (num) {
			case 0:
				position.put(Position_v5.KAccountId, accountIdList.get(index));
				break;
			case 1:
				position.put(Position_v5.KExposure1, getRandomPrice(10000));
				break;
			case 2:
				position.put(Position_v5.KExposure2, getRandomPrice(10000));
				break;
			default:
				position.put(Position_v5.KOriginalFace, getRandomPrice(100));
				position.put(Position_v5.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
				break;
			}
		} else {
			position.put(Position_v5.KAccountId, accountIdList.get(index));
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
		}
		return position;
	}

	public KeyMap createPosition6(String secId, long startValidTime, long endValidTime, boolean isDelta)
	{

		KeyMap position = newKeyMap(Position_v6.getKeyType());
		int index = RANDOM.nextInt(accountIdList.size());
		if (isDelta) {
			int num = RANDOM.nextInt(5);
			switch (num) {
			case 0:
				position.put(Position_v6.KAccountId, accountIdList.get(index));
				break;
			case 1:
				position.put(Position_v6.KExposure1, getRandomPrice(10000));
				break;
			case 2:
				position.put(Position_v6.KExposure2, getRandomPrice(10000));
				break;
			default:
				position.put(Position_v6.KOriginalFace, getRandomPrice(100));
				position.put(Position_v6.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
				break;
			}
		} else {
			position.put(Position_v6.KAccountId, accountIdList.get(index));
			position.put(Position_v6.KExposure1, getRandomPrice(10000));
			position.put(Position_v6.KExposure2, getRandomPrice(10000));
			// position.put(Position2_v5.FiImntId, getRandomLong(10000)); //
			// merged
			// position.put(Position2_v5.MarketPlace, getRandomPrice(10000)); //
			// merged
			position.put(Position_v6.KOriginalCost, getRandomPrice(100));
			position.put(Position_v6.KOriginalFace, getRandomPrice(100));
			position.put(Position_v6.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
			position.put(Position_v6.KSecId, secId);
		}
		return position;
	}

	public KeyMap createPosition7(String secId, long startValidTime, long endValidTime, boolean isDelta)
	{
		KeyMap position = newKeyMap(Position_v7.getKeyType());
		int index = RANDOM.nextInt(accountIdList.size());

		if (isDelta) {
			int num = RANDOM.nextInt(6);
			switch (num) {
			case 0:
				position.put(Position_v7.KAccountId, accountIdList.get(index));
				break;
			case 1:
				position.put(Position_v7.KExposure1, getRandomPrice(10000));
				break;
			case 2:
				position.put(Position_v7.KExposure2, getRandomPrice(10000));
				break;
			case 3:
				position.put(Position_v7.KRiskFactor, getRandomPrice(100));
				break;
			default:
				position.put(Position_v7.KOriginalFace, getRandomPrice(100));
				position.put(Position_v7.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
				break;
			}
		} else {
			position.put(Position_v7.KAccountId, accountIdList.get(index));
			position.put(Position_v7.KExposure1, getRandomPrice(10000));
			position.put(Position_v7.KExposure2, getRandomPrice(10000));
			// position.put(Position2_v7.FiImntId, getRandomLong(10000)); //
			// merged
			// position.put(Position2_v7.MarketPlace, getRandomPrice(10000)); //
			// merged
			position.put(Position_v7.KOriginalCost, getRandomPrice(100));
			position.put(Position_v7.KOriginalFace, getRandomPrice(100));
			position.put(Position_v7.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
			position.put(Position_v7.KRiskFactor, getRandomPrice(100));
			position.put(Position_v7.KSecId, secId);
		}
		return position;
	}

	public KeyMap createPosition8(String secId, long startValidTime, long endValidTime, boolean isDelta)
	{
		KeyMap position = newKeyMap(Position_v8.getKeyType());
		int index = RANDOM.nextInt(accountIdList.size());

		if (isDelta) {
			int num = RANDOM.nextInt(5);
			switch (num) {
			case 0:
				position.put(Position_v8.KAccountId, accountIdList.get(index));
				break;
			case 1:
				position.put(Position_v8.KExposure1, getRandomPrice(10000));
				break;
			case 2:
				position.put(Position_v8.KExposure2, getRandomPrice(10000));
				break;
			default:
				position.put(Position_v8.KRiskFactor, getRandomPrice(100));
				position.put(Position_v8.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
				break;
			}
		} else {
			position.put(Position_v8.KAccountId, accountIdList.get(index));
			position.put(Position_v8.KExposure1, getRandomPrice(10000));
			position.put(Position_v8.KExposure2, getRandomPrice(10000));
			// position.put(Position2_v8.FiImntId, getRandomLong(10000)); //
			// merged
			// position.put(Position2_v8.MarketPlace, getRandomPrice(10000)); //
			// merged
			// position.put(Position2_v8.OriginalCost, getRandomPrice(100)); //
			// deprecated
			// position.put(Position2_v8.OriginalFace, getRandomPrice(100)); //
			// deprecated
			position.put(Position_v8.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
			position.put(Position_v8.KRiskFactor, getRandomPrice(100));
			position.put(Position_v8.KSecId, secId);
		}
		return position;
	}

	public KeyMap createPosition9(String secId, long startValidTime, long endValidTime, boolean isDelta)
	{
		KeyMap position = newKeyMap(Position_v9.getKeyType());
		int index = RANDOM.nextInt(accountIdList.size());

		if (isDelta) {
			int num = RANDOM.nextInt(5);
			switch (num) {
			case 0:
				position.put(Position_v9.KAccountId, accountIdList.get(index));
				break;
			case 1:
				position.put(Position_v9.KExposure1, getRandomPrice(10000));
				break;
			case 2:
				position.put(Position_v9.KExposure2, getRandomPrice(10000));
				break;
			default:
				position.put(Position_v9.KRiskFactor, getRandomPrice(100));
				position.put(Position_v9.KSettlementDate, new Date((startValidTime + endValidTime) / 2));
				break;
			}
		} else {
			position.put(Position_v9.KAccountId, accountIdList.get(index));
			position.put(Position_v9.KExposure1, getRandomPrice(10000));
			position.put(Position_v9.KExposure2, getRandomPrice(10000));
			// position.put(Position2_v9.FiImntId, getRandomLong(10000)); //
			// merged
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
		}
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

	private List<String> createIdentityKeyList(String prefix, int entryCount, int maxStringLength)
	{
		// Get a random identity key but unique
		List<String> list = new ArrayList();
		if (prefix == null) {
			for (int i = 0; i < entryCount; i++) {
				String key = getRandomString(maxStringLength, 1);
				while (list.contains(key)) {
					key = getRandomString(maxStringLength, 1);
				}
				list.add(key);
			}
		} else {
			for (int i = 0; i < entryCount; i++) {
				String key = prefix + getRandomString(maxStringLength, 1);
				while (list.contains(key)) {
					key = prefix + getRandomString(maxStringLength, 1);
				}
				list.add(key);
			}
		}
		return list;
	}
}
