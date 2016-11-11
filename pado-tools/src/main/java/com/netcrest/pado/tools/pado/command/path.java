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
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.netcrest.pado.biz.mon.ISysBiz;
import com.netcrest.pado.info.ServerInfo;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.PrintUtil;
import com.netcrest.pado.util.GridUtil;

public class path implements ICommand
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
		PadoShell.println("path <path> ... | [-?]");
		PadoShell.println("   Prints details of the specified path(s).");
	}

	@Override
	public String getShortDescription()
	{
		return "Prints details of the specified path(s).";
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
		if (argList.size() < 1) {
			PadoShell.printlnError(this, "Path(s) must be specified.");
			return;
		}
		
		printPathInfo(commandLine);
	}

	private void printPathInfo(CommandLine commandLine) throws Exception
	{
		// Create path(s)
		List<String> fullPathList = getFullPathList(commandLine);
		if (fullPathList == null) {
			return;
		}
		ISysBiz sysBiz = SharedCache.getSharedCache().getSysBiz();
		for (String fullPath : fullPathList) {
			String gridId = sysBiz.getBizContext().getGridService().getGridId(fullPath);
			if (gridId == null) {
				PadoShell.printlnError(this, fullPath + ": Unable to determine grid ID");
			} else {
				List<ServerInfo> serverInfoList = sysBiz.getServerInfoList(fullPath);
				Collections.sort(serverInfoList);
				PrintUtil.printList(serverInfoList, 0, 1, serverInfoList.size(), serverInfoList.size(), null, false);
			}
		}
	}

	/**
	 * Converts the path arguments to a list of full paths.
	 * @param commandLine Command line
	 * @return null if error. 
	 */
	@SuppressWarnings("unchecked")
	private List<String> getFullPathList(CommandLine commandLine)
	{
		// Check the specified paths and build the full paths
		List<String> argList = commandLine.getArgList();
		String currentPath = padoShell.getCurrentPath();
		List<String> fullPathList = new ArrayList<String>(argList.size() - 1);
		for (int i = 1; i < argList.size(); i++) {
			String path = (String) commandLine.getArgList().get(i);
			String fullPath = padoShell.getFullPath(path, currentPath);
			String gridPath = GridUtil.getChildPath(fullPath);
			if (gridPath.length() == 0) {
				PadoShell.printlnError(this, path + ": Invalid path. Top-level paths not allowed.");
				return null;
			}
			fullPathList.add(fullPath);
		}
		return fullPathList;
	}
}
