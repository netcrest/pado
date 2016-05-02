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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.netcrest.pado.IPado;
import com.netcrest.pado.biz.IIndexMatrixBiz;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.link.IIndexMatrixBizLink.QueryType;
import com.netcrest.pado.tools.pado.BufferInfo;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.ResultSetDisplay;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.PadoShellUtil;
import com.netcrest.pado.tools.pado.util.PrintUtil;
import com.netcrest.pado.util.GridUtil;

public class less implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("k", false, "");
		options.addOption("v", false, "");
		options.addOption("kv", false, "");
		options.addOption("vk", false, "");
		options.addOption("refresh", false, "");
		options.addOption("buffer", true, "");
	}

	final static String QUERY_KEYS = "select * from %s.keySet";
	final static String QUERY_VALUES = "select * from %s.values";
	final static String QUERY_KEYS_VALUES = "select e.key, e.value from %s.entrySet e";
	
	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
		padoShell.addBufferCommand(this.getClass().getSimpleName());
	}

	@Override
	public void help()
	{
		PadoShell.println("less [-kv] [-refresh] [-buffer <name>] <path> | [-?]");
		PadoShell.println("less -buffer <name>");
		PadoShell.println("   Display the contents of the specified path or buffer.");
		PadoShell.println("   Commands that use buffered results are:");
		PadoShell.println("   " + padoShell.getBufferCommandSet());
		PadoShell.println("      -buffer  If <path> is not specified then it displays the specified buffer else");
		PadoShell.println("               it buffers the less results.");
		PadoShell.println("      -k       Display keys only.");
		PadoShell.println("      -v       Display values only.");
		PadoShell.println("      -refresh Refresh the contents by forcing the grid to rebuild the index matrix.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Display contents of specified path or buffer. Create a named buffer.";
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
	@SuppressWarnings({ "rawtypes" })
	public void run(CommandLine commandLine, String command) throws Exception
	{
		boolean isRefresh = commandLine.hasOption("refresh");
		String bufferName = commandLine.getOptionValue("buffer");
		String path = null;
		if (commandLine.getArgList().size() == 1) {
			if (bufferName == null) {
				PadoShell.printlnError(this, "Path not specified.");
				return;
			}
		} else {
			path = (String) commandLine.getArgList().get(1);
		}
		
		IScrollableResultSet rs = null;
		if (path == null) {
			BufferInfo bufferInfo = SharedCache.getSharedCache().getBufferInfo(bufferName);
			if (bufferInfo == null) {
				PadoShell.printlnError(this, bufferName + ": Buffer undefined.");
				return;
			}
			rs = bufferInfo.getScrollableResultSet();
			rs.setFetchSize(padoShell.getFetchSize());
		} else {
			boolean showKeys = PadoShellUtil.hasSingleLetterOption(commandLine, 'k', "refresh");
			boolean showValues = PadoShellUtil.hasSingleLetterOption(commandLine, 'v', "refresh");
			rs = queryPath(path, isRefresh, showKeys, showValues);
			if (rs == null) {
				return;
			}
			if (bufferName != null) {
				String fullPath = padoShell.getFullPath(path);
				String gridPath = GridUtil.getChildPath(fullPath);
				String gridId = SharedCache.getSharedCache().getPado().getCatalog().getGridService().getGridId(fullPath);
				SharedCache.getSharedCache().putBufferInfo(bufferName, new BufferInfo(bufferName, command, this, rs, gridId, gridPath));
			}
		}
		if (padoShell.isInteractiveMode()) {
			ResultSetDisplay.display(rs);
		} else {
			// Show header only for the first set.
			int startRowNum = 1;
			int rowsPrinted = PrintUtil.printScrollableResultSet(rs, startRowNum, true);
			startRowNum += rowsPrinted;
			if (rs.nextSet()) {
				do {
					rowsPrinted = PrintUtil.printScrollableResultSet(rs, startRowNum, false);
					startRowNum += rowsPrinted;
				} while (rs.nextSet());
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public IScrollableResultSet queryPath(String path, boolean isRefresh, boolean includeKeys, boolean includeValues)
	{
		String fullPath = padoShell.getFullPath(path);
		
		IPado pado = SharedCache.getSharedCache().getPado();
		IIndexMatrixBiz imBiz = pado.getCatalog().newInstance(IIndexMatrixBiz.class, true);
		imBiz.setForceRebuildIndex(isRefresh);
		imBiz.setQueryType(QueryType.OQL);
		String gridId = imBiz.getBizContext().getGridService().getGridId(fullPath);
		if (gridId == null) {
			if (padoShell.isRootPath(fullPath)) {
				PadoShell.printlnError(this, "Root path is always empty.");
			} else {
				PadoShell.printlnError(this, "Undefined path.");
			}
			return null;
		}
		GridInfo gridInfo = SharedCache.getSharedCache().getGridInfo(gridId);
		if (gridInfo == null) {
			PadoShell.printlnError(this, "Invalid path in from clause.");
			return null;
		}
		
		imBiz.setGridIds(gridId);
		imBiz.setFetchSize(padoShell.getFetchSize());
		int limit = padoShell.getSelectLimit();

		String queryString;
		if (includeKeys && includeValues == false) {
			queryString = String.format(QUERY_KEYS, fullPath);
		} else if (includeKeys == false && includeValues) {
			queryString = String.format(QUERY_VALUES, fullPath);
		} else {
			queryString = String.format(QUERY_KEYS_VALUES, fullPath);
		}
//		if (limit > 0) {
//			int limitPerServer = limit;
//			if (gridInfo.getCacheInfoList() != null) {
//				int serverCount = gridInfo.getCacheInfoList().size();
//				if (serverCount != 0) {
//					limitPerServer = limit / serverCount;
//				}
//			}
//			queryString += " limit " + limitPerServer;	
//		}
		imBiz.setLimit(limit);
		return imBiz.execute(queryString, queryString + " limit " + limit);
	}
}
