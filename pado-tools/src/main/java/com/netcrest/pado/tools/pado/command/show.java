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
package com.netcrest.pado.tools.pado.command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.netcrest.pado.internal.util.ReflectionUtil;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;

public class show implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");

		Option opt = OptionBuilder.create("col");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
		
		options.addOption("config", false, "");
		
		opt = OptionBuilder.create("fetch");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
		
//		opt = OptionBuilder.create("history");
//		opt.setArgs(1);
//		opt.setOptionalArg(true);
//		options.addOption(opt);
		
		options.addOption("key", false, "");
		
		opt = OptionBuilder.create("limit");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

//		opt = OptionBuilder.create("print");
//		opt.setArgs(1);
//		opt.setOptionalArg(true);
//		options.addOption(opt);

		opt = OptionBuilder.create("table");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
		
		opt = OptionBuilder.create("time");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
		
		opt = OptionBuilder.create("type");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
		
		options.addOption("value", false, "");
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("show [-col <collection-entry-print-count>] [-?]");
		PadoShell.println("     [-config|-key|-value]");
		PadoShell.println("     [-fetch [<result-set-fetch-size>]]");
		PadoShell.println("     [-history [<history size>]]");
		PadoShell.println("     [-limit [<select-and-less-result-set-limit-size>]]");
//		PadoShell.println("     [-print [true|false]]");
		PadoShell.println("     [-table [true|false]");
		PadoShell.println("     [-time [true|false]]");
		PadoShell.println("     [-type [true|false]");
		PadoShell.println("   Show or set or toggle (true/false) command specifics.");
		PadoShell.println("      <no option> Show all current settings.");

		PadoShell.println("      -col <collection-entry-print-count> In the catalog mode, PadoShell");
		PadoShell.println("               prints the contents of Map and Collection objects. By default, it");
		PadoShell.println("               prints 5 entries per object. Use this option to change the count.");
		PadoShell.println("      -config  Show configuration");
		PadoShell.println("      -fetch   Query result set fetch size for commands such as 'less', 'select', etc.");
		PadoShell.println("      -history History size.");
		PadoShell.println("      -key     Show key class fields. Use the 'key' command to set key class.");
		PadoShell.println("      -limit   'select' and 'less' command limit size. -1 to set no limit.");
		PadoShell.println("               CAUTION: Note that setting no limit -1 may lead to grid performance degradation.");
