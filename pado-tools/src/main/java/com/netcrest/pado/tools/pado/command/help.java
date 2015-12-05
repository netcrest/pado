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

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;

public class help implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("f", false, "");
	}
	
	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("help [-f] [<command> ...] [-?]");
		PadoShell.println("   List command descriptions. If command name is not specified then all commands are displayed.");
		PadoShell.println("      -f Display full command descriptions.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Display descriptions of all commands or detailed description of specified command.";
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
		boolean isFull = commandLine.hasOption('f');
		List<String> argList = (List<String>)commandLine.getArgList();
		if (argList.size() == 1) {
			padoShell.showHelp(isFull);
		} else {
			for (int i = 1; i < argList.size(); i++) {
				String commandName = argList.get(i);
				padoShell.runCommand(commandName + " -?", false);
			}
		}
	}
}
