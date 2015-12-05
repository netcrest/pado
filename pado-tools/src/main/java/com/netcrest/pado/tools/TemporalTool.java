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
package com.netcrest.pado.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ILuceneBiz;
import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.biz.ITemporalAdminBiz;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.biz.IUtilBiz;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.temporal.TemporalDataList;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalTool
{
	private IPado pado;
	private ICatalog catalog;
	private Map<String, ITemporalBiz> temporalBizMap = new HashMap(3);
	DecimalFormat decimalFormat = new DecimalFormat("#,###");

	// jline reader
	private ConsoleReader consoleReader;
	private boolean echo;

	private BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
	private String currentPath = "temporal";

	enum Type
	{
		BUILD_LUCENE, DUMP_SERVERS, INTERACTIVE, APP, ENABLE, DISABLE, CREATE, CLEAR, REMOVE, LIST, IMPORT
	}

	public TemporalTool()
	{
	}

	private void login(String appId, String locators) throws PadoLoginException
	{
		Pado.connect(locators, true);
		pado = Pado.login();
		catalog = pado.getCatalog();
	}

	public void close()
	{
		Pado.close();
	}

	public void setTemporalEnabled(boolean enabled, String[] gridPaths)
	{
		writeLine();
		ITemporalAdminBiz temporalAdminBiz = (ITemporalAdminBiz) catalog.newInstance(ITemporalAdminBiz.class);
		long startTime;
		long delta;
		if (enabled) {
			if (gridPaths == null || gridPaths.length == 0) {
				writeLine("Enabling temporal data for ALL temporal paths... Please wait.");
				startTime = System.currentTimeMillis();
				// Block till done
				temporalAdminBiz.setEnabledAll(true, false /* spawnThread */);
				delta = System.currentTimeMillis() - startTime;
			} else {
				writeLine("Enabling temporal data for specified paths... Please wait.");
				startTime = System.currentTimeMillis();
				for (String gridPath : gridPaths) {
					temporalAdminBiz.setGridPath(gridPath);
					// Block till done
					temporalAdminBiz.setEnabled(true, false /* spawnThread */);
				}
				delta = System.currentTimeMillis() - startTime;
			}
		} else {
			if (gridPaths == null || gridPaths.length == 0) {
				writeLine("Disabling temporal data for ALL temporal paths... Please wait.");
				startTime = System.currentTimeMillis();
				// Block till done
				temporalAdminBiz.setEnabledAll(false, false /* spawnThread */);
				delta = System.currentTimeMillis() - startTime;
			} else {
				writeLine("Disabling temporal data for specified paths... Please wait.");
				startTime = System.currentTimeMillis();
				for (String gridPath : gridPaths) {
					temporalAdminBiz.setGridPath(gridPath);
					// Block till done
					temporalAdminBiz.setEnabled(false, false /* spawnThread */);
				}
				delta = System.currentTimeMillis() - startTime;
			}
		}
		writeLine("Elspsed time (sec): " + (delta / 1000));
		writeLine();
	}

	/**
	 * Builds Lucene indexes.
	 * 
	 * @param isAllGrids
	 *            true to build indexes for all grids, false to build indexes
	 *            for the default grid only.
	 * @param gridPaths
	 *            null or empty to build all paths.
	 */
	public void buildLucene(boolean isAllGrids, String... gridPaths)
	{
		writeLine();
		ILuceneBiz luceneBiz = (ILuceneBiz) catalog.newInstance(ILuceneBiz.class);
		long startTime = System.currentTimeMillis();
		if (gridPaths == null || gridPaths.length == 0) {
			if (isAllGrids) {
				writeLine("Building Lucene indexes for ALL temporal paths for ALL grids... Please wait.");
				luceneBiz.buildAllIndexes();
			} else {
				writeLine("Building Lucene indexes for ALL temporal paths for this grid only... Please wait.");
				luceneBiz.buildAllPathIndexes(luceneBiz.getBizContext().getGridService().getDefaultGridId());
			}
		} else {

			if (isAllGrids) {
				writeLine("Building Lucene indexes for the specified temporal paths for all grids... Please wait.");
				luceneBiz.buildAllGridIndexes(gridPaths);
			} else {
				writeLine("Building Lucene indexes for the specified temporal paths for this grid only... Please wait.");
				luceneBiz.buildIndexes(luceneBiz.getBizContext().getGridService().getDefaultGridId(), gridPaths);
			}
		}
		long elapsedTimeInSec = (System.currentTimeMillis() - startTime) / 1000;
		writeLine("Elspsed time (sec): " + decimalFormat.format(elapsedTimeInSec));
		writeLine("Lucene indexing complete.");
		writeLine();
	}

	/**
	 * Dumps temporal data in the server's dump dir.
	 * 
	 * @param gridPaths
	 *            null or empty to dump all paths.
	 */
	public void dumpServers(String... gridPaths)
	{
		writeLine();
		writeLine("Dumping grid paths in the server... Please wait. This may take some time.");
		writeLine();
		IUtilBiz utilBiz = catalog.newInstance(IUtilBiz.class);
		List<String> serverDirs;
		long startTime = System.currentTimeMillis();
		if (gridPaths == null || gridPaths.length == 0) {
			serverDirs = utilBiz.dumpAll();
		} else {
			serverDirs = utilBiz.dumpServers(gridPaths);
		}
		long elapsedTimeInSec = (System.currentTimeMillis() - startTime) / 1000;
		for (int i = 0; i < serverDirs.size(); i++) {
			writeLine("   " + (i + 1) + ". " + serverDirs.get(i));
		}
		writeLine("Elapsed time (sec): " + decimalFormat.format(elapsedTimeInSec));
		writeLine("File dump complete.");
		writeLine();
	}
	
	/**
	 * Imports the latest files found in the server's dump dir.
	 * @param gridPaths null or empty to import all paths
	 */
	public void importServers(String... gridPaths)
	{
		writeLine();
		writeLine("Importing data from the latest dump files... Please wait. This may take some time.");
		writeLine();
		IUtilBiz utilBiz = catalog.newInstance(IUtilBiz.class);
		List<String> serverDirs;
		long startTime = System.currentTimeMillis();
		if (gridPaths == null || gridPaths.length == 0) {
			serverDirs = utilBiz.importAll();
		} else {
			serverDirs = utilBiz.importServers(true, gridPaths);
		}
		long elapsedTimeInSec = (System.currentTimeMillis() - startTime) / 1000;
		for (int i = 0; i < serverDirs.size(); i++) {
			writeLine("   " + (i + 1) + ". " + serverDirs.get(i));
		}
		writeLine("Elapsed time (sec): " + decimalFormat.format(elapsedTimeInSec));
		writeLine("File import complete.");
		writeLine();
	}
	
	public void createPaths(String gridId, String[] gridPaths, IPathBiz.PathType pathType,
			String colocatedWithGridPath, int redundantCopies, int totalBucketCount)
	{
		if (gridPaths == null || gridPaths.length == 0) {
			return;
		}
		IPathBiz pathBiz = pado.getCatalog().newInstance(IPathBiz.class);
		if (gridId == null) {
			gridId = pathBiz.getBizContext().getGridService().getDefaultGridId();
		}
		for (String gridPath : gridPaths) {
			boolean created = pathBiz.createPath(gridId, gridPath, pathType, false);
			if (created) {
				writeLine(gridId + "//" + gridPath + " created");
			} else {
				writeLine(gridId + "//" + gridPath + " failed");
			}
		}
		writeLine();
	}

	public void clearTemporalPaths(String gridId, String[] gridPaths) throws IOException
	{
		if (gridPaths == null || gridPaths.length == 0) {
			return;
		}
		IPathBiz pathBiz = pado.getCatalog().newInstance(IPathBiz.class);
		if (gridId == null) {
			gridId = pathBiz.getBizContext().getGridService().getDefaultGridId();
		}
		writeLine("CLEAR: The following grid paths will be cleared. Type 'continue' to confirm or any other keys to quit.");
		for (int i = 0; i < gridPaths.length; i++) {
			writeLine((i + 1) + ". " + gridId + "//" + gridPaths[i]);
		}
		String input = getLine(gridId + ">");
		if (input.equalsIgnoreCase("continue") == false) {
			writeLine("'clear' command aborted.");
			writeLine();
			return;
		}
		writeLine("CLEAR in progress. This may take some time to complete. Please wait.");
		for (String gridPath : gridPaths) {
			pathBiz.clear(gridId, gridPath, true);
		}
		writeLine("Temporal paths cleared.");
		writeLine();
	}

	public void removeTemporalPaths(String gridId, String[] gridPaths) throws IOException
	{
		if (gridPaths == null || gridPaths.length == 0) {
			return;
		}
		IPathBiz pathBiz = pado.getCatalog().newInstance(IPathBiz.class);
		if (gridId == null) {
			gridId = pathBiz.getBizContext().getGridService().getDefaultGridId();
		}
		writeLine("REMOVE: The following grid paths will be removed. Type 'continue' to confirm or any other keys to quit.");
		for (int i = 0; i < gridPaths.length; i++) {
			writeLine((i + 1) + ". " + gridId + "//" + gridPaths[i]);
		}
		String input = getLine(gridId + ">");
		if (input.equalsIgnoreCase("continue") == false) {
			writeLine("'remove' command aborted.");
			writeLine();
			return;
		}
		writeLine("REMOVE in progress. Please wait.");
		for (String gridPath : gridPaths) {
			pathBiz.remove(gridId, gridPath, true);
		}
		writeLine("Temporal paths removed.");
		writeLine();
	}

	public void listTemporalPaths(String gridId)
	{
		IPathBiz pathBiz = pado.getCatalog().newInstance(IPathBiz.class);
		List<PathInfo> gridPathList = pathBiz.getAllTemporalPathInfoList(gridId);
		Collections.sort(gridPathList);
		writeLine("Temporal paths defined in the grid: ");
		if (gridId == null) {
			gridId = pathBiz.getBizContext().getGridService().getDefaultGridId();
		}
		writeLine("Grid ID: " + gridId);
		if (gridPathList.size() == 0) {
			writeLine("   No temporal paths found.");
		} else {
			for (int i = 0; i < gridPathList.size(); i++) {
				PathInfo pathInfo = gridPathList.get(i);
				writeLine("   " + (i + 1) + ". " + pathInfo.getFullPath() + " (" + pathInfo.getSize() + ")");
			}
		}
		writeLine();
	}

	private void runPut(ITemporalBiz<String, Object> temporalBiz, String identityKey, long startValidTime,
			long endValidTime, long writtenTime, String value)
	{
		writeLine("Put:  " + identityKey + "  " + startValidTime + "  " + endValidTime + "  " + writtenTime + "  "
				+ value);
		temporalBiz.put(identityKey, value, startValidTime, endValidTime, writtenTime, false);
		temporalBiz.dump(identityKey);
	}

	private void runGet(ITemporalBiz<String, Object> temporalBiz, String identityKey, long validAtTime, long asOfTime)
	{
		Object value;
		if (asOfTime == -1) {
			if (validAtTime != -1) {
				value = temporalBiz.get(identityKey, validAtTime);
			} else {
				value = temporalBiz.get(identityKey);
			}
		} else {
			value = temporalBiz.get(identityKey, validAtTime, asOfTime);
		}
		writeLine();
		if (value == null) {
			writeLine("Not found.");
		} else {
			writeLine("Entity: " + identityKey + ", " + value);
		}
		writeLine();
	}

	private void dump(ITemporalBiz<String, Object> temporalBiz, String identityKey)
	{
		writeLine();
		writeLine("TemporalList: " + identityKey);
		writeLine();

		TemporalDataList tdl = temporalBiz.getTemporalAdminBiz().getTemporalDataList(identityKey);
		if (tdl == null) {
			writeLine("Not found");
		} else {
			tdl.dump();
		}
		writeLine();
	}

	private ITemporalBiz getTemporalBiz(String gridPath)
	{
		ITemporalBiz temporalBiz = temporalBizMap.get(gridPath);
		if (temporalBiz == null) {
			temporalBiz = catalog.newInstance(ITemporalBiz.class, gridPath);
			temporalBizMap.put(gridPath, temporalBiz);
		}
		return temporalBiz;
	}

	private void printCommands()
	{
		writeLine("Commands:");
		writeLine("   put <identity key> <value> [<start-valid-time> [<end-valid-time> [<written-time>]]]");
		writeLine("   get <identity key> [<valid-at> [<as-of>]");
		writeLine("   remove <identityKey>");
		writeLine("   dump <identity key>");
		writeLine("   path <grid path>");
		writeLine("   quit or q");
		writeLine("   ? or help");
	}

	public void interact(String gridPath) throws Exception
	{
		initJline();

		ITemporalBiz temporalBiz = getTemporalBiz(gridPath);

		currentPath = gridPath;
		writeLine();
		printCommands();
		writeLine();
		String line;
		String identityKey;
		long validAt;
		long asOf;
		do {
			line = getLine(null);
			String args[] = line.split(" ");
			if (args.length == 0) {
				continue;
			}
			String arg = args[0];
			if (arg.equalsIgnoreCase("get")) {
				if (args.length > 1) {
					identityKey = args[1];
					validAt = -1;
					asOf = -1;
					if (args.length > 2) {
						validAt = Long.parseLong(args[2]);
					}
					if (args.length > 3) {
						asOf = Long.parseLong(args[3]);
					}
					runGet(temporalBiz, identityKey, validAt, asOf);
				} else {
					writeLine("Usage: get <identity key> [<valid-at> [<as-of>]]");
				}
			} else if (arg.equalsIgnoreCase("put")) {
				if (args.length > 2) {
					identityKey = args[1];
					long startValidTime = System.currentTimeMillis();
					long endValidTime = Long.MAX_VALUE;
					long writtenTime = System.currentTimeMillis();
					String value = args[2];
					if (args.length > 3) {
						try {
							startValidTime = Long.parseLong(args[3]);
						} catch (Exception ex) {
							writeLine("Invalid input (start-valid-time): " + ex.getMessage());
						}
						startValidTime = getTimeValue(startValidTime);
					}
					if (args.length > 4) {
						try {
							endValidTime = Long.parseLong(args[4]);
						} catch (Exception ex) {
							writeLine("Invalid input (end-valid-time): " + ex.getMessage());
						}
						if (endValidTime < 0) {
							endValidTime = Long.MAX_VALUE;
						}
					}
					if (args.length > 5) {
						try {
							writtenTime = Long.parseLong(args[5]);
						} catch (Exception ex) {
							writeLine("Invalid input (writte-time): " + ex.getMessage());
						}
						if (writtenTime < 0) {
							writtenTime = Long.MAX_VALUE;
						}
					}
					runPut(temporalBiz, identityKey, startValidTime, endValidTime, writtenTime, value);
				} else {
					writeLine("Usage: put <identity key> <value> [<start-valid-time> [<end-valid-time> [<written-time>]]]");
				}

			} else if (arg.equalsIgnoreCase("remove")) {
				if (args.length > 1) {
					identityKey = args[1];
					temporalBiz.remove(identityKey);
					dump(temporalBiz, identityKey);
				} else {
					writeLine("Usage: remove <identity key>");
				}

			} else if (arg.equalsIgnoreCase("dump")) {
				if (args.length > 1) {
					identityKey = args[1];
					dump(temporalBiz, identityKey);
				} else {
					writeLine("Usage: dump <identity key>");
				}
			} else if (arg.equalsIgnoreCase("path")) {
				if (args.length > 1) {
					String path = args[1];
					try {
						temporalBiz = getTemporalBiz(path);
						currentPath = temporalBiz.getGridPath();
						writeLine("Grid path: " + temporalBiz.getGridPath());
					} catch (Exception ex) {
						writeLine("Invalid path: " + path + ". " + ex.getMessage());
					}
				} else {
					writeLine("Usage: path <grid-path>");
				}
			} else if (arg.equalsIgnoreCase("?") || arg.equalsIgnoreCase("help")) {
				writeLine();
				printCommands();
			} else if (arg.equalsIgnoreCase("q") || arg.equalsIgnoreCase("quit")) {
				break;
			} else {
				writeLine("Invalid command. Supported commands are 'put', 'get', 'dump', 'path', 'help', '?', and 'q(uit)'");
			}
			writeLine();

		} while (line.equalsIgnoreCase("q") == false);
		writeLine("Exit");
	}

	private static void writeLine()
	{
		System.out.println();
	}

	private static void writeLine(String line)
	{
		System.out.println(line);
	}

	private static void write(String str)
	{
		System.out.print(str);
	}

	/**
	 * Returns user inputs
	 * 
	 * @throws IOException
	 *             Thrown if it encounters an I/O error.
	 */
	private static String readLine() throws IOException
	{
		byte data[] = new byte[256];
		byte b;
		int offset = 0;
		while ((b = (byte) System.in.read()) != 0xa) {
			data[offset++] = b;
		}
		return new String(data, 0, offset).trim();
	}

	private long getTimeValue(long time)
	{
		if (time == -1) {
			return System.currentTimeMillis();
		} else if (time == -2) {
			return Long.MAX_VALUE;
		} else if (time < -2) {
			return 0;
		} else {
			return time;
		}
	}

	private void initJline() throws Exception
	{
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows")) {
			return;
		}

		// Character mask = null;
		// String trigger = null;
		if (System.console() != null) {
			consoleReader = new ConsoleReader();
			if (consoleReader != null) {
				consoleReader.setBellEnabled(false);

				List<Completer> completors = new LinkedList<Completer>();
				completors.add(new StringsCompleter("put", "get", "dump", "path", "quit", "q" ));
				consoleReader.addCompleter(new ArgumentCompleter(completors));
			}
		}
		// reader.setDebug(new PrintWriter(new FileWriter("writer.debug",
		// true)));
	}

	public String getLine(String prompt) throws IOException
	{
		boolean addAngle = prompt == null;
		if (prompt == null) {
			prompt = currentPath;
		}

		StringBuffer cmdBuffer = null;
		boolean keepGoing;
		String nextLine;
		do {
			keepGoing = false;
			if (consoleReader == null) {
				if (addAngle) {
					writeLine(prompt + "> ");
				} else {
					writeLine(prompt);
				}
				nextLine = bufferedReader.readLine();
			} else {
				if (addAngle) {
					nextLine = consoleReader.readLine(prompt + "> ");
				} else {
					nextLine = consoleReader.readLine(prompt);
				}
			}

			// if nextLine is null then we encountered EOF.
			// In that case, leave cmdBuffer null if it is still null

			if (this.echo) {
				if (nextLine == null) {
					writeLine("EOF");
				} else if (nextLine.length() != 0) {
					writeLine(nextLine);
				}
			}

			if (nextLine == null) {
				break;
			} else if (cmdBuffer == null) {
				cmdBuffer = new StringBuffer();
			}

			// if last character is a backward slash, replace backward slash
			// with LF and continue to next line
			if (nextLine.endsWith("\\")) {
				nextLine = nextLine.substring(0, nextLine.length() - 1);
				keepGoing = true;
			}
			cmdBuffer.append(nextLine);
			// if (keepGoing) {
			// cmdBuffer.append('\n');
			// }
		} while (keepGoing);

		if (this.echo)
			writeLine();

		return cmdBuffer == null ? null : cmdBuffer.toString();
	}

	private static void usage()
	{
		writeLine();
		writeLine("Usage:");
		writeLine("   TemporalTool [-buildlucene [-path <comma-separated grid paths> [-all]");
		writeLine("                [-clear [-path <comma-separated grid paths> [-grid <grid ID>]]]");
		writeLine("                [-create [-nontemporal] [-path <comma-separated grid paths> [-grid <grid ID>]]]");
		writeLine("                         [-type local | local_persistent | local_overflow | local_persistent_overflow |");
		writeLine("                                replicate | replicate_persistent | repliate_overflow |");
		writeLine("                                replicate_persistent_overflow | repliate_empty | partition | ");
		writeLine("                                partition_persistent | partition_persistent_overflow");
		writeLine("                         [-colocatedWith <grid path>]");
		writeLine("                         [-bucket <total bucket count>]");
		writeLine("                         [-redundantCopies <Number of redundant copies of path entries>]");
		writeLine("                [-dump [-path <comma-separated grid paths>]]");
		writeLine("                [-enable | -disable [-path <comma-separated grid paths>]]");
		writeLine("                [-interactive [-path <grid path>]");
		writeLine("                [-list [-grid <grid ID>]]");
		writeLine("                [-locators <host:port>]");
		writeLine("                [-remove [-path <comma-separated grid paths> [-grid <grid ID>]]]");
		writeLine("                [-?]");
		writeLine();
		writeLine("   TemporalTool is a uility tool for for building Lucene indexes, enabling or");
		writeLine("   disabling temporal data, dumping gridpath contents, and interactively");
		writeLine("   accessing temporal data.");
		writeLine();
		writeLine("   Default: TemporalTool -locators localhost:20000 -interactive");
		writeLine();
		writeLine("      -all          builds Lucene indexes in all grids including this grid's");
		writeLine("                    child grids.");
		writeLine("      -buildlucene  builds Lucene indexes for all or specified paths. If -all");
		writeLine("                    is not specified then it build indexes for this (default) grid only.");
		writeLine("      -clear        clears the specified paths");
		writeLine("      -create       creates specified paths in the specified grid ID. If the");
		writeLine("                    grid ID is not specified then it creates the paths");
		writeLine("                    in this (default) grid. It always creates temporal paths");
		writeLine("                    unless -nontemporal is specified.");
		writeLine("      -colocatedWith if specified then the specified grid paths are colocated");
		writeLine("                    with this grid path");
		writeLine("      -bucket       Total number of buckets. Default: 113");
		writeLine("      -type         Partitioned path type. Default: partition");
		writeLine("      -redundantCopies  Number of redudant copies of path entries. Default: 1");
		writeLine("      -dump         dumps the contents of all paths to files in the servers' dump dir");
		writeLine("      -disable      disables all or specified paths");
		writeLine("      -enable       enables all or specified paths");
		writeLine("      -list         lists all tempral paths defined in the grid");
		writeLine("      -removes      removes the specified paths");
		writeLine("      -interactive  runs in an interactive mode for executing 'put', 'get'");
		writeLine("                    and 'dump' commands on a grid path.");
		writeLine();
		System.exit(0);
	}

	public static void main(String[] args)
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		String locators = System.getProperty("pado.locators", "localhost:20000");
		String appId = "sys";

		Type type = Type.INTERACTIVE;
		String gridId = null;
		boolean isAll = false;
		String gridPaths[] = null;
		String gridPathStr = null;
		int redundantCopies = 1;
		int totalBucketCount = 113;
		IPathBiz.PathType pathType = IPathBiz.PathType.PARTITION;
		String colocatedWith = null;
		boolean isNonTemporal = false;

		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-id")) {
				if (i < args.length - 1) {
					gridId = args[++i].trim();
				}
			} else if (arg.equals("-path")) {
				if (i < args.length - 1) {
					gridPathStr = args[++i].trim();
				}
			} else if (arg.equals("-buildlucene")) {
				type = Type.BUILD_LUCENE;
			} else if (arg.equals("-all")) {
				isAll = true;
			} else if (arg.equals("-create")) {
				type = Type.CREATE;
			} else if (arg.equals("-type")) {
				if (i < args.length - 1) {
					String val = args[++i].trim();
					if (val.startsWith("-")) {
						continue;
					}
					pathType = IPathBiz.PathType.valueOf(val.toUpperCase());
					if (pathType == null) {
						System.err.println("Error. Invalid path type: " + val);
						System.exit(-1);
					}
				}
			} else if (arg.equals("-colocatedWith")) {
				if (i < args.length - 1) {
					String val = args[++i].trim();
					if (val.startsWith("-")) {
						continue;
					}
					colocatedWith = args[++i].trim();
				}
			} else if (arg.equals("-bucket")) {
				if (i < args.length - 1) {
					String val = args[++i].trim();
					if (val.startsWith("-")) {
						continue;
					}
					totalBucketCount = Integer.parseInt(val);
				}
			} else if (arg.equals("-redundantCopies")) {
				if (i < args.length - 1) {
					String val = args[++i].trim();
					if (val.startsWith("-")) {
						continue;
					}
					redundantCopies = Integer.parseInt(val);
				}
			} else if (arg.equals("-clear")) {
				type = Type.CLEAR;
			} else if (arg.equals("-remove")) {
				type = Type.REMOVE;
			} else if (arg.equals("-list")) {
				type = Type.LIST;
			} else if (arg.equals("-enable")) {
				type = Type.ENABLE;
			} else if (arg.equals("-disable")) {
				type = Type.DISABLE;
			} else if (arg.equals("-dump")) {
				type = Type.DUMP_SERVERS;
			} else if (arg.equals("-import")) {
				type = Type.IMPORT;
			} else if (arg.equals("-interactive")) {
				type = Type.INTERACTIVE;
			} else if (arg.equals("-locators")) {
				if (i < args.length - 1) {
					locators = args[++i];
				}
			}
		}

		// Grid paths
		if (gridPathStr != null) {
			String split[] = gridPathStr.split(",");
			gridPaths = new String[split.length];
			for (int i = 0; i < split.length; i++) {
				gridPaths[i] = split[i].trim();
			}
		}

		TemporalTool client = new TemporalTool();
		try {

			client.login(appId, locators);

			switch (type) {
			case BUILD_LUCENE:
				client.buildLucene(isAll, gridPaths);
				break;
			case DUMP_SERVERS:
				client.dumpServers(gridPaths);
				break;
			case IMPORT:
				client.importServers(gridPaths);
				break;
			case ENABLE:
				client.setTemporalEnabled(true, gridPaths);
				break;
			case DISABLE:
				client.setTemporalEnabled(false, gridPaths);
				break;
			case CREATE:
					client.createPaths(gridId, gridPaths, pathType, colocatedWith, redundantCopies, totalBucketCount);
				break;
			case CLEAR:
				client.clearTemporalPaths(gridId, gridPaths);
				break;
			case REMOVE:
				client.removeTemporalPaths(gridId, gridPaths);
				break;
			case LIST:
				client.listTemporalPaths(gridId);
				break;
			case INTERACTIVE:
			default:
				if (gridPaths == null || gridPaths.length == 0) {
					gridPaths = new String[] { "temporal" };
				}
				client.interact(gridPaths[0]);
				break;
			}

		} catch (Throwable th) {
			th.printStackTrace();
		} finally {
			client.close();
		}
	}
}
