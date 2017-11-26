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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.netcrest.pado.biz.mon.ISysBiz;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;

public class deploy implements ICommand {
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
		options.addOption("i", false, "Ignore invalid file paths");
	}

	@Override
	public void initialize(PadoShell padoShell) {
		this.padoShell = padoShell;
	}

	@Override
	public void help() {
		PadoShell.println("deploy <deployment-files> [-i] [-?]");
		PadoShell.println("   Deploys the specified jar or DNA distribution files to the grid.");
		PadoShell.println("      <deployment-files> Plugin jar and/or DNA distribution files.");
		PadoShell.println("          Files with the extension .jar are deployed to the plugins directory and");
		PadoShell.println("          files with the extension .zip are deployed to the DNA desiginated");
		PadoShell.println("          directory in the grid. Zip files must conform to directory structure");
		PadoShell.println("          described below.");
		PadoShell.println("      -i  If specified, ignore invalid file paths, otherwise, if any of the");
		PadoShell.println("          specified files are invalid then it aborts the deploy command and");
		PadoShell.println("          none of the files are deployed.");
		PadoShell.println();
		PadoShell.println("DNA zip file structure:");
		PadoShell.println("    python - Python source code in the root directory. It is recommended that");
		PadoShell.println("             the zip file name should end with '-py.zip'");
		PadoShell.println("             Example:");
		PadoShell.println("                some-dna-py.zip");
		PadoShell.println("                      . ");
		PadoShell.println("                      |-- com");
		PadoShell.println("                      |   |-- foo");
		PadoShell.println("                      |   |   |-- dna");
		PadoShell.println("                      |   |   |   |-- helloworld.py");
		PadoShell.println();
		PadoShell.println("    java - Java jar files in the root directory. It is recommended that");
		PadoShell.println("           the zip file name should end with '-java.zip'");
		PadoShell.println("           Example:");
		PadoShell.println("                some-dna-java.zip");
		PadoShell.println("                      . ");
		PadoShell.println("                      |-- helloworld.jar");
		PadoShell.println();
		PadoShell.println("Examples:");
		PadoShell.println("   deploy /tmp/some-biz.jar /tmp/demo-biz.jar /tmp/some-dna.zip");
		PadoShell.println(
				"   deploy C:\\tmp\\some-biz.jar C:\\tmp\\demo-biz.jar C:\\tmp\\some-dna-py.zip  C:\\tmp\\some-dna-java.zip");
	}

	@Override
	public String getShortDescription() {
		return "Deploy plugin jars and/or DNA distribution files to the grid.";
	}

	@Override
	public boolean isLoginRequired() {
		return true;
	}

	@Override
	public Options getOptions() {
		return options;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void run(CommandLine commandLine, String command) throws Exception {
		List<String> argList = commandLine.getArgList();
		if (argList.size() == 1) {
			PadoShell.printlnError(this, "File path(s) not specified.");
			return;
		}

		HashSet<File> fileSet = new HashSet<File>(10);
		for (int i = 1; i < argList.size(); i++) {
			String filePath = (String) argList.get(i);
			File file = new File(filePath);
			fileSet.add(file);
		}
		if (commandLine.hasOption('i') == false) {
			List<File> notExistFileList = new ArrayList<File>(fileSet.size());
			for (File file : fileSet) {
				if (file.exists() == false) {
					notExistFileList.add(file);
				}
			}
			if (notExistFileList.size() > 0) {
				PadoShell.printlnError(this, "File(s) not exist: " + notExistFileList);
				return;
			}
		}
		
		List<File> jarFileList = new ArrayList<File>(fileSet.size());
		List<File> zipTarFileList = new ArrayList<File>(fileSet.size());
		for (File file : fileSet) {
			if (file.isDirectory()) {
				PadoShell.printlnError(this, "Invalid file. File cannot be directory: " + file );
				return;
			}
			if (file.getName().endsWith(".jar")) {
				jarFileList.add(file);
			} else if (file.getName().endsWith(".zip") || file.getName().endsWith(".tar") || file.getName().endsWith(".tar.gz")) {
				zipTarFileList.add(file);
			} else {
				PadoShell.printlnError(this, "Invalid file extension: " + file + ". Supported file extensions are .jar, .zip, .tar, .tar.gz");
				return;
			}
		}
		
		// Deploy Plugins
		deployPlugins(jarFileList);
		
		
		// Deploy DNAs
		deployDnas(zipTarFileList);
	}
	
	private void deployPlugins(List<File> jarFileList)
	{
		ISysBiz sysBiz = SharedCache.getSharedCache().getPado().getCatalog()
				.newInstance(ISysBiz.class);
		long serverTime = sysBiz.getCurrentTimeMillis();
		
		String jarNames[] = new String[jarFileList.size()];
		byte[][] payloadBuffers = new byte[jarFileList.size()][];
		int i =0;
		for (File file : jarFileList) {
			jarNames[i] = file.getName();
			try {
				payloadBuffers[i] = readBinary(file);
			} catch (Exception ex) {
				PadoShell.printlnError(this, "Error occurred while loading jar file: " + ex.getClass().getName() + " - " + ex.getMessage());
				return;
			}
			
			// Deploy the jar files
			sysBiz.deployJars(jarNames, payloadBuffers, new Date(serverTime));
			PadoShell.println(this, file + " - deployed");
			i++;
		}
	}
	
	private void deployDnas(List<File> zipTarFileList)
	{
		ISysBiz sysBiz = SharedCache.getSharedCache().getPado().getCatalog()
				.newInstance(ISysBiz.class);
		long serverTime = sysBiz.getCurrentTimeMillis();
		
		String distNames[] = new String[zipTarFileList.size()];
		byte[][] payloadBuffers = new byte[zipTarFileList.size()][];
		int i =0;
		for (File file : zipTarFileList) {
			distNames[i] = file.getName();
			try {
				payloadBuffers[i] = readBinary(file);
			} catch (Exception ex) {
				PadoShell.printlnError(this, "Error occurred while loading distribution file: " + ex.getClass().getName() + " - " + ex.getMessage());
				return;
			}
			
			// Deploy the jar files
			sysBiz.deployDnas(distNames, payloadBuffers, new Date(serverTime));
			i++;
		}
	}
	

	private static int BUFFER_SIZE = 10000;
	
	private byte[] readBinary(File file) throws FileNotFoundException, IOException
	{
		FileInputStream fis = new FileInputStream(file);
		ArrayList<byte[]> byteList = new ArrayList<byte[]>();
		int bytesRead;
		int lastBytesRead = 0;
		byte buffer[];
		do {
			buffer = new byte[BUFFER_SIZE];
			bytesRead = fis.read(buffer);
			if (bytesRead != -1) {
				lastBytesRead = bytesRead;
				byteList.add(buffer);
			}
		} while (bytesRead != -1);
		fis.close();

		int lastIndex = byteList.size() - 1;
		int bufferLength = lastIndex * BUFFER_SIZE + lastBytesRead;
		int destPos = 0;
		buffer = new byte[bufferLength];
		for (int j = 0; j < lastIndex; j++) {
			byte srcBuffer[] = byteList.get(j);
			destPos = j * BUFFER_SIZE;
			System.arraycopy(srcBuffer, 0, buffer, destPos, srcBuffer.length);
		}
		if (lastIndex >= 0) {
			byte srcBuffer[] = byteList.get(lastIndex);
			destPos = lastIndex * BUFFER_SIZE;
			System.arraycopy(srcBuffer, 0, buffer, destPos, lastBytesRead);
		}

		return buffer;
	}
}
