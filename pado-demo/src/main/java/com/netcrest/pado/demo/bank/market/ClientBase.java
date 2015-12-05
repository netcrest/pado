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
package com.netcrest.pado.demo.bank.market;

import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;

public class ClientBase
{
	protected String gridId;
	protected IPado pado;
	
	protected void login() throws PadoException, PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		gridId = System.getProperty("pado.gridId", "grid2");
		String appId = System.getProperty("pado.appId", "equity");
		String locators = System.getProperty("pado.locators", "localhost:20000");
		String user = System.getProperty("pado.userId", "test1");
		String password = System.getProperty("pado.password", "test123");
		Pado.connect(locators, true);
		pado = Pado.login(appId, "domain", user, password.toCharArray());
	}
	
	protected synchronized void waitForever()
	{
		while (true) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
