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
package com.netcrest.pado.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IUtilBiz;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.internal.security.AESCipher;

public class CsvFileExporter
{
	private Properties csvProperties = new Properties();
	private IPado pado;
	private IUtilBiz utilBiz;

	public CsvFileExporter() throws PadoLoginException, IOException
	{
		init();
		login();
		utilBiz = pado.getCatalog().newInstance(IUtilBiz.class);
	}
	
	private void init() throws IOException
	{
		String csvPropertiesPath = System.getProperty("pado.csv.properties");
		File file = new File(csvPropertiesPath);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			csvProperties.load(reader);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
	
	private void login() throws PadoLoginException
	{
		String locators = System.getProperty("pado.locators", "localhost:20000");
		csvProperties.setProperty("locators", locators);

		String appId = csvProperties.getProperty("appid");
		String userName = csvProperties.getProperty("username");
		String passwd = csvProperties.getProperty("password");

		if (passwd != null) {
			if (userName == null) {
				passwd = null;
			} else {
				try {
					passwd = AESCipher.decryptUserTextToText(passwd);
				} catch (Exception ex) {
					throw new PadoLoginException("Invalid password.", ex);
				}
			}
		}
		Pado.connect(locators, false);
		pado = Pado.login(appId, appId, userName, passwd.toCharArray());
	}

	
	private void logout()
	{
		pado.logout();
		Pado.close();
	}
	
	public void exportPath(String gridPath)
	{
		writeLine();
		writeLine("Exporting grid path " + gridPath + "... Please wait.");
		long startTime = System.currentTimeMillis();
		List<String> list = utilBiz.dumpServers(gridPath);
		long elapsedTimeInSec = (System.currentTimeMillis() - startTime) / 1000;
		for (int i = 0; i < list.size(); i++) {
			writeLine("   " + (i+1) + ". " + list.get(i));
		}
		writeLine("Grid path export complete.");
		writeLine("Elapsed time (sec): " + elapsedTimeInSec);
		writeLine();
	}

	public void exportAll() throws Exception
	{
		writeLine();
		writeLine("Exporting all public grid paths to CSV files... This may take some time. Please wait.");
		long startTime = System.currentTimeMillis();
		List<String> serverDirs = utilBiz.dumpAll();
		long elapsedTimeInSec = (System.currentTimeMillis() - startTime) / 1000;
		for (int i = 0; i < serverDirs.size(); i++) {
			writeLine("   " + (i+1) + ". " + serverDirs.get(i));
		}
		writeLine("Grid path export complete.");
		writeLine("Elapsed time (sec): " + elapsedTimeInSec);
		writeLine();
	}
	
	private static void writeLine()
	{
		System.out.println();
	}

	private static void writeLine(String line)
	{
		System.out.println(line);
	}

	private static void write(String str)
	{
		System.out.print(str);
	}
	
	private static void usage()
	{
		writeLine();
		writeLine("Usage:");
		writeLine("   CsvFileExporter [-gridpath <gridPath>] [-all] [-?]");
		writeLine();
		writeLine("   Default: CsvFileExporter -gridpath temporal");
		writeLine();
		writeLine("      -gridpath  grid path to export. <gridPath> must not begin with '/'.");
		writeLine("      -all       exports all grid paths");
		writeLine();
		System.exit(0);
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		boolean isAll = false;
		String gridPath = "temporal";
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-gridpath")) {
				if (i < args.length - 1) {
					gridPath = args[++i];
				}
			} else if (arg.equals("-all")) {
				isAll = true;
			}
		}
		
		CsvFileExporter importer = new CsvFileExporter();
		try {
			if (isAll) {
				importer.exportAll();
			} else {
				importer.exportPath(gridPath);
			}
		} finally {
			importer.logout();
		}
	}

}
