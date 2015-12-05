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
package com.netcrest.pado.test.junit.file;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.file.CsvFileLoader;
import com.netcrest.pado.biz.file.SchemaInfo;
import com.netcrest.pado.exception.PadoLoginException;

public class CsvFileLoaderTest
{
	private static IPado pado;

	@BeforeClass
	public static void loginPado() throws PadoLoginException, Exception
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
	@Test
	public void testAccountSchema()
	{
		File schemaFile = new File("data/schema/mygrid-account.schema");
		File dataFile = new File("data/processed/mygrid-account.csv");
		SchemaInfo schemaInfo = new SchemaInfo("csv", schemaFile);
		System.out.println(schemaInfo);
		CsvFileLoader fileLoader = new CsvFileLoader(pado);
		fileLoader.load(schemaInfo, dataFile);
	}
}
