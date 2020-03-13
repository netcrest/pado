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
package com.netcrest.pado.tools.hazelcast;

import java.io.IOException;

import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.tools.db.DbManager;
import com.netcrest.pado.tools.db.DbManagerException;
import com.netcrest.pado.tools.db.InvalidAttributeException;

public class HazelcastImportScheduler
{
	public HazelcastImportScheduler()
	{
		initLog();
		logDisclaimer();
	}
	
	private void initLog()
	{
		System.setProperty("pado." + Constants.PROP_CLASS_LOGGER, "com.netcrest.pado.hazelcast.util.HazelcastLogger");
	}
	
	void logDisclaimer()
	{
		StringBuffer b = new StringBuffer(1000);
		b.append("\n\n");
		b.append("==========================================================================\n");
		b.append("           Netcrest PADO: Extreme Scalability of Data Grid\n");
		b.append(" Copyright (c) 2013-2015 Netcrest Technologies, LLC, All rights reserved.\n");
		b.append("\n");
		b.append("       This product is protected by  U.S. and international\n");
		b.append("       copyright  and intellectual property laws. All other\n");
		b.append("       products mentioned herein may be trademarks of their\n");
		b.append("       respective companies.\n");
		b.append("==========================================================================\n");
		b.append("\n");
		Logger.log(b.toString());
		Logger.log("ImportScheduler started. All import results are logged in this file.");
	}
	
	private void importData(boolean isSched, boolean isNow, boolean isImport) throws InvalidAttributeException, DbManagerException, IOException, InterruptedException
	{	
		DbManager.ImportType importType;
		System.out.println("isSched="+ isSched);
		System.out.println("isNow="+ isNow);
		System.out.println("isImport="+ isImport);
		
		if (isSched == false && isNow == false) {
			if (isImport) {
				// Import files only
				importType = DbManager.ImportType.IMPORT_THEN_TERMINATE;
			} else {
				// One of the options must be true
				return;
			}
		} else {
			if (isSched) {
				if (isNow) {
					importType = DbManager.ImportType.IMPORT_NOW_THEN_ON_SCHEDULE;
				} else {
					importType = DbManager.ImportType.IMPORT_ON_SCHEDULE;
				}
			} else {
				if (isNow) {
					importType = DbManager.ImportType.IMPORT_NOW_THEN_TERMINATE;
				} else {
					return;
				}
			}
		}
		
		DbManager dbManager = DbManager.initialize(importType);
		dbManager.importData(importType, isImport, false);
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
		writeLine("   ImportScheduler [-sched] [-now] [-import] [-?]");
		writeLine();
		writeLine("   ImportScheduler imports database tables into the grid paths defined in the");
        writeLine("   config files found in the data/scheduler/etc/ directory.");
		writeLine();
		writeLine("      -sched  Runs the scheduler which periodically imports data into the grid.");
		writeLine("      -now    Downloads data immediately. If -sched is not specified");
		writeLine("              then it teminates upon completion.");
		writeLine("      -import Imports downloaded data into the grid.");
		writeLine();
		writeLine("   Default: ImportScheduler");
		writeLine();
		writeLine("   Note that if no options are specified, it exits immediately. It does not execute the");
		writeLine("   scheduler and has no effect.");
		writeLine();
		writeLine("   Examples:");
		writeLine("      Download immeidately, import data into the grid, and terminate:");
		writeLine("         ImporScheduler -now -import");
		writeLine("      Download immeidately, import data into the grid, and start the schduler:");
		writeLine("         ImporScheduler -now -import -sched");
		writeLine("      Download immeidately and then terminate without importing data into the grid:");
		writeLine("         ImporScheduler -now");
		writeLine("      Import previously downloaded data into the grid and then terminate:");
		writeLine("         ImporScheduler -import");
		writeLine();
		System.exit(0);
	}

	public static void main(String[] args)
	{
		boolean isSched = false;
		boolean isNow = false;
		boolean isImport = false;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-sched")) {
				isSched = true;
			} else if (arg.equals("-now")) {
				isNow = true;
			} else if (arg.equals("-import")) {
				isImport = true;
			}
		}
		
		if (isSched == false && isNow == false && isImport == false) {
			writeLine("Must specify one of -sched, -now, import. Exited without executing");
			writeLine("the scheduler. Use the -? option to see usage.");
			System.exit(-1);
		}

		HazelcastImportScheduler client = new HazelcastImportScheduler();
		try {
			client.importData(isSched, isNow, isImport);
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}
}
