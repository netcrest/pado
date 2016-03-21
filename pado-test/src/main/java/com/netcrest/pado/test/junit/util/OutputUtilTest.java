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
package com.netcrest.pado.test.junit.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gemstone.gemfire.internal.Assert;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.internal.util.OutputUtil;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalData;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalKey;
import com.netcrest.pado.temporal.test.TemporalLoader;
import com.netcrest.pado.temporal.test.data.Bank;

public class OutputUtilTest
{
	static TemporalLoader temporalLoader;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		System.setProperty("keyMapType", "jsonlite");
		temporalLoader = new TemporalLoader();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testOutputUtil()
	{
		String user = System.getProperty("user.name");
		Map<ITemporalKey, ITemporalData> map = new HashMap();
		ITemporalKey key = null;
		ITemporalData data = null;
		for (int i = 0; i < 100; i++) {
			long time = System.currentTimeMillis();
			KeyMap keyMap  = temporalLoader.createBank("MyBank" + i, false);
			key = new GemfireTemporalKey(keyMap.get(Bank.KBankId), time, time, time, user);
			data = new GemfireTemporalData(key, keyMap);
			map.put(key, data);
		}
		
		Object value = data;
		if (value instanceof TemporalData) {
			TemporalData td = (TemporalData) value;
			value = td.getValue();
		}
		List keyList = null;
		if (value instanceof Map) {
			// Must iterate the entire map to get all unique keys
			Map valueMap = (Map)value;
			Set keySet = valueMap.keySet();
			HashSet set = new HashSet(keySet.size(), 1f);
			for (Object k : keySet) {
				set.add(k);
			}
			keyList = new ArrayList(set);
			Collections.sort(keyList);
		}
		SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		File schemaFile = new File("data/dump/account-server1.schema");
		File csvFile = new File("data/dump/account-server1.csv");
		String gridPath = "account";
		try {
			PrintWriter schemaWriter = new PrintWriter(schemaFile);
			OutputUtil.printSchema(schemaWriter, gridPath, key, data, keyList, OutputUtil.TYPE_KEYS_VALUES, ",", iso8601DateFormat, true, true, null);
			schemaWriter.flush();
			schemaWriter.close();
			PrintWriter csvWriter = new PrintWriter(csvFile);
			OutputUtil.printEntries(csvWriter, map, ",", OutputUtil.TYPE_KEYS_VALUES, new SimpleDateFormat("MM/dd/yyyy"), true);
			csvWriter.flush();
			csvWriter.close();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
		
	}

}
