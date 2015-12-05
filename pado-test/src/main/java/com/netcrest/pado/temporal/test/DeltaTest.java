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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.temporal.ITemporalBulkLoader;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.test.data.ConcretePosition;
import com.netcrest.pado.temporal.test.data.ConcretePositionImpl;
import com.netcrest.pado.temporal.test.data.v.Position_v5;

public class DeltaTest
{
	IPado pado;
	ICatalog catalog;
	ITemporalBiz<String, Object> temporalBiz;

	Date[] dates;

	public DeltaTest(String gridPath)
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		login("sys", "localhost:20000", gridPath);
		dates = createDates(7);
	}

	private Date[] createDates(int dateCount)
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
					// bump up the year if january
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

	private void login(String appId, String locators, String gridPath) throws PadoLoginException
	{
		Pado.connect(locators, true);
		pado = Pado.login(appId, "netcrest", "dpark", "dpark".toCharArray());
		catalog = pado.getCatalog();
		temporalBiz = catalog.newInstance(ITemporalBiz.class, gridPath);
	}

	private void putDeltaKeyMap(ITemporalBiz temporalBiz, String identityKey, KeyMap keyMap)
	{
		System.out.println();
		System.out.println("putDeltaKeyMap()");
		System.out.println("----------------");
		System.out.println();
		System.out.println("identityKey=" + identityKey);
		System.out.println();
		keyMap.put(Position_v5.KSecId, identityKey);
		keyMap.put(Position_v5.KAccountId, 1l);
		keyMap.put(Position_v5.KExposure1, 1.0d);
		keyMap.put(Position_v5.KExposure2, 2.0d);
		keyMap.put(Position_v5.KFiImntId, 1l);
		keyMap.put(Position_v5.KMarketPlace, 10d);
		keyMap.put(Position_v5.KOriginalCost, 9d);
		keyMap.put(Position_v5.KOriginalFace, 8d);
		keyMap.put(Position_v5.KSettlementDate, dates[0]);

		// valid [1, &] @1 -- 0
		temporalBiz.put(identityKey, keyMap, 1, Long.MAX_VALUE, 1, false);

		// valid [5, &] @5 -- 1
		keyMap.put(Position_v5.KExposure1, 1.1d);
		keyMap.put(Position_v5.KExposure2, 2.1d);
		temporalBiz.put(identityKey, keyMap, 5, Long.MAX_VALUE, 5, true);

		// valid [8, &] @10 -- 2
		keyMap.put(Position_v5.KExposure1, 1.2d);
		keyMap.put(Position_v5.KExposure2, 2.2d);
		temporalBiz.put(identityKey, keyMap, 8, Long.MAX_VALUE, 10, true);

		// valid [10, &] @15 -- 3
		keyMap.put(Position_v5.KExposure1, 1.3d);
		keyMap.put(Position_v5.KExposure2, 2.3d);
		temporalBiz.put(identityKey, keyMap, 15, Long.MAX_VALUE, 15, true);

		System.out.println();
		temporalBiz.dump(identityKey);
		System.out.println();
	}

	private ConcretePosition createConcretePosition(String identityKey)
	{
		Date startDate = dates[0];
		ConcretePosition pos = new ConcretePositionImpl();
		pos.setAccountCd("acctCd");
		pos.setAccountId(1000l);
		pos.setAccrualAm(new BigDecimal("10.0"));
		pos.setBvAm(new BigDecimal("11.0"));
		pos.setCurrFaceAm(new BigDecimal("12.0"));
		pos.setFiImntId(2000l);
		pos.setImntAltCd("imnt");
		pos.setMkPr(new BigDecimal("13.0"));
		pos.setMvAm(new BigDecimal("14.0"));
		pos.setNavAm(new BigDecimal("15.0"));
		pos.setOrgFaceAm(new BigDecimal("16.0"));
		pos.setParAm(new BigDecimal("17.0"));
		pos.setPositionCd("posCd");
		pos.setTavAm(new BigDecimal("18.0"));
		pos.setUuid(identityKey);
		return pos;
	}
	
	private void putDeltaConcretePostion(ITemporalBiz<String, Object> temporalBiz, String identityKey)
	{
		System.out.println();
		System.out.println("putDeltaConcretePostion()");
		System.out.println("-------------------------");
		System.out.println();
		System.out.println("identityKey=" + identityKey);
		System.out.println();
		
		ConcretePosition pos = createConcretePosition(identityKey);
		
		// valid [1, &] @1 -- 0
		temporalBiz.put(identityKey, pos, dates[0].getTime(), Long.MAX_VALUE, dates[0].getTime(), false);

		// valid [5, &] @5 -- 1
		pos.setAccrualAm(new BigDecimal("10.1"));
		temporalBiz.put(identityKey, pos, dates[1].getTime(), Long.MAX_VALUE, dates[1].getTime(), true);

		// valid [8, &] @10 -- 2
		pos.setAccrualAm(new BigDecimal("10.2"));
		pos.setCurrFaceAm(new BigDecimal("12.1"));
		pos.setMkPr(new BigDecimal("13.1"));
		temporalBiz.put(identityKey, pos, dates[2].getTime(), Long.MAX_VALUE, dates[2].getTime(), true);

		// valid [10, &] @15 -- 3
		pos.setAccrualAm(new BigDecimal("10.3"));
		pos.setMkPr(new BigDecimal("13.2"));
		pos.setTavAm(new BigDecimal("18.2"));
		temporalBiz.put(identityKey, pos, dates[3].getTime(), Long.MAX_VALUE, dates[3].getTime(), true);

		System.out.println();
		temporalBiz.dump(identityKey, System.out, new SimpleDateFormat("yyyy-MM-dd HHmmss.SSS"));
		System.out.println();
	}

	private Object getValue(ITemporalBiz temporalBiz, String identityKey, long validAt)
	{
		Object value = temporalBiz.get(identityKey, validAt);
		System.out.println("IdentityKey= " + identityKey + "  validAt=" + validAt);
		System.out.println("   " + value);
		return value;
	}
	
	public void testJsonLite(String identityKey)
	{
		putDeltaKeyMap(temporalBiz, identityKey, new JsonLite());

		getValue(temporalBiz, identityKey, dates[0].getTime()); // checkpoint
		getValue(temporalBiz, identityKey, dates[1].getTime()); // delta
		getValue(temporalBiz, identityKey, dates[2].getTime()); // delta
		getValue(temporalBiz, identityKey, dates[3].getTime()); // delta

	}

	public void dumpTemporalList(String identityKey)
	{
		System.out.println();
		temporalBiz.dump(identityKey);
		System.out.println();
	}

	public void testConcretePosition(String identityKey)
	{
		putDeltaConcretePostion(temporalBiz, identityKey); // checkpoint

		getValue(temporalBiz, identityKey, dates[0].getTime()); // checkpoint
		getValue(temporalBiz, identityKey, dates[1].getTime()); // delta
		getValue(temporalBiz, identityKey, dates[2].getTime()); // delta
		getValue(temporalBiz, identityKey, dates[3].getTime()); // delta

		System.out.println();
		temporalBiz.dump(identityKey);
		System.out.println();
	}
	
	public void testBulkLoader(String...identityKeys)
	{
		System.out.println("DeltaTest.testBulkLoader()");
		System.out.println("--------------------------");
		ITemporalBulkLoader bulkLoader = temporalBiz.getTemporalAdminBiz().createBulkLoader(10);
		for (String identityKey : identityKeys) {
			
			// Get the now-relative position
			ConcretePosition pos = (ConcretePosition)temporalBiz.get(identityKey);
			
			// change AccrualAm - delta
			pos.setAccrualAm(new BigDecimal("100.00"));
			// set same value for MkPr - no delta
			pos.setMkPr(new BigDecimal(pos.getMkPr().toString()));
			if (true) {
				bulkLoader.put(identityKey, pos, null, dates[4].getTime(), Long.MAX_VALUE, dates[4].getTime(), true);
			}
		}
		
		bulkLoader.flush();
		
		// dump
		for (String identityKey : identityKeys) {
			TemporalDataList list = temporalBiz.getTemporalAdminBiz().getTemporalDataList(identityKey);
			list.dump();
		}
	}

	public static void main(String args[])
	{
		// Set start, stop to the position key range
		int start = 1;
		int end = 5;
		
		DeltaTest deltaTest = new DeltaTest("position");
		String identityKeys[] = new String[end-start+1];
		int index = 0;
		for (int i = start; i <= end; i++) {
			identityKeys[index] = "pos" + i;
			deltaTest.testConcretePosition(identityKeys[index]);
			index++;
		}
		deltaTest.testBulkLoader(identityKeys);
	}
}
