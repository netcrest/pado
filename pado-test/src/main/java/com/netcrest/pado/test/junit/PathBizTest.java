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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.exception.PadoLoginException;

public class PathBizTest
{
	private static IPado pado;
	private static IPathBiz pathBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		pathBiz = catalog.newInstance(IPathBiz.class);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

//	@Test
	public void testCreateTemporalPath()
	{
		String gridId = "mygrid";
		String gridPath = "dynamic_temporal";
		String colocatedWithGridPath = null;
		int redundantCopies = 1;
		int totalBucketCount = 40;
		boolean created = pathBiz.createPath(gridId, gridPath, IPathBiz.PathType.TEMPORAL, null, null, colocatedWithGridPath, redundantCopies, totalBucketCount, false);
		Assert.assertTrue(created);
	}
	
//	@Test
	public void testClearTemporalPath()
	{
		String gridId = "mygrid";
		String gridPath = "dynamic_temporal";
		pathBiz.clear(gridId, gridPath, true);
	}

	@Test
	public void testRemoveTemporalPath()
	{
		String gridId = "mygrid";
		String gridPath = "dynamic_temporal";
		pathBiz.remove(gridId, gridPath, true);
	}
}
