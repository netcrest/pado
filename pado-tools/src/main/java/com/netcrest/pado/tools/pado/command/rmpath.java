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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.util.GridUtil;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.PadoShellUtil;

public class rmpath implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("f", false, "");
		options.addOption("r", false, "");
		options.addOption("rf", false, "");
		options.addOption("fr", false, "");
	}
	private static String[] excludes = null;

	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("rmpath [-fr] <path> ... | [-?]");
		PadoShell.println("   Remove the specified path(s).");
		PadoShell.println("      -r   Recursively remove all nested paths including itself.");
		PadoShell.println("      -f   Force rmpath. Disable confirmation prompt.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Remove one or more directories (paths).";
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
	
	@Override
	@SuppressWarnings("rawtypes")
	public void run(CommandLine commandLine, String command) throws Exception
	{
		List argList = commandLine.getArgList();
		if (argList.size() < 1) {
			PadoShell.printlnError(this, "Path(s) must be specified.");
			return;
		}
		
		boolean recursive = PadoShellUtil.hasSingleLetterOption(commandLine, 'r', excludes);
		boolean force = PadoShellUtil.hasSingleLetterOption(commandLine, 'f', excludes);
		
		if (force == false && padoShell.isInteractiveMode()) {
			PadoShell.println("This command removes the specified paths.");
			PadoShell.println("Enter 'continue' to continue or any other keys to abort:");
			String line = padoShell.readLine("");
			if (line.equals("continue") == false) {
				PadoShell.println("Command aborted.");
				return;
			}
		}
		
		

		// Check the specified paths and build the full paths
		List<String> fullPathList = new ArrayList<String>(argList.size() - 1);
		for (int i = 1; i < argList.size(); i++) {
			String path = (String) commandLine.getArgList().get(i);
			String fullPath = padoShell.getFullPath(path);
			String gridPath = GridUtil.getChildPath(fullPath);
			if (gridPath.length() == 0) {
				PadoShell.printlnError(this, path + ": Invalid path. Top-level paths not allowed.");
				return;
			}
			fullPathList.add(fullPath);
		}

		// Remove path(s)
		boolean atLeastOneDirRemoved = false;
		IPathBiz pathBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IPathBiz.class);
		for (String fullPath : fullPathList) {
			String gridId = padoShell.getGridId(fullPath);
			String gridPath = padoShell.getGridPath(fullPath);
			if (gridId == null) {
				PadoShell.printlnError(this, fullPath + ": Unable to determine grid ID.");
			} else if (pathBiz.exists(gridId, gridPath) == false) {
				PadoShell.printlnError(this, fullPath + ": Path does not exist.");
			} else {
				pathBiz.remove(gridId, gridPath, recursive);
				atLeastOneDirRemoved = true;
			}
		}
		if (atLeastOneDirRemoved) {
			SharedCache.getSharedCache().refresh();
		}
	}
}
