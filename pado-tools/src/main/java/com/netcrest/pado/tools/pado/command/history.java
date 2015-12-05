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

import java.io.IOException;
import java.util.List;

import jline.console.ConsoleReader;
import jline.console.history.History;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;

public class history implements ICommand
{
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("n", false, "");
		options.addOption("c", false, "");
		options.addOption("w", false, "");

		Option opt = OptionBuilder.create("d");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);
	}

	private PadoShell padoShell;

	public void help()
	{
		PadoShell.println("history [-d <offset>] [-n] [-c] [-w] [n] [-?]");
		PadoShell.println("   Display or manipulate the history list with line numbers.");
		PadoShell.println("   An argument of n lists only the last n entries.");
		PadoShell.println("      -d <offset> Delete the history entry at the sepcified offset.");
		PadoShell.println("      -n          Suppress offset numbers when listing history.");
		PadoShell.println("      -c          Clear the history.");
		PadoShell.println("      -w          Write the current history to the history file.");
		PadoShell.println();
		PadoShell.println("   Other History and Shortcuts:");
		PadoShell.println("      !string      Execute the most recent command that begins with string.");
		PadoShell.println("      !num         Execute command that is number num in the command history.");
		PadoShell.println("      !-num        Execute the command was run num commands previous in the history.");
		PadoShell.println("      !!           Execute the previous (most recently-executed) command.");
		PadoShell.println("      !?string[?]  Execute the most recent command containing the string string.");
		PadoShell.println("                   The trailing ? may be omitted if string represents the end of");
		PadoShell.println("                   the command in question.");
		PadoShell.println("      ^str1^str2^  Repeat the previous command executed, replacing str1 with str2.");
		PadoShell.println("                   The previous command must contain str1.");
		PadoShell.println();
		PadoShell.println("   IMPORTANT: History may be lost if PadoShell is improperly terminated.");
		PadoShell.println("              Always gracefully terminate PadoShell by executing 'quit' or 'exit'.");
		PadoShell.println("              Your can also write the current history by executing  'history -w'.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Display or manipulate the history list with line numbers.";
	}
	
	@Override
	public boolean isLoginRequired()
	{
		return false;
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
		if (argList.size() > 2) {
			PadoShell.printlnError(this, "Too many arguments.");
			return;
		}
		int numEntries = 100;
		if (argList.size() == 2) {
			try {
				numEntries = Integer.parseInt(argList.get(1));
			} catch (NumberFormatException ex) {
				PadoShell.printlnError(this, "Numeric argument required.");
				return;
			} catch (Exception ex) {
				throw ex;
			}
		}
		if (commandLine.hasOption("n")) {
			history_n(numEntries);
		} else if (commandLine.hasOption("d")) {
			String value = commandLine.getOptionValue("d");
			try {
				int offset = Integer.parseInt(value);
				history_d(offset);
			} catch (NumberFormatException ex) {
				PadoShell.printlnError(this, "Numeric argument required.");
				return;
			} catch (Exception ex) {
				throw ex;
			}
		} else if (commandLine.hasOption("c")) {
			history_c();
		} else if (commandLine.hasOption("w")) {
			history_w();
		} else {
			history_l(numEntries);
		}
	}

	private void history_c()
	{
		padoShell.getConsoleReader().getHistory().clear();
	}

	private void history_w() throws IOException
	{
		padoShell.writeHistoryToFile();
	}

	private void history_n(int numEntries)
	{
		if (numEntries <= 0) {
			return;
		}
		ConsoleReader consoleReader = padoShell.getConsoleReader();
		History history = consoleReader.getHistory();
		int startIndex = history.size() - numEntries;
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < history.size(); i++) {
			PadoShell.println(history.get(i) + "");
		}
	}

	private void history_l(int numEntries)
	{
		if (numEntries <= 0) {
			return;
		}
		ConsoleReader consoleReader = padoShell.getConsoleReader();
		History history = consoleReader.getHistory();
		int startIndex = history.size() - numEntries;
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < history.size(); i++) {
			PadoShell.println((i+1) + "  " + history.get(i));
		}
	}
	
	private void history_d(int offset)
	{
		ConsoleReader consoleReader = padoShell.getConsoleReader();
		int index = offset - 1;
		if (index >= consoleReader.getHistory().size()) {
			return;
		}
		consoleReader.getHistory().remove(index);
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}
}
