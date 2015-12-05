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

public class value implements ICommand
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
		PadoShell.println("value <class-name> | [-?]");
		PadoShell.println("   Set the value class to be used for the 'put' command.");
		PadoShell.println("   Use the 'key' command to set the key class name.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Set the value class to be used for the 'put' command.";
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
			PadoShell.println(this, padoShell.getValueClassName());
			PadoShell.println("   Use value <class name> to set the value class");
		} else {
			if (commandLine.getArgList().size() > 1) {
				padoShell.setValueClass((String) commandLine.getArgList().get(1));
			}
		}
	}
}
