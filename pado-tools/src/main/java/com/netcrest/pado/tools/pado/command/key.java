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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;

public class key implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
	}

	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	public void help()
	{
		PadoShell.println("key [<class-name>] [-?]");
		PadoShell.println("   Set the key class to be used for the 'get', 'put' and 'which' ");
		PadoShell.println("   commands. Use the 'value' command to set the value class name.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Set the key class to be used for the 'get', 'put' and 'which' commands.";
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
		if (commandLine.getArgList().size() < 2) {
			PadoShell.println(this, padoShell.getKeyClassName());
		} else {
			if (commandLine.getArgList().size() > 1) {
				padoShell.setKeyClass((String) commandLine.getArgList().get(1));
			}
		}
	}
}
