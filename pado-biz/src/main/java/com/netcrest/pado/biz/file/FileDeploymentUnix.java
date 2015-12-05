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
package com.netcrest.pado.biz.file;

import java.io.File;

/**
 * FileDeploymentUnix deploys splitted files to individual servers using the
 * bin_sh/tools/deploy_data_files command.
 * 
 * @author dpark
 * 
 */
public class FileDeploymentUnix
{
	private static void usage()
	{
		System.out.println();
		System.out.println("FileDeploymentUnix deploys splitted files to individual servers");
		System.out.println("using the bin_sh/tools/deploy_data_files command.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("   FileDeploymentUnix <input directory>");
		System.out.println("              -hosts <host names>");
		System.out.println("              -servers <server names");
		System.out.println("              [-prefix <file prefix>]");
		System.out.println();
		System.out.println("   -prefix <output file prefix> File prefix. All of the files that begin");
		System.out.println("                       with this prefix are deployed.");
		System.out.println("   -hosts <host names> Host names separated by space. Must be enclosed in double quotes.");
		System.out.println("   <input directory>  The directory of the files to be deployed.");
		System.out.println();
		System.exit(0);
	}

	public static void main(String[] args) throws Exception
	{
		String inputDirPath = null;
		String workingDirPath = ".";
		String hostNames = "";
		String serverNames = "";
		String filePrefix = null;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-prefix")) {
				if (i < args.length - 1) {
					filePrefix = args[++i];
				}
			} else if (arg.equals("-hosts")) {
				if (i < args.length - 1) {
					hostNames = args[++i];
				}
			} else if (arg.equals("-servers")) {
				if (i < args.length - 1) {
					serverNames = args[++i];
				}
			} else {
				inputDirPath = arg;
			}
		}

		if (inputDirPath == null) {
			System.err.println();
			System.err.println("Input directory path not specified.");
			System.err.println();
			System.exit(-1);
		}
		File inputDir = new File(inputDirPath);
		if (inputDir.exists() == false) {
			System.err.println();
			System.err.println("Input directory does not exist: " + inputDirPath);
			System.err.println();
			System.exit(-1);
		}
		if (inputDir.isDirectory() == false) {
			System.err.println();
			System.err.println(inputDirPath + " is not a directory.");
			System.err.println();
			System.exit(-1);
		}
		if (hostNames == null || hostNames.length() == 0) {
			System.err.println();
			System.err.println("Host names not specified.");
			System.err.println();
			System.exit(-1);
		}
		if (serverNames == null || serverNames.length() == 0) {
			System.err.println();
			System.err.println("Server names not specified.");
			System.err.println();
			System.exit(-1);
		}

		workingDirPath = System.getProperty("user.dir");
		File workingDir = new File(workingDirPath);

		System.out.println();
		System.out.println("File deployment started. Please wait.");
		FileUtilUnix.setVerbose(true);
		long startTime = System.currentTimeMillis();
		FileUtilUnix.deployFilesToServers(inputDir, filePrefix, workingDir, hostNames, serverNames);
		long endTime = System.currentTimeMillis();
		long delta = endTime - startTime;
		long seconds = delta / 1000;
		if (seconds != 0) {
			System.out.println("Elapsed time: " + seconds + " sec.");
		} else {
			System.out.println("Elapsed time: " + delta + " msec.");
		}
		System.out.println("File deployment complete.");
		System.out.println();
	}

}