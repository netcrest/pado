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
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.netcrest.pado.biz.file.CsvFileLoader;
import com.netcrest.pado.biz.file.SchemaInfo;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;

public class import_ implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("import <csv-file> [-?]");
		PadoShell.println("   Imports the specified CSV file into the grid. The paired schema file");
		PadoShell.println("   must co-exist with the specified CSV file in the same directory.");
		PadoShell.println("   See the 'export' command for exporting schema and CSV files.");
		PadoShell.println("   IMPORTANT: The importing file must contain both keys and values, i.e.,");
		PadoShell.println("              it must have been created by 'import -kv ...'");
	}

	@Override 
	public String getShortDescription()
	{
		return "Import CSV file contents into grid.";
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

	@SuppressWarnings({ "unchecked" })
	@Override
	public void run(CommandLine commandLine, String command) throws Exception
	{
		List<String> argList = commandLine.getArgList();
		if (argList.size() < 2) {
			PadoShell.printlnError(this, "Invalid number of arguments.");
			return;
		}
		
		String csvFilePath = argList.get(1);
		File csvFile = new File(csvFilePath);
		if (csvFile.exists() == false) {
			PadoShell.println(this, csvFilePath + ": File does not exist.");
			return;
		}
		
		String schemaFilePath;
		int index = csvFilePath.lastIndexOf(".csv");
		if (index == -1) {
			schemaFilePath = csvFilePath + ".schema";
		} else {
			schemaFilePath = csvFilePath.substring(0, index) + ".schema";
		}
		File schemaFile = new File(schemaFilePath);
		if (schemaFile.exists() == false) {
			PadoShell.println(this, schemaFilePath + ": File does not exist.");
			return;
		}
		
		// Import file
		CsvFileLoader loader = new CsvFileLoader(SharedCache.getSharedCache().getPado());
		SchemaInfo schemaInfo = new SchemaInfo("file", schemaFile);
		loader.load(schemaInfo, csvFile);
		SharedCache.getSharedCache().refresh();
	}

}
