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
import java.util.Date;
import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.temporal.AttachmentResults;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.AttachmentSetFactory;
import com.netcrest.pado.temporal.test.TemporalLoader;
import com.netcrest.pado.temporal.test.data.Account;

/**
 * TemporalSinglePutTest requires "mygrid". It puts a single entry to the
 * position path.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalSinglePutTest
{
	private static IPado pado;
	private static ITemporalBiz<String, KeyMap> temporalBiz;
	private static TemporalLoader loader;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		temporalBiz = catalog.newInstance(ITemporalBiz.class, "account");
		System.setProperty("keyMapType", "jsonlite");
		loader = new TemporalLoader();
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	private static String IDENTITY_KEY = "acctx_a";
	@Test
	public void testPut() throws Exception
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date startValidTime = dateFormat.parse("10/10/2013");
		Date endValidtime = dateFormat.parse("12/31/2099");
		Date writtenTime = startValidTime;
		loader.addBankId("bank");
		// Lucene search of the key
		AttachmentSetFactory<String> factory = new AttachmentSetFactory<String>();
		AttachmentSet<String> as = factory.createAttachmentSet();
		as.setGridPath("account");
		HashSet<String> set = new HashSet();
		// set.add((String)keyMap.get(Account.KAccountId));
		as.setQueryStatement(IDENTITY_KEY);
		as.setAttachments(set);
		KeyMap keyMap = loader.createAccount(IDENTITY_KEY, false);
		temporalBiz.putAttachments((String) keyMap.get(Account.KAccountId), keyMap, new AttachmentSet[] { as },
				startValidTime.getTime(), endValidtime.getTime(), writtenTime.getTime(), false);
	
		Thread.sleep(3000);
		AttachmentResults<KeyMap> ar = temporalBiz.getAttachmentsEntries(IDENTITY_KEY);
		System.out.println(ar.getAttachmentValues());
	}

}
