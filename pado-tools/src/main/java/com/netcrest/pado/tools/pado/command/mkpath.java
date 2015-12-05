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
import com.netcrest.pado.biz.IPathBiz.PathType;
import com.netcrest.pado.util.GridUtil;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.PadoShellUtil;

public class mkpath implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("p", false, "");
		options.addOption("v", false, "");
		options.addOption("pv", false, "");
		options.addOption("vp", false, "");
		options.addOption("refid", true, "");
		options.addOption("temporal", false, "");
		options.addOption("temporalLucene", false, "");
		options.addOption("type", true, "");
		options.addOption("buckets", true, "");
		options.addOption("diskStoreName", true, "");
		options.addOption("colocatedWith", true, "");
		options.addOption("redundantCopies", true, "");
		options.addOption("gatewaySenderIds", true, "");
	}

	private static String[] excludes = new String[] { "type", "diskStoreName", "buckets", "colocatedWith",
			"redundantCopies" };

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("mkpath -refid <reference id> [-pv] [-temporal|-temporalLucene] <path> ...");
		PadoShell.println("mkpath [-pv] [-type <path type>] <path> ... | [-?]");
		PadoShell.println("       [-buckets <total-number-of-buckets>]");
		PadoShell.println("       [-colocatedWith <full path>]");
		PadoShell.println("       [-redundantCopies <number-of-redundant-copies-of-path-entries>]");
		PadoShell.println("       [-?]");
		PadoShell.println("   Create path(s). '-buckets', '-colocatedWith', and '-redundantCopies' are only valid");
		PadoShell.println("   for partitioned and temporal paths.");
		PadoShell.println("       -refid <reference id> Create the specified path using the path attributes");
		PadoShell.println("                 identified by the specified reference ID.");
		PadoShell.println("       -temporal Create temporal path. This option is valid only if -refid is specified.");
		PadoShell.println("       -temporalLucene Create temporal path with Lucene dynamic indexing enabled. This");
		PadoShell.println("                 option is valid only if -refid is specified.");
		PadoShell.println("       -p        Create intermediate paths as required. Intermediate paths");
		PadoShell.println("                 are created with the " + PathType.REPLICATE + " type.");
		PadoShell.println("       -v        List paths as they are created.");
		PadoShell.println("       -type <path type>  Path type. Default: " + PathType.TEMPORAL.toString());
		PadoShell.println("           Valid <path type>:");
		PathType pathTypes[] = PathType.values();
		for (PathType pathType : pathTypes) {
			if (pathType != PathType.NOT_SUPPORTED) {
				PadoShell.println("               " + pathType.toString());
			}
		}
		PadoShell.println("       -diskStoreName <disk-store-name> Disk store name defined in the grid.");
		PadoShell.println("              If undefined then the default disk store name is used. This option");
		PadoShell.println("              applies to persistent paths only.");
		PadoShell.println("              Default: default grid store name defined by the grid");
		PadoShell.println("       -gatewaySenderIds <comma-separted sender ids>  Enables gateways if specified.");
		PadoShell.println("              Gateway sender IDs are typically defined in server configuration files, ");
		PadoShell.println("              i.e. GemFire server.xml.");
		PadoShell.println("       -buckets <total-number-of-buckets> Total number of buckets across the entire");
		PadoShell.println("              grid. Default: 113");
		PadoShell.println("       -colocatedWith <full path> Colocate the new path with the specified full path.");
		PadoShell.println("       -redundantCopies <number-of-redundant-copies-of-path-entries>  Default: 1");

	}

	@Override
	public String getShortDescription()
	{
		return "Make one or more directories (or paths).";
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
		String refid = commandLine.getOptionValue("refid");
		if (refid != null) {
			handleRefid(commandLine);
		} else {
			handlePathType(commandLine);
		}
	}

	@SuppressWarnings({ "unchecked" })
	private void handlePathType(CommandLine commandLine)
	{
		List<String> argList = commandLine.getArgList();
		boolean recursive = PadoShellUtil.hasSingleLetterOption(commandLine, 'p', excludes);
		boolean verbose = PadoShellUtil.hasSingleLetterOption(commandLine, 'v', excludes);
		String type = commandLine.getOptionValue("type");
		String diskStoreName = commandLine.getOptionValue("diskStoreName");
		int numBuckets = 113;
		String colocatedWith = null;
		int redundantCopies = 1;
		String value = commandLine.getOptionValue("buckets");
		if (value != null) {
			numBuckets = Integer.parseInt(value);
		}
		colocatedWith = commandLine.getOptionValue("colocatedWith");
		value = commandLine.getOptionValue("redundantCopies");
		if (value != null) {
			redundantCopies = Integer.parseInt(value);
		}

		// Determine the path type
		PathType pathType;
		if (type == null) {
			pathType = PathType.TEMPORAL;
		} else {
			pathType = PathType.valueOf(type.toUpperCase());
		}
		if (pathType == null) {
			pathType = PathType.TEMPORAL;
		}
		
		String gatewaySenderIds = commandLine.getOptionValue("gatewaySenderIds");
		
		// Create path(s)
		List<String> fullPathList = getFullPathList(commandLine);
		if (fullPathList == null) {
			return;
		}
		boolean createdAtLeastOnePath = false;
		IPathBiz pathBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IPathBiz.class);
		for (String fullPath : fullPathList) {
			String gridId = pathBiz.getBizContext().getGridService().getGridId(fullPath);
			if (gridId == null) {
				PadoShell.printlnError(this, fullPath + ": Unable to determine grid ID");
			} else {
				String gridPath = GridUtil.getChildPath(fullPath);
				String colocatedWithFullPath = padoShell.getFullPath(colocatedWith);
				String colocatedWithGridPath = GridUtil.getChildPath(colocatedWithFullPath);
				boolean created = pathBiz.createPath(gridId, gridPath, pathType, diskStoreName, gatewaySenderIds, colocatedWithGridPath,
						redundantCopies, numBuckets, recursive);
				if (created == false) {
					PadoShell.printlnError(this, fullPath + ": Path creation failed");
				} else {
					createdAtLeastOnePath = true;
					if (verbose) {
						PadoShell.println(this, "Created path '" + fullPath + "'");
					}
				}
			}
		}
		if (createdAtLeastOnePath) {
			SharedCache.getSharedCache().refresh();
		}
	}

	@SuppressWarnings({ "unchecked" })
	private void handleRefid(CommandLine commandLine)
	{
		List<String> argList = commandLine.getArgList();
		String refid = commandLine.getOptionValue("refid");
		boolean temporal = commandLine.hasOption("temporal");
		boolean temporalLucene = commandLine.hasOption("temporalLucene");
		boolean recursive = PadoShellUtil.hasSingleLetterOption(commandLine, 'p', excludes);
		boolean verbose = PadoShellUtil.hasSingleLetterOption(commandLine, 'v', excludes);
		
		// Create path(s)
		List<String> fullPathList = getFullPathList(commandLine);
		if (fullPathList == null) {
			return;
		}
		
		if (temporalLucene) {
			temporal = true;
		}
		
		boolean createdAtLeastOnePath = false;
		IPathBiz pathBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IPathBiz.class);
		for (String fullPath : fullPathList) {
			String gridId = pathBiz.getBizContext().getGridService().getGridId(fullPath);
			if (gridId == null) {
				PadoShell.printlnError(this, fullPath + ": Unable to determine grid ID");
			} else {
				String gridPath = GridUtil.getChildPath(fullPath);
				boolean created = pathBiz.createPath(gridId, gridPath, refid, temporal, temporalLucene, recursive);
				if (created == false) {
					PadoShell.printlnError(this, fullPath + ": Path creation failed");
				} else {
					createdAtLeastOnePath = true;
					if (verbose) {
						PadoShell.println(this, "Created path '" + fullPath + "'");
					}
				}
			}
		}
		if (createdAtLeastOnePath) {
			SharedCache.getSharedCache().refresh();
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
