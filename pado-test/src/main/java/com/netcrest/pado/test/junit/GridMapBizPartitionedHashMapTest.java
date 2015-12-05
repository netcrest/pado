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

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IGridMapBiz;

/**
 * Tests IGridMapBiz on a partitioned path. This class and
 * {@link GridMapBizReplicatedTest} are identical except that it tests a
 * partitioned path (test/partitioned). The test case results should be
 * identical except for some constraints enforced by the underlying data grid.
 * For example, the {@link IGridMapBiz#clear()} method may throw an
 * PadoException for partitioned paths because it may not be supported by the
 * underlying data grid.
 * 
 * @author dpark
 * 
 */
public class GridMapBizPartitionedHashMapTest
{
	static IPado pado;
	static String appId = "test";
	static String GRID_PATH = "test/partitioned";
	static IGridMapBiz<String, HashMap> gridMapBiz;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		if (Pado.isClosed()) {
			Pado.connect("localhost:21000", false);
		}
		pado = Pado.login(appId, "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		gridMapBiz = catalog.newInstance(IGridMapBiz.class, GRID_PATH);
	}

	@Test
	public void putData()
	{
		int count = 10;
		for (int i = 0; i < count; i++) {
			String key = "put" + i;
			HashMap<String,  String> value = new HashMap<String, String>();
			for (int j = 0; j < 10; j++) {
				value.put(j+ "", "value" + j);
			}
			if (i == 0 || i == 5) {
				value.put("LAST_UPDATE", "DATA_LOADED");
			} else {
				value.put("LAST_UPDATE",  "PROCESSING");
			}
			gridMapBiz.put(key, value);
		}
	}

}
