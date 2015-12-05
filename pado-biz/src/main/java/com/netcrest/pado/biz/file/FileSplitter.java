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
 * FileSplitter splits a large file into smaller files by an output file size or
 * a number of output files.
 * 
 * @author dpark
 * 
 */
public class FileSplitter
{
	private static void usage()
	{
		System.out.println();
		System.out.println("FileSplitter splits a file into smaller files. A file");
		System.out.println("can be split by a number of lines per output file (-l)");
		System.out.println("or a number of output files (-f). Note that the latter");
		System.out.println("option (-f) scans the input file first to determine");
		System.out.println("the file size. Therefore, it will be slower than the");
		System.out.println("former option (-l).");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("   FileSplitter <input file path>");
		System.out.println("              [-l <line count> | -f <output file count>]");
		System.out.println("              [-prefix <output file prefix>]");
		System.out.println("              [-dir <output dir>] [-?]");
		System.out.println("   Default: FileSplitter -l 10000 -dir .");
		System.out.println();
		System.out.println("   -l <line count> The number of lines in each output file.");
		System.out.println("   -f <output file count> The number of output files.");
		System.out.println("   -prefix <output file prefix> Output file prefix. Output files");
		System.out.println("                are enumerated with the suffix _# starting from 1.");
		System.out.println("   -dir <output dir> The output directory in which the output files");
		System.out.println("                are created.");
		System.out.println();
		System.exit(0);
	}

	public static void main(String[] args) throws Exception
	{
		String inputFilePath = null;
		int lineCountPerSplit = 10000;
		int outputFileCount = -1;
		String outputDirPath = ".";
		String outputFilePrefix = null;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-dir")) {
				if (i < args.length - 1) {
					outputDirPath = args[++i];
				}
			} else if (arg.equals("-prefix")) {
				if (i < args.length - 1) {
					outputFilePrefix = args[++i];
				}
			} else if (arg.equals("-l")) {
				if (i < args.length - 1) {
					lineCountPerSplit = Integer.parseInt(args[++i]);
				}
			} else if (arg.equals("-f")) {
				if (i < args.length - 1) {
					outputFileCount = Integer.parseInt(args[++i]);
				}
			} else {
				inputFilePath = arg;
			}
		}

		if (inputFilePath == null) {
			System.err.println();
			System.err.println("Input file not specified.");
			System.err.println();
			System.exit(-1);
		}
		File inputFile = new File(inputFilePath);
		if (inputFile.exists() == false) {
			System.err.println();
			System.err.println("Input file does not exist: " + inputFilePath);
			System.err.println();
			System.exit(-1);
		}
		File outputDir = new File(outputDirPath);
		if (outputDir.exists() == false) {
			System.err.println();
			System.err.println("Output diretory does not exist: " + outputDirPath);
			System.err.println();
			System.exit(-1);
		}
		if (outputDir.isDirectory() == false) {
			System.err.println();
			System.err.println(outputDirPath + " is not a directory.");
			System.err.println();
			System.exit(-1);
		}

		System.out.println();
		System.out.println("File splitting. Please wait.");
		long startTime = System.currentTimeMillis();
		if (outputFileCount == -1) {
			FileUtil.splitByOutputFileSize(inputFile, outputDir, outputFilePrefix, lineCountPerSplit);
		} else {
			FileUtil.splitByOutputFileCount(inputFile, outputDir, outputFilePrefix, outputFileCount);
		}
		long endTime = System.currentTimeMillis();
		long delta = endTime - startTime;
		long seconds = delta / 1000;
		if (seconds != 0) {
			System.out.println("Elapsed time: " + seconds + " sec.");
		} else {
			System.out.println("Elapsed time: " + delta + " msec.");
		}
		System.out.println("File split complete.");
		System.out.println();
	}

}