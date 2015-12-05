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

public class echo implements ICommand
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
		PadoShell.println("echo [true|false] [<message>] [-?]");
		PadoShell.println("   Specify true to enable, false to disable echo. If <message> is specified");
		PadoShell.println("   then it is printed. If <message> is white spaces or not specified then");
		PadoShell.println("   a blank line is printed.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Enable or disable echo, or print message.";
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
		if (commandLine.getArgList().size() >= 2) {
			if (((String) commandLine.getArgList().get(1))
					.equalsIgnoreCase("true")) {
				padoShell.setEcho(true);
			} else if (((String) commandLine.getArgList().get(1))
					.equalsIgnoreCase("false")) {
				padoShell.setEcho(false);
			} else {
				// message
				// command is already trimmed. no need to trim
				int index = command.indexOf(' ');
				String message = command.substring(index + 1);
				PadoShell.println(message);
				return;
			}

		} else {
			PadoShell.println();
		}
	}
}
