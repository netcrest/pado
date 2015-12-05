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
package com.netcrest.pado.temporal.test.junit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.temporal.ITemporalBulkLoader;
import com.netcrest.pado.temporal.test.data.JsonLiteDeltaDisabledPosition;
import com.netcrest.pado.temporal.test.data.JsonLiteDeltaEnabledPosition;

/**
 * TemporalDeltaComparisonTest requires "mygrid". This test case compares data
 * sizes of temporal and non-temporal data. In this test, with temporal delta
 * enabled, you will see the memory footprint reduction of over 50% for changes
 * in a single object field. The reduction linearly decreases as the number of
 * fields increases. With all of the fields changed, the temporal data takes up
 * more memory than the non-temporal data by about 7%. See the results below.
 * <p>
 * To run this test case, follow the steps below:
 * <ol>
 * <li>Start "mygrid" with one (1) server.</li>
 * <li>Run this test case.</li>
 * <li>Open VSD and chart PartitionedRegionStats/dataStoreBytesInUse of
 * /mygrid/nontemporal and /mygrid/temporal.</li>
 * <li>Repeat the above steps after increasing the number of fields by editing
 * the {@link #testTemporalDelta()} method. See instructions in the method.</li>
 * </ol>
 * <p>
 * 
 * <pre>
 * Delta  Entries  Temporal     Non-temporal  Diff        Memory 
 * Count           (bytes)      (bytes)       (bytes)     Overhead
 * -----  -------  ----------   ------------  ----------  --------
 * 1      60,000   10,768,894   16,353,364    -5,584,470   -52%
 * 2      60,000   11,218,894   16,353,364    -5,134,470   -46%
 * 3      60,000   11,668,894   16,353,364    -4,684,470   -40%
 * 4      60,000   12,118,894   16,353,364    -4,234,470   -35%
 * 5      60,000   12,568,894   16,353,364    -3,784,470   -30%
 * 6      60,000   13,018,894   16,353,364    -3,334,470   -26%
 * 7      60,000   13,468,894   16,353,364    -2,884,470   -21%
 * 8      60,000   13,918,894   16,353,364    -2,434,470   -17%
 * 9      60,000   14,368,894   16,353,364    -1,984,470   -14%
 * All    60,000   17,513,364   16,353,364    -1,160,000     7% 
 * 
 * *10,000 unique identity keys with 6 updates per identity key
 * </pre>
 * 
 * @author dpark
 * 
 */
public class TemporalJsonLiteDeltaComparisonTest
{
	private static IPado pado;
	private static IGridMapBiz<String, JsonLite> nontemporalMap;
	private static ITemporalBiz<String, JsonLite> temporalDeltaBiz;
	private static Date[] dates;

