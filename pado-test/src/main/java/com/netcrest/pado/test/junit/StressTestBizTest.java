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
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.test.biz.IStressTestBiz;

public class StressTestBizTest
{
	private static IPado pado;
	
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{		
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
//		System.setProperty("gemfireSecurityPropertyFile", "etc/client/gfsecurity.properties");
//		System.setProperty("pado.security.aes.user.certificate", "etc/user.cer");
//		System.setProperty("pado.security.enabled", "true");
		Pado.connect("localhost:20100", true);
		pado = Pado.login("sys", "netcrest", "test1", "test123".toCharArray());
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
	@Test
	public void testStressTestBiz()
	{
		IStressTestBiz stressBiz = pado.getCatalog().newInstance(IStressTestBiz.class);
		stressBiz.setIncludeObjectCreationTime(false);
		stressBiz.addPath("/mygrid/test1", 100, 10, 0, 10000, 1000);
//		stressBiz.addPath("stress/test1", 100, 10, 0, 10000, 1000);
//		stressBiz.addPath("stress/test1");
//		stressBiz.addPath("stress/test3");
		stressBiz.start();
	}
}
