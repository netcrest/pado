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

import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.util.GridUtil;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;

public class clear implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("f", false, "");
		options.addOption("v", false, "");
		options.addOption("fv", false, "");
		options.addOption("vf", false, "");
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("clear [-fv] <path> ... | [-?]");
		PadoShell.println("   Clear the specified path(s) by removing all of the data entries.");
		PadoShell.println("   CAUTION: This is an irrecoverable command that removes all entries in the specified");
		PadoShell.println("   paths. It will prompt for confirmation before proceeding the command execution unless");
		PadoShell.println("   the -f option is specified.");
		PadoShell.println("      -f  Force clear. Disable confirmation prompt.");
		PadoShell.println("      -v  Verbose cleared path(s). List path(s) as they are cleared.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Clear contents of one or more paths.";
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(CommandLine commandLine, String command) throws Exception
	{
		List<String> argList = (List<String>)commandLine.getArgList();
		if (argList.size() == 1) {
			PadoShell.printlnError(this, "Path(s) not specified.");
			return;
		}
		
		boolean forceClear = command.matches(".*\\-.*f.*");
		boolean verbose = command.matches(".*\\-.*v.*");
		
		if (forceClear == false && padoShell.isInteractiveMode()) {
			PadoShell.println("This command clears all of the contents in the current or specified paths.");
			PadoShell.println("Enter 'continue' to continue or any other keys to abort:");
			String line = padoShell.readLine("");
			if (line.equals("continue") == false) {
				PadoShell.println("Command aborted.");
				return;
			}
		}

		IPathBiz pathBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IPathBiz.class);
		boolean clearedAtLeastOnePath = false;
		for (int i = 1; i < argList.size(); i++) {
			String path = argList.get(i);
			String fullPath = padoShell.getFullPath(path);
			if (padoShell.isRootPath(fullPath)) {
				PadoShell.printlnError(this, fullPath + ": Root path cannot be cleared");
			} else {
				String gridId = SharedCache.getSharedCache().getGridId(fullPath);
				String gridPath = GridUtil.getChildPath(fullPath);
				if (gridId == null) {
					PadoShell.printlnError(this, fullPath + ": Unable to determine grid ID.");
				} else if (pathBiz.exists(gridId, gridPath) == false) {
					PadoShell.printlnError(this, fullPath + ": Path does not exist.");
				} else {
					pathBiz.clear(gridId, gridPath, true);
					clearedAtLeastOnePath = true;
					if (verbose) {
						PadoShell.println(this, fullPath + ": cleared");
					}
				}
			}
		}
		
		if (clearedAtLeastOnePath) {
			SharedCache.getSharedCache().refresh();
		}
	}
}
