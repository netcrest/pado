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
package com.netcrest.pado.tools.pado;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.netcrest.pado.PadoVersion;
import com.netcrest.pado.internal.util.ClassFinder;
import com.netcrest.pado.internal.util.StringUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.tools.pado.command.logout;
import com.netcrest.pado.tools.pado.util.ObjectUtil;
import com.netcrest.pado.tools.pado.util.PrintUtil;
import com.netcrest.pado.tools.pado.util.SimplePrintUtil;
import com.netcrest.pado.tools.pado.util.TimerUtil;
import com.netcrest.pado.util.GridUtil;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import jline.console.history.FileHistory;
import jline.console.history.MemoryHistory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class PadoShell
{
	public final static String PROPERTY_COMMAND_JAR_PATH = "pado.command.jar.path";

	public static final List<String> PROPERTIES_CMD_LIST = Arrays.asList(new String[] { "echo", "show", "debug" });

	public final static String PROPERTY_PLUGIN_JARS = "pado.plugin.jars";
	public final static String PROPERTY_PADO_INIT_FILE = "padoInitFile";

	public static final int DEFAULT_MAX_HIST_SIZE = 500;

	public static boolean DEBUG_ENABLED = Boolean.getBoolean("pado.debug");

	private String historyFileName = "pado_history";
	private String editorName = "vi";

	// Contains all of the supported commands (commandName, ICommand)
	private HashMap<String, ICommand> commandMap = new HashMap<String, ICommand>();
	private static ConsoleReader consoleReader;
	private BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

	private String[] commands;
	private ArrayList<String> commandArgList = new ArrayList<String>();
	private Properties envProperties = new Properties();
	private CommandLineParser parser = new BasicParser();

	private String startupDir = System.getProperty("pado.pwd");

	private String locators;
	private String appId;
	private String user;
	private char[] password;
	private String domain;
	private String jarDirectoryPath;
	private String inputFilePath;
	private String[] inputCommands;
	private String jarPaths;
	private String scriptFilePath;
	private boolean interactiveMode = true;
	private boolean ignorePadoRcFile = false;

	private boolean isEcho = false;
	private String currentPath = "/";
	private boolean historyPerSession = false;
	private boolean showTime = true;
	private boolean showResults = true;
	private boolean tableFormat = true;
	private int selectLimit = 1000;
	private int fetchSize = 100;
	private Class<?> keyClass;
	private Class<?> valueClass;
	private long zoneDifference = 0;
	private long readTimeout = 300000; // 5 sec

	private SimpleDateFormat dateFormat = new SimpleDateFormat();
	private boolean printType;
	private int collectionEntryPrintCount = 6;

	private TreeSet<String> bufferCommandSet = new TreeSet<String>();

	private List<BackgroundCommandRunner> backgroundCommandRunnerList;
	private ExecutorService backgroundThreadPool;

	private volatile boolean controlZPressed = false;

	public PadoShell(String args[]) throws Exception
	{
		// String fileSeparator = System.getProperty("file.separator");
		// String homeDir = System.getProperty("user.home");
		preInit(args);
		parseArgs(args);

		initCommands();
		initJline();

		postInit(args);

		// Shutdown Hook To Persist History To .pado_history file
		Thread historyShutdownHook = new Thread(new Runnable() {
			@Override
			public void run()
			{
				startTimer();
				writeHistoryToFile();
				stopTimer();
			}

		}, "Pado-PadoShell.HistoryShutdownWorker");

		// Registering Shutdown Hook
		Runtime.getRuntime().addShutdownHook(historyShutdownHook);

	}

	private void preInit(String args[])
	{
		PrintUtil.setTableFormat(tableFormat);
	}

	private void postInit(String args[])
	{
		String initFilePath = System.getProperty(PROPERTY_PADO_INIT_FILE, ".padorc");
		try {
			initFile(initFilePath);
		} catch (IOException ex) {
			printlnError("Reading file " + initFilePath + " -- " + getCauseMessage(ex));
			Logger.error(ex);
		}
	}

	private void parseArgs(String args[])
	{
		Options options = new Options();

		Option opt = OptionBuilder.create("dir");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("i");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("e");
		opt.setArgs(Option.UNLIMITED_VALUES);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("f");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("jar");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("l");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("a");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("u");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("p");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		// domain
		opt = OptionBuilder.create("d");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
		
		// history
		opt = OptionBuilder.create("h");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
		
		// editor (vi or emacs) - default vi
		opt = OptionBuilder.create("o");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);


		options.addOption("n", false, "");
		options.addOption("v", false, "");
		options.addOption("?", false, "");

		CommandLine commandLine = null;
		try {
			commandLine = cliParseCommandLine(options, args);
		} catch (Exception e) {
			Logger.error(e);
		}

		if (commandLine == null || commandLine.hasOption('?')) {
			usage();
			exit(0);
		}
		
		if (commandLine.hasOption('v')) {
			PadoVersion padoVersion = new PadoVersion();
			println("v" + padoVersion.getVersion());
			exit(0);
		}

		if (commandLine.hasOption("dir") && commandLine.getOptionValue("dir") != null) {
//			jarDirectoryPath = commandLine.getOptionValue("dir");
			// ignore dir. dir is handled by the shell script.
		}

		if (commandLine.hasOption("i") && commandLine.getOptionValue("i") != null) {
			inputFilePath = commandLine.getOptionValue("i");
		}

		if (commandLine.hasOption("e") && commandLine.getOptionValue("e") != null) {
			inputCommands = commandLine.getOptionValues("e");
		}
		if (commandLine.hasOption("f") && commandLine.getOptionValue("f") != null) {
			scriptFilePath = commandLine.getOptionValue("f");
		}

		if (commandLine.hasOption("jar") && commandLine.getOptionValue("jar") != null) {
			jarPaths = commandLine.getOptionValue("jar");
		}
		
		if (commandLine.hasOption("h") && commandLine.getOptionValue("h") != null) {
			historyFileName = commandLine.getOptionValue("h");
		}
		
		if (commandLine.hasOption("o") && commandLine.getOptionValue("o") != null) {
			editorName = commandLine.getOptionValue("o");
			// Only vi and emacs supported. Default to vi if a bad name.
			if (editorName.equalsIgnoreCase("vi") == false && editorName.equalsIgnoreCase("emacs")) {
				editorName = "vi";
			}
		}

		locators = commandLine.getOptionValue("l");
		appId = commandLine.getOptionValue("a");
		user = commandLine.getOptionValue("u");
		String pw = commandLine.getOptionValue("p");
		if (pw != null) {
			password = pw.toCharArray();
		}

		ignorePadoRcFile = commandLine.hasOption("n");

		if (commandLine.hasOption("h")) {
			setHistoryPerSession(Boolean.TRUE);
		}

		interactiveMode = scriptFilePath == null && inputCommands == null;

		if (interactiveMode) {
			println();
			println(PadoShellLogo.getPadoLogo());
			println(PadoShellLogo.getCopyrights());
			println();
		}

		envProperties.putAll(System.getenv());
	}

	/**
	 * Executes the arguments provided by the user during startup.
	 */
	private void executeArgs()
	{
		String loginCommand = null;
		if ((appId != null && user == null) || (appId == null && user != null)) {
			printlnError("Login failed. Both app ID (-a) and user name (-u) must be specified.");
		} else if (appId != null && user != null) {
			loginCommand = "login -a " + appId + " -u " + user;
			if (locators != null) {
				loginCommand += " -l " + locators;
			}
			if (password != null) {
				loginCommand += " -p " + String.valueOf(password);
			}
			if (domain != null) {
				loginCommand += " -d " + domain;
			}
			runCommand(loginCommand, false);
		}
	}

	@SuppressWarnings("rawtypes")
	private void initCommands() throws Exception
	{
		// Read all core command classes found in the package
		// com.netcrest.pado.tools.pado.command
		HashSet<Class> classSet = new HashSet<Class>();
		loadCommands(PROPERTY_COMMAND_JAR_PATH, "com.netcrest.pado.tools.pado.command", classSet);

		ArrayList<String> commandList = new ArrayList<String>();
		for (Class class1 : classSet) {
			commandList.add(getCommandName(class1));
		}
		Collections.sort(commandList);
		commands = commandList.toArray(new String[0]);

		for (Class commandClass : classSet) {
			if (ICommand.class.isAssignableFrom(commandClass)) {
				ICommand command = (ICommand) commandClass.newInstance();
				command.initialize(this);
				commandMap.put(getCommandName(command.getClass()), command);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void loadCommands(String packageName, String commandPackageName, HashSet<Class> classSet) throws Exception
	{
		Class classes[] = ClassFinder.getClasses(commandPackageName);
		if (classes.length == 0) {
			String jarPath = System.getProperty(packageName);
			classes = ClassFinder.getClasses(jarPath, commandPackageName);
		}

		for (int i = 0; i < classes.length; i++) {
			if (isImplement(classes[i], ICommand.class)) {
				classSet.add(classes[i]);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private String getCommandName(Class class1)
	{
		String name = class1.getSimpleName();
		if (name.endsWith("_")) {
			name = name.substring(0, name.length() - 1);
		}
		if (name.equals("os")) {
			name = "!";
		}
		return name;
	}

	private void initFile(String relativeFilePath) throws IOException
	{
		// Load jars in directory
		// TODO: classpath currently not supported
		if (jarDirectoryPath != null) {
			File file = new File(jarDirectoryPath);
			if (file.exists() == false) {
				println();
				printlnError("Specified file does not exist: " + jarDirectoryPath);
				println();
				System.exit(-1);
			}
			runCommand("classpath -jar " + jarDirectoryPath, false);
		}

		// Load jars
		if (jarPaths != null) {
			runCommand("classpath -jar " + jarPaths, false);
		}

		// Load all plugins - DataSerializables.txt not needed if
		// the data class jar files are placed in the plugins
		// directory. Note that loadPlugins() loads all classes
		// in that directory including subdirectories.
		loadPlugins();

		// Get .padorc file
		File padorcFile = null;
		if (inputFilePath != null) {
			if (inputFilePath.startsWith("/") || inputFilePath.indexOf(':') >= 0) {
				// absolute path
				padorcFile = new File(inputFilePath);
			} else {
				// relative path
				if (startupDir != null) {
					padorcFile = new File(startupDir, inputFilePath);
				} else {
					padorcFile = new File(inputFilePath);
				}
			}
		}

		// TODO: Handle user with no home directory
		String userHomeDir = System.getProperty("user.home");

		// if the input file is valid
		if (padorcFile != null) {
			if (padorcFile.exists() == false || padorcFile.isFile() == false) {
				printlnError("Invalid input file - " + inputFilePath);
				System.exit(-1);
			}
		} else {
			padorcFile = new File(userHomeDir, relativeFilePath);
			if (padorcFile.exists() == false) {
				padorcFile = new File(userHomeDir, ".padorc");
				if (padorcFile.exists() == false) {
					padorcFile.createNewFile();
				}
			}
		}

		File padoDir = new File(userHomeDir, ".pado");
		if (padoDir.exists() == false) {
			padoDir.mkdir();
		}

		File etcDir = new File(padoDir, "etc");
		if (etcDir.exists() == false) {
			etcDir.mkdir();
		}

		File logDir = new File(padoDir, "log");
		if (logDir.exists() == false) {
			logDir.mkdir();
		}

		File statsDir = new File(padoDir, "stats");
		if (statsDir.exists() == false) {
			statsDir.mkdir();
		}

		File historyDir = new File(padoDir, "history");
		if (historyDir.exists() == false) {
			historyDir.mkdir();
		}

		// System.setProperty("gemfirePropertyFile",
		// "etc/client/client.properties");
		System.setProperty("gemfire.log-file", logDir.getAbsolutePath() + "/pado.log");
		System.setProperty("gemfire.statistic-archive-file", statsDir.getAbsolutePath() + "/pado.gfs");

		// Execute arguments
		executeArgs();
		
		if (ignorePadoRcFile == false) {
			runScript(padorcFile);
		}

		// load the history file before the first command is executed.
		loadHistoryFromFile();
	}

	private void loadPlugins()
	{
		String pluginJars = System.getProperty(PROPERTY_PLUGIN_JARS);
		if (pluginJars == null) {
			return;
		}
		pluginJars = pluginJars.trim();
		if (pluginJars.length() == 0) {
			return;
		}

		String pathSeparator = System.getProperty("path.separator");
		String split[] = pluginJars.split(pathSeparator);
		for (int i = 0; i < split.length; i++) {
			try {
				ClassFinder.getAllClasses(split[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private boolean isImplement(Class cls, Class interf)
	{
		Class interfaces[] = cls.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			if (interfaces[i] == interf) {
				return true;
			}
		}
		return false;
	}

	private void initJline() throws Exception
	{
		consoleReader = new ConsoleReader();
		if (consoleReader != null) {
			if (editorName.equals("emacs")) {
				consoleReader.setKeyMap("emacs");
			} else {
				// "vi"
				consoleReader.setKeyMap("vi-insert");
			}
			consoleReader.setBellEnabled(true);
			consoleReader.setHandleUserInterrupt(false);
			List<Completer> completers = new LinkedList<Completer>();
			completers.add(new StringsCompleter(commands));
			completers.add(new FileNameCompleter());
			// consoleReader.addCompleter(new ArgumentCompleter(completers));
			for (Completer c : completers) {
				consoleReader.addCompleter(c);
			}
		}
	}

	public static void println()
	{
		if (consoleReader == null) {
			System.out.println();
		} else {
			try {
				consoleReader.println();
				consoleReader.flush();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public static void print(String string)
	{
		if (consoleReader == null) {
			System.out.print(string);
		} else {
			try {
				consoleReader.print(string);
				consoleReader.flush();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public static void println(String string)
	{
		if (consoleReader == null) {
			System.out.println(string);
		} else {
			try {
				consoleReader.println(string);
				consoleReader.flush();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public static void println(ICommand command, String string)
	{
		if (command == null) {
			println(string);
		} else {
			println(command.getClass().getSimpleName() + ": " + string);
		}
	}

	/**
	 * Prints only the prompt string.
	 */
	public static void printPrompt(String string)
	{
		if (consoleReader == null) {
			System.out.print(string);
		} else {
			try {
				consoleReader.print(string);
				consoleReader.flush();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public static void printlnError()
	{
		System.err.println();
	}

	public static void printlnError(String string)
	{
		System.err.println(string);
	}

	public static void printlnError(ICommand command, String string)
	{
		if (command == null) {
			printlnError(string);
		} else {
			System.err.println(command.getClass().getSimpleName() + ": " + string);
		}
	}

	/**
	 * Returns a line read from the prompt. If line ends with '\' then it also
	 * includes the next line.
	 * 
	 * @param prompt
	 *            Prompt message. If null, it displays the current path. If an
	 *            empty string, it does not record the input in history.
	 * @throws IOException
	 *             Thrown if a console I/O error occurs.
	 */
	public String readLine(String prompt) throws IOException
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
					printPrompt(prompt + "> ");
				} else {
					printPrompt(prompt);
				}
				nextLine = bufferedReader.readLine();
			} else {
				if (prompt.length() == 0) {
					consoleReader.setHistoryEnabled(false);
				}
				if (addAngle) {
					nextLine = consoleReader.readLine(prompt + "> ");
				} else {
					nextLine = consoleReader.readLine(prompt);
				}
				if (prompt.length() == 0) {
					consoleReader.setHistoryEnabled(true);
				}
			}

			// if nextLine is null then we encountered EOF.
			// In that case, leave cmdBuffer null if it is still null

			// if (isEcho()) {
			// if (nextLine == null) {
			// println("EOF");
			// } else if (nextLine.length() != 0) {
			// println(nextLine);
			// }
			// }

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

		return cmdBuffer == null ? null : cmdBuffer.toString();
	}

	public boolean isEcho()
	{
		return isEcho;
	}

	public void setEcho(boolean isEcho)
	{
		this.isEcho = isEcho;
	}

	public String expandEnvs(String value)
	{
		value = value.trim();

		// Find properties and place them in list.
		String split[] = value.split("\\$\\{");
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < split.length; i++) {
			int index = split[i].indexOf('}');
			if (index != -1) {
				list.add(split[i].substring(0, index));
			}
		}

		// apply each property (key) in the list
		for (String key : list) {
			String val = getEnv(key);
			if (val == null) {
				value = value.replaceAll("\\$\\{" + key + "\\}", "");
			} else {
				value = value.replaceAll("\\$\\{" + key + "\\}", val);
			}
		}
		return value;
	}

	/**
	 * Executes the specified PadoShell command string.
	 * 
	 * @param commandString
	 *            Command string
	 * @param isResetTimer
	 *            true to reset the timer.
	 * @return true if the specified command is successfully executed, false
	 *         otherwise.
	 */
	public boolean runCommand(String commandString, boolean isResetTimer)
	{
		String commandName = null;
		if (commandString != null) {
			commandString = commandString.trim();
			commandString = expandEnvs(commandString);
			if (isEcho()) {
				println(commandString);
			}
			String[] split = commandString.split(" ");
			commandName = split[0];
			if (commandName.endsWith("&")) {
				commandName = commandName.substring(0, commandName.length() - 1);
			}
		}

		// Add quit/exit command to history before executing it.
//		if (commandString == null /* EOF */|| commandString.endsWith("exit") || commandString.endsWith("quit")) {
//			addCmdHistory(commandString);
//		}

		try {
			ICommand command = getCommand(commandName);
			if (command != null) {
				// CommandLine commandLine =
				// cliParseCommandLine(command.getOptions(), commandString);
				// if (commandLine.hasOption('?')) {
				// command.help();
				// } else {
				// if (command.isLoginRequired() &&
				// SharedCache.getSharedCache().isLoggedIn() == false) {
				// printlnError(command, "Not logged in. Command aborted.");
				// return false;
				// }
				// startTimer();
				// command.run(commandLine, commandString);
				// stopTimer();
				// if (!commandName.equals("r")) {
				// addCmdHistory(commandString);
				// }
				// }
			} else {

				// Some command name exceptions...
				if (commandString.matches("\\(.*select.*")) {
					command = getCommand("select");
				} else if (commandString.startsWith("?")) {
					command = getCommand("help");
				}
			}

			if (command == null) {
				// the specified command not supported
				if (commandString.length() != 0) {
					printlnError("Unrecognized command. Enter Tab, 'help' or '?' to get a list of commands.");
				}
				return false;
			} else {
				boolean isBackground = commandString.endsWith("&");
				CommandLine commandLine;
				if (isBackground) {
					commandLine = cliParseCommandLine(command.getOptions(),
							commandString.substring(0, commandString.length() - 1));
				} else {
					commandLine = cliParseCommandLine(command.getOptions(), commandString);
				}
				if (commandLine.hasOption('?')) {
					command.help();
				} else {
					if (command.isLoginRequired() && SharedCache.getSharedCache().isLoggedIn() == false) {
						printlnError(command, "Not logged in. Command aborted.");
						return false;
					}

					if (isBackground) {
						if (backgroundThreadPool == null) {
							backgroundThreadPool = Executors.newCachedThreadPool(new ThreadFactory() {
					            public Thread newThread(Runnable r) {
					                Thread t = new Thread(r, "Pado-PadoShellBgCached");
					                t.setDaemon(true);
					                return t;
					            }
					        });
							backgroundCommandRunnerList = Collections
									.synchronizedList(new ArrayList<BackgroundCommandRunner>(10));
						}
						int jobNumber = 1;
						if (backgroundCommandRunnerList.size() > 0) {
							BackgroundCommandRunner runner = backgroundCommandRunnerList
									.get(backgroundCommandRunnerList.size() - 1);
							jobNumber = runner.jobNumber + 1;
						}
						BackgroundCommandRunner runner = new BackgroundCommandRunner(jobNumber, command, commandLine,
								commandString);
						backgroundCommandRunnerList.add(runner);
						backgroundThreadPool.execute(runner);
					} else {
						if (isResetTimer) {
							startTimer();
						}
						command.run(commandLine, commandString);
						if (isResetTimer) {
							stopTimer();
						}
					}

					if (command instanceof logout) {
						reset();
					}
				}
			}

		} catch (Exception ex) {
			printlnError(getCauseMessage(ex));
			if (DEBUG_ENABLED) {
				ex.printStackTrace();
			}
			return false;
		}
		return true;
	}

	class BackgroundCommandRunner implements Runnable
	{
		int jobNumber;
		ICommand command;
		CommandLine commandLine;
		String commandString;
		long startTime;
		long endTime;

		BackgroundCommandRunner(int jobNumber, ICommand command, CommandLine commandLine, String commandString)
		{
			this.jobNumber = jobNumber;
			this.command = command;
			this.commandLine = commandLine;
			this.commandString = commandString;
			this.startTime = System.currentTimeMillis();
		}

		@Override
		public void run()
		{
			try {
				command.run(commandLine, commandString);
			} catch (Exception ex) {
				printlnError(getCauseMessage(ex));
				if (DEBUG_ENABLED) {
					ex.printStackTrace();
				}
			} finally {
				endTime = System.currentTimeMillis();
				backgroundCommandRunnerList.remove(this);
			}
		}

		public long getElapsedTime()
		{
			if (endTime == 0) {
				return System.currentTimeMillis() - startTime;
			} else {
				return endTime - startTime;
			}
		}
	}

	/**
	 * Parses a <code>command</code> and places each of its tokens in a
	 * <code>List</code>. Tokens are separated by whitespace, or can be wrapped
	 * with double-quotes
	 */
	public boolean parseCommand(String command)
	{
		commandArgList.clear();
		Reader in = new StringReader(command);
		StringBuffer currToken = new StringBuffer();
		String delim = " \t\n\r\f";
		int c;
		boolean inQuotes = false;
		do {
			try {
				c = in.read();
			} catch (IOException e) {
				throw new Error("unexpected exception", e);
			}

			if (c < 0)
				break;

			if (c == '"') {
				if (inQuotes) {
					inQuotes = false;
					commandArgList.add(currToken.toString().trim());
					currToken = new StringBuffer();
				} else {
					inQuotes = true;
				}
				continue;
			}

			if (inQuotes) {
				currToken.append((char) c);
				continue;
			}

			if (delim.indexOf((char) c) >= 0) {
				// whitespace
				if (currToken.length() > 0) {
					commandArgList.add(currToken.toString().trim());
					currToken = new StringBuffer();
				}
				continue;
			}

			currToken.append((char) c);
		} while (true);

		if (currToken.length() > 0) {
			commandArgList.add(currToken.toString().trim());
		}
		return true;
	}

	/**
	 * @param command
	 * @return
	 * @throws Exception
	 * @throws org.apache.commons.cli.ParseException
	 */
	public CommandLine cliParseCommandLine(Options options, String command) throws Exception
	{
		parseCommand(command);
		ArrayList<String> list = getCommandArgList();
		String[] args = list.toArray(new String[list.size()]);
		return cliParseCommandLine(options, args);
	}

	public CommandLine cliParseCommandLine(Options options, String[] args) throws Exception
	{
		// Parse the program arguments
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (org.apache.commons.cli.ParseException e) {
			throw new Exception(e.getMessage());
		}
		return commandLine;
	}

	public boolean isHistoryPerSession()
	{
		return historyPerSession;
	}

	public void setHistoryPerSession(boolean historyPerSession)
	{
		this.historyPerSession = historyPerSession;
	}

	public int getHistoryMaxSize()
	{
		return ((MemoryHistory)consoleReader.getHistory()).getMaxSize();
	}

	public void setHistoryMaxSize(int maxSize)
	{
		((MemoryHistory)consoleReader.getHistory()).setMaxSize(maxSize);
	}
	
	public String getHistoryFileName()
	{
		return historyFileName;
	}

	public String getEnv(String variable)
	{
		return envProperties.getProperty(variable);
	}

	/**
	 * Sets an environment variable.
	 * 
	 * @param variable
	 *            Environment variable
	 * @param value
	 *            Value. If null, removes the variable.
	 */
	public void setEnv(String variable, String value)
	{
		if (value == null) {
			envProperties.remove(variable);
		} else {
			envProperties.setProperty(variable, value);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void printEnvs()
	{
		ArrayList<String> list = new ArrayList(envProperties.keySet());
		Collections.sort(list);
		for (String variable : list) {
			String value = getEnv(variable);
			println(variable + "=" + value);
		}
	}

	public ArrayList<String> getCommandArgList()
	{
		return commandArgList;
	}

	public ICommand getCommand(String commandName)
	{
		if (commandName.equals("n")) {
			commandName = "next";
		} else if (commandName.equals("class")) {

		}
		return (ICommand) commandMap.get(commandName);
	}

	public String getCauseMessage(Throwable ex)
	{
		Throwable cause = ex.getCause();
		String causeMessage = null;
		if (cause != null) {
			causeMessage = getCauseMessage(cause);
		} else {
			causeMessage = ex.getClass().getSimpleName();
			if (ex.getMessage() != null) {
				causeMessage += ": " + ex.getMessage();
			}
		}
		return causeMessage;
	}

	public void exit(int status)
	{
		try {
			SharedCache.getSharedCache().disconnect();
		} catch (Exception ex) {
			// ignore
		}
		if (consoleReader != null) {
			consoleReader.shutdown();
		}
		System.exit(status);
	}

	public boolean isShowTime()
	{
		return showTime;
	}

	public void setShowTime(boolean showTime)
	{
		this.showTime = showTime;
	}

	public boolean isShowResults()
	{
		return showResults;
	}

	public void setShowResults(boolean showResults)
	{
		this.showResults = showResults;
	}

	public void writeHistoryToFile()
	{
		try {
			// History may be not be FileHistory if PadoShell is executed
			// as a script.
			if (consoleReader.getHistory() instanceof FileHistory) {
				FileHistory history = (FileHistory)consoleReader.getHistory();
				history.flush();
			}
		} catch (IOException ioex) {
			printlnError("History file write failed.");
			printlnError(ioex.getMessage());
			Logger.error(ioex);
		}
	}

	private void loadHistoryFromFile()
	{
		File historyFile = null;
		File historyDir = null;
		String userHomeDir = System.getProperty("user.home");
		File padoDir = new File(userHomeDir, ".pado");
		if (!padoDir.exists()) {
			padoDir.mkdir();
		}
		historyDir = new File(padoDir, "history");
		if (!historyDir.exists()) {
			historyDir.mkdir();
		}
		if (historyDir.exists()) {
			historyFile = new File(historyDir, historyFileName);
		}
		try {
			FileHistory history = new FileHistory(historyFile);
			history.setMaxSize(getHistoryMaxSize());
			history.setAutoTrim(true);
			consoleReader.setHistory(history);
		} catch (Exception ex) {
			printlnError("History file read failed.");
			printlnError(ex.getMessage());
			Logger.error(ex);
		}
	}

	public boolean isTableFormat()
	{
		return tableFormat;
	}

	public void setTableFormat(boolean tableFormat)
	{
		this.tableFormat = tableFormat;
		PrintUtil.setTableFormat(tableFormat);
	}

	public boolean isPrintType()
	{
		return printType;
	}

	public void setPrintType(boolean printType)
	{
		this.printType = printType;
		SimplePrintUtil.setPrintType(printType);
	}

	public int getCollectionEntryPrintCount()
	{
		return collectionEntryPrintCount;
	}

	public void setCollectionEntryPrintCount(int collectionEntryPrintCount)
	{
		this.collectionEntryPrintCount = collectionEntryPrintCount;
		SimplePrintUtil.setCollectionEntryPrintCount(collectionEntryPrintCount);
	}

	public void setReadTimeout(long readTimeout)
	{
		this.readTimeout = readTimeout;
	}

	public long getReadTimeout()
	{
		return readTimeout;
	}

	/**
	 * Result set limit size. -1 if no limit.
	 */
	public int getSelectLimit()
	{
		return selectLimit;
	}

	public void setSelectLimit(int selectLimit)
	{
		this.selectLimit = selectLimit;
	}

	public int getFetchSize()
	{
		return fetchSize;
	}

	public void setFetchSize(int fetchSize)
	{
		this.fetchSize = fetchSize;
	}

	public void setKeyClass(String keyClassName)
	{
		if (keyClassName == null) {
			keyClass = null;
		}
		try {
			keyClass = Class.forName(keyClassName);
		} catch (ClassNotFoundException e) {
			printlnError("Class not found.");
		}
	}

	public void setKeyClass(Class<?> queryKeyClass)
	{
		this.keyClass = queryKeyClass;
	}

	public Class<?> getKeyClass()
	{
		return keyClass;
	}

	public String getKeyClassName()
	{
		return keyClass == null ? null : keyClass.getName();
	}

	public Class<?> getValueClass()
	{
		return valueClass;
	}

	public String getValueClassName()
	{
		return valueClass == null ? null : valueClass.getName();
	}

	public void setValueClass(String valueClassName)
	{
		if (valueClassName == null) {
			valueClass = null;
		}
		try {
			valueClass = Class.forName(valueClassName);
		} catch (ClassNotFoundException e) {
			printlnError("Class not found.");
		}
	}

	public void setValueClass(Class<?> valueClass)
	{
		this.valueClass = valueClass;
	}

	public long getZoneDifference()
	{
		return zoneDifference;
	}

	public void setZoneDifference(long zoneDifference)
	{
		this.zoneDifference = zoneDifference;
	}

	public void setCurrentPath(String currentPath)
	{
		this.currentPath = currentPath;
	}

	public String getCurrentPath()
	{
		return currentPath;
	}

	/**
	 * Returns a data object for the specified function call
	 * 
	 * @param value
	 *            The data function of the formant
	 *            "to_date('date', 'simple date formant')"
	 * @throws ParseException
	 */
	public Date getDate(String value) throws ParseException
	{
		Date date = null;

		// to_date('10/10/2008', 'MM/dd/yyyy')
		String lowercase = value.toLowerCase();
		boolean error = false;
		if (lowercase.startsWith("to_date") == false) {
			error = true;
		} else {
			int index = value.indexOf('(');
			if (index == -1) {
				error = true;
			}
			value = value.substring(index + 1);
			String split2[] = value.split(",");
			if (split2.length != 2) {
				error = true;
			} else {
				for (int j = 0; j < split2.length; j++) {
					split2[j] = split2[j].trim();
				}
				String dateStr = StringUtil.trim(split2[0], '\'');
				String format = StringUtil.trim(StringUtil.trimRight(split2[1], ')'), '\'');
				dateFormat.applyPattern(format);
				date = dateFormat.parse(dateStr);
			}
		}
		if (error) {
			printlnError("Invalid date macro. Must use to_date('<date>', '<format>'). Ex, to_date('10/10/08', 'MM/dd/yy')");
		}
		return date;
	}

	public void startTimer()
	{
		if (isShowTime() && isShowResults()) {
			TimerUtil.startTimer();
		}
	}

	public void stopTimer()
	{
		if (isShowTime() && isShowResults()) {
			TimerUtil.printExecutionTime();
		}
	}

	/**
	 * Resolves and returns the full path of the specified path relative to the
	 * current path. Returns null if the specified path is null.
	 * 
	 * @param path
	 *            PadoShell path.
	 */
	public String getFullPath(String path)
	{
		return getFullPath(path, getCurrentPath());
	}

	/**
	 * Resolves and returns the grid path of the specified path relative to the
	 * current path. Returns null if the specified path is null.
	 * 
	 * @param path
	 *            PadoShell path.
	 */
	public String getGridPath(String path)
	{
		return GridUtil.getChildPath(getFullPath(path));
	}

	/**
	 * Returns the grid path of the specified full path,
	 * 
	 * @param fullPath
	 *            Full path
	 */
	public String getGridPathFromFullPath(String fullPath)
	{
		return GridUtil.getChildPath(fullPath);
	}

	/**
	 * Returns the grid ID of the specified path.
	 * 
	 * @param path
	 *            PadoShell path.
	 * @return
	 */
	public String getGridId(String path)
	{
		return SharedCache.getSharedCache().getGridId(getFullPath(path));
	}

	public boolean isRootPath(String fullPath)
	{
		if (fullPath == null) {
			return false;
		}
		return fullPath.equals("/");
	}

	/**
	 * Returns true if PadoShell is running in the interactive mode. If not
	 * interactive mode, then each command must force execution by disabling
	 * prompts.
	 */
	public boolean isInteractiveMode()
	{
		return interactiveMode;
	}

	public void setInteractiveMode(boolean interactiveMode)
	{
		this.interactiveMode = interactiveMode;
	}

	/**
	 * Prints information on how this program should be used.
	 */
	public void showHelp(boolean isFull)
	{
		for (int i = 0; i < commands.length; i++) {
			ICommand command = getCommand(commands[i]);
			String commandName = command.getClass().getSimpleName();
			if (commandName.endsWith("_")) {
				commandName = commandName.substring(0, commandName.length() - 1);
			}
			if (isFull) {
				command.help();
			} else {
				println(commandName);
				println("   " + command.getShortDescription());
			}
		}
		println();
		println("Run 'help <command>' to get detailed command descriptions.");
		println("TAB to get complete command name(s).");
		println("Run 'refresh' to get the latest grid information.");
		println("Note that PadoShell commands auto-refresh where appropriate.");
	}

	/**
	 * Returns the full path. Supports '..'.
	 * 
	 * @param newPath
	 *            The new path to be evaluated
	 * @param relativePath
	 *            The current path
	 * @return Returns null if the new path is invalid.
	 */
	public String getFullPath(String newPath, String relativePath)
	{
		if (newPath == null) {
			return null;
		}
		if (newPath.startsWith("/")) {
			return newPath;
		}

		if (relativePath == null) {
			relativePath = "/";
		}

		String[] split = relativePath.split("/");
		Stack<String> pathStack = new Stack<String>();
		for (int i = 0; i < split.length; i++) {
			if (split[i].length() == 0) {
				continue;
			}
			pathStack.add(split[i]);
		}
		split = newPath.split("/");
		boolean invalidPath = false;
		for (int i = 0; i < split.length; i++) {
			if (split[i].length() == 0) {
				continue;
			}
			String dirName = split[i];
			if (dirName.equals("..")) {
				if (pathStack.size() == 0) {
					invalidPath = true;
					break;
				}
				pathStack.pop();
			} else if (dirName.equals(".")) {
				continue;
			} else {
				pathStack.add(dirName);
			}
		}

		if (invalidPath) {
			return null;
		}

		String fullPath = "";
		while (pathStack.size() > 0) {
			fullPath = "/" + pathStack.pop() + fullPath;
		}
		if (fullPath.length() == 0) {
			fullPath = "/";
		}
		return fullPath;
	}

	public void addBufferCommand(String commandName)
	{
		bufferCommandSet.add(commandName);
	}

	public Set<String> getBufferCommandSet()
	{
		return bufferCommandSet;
	}

	public Object getQueryKey(List<?> queryPredicateList, int startIndex) throws Exception
	{
		// See if key is a primitive
		String input = (String) queryPredicateList.get(startIndex);
		Object key = null;
		if (input.startsWith("'")) {
			int lastIndex = -1;
			if (input.endsWith("'") == false) {
				lastIndex = input.length();
			} else {
				lastIndex = input.lastIndexOf("'");
			}
			if (lastIndex <= 1) {
				PadoShell.printlnError("Invalid key. Empty string not allowed.");
				return null;
			}
			key = input.subSequence(1, lastIndex); // lastIndex exclusive
		} else {
			key = ObjectUtil.getPrimitive(this, input, false);
		}
		if (key != null) {
			return key;
		}

		// Key is an object

		// query key class must be defined
		if (keyClass == null) {
			PadoShell.printlnError("Key undefined. Use the key command to specify the key class.");
			return null;
		}

		// f1=v1 and f2='v2' and f3=v3

		// Build the query predicate from the argument list
		String queryPredicate = "";
		for (int i = startIndex; i < queryPredicateList.size(); i++) {
			queryPredicate += queryPredicateList.get(i) + " ";
		}
		String[] split = queryPredicate.split("and");

		// Create the query key by invoking setters for each
		// parameter listed in the queryPredicate
		Object queryKey = keyClass.newInstance();
		// Map<String, Method> setterMap =
		// ReflectionUtil.getAllSettersMap(queryKey.getClass());
		Map<String, String> fieldsMap = new HashMap<String, String>();
		for (int i = 0; i < split.length; i++) {
			String token = split[i];
			String[] tokenSplit = token.split("=");
			if (tokenSplit.length < 2) {
				printlnError(token + ": Invalid query.");
				return null;
			}
			String field = tokenSplit[0].trim();
			String value = tokenSplit[1].trim();
			fieldsMap.put(field, value);
			queryKey = ObjectUtil.generateObject(keyClass.getName(), fieldsMap, this);
		}

		return queryKey;
	}

	/**
	 * Resets PadoShell to the initialization state. If logged on, then it only
	 * clears relevant resources. Otherwise, clears all resources.
	 */
	private void reset()
	{
		TimerUtil.reset();
		SharedCache.getSharedCache().reset();
	}

	/**
	 * Blocks until backgroundCommandRunnerList is empty.
	 */
	public void waitForBackgroundToComplete()
	{
		if (backgroundCommandRunnerList == null || backgroundCommandRunnerList.size() == 0) {
			return;
		}
		controlZPressed = false;
//		Executors.newSingleThreadExecutor().execute(new Runnable() {
//
//			@Override
//			public void run()
//			{
//				controlZPressed = false;
//				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//				char input = ' ';
//				do {
//					try {
//						input = (char) reader.read();
//						System.out.println((int)input);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				} while (input != 4);
//				controlZPressed = true;
//			}
//			
//		});
		while (backgroundCommandRunnerList.size() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			} finally {
				if (controlZPressed) {
					break;
				}
			}
		}
		controlZPressed = false;
	}

	private void initSignals()
	{
		// Control-Z (SIGTSTP) - Not supported on Windows.
		// Also, unable to trap on Mac.
//		Signal.handle(new Signal("TSTP"), new SignalHandler() {
//			// Signal handler method
//			public void handle(Signal signal)
//			{
//				controlZPressed = true;
//				// System.out.println("Control-Z pressed");
//			}
//		});
		
		// Use ctrl-c instead of ctrl-z - Not available on Windows
		Signal.handle(new Signal("INT"), new SignalHandler() {
			// Signal handler method
			public void handle(Signal signal)
			{
				controlZPressed = true;
				// System.out.println("Control-C pressed");
			}
		});
	}

	public void printJobs()
	{
		if (backgroundCommandRunnerList == null) {
			return;
		}
		for (BackgroundCommandRunner runner : backgroundCommandRunnerList) {
			println("[" + runner.jobNumber + "]+  Running(" + runner.getElapsedTime() + " msec)  "
					+ runner.commandString);
		}
	}
	
	public ConsoleReader getConsoleReader()
	{
		return consoleReader;
	}

	/**
	 * Prompts the user for input and executes the command accordingly.
	 */
	void go()
	{
		println();
		println("Enter Tab, 'help' or '?' for command descriptions.");
		println("");
		while (true) {
			try {
				String line = readLine((String) null);
				if (line != null) {
					String commandStrings[] = splitCommands(line);
					for (String commandString : commandStrings) {
						runCommand(commandString, true);
					}
				}
			} catch (Exception ex) {
				printlnError(getCauseMessage(ex));
				if (DEBUG_ENABLED) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns an array of commands by splitting the specified line separated
	 * by ';'. It resolves '"' and '\'.
	 * @param line Commands separated by ';'.
	 * @return null only if line is null.
	 */
	private String[] splitCommands(String line)
	{
		if (line == null) {
			return null;
		}
		boolean openQuote = false;
		char pc = ' ';
		StringBuffer buffer = new StringBuffer();
		ArrayList<String> commandList = new ArrayList<String>(10);
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			switch (c) {
			case '"':
				if (pc == '\\') {
					break;
				}
				openQuote = !openQuote;
				buffer.append(c);
				break;
			case ';':
				if (openQuote == false) {
					commandList.add(buffer.toString());
					buffer = new StringBuffer();
				} else {
					buffer.append(c);
				}
				break;
			default:
				buffer.append(c);
				break;
			}
			pc = c;
		}
		commandList.add(buffer.toString());
		return commandList.toArray(new String[commandList.size()]);
	}
	
	public void runScript(File scriptFile) throws IOException
	{
		LineNumberReader reader = null;
		try {
			reader = new LineNumberReader(new FileReader(scriptFile));
			String line = reader.readLine();
			String command;
			ArrayList<String> propertyCommandList = new ArrayList<String>();
			ArrayList<String> commandList = new ArrayList<String>();
			StringBuffer buffer = new StringBuffer();

			while (line != null) {
				command = line.trim();
				if (command.length() > 0 && command.startsWith("#") == false) {
					if (command.endsWith("\\")) {
						buffer.append(command.substring(0, command.length() - 1));
					} else {
						buffer.append(command);
						StringTokenizer strToken = new StringTokenizer(command);
						if (strToken.hasMoreTokens()) {
							String commandName = strToken.nextToken();
							// if (PROPERTIES_CMD_LIST.contains(commandName)) {
							// propertyCommandList.add(buffer.toString().trim());
							// }
							/*
							 * else if (commandName != null && commandName
							 * .startsWith("historyPerSession")) { try { String
							 * value = command .substring("historyPerSession"
							 * .length() + 1);
							 * setMultiSessionPerUser(Boolean.valueOf(value)); }
							 * catch (Exception ex) { // do nothing } }
							 */
							// else {
							commandList.add(buffer.toString().trim());
							// }
						}
						buffer = new StringBuffer();
					}
				}
				line = reader.readLine();
			}

			command = null;
			// Execute properties set commands
			// for (String propertyCommand : PROPERTIES_CMD_LIST) {
			// for (int i = 0; i < propertyCommandList.size(); i++) {
			// command = propertyCommandList.get(i);
			// StringTokenizer strToken = new StringTokenizer(command);
			// if (strToken.hasMoreTokens()) {
			// String commandName = strToken.nextToken();
			// if (commandName.equals(propertyCommand)) {
			// runCommand(command);
			// propertyCommandList.remove(i);
			// i--;
			// }
			// }
			// }
			// }
			// Execute commands
			for (int i = 0; i < commandList.size(); i++) {
				command = commandList.get(i);
				runCommand(command, false);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private void usage()
	{
		String homeDir = System.getProperty("user.home");
		println();
		println("pado [-l <host1:port2>[<@group1>],<host2:port2>[[@group2>],...");
		println("     [-a <adpp ID>");
		println("     [-clean]");
		println("     [-d <domain name>]");
		println("     [-dir <directory>]");
		println("     [-u <user>]");
		println("     [-p <password>]");
		println("     [-jar <directory>]");
		println("     [-f <script_file>]");
		println("     [-i <.padorc_file>]");
		println("     [-n]");
		println("     [-e \"<commands separated by ;>\"");
		println("     [-h <name>]");
		println("     [-o vi|emacs]");
		println("     [-v]");
		println("     [-?]");
		println();
		println("   -a <app ID>       Specify app ID.");
		println("   -clean            Cleans up the log and stats files in the folowing directories:");
		println("                        " + homeDir + "/.pado/log");
		println("                        " + homeDir + "/.pado/stats");
		println("   -d <domain name>  Specify domain name if required by the entitlement system.");
		println("   -dir <directory>  Change directory to the specified directory before running");
		println("                     PadoShell. The specified directory effectively becomes");
		println("                     the PadoShell working directory.");
		println("   -e \"<commands separated by ;>\"  Execute the specified commands. Commands");
		println("                     can be optionally enclosed in double quotes. Note that");
		println("                     -e takes precedence over -f.");
		println("   -f <script_file>  Specify a script file to be executed. PadoShell terminates");
		println("                     upon completion of script execution.");
		println("   -h <name>         Record history in the specified file name. By default, history");
		println("                     is saved in " + homeDir + ".pado/history/pado_history.");
		println("                     Use this option to bypass the default name. <name> must be a valid");
		println("                     file name.");
		println("   -i <.padorc_file> Specify the input file that overrides the default");
		println("                     .padorc file in " + homeDir + ".");
		println("   -jar <directory>  Include all of jar files found in the specified directory");
		println("                     and its sub-directories in the class path.");
		println("   -l <host:port>    Specify locators.");
		println("   -n                Do not execute <.padorc_file>. Override the -i option.");
		println("   -o vi|emacs       Specify command line editor.");
		println("   -p <password>     Specify password.");
		println("   -u <user>         Specify user name.");
		println("   -v                Prints Pado version information.");
		println();
		println("   By default, if the -i option is not specified during startup, pado sequentially executes");
		println("   the commands listed in the .padorc file found in your home directory unless the -n option");
		println("   is specified.");
		println();
		println("   IMPORTANT: If login is required you can optionally specify the login information (app ID,");
		println("              user name and password in $PADO_HOME/etc/client/pado.properties using");
		println("              security.client.* properties.");
		println();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		PadoShell padoShell = new PadoShell(args);
		if (padoShell.isInteractiveMode()) {
			padoShell.initSignals();
			padoShell.go();
		} else {
			padoShell.setShowTime(false);
			if (padoShell.inputCommands != null) {
				StringBuffer buffer = new StringBuffer(padoShell.inputCommands.length * 10);
				for (String value : padoShell.inputCommands) {
					buffer.append(value + " ");
				}
				String parsedCommands[] = buffer.toString().split(";");
				for (String command : parsedCommands) {
					padoShell.runCommand(command, false);
				}
			}
			if (padoShell.scriptFilePath != null) {
				File file = new File(padoShell.scriptFilePath);
				if (file.exists() == false) {
					printlnError(padoShell.scriptFilePath + ": File does not exist.");
					System.exit(-1);
				}
				padoShell.runScript(file);
			}
			padoShell.waitForBackgroundToComplete();
			SharedCache.getSharedCache().disconnect();
			System.exit(0);
		}
	}
}
