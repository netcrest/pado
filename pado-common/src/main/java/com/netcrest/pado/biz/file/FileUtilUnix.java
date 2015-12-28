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

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;

/**
 * FileUtilUnix provides methods that execute file-related Unix commands.
 * 
 * @author dpark
 * 
 */
public class FileUtilUnix
{
	private static boolean isVerbose = Boolean.getBoolean(PadoUtil
			.getSystemPropertyName(Constants.PROP_LOADER_DATA_VERBOSE));
	private final static String DEPLOY_COMMAND = "./deploy_data_files_input";

	/**
	 * Splits the input file into small files that are equal in size. The file
	 * size is specified by the argument lineCountPerSplit.
	 * 
	 * @param inputFile
	 *            The input file to split.
	 * @param outputDir
	 *            The output directory. If null, the output files are created in
	 *            the working directory.
	 * @param outputFilePrefix
	 *            The output file prefix. All output files are enumerated with
	 *            the suffix "#" starting from 1. If null or empty, then the
	 *            input file name is used as the prefix.
	 * @param lineCountPerSplit
	 *            The number of lines in each output file.
	 * 
	 * @throws IOException
	 *             Thrown if an I/O error occurs.
	 * @throws InterruptedException
	 */
	public static void splitByOutputFileSize(File inputFile, File outputDir, String outputFilePrefix,
			int lineCountPerSplit) throws IOException, InterruptedException
	{
		// Wait until it is done.
		String command = "split -l " + lineCountPerSplit + " " + inputFile.getAbsolutePath() + " " + outputFilePrefix;
		if (isVerbose) {
			System.out.println(command);
		}
		ProcessBuilder pb = new ProcessBuilder("split", "-l", Integer.toString(lineCountPerSplit), inputFile.getAbsolutePath(), outputFilePrefix);
		pb.directory(outputDir);
		Process proc = pb.start();
		if (isVerbose) {
			FileUtil.printProcess(proc);
		}
		proc.waitFor();
	}

	/**
	 * Splits the input file into small files that are equal in size. The number
	 * of small files is specified by the argument outputFileCount. Note that in
	 * order to determine the input file size, it first scans the entire input
	 * file. Therefore, this method is slower than
	 * {@link #splitByOutputFileSize(File, File, String, int)}, which creates
	 * output files based on the specified output file size (lineCountPerSplit).
	 * 
	 * @param inputFile
	 *            The input file to split.
	 * @param outputDir
	 *            The output directory. If null, the output files are created in
	 *            the working directory.
	 * @param outputFilePrefix
	 *            The output file prefix. All output files are enumerated with
	 *            the suffix "#" starting from 1. If null or empty, then the
	 *            input file name is used as the prefix.
	 * @param outputFileCount
	 *            The number of output files to create.
	 * @return Split files
	 * 
	 * @throws IOException
	 *             Thrown if an I/O error occurs.
	 * @throws InterruptedException
	 */
	public static File[] splitByOutputFileCount(File inputFile, File outputDir, final String outputFilePrefix,
			int outputFileCount) throws IOException, InterruptedException
	{
		if (inputFile == null) {
			throw new IOException("Input file is null. Unable to split file.");
		}
		if (inputFile.exists() == false) {
			throw new IOException("File does not exist: " + inputFile.getAbsolutePath());
		}
		// First, determine the line count by executing "wc -l"
		long lineCount = getLineCount(inputFile);

		int lineCountPerSplit = (int) (lineCount / outputFileCount);

		// Compensate for the remainder
		long remainder = lineCount % outputFileCount;
		if (remainder > 0) {
			lineCountPerSplit++;
		}

		// Split the file using the line count.
		// Wait until it is done.
		String command = "split -l " + lineCountPerSplit + " " + inputFile.getAbsolutePath() + " " + outputFilePrefix;
		if (isVerbose) {
			System.out.println(command);
		}
		ProcessBuilder pb = new ProcessBuilder("split", "-l", Integer.toString(lineCountPerSplit), inputFile.getAbsolutePath(), outputFilePrefix);
		pb.directory(outputDir);
		Process proc = pb.start();
		if (isVerbose) {
			FileUtil.printProcess(proc);
		}
		proc.waitFor();

		File files[] = outputDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name)
			{
				if (name.startsWith(outputFilePrefix)) {
					return true;
				}
				return false;
			}

		});
		return files;
	}

	/**
	 * Returns the number of lines in the file by executing "wc -l"
	 * 
	 * @param inputFile
	 *            The input file to read.
	 * 
	 * @throws IOException
	 *             Thrown if an I/O error occurs.
	 * @throws InterruptedException
	 */
	public static long getLineCount(File inputFile) throws IOException, InterruptedException
	{
		String path = inputFile.getAbsolutePath();
		ProcessBuilder pb = new ProcessBuilder("wc", "-l", path);
		Process proc = pb.start();
		InputStreamReader isr = new InputStreamReader(proc.getInputStream());
		BufferedReader reader = new BufferedReader(isr);
		String line;
		long lineCount = -1;
		while ((line = reader.readLine()) != null) {
			String split[] = line.split(" ");
			if (split.length > 0) {
				for (String string : split) {
					String val = string.trim();
					if (val.length() > 0) {
						lineCount = Long.parseLong(val);
						break;
					}
				}
			}
			if (lineCount != -1) {
				break;
			}
		}
		reader.close();
		proc.waitFor();
		return lineCount;
	}

	/**
	 * Deploys files to individual servers.
	 * 
	 * @param inputDir
	 *            The input directory that contains files to be deployed. This
	 *            directory is relative to the server root directory.
	 * @param filePrefix
	 *            The file prefix. All of files that begin with this prefix in
	 *            the inputDir directory are deployed.
	 * @param commandDir
	 *            The directory that contains the deploy_data_files command. If
	 *            null, then "bin_sh/tools" is assumed.
	 * @param hostNames
	 *            List of host names separated by space.
	 * @param serverNames
	 *            List of server names separated by space.
	 * @throws IOException
	 *             Thrown if an I/O error occurs.
	 * @throws InterruptedException
	 */
	public static void deployFilesToServers(File inputDir, String filePrefix, File commandDir, String hostNames,
			String serverNames) throws IOException, InterruptedException
	{
		if (commandDir == null) {
			commandDir = new File("../../bin_sh/tools");
		}

		String command[] = new String[] { DEPLOY_COMMAND, inputDir.getAbsolutePath(), "-prefix", filePrefix, "-hosts",
				hostNames, "-servers", serverNames };
		if (isVerbose) {
			System.out.println("Command directory=" + commandDir.getAbsolutePath());
			for (String string : command) {
				System.out.print(string + " ");
			}
			System.out.println();
		}
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(commandDir);
		Process proc = pb.start();

		if (isVerbose) {
			FileUtil.printProcess(proc);
		}

		proc.waitFor();
	}

	public static boolean isVerbose()
	{
		return isVerbose;
	}

	public static void setVerbose(boolean isVerbose)
	{
		FileUtilUnix.isVerbose = isVerbose;
	}
}