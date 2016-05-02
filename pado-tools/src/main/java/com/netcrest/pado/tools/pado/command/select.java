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

import com.netcrest.pado.biz.IIndexMatrixBiz;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.link.IIndexMatrixBizLink.QueryType;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.ResultSetDisplay;
import com.netcrest.pado.tools.pado.SharedCache;

public class select implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	
	static {
		options.addOption("?", false, "");
		options.addOption("refresh", false, "");
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("select <tuples where ...> | [-?]");
		PadoShell.println("   Execute the query.");
		PadoShell.println("      -refresh Refresh the contents by forcing the grid to rebuild the index matrix.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Execute GemFire OQL select query.";
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
		String queryString = command;
		String args[] = commandLine.getArgs();
		String pathToken = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("from")) {
				i++;
				if (i <= args.length - 1) {
					pathToken = args[i];
				}
				break;
			}
		}
		String gridId = SharedCache.getSharedCache().getSysBiz().getBizContext().getGridService().getGridId(pathToken);
		GridInfo gridInfo = SharedCache.getSharedCache().getGridInfo(gridId);
		if (gridInfo == null) {
			PadoShell.printlnError(this, "Invalid path in from clause");
			return;
		}
		
//		int limit = padoShell.getSelectLimit();
//		int limitPerServer = limit;
//		if (gridInfo.getCacheInfoList() != null) {
//			int serverCount = gridInfo.getCacheInfoList().size();
//			if (serverCount != 0) {
//				limitPerServer = limit / serverCount;
//			}
//		}
//		if (limit > 0) {
//			queryString += " limit " + limitPerServer;
//		}
		
		boolean refresh = commandLine.hasOption("refresh");
		executeQuery(gridId, queryString, refresh);
	}

	@SuppressWarnings("rawtypes")
	public void executeQuery(String gridId, String queryString, boolean refresh) throws Exception
	{
		IIndexMatrixBiz imBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IIndexMatrixBiz.class);
		imBiz.setQueryType(QueryType.OQL);
		imBiz.setGridIds(gridId);
		imBiz.setForceRebuildIndex(refresh);
		imBiz.setFetchSize(padoShell.getFetchSize());
		imBiz.setLimit(padoShell.getSelectLimit());
		IScrollableResultSet rs = imBiz.execute(queryString,  queryString + " limit " + padoShell.getSelectLimit());
		ResultSetDisplay.display(rs);
	}
}
