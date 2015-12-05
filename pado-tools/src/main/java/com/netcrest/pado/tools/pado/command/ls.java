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
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.netcrest.pado.biz.IPathBiz.PathType;
import com.netcrest.pado.gemfire.info.GemfireRegionInfo;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.RootPathInfo;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.PrintUtil;

public class ls implements ICommand
{
	private static final String HIDDEN_PATH_NAME_PREFIX = "__"; // 2 underscore

	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("a", false, "");
		options.addOption("l", false, "");
		options.addOption("R", false, "");
		options.addOption("aR", false, "");
		options.addOption("Ra", false, "");
		options.addOption("al", false, "");
		options.addOption("la", false, "");
		options.addOption("lR", false, "");
		options.addOption("Rl", false, "");
		options.addOption("alR", false, "");
		options.addOption("aRl", false, "");
		options.addOption("laR", false, "");
		options.addOption("lRa", false, "");
		options.addOption("Ral", false, "");
		options.addOption("Rla", false, "");
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("ls [-alR] <path> | [-?]");
		PadoShell.println("   List path information. If no option is specified, then it lists all");
		PadoShell.println("   path names except the hidden path names. A hidden path name begins");
		PadoShell.println("   with the prefix " + HIDDEN_PATH_NAME_PREFIX + " (2 underscores).");
		PadoShell.println("      -a  List all nested paths in the current directory including the hidden paths");
		PadoShell.println("          that begin with the prefix " + HIDDEN_PATH_NAME_PREFIX + " (2 underscores).");
		PadoShell.println("      -l  List in long format.");
		PadoShell.println("      -R  Recursively list all nested paths.");
	}

	@Override 
	public String getShortDescription()
	{
		return "List path information such as nested paths, sizes, types, etc.";
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
	public void run(CommandLine commandLine, String command) throws Exception
	{
		String path;
		if (commandLine.getArgList().size() == 1) {
			path = padoShell.getCurrentPath();
		} else {
			path = (String) commandLine.getArgList().get(1);
		}
		boolean includeHidden = commandLine.hasOption("a");
		boolean longList = commandLine.hasOption("l");
		boolean recursive = commandLine.hasOption("R");
		Option options[] = commandLine.getOptions();
		for (Option option : options) {
			if (includeHidden == false) {
				includeHidden = option.getOpt().contains("a");
			}
			if (longList == false) {
				longList = option.getOpt().contains("l");
			}
			if (recursive == false) {
				recursive = option.getOpt().contains("R");
			}
		}
		listPaths(path, includeHidden, longList, recursive);
	}

	private void listPaths(String path, boolean includeHidden, boolean longList, boolean recursive) throws Exception
	{
		if (path == null) {
			return;
		}
		String fullPath = padoShell.getFullPath(path);
		PathInfo pathInfo = SharedCache.getSharedCache().getPathInfo(fullPath);
		if (pathInfo == null) {
			PadoShell.printlnError(this, path + ": No such path.");
			return;
		}
		String gridId = SharedCache.getSharedCache().getGridId(fullPath);
		GridInfo gridInfo = SharedCache.getSharedCache().getGridInfo(gridId);
		List<CacheInfo> cacheInfoList = null;
		if (gridInfo != null) {
			cacheInfoList = gridInfo.getCacheInfoList();
		}
		int beginIndex = fullPath.length() + 1;
		Set<String> childPathSet = pathInfo.getChildGridPathSet(recursive);
		if (includeHidden) {
			childPathSet.add(".");
			childPathSet.add("..");
		}
		List<PathAttributes> pathList = new ArrayList<PathAttributes>(childPathSet.size());
		for (String childPath : childPathSet) {
			String path2;
			// If gridID is null then the path is at the top-level
			String fullPath2 = null;
			if (fullPath.equals("/")) {
				fullPath2 = "/" + childPath;
				path2 = childPath;
				gridId = SharedCache.getSharedCache().getGridId(fullPath2);
				gridInfo = SharedCache.getSharedCache().getGridInfo(gridId);
				cacheInfoList = null;
				if (gridInfo != null) {
					cacheInfoList = gridInfo.getCacheInfoList();
				}
			} else {
				if (gridId != null) {
					if (childPath.contains(".")) {
						fullPath2 = padoShell.getFullPath(childPath, fullPath);
						path2 = childPath;
					} else {
						fullPath2 = SharedCache.getSharedCache().getFullPath(gridId, childPath);
						path2 = fullPath2.substring(beginIndex);
					}
				} else {
					path2 = childPath;
				}
			}
			if (includeHidden == false && (path2.startsWith(HIDDEN_PATH_NAME_PREFIX) || path2.contains("/__"))) {
				continue;
			}

			String pathType;
			int bucketCount = 0;
			PathInfo pi = SharedCache.getSharedCache().getPathInfo(fullPath2);
			if (pi instanceof RootPathInfo) {
				pathType = "GRID";
			} else {
				GemfireRegionInfo pathInfo2 = (GemfireRegionInfo) pi;
				if (pathInfo2 != null) {
					bucketCount = pathInfo2.getTotalBucketCount();
				}
				if (pathInfo2 == null) {
					pathType = "GRID";
				} else if (pathInfo2.isTemporal(false)) {
					pathType = PathType.TEMPORAL.name();
				} else if (pathInfo2.isDataPolicyPartitionedRegion(false)) {
					pathType = PathType.PARTITION.name();
				} else if (pathInfo2.isScopeGlobalRegion(false)) {
					pathType = PathType.GLOBAL.name();
				} else if (pathInfo2.isScopeLocalRegion(false)) {
					pathType = PathType.LOCAL.name();
				} else if (pathInfo2.isDataPolicyReplicateRegion(false)) {
					pathType = PathType.REPLICATE.name();
				} else if (pathInfo2.isDataPolicyEmptyRegion(false)) {
					pathType = PathType.REPLICATE_EMPTY.name();
				} else if (pathInfo2.isDataPolicyNormalRegion(false)) {
					pathType = PathType.NORMAL.name();
				} else if (pathInfo2.isDataPolicyPreloadedRegion(false)) {
					pathType = PathType.PRELOADED.name();
				} else {
					pathType = "UNKNOWN";
				}
			}

			int size = 0;
			if (pathType.equals(PathType.TEMPORAL.name()) || pathType.equals(PathType.PARTITION.name())) {
				if (cacheInfoList != null) {
					for (CacheInfo cacheInfo : cacheInfoList) {
						PathInfo pathInfo3 = cacheInfo.getPathInfo(fullPath2);
						if (pathInfo3 != null) {
							size += pathInfo3.getSize();
						}
					}
				}
			} else {
				size = pi == null ? 0 : pi.getSize();
			}
			pathList.add(new PathAttributes(path2, pathType, size, bucketCount));
		}
		Collections.sort(pathList);
		if (longList) {
			PrintUtil.printList(pathList);
		} else {
			for (PathAttributes pa : pathList) {
				PadoShell.println(pa.path);
			}
		}
	}

	public class PathAttributes implements Comparable<PathAttributes>
	{
		String pathType;
		int size;
		int buckets;
		String path;

		PathAttributes(String path, String pathType, int size, int buckets)
		{
			this.path = path;
			this.pathType = pathType;
			this.size = size;
			this.buckets = buckets;
		}

		@Override
		public int compareTo(PathAttributes o)
		{
			return this.path.compareTo(o.path);
		}

		public String getPathType()
		{
			return pathType;
		}

		public int getSize()
		{
			return size;
		}

		public int getBuckets()
		{
			return buckets;
		}

		public String getPath()
		{
			return path;
		}
	}
}
