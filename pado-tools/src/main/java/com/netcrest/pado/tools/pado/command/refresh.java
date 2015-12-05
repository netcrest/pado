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

import com.netcrest.pado.exception.PadoServerException;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;

public class refresh implements ICommand {
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
	}

	public void initialize(PadoShell padoShell) {
		this.padoShell = padoShell;
	}

	@Override
	public void help() {
		PadoShell.println("refresh [-?]");
		PadoShell.println("   Refresh the login session by retrieving the latest system information from the grids.");
		PadoShell.println("   IMPORTANT: PadoShell caches data and therefore it can be stale. If the most current");
		PadoShell.println("              data is desired, then first execute this command.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Refresh the login session by retrieving the latest system information from the grids.";
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
	public void run(CommandLine commandLine, String command) throws Exception {
		if (SharedCache.getSharedCache().isLoggedIn()) {
			try {
				SharedCache.getSharedCache().refresh();
			} catch (PadoServerException ex) {
				// relogin
				SharedCache.getSharedCache().relogin();
			}
		} else {
			SharedCache.getSharedCache().login();
		}
	}
}
