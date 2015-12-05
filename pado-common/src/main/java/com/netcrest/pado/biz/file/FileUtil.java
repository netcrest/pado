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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;

/**
 * FileUtil provides general file utility methods for manipulating file size,
 * contents, etc.
 * 
 * @author dpark
 * 
 */
public class FileUtil
{
	private static boolean isVerbose = Boolean.getBoolean(PadoUtil.getSystemPropertyName(Constants.PROP_LOADER_DATA_VERBOSE));
	private final static String DEPLOY_COMMAND = "deploy_data_files_input";

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
	 *            the suffix "_#" starting from 1. If null or empty, then the
	 *            input file name is used as the prefix.
	 * @param lineCountPerSplit
	 *            The number of lines in each output file.
	 * 
	 * @throws IOException
	 *             Thrown if an I/O error occurs.
	 */
	public static void splitByOutputFileSize(File inputFile, File outputDir, String outputFilePrefix,
			int lineCountPerSplit) throws IOException
	{
		if (lineCountPerSplit <= 0) {
			return;
		}

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		if (outputFilePrefix != null) {
			outputFilePrefix = outputFilePrefix.trim();
		}
		if (outputFilePrefix == null || outputFilePrefix.length() == 0) {
			outputFilePrefix = inputFile.getName();
		}
		String line;
		long lineCount = 0;
		int fileCount = 0;
		BufferedWriter writer = null;
		while ((line = reader.readLine()) != null) {
			if (lineCount % lineCountPerSplit == 0) {
				if (writer != null) {
					writer.close();
				}
				fileCount++;
				String fileName = outputFilePrefix + fileCount;
				File outputFile = new File(outputDir, fileName);
				writer = new BufferedWriter(new FileWriter(outputFile));
			}
			writer.write(line);
			writer.newLine();
			lineCount++;
		}
		if (writer != null) {
			writer.close();
		}
		reader.close();
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
	 *            the suffix "_#" starting from 1. If null or empty, then the
	 *            input file name is used as the prefix.
	 * @param outputFileCount
	 *            The number of output files to create. If <= 0, it assumes 1.
	 * 
	 * @throws IOException
	 *             Thrown if an I/O error occurs.
	 */
	public static void splitByOutputFileCount(File inputFile, File outputDir, String outputFilePrefix,
			int outputFileCount) throws IOException
	{
		// outputFileCount cannot be <=0. If so, use 1.
		if (outputFileCount <= 0) {
			outputFileCount = 1;
		}
		
		long lineCount = getLineCount(inputFile);
		int lineCountPerSplit = (int) (lineCount / outputFileCount);

		// Compensate for the remainder
		long remainder = lineCount % outputFileCount;

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		if (outputFilePrefix != null) {
			outputFilePrefix = outputFilePrefix.trim();
		}
		if (outputFilePrefix == null || outputFilePrefix.length() == 0) {
			outputFilePrefix = inputFile.getName();
		}
		String line;
		lineCount = 0;
		int fileCount = 0;
		BufferedWriter writer = null;
		while ((line = reader.readLine()) != null) {
			if (lineCountPerSplit == 0 || lineCount % lineCountPerSplit == 0) {
				if (writer != null) {
					// Add one more line until the remainder reaches 0.
					// Note that lineCount is not incremented in order
					// to keep the modulo function intact.
					if (remainder > 0) {
						writer.write(line);
						writer.newLine();
						remainder--;
						line = reader.readLine();
					}
					writer.close();
					if (line == null) {
						break;
					}
				}
				fileCount++;
				String fileName = outputFilePrefix + fileCount;
				File outputFile = new File(outputDir, fileName);
				writer = new BufferedWriter(new FileWriter(outputFile));
			}
			writer.write(line);
			writer.newLine();
			lineCount++;
		}
		if (writer != null) {
			writer.close();
		}
		reader.close();
	}

	/**
	 * Returns the number of lines in the file.
	 * 
	 * @param inputFile
	 *            The input file to read.
	 * 
	 * @throws IOException
	 *             Thrown if an I/O error occurs.
	 */
	public static long getLineCount(File inputFile) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		long lineCount = 0;
		while (reader.readLine() != null) {
			lineCount++;
		}
		reader.close();
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
	 * @throws IOException
	 *             Thrown if an I/O error occurs.
	 * @throws InterruptedException
	 */
	public static void deployFilesToServersWindowsLocalHost(File inputDir, String filePrefix, File commandDir,
			String serverNames) throws IOException, InterruptedException
	{
		if (commandDir == null) {
			commandDir = new File("../../bin_win/tools");
		}
		String command[] = new String[] { "cmd.exe", "/C", DEPLOY_COMMAND, inputDir.getAbsolutePath(), "-prefix", filePrefix,
				"-servers", serverNames };

		if (isVerbose) {
			System.out.println("Command directory=" + commandDir.getAbsolutePath());
			System.out.print("Command=");
			for (String string : command) {
				System.out.print(string + " ");
			}
			System.out.println();
		}
		Process proc = Runtime.getRuntime().exec(
				command, null, commandDir);

		if (isVerbose) {
			printProcess(proc);
		}

		proc.waitFor();
	}
	
	public static boolean isVerbose()
	{
		return isVerbose;
	}

	public static void setVerbose(boolean isVerbose)
	{
		FileUtil.isVerbose = isVerbose;
	}
	
	public static void printProcess(Process proc) throws IOException
	{
		InputStreamReader isr = new InputStreamReader(proc.getInputStream());
		BufferedReader reader = new BufferedReader(isr);
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
	}
}