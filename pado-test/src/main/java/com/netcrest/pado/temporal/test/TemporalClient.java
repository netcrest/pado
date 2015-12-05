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
package com.netcrest.pado.temporal.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ILuceneBiz;
import com.netcrest.pado.biz.ITemporalAdminBiz;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.biz.IUtilBiz;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.test.biz.ITemporalLoaderBizFuture;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TemporalClient
{	
	private IPado pado;
	private ICatalog catalog;
	private Map<String, ITemporalBiz> temporalBizMap = new HashMap(3);
	private ITemporalLoaderBizFuture temporalLoaderBizFuture;

	// jline reader
	private ConsoleReader consoleReader;
	private boolean echo;
	
	private BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
	private String currentPath = "temporal";

	enum Type
	{
		ALL, ACCOUNT, BANK, PORTFOLIO, POSITION, TRADE, BUILD_LUCENE, DUMP_SERVERS, INTERACTIVE, APP, BUILD_TEMPORAL
	}

	public TemporalClient()
	{
	}

	private void login(String locators, String appId, String domain, String username, char[] pass) throws PadoLoginException
	{
		Pado.connect(locators, true);
		pado = Pado.login(appId, domain, username, pass);
		catalog = pado.getCatalog();
	}

	public void close()
	{
		Pado.close();
	}

	public void loadAll(int entryCount, int batchSize, boolean isLoadDelta) throws Exception
	{
		TemporalLoader temporalLoader = new TemporalLoader();
		temporalLoader.bulkLoadAll(catalog, entryCount, batchSize, isLoadDelta);
	}
	
	public void loadPositions(String gridPath, int entryCount, int batchSize) throws Exception
	{
		if (gridPath == null) {
			gridPath = "position";
		}
		ITemporalBiz temporalBiz = getTemporalBiz(gridPath);
		TemporalLoader temporalLoader = new TemporalLoader();
		temporalLoader.bulkLoadPositions(temporalBiz, entryCount, batchSize, false);
	}
	
	public void loadAccounts(String gridPath, int entryCount, int batchSize) throws Exception
	{
		if (gridPath == null) {
			gridPath = "account";
		}
		ITemporalBiz temporalBiz = getTemporalBiz(gridPath);
		TemporalLoader temporalLoader = new TemporalLoader();
		temporalLoader.bulkLoadAccounts(temporalBiz, entryCount, batchSize, false);
	}
	
	public void loadBanks(String gridPath, int entryCount, int batchSize) throws Exception
	{
		if (gridPath == null) {
			gridPath = "bank";
		}
		ITemporalBiz temporalBiz = getTemporalBiz(gridPath);
		TemporalLoader temporalLoader = new TemporalLoader();
		temporalLoader.bulkLoadBanks(temporalBiz, entryCount, batchSize, false);
	}
	
	public void loadPortfolios(String gridPath, int entryCount, int batchSize) throws Exception
	{
		if (gridPath == null) {
			gridPath = "portfolio";
		}
		ITemporalBiz temporalBiz = getTemporalBiz(gridPath);
		TemporalLoader temporalLoader = new TemporalLoader();
		temporalLoader.bulkLoadPortfolios(temporalBiz, entryCount, batchSize, false);
	}
	
	public void loadTrades(String gridPath, int entryCount, int batchSize) throws Exception
	{
		if (gridPath == null) {
			gridPath = "trade";
		}
		writeLine();
		writeLine("Loading Trade objects. Please wait.");
		ITemporalLoaderBizFuture temporalLoaderBizFuture = getTemporalLoaderBiz();
		long startTime = System.currentTimeMillis();
		Future<List<String>> future = temporalLoaderBizFuture.loadTrades(gridPath, entryCount, batchSize);
		List<String> responses = future.get();
		long endTime = System.currentTimeMillis();
		long elapsedTimeInSec = (endTime - startTime) / 1000;
		Collections.sort(responses);
		int i = 0;
		for (String response : responses) {
			i++;
			System.out.println(i + ". " + response);
		}
		System.out.println("Trade objects loaded.");
		System.out.println("Elapsed time: " + elapsedTimeInSec);
		System.out.println();
	}
	
	public void buildTemporal()
	{
		writeLine();
		writeLine("Building temporal data for all temporal paths... Please wait.");
		ITemporalAdminBiz temporalAdminBiz = (ITemporalAdminBiz) catalog.newInstance(ITemporalAdminBiz.class);
		long startTime = System.currentTimeMillis();
		// Block till done
		temporalAdminBiz.setEnabledAll(true, false /* spawnThread */);
		long delta = System.currentTimeMillis() - startTime;
		System.out.println("Elspsed time (sec): " + (delta / 1000) );
		writeLine("Temporal data build complete.");
		writeLine();
	}
	
	public void buildLucene()
	{
		writeLine();
		writeLine("Building Lucene indexes for temporal paths... Please wait.");
		ILuceneBiz luceneBiz = (ILuceneBiz) catalog.newInstance(ILuceneBiz.class);
		long startTime = System.currentTimeMillis();
		luceneBiz.buildAllIndexes();
		long delta = System.currentTimeMillis() - startTime;
		System.out.println("Elspsed time (sec): " + (delta / 1000) );
		writeLine("Lucene indexing complete.");
		writeLine();
	}
	
	public void dumpServers()
	{
		System.out.println();
		System.out.println("Dumping all paths in the server...");
		System.out.println();
		IUtilBiz utilBiz = catalog.newInstance(IUtilBiz.class);
		List<String> serverDirs = utilBiz.dumpAll();
		for (int i = 0; i < serverDirs.size(); i++) {
			System.out.println("   " + (i+1) + ". " + serverDirs.get(i));
		}
		System.out.println("File dump complete.");
		System.out.println();
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
	
	private ITemporalLoaderBizFuture getTemporalLoaderBiz()
	{
		if (temporalLoaderBizFuture == null) {
			temporalLoaderBizFuture = catalog.newInstance(ITemporalLoaderBizFuture.class);
		}
		return temporalLoaderBizFuture;
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
		String validAtStr;
		String asOfStr;
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
				completors.add(new StringsCompleter( "put", "get", "dump", "path", "quit", "q"));
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
		writeLine("   TemporalClient [-gridpath <gridPath>] [-interactive] [-locators <host:port>] [-a <appId>] [-d <domain name>] [-u <user name>] [-p <password>]");
		writeLine("                  [-all|-account|-bank|-portfolio|-position [<entry count> [<batch size>]]]");
		writeLine("                  [-trade [<entry count> [<batch size]]]");
		writeLine("                  [-delta]");
		writeLine("                  [-dump]");
		writeLine("                  [-buildlucene]");
		writeLine("                  [-?]");
		writeLine();
		writeLine("   Default: TemporalClient -all -app sys -locators localhost:20000 ...");
		writeLine("   TemporalClient loads mocked data into the specified grid paths. This client is");
		writeLine("   made available specifically for \"mygrid\" and \"grid0-grid5\" examples.");
		writeLine("   mygrid, the -gridpath option is not required. For grid0-grid5, use the -gridpath");
		writeLine("   to specify the any of the paths defined in etc/<grid>/pado.xml. See examples below.");
		writeLine();
		writeLine("   Examples:");
		writeLine("       mygrid");
		writeLine("          -all 5000 1000       -- loads data to account, bank, portfolio, and position");
		writeLine("          -position 5000 1000  -- loads data to position only");
		writeLine("       grid0-grid5");
		writeLine("         -gridpath shared/portfolio -portfolio 5000 1000");
		writeLine("         -gridpath shared/position -position 5000 1000");
		writeLine();
		writeLine("      -all         loads data to all paths: account, bank, porfolio and position");
		writeLine("      -account     loads data to the account path");
		writeLine("      -bank        loads data to the bank path");
		writeLine("      -delta       loads deltas. This option is only for -all. No deltas by defalut.");
		writeLine("      -portfolio   loads data to the portfolio path");
		writeLine("      -position    loads data to the position path");
		writeLine();
		writeLine("      -trade       loads data to the trade path");
		writeLine();
//		writeLine("      -buildtemporal builds temporal data for all temporal paths");
//		writeLine("      -buildlucene builds Lucene index for all temporal paths");
//		writeLine("      -dump        Dumps the contents of all of grid paths to files in the servers' dump dir");
//		writeLine("      -interactive runs in an interactive mode for executing 'put', 'get'");
//		writeLine("                    and 'dump' commands on a grid path.");
//		writeLine();
		writeLine("   <entry count> is the number of entries to put in the cache.");
		writeLine("                 The default is 5000.");
		writeLine("   <batch size> is the number of entries per batch in the bulkload");
		writeLine("                buffer. The default is 1000.");
		writeLine();
		System.exit(0);
	}

	public static void main(String[] args)
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		String locators = System.getProperty("pado.locators", "localhost:20000");
		String appId = null;
		String domain = null;
		String username = null;
		String pass = null;

		Type type = Type.ALL;
		String gridPath = null;
		int entryCount = 5000;
		int batchSize = 1000;
		boolean isLoadDelta = false;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-gridpath")) {
				if (i < args.length - 1) {
					gridPath = args[++i];
				}
			} else if (arg.equals("-all")) {
				type = Type.ALL;
				if (i < args.length - 1) {
					entryCount = Integer.parseInt(args[++i]);
				}
				if (i < args.length - 1) {
					batchSize = Integer.parseInt(args[++i]);
				}
			} else if (arg.equals("-account")) {
				type = Type.ACCOUNT;
				if (i < args.length - 1) {
					entryCount = Integer.parseInt(args[++i]);
				}
				if (i < args.length - 1) {
					batchSize = Integer.parseInt(args[++i]);
				}
			} else if (arg.equals("-bank")) {
				type = Type.BANK;
				if (i < args.length - 1) {
					entryCount = Integer.parseInt(args[++i]);
				}
				if (i < args.length - 1) {
					batchSize = Integer.parseInt(args[++i]);
				}
			} else if (arg.equals("-delta")) {
				isLoadDelta = true;
			} else if (arg.equals("-portfolio")) {
				type = Type.PORTFOLIO;
				if (i < args.length - 1) {
					entryCount = Integer.parseInt(args[++i]);
				}
				if (i < args.length - 1) {
					batchSize = Integer.parseInt(args[++i]);
				}
			} else if (arg.equals("-position")) {
				type = Type.POSITION;
				if (i < args.length - 1) {
					entryCount = Integer.parseInt(args[++i]);
				}
				if (i < args.length - 1) {
					batchSize = Integer.parseInt(args[++i]);
				}
			} else if (arg.equals("-trade")) {
				type = Type.TRADE;
				if (i < args.length - 1) {
					entryCount = Integer.parseInt(args[++i]);
				}
				if (i < args.length - 1) {
					batchSize = Integer.parseInt(args[++i]);
				}
			} else if (arg.equals("-buildlucene")) {
				type = Type.BUILD_LUCENE;
			} else if (arg.equals("-buildtemporal")) {
				type = Type.BUILD_TEMPORAL;
			} else if (arg.equals("-dump")) {
				type = Type.DUMP_SERVERS;
			} else if (arg.equals("-interactive")) {
				type = Type.INTERACTIVE;
			} else if (arg.equals("-locators")) {
				if (i < args.length - 1) {
					locators = args[++i];
				}
			} else if (arg.equals("-a")) {
				if (i < args.length - 1) {
					appId = args[++i];
				}
			} else if (arg.equals("-d")) {
				if (i < args.length - 1) {
					domain = args[++i];
				}
			} else if (arg.equals("-u")) {
				if (i < args.length - 1) {
					username = args[++i];
				}
			} else if (arg.equals("-p")) {
				if (i < args.length - 1) {
					pass = args[++i];
				}
			}
		}

		TemporalClient client = new TemporalClient();
		try {

			client.login(locators, appId, domain, username, pass == null ? null : pass.toCharArray());

			switch (type) {
			case ALL:
				client.loadAll(entryCount, batchSize, isLoadDelta);
				break;
			case ACCOUNT:
				client.loadAccounts(gridPath, entryCount, batchSize);
				break;
			case POSITION:
				client.loadPositions(gridPath, entryCount, batchSize);
			case PORTFOLIO:
				client.loadPortfolios(gridPath, entryCount, batchSize);
				break;
			case TRADE:
				client.loadTrades("trade", entryCount, batchSize);
				break;
			case BUILD_LUCENE:
				client.buildLucene();
				break;
			case BUILD_TEMPORAL:
				client.buildTemporal();
				break;
			case DUMP_SERVERS:
				client.dumpServers();
				break;
			case INTERACTIVE:
				client.interact(gridPath);
				break;
			}

		} catch (Throwable th) {
			th.printStackTrace();
		} finally {
			client.close();
		}
	}
}
