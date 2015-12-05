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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;

public class login implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");

		Option opt = OptionBuilder.create("l");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("a");
		opt.setArgs(1);
		opt.setOptionalArg(true);
		options.addOption(opt);

		opt = OptionBuilder.create("d");
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

		// opt = OptionBuilder.create("t");
		// opt.setArgs(1);
		// opt.setOptionalArg(true);
		// options.addOption(opt);
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("login -l <host:port>[@<server-group>[,<host:port>[@server-group]] [-?]");
		PadoShell.println("   Login to the specified locator(s).");
		PadoShell.println("      -l <locator list>    Locators. Default: localhost:20000");
		PadoShell.println("      -a <app ID>  app ID. Default: sys");
		PadoShell.println("      -d <domain name>     Optional domain name. Required only if the grid has been");
		PadoShell.println("                           configured to authenticate against system domains.");
		PadoShell.println("      -u <user name>       User name. Default: " + System.getProperty("user.name"));
		PadoShell.println("      -p <passowrd>        Password");
		// PadoShell.println("      -t <read timeout>  Read timeout in msec.");
		// PadoShell.println("                         The default value is 300000 or 300 sec (5 min).");
		PadoShell.println("   Default: login -l localhost:20000 -a sys -u " + System.getProperty("user.name"));
	}

	@Override
	public String getShortDescription()
	{
		return "Login to Pado grid.";
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

	@Override
	public void run(CommandLine commandLine, String command) throws Exception
	{
		if (SharedCache.getSharedCache().isLoggedIn()) {
			PadoShell.printlnError(this, "Already logged in to " + SharedCache.getSharedCache().getPadoUrlString(true));
			return;
		}

		ArrayList<String> list = padoShell.getCommandArgList();
		String locators = null;
		String appId = null;
		String domain = null;
		String user = null;
		String pw = null;
		int readTimeout = -1;
		try {
			if (commandLine.hasOption('l')) {
				locators = commandLine.getOptionValue('l');
			}
			if (commandLine.hasOption('a')) {
				appId = commandLine.getOptionValue('a');
			}
			if (commandLine.hasOption('d')) {
				domain = commandLine.getOptionValue('d');
			}
			if (commandLine.hasOption('u')) {
				user = commandLine.getOptionValue('u');
			}
			if (commandLine.hasOption('p')) {
				pw = commandLine.getOptionValue('p');
			}
			if (commandLine.hasOption('t')) {
				String readTimeOutStr = commandLine.getOptionValue('t');
				if (readTimeOutStr != null) {
					readTimeout = Integer.parseInt(readTimeOutStr);
				}
			}
		} catch (Exception ex) {
			PadoShell.printlnError(this, "Invalid command");
			return;
		}

		padoShell.setReadTimeout(readTimeout);

//		// Get login info from pado.properties if not defined
//		if (pw == null) {
//			if (appId == null) {
//				appId = PadoUtil.getProperty(Constants.PROP_SECURITY_CLIENT_APPID);
//			}
//			if (domain == null) {
//				domain = PadoUtil.getProperty(Constants.PROP_SECURITY_CLIENT_DOMAIN);
//			}
//			if (user == null) {
//				user = PadoUtil.getProperty(Constants.PROP_SECURITY_CLIENT_USER);
//			}
//			String epw = PadoUtil.getProperty(Constants.PROP_SECURITY_CLIENT_PASS);
//			if (epw != null) {
////				try {
//					pw = AESCipher.decryptUserTextToText(epw);
////				} catch (Exception ex) {
////					PadoShell.printlnError("Error occurred while accessing encrypted password from pado.properties. "
////							+ ex.getMessage());
////					throw ex;
////				}
//			}
//		}

		// Undefined arguments (null) are set to default values in the following
		// call.
		if (SharedCache.getSharedCache().login(locators, appId, domain, user, pw != null ? pw.toCharArray() : null)) {
			if (padoShell.isInteractiveMode()) {
				String dispStr = "Login success: " + SharedCache.getSharedCache().getPadoUrlString(true);
				PadoShell.println(this, dispStr);
			}
			// Change directory to new home dir.
			padoShell.runCommand("cd", false);
		} else {
			PadoShell.printlnError(this, SharedCache.getSharedCache().getPadoUrlString(true) + ": Login failed");
		}
	}
}
