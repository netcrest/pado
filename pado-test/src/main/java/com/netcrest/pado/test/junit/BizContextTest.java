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

import java.io.InvalidObjectException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.context.IDataInfo;
import com.netcrest.pado.context.ISimpleDataContext;
import com.netcrest.pado.context.ISimpleUserContext;
import com.netcrest.pado.context.IUserInfo;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.test.biz.IPartitionedRegionBiz;
import com.netcrest.pado.test.biz.TestException;
import com.netcrest.pado.test.biz.impl.gemfire.PartitionedRegionBizImplLocalOverride;
import com.netcrest.pado.test.junit.context.IPbmDataInfo;
import com.netcrest.pado.test.junit.context.IPbmDataContext;
import com.netcrest.pado.test.junit.context.IPbmUserContext;
import com.netcrest.pado.test.junit.context.IPbmUserInfo;
import com.netcrest.pado.test.junit.context.data.PbmDataInfoKey;
import com.netcrest.pado.test.junit.context.data.PbmUserInfoKey;

public class BizContextTest
{

	private static IPado pado;
	private static IPartitionedRegionBiz prBiz;
	private static IPartitionedRegionBiz prBizLocal;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		System.setProperty("gemfireSecurityPropertyFile", "etc/client/gfsecurity.properties");
		System.setProperty("pado.properties", "etc/client/pado.properties");
		System.setProperty("pado.security.enabled", "true");
		if (Pado.isClosed()) {
			Pado.connect("localhost:20000", false);
		}
		try {
			pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		ICatalog catalog = pado.getCatalog();
		
		// Local implementation PartitionedRegionBizLocalImpl is defined 
		// by @BizClass.
		prBiz = catalog.newInstance(IPartitionedRegionBiz.class);
		// Local implementation of IBiz that overrides the default local implementation
		// designated by @BizClass.
		prBizLocal = catalog.newInstanceLocal(IPartitionedRegionBiz.class,
				new PartitionedRegionBizImplLocalOverride());
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testBizLocal()
	{
		int x = 1;
		int y = 2;
		
		// Invoke the remote biz method, which returns the result of x + y.
		int z = prBiz.add(x, y);
		Assert.assertTrue( z == x + y);
		
		// Invoke the local biz method, which increases the value of x by 10
		// and invokes the remote biz method.
		z = prBizLocal.add(x, y);
		Assert.assertTrue( z == (x + 10) + y);
	}

	@Test
	public void testPadoException()
	{
		System.out.println("BizContextTest.testPadoException()");
		System.out.println("----------------------------------");
		// Exception not expected
		try {
			int length = prBiz.testPadoException("hello, world");
		} catch (TestException e) {
			e.printStackTrace();
			Assert.fail("Exception not expected but thrown");
		}

		// Exception expected
		try {
			int length = prBiz.testPadoException(null);
			Assert.fail("Exception expected but not thrown");
		} catch (TestException e) {
			System.out.println("Valid exception thrown as expected (Test succesful):");
			System.out.flush();
			e.printStackTrace();
		}
	}

	@Test
	public void testNonPadoException()
	{
		System.out.println("BizContextTest.testNonPadoException()");
		System.out.println("-------------------------------------");
		
		// Exception not expected
		try {
			int length = prBiz.testNonPadoException("hello, world");
		} catch (InvalidObjectException e) {
			e.printStackTrace();
			Assert.fail("Exception not expected but thrown");
		}

		// Exception expected
		try {
			int length = prBiz.testNonPadoException(null);
			Assert.fail("Exception expected but not thrown");
		} catch (InvalidObjectException e) {
			System.out.println("Valid exception thrown as expected (Test succesful):");
			System.out.flush();
			e.printStackTrace();
		}
	}

	@Test
	public void testBizContext()
	{
		System.out.println("BizContextTest.testBizContext()");
		System.out.println("-------------------------------");
		
		IBizContextClient bizContext = prBiz.getBizContext();
		bizContext.reset();
		ISimpleUserContext userContext = (ISimpleUserContext) bizContext.getUserContext();
		IUserInfo userInfo = userContext.getUserInfo();
		userInfo.setAttribute("FUserId", "foo");
		userInfo.setAttribute("FLocation", "CT");
		userInfo.setAttribute("FOrg", "HR");
		userInfo.setAttribute("FIsOnBehalf", true);
		ISimpleDataContext dataContext = (ISimpleDataContext) bizContext.getDataContext();
		IDataInfo dataInfo = dataContext.getDataInfo();
		dataInfo.setAttribute("FBusinessConfidentialType", (byte) 1);
		dataInfo.setAttribute("FMedication", (byte) 1);
		dataInfo.setAttribute("FOrderNumber", 123);
		dataInfo.setAttribute("FRxNumber", 456);
		List<String> serverIdList = prBiz.getServerIdList();
		Assert.assertNotNull(serverIdList);
		System.out.println("testServerIdList()");
		System.out.println("   " + serverIdList);
		System.out.println();
	}
	
	/**
	 * Tests PBM context objects. In order to run this test, the PBM context 
	 * classes must be registered in plugins.properties in the server side.
	 */
	public void testPbmBizContext()
	{
		System.out.println("BizContextTest.testPbmBizContext()");
		System.out.println("----------------------------------");
		
		IBizContextClient bizContext = prBiz.getBizContext();
		bizContext.reset();
		IPbmUserContext userContext = (IPbmUserContext) bizContext.getUserContext();
		IPbmUserInfo userInfo = userContext.getUserInfo();
		userInfo.setAttribute(PbmUserInfoKey.KFUserId, "foo");
		userInfo.setAttribute(PbmUserInfoKey.KFLocation, "CT");
		userInfo.setAttribute(PbmUserInfoKey.KFOrg, "HR");
		userInfo.setAttribute(PbmUserInfoKey.KFIsOnBehalf, true);
		IPbmDataContext dataContext = (IPbmDataContext) bizContext.getDataContext();
		IPbmDataInfo dataInfo = dataContext.getDataInfo();
		dataInfo.setAttribute(PbmDataInfoKey.KFBusinessConfidentialType, (byte) 1);
		dataInfo.setAttribute(PbmDataInfoKey.KFMedication, (byte) 1);
		dataInfo.setAttribute(PbmDataInfoKey.KFOrderNumber, 123);
		dataInfo.setAttribute(PbmDataInfoKey.KFRxNumber, 456);
		List<String> serverIdList = prBiz.getServerIdList();
		Assert.assertNotNull(serverIdList);
		System.out.println("testServerIdList()");
		System.out.println("   " + serverIdList);
		System.out.println();
	}

}
