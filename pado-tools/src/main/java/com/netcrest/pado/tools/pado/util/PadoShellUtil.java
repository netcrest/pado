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
package com.netcrest.pado.tools.pado.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;

public class PadoShellUtil
{
	private final static SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private final static String QUERY_KEYS = "select * from %s.keySet";
	private final static String QUERY_VALUES = "select * from %s.values";
	private final static String QUERY_KEYS_VALUES = "select e.key, e.value from %s.entrySet e";

	/**
	 * Returns an array of grid IDs found in the specified commandLine with the
	 * -grid option.
	 * 
	 * @param command
	 *            Commnand
	 * @param commandLine
	 *            Command line with the -grid option. Grid IDs must be comma
	 *            separated with no spaces.
	 * @param isGridAllBothRequired
	 *            true if both -grid and -all must present in the specified
	 *            commandLine.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final static String[] getGridIds(ICommand command, CommandLine commandLine, boolean isGridAllBothRequired)
	{
		boolean isAllPaths = commandLine.hasOption("all");
		List<String> argList = (List<String>) commandLine.getArgList();
		if (isAllPaths && argList.size() > 1) {
			PadoShell.printlnError(command, "Both '-all' and path not allowed.");
			return null;
		}
		String gridValue = commandLine.getOptionValue("grid");
		if (isGridAllBothRequired) {
			if (gridValue != null && isAllPaths == false) {
				PadoShell.printlnError(command, "'-grid' requires '-all'");
				return null;
			}
		}
		String gridIds[] = null;
		if (gridValue == null) {
			String gridId = SharedCache.getSharedCache().getPado().getGridId();
			gridIds = new String[] { gridId };
		} else {
			gridIds = gridValue.split(",");
			List<String> gridIdList = new ArrayList<String>(gridIds.length);
			for (String gridId : gridIds) {
				if (gridId.length() == 0) {
					continue;
				}
				if (SharedCache.getSharedCache().getGridInfo(gridId) == null) {
					PadoShell.printlnError(command, gridId + ": Undefined grid ID.");
					return null;
				}
				if (gridIdList.contains(gridId) == false) {
					gridIdList.add(gridId);
				}
			}
			gridIds = gridIdList.toArray(new String[gridIdList.size()]);
		}
		return gridIds;
	}

	/**
	 * Returns an array of full paths determined by parsing all paths arguments
	 * found in the specified commandLine. Duplicates are ignored.
	 * 
	 * @param padoShell
	 *            PadoShell instance
	 * @param commandLine
	 *            Command line with path arguments.
	 * @return null if paths are not found in the specified commandLine.
	 */
	@SuppressWarnings("unchecked")
	public final static String[] getFullPaths(PadoShell padoShell, CommandLine commandLine)
	{
		List<String> argList = (List<String>) commandLine.getArgList();
		if (argList.size() <= 1) {
			return null;
		}
		HashSet<String> set = new HashSet<String>(argList.size());
		for (int i = 1; i < argList.size(); i++) {
			String path = argList.get(i);
			set.add(padoShell.getFullPath(path));
		}
		String fullPaths[] = set.toArray(new String[set.size()]);
		return fullPaths;
	}

	public final static boolean hasError(ICommand command, PadoShell padoShell, String fullPaths[],
			boolean isTemporalOnly)
	{
		if (fullPaths == null) {
			return false;
		}
		for (String fullPath : fullPaths) {
			if (padoShell.isRootPath(fullPath)) {
				PadoShell.printlnError(command, "Invalid path. Root path not allowed.");
				return true;
			}
			PathInfo pathInfo = SharedCache.getSharedCache().getPathInfo(fullPath);
			if (pathInfo == null) {
				PadoShell.printlnError(command, "Invalid path.");
				return true;
			}
			if (isTemporalOnly && pathInfo.isTemporal(false) == false) {
				PadoShell.printlnError(command, ": " + fullPath + ": Not a temporal path.");
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a map of <grid-id, List<grid-path> entries extracted from the
	 * specified full paths. Duplicate paths are ignored.
	 * 
	 * @param padoShell
	 *            PadoShell instance
	 * @param fullPaths
	 *            An array of full paths.
	 * @return Always returns a non-null map.
	 */
	public final static Map<String, List<String>> getGridPathMap(PadoShell padoShell, String[] fullPaths)
	{
		Map<String, List<String>> map = new HashMap<String, List<String>>(fullPaths.length, 1f);
		if (fullPaths != null) {
			for (String fullPath : fullPaths) {
				String gridId = SharedCache.getSharedCache().getGridId(fullPath);
				String gridPath = padoShell.getGridPathFromFullPath(fullPath);
				List<String> list = map.get(gridId);
				if (list == null) {
					list = new ArrayList<String>(fullPaths.length);
					map.put(gridId, list);
				}
				if (list.contains(gridPath) == false) {
					list.add(gridPath);
				}
				PadoShell.println("   " + gridId + ": " + gridPath);
			}
		}
		return map;
	}

	/**
	 * Not working. Do NOT use it.
	 * 
	 * @param options
	 * @param singleLetterOptions
	 */
	public final static void addSingleLetterOptions(Options options, String singleLetterOptions)
	{
		if (singleLetterOptions == null) {
			return;
		}
		singleLetterOptions = singleLetterOptions.trim();
		if (singleLetterOptions.length() == 0) {
			return;
		}
		char[] letters = singleLetterOptions.toCharArray();
		addOption(options, "", letters);
	}

	private final static void addOption(Options options, String prefix, char[] postfixLetters)
	{
		if (postfixLetters == null || postfixLetters.length == 0) {
			return;
		}
		char[] nextPostfixLetters = new char[postfixLetters.length - 1];
		System.arraycopy(postfixLetters, 1, nextPostfixLetters, 0, nextPostfixLetters.length);
		for (int i = 0; i < postfixLetters.length; i++) {
			String opt = prefix + postfixLetters[i];
			options.addOption(opt, false, "");
			System.out.println(opt);
			addOption(options, opt, nextPostfixLetters);

			// Go thru all combinations of nextPosfixLetters
			// String nextPrefix = opt;
			// for (int j = 0; j < nextPostfixLetters.length - 1; j++) {
			// char[] next = new char[nextPostfixLetters.length];
			// int nextRemainderLength = nextPostfixLetters.length - (j+1);
			// int index = nextRemainderLength - 1;
			// int length = j + 1;
			// System.arraycopy(nextPostfixLetters, j+1, next, 0,
			// nextRemainderLength);
			// System.arraycopy(nextPostfixLetters, 0, next, index, length);
			// addOption(options, nextPrefix, next);
			// }
		}

		System.out.println();
	}

	public final static boolean hasSingleLetterOption(CommandLine commandLine, char option, String... excludes)
	{
		Option[] options = commandLine.getOptions();
		for (Option option2 : options) {
			boolean skip = false;
			if (excludes != null) {
				for (String exclude : excludes) {
					if (option2.getOpt().equals(exclude)) {
						skip = true;
						break;
					}
				}
			}
			if (skip == false && option2.getOpt().indexOf(option) != -1) {
				return true;
			}
		}
		return false;
	}

	public final static SimpleDateFormat getIso8601DateFormat()
	{
		return iso8601DateFormat;
	}
}
