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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.gemstone.gemfire.internal.tools.gfsh.app.util.PrintUtil;
import com.netcrest.pado.biz.IUtilBiz;
import com.netcrest.pado.info.CacheDumpInfo;
import com.netcrest.pado.info.DumpInfo;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.PadoShellUtil;

public class dump implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("all", false, "");
		options.addOption("l", false, "");
		options.addOption("R", false, "");
		options.addOption("r", false, "");
		options.addOption("lR", false, "");
		options.addOption("Rl", false, "");
		options.addOption("lr", false, "");
		options.addOption("rl", false, "");


		// PadoShellUtil.addSingleLetterOptions(options, "lR");

		Option opt = OptionBuilder.create("grid");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("import");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
	}
	
	private final static String[] excludes = new String[] { "all", "grid" };

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("dump [-import <as-of-time>] [-all] [-grid <grid ID>[,...]] [<path>...] [-?]");
		PadoShell.println("dump [-lR] [-all] [-grid <grid ID>[,...]]");
		PadoShell.println("   Dump the contents of the entire grid(s) or the specified path(s) to CSV file(s)");
		PadoShell.println("   in the grid server file systems. To import the dumped files, use the");
		PadoShell.println("   '-import' option.");
		PadoShell.println("      -all    If specified, then all of the paths are assigned. If '-grid' is not");
		PadoShell.println("              specified, then the host grid is assigned.");
		PadoShell.println("      -grid   This option is must be specified with '-all'. If specified, then");
		PadoShell.println("              only the specified grids are assigned. Grid IDs must be comma-separated");
		PadoShell.println("              with no spaces.");
		PadoShell.println("      -l      List all dumped files in all grids.");
		PadoShell.println("      -import Import the specified timed dump(s) from the host or");
		PadoShell.println("              specified grid. If <as-of-time> is 'now' then it imports the latest dumped");
		PadoShell.println("              files. <as-of-time> format is yyyy/MM/dd HH:mm");
		PadoShell.println("      -R      Recursively list all paths.");
		PadoShell.println("      -r      Same as -R.");
		PadoShell.println("   Examples:");
		PadoShell.println("      dump -grid mygrid -all     -- Dump all paths in mygrid.");
		PadoShell.println("      dump -grid mygrid          -- Dump the current path.");
		PadoShell.println("      dump -import now           -- Import that latest into the current path.");
		PadoShell.println("      dump -import now /mygrid/foo /mygrid/parent/child ");
		PadoShell.println("                                 -- Import the specified paths.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Dump contents of all or specified paths to CSV files in the server-side for archival. Use 'import' to reload dumped files.";
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
		boolean isAll = commandLine.hasOption("all");
		List<String> argList = (List<String>) commandLine.getArgList();
		if (isAll && argList.size() > 1) {
			PadoShell.printlnError(this, "'-all' and path(s) not allowed together.");
			return;
		}

		boolean isList = PadoShellUtil.hasSingleLetterOption(commandLine, 'l', excludes);
		boolean isRecursive = PadoShellUtil.hasSingleLetterOption(commandLine, 'R', excludes) || PadoShellUtil.hasSingleLetterOption(commandLine, 'r', excludes);

		if (isList) {
			String[] gridIds = PadoShellUtil.getGridIds(this, commandLine, false);
			if (gridIds == null) {
				return;
			}
			listDumps(gridIds, isAll, isRecursive);

		} else {
		
			// Parse [-all [-grid <grid ID>[,...]] | [<path>...]]
			String[] gridIds = PadoShellUtil.getGridIds(this, commandLine, false);
			if (gridIds == null) {
				return;
			}
			String fullPaths[] = PadoShellUtil.getFullPaths(padoShell, commandLine);
			if (fullPaths == null && isAll == false) {
				PadoShell.printlnError(this, "Path(s) not specified.");
				return;
			}
			if (PadoShellUtil.hasError(this, padoShell, fullPaths, false)) {
				return;
			}

			if (commandLine.hasOption("import")) {
				String asOfTimeStr = commandLine.getOptionValue("import");
				Date asOfDate = null;
				if (asOfTimeStr.equalsIgnoreCase("now") == false) {
					try {
						asOfDate = dateFormat.parse(asOfTimeStr);
					} catch (Exception ex) {
						PadoShell.printlnError(this, asOfTimeStr + ": Invalid time format (yyyy/MM/dd HH:mm)");
						return;
					}
				}
				importServers(gridIds, asOfDate, isAll, fullPaths);
			} else {
				dumpServers(gridIds, fullPaths);
			}
		}
	}

	private void importServers(String gridIds[], Date asOfDate, boolean isAll, String... fullPaths)
	{
		List<String> serverDirList = null;
		IUtilBiz utilBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IUtilBiz.class);
		if (fullPaths == null || fullPaths.length == 0) {
			if (gridIds == null || gridIds.length == 0) {
				// This code is not reachable by the 'dump' command.
				// Grid IDs are always specified.
				PadoShell.println("Importing ALL paths for ALL grids... Please wait. This may take some time.");
				gridIds = SharedCache.getSharedCache().getPado().getCatalog().getGridIds();
			} else {
				PadoShell.println("Importing ALL paths... Please wait. This may take some time.");
			}
			for (String gridId : gridIds) {
				PadoShell.println("   " + gridId + ": <all paths>");
			}
			for (String gridId : gridIds) {
				utilBiz.getBizContext().getGridContextClient().setGridIds(gridId);
				if (asOfDate == null) {
					utilBiz.importAll();
				} else {
					serverDirList = utilBiz.importAll(asOfDate);
				}
				printServerDirList(serverDirList);
			}
		} else {
			PadoShell.println("Importing the specified  paths... Please wait. This may take some time.");
			Map<String, List<String>> map = PadoShellUtil.getGridPathMap(padoShell, fullPaths);
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				utilBiz.getBizContext().getGridContextClient().setGridIds(entry.getKey());
				if (asOfDate == null) {
					serverDirList = utilBiz.importServers(isAll, entry.getValue().toArray(new String[0]));
				} else {
					serverDirList = utilBiz.importServers(asOfDate, isAll, entry.getValue().toArray(new String[0]));
				}
				printServerDirList(serverDirList);
			}
		}
		SharedCache.getSharedCache().refresh();
		PadoShell.println("File import complete.");
	}

	/**
	 * Dumps data in the server's dump dir.
	 * 
	 * @param gridIds
	 *            null or empty to dump all grids.
	 * @param gridPaths
	 *            null or empty to dump all paths.
	 */
	private void dumpServers(String gridIds[], String... fullPaths)
	{
		List<String> serverDirList = null;
		IUtilBiz utilBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IUtilBiz.class);
		if (fullPaths == null || fullPaths.length == 0) {
			if (gridIds == null || gridIds.length == 0) {
				// This code is not reachable by the 'dump' command.
				// Grid IDs are always specified.
				PadoShell.println("Dumping ALL paths for ALL grids... Please wait. This may take some time.");
				gridIds = SharedCache.getSharedCache().getPado().getCatalog().getGridIds();
			} else {
				PadoShell.println("Dumping ALL paths... Please wait. This may take some time.");
			}
			for (String gridId : gridIds) {
				PadoShell.println("   " + gridId + ": <all paths>");
			}
			for (String gridId : gridIds) {
				utilBiz.getBizContext().getGridContextClient().setGridIds(gridId);
				serverDirList = utilBiz.dumpAll();
				printServerDirList(serverDirList);
			}
		} else {
			PadoShell.println("Dumping the specified  paths... Please wait. This may take some time.");
			Map<String, List<String>> map = PadoShellUtil.getGridPathMap(padoShell, fullPaths);
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				utilBiz.getBizContext().getGridContextClient().setGridIds(entry.getKey());
				serverDirList = utilBiz.dumpServers(entry.getValue().toArray(new String[0]));
				printServerDirList(serverDirList);
			}
		}

		PadoShell.println("File dump complete.");
	}

	private void printServerDirList(List<String> serverDirList)
	{
		if (serverDirList == null) {
			return;
		}
		for (int i = 0; i < serverDirList.size(); i++) {
			PadoShell.println("   " + (i + 1) + ". " + serverDirList.get(i));
		}
	}

	private void listDumps(String gridIds[], boolean isAll, boolean isRecursive)
	{
		IUtilBiz utilBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IUtilBiz.class);
		if (gridIds == null || gridIds.length == 0) {
			gridIds = SharedCache.getSharedCache().getPado().getCatalog().getGridIds();
		}

		if (isRecursive) {
			for (String gridId : gridIds) {
				List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
				utilBiz.getBizContext().getGridContextClient().setGridIds(gridId);
				List<CacheDumpInfo> list = utilBiz.getCacheDumpInfoList(isAll);
				if (list != null) {
					int i = 0;
					for (CacheDumpInfo cacheDumpInfo : list) {
						mapList = createDumpInfoMaps(mapList, cacheDumpInfo, isRecursive);
						if (i > 0) {
							PadoShell.println();
						}
						i++;
						PadoShell.println(gridId + ": " + cacheDumpInfo.getHost() + ": " + cacheDumpInfo.getName()
								+ ": " + cacheDumpInfo.getProcessId() + "(PID): " + cacheDumpInfo.getId());
						PrintUtil.printList(mapList);
						mapList = new ArrayList<Map<String, Object>>();
					}
				}
			}
		} else {
			List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
			for (String gridId : gridIds) {
				utilBiz.getBizContext().getGridContextClient().setGridIds(gridId);
				CacheDumpInfo cacheDumpInfo = utilBiz.getCacheDumpInfo(isAll);
				mapList = createRootInfoOnlyMaps(mapList, cacheDumpInfo);
			}
			PrintUtil.printList(mapList);
		}
	}

	/**
	 * Creates a map list that contains only the top-level (root) info.
	 * 
	 * @param mapList
	 * @param cacheDumpInfo
	 * @return
	 */
	private List<Map<String, Object>> createRootInfoOnlyMaps(List<Map<String, Object>> mapList,
			CacheDumpInfo cacheDumpInfo)
	{
		if (cacheDumpInfo != null) {
			List<DumpInfo> list = cacheDumpInfo.getDumpInfoList();
			Map<String, Object> map = null;
			for (DumpInfo dumpInfo : list) {
				map = createCacheDumpInfoMap(cacheDumpInfo);
				map = createDumpInfoMap(map, dumpInfo, false);
				mapList.add(map);
			}
		}
		return mapList;
	}

	private List<Map<String, Object>> createDumpInfoMaps(List<Map<String, Object>> mapList,
			CacheDumpInfo cacheDumpInfo, boolean isRecursive)
	{
		List<DumpInfo> list = cacheDumpInfo.getDumpInfoList();
		for (DumpInfo dumpInfo : list) {
			Map<String, Object> map = new TreeMap<String, Object>();
			map = createDumpInfoMap(map, dumpInfo, true);
			mapList.add(map);
			Set<DumpInfo> set = dumpInfo.getChildDumpInfoSet(isRecursive);
			List<DumpInfo> dumpList = new ArrayList<DumpInfo>(set);
			Collections.sort(dumpList);
			for (DumpInfo dumpInfo2 : set) {
				map = new TreeMap<String, Object>();
				map = createDumpInfoMap(map, dumpInfo2, true);
				mapList.add(map);
			}
		}
		return mapList;
	}

	private Map<String, Object> createCacheDumpInfoMap(CacheDumpInfo cacheDumpInfo)
	{
		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("GridId", cacheDumpInfo.getGridId());
		// map.put("Host", cacheDumpInfo.getHost());
		// map.put("Id", cacheDumpInfo.getId());
		// map.put("Name", cacheDumpInfo.getName());
		// map.put("Pid", cacheDumpInfo.getProcessId());
		return map;
	}

	private Map<String, Object> createDumpInfoMap(Map<String, Object> map, DumpInfo dumpInfo, boolean isForRecursive)
	{
		// Map<String, Object> map = createHeaderMap(cacheDumpInfo);
		map.put("Date", dateFormat.format(dumpInfo.getDate()));
		// map.put("GridPath", dumpInfo.getGridPath());
		map.put("FullPath", dumpInfo.getFullPath());
		// map.put("Name", dumpInfo.getName());
		if (isForRecursive) {
			map.put("FileSize", dumpInfo.getFileSize());
			map.put("Size", dumpInfo.getSize());
		}
		return map;
	}
}
