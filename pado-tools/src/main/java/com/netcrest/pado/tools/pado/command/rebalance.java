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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.info.PadoInfo;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.PrintUtil;

public class rebalance implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("all", true, "");
		options.addOption("commit", false, "");
		options.addOption("t", true, "");
		options.addOption("include", true, "");
		options.addOption("exclude", true, "");
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("rebalance [-all <comma separated grid IDs>] [-commit] [-t <timeout in msec>] [-?]");
		PadoShell.println("          [-include <comma separated list of paths>]");
		PadoShell.println("          [-exclude <comma separted list of paths>]");
		PadoShell.println("   Rebalance partitioned paths.");
		PadoShell.println("      -all     Rebalance all partitioned paths of the specified grid IDs.");
		PadoShell.println("               '-all' overrides '-include'.");
		PadoShell.println("      -include Rebalance the specified partitioned paths. Regular expression supported.");
		PadoShell.println("      -exclude Exclude the specified partitioned paths from rebalancing");
		PadoShell.println("               Rebalanced all other partitioned paths. Regular expresssion supported.");
		PadoShell
				.println("      -commit  If specified, then commits rebalancing, otherwise, rebalancing is simulated only.");
		PadoShell.println("      -t       Timeout period in second. Timeout is ignored if '-commit' is not");
		PadoShell.println("               specified. Default: 60.");
	}

	@Override
	public String getShortDescription()
	{
		return "Rebalance all or the specified partitioned paths.";
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

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void run(CommandLine commandLine, String command) throws Exception
	{
		String gridIdsStr = commandLine.getOptionValue("all");
		String[] gridIds = null;
		boolean isAll = gridIdsStr != null;
		String includePaths = null;
		if (isAll == false) {
			includePaths = commandLine.getOptionValue("include");
			if (includePaths == null) {
				PadoShell.printlnError(this, "Either '-all' or '-include' must be specified.");
				return;
			}
		} else {
			gridIds = gridIdsStr.split(",");
			PadoInfo padoInfo = SharedCache.getSharedCache().getHostPadoInfo();
			if (padoInfo == null || padoInfo.getAllGridInfos() == null) {
				PadoShell.println(this, "Incomplete grid status. Please run 'refresh' and try it again.");
				return;
			}
			for (String gridId : gridIds) {
				if (padoInfo.getGridInfo(gridId) == null) {
					PadoShell.println(this, "Invalid grid ID: " + gridId);
					return;
				}
			}
		}
		String excludePaths = commandLine.getOptionValue("exclude");
		HashMap<String, Set<String>> includeGridPathMap = new HashMap<String, Set<String>>();
		HashMap<String, Set<String>> excludeGridPathMap = new HashMap<String, Set<String>>();
		addGridPathSets(includeGridPathMap, includePaths);
		addGridPathSets(excludeGridPathMap, excludePaths);
		int timeoutInSec = 60;
		String timeoutInSecStr = commandLine.getOptionValue('t');
		if (timeoutInSecStr != null) {
			timeoutInSec = Integer.parseInt(timeoutInSecStr);
		}
		long timeoutInMsec = timeoutInSec * 1000;
		boolean isSimulate = !commandLine.hasOption("commit");

		IPathBiz pathBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IPathBiz.class);

		// Override the includeGridSet for all specified grid IDs by setting
		// it to null.
		if (gridIds != null) {
			for (String gridId : gridIds) {
				includeGridPathMap.put(gridId, null);
			}
		}

		// Rebalance all entries in includeGridPathMap.
		// Note that we don't iterate excludeGridPathMap. Any paths
		// in excludeGridPathMap that are not in includeGridPathMap are
		// simply ignored.
		for (Map.Entry<String, Set<String>> entry : includeGridPathMap.entrySet()) {
			String gridId = entry.getKey();
			Set<String> includeGridPathSet = entry.getValue();
			Set<String> excludeGridPathSet = excludeGridPathMap.get(gridId);
			// IMPORTANT: "__pado/server" must be included. Pado relies on this
			// path to obtain the buckets IDs to targer servers.
			if (includeGridPathSet != null) {
				includeGridPathSet.add("__pado/server");
			}
			JsonLite results = pathBiz.rebalance(gridId, includeGridPathSet, excludeGridPathSet, timeoutInMsec,
					isSimulate);
			printResults(results, isSimulate);
		}
	}

	private void printResults(JsonLite results, boolean isSimulate) throws Exception
	{
		if (isSimulate) {
			PadoShell.println("Path Rebalance: Simulation Only");
		} else {
			PadoShell.println("Path Rebalance: Committed");
		}
		if (results == null) {
			PadoShell.println("Rebalacning request timed out. Rebalancing will continue until completed.");
		} else {
			PrintUtil.printEntries(results, results.keySet(), null);
		}
	}

	private void addGridPathSets(Map<String, Set<String>> gridPathMap, String gridPaths)
	{
		if (gridPaths == null) {
			return;
		}

		String tokens[] = gridPaths.split(",");
		for (String path : tokens) {
			String gridId = padoShell.getGridId(path);
			String gridPath = padoShell.getGridPath(path);
			Set<String> gridPathSet = gridPathMap.get(gridId);
			if (gridPathSet == null) {
				gridPathSet = new HashSet<String>();
				gridPathMap.put(gridId, gridPathSet);
			}
			gridPathSet.add(gridPath);
		}
	}

}
