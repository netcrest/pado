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
import com.netcrest.pado.tools.pado.SharedCache;

public class cd implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
	}

	private String previousPath;

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("cd [- | .. | <path>] [-?]");
		PadoShell.println("   Change directory (path).");
		PadoShell.println("      -  Change directory to the previous directory.");
		PadoShell.println("      .. Change directory up one level.");
	}
	
	@Override 
	public String getShortDescription()
	{
		return "Change directory (or path).";
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
		List<String> argList = commandLine.getArgList();
		String path;
		if (argList.size() == 1) {
			path = SharedCache.getSharedCache().getHomePath();
		} else {
			path = argList.get(1);
		}
		if ("-".equals(path)) {
			if (previousPath == null) {
				previousPath = padoShell.getCurrentPath();
				return;
			}
			path = previousPath;
		}
		chdir(path);
	}

	private void chdir(String newPath)
	{
		if (newPath == null) {
			return;
		}

		String currentPath = padoShell.getCurrentPath();
		String fullPath = padoShell.getFullPath(newPath, currentPath);

		if (fullPath == null) {
			PadoShell.printlnError(this, newPath + ": Invalid path");
		} else if (fullPath.equals("/")) {
			padoShell.setCurrentPath(fullPath);
			previousPath = currentPath;
		} else {
			if (isPathDefined(fullPath) == false) {
				PadoShell.printlnError(this, fullPath + ": No such path");
				return;
			} else {
				padoShell.setCurrentPath(fullPath);
			}
			previousPath = currentPath;
		}
	}
	
	private boolean isPathDefined(String fullPath)
	{
		return SharedCache.getSharedCache().isFullPathExist(fullPath);
	}
}