	private static int startIdentityKeyNum = 1;
	private static int endIdentityKeyNum = 10000;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		temporalDeltaBiz = catalog.newInstance(ITemporalBiz.class, "temporal");
		nontemporalMap = catalog.newInstance(IGridMapBiz.class, "nontemporal");
		dates = createDates(7);
	}

	private static Date[] createDates(int dateCount)
	{
		ArrayList<Date> dateList = new ArrayList<Date>();
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			int todayMonth = calendar.get(Calendar.MONTH);

			// Go back to a past half way and create months into future from
			// there
			int pastCount = dateCount / 2;
			int firstMonth = todayMonth - pastCount;
			firstMonth = firstMonth % 12;
			if (firstMonth == 0) {
				calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
			}
			calendar.set(Calendar.MONTH, firstMonth);

			// Future dates
			for (int i = 0; i < dateCount; i++) {
				int nextMonth = firstMonth + i;
				nextMonth = nextMonth % 12;
				if (nextMonth == 0) {
					// bump up the year if January
					calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
				}
				calendar.set(Calendar.MONTH, nextMonth);
				dateList.add(calendar.getTime());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return (Date[]) dateList.toArray(new Date[dateList.size()]);
	}

	private void putJsonLiteDeltaEnabledPosition(ITemporalBiz<String, JsonLite> temporalBiz, String identityKey)
	{
		JsonLite pos = createJsonLiteDeltaEnabledPosition(identityKey);

		// valid [0, &] @0 -- 0
		temporalBiz.put(identityKey, pos, dates[0].getTime(), Long.MAX_VALUE, dates[0].getTime(), false);

		// valid [1, &] @1 -- 1
		pos.put(JsonLiteDeltaEnabledPosition.KAccrualAm, 10.1d);
		temporalBiz.put(identityKey, pos, dates[1].getTime(), Long.MAX_VALUE, dates[1].getTime(), true);

		// valid [2, &] @2 -- 2
		pos.put(JsonLiteDeltaEnabledPosition.KAccrualAm, 10.2d);
		pos.put(JsonLiteDeltaEnabledPosition.KCurrFaceAm, 12.1d);
		pos.put(JsonLiteDeltaEnabledPosition.KMkPr, 13.1d);
		temporalBiz.put(identityKey, pos, dates[2].getTime(), Long.MAX_VALUE, dates[2].getTime(), true);

		// valid [3, &] @3 -- 3
		pos.put(JsonLiteDeltaEnabledPosition.KAccrualAm, 10.3d);
		pos.put(JsonLiteDeltaEnabledPosition.KMkPr, 13.2d);
		pos.put(JsonLiteDeltaEnabledPosition.KTavAm, 18.2d);
		temporalBiz.put(identityKey, pos, dates[3].getTime(), Long.MAX_VALUE, dates[3].getTime(), true);
	}

	private JsonLite createJsonLiteDeltaDisabledPosition(String identityKey)
	{
		Date startDate = dates[0];
		JsonLite pos = new JsonLite(JsonLiteDeltaDisabledPosition.getKeyType());
		pos.put(JsonLiteDeltaDisabledPosition.KAccountCd, "acctCd");
		pos.put(JsonLiteDeltaDisabledPosition.KAccountId, 1000l);
		pos.put(JsonLiteDeltaDisabledPosition.KAccrualAm, 10.0d);
		pos.put(JsonLiteDeltaDisabledPosition.KAsOfDt, startDate);
		pos.put(JsonLiteDeltaDisabledPosition.KBvAm, 11.0d);
		pos.put(JsonLiteDeltaDisabledPosition.KCurrFaceAm, 12.0d);
		pos.put(JsonLiteDeltaDisabledPosition.KFiImntId, 20000l);
		pos.put(JsonLiteDeltaDisabledPosition.KImntAltCd, "imnt");
		pos.put(JsonLiteDeltaDisabledPosition.KMkPr, 13.0d);
		pos.put(JsonLiteDeltaDisabledPosition.KMvAm, 14.0d);
		pos.put(JsonLiteDeltaDisabledPosition.KNavAm, 15.0d);
		pos.put(JsonLiteDeltaDisabledPosition.KOrgFaceAm, 16.0d);
		pos.put(JsonLiteDeltaDisabledPosition.KParAm, 17.0d);
		pos.put(JsonLiteDeltaDisabledPosition.KPositionCd, "posCd");
		pos.put(JsonLiteDeltaDisabledPosition.KTavAm, 18.0d);
		pos.put(JsonLiteDeltaDisabledPosition.KUuid, identityKey);
		return pos;
	}

	private JsonLite createJsonLiteDeltaEnabledPosition(String identityKey)
	{
		Date startDate = dates[0];
		JsonLite pos = new JsonLite(JsonLiteDeltaEnabledPosition.getKeyType());
		pos.put(JsonLiteDeltaEnabledPosition.KAccountCd, "acctCd");
		pos.put(JsonLiteDeltaEnabledPosition.KAccountId, 1000l);
		pos.put(JsonLiteDeltaEnabledPosition.KAccrualAm, 10.0d);
		pos.put(JsonLiteDeltaEnabledPosition.KAsOfDt, startDate);
		pos.put(JsonLiteDeltaEnabledPosition.KBvAm, 11.0d);
		pos.put(JsonLiteDeltaEnabledPosition.KCurrFaceAm, 12.0d);
		pos.put(JsonLiteDeltaEnabledPosition.KFiImntId, 20000l);
		pos.put(JsonLiteDeltaEnabledPosition.KImntAltCd, "imnt");
		pos.put(JsonLiteDeltaEnabledPosition.KMkPr, 13.0d);
		pos.put(JsonLiteDeltaEnabledPosition.KMvAm, 14.0d);
		pos.put(JsonLiteDeltaEnabledPosition.KNavAm, 15.0d);
		pos.put(JsonLiteDeltaEnabledPosition.KOrgFaceAm, 16.0d);
		pos.put(JsonLiteDeltaEnabledPosition.KParAm, 17.0d);
		pos.put(JsonLiteDeltaEnabledPosition.KPositionCd, "posCd");
		pos.put(JsonLiteDeltaEnabledPosition.KTavAm, 18.0d);
		pos.put(JsonLiteDeltaEnabledPosition.KUuid, identityKey);
		return pos;
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testNonTemporalNoDelta() throws Exception
	{
		long startTime = System.currentTimeMillis();

		// initial objects
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Map<String, JsonLite> map = new HashMap(1000, 1f);
		ArrayList<JsonLite> posList = new ArrayList(endIdentityKeyNum - startIdentityKeyNum + 1);
		for (int i = startIdentityKeyNum; i <= endIdentityKeyNum; i++) {
			String identityKey = "pos" + i;
			JsonLite pos = createJsonLiteDeltaDisabledPosition(identityKey);
			map.put(identityKey + "." + formatter.format(dates[0]), pos);
			if (i % 1000 == 0) {
				nontemporalMap.putAll(map);
				map.clear();
			}
			posList.add(pos);
		}
		if (map.isEmpty() == false) {
			nontemporalMap.putAll(map);
			map.clear();
		}

		// modified objects - no delta
		for (int i = 1; i <= 5; i++) {
			for (JsonLite pos : posList) {
				// change AccrualAm - delta
				pos.put(JsonLiteDeltaDisabledPosition.KAccrualAm, 100d  + i);
				String identityKey = (String)pos.get(JsonLiteDeltaDisabledPosition.KUuid);
				map.put(identityKey + "." + formatter.format(dates[i]), pos);
				if (i % 1000 == 0) {
					nontemporalMap.putAll(map);
					map.clear();
				}
			}
		}
		if (map.isEmpty() == false) {
			nontemporalMap.putAll(map);
			map.clear();
		}

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Non-temporal Elapsed (msec): " + elapsedTime);
	}
	
	@Test
	public void testTemporalPut() throws Exception
	{
		String identityKey = System.currentTimeMillis() + "";
		JsonLite pos = createJsonLiteDeltaEnabledPosition(identityKey);
		temporalDeltaBiz.put(identityKey, pos, 1, 10, 1, true);
	}

	@Test
	public void testTemporalDelta() throws Exception
	{
		long startTime = System.currentTimeMillis();
		ITemporalBulkLoader bulkLoader = temporalDeltaBiz.getTemporalAdminBiz().createBulkLoader(1000);
		String identityKeys[] = new String[endIdentityKeyNum - startIdentityKeyNum + 1];
		ArrayList<JsonLite> posList = new ArrayList(endIdentityKeyNum - startIdentityKeyNum + 1);
		for (int i = startIdentityKeyNum; i <= endIdentityKeyNum; i++) {
			String identityKey = "pos" + i;
			JsonLite pos = createJsonLiteDeltaEnabledPosition(identityKey);
			// first object is a whole object
			bulkLoader.put(identityKey, pos, null, dates[0].getTime(), Long.MAX_VALUE, dates[0].getTime(), false);
			posList.add(pos);
		}
		bulkLoader.flush();

		// Deltas
		// Uncomment the setters to increase the number of delta fields.
		for (int i = 1; i <= 5; i++) {
			for (JsonLite pos : posList) {
				// change AccrualAm - delta
				pos.put(JsonLiteDeltaEnabledPosition.KAccrualAm, 100d + i);
//				pos.put(JsonLiteDeltaEnabledPosition.BvAm, 100d + i);
//				pos.put(JsonLiteDeltaEnabledPosition.CurrFaceAm, 100d + i);
//				pos.put(JsonLiteDeltaEnabledPosition.MkPr, 100d + i);
//				pos.put(JsonLiteDeltaEnabledPosition.MvAm, 100d + i);
//				pos.put(JsonLiteDeltaEnabledPosition.NavAm, 100d + i);
//				pos.put(JsonLiteDeltaEnabledPosition.OrgFaceAm, 100d + i);
//				pos.put(JsonLiteDeltaEnabledPosition.ParAm, 100d + i);
//				pos.put(JsonLiteDeltaEnabledPosition.TavAm, 100d + i);
				String identityKey = (String)pos.get(JsonLiteDeltaEnabledPosition.KUuid);
				bulkLoader.put(identityKey, pos, null, dates[i].getTime(), Long.MAX_VALUE, dates[i].getTime(), true);
			}
		}
		bulkLoader.flush();

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Temporal elapsed (msec): " + elapsedTime);
	}
}