//		PadoShell.println("      -print   Toggle print. If enabled, results are printed to stdout.");
		PadoShell.println("      -table   Set the print format to the tabular or catalog form. The");
		PadoShell.println("               tabular form prints in a table with a column header. The catalog");
		PadoShell.println("               form prints in each row in a data structure form.");
		PadoShell.println("      -time    Toggle the time taken to execute each command.");
		PadoShell.println("      -type    Enable or disable printing the data type. This option is");
		PadoShell.println("               valid only for the '-table false' option.");
		PadoShell.println("      -value   Show value class fields. Use the 'value' command to set value class.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Show or set or toggle (true/false) command specifics.";
	}
	
	@Override
	public boolean isLoginRequired()
	{
		return false;
	}

	@Override
	public Options getOptions()
	{
		return options;
	}

	@Override
	public void run(CommandLine commandLine, String command) throws Exception
	{
		if (commandLine.hasOption("col")) {
			show_col(commandLine);
		} else if (commandLine.hasOption("config")) {
			show_config();
		} else if (commandLine.hasOption("fetch")) {
			show_fetch(commandLine);
		} else if (commandLine.hasOption("history")) {
			show_history(commandLine);
		} else if (commandLine.hasOption("key")) {
			show_key();
		} else if (commandLine.hasOption("limit")) {
			show_limit(commandLine);
		} else if (commandLine.hasOption("print")) {
			show_print(commandLine);
		} else if (commandLine.hasOption("table")) {
			show_table(commandLine);
		} else if (commandLine.hasOption("time")) {
			show_time(commandLine);
		} else if (commandLine.hasOption("type")) {
			show_type(commandLine);
		} else if (commandLine.hasOption("value")) {
			show_value();
		} else {
			show();
		}
	}
	
	private void show()
	{
		show_config();
		show_key();
		show_value();
	}

	private void show_col(CommandLine commandLine) throws Exception
	{
		if (commandLine.getOptionValue("col") == null) {
			PadoShell.printlnError(
					this,
					"Must specify <collection entry print count>. Current count is "
							+ padoShell.getCollectionEntryPrintCount());
			return;
		}
		try {
			int count = Integer.parseInt(commandLine.getOptionValue("col"));
			padoShell.setCollectionEntryPrintCount(count);
		} catch (Exception ex) {
			PadoShell.printlnError(this, ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}
	
	public void show_config()
	{
		PadoShell.println("       echo: " + padoShell.isEcho());
		if (SharedCache.getSharedCache().getLocators() == null) {
			PadoShell.println("   locators: null");
		} else {
			PadoShell.println("   locators: " + SharedCache.getSharedCache().getLocators());
		}
		PadoShell.println("        col: " + padoShell.getCollectionEntryPrintCount());
		PadoShell.println("      fetch: " + padoShell.getFetchSize());
		PadoShell.println("    history: " + padoShell.getHistoryFileName());
		PadoShell.println("        key: " + padoShell.getKeyClassName());
		PadoShell.println("      limit: " + padoShell.getSelectLimit());
		PadoShell.println("      print: " + padoShell.isShowResults());
		PadoShell.println("      table: " + padoShell.isTableFormat());
		PadoShell.println("       time: " + padoShell.isShowTime());
		PadoShell.println("       type: " + padoShell.isPrintType());
		PadoShell.println("      value: " + padoShell.getValueClassName());
//		PadoShell.println("  zone (hours): " + padoShell.getZoneDifference() / (60 * 60 * 1000));
	}

	public void show_fetch(CommandLine commandLine)
	{
		try {
			if (commandLine.hasOption("fetch") && commandLine.getOptionValue("fetch") != null) {
				int fetchSize = Integer.parseInt(commandLine.getOptionValue("fetch"));
				if (fetchSize < 0) {
					PadoShell.printlnError(this, fetchSize + ": Invalid fetch size.");
				} else {
					padoShell.setFetchSize(fetchSize);
				}
			}
		} catch (NumberFormatException nfe) {
			PadoShell.println(this, commandLine.getOptionValue("fetch") + ": Invalid number.");
		}
	}
	
	public void show_history(CommandLine commandLine)
	{
		try {
			if (commandLine.hasOption("history") && commandLine.getOptionValue("history") != null) {
				int histSize = Integer.parseInt(commandLine.getOptionValue("history"));
				if (histSize <= PadoShell.DEFAULT_MAX_HIST_SIZE) {
					padoShell.setHistoryMaxSize(histSize);
				} else {
					PadoShell.printlnError(this, "Invalid history size. Cannot be greater than "
							+ PadoShell.DEFAULT_MAX_HIST_SIZE + ".");
				}
			}
		} catch (NumberFormatException nfe) {
			PadoShell.println(this, commandLine.getOptionValue("history") + ": Invalid number.");
		}
	}
	
	public void show_key()
	{
		printClassSetters(padoShell.getKeyClass(), "  key");
	}

	public void show_print(CommandLine commandLine)
	{
		if (commandLine.hasOption("print") && commandLine.getOptionValue("print") != null) {
			boolean enable = commandLine.getOptionValue("print").equalsIgnoreCase("true");
			padoShell.setShowResults(enable);
		} else {
			padoShell.setShowResults(!padoShell.isShowResults());
		}
	}
	
	public void show_limit(CommandLine commandLine)
	{
		try {
			if (commandLine.hasOption("limit") && commandLine.getOptionValue("limit") != null) {
				int limit = Integer.parseInt(commandLine.getOptionValue("limit"));
				if (limit < 0) {
					PadoShell.printlnError(this, limit + ": Invalid limit size.");
				} else {
					padoShell.setSelectLimit(limit);
				}
			}
		} catch (NumberFormatException nfe) {
			PadoShell.println(this, commandLine.getOptionValue("select") + ": Invalid number.");
		}
	}
	
	private void show_table(CommandLine commandLine) throws Exception
	{
		if (commandLine.hasOption("table") && commandLine.getOptionValue("table") != null) {
			boolean enable = commandLine.getOptionValue("table").equalsIgnoreCase("true");
			padoShell.setTableFormat(enable);
		} else {
			padoShell.setTableFormat(!padoShell.isTableFormat());
		}
	}
	
	private void show_time(CommandLine commandLine) throws Exception
	{

		if (commandLine.hasOption("time") && commandLine.getOptionValue("time") != null) {
			boolean enable = commandLine.getOptionValue("time").equalsIgnoreCase("true");
			padoShell.setShowTime(enable);
		} else {
			padoShell.setShowTime(!padoShell.isShowTime());
		}
	}

	private void show_type(CommandLine commandLine) throws Exception
	{
		if (commandLine.hasOption("type") && commandLine.getOptionValue("type") != null) {
			boolean enable = commandLine.getOptionValue("type").equalsIgnoreCase("true");
			padoShell.setPrintType(enable);
		} else {
			padoShell.setPrintType(!padoShell.isPrintType());
		}
	}
	
	private void show_value()
	{
		printClassSetters(padoShell.getValueClass(), "value");
	}

	@SuppressWarnings("unchecked")
	private void printClassSetters(Class<?> cls, String header)
	{
		if (cls == null) {
			PadoShell.println(header + " class: undefined");
		} else {
			PadoShell.println(header + " class " + cls.getName());
			PadoShell.println("{");
			try {
				Map<String, Method> setterMap = ReflectionUtil.getAllSettersMap(cls);
				ArrayList<String> list = new ArrayList<String>(setterMap.keySet());
				Collections.sort(list);
				for (Object object : list) {
					Method method = setterMap.get(object);
					if (isSupportedMethod(method)) {
						PadoShell.println("    " + method.getName().substring(3) + "::"
								+ method.getParameterTypes()[0].getCanonicalName());
						// PadoShell.println("    " +
						// method.getParameterTypes()[0].getCanonicalName() +
						// " " + method.getName().substring(3));
					}
				}
			} catch (Exception e) {
				PadoShell.println(e.getMessage());
			}
			PadoShell.println("}");
		}

	}

	private boolean isSupportedMethod(Method method)
	{
		return true;
	}

}
