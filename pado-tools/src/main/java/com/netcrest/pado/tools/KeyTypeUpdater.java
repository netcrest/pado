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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.mon.ISysBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.data.jsonlite.JsonLiteTokenizer;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;

public class KeyTypeUpdater
{
	private Properties csvProperties = new Properties();
	private IPado pado;

	public KeyTypeUpdater() throws PadoLoginException, IOException
	{
		String locators = System.getProperty("pado.locators", "localhost:20000");
		
		String csvPropertiesPath = System.getProperty("pado.csv.properties");
		
		File file = new File(csvPropertiesPath);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			csvProperties.load(reader);
			reader.close();
		} catch (Exception ex) {
			// ignore
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		
		csvProperties.setProperty("pado.locators", locators);
		
		String appId = null;
		String userName = null;
		String passwd = null;
		
		login(locators, appId, userName, passwd);
	}
	
	@SuppressWarnings("rawtypes")
	public void registerKeyTypeQueryReferences(String dbDir, String keyTypeClassName, boolean isPersist)
	{
		File keyTypeDir = new File(dbDir, "keytype");
		File file = new File(keyTypeDir, keyTypeClassName + ".json");
		JsonLite keyTypeDefinitions = null;
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			JsonLiteTokenizer jt = new JsonLiteTokenizer(reader, null);
			keyTypeDefinitions = new JsonLite(jt, null);
		} catch (FileNotFoundException e) {
			writeLine("File not found: " + file.getAbsolutePath());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
		if (keyTypeDefinitions == null) {
			return;
		}
		
		ISysBiz sysBiz = pado.getCatalog().newInstance(ISysBiz.class);
		try {
			sysBiz.registerKeyTypeQueryReferences(keyTypeDefinitions, isPersist);
		} catch (PadoException e) {
			writeLine(e.getMessage() + " KeyType query reference not registered. [file=" + file.getAbsolutePath() + ", KeyType="
					+ keyTypeClassName + "]");
		} catch (ClassNotFoundException e) {
			writeLine("KeyType class not found. Query reference not registered. [file=" + file.getAbsolutePath()
					+ ", KeyType=" + keyTypeClassName + "]");
		} catch (ClassCastException e) {
			writeLine(e.getMessage() + " Query reference not registered. [file=" + file.getAbsolutePath() + ", KeyType="
					+ keyTypeClassName + "]");
		} catch (IOException e) {
			writeLine(e.getMessage() + " Query reference not registered. [file=" + file.getAbsolutePath() + ", KeyType="
					+ keyTypeClassName + "]");
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void registerAllKeyTypeQueryReferences(String dbDir, boolean isPersist)
	{
		File keyTypeDir = new File(dbDir, "keytype");
		ArrayList<JsonLite> list = new ArrayList<JsonLite>();
		File files[] = keyTypeDir.listFiles(new FilenameFilter(){

			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".json");
			}
			
		});
		if (files == null) {
			return;
		}
		for (File file : files) {
			JsonLite jl = null;
			FileReader reader = null;
			try {
				reader = new FileReader(file);
				JsonLiteTokenizer jt = new JsonLiteTokenizer(reader, null);
				jl = new JsonLite(jt, null);
				list.add(jl);
			} catch (FileNotFoundException e) {
				// ignore
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ex) {
						// ignore
					}
				}
			}
		}
		
		ISysBiz sysBiz = pado.getCatalog().newInstance(ISysBiz.class);
		try {
			sysBiz.registerKeyTypeQueryReferences(list.toArray(new JsonLite[list.size()]), true);
		} catch (Exception e) {
			writeLine(e.getMessage());
		}
	}
	
	public void reset(String dbDir, boolean isPersist)
	{
		ISysBiz sysBiz = pado.getCatalog().newInstance(ISysBiz.class);
		sysBiz.resetKeyTypeQueryRerences();
	}

	private void login(String locators, String appId, String userName, String passwd) throws PadoLoginException
	{
		Pado.connect(locators, false);
		pado = Pado.login(appId, appId, userName, passwd == null ? null : passwd.toCharArray());
	}
	
	private void logout()
	{
		pado.logout();
		Pado.close();
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
		String dbDir = System.getProperty("pado.db.dir", System.getProperty("user.dir", "db"));
		writeLine();
		writeLine("Usage:");
		writeLine("   KeyTypeUpdater [-dbdir <dbDirPath] [-keyType <className> | -all |  -reset] [-?]");
		writeLine();
		writeLine("   Updates the KeyType query reference definitions found in the specified Pado DB directory.");
		writeLine();
		writeLine("      -dbdir   DB directory path. If not specified, defaults to " + dbDir + ".");
		writeLine("      -keyType Updates the grid(s) to the specified KeyType definitions found in this node's DB.");
		writeLine("      -all     Updates the grid(s) to the persistent state found in this node's DB.");
		writeLine("      -reset   Resets the grid(s) to the their respective persistent state.");
		writeLine();
		writeLine("   Default: KeyTypeUpdater -dbdir " + dbDir + "...");
		writeLine();
		System.exit(0);
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		String arg;
		String dbDir = System.getProperty("pado.db.dir", "db");
		String keyTypeClassName = null;
		boolean isReset = false;
		boolean isAll = false;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-dbdir")) {
				if (i < args.length - 1) {
					dbDir = args[++i];
				}
			} else if (arg.equals("-keyType")) {
				if (i < args.length - 1) {
					keyTypeClassName = args[++i];
				}
			} else if (arg.equals("-reset")) {
				isReset = true;
			} else if (arg.equals("-all")) {
				isAll = true;
			}
		}
		if (keyTypeClassName == null && isReset == false && isAll == false) {
			writeLine();
			writeLine("Error: Must specify one of the options.");
			usage();
		}
		
		KeyTypeUpdater updater = new KeyTypeUpdater();
		if (keyTypeClassName != null) {
			updater.registerKeyTypeQueryReferences(dbDir, keyTypeClassName, true);
		} else if (isReset) {
			updater.reset(dbDir, true);
		} else if (isAll) {
			updater.registerAllKeyTypeQueryReferences(dbDir, true);
		}
		updater.logout();
	}

}
