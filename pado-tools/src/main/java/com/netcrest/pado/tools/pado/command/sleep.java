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

public class sleep implements ICommand
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
		PadoShell.println("sleep <seconds> [?]");
		PadoShell.println("   Suspend execution for the specified interval of time in seconds.");
	}

	@Override
	public String getShortDescription()
	{
		return "Suspend execution for the specified interval of time in seconds.";
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
		if (argList.size() == 1 || argList.size() > 2) {
			PadoShell.printlnError(this, "usage: sleep <seconds>");
			return;
		}
		
		try {
			String value = argList.get(1);
			int timeInSec = Integer.parseInt(value);
			Thread.sleep(timeInSec * 1000);
		} catch (NumberFormatException ex) {
			PadoShell.printlnError(this, "Invalid number");
		}
	}
}
