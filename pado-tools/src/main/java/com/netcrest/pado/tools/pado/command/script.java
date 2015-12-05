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

import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;

public class script implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("i", false, "");
	}

	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	public void help()
	{
		PadoShell.println("script [-i] <script-file> | [-?]");
		PadoShell.println("   Run the specified PadoShell script file.");
		PadoShell.println("      -i  Interactive mode. If specified, it runs the commands in the script");
		PadoShell.println("          interactively. Default is non-interactive.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Execute specified script.";
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

	@SuppressWarnings("unchecked")
	@Override
	public void run(CommandLine commandLine, String command) throws Exception
	{
		List<String> argList = commandLine.getArgList();
		if (argList.size() < 2) {
			PadoShell.printlnError(this, "Invalid number of arguments. <script_file> required.");
			return;
		}
		
		String scriptFilePath = argList.get(1);
		File scriptFile = new File(scriptFilePath);
		if (scriptFile.exists() == false) {
			PadoShell.printlnError(this, scriptFilePath + ": File does not exist.");
			return;
		}
		boolean isInteractive = commandLine.hasOption('i');
		boolean isInteractiveBefore = padoShell.isInteractiveMode();
		padoShell.setInteractiveMode(isInteractive);
		if (isInteractive == false) {
			boolean showTimeBefore = padoShell.isShowTime();
			padoShell.setShowTime(false);
			padoShell.runScript(scriptFile);
			padoShell.setShowTime(showTimeBefore);
		} else {
			padoShell.runScript(scriptFile);
		}
		padoShell.setInteractiveMode(isInteractiveBefore);
	}
}
