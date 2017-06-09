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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.gemstone.gemfire.internal.tools.gfsh.app.util.PrintUtil;
import com.netcrest.pado.IPado;
import com.netcrest.pado.biz.mon.ISysBiz;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.CacheServerInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.PadoInfo;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;

public class grid implements ICommand {
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("s", false, "");
		Option opt = OptionBuilder.create("attach");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
		// options.addOption("table", false, "");
		opt = OptionBuilder.create("detach");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
		// options.addOption("type", false, "");
		opt = OptionBuilder.create("parent");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
		// options.addOption("col", false, "");
		opt = OptionBuilder.create("child");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
	}

	@Override
	public void initialize(PadoShell padoShell) {
		this.padoShell = padoShell;
	}

	@Override
	public void help() {
		String hostGridId = "host";
		if (SharedCache.getSharedCache().isLoggedIn()) {
			hostGridId = "'" + SharedCache.getSharedCache().getPado().getGridId() + "'";
		}
		PadoShell.println("grid [-s] [-?]");
		PadoShell.println("grid -attach|-detach -parent <grid-id>");
		PadoShell.println("grid [-attach -child <grid-id>]");
		PadoShell.println("   Execute grid operations. If options are not specified then show grid status.");
		PadoShell.println("   Use -attach/-detach options to attach/detach the " + hostGridId + " grid from its");
		PadoShell.println("   parent grid or the specified child grid. An attached grid participates as a child");
		PadoShell.println("   grid to the parent grid which provides data services to all of its child grids as");
		PadoShell.println("   a single entry point. A detached grid no longer participates in the Pado cluster");
		PadoShell.println("   and as a result, its previous parent also no longer privides data services from");
		PadoShell.println("   the detached grid.");
		PadoShell.println();
		PadoShell.println("      -s      List server information.");
		PadoShell.println("      -attach Attach the specified grid.");
		PadoShell.println("      -detach Detach the specified grid.");
		PadoShell.println("      -parent <grid-id>  Attach or detach the " + hostGridId + " grid to/from");
		PadoShell.println("                         the specified parent grid.");
		PadoShell.println("      -child <grid-id>   Attach the specified child grid to the " + hostGridId + " grid.");
		PadoShell.println("                         Note that detaching a child grid is not supported.");
		PadoShell.println("   Default: Must specify all options.");
		PadoShell.println("   Examples:");
		PadoShell.println("      grid -attach -parent parentGrid   -- Attach the " + hostGridId
				+ " grid as a child to 'parentGrid'.");
		PadoShell.println(
				"      grid -detach -parent parentGrid   -- Detach the " + hostGridId + " grid from 'parentGrid'");
		PadoShell.println(
				"      grid -attach -child childGrid     -- Attach 'childGrid' to the " + hostGridId + " grid.");
	}

	@Override
	public String getShortDescription() {
		return "Display grid status or attach/detach grids to/from the parent grid.";
	}

	@Override
	public boolean isLoginRequired() {
		return true;
	}

	@Override
	public Options getOptions() {
		return options;
	}

	@Override
	public void run(CommandLine commandLine, String command) throws Exception {
		String parentGridId = commandLine.getOptionValue("parent");
		String childGridId = commandLine.getOptionValue("child");
		if (parentGridId != null && childGridId != null) {
			PadoShell.printlnError(this, "Invalid command. Both -parent and -child are not allowed.");
		} else if (commandLine.hasOption("attach")) {
			if (childGridId != null) {
				PadoShell.printlnError(this,
						"A child grid cannot be attached by a parent grid.\n"
								+ "To attach a child grid, login to the child grid and use the\n"
								+ "\"-attach -parent <grid id>\" option.");
			}
			runCommand(commandLine);
		} else if (commandLine.hasOption("detach")) {
			runCommand(commandLine);
		} else if (commandLine.hasOption("s")) {
			showAllGridServers();
		} else {
			showGridSummary();
		}
	}

	private void runCommand(CommandLine commandLine) throws Exception {
		String parentGridId = commandLine.getOptionValue("parent");
		String childGridId = commandLine.getOptionValue("child");
		if (commandLine.hasOption("attach")) {
			attach(parentGridId, childGridId);
		} else if (commandLine.hasOption("detach")) {
			detach(parentGridId, childGridId);
		} else if (commandLine.hasOption("server")) {
			showAllGridServers();
		} else {
			showGridSummary();
		}
	}

	private void attach(String parentGridId, String childGridId) throws Exception {
		IPado pado = SharedCache.getSharedCache().getPado();
		ISysBiz sysBiz = pado.getCatalog().newInstance(ISysBiz.class);

		if (parentGridId != null) {
			sysBiz.attachToParentGrid(parentGridId);
		} else if (childGridId != null) {
			sysBiz.attachChildToParentGrid(childGridId);
		}
		SharedCache.getSharedCache().refresh();
	}

	private void detach(String parentGridId, String childGridId) throws Exception {
		IPado pado = SharedCache.getSharedCache().getPado();
		ISysBiz sysBiz = pado.getCatalog().newInstance(ISysBiz.class);
		if (parentGridId != null) {
			String padoGridId = sysBiz.getBizContext().getGridService().getPadoGridId();
			sysBiz.getBizContext().getGridContextClient().setGridIds(padoGridId);
			sysBiz.detachFromParentGrid(parentGridId);
		} else if (childGridId != null) {

			// Logged on to parent grid. Wants to detach the specified child
			// grid.

			// First, invoke the child grid to remove itself from the parent
			// grid
			sysBiz.getBizContext().getGridContextClient().setGridIds(childGridId);
			parentGridId = sysBiz.getBizContext().getGridService().getPadoGridId();
			sysBiz.detachFromParentGrid(parentGridId);

			// In case the child grid was already detached before the above
			// call, detach the child grid from the parent grid.
			sysBiz.getBizContext().getGridContextClient().setGridIds();
			sysBiz.detachChildFromParentGrid(childGridId);
		}
		SharedCache.getSharedCache().refresh();
	}

	@SuppressWarnings({ "rawtypes" })
	private void showGridSummary() {
		PadoInfo padoInfo = SharedCache.getSharedCache().getHostPadoInfo();
		if (padoInfo == null || padoInfo.getAllGridInfos() == null) {
			PadoShell.println(this, "Incomplete grid status. Please run 'refresh' and try it again.");
			return;
		}
		ArrayList<Map> list = new ArrayList<Map>(padoInfo.getAllGridInfos().length);
		GridInfo gridInfo = padoInfo.getGridInfo();
		list.add(createGridSummaryMap(gridInfo, "super"));
		GridInfo[] childGridInfos = padoInfo.getChildGridInfos();
		for (GridInfo gridInfo2 : childGridInfos) {
			list.add(createGridSummaryMap(gridInfo2, "child"));
		}
		PrintUtil.printList(list);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map createGridSummaryMap(GridInfo gridInfo, String gridType) {
		HashMap map = new HashMap();
		map.put("GridId", gridInfo.getGridId());
		map.put("Grid", gridType);
		map.put("RootPath", gridInfo.getGridRootPath());
		map.put("Servers", gridInfo.getCacheInfoList().size());
		map.put("Locators", gridInfo.getLocators());
		map.put("ClientLocators", gridInfo.getClientLocators());
		map.put("IBiz", gridInfo.getBizSet().size());
		map.put("Children", gridInfo.getChildGridIds().length);
		map.put("Location", gridInfo.getLocation());
		map.put("Paths", gridInfo.getCacheInfo().getAllPathInfoList().size());
		map.put("TemporalPaths", gridInfo.getCacheInfo().getAllTemporalPathInfoList().size());
		return map;
	}

	@SuppressWarnings({ "rawtypes" })
	private void showAllGridServers() {
		PadoInfo padoInfo = SharedCache.getSharedCache().getHostPadoInfo();
		if (padoInfo == null || padoInfo.getAllGridInfos() == null) {
			PadoShell.println(this, "Incomplete grid status. Please run 'refresh' and try it again.");
			return;
		}
		ArrayList<Map> mapList = new ArrayList<Map>(padoInfo.getAllGridInfos().length);
		GridInfo gridInfo = padoInfo.getGridInfo();
		addServerMaps(gridInfo, "super", mapList);
		GridInfo[] childGridInfos = padoInfo.getChildGridInfos();
		for (GridInfo gridInfo2 : childGridInfos) {
			addServerMaps(gridInfo2, "child", mapList);
		}
		PrintUtil.printList(mapList);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Map> addServerMaps(GridInfo gridInfo, String gridType, List<Map> mapList) {
		List<CacheInfo> clist = gridInfo.getCacheInfoList();
		if (clist != null) {
			for (CacheInfo cacheInfo : clist) {
				HashMap map = new HashMap();
				map.put("GridId", gridInfo.getGridId());
				map.put("ServerId", cacheInfo.getId());
				map.put("Grid", gridType);
				map.put("RootPath", gridInfo.getGridRootPath());
				map.put("Location", gridInfo.getLocation());
				map.put("Host", cacheInfo.getHost());
				map.put("Paths", cacheInfo.getAllPathInfoList().size());
				map.put("PID", cacheInfo.getProcessId());
				List<CacheServerInfo> csiList = cacheInfo.getCacheServerInfoList();
				StringBuffer buffer = new StringBuffer(csiList.size() * 6);
				for (int i = 0; i < csiList.size(); i++) {
					if (i != 0) {
						buffer.append(",");
					}
					buffer.append(csiList.get(i).getPort());
				}
				map.put("Ports", buffer.toString());
				mapList.add(map);
			}
		}
		return mapList;
	}
}
