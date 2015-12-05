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
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.netcrest.pado.biz.ILuceneBiz;
import com.netcrest.pado.biz.ITemporalAdminBiz;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.util.GridUtil;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.temporal.AttachmentResults;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.AttachmentSetFactory;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.ResultSetDisplay;
import com.netcrest.pado.tools.pado.SharedCache;
import com.netcrest.pado.tools.pado.util.ObjectUtil;
import com.netcrest.pado.tools.pado.util.PadoShellUtil;
import com.netcrest.pado.tools.pado.util.TemporalPrintUtil;

public class temporal<K, V> implements ICommand
{
	private Pattern objectPattern;
	private Object identityKey = null;
	private long startValidTime;
	private long endValidTime;
	private Object value = null;
	private String valueStr, identityKeyStr, attachmentsStr = null;
	private long writtenTime;
	private boolean rawFormat;
	private boolean isDelta = false;
	private PadoShell padoShell;

	private static Options options = new Options();

	static {
		options.addOption("?", false, "");
		options.addOption("history", true, "");
		options.addOption("rawformat", false, "");
		options.addOption("lucene", false, "");
		options.addOption("all", false, "");
		options.addOption("enable", false, "");
		options.addOption("disable", false, "");
		options.addOption("refresh", false, "");

		// options.addOption("bulkload",false,"");
		// options.addOption("file",true,"");
		// options.addOption("batchsize",true,"");

		Option opt = OptionBuilder.create("put");
		opt.setArgs(6);
		opt.setOptionalArg(true);
		// opt.setValueSeparator(',');
		options.addOption(opt);

		opt = OptionBuilder.create("putAttachments");
		opt.setArgs(7);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("get");
		opt.setArgs(3);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("getAttachments1");
		opt.setArgs(3);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("getAttachments2");
		opt.setArgs(4);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("getAttachments3");
		opt.setArgs(3);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("remove");
		opt.setArgs(2);
		opt.setOptionalArg(true);
		options.addOption(opt);
		
		opt = OptionBuilder.create("grid");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
	}

	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
		this.objectPattern = Pattern.compile(".*\\[.*\\]");
	}

	@Override
	public void help()
	{
		PadoShell.println("temporal [-refresh] [<path>] [?]");
		PadoShell.println("         [-put <identity-key> <value> [<start-valid-time> <end-valid-time> <written-time>][<is-delta>]");
		PadoShell.println("         [-putAttachments <identity-key> <value> <attachmentSets> [<start-valid-time> <end-valid-time> <written-time>][<is-delta>]]");
		PadoShell.println("         [-get <identity key> [<valid-at> [<as-of>]]]");
		PadoShell.println("         [-getAttachments1 <identity-key>  [<valid-at> [<as-of>]]]");
		PadoShell.println("         [-getAttachments2 <identity-keys> <attachment-full-path> [<valid-at> [<as-of>]]]");
		PadoShell.println("         [-getAttachments3 <attachmentSets>  [<valid-at> [<as-of>]]]");
		PadoShell.println("         [-remove <identity-key> [<written-time>]]");
		PadoShell.println("         [-history <identity-key>] [-rawformat]]");
		PadoShell.println("temporal -lucene [-all [-grid <grid ID>[,...]] | [<path>...]]");
		PadoShell.println("temporal -enable|-disable [-all [-grid <grid ID>[,...]] | [<path>...]]");
		PadoShell.println("   Execute temporal data specific operations. If no options are specified, then it displays");
		PadoShell.println("   all now-relative temporal entries in the current or specified path.");
		PadoShell.println("   Dates are displayed as time in millisec since 1970 if -rawformat is specified.");
		PadoShell.println("      -lucene  Build lucene indexes for the current or specified paths.");
		PadoShell.println("               If '-all' is specified, then <path> is not required.");
		PadoShell.println("      -enable  Enable temporal data for the current or the specified paths.");
		PadoShell.println("      -disable Disabled temporal data for the current or the specified paths.");
		PadoShell.println("      -all     This option must be specified with '-lucene', '-enable', or '-disable'.");
		PadoShell.println("               If specified, then all of the temporal paths are assigned. If '-grid'");
		PadoShell.println("               is not specified, then the host grid is assigned.");
		PadoShell.println("      -grid    This option is must be specified with '-all'. If specified, then");
		PadoShell.println("               only the specified grids are assigned. Grid IDs must be comma-separated");
		PadoShell.println("               with no spaces.");
		PadoShell.println("      -refresh Refresh the contents by forcing the grid to rebuild the index matrix.");
		PadoShell.println();
		PadoShell.println("Examples:");
		PadoShell.println();
		PadoShell.println("      Add an entry:");
		PadoShell.println("         temporal foo/path -put string[key1] \"com.netcrest.grid.temporal.test.FooData[x=100 and y=101 and z=gfsh 1]\"");
		PadoShell.println("         temporal foo/path -put \"string[key 1]\" \"com.netcrest.grid.temporal.test.FooData[x=100 and y=101 and z=gfshz] false\"");
		PadoShell.println("         temporal foo/path -put \"com.netcrest.grid.temporal.test.DummyKey2[secId=id1 and dummy=dum1]\" \"com.netcrest.grid.temporal.test.FooData[x=100 and y=101 and z=gfshz]\" to_date('10/11/2005','dd/MM/yyyy') to_date('10/11/2006','MM/dd/yyyy') to_date('10/11/2005','dd/MM/yyyy') false ");
		PadoShell.println();
		PadoShell.println("      Add an entry with attachments:");
		PadoShell.println("         temporal foo/path -putAttachments string[key1] \"com.netcrest.grid.temporal.test.FooData[x=100 and y=101 and z=gfshz]\" \"string[null-key 1:key2,temporal2-key7]\"");
		PadoShell.println("         temporal foo/path -putAttachments  \"com.netcrest.grid.temporal.test.DummyKey2[secId=id1 and dummy=dum1]\" \"com.netcrest.grid.temporal.test.FooData[x=100 and y=101 and z=gfshz]\" com.netcrest.grid.temporal.test.DummyKey2[null-secId=id2|dummy=dum2:secId=id3|dummy=dum3]");
		PadoShell.println();
		PadoShell.println("      Show an entry:");
		PadoShell.println("         temporal foo/path -get string[key1] to_date('10/11/2005','dd/MM/yyyy') to_date('10/11/2006','MM/dd/yyyy')");
		PadoShell.println("         temporal -get string[key1] to_date('10/11/2005','MM/dd/yyyy')");
		PadoShell.println();
		PadoShell.println("      Show the latest entry:");
		PadoShell.println("         temporal foo/path -get string[key1]");
		PadoShell.println();
		PadoShell.println("      Show attachments of an entry:");
		PadoShell.println("         temporal foo/path -getAttachments1 string[key1] to_date('10/11/2005','dd/MM/yyyy') to_date('10/11/2006','MM/dd/yyyy')");
		PadoShell.println("         temporal -getAttachments1  com.netcrest.grid.temporal.test.DummyKey2[secId=id1|dummy=dum1] to_date('10/11/2005','MM/dd/yyyy')");
		PadoShell.println("         temporal foo/path  -getAttachments2 string[key1]  temporal2 to_date('10/11/2005','dd/MM/yyyy') to_date('10/11/2006','MM/dd/yyyy')");
		PadoShell.println("         temporal -getAttachments2  \"string[key 1,key2]\" null to_date('10/11/2005','MM/dd/yyyy')");
		PadoShell.println("         temporal foo/path  -getAttachments2 \"com.netcrest.grid.temporal.test.DummyKey2[secId=id1 and dummy=dum1,secId=id2 and dummy=dum2]\" null");
		PadoShell.println("         temporal foo/path  -getAttachments3 \"string[temporal1-key 1:key2,null-key3]\" to_date('10/11/2005','dd/MM/yyyy') to_date('10/11/2006','MM/dd/yyyy')");
		PadoShell.println("         temporal -getAttachments3  string[null-key1] to_date('10/11/2005','MM/dd/yyyy')");
		PadoShell.println("         temporal foo/path  -getAttachments3 string[temporal3-key1]");
		PadoShell.println();
		PadoShell.println("      Remove an entry:");
		PadoShell.println("         temporal foo/path -remove \"string[key 1]\" to_date('10/11/2003','MM/dd/yyyy')");
		PadoShell.println();
		PadoShell.println("      Show history of the specified indentity key:");
		PadoShell.println("         temporal -history \"string[key 1]\"");
		PadoShell.println("         temporal foo/path -history string[key1] -rawformat");
	}

	@Override 
	public String getShortDescription()
	{
		return "Execute temporal data specific operations, i.e., enable/disable temporal path, build lucene indexes, display history, etc.";
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
		boolean isLucene = commandLine.hasOption("lucene");
		boolean isEnable = commandLine.hasOption("enable");
		boolean isDisable = commandLine.hasOption("disable");
		boolean isAll = commandLine.hasOption("all");
		if (isAll && (isLucene == false && isEnable == false && isDisable == false)) {
			PadoShell.printlnError(this, "Invalid option. '-all' only applies to '-lucene', '-enable', and '-disable'.");
			return;
		} 
		
		List<String> argList = (List<String>) commandLine.getArgList();
		
		if (isLucene || isEnable || isDisable) {
			
			// Build Lucene indexes
			
			// Parse [-all [-grid <grid ID>[,...]] | [<path>...]]
			String[] gridIds = PadoShellUtil.getGridIds(this, commandLine, true);
			if (gridIds == null) {
				return;
			}
			String fullPaths[] = PadoShellUtil.getFullPaths(padoShell, commandLine);
			if (PadoShellUtil.hasError(this, padoShell, fullPaths, true)) {
				return;
			}
			
			if (isLucene) {
				runLucene(gridIds, fullPaths);
			} else {
				setTemporalEnabled(isEnable, gridIds, fullPaths);
			}
			
		} else {
			
			// Execute temporal command
			String path;
			if (argList.size() == 1) {
				path = padoShell.getCurrentPath();
			} else {
				path = argList.get(1);
			}
			String fullPath = padoShell.getFullPath(path);
			String gridId = SharedCache.getSharedCache().getGridId(fullPath);
			String gridPath = GridUtil.getChildPath(fullPath);
			
			String fullPaths[] = PadoShellUtil.getFullPaths(padoShell, commandLine);
			if (fullPaths == null) {
				fullPaths = new String[] { fullPath };
			}
			if (PadoShellUtil.hasError(this, padoShell, fullPaths, false)) {
				return;
			}

			ITemporalBiz temporalBiz = SharedCache.getSharedCache().getPado().getCatalog()
					.newInstance(ITemporalBiz.class, gridPath);
			temporalBiz.getBizContext().getGridContextClient().setGridIds(gridId);
			temporalBiz.setGridPath(gridPath);
			rawFormat = false;
			if (commandLine.hasOption("put")) {
				runPut(temporalBiz, commandLine.getOptionValues("put"));
			} else if (commandLine.hasOption("putAttachments")) {
				runPutAttachments(temporalBiz, commandLine.getOptionValues("putAttachments"));
			} else if (commandLine.hasOption("get")) {
				runGet(temporalBiz, commandLine.getOptionValues("get"));
			} else if (commandLine.hasOption("getAttachments1")) {
				runGetAttachments1(temporalBiz, commandLine.getOptionValues("getAttachments1"));
			} else if (commandLine.hasOption("getAttachments2")) {
				runGetAttachments2(temporalBiz, commandLine.getOptionValues("getAttachments2"));
			} else if (commandLine.hasOption("getAttachments3")) {
				runGetAttachments3(temporalBiz, commandLine.getOptionValues("getAttachments3"));
			} else if (commandLine.hasOption("remove")) {
				runRemove(temporalBiz, commandLine.getOptionValues("remove"));
			} else if (commandLine.hasOption("history")) {
				if (commandLine.hasOption("rawformat")) {
					rawFormat = true;
				} else {
					rawFormat = false;
				}
				runHistory(temporalBiz, commandLine.getOptionValue("history"));
			} else {
				boolean isRefresh = commandLine.hasOption("-refresh");
				IScrollableResultSet srs = temporalBiz.getEntryResultSet(System.currentTimeMillis(), null, true, padoShell.getFetchSize(), isRefresh);
				srs.setFetchSize(padoShell.getFetchSize());
				ResultSetDisplay.display(srs);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void runGet(ITemporalBiz temporalBiz, String[] values) throws Exception
	{
		long validAtTime = -1;
		long asOfTime = -1;
		Object result;

		if (values == null) {
			PadoShell.printlnError(this, "get: Invalid number of arguments.");
			return;
		} else if (values.length == 1) {
			identityKeyStr = values[0];
		} else if (values.length == 2) {
			identityKeyStr = values[0];
			validAtTime = padoShell.getDate(values[1]).getTime();
		} else if (values.length == 3) {
			identityKeyStr = values[0];
			validAtTime = padoShell.getDate(values[1]).getTime();
			asOfTime = padoShell.getDate(values[2]).getTime();
		}

		if (objectPattern.matcher(identityKeyStr).matches()) {
			identityKey = generateObject(identityKeyStr);
		} else {
			PadoShell.printlnError(this, "get: Identity key is not specified in proper format.");
			return;
		}

		if (asOfTime == -1) {
			if (validAtTime != -1) {
				result = temporalBiz.get(identityKey, validAtTime);
			} else {
				result = temporalBiz.get(identityKey);
			}
		} else {
			result = temporalBiz.get(identityKey, validAtTime, asOfTime);
		}
		if (result == null) {
			PadoShell.println("Not found.");
		} else {
			TemporalPrintUtil.printObject(result);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void runGetAttachments1(ITemporalBiz temporalBiz, String[] values) throws Exception
	{
		long validAtTime = -1;
		long asOfTime = -1;
		AttachmentResults<Object> attResult = null;

		if (values == null) {
			PadoShell.printlnError("getAttachments1: Invalid number of arguments.");
			return;
		} else if (values.length == 1) {
			identityKeyStr = values[0];
		} else if (values.length == 2) {
			identityKeyStr = values[0];
			validAtTime = padoShell.getDate(values[1]).getTime();
		} else if (values.length == 3) {
			identityKeyStr = values[0];
			validAtTime = padoShell.getDate(values[1]).getTime();
			asOfTime = padoShell.getDate(values[2]).getTime();
		} else {
			PadoShell.printlnError(this, "getAttachments1: Invalid number of arguments.");
			return;
		}

		if (objectPattern.matcher(identityKeyStr).matches()) {
			identityKey = generateObject(identityKeyStr);
			if (asOfTime == -1) {
				if (validAtTime != -1) {
					attResult = temporalBiz.getAttachments(identityKey, validAtTime);
				} else {
					attResult = temporalBiz.getAttachments(identityKey);
				}
			} else {
				attResult = temporalBiz.getAttachments(identityKey, validAtTime, asOfTime);
			}
			if (attResult == null) {
				PadoShell.println("Not found.");
			} else {
				TemporalPrintUtil.printAttachments(attResult.getAttachmentValues(), GridUtil.getFullPath(temporalBiz.getGridPath()), attResult.getValue());
			}
		} else {
			PadoShell.printlnError(this, "getAttachment1: Identity key is not specified in proper format.");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void runGetAttachments2(ITemporalBiz temporalBiz, String[] values) throws Exception
	{
		long validAtTime = -1;
		long asOfTime = -1;
		String path;
		List<Object> resultList = null;

		if (values == null) {
			PadoShell.printlnError(this, "getAttachment2: Invalid number of arguments.");
			return;

		} else if (values.length == 2) {
			identityKeyStr = values[0];
			path = values[1];
		} else if (values.length == 3) {
			identityKeyStr = values[0];
			path = values[1];
			validAtTime = padoShell.getDate(values[2]).getTime();
		} else if (values.length == 4) {
			identityKeyStr = values[0];
			path = values[1];
			validAtTime = padoShell.getDate(values[2]).getTime();
			asOfTime = padoShell.getDate(values[3]).getTime();
		} else {
			PadoShell.printlnError(this, "getAttachment2: Invalid number of arguments.");
			return;
		}

		String gridPath = null;
		String fullPath = null;
		if (path != null) {
			if (path.equalsIgnoreCase("null")) {
				path = padoShell.getCurrentPath();
			}
			fullPath = padoShell.getFullPath(path);
			gridPath = GridUtil.getChildPath(fullPath);
		}
		
		AttachmentSetFactory factory = new AttachmentSetFactory();
		if (objectPattern.matcher(identityKeyStr).matches()) {
			Set<Object> identityKeys = new HashSet<Object>();
			AttachmentSet as = factory.createAttachmentSet(identityKeys);
			identityKeys = generateObjects(identityKeyStr);	
			as.setGridPath(gridPath);
			if (asOfTime == -1) {
				if (validAtTime != -1) {
					resultList = temporalBiz.getAttachments(as, validAtTime);
				} else {
					resultList = temporalBiz.getAttachments(as);
				}
			} else {
				resultList = temporalBiz.getAttachments(as, validAtTime, asOfTime);
			}
			if (resultList != null && resultList.size() == 0) {
				PadoShell.println("Not found.");
			} else {
				TemporalPrintUtil.printAttachments(resultList, path);
			}
		} else {
			PadoShell.printlnError(this, "getAttachment2: IdentityKey is not specified in proper format.");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void runGetAttachments3(ITemporalBiz temporalBiz, String[] values) throws Exception
	{
		long validAtTime = -1;
		long asOfTime = -1;
		AttachmentSet[] attSets = null;
		List<Object>[] resultList = null;

		if (values == null) {
			PadoShell.printlnError(this, "getAttachments3: Invalid no. of arguments.");
			return;
		} else if (values.length == 1) {
			identityKeyStr = values[0];
		} else if (values.length == 2) {
			identityKeyStr = values[0];
			validAtTime = padoShell.getDate(values[1]).getTime();
		} else if (values.length == 3) {
			identityKeyStr = values[0];
			validAtTime = padoShell.getDate(values[1]).getTime();
			asOfTime = padoShell.getDate(values[2]).getTime();
		} else {
			PadoShell.printlnError(this, "getAttachments3: Invalid no. of arguments.");
			return;
		}

		if (objectPattern.matcher(identityKeyStr).matches()) {
			attSets = generateAttachmentSets(identityKeyStr);
			if (asOfTime == -1) {
				if (validAtTime != -1) {
					resultList = temporalBiz.getAttachments(attSets, validAtTime);
				} else {
					resultList = temporalBiz.getAttachments(attSets);
				}
			} else {
				resultList = temporalBiz.getAttachments(attSets, validAtTime, asOfTime);
			}
			if (resultList != null && resultList.length == 0) {
				PadoShell.printlnError(this, "getAttachments3: Not found.");
			} else {
				if (resultList != null) {
					for (List<Object> list : resultList) {
						TemporalPrintUtil.printAttachments(list, "");
					}
				}
			}
		} else {
			PadoShell.printlnError(this, "getAttachments3: IdentityKey is not specified in proper format.");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void runHistory(ITemporalBiz temporalBiz, String commandLineArg) throws Exception
	{
		Scanner scanner = new Scanner(commandLineArg);
		if (scanner.hasNext()) {
			identityKeyStr = scanner.next();
		} else {
			PadoShell.printlnError(this, "history: IdentityKey not specified.");
			return;
		}

		if (objectPattern.matcher(identityKeyStr).matches()) {
			identityKey = generateObject(identityKeyStr);
		} else {
			PadoShell.printlnError(this, "history: IdentityKey is not specified in proper format.");
			return;
		}

		identityKey = generateObject(identityKeyStr);

		TemporalDataList tdl = temporalBiz.getTemporalAdminBiz().getTemporalDataList(identityKey);
		if (tdl == null) {
			PadoShell.printlnError(this, "history: Identity key not found.");
			return;
		}
		TemporalPrintUtil.dump(tdl, rawFormat);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void runPut(ITemporalBiz temporalBiz, String[] values) throws Exception
	{
		if (!((values.length == 2) || (values.length == 3) || (values.length == 6))) {
			PadoShell.printlnError(this, "put: Invalid number of arguments.");
			return;
		} else if (values.length == 2) {
			identityKeyStr = values[0];
			valueStr = values[1];
			startValidTime = writtenTime = System.currentTimeMillis();
			endValidTime = Long.MAX_VALUE;
			isDelta = false;
		} else if (values.length == 3) {
			identityKeyStr = values[0];
			valueStr = values[1];
			startValidTime = writtenTime = System.currentTimeMillis();
			endValidTime = Long.MAX_VALUE;
			isDelta = Boolean.valueOf(values[2]);
		} else if (values.length == 6) {
			identityKeyStr = values[0];
			valueStr = values[1];
			startValidTime = padoShell.getDate(values[2]).getTime();
			endValidTime = padoShell.getDate(values[3]).getTime();
			writtenTime = padoShell.getDate(values[4]).getTime();
			isDelta = Boolean.valueOf(values[5]);
		}

		if (objectPattern.matcher(identityKeyStr).matches()) {
			identityKey = generateObject(identityKeyStr);
		} else {
			PadoShell.printlnError(this, "put: Identity key is not specified in proper format.");
			return;
		}

		if (objectPattern.matcher(valueStr).matches()) {
			value = generateObject(valueStr);
		} else {
			PadoShell.printlnError(this, "put: Value is not specified in proper format.");
			return;
		}

		temporalBiz.put(identityKey, value, startValidTime, endValidTime, writtenTime, isDelta);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void runPutAttachments(ITemporalBiz temporalBiz, String[] values) throws Exception
	{
		if (!((values.length == 3) || (values.length == 4) || (values.length == 7))) {
			PadoShell.printlnError(this, "putAttachments: Invalid no. of arguments.");
			return;
		} else if (values.length == 3) {
			identityKeyStr = values[0];
			valueStr = values[1];
			attachmentsStr = values[2];
			startValidTime = writtenTime = System.currentTimeMillis();
			endValidTime = Long.MAX_VALUE;
			isDelta = false;
		} else if (values.length == 4) {
			identityKeyStr = values[0];
			valueStr = values[1];
			attachmentsStr = values[2];
			startValidTime = writtenTime = System.currentTimeMillis();
			endValidTime = Long.MAX_VALUE;
			isDelta = Boolean.valueOf(values[3]);
		} else if (values.length == 7) {
			identityKeyStr = values[0];
			valueStr = values[1];
			attachmentsStr = values[2];
			startValidTime = padoShell.getDate(values[3]).getTime();
			endValidTime = padoShell.getDate(values[4]).getTime();
			writtenTime = padoShell.getDate(values[5]).getTime();
			isDelta = Boolean.valueOf(values[6]);
		}

		if (objectPattern.matcher(identityKeyStr).matches()) {
			identityKey = generateObject(identityKeyStr);
		} else {
			PadoShell.printlnError(this, "putAttachments: IdentityKey is not specified in proper format.");
			return;
		}

		if (objectPattern.matcher(valueStr).matches()) {
			value = generateObject(valueStr);
		} else {
			PadoShell.printlnError(this, "putAttachments: Value is not specified in proper format.");
			return;
		}

		AttachmentSet[] attachments = generateAttachmentSets(attachmentsStr);
		TemporalPrintUtil.printTemporalObject(temporalBiz.putAttachments(identityKey, value, attachments, startValidTime,
				endValidTime, writtenTime, isDelta));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void runRemove(ITemporalBiz temporalBiz, String[] values) throws Exception
	{
		if (values.length < 1) {
			PadoShell.printlnError(this, "remove: Invalid number of arguments.");
			return;
		} else if (values.length == 1) {
			identityKeyStr = values[0];
			writtenTime = System.currentTimeMillis();
		} else if (values.length == 2) {
			identityKeyStr = values[0];
			writtenTime = padoShell.getDate(values[1]).getTime();
		}

		if (objectPattern.matcher(identityKeyStr).matches()) {
			identityKey = generateObject(identityKeyStr);
		} else {
			PadoShell.printlnError(this, "remove: Identity key is not specified in proper format.");
			return;
		}

		temporalBiz.remove(identityKey);
	}

	private Object generateObject(String str) throws Exception
	{
		Object obj = null;

		String[] args = str.substring(0, str.length() - 1).split("\\[");
		String className = args[0];

		if (className.equals("java.lang.String") || className.equalsIgnoreCase("String")) {
			obj = args[1];
		} else if (className.equals("java.lang.Integer") || className.equalsIgnoreCase("Integer") || className.equalsIgnoreCase("int")) {
			obj = Integer.parseInt(args[1]);
		} else if (className.equals("java.lang.Long") || className.equalsIgnoreCase("Long")) {
			obj = Long.parseLong(args[1]);
		} else if (className.equals("java.lang.Float") || className.equalsIgnoreCase("Float")) {
			obj = Float.parseFloat(args[1]);
		} else if (className.equals("java.lang.Double") || className.equalsIgnoreCase("Double")) {
			obj = Double.parseDouble(args[1]);
		}
		
		if (obj != null) {
			return obj;
		}

		Map<String, String> fieldsMap = new HashMap<String, String>();

		// String parameters[] = args[1].split("\\|");
		String parameters[] = args[1].split(" and ");
		for (String s : parameters) {
			String[] arg = s.split("=");
			fieldsMap.put(arg[0].trim(), arg[1].trim());
		}
		return ObjectUtil.generateObject(className, fieldsMap, padoShell);
	}

	private Set<Object> generateObjects(String str) throws Exception
	{
		String[] args = str.substring(0, str.length() - 1).split("\\[");
		String className = args[0];
		String[] keysStr = args[1].split(",");
		Set<Object> keys = new HashSet<Object>();

		for (String keyStr : keysStr) {

			if (className.startsWith("java")) {
				if (className.equals("java.lang.String")) {
					keys.add(keyStr);
				} else if (className.equals("java.lang.Integer")) {
					keys.add(Integer.parseInt(keyStr));
				} else if (className.equals("java.lang.Long")) {
					keys.add(Long.parseLong(keyStr));
				} else if (className.equals("java.lang.Float")) {
					keys.add(Float.parseFloat(keyStr));
				} else if (className.equals("java.lang.Double")) {
					keys.add(Double.parseDouble(keyStr));
				}
			} else {
				Map<String, String> fieldsMap = new HashMap<String, String>();

				// String parameters[] = keyStr.split("\\|");
				String parameters[] = keyStr.split(" and ");
				for (String s : parameters) {
					String[] arg = s.split("=");
					fieldsMap.put(arg[0], arg[1]);
				}
				keys.add(ObjectUtil.generateObject(className, fieldsMap, padoShell));
			}
		}
		return keys;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private AttachmentSet[] generateAttachmentSets(String str) throws Exception
	{
		String[] args = str.substring(0, str.length() - 1).split("\\[");
		String className = args[0];
		String[] attachments = args[1].split(",");

		AttachmentSet[] result = new AttachmentSet[attachments.length];

		Set<Object> idenKeySet = null;
		int i = 0;
		AttachmentSetFactory factory = new AttachmentSetFactory();
		for (String attachment : attachments) {
			idenKeySet = new HashSet<Object>();
			String[] args1 = attachment.split("-");
			// TODO: Take care of name.
			String name = "attachments";
			String fullPath = args1[0];
			String gridPath = GridUtil.getChildPath(fullPath);
			String[] keys = args1[1].split(":");

			for (String key : keys) {
				if (className.startsWith("java")) {
					if (className.equals("java.lang.String")) {
						idenKeySet.add(key);
					} else if (className.equals("java.lang.Integer")) {
						idenKeySet.add(Integer.parseInt(key));
					} else if (className.equals("java.lang.Long")) {
						idenKeySet.add(Long.parseLong(key));
					} else if (className.equals("java.lang.Float")) {
						idenKeySet.add(Float.parseFloat(key));
					} else if (className.equals("java.lang.Double")) {
						idenKeySet.add(Double.parseDouble(key));
					}
				} else {
					idenKeySet.add(generateObject(className + "[" + key + "]"));
				}
			}
			result[i] = factory.createAttachmentSet(name, idenKeySet, gridPath);
			i++;
		}
		return result;
	}
	
	/**
	 * Builds Lucene indexes.
	 * 
	 * @param gridIds
	 *            null or empty to build indexes for all grids.
	 * @param fullPaths
	 *            null or empty to build all paths.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void runLucene(String gridIds[], String... fullPaths)
	{
		
		if (fullPaths == null || fullPaths.length == 0) {
			if (gridIds == null || gridIds.length == 0) {
				// This code is not reachable by the 'temporal' command.
				// Grid IDs are always specified.
				PadoShell.println("Building Lucene indexes for ALL temporal paths for ALL grids... Please wait.");
				ILuceneBiz luceneBiz = (ILuceneBiz) SharedCache.getSharedCache().getPado().getCatalog().newInstance(ILuceneBiz.class);
				gridIds = SharedCache.getSharedCache().getPado().getCatalog().getGridIds();
				for (String gridId : gridIds) {
					PadoShell.println("   " + gridId + ": <all paths>");
				}
				luceneBiz.buildAllIndexes();
			} else {
				PadoShell.println("Building Lucene indexes for ALL temporal paths... Please wait.");
				ExecutorService es = Executors.newFixedThreadPool(gridIds.length);
				Future futures[] = new Future[gridIds.length];
				int i = 0;
				for (final String gridId : gridIds) {
					PadoShell.println("   " + gridId + ": <all paths>");
					futures[i++] = es.submit(new Callable() {
	
						@Override
						public Object call() throws Exception
						{
							ILuceneBiz luceneBiz = (ILuceneBiz) SharedCache.getSharedCache().getPado().getCatalog().newInstance(ILuceneBiz.class);
							luceneBiz.buildAllPathIndexes(gridId);
							return true;
						}
						
					});
				}
				for (Future future : futures) {
					try {
						future.get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			PadoShell.println("Building Lucene indexes for the specified temporal paths... Please wait.");
			ILuceneBiz luceneBiz = (ILuceneBiz) SharedCache.getSharedCache().getPado().getCatalog().newInstance(ILuceneBiz.class);
			Map<String, List<String>> map = PadoShellUtil.getGridPathMap(padoShell, fullPaths);
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				luceneBiz.buildIndexes(entry.getKey(), entry.getValue().toArray(new String[0]));
			}
		}
		PadoShell.println("Lucene indexing complete.");
	}
	
	@SuppressWarnings("rawtypes")
	private void setTemporalEnabled(boolean enabled, String gridIds[], String... fullPaths)
	{
		ITemporalAdminBiz temporalAdminBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(ITemporalAdminBiz.class);
		String text = enabled ? "Enabling" : "Disabling";
		if (fullPaths == null || fullPaths.length == 0) {
			if (gridIds == null || gridIds.length == 0) {
				// This code is not reachable by the 'temporal' command.
				// Grid IDs are always specified.
				PadoShell.println(text + " temporal data for ALL temporal paths for ALL grids... Please wait.");
				gridIds = SharedCache.getSharedCache().getPado().getCatalog().getGridIds();
			} else {
				PadoShell.println(text + " temporal data for ALL temporal paths... Please wait.");
			}
			for (String gridId : gridIds) {
				PadoShell.println("   " + gridId + ": <all paths>");
			}
			for (String gridId : gridIds) {
				temporalAdminBiz.getBizContext().getGridContextClient().setGridIds(gridId);
				temporalAdminBiz.setEnabledAll(enabled, false /* spawnThread */);
			}
		} else {
			PadoShell.println(text + " temporal data for specified temporal paths... Please wait.");
			Map<String, List<String>> map = PadoShellUtil.getGridPathMap(padoShell, fullPaths);
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				temporalAdminBiz.getBizContext().getGridContextClient().setGridIds(entry.getKey());
				for (String gridPath : entry.getValue()) {
					temporalAdminBiz.setGridPath(gridPath);
					temporalAdminBiz.setEnabled(enabled, false /* spawnThread */);
				}
			}
		}
	}
}
