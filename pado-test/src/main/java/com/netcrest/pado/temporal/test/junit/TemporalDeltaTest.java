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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.temporal.ITemporalAdminBizLink;
import com.netcrest.pado.temporal.ITemporalBulkLoader;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.test.data.ConcretePosition;
import com.netcrest.pado.temporal.test.data.ConcretePositionImpl;

/**
 * TemporalDeltaTest requires "mygrid".
 * @author dpark
 *
 */
public class TemporalDeltaTest
{
	private static IPado pado;
	private static ITemporalBiz<String, ConcretePosition> temporalBiz;
	private static Date[] dates;
	private static String identityKeys[] = new String[] { "pos1", "pos2", "pos3", "pos4", "pos5" };
	private static int expectedTemporalListSize = 0;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		temporalBiz = catalog.newInstance(ITemporalBiz.class, "temporal");
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

	private void putDeltaConcretePostion(ITemporalBiz<String, ConcretePosition> temporalBiz, String identityKey)
	{
		ConcretePosition pos = createConcretePosition(identityKey);
		expectedTemporalListSize = 0;

		// valid [0, &] @0 -- 0
		temporalBiz.put(identityKey, pos, dates[0].getTime(), Long.MAX_VALUE, dates[0].getTime(), false);
		expectedTemporalListSize++;

		// valid [1, &] @1 -- 1
		pos.setAccrualAm(new BigDecimal("10.1"));
		temporalBiz.put(identityKey, pos, dates[1].getTime(), Long.MAX_VALUE, dates[1].getTime(), true);
		expectedTemporalListSize++;

		// valid [2, &] @2 -- 2
		pos.setAccrualAm(new BigDecimal("10.2"));
		pos.setCurrFaceAm(new BigDecimal("12.1"));
		pos.setMkPr(new BigDecimal("13.1"));
		temporalBiz.put(identityKey, pos, dates[2].getTime(), Long.MAX_VALUE, dates[2].getTime(), true);
		expectedTemporalListSize++;

		// valid [3, &] @3 -- 3
		pos.setAccrualAm(new BigDecimal("10.3"));
		pos.setMkPr(new BigDecimal("13.2"));
		pos.setTavAm(new BigDecimal("18.2"));
		temporalBiz.put(identityKey, pos, dates[3].getTime(), Long.MAX_VALUE, dates[3].getTime(), true);
		expectedTemporalListSize++;

	}

	private ConcretePosition createConcretePosition(String identityKey)
	{
		Date startDate = dates[0];
		ConcretePosition pos = new ConcretePositionImpl();
		pos.setAccountCd("acctCd");
		pos.setAccountId(1000l);
		pos.setAccrualAm(new BigDecimal("10.0"));
		pos.setAsOfDt(startDate);
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

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testDelta() throws Exception
	{
		// First, clear the temporal lists
		ITemporalAdminBizLink adminBiz = temporalBiz.getTemporalAdminBiz();
		for (String identityKey : identityKeys) {
			adminBiz.clearTemporalList(identityKey);
		}
		
		// Put data/deltas for each identity key
		for (String identityKey : identityKeys) {
			putDeltaConcretePostion(temporalBiz, identityKey);
		}

		// Do bulkload of deltas
		ITemporalBulkLoader bulkLoader = temporalBiz.getTemporalAdminBiz().createBulkLoader(10);
		expectedTemporalListSize++;

		for (String identityKey : identityKeys) {

			// Get the now-relative position
			ConcretePosition pos = temporalBiz.get(identityKey);

			// change AccrualAm - delta
			pos.setAccrualAm(new BigDecimal("100.00"));
			// set same value for MkPr - no delta
			pos.setMkPr(new BigDecimal(pos.getMkPr().toString()));
			if (true) {
				bulkLoader.put(identityKey, pos, null, dates[4].getTime(), Long.MAX_VALUE, dates[4].getTime(), true);
			}
		}
		bulkLoader.flush();
	
		// Dump
		for (String identityKey : identityKeys) {
			TemporalDataList list = temporalBiz.getTemporalAdminBiz().getTemporalDataList(identityKey);
			list.dump(new SimpleDateFormat("yyyy-MM-dd HHmmss.SSS"));
			Assert.assertEquals(expectedTemporalListSize, list.getTemporalList().size());
		}
	}

	@Test
	public void testDeltaMemorySize() throws Exception
	{
		// Set start, stop to the position key range
		int start = 10001;
		int end = 20000;

		ITemporalBulkLoader bulkLoader = temporalBiz.getTemporalAdminBiz().createBulkLoader(1000);
		String identityKeys[] = new String[end - start + 1];
		ArrayList<ConcretePosition> posList = new ArrayList(end - start + 1);
		int index = 0;
		for (int i = start; i <= end; i++) {
			String identityKey = "pos" + i;
			identityKeys[index++] = identityKey;
			ConcretePosition pos = createConcretePosition(identityKey);
			// first object is a whole object
			bulkLoader.put(identityKey, pos, null, dates[0].getTime(), Long.MAX_VALUE, dates[0].getTime(), false);
			posList.add(pos);
		}
		bulkLoader.flush();
		
		// deltas
		for (int i = 1; i <= 5; i++) {
			index = 0;
			for (String identityKey : identityKeys) {
				ConcretePosition pos = posList.get(index++);
				// change AccrualAm - delta
				pos.setAccrualAm(new BigDecimal("10" + i + ".00"));
				bulkLoader.put(identityKey, pos, null, dates[i].getTime(), Long.MAX_VALUE, dates[i].getTime(), true);
			}
		}
		bulkLoader.flush();
	}
}
