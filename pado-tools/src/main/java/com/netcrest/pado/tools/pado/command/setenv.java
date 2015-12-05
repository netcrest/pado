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

public class setenv implements ICommand
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
		PadoShell.println("setenv [<variable> [<value>]]");
		PadoShell.println("   Add or change the value of a PadoShell environment variable. If no arguments");
		PadoShell.println("   are specified then lists all environment variables. To unset use the 'unsetenv'");
		PadoShell.println("   command.");
		PadoShell.println("      <variable> The name of variable to set.");
		PadoShell.println("      [<value>]  The value of the variable. If not quoted then white spaces");
		PadoShell.println("                 are not preserved.");
	}

	@Override
	public String getShortDescription()
	{
		return "Add or change the value of a PadoShell environment variable.";
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
		if (argList.size() == 1) {
			padoShell.printEnvs();
			return;
		} else {
			String variable = argList.get(1);
			int index = command.indexOf(' ');
			String value = command.substring(index).trim();
			index = value.indexOf(' ');
			if (index != -1) {
				value = value.substring(index).trim();
			}
			// strip quotes
			if (value.startsWith("\"")) {
				value = value.substring(1);
			}
			if (value.endsWith("\"")) {
				value = value.substring(0, value.length() - 1);
			}
			padoShell.setEnv(variable, value);
		}
	}
}
