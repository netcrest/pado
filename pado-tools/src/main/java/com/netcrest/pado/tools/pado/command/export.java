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

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.gemstone.gemfire.cache.query.Struct;
import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.internal.util.OutputUtil;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.PadoShellUtil;

public class export implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("k", false, "");
		options.addOption("v", false, "");
		options.addOption("f", false, "");
		options.addOption("kv", false, "");
		options.addOption("vk", false, "");
		options.addOption("kf", false, "");
		options.addOption("vf", false, "");
		options.addOption("fk", false, "");
		options.addOption("fv", false, "");
		options.addOption("kvf", false, "");
		options.addOption("vfk", false, "");
		options.addOption("fkv", false, "");
		options.addOption("kfv", false, "");
		options.addOption("fvk", false, "");
		options.addOption("refresh", false, "");
		
//		PadoShellUtil.addSingleLetterOptions(options, "abcde");
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("export [-kvf] [-refresh] <from-path> <to-file> | [-?]");
		PadoShell.println("   Export the specified path to the specified file path in the form of");
		PadoShell.println("   CSV file. The first row of the exported CSV file lists column names.");
		PadoShell.println("   Exported files can be imported back using the 'import' command.");
		PadoShell.println("   If neither -k nor -v is specified then it assumes -kv and exports");
		PadoShell.println("   both keys and values. It also generates the paired schema file if both");
		PadoShell.println("   keys and values are exported. To import, the schema file is required in");
		PadoShell.println("   the same directory as the CSV file.");
		PadoShell.println("   IMPORTANT: In order to import back, you must include both keys and values");
		PadoShell.println("              in the file by executing 'export -kv ...'");
		PadoShell.println("      -f  Force write. Overwrites the existing file. If not specified, then");
		PadoShell.println("          it prompts for confirmation.");
		PadoShell.println("      -k  Export keys only.");
		PadoShell.println("      -v  Export values only.");
		PadoShell.println("      -kv Export both keys and values. Same as not specifying -k and -v");
		PadoShell.println("      -refresh Refresh the contents by forcing the grid to rebuild the index matrix.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Export path contents to a CSV file in the local file system.";
	}
	
	@Override
	public boolean isLoginRequired()
	{
		return true;
	}

	@Override
	public Options getOptions()
	{
		return options;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void run(CommandLine commandLine, String command) throws Exception
	{
		List<String> argList = commandLine.getArgList();
		if (argList.size() < 3) {
			PadoShell.printlnError(this, "Invalid number of arguments.");
			return;
		}
		
		String fromPath = argList.get(1);
		String toFilePath = argList.get(2);
		
		boolean isRefresh = commandLine.hasOption("refresh");
		boolean isForce = PadoShellUtil.hasSingleLetterOption(commandLine, 'f', "refresh");
		boolean includeKeys = PadoShellUtil.hasSingleLetterOption(commandLine, 'k', "refresh");
		boolean includeValues = PadoShellUtil.hasSingleLetterOption(commandLine, 'v', "refresh");
		boolean isSchema = includeKeys && includeValues || (includeKeys == false && includeValues == false);
		
		IPathBiz pathBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IPathBiz.class);
		String gridId = padoShell.getGridId(fromPath);
		String gridPath = padoShell.getGridPath(fromPath);
		if (pathBiz.exists(gridId, gridPath) == false) {
			PadoShell.printlnError(this, fromPath + ": Path does not exist.");
			return;
		}
		
		less l = (less)padoShell.getCommand("less");
		IScrollableResultSet rs = l.queryPath(fromPath, isRefresh, includeKeys, includeValues);
		if (rs == null || rs.toList() == null || rs.toList().size() == 0) {
			PadoShell.printlnError(this, fromPath + ": Path empty. File not created.");
			return;
		}

		File csvFile;
		if (toFilePath.endsWith(".csv") == false) {
			csvFile = new File(toFilePath + ".csv");
		} else {
			csvFile = new File(toFilePath);
		}
		String fn = csvFile.getName().substring(0, csvFile.getName().lastIndexOf(".csv"));
		File schemaFile = new File(fn + ".schema");
		
		if (isForce == false && padoShell.isInteractiveMode() && csvFile.exists()) {
			PadoShell.println(this, toFilePath + ": File exists. Do you want to overwrite?");
			PadoShell.println("Enter 'continue' to continue or any other keys to abort:");
			String line = padoShell.readLine("");
			if (line.equals("continue") == false) {
				PadoShell.println("Command aborted.");
				return;
			}
		}
		
		if (isSchema) {
			List list = rs.toList();
			Struct struct = (Struct) list.get(0);
			Object key = struct.getFieldValues()[0];
			Object value = struct.getFieldValues()[1];
			PrintWriter schemaWriter = new PrintWriter(schemaFile);
			List keyList = null;
			if (value instanceof Map) {
				// Must iterate the entire map to get all unique keys
				Map valueMap = (Map) value;
				Set keySet = valueMap.keySet();
				HashSet set = new HashSet(keySet.size(), 1f);
				set.addAll(keySet);
				keyList = new ArrayList(set);
				Collections.sort(keyList);
			}
			OutputUtil.printSchema(schemaWriter, gridPath, key, value, keyList, OutputUtil.TYPE_KEYS_VALUES, ",", PadoShellUtil.getIso8601DateFormat(), true, true);
			schemaWriter.close();
		}
		
		PrintWriter csvWriter = new PrintWriter(csvFile);
		int printType;
		if (includeKeys && includeValues == false) {
			printType = OutputUtil.TYPE_KEYS;
		} else if (includeKeys == false && includeValues) {
			printType = OutputUtil.TYPE_VALUES;
		} else {
			printType = OutputUtil.TYPE_KEYS_VALUES;
		}
		try {
			OutputUtil.printScrollableResultSet(csvWriter, rs, ",", printType, PadoShellUtil.getIso8601DateFormat());
		} finally {
			if (csvWriter != null) {
				csvWriter.close();
			}
		}
	}
}
