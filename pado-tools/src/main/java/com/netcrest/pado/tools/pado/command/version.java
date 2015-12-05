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

import com.netcrest.pado.PadoVersion;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;

public class version implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("l", false, "");
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("version [?]");
		PadoShell.println("   Display Pado version.");
		PadoShell.println("      -l  Display long (full) detail.");
	}

	@Override
	public String getShortDescription()
	{
		return "Display Pado version.";
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
		boolean isLong = commandLine.hasOption('l');
		PadoVersion padoVersion = new PadoVersion();
		if (isLong) {
			PadoShell.println(" Version: " + padoVersion.getVersion());
			PadoShell.println("     Tag: " + padoVersion.getRepositoryTag());
			PadoShell.println("    Date: " + padoVersion.buildDate);
			PadoShell.println("Built By: " + padoVersion.getBuilderName());
		} else {
			PadoShell.println(padoVersion.getVersion());
		}
	}
}
