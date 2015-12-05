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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.netcrest.pado.tools.pado.BufferInfo;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.PrintUtil;

public class buffer implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("delete", true, "");
	}

	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
		padoShell.addBufferCommand(this.getClass().getSimpleName());
	}

	@Override
	public void help()
	{
		PadoShell.println("buffer [-delete <name>] [-?]");
		PadoShell.println("   Display buffer contents or delete buffer. If options are not specified");
		PadoShell.println("   then it lists all of the defined buffers.");
		PadoShell.println("   Commands that use buffered results are:");
		PadoShell.println("   " + padoShell.getBufferCommandSet());
		PadoShell.println("      -delete  Delete the specified buffer.");
	}
	
	@Override 
	public String getShortDescription()
	{
		return "Display buffer contents or delete buffer. Buffers hold and stream named results of 'less'.";
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void run(CommandLine commandLine, String command) throws Exception
	{
		String deleteBufferName = commandLine.getOptionValue("delete");
		List<String> argList = commandLine.getArgList();
		String bufferName = null;
		if (argList.size() > 2) {
			PadoShell.printlnError(this, "Invalid command. Only one buffer name allowed.");
			return;
		} else if (argList.size() == 2) {
			bufferName = argList.get(1);
		}

		boolean isList = argList.size() == 1 && bufferName == null;

		if (deleteBufferName != null) {
			SharedCache.getSharedCache().deleteBufferInfo(deleteBufferName);
		} else if (isList) {
			if (bufferName == null) {
				Map<String, BufferInfo> map = SharedCache.getSharedCache().getBufferInfoMap();
				List list = new ArrayList(map.size());
				for (BufferInfo bi : map.values()) {
					list.add(createBufferInfoMap(bi));
				}
				PrintUtil.printList(list);
			} else {
				BufferInfo bufferInfo = SharedCache.getSharedCache().getBufferInfo(bufferName);
				if (bufferInfo == null) {
					PadoShell.printlnError(this, bufferName + ": Buffer undefined.");
				} else {
					List list = new ArrayList(1);
					list.add(createBufferInfoMap(bufferInfo));
					PrintUtil.printList(list);
				}
			}
		} else if (bufferName != null) {
			padoShell.runCommand("less -buffer " + bufferName, false);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map createBufferInfoMap(BufferInfo bufferInfo)
	{
		Map map = new HashMap(5, 1f);
		map.put("Name", bufferInfo.getName());
		map.put("RemovedRowNumbers", bufferInfo.getRemovedRowNumberSet());
		map.put("Command", bufferInfo.getCommandString());
		map.put("GridId", bufferInfo.getGridId());
		map.put("GridPath", bufferInfo.getGridPath());
		return map;
	}
}
