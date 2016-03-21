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
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.netcrest.pado.biz.IUtilBiz;
import com.netcrest.pado.info.WhichInfo;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.tools.pado.BufferInfo;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.ObjectUtil;
import com.netcrest.pado.tools.pado.util.PrintUtil;
import com.netcrest.pado.tools.pado.util.TimerUtil;
import com.netcrest.pado.util.GridUtil;

public class which implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();

	static {
		options.addOption("?", false, "");

		Option opt = OptionBuilder.create("path");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("buffer");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		options.addOption("r", false, "");
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
		padoShell.addBufferCommand(this.getClass().getSimpleName());
	}

	@Override
	public void help()
	{
		PadoShell.println("which [-path <path>] <key> | [-?]");
		PadoShell.println("which -buffer <name> <number-list>");
		PadoShell.println("which -r <routing key>");
		PadoShell.println("   Show the server(s) that have the specified key or targetted by the specified routing key.");
		PadoShell.println("   Commands that use buffered results are:");
		PadoShell.println("   " + padoShell.getBufferCommandSet());
		PadoShell.println("      <key fields>: field=val1 and field2='val1' \\");
		PadoShell.println("                      and field3=to_date('<date>', '<format>')");
		PadoShell.println("      Data formats: primitives, String, and java.util.Date");
		PadoShell.println("         <decimal> b|B - Byte      (e.g., 1b)");
		PadoShell.println("         <decimal> c|C - Character (e.g., 1c)");
		PadoShell.println("         <decimal> s|S - Short     (e.g., 12s)");
		PadoShell.println("         <decimal> i|I - Integer   (e.g., 15 or 15i)");
		PadoShell.println("         <decimal> l|L - Long      (e.g., 20l");
		PadoShell.println("         <decimal> f|F - Float     (e.g., 15.5 or 15.5f)");
		PadoShell.println("         <decimal> d|D - Double    (e.g., 20.0d)");
		PadoShell.println("         '<string with \\ delimiter>' (e.g., '\\'Wow!\\'!' Hello, world')");
		PadoShell.println("         to_date('<date string>', '<simple date format>'");
		PadoShell.println("                       (e.g., to_date('04/10/2009', 'MM/dd/yyyy')");
		PadoShell.println("     -buffer <name> <row number list>  Get entries from the specified buffer using the");
		PadoShell.println("                   enumerated buffer row numbers. Use 'buffer <name>' to get the list");
		PadoShell.println("                   of enumerated keys.");
		PadoShell.println("     -r   Show the server that is targetted by the specified routing key.");
		PadoShell.println(
				"     <row number list> format: num1 num2 num3-num5 ... e.g., 'which -buffer product 1 2 4 10-20'");
	}

	@Override
	public String getShortDescription()
	{
		return "Get one or more path values for specified keys.";
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
	@SuppressWarnings({ "unchecked" })
	public void run(CommandLine commandLine, String command) throws Exception
	{
		String path = commandLine.getOptionValue("path");
		String bufferName = commandLine.getOptionValue("buffer");
		boolean isRoutingKey = commandLine.hasOption('r');
		List<String> argList = commandLine.getArgList();

		if (path != null && bufferName != null) {
			PadoShell.printlnError(this, "Specifying both path and buffer not allowed. Only one option allowed.");
			return;
		}
		if (path == null && bufferName == null) {
			path = padoShell.getCurrentPath();
		}

		if (path != null) {

			if (commandLine.getArgList().size() < 2) {
				PadoShell.printlnError(this, "Invalid command. Key or key fields must be specified.");
				return;
			}
			String input = (String) commandLine.getArgList().get(1);
			Object key = null;
			if (input.startsWith("'")) {
				int lastIndex = -1;
				if (input.endsWith("'") == false) {
					lastIndex = input.length();
				} else {
					lastIndex = input.lastIndexOf("'");
				}
				if (lastIndex <= 1) {
					PadoShell.printlnError(this, "Invalid key. Empty string not allowed.");
					return;
				}
				key = input.subSequence(1, lastIndex); // lastIndex exclusive
			} else {
				key = ObjectUtil.getPrimitive(padoShell, input, false);
				if (key == null) {
					key = padoShell.getQueryKey(argList, 1);
				}
			}
			if (key == null) {
				return;
			}
			// long startTime = System.currentTimeMillis();
			if (padoShell.isShowTime() && padoShell.isShowResults()) {
				TimerUtil.startTimer();
			}

			String fullPath = padoShell.getFullPath(path);
			String gridPath = GridUtil.getChildPath(fullPath);
			String gridId = SharedCache.getSharedCache().getGridId(fullPath);
			IUtilBiz utilBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IUtilBiz.class,
					gridPath);
			utilBiz.getBizContext().getGridContextClient().setGridIds(gridId);
			if (isRoutingKey) {
				WhichInfo whichInfo = utilBiz.whichRoutingKey(gridPath, key);
				if (whichInfo == null) {
					PadoShell.printlnError(this, "Routing key not found.");
					return;
				}
				if (padoShell.isShowResults()) {

					List<Map<String, Object>> whichMapList = new ArrayList<Map<String, Object>>(1);
					TreeMap<String, Object> map = new TreeMap<String, Object>();
					whichMapList.add(map);
					map.put("GridId", whichInfo.getGridId());
					map.put("Host", whichInfo.getHost());
					map.put("ServerName", whichInfo.getServerName());
//					map.put("ServerId", whichInfo.getServerId());
					map.put("RedundancyZone", whichInfo.getRedundancyZone());
					PrintUtil.printList(whichMapList, 0, 1, whichMapList.size(), whichMapList.size(), null);
				}
			} else {
				List<WhichInfo> whichList = utilBiz.which(gridPath, key);
				if (whichList == null) {
					PadoShell.printlnError(this, "Key not found.");
					return;
				}
				if (padoShell.isShowResults()) {
					printWhichInfoList(whichList);
				}
			}

		} else {
			// Get key from the buffer
			BufferInfo bufferInfo = SharedCache.getSharedCache().getBufferInfo(bufferName);
			if (bufferInfo == null) {
				PadoShell.printlnError(this, bufferName + ": Buffer undefined.");
				return;
			}
			String gridId = bufferInfo.getGridId();
			String gridPath = bufferInfo.getGridPath();
			if (gridId == null || gridPath == null) {
				PadoShell.printlnError(this, bufferName + ": Invalid buffer. This buffer does not contain keys.");
				return;
			}
			Map<Integer, Object> keyMap = bufferInfo.getKeyMap(argList, 1);
			if (keyMap.size() > 0) {
				IUtilBiz utilBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IUtilBiz.class,
						gridPath);
				utilBiz.getBizContext().getGridContextClient().setGridIds(gridId);
				for (Object key : keyMap.values()) {
					List<WhichInfo> whichList = utilBiz.which(gridPath, key);
					printWhichInfoList(whichList);
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void printWhichInfoList(List<WhichInfo> whichInfoList) throws Exception
	{
		List<Map<String, Object>> whichMapList = new ArrayList<Map<String, Object>>(whichInfoList.size());
		for (WhichInfo whichInfo : whichInfoList) {
			TreeMap<String, Object> map = new TreeMap<String, Object>();
			whichMapList.add(map);
			map.put("GridId", whichInfo.getGridId());
			map.put("Host", whichInfo.getHost());
			map.put("ServerName", whichInfo.getServerName());
//			map.put("ServerId", whichInfo.getServerId());
			map.put("RedundancyZone", whichInfo.getRedundancyZone());
			map.put("BucketId", whichInfo.getBucketInfo().getBucketId());
			map.put("BucktSize", whichInfo.getBucketInfo().getSize());
			map.put("BucktSize", whichInfo.getBucketInfo().getTotalBytes());
			map.put("IsPrimary", whichInfo.getBucketInfo().isPrimary());
			Object key = whichInfo.getKey();
			if (key != null) {
				if (key instanceof ITemporalKey) {
					ITemporalKey tk = (ITemporalKey)key;
					map.put("IdentityKey", tk.getIdentityKey());
				} else {
					map.put("Key", key);
				}
			} else {
				map.put("Key", key);
			}
			
		}
		PrintUtil.printList(whichMapList, 0, 1, whichMapList.size(), whichMapList.size(), null);
	}
}
