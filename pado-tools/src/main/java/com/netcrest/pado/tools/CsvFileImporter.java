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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalAdminBiz;
import com.netcrest.pado.biz.file.CsvFileLoader;
import com.netcrest.pado.biz.file.FileUtilUnix;
import com.netcrest.pado.biz.file.SchemaInfo;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.internal.security.AESCipher;
import com.netcrest.pado.internal.util.SchemaUtil;
import com.netcrest.pado.internal.util.SchemaUtil.SchemaProp;
import com.netcrest.pado.log.Logger;

public class CsvFileImporter
{
	private Properties csvProperties = new Properties();
	private IPado pado;

	public CsvFileImporter() throws PadoLoginException, IOException
	{
		init();
		login();
	}

	public CsvFileImporter(Properties csvProperties)
	{
		this.csvProperties = csvProperties;
		login();
	}

	public CsvFileImporter(IPado pado) throws IOException
	{
		this.pado = pado;
		init();
	}

	private void init() throws IOException
	{
		String csvPropertiesPath = System.getProperty("pado.csv.properties");
		File file = new File(csvPropertiesPath);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			csvProperties.load(reader);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private void login() throws PadoLoginException
	{
		String locators = System.getProperty("pado.locators", "localhost:20000");
		csvProperties.setProperty("locators", locators);

		String appId = csvProperties.getProperty("appid");
		String userName = csvProperties.getProperty("username");
		String passwd = csvProperties.getProperty("password");

		if (passwd != null) {
			if (userName == null) {
				passwd = null;
			} else {
				try {
					passwd = AESCipher.decryptUserTextToText(passwd);
				} catch (Exception ex) {
					throw new PadoLoginException("Invalid password.", ex);
				}
			}
		}
		Pado.connect(locators, false);
		pado = Pado.login(appId, appId, userName, passwd.toCharArray());
	}

	public void close()
	{
		if (pado != null) {
			if (pado.isLoggedOut() == false) {
				pado.logout();
			}
			if (Pado.isClosed() == false) {
				Pado.close();
			}
		}
	}

	/**
	 * 
	 * @param isTemporalOffWhileImportingData
	 *            true to temporarily disable temporal indexing before importing
	 *            data. Upon completion, it enables temporal indexing.
	 * @param isGenerateSchema
	 *            true to generate schema files for CSV files that do not have
	 *            corresponding schema files.
	 * @param headerRow
	 *            Column header row number. Row number begins from 1. -1 then
	 *            the header row is determined. This argument is meaningful only
	 *            if isGenerateSchema is true.
	 * @param startRow
	 *            Start row. If -1, then the row immediately after the header
	 *            row is assigned as the start row. This argument is meaningful
	 *            only if isGenerateSchema is true.
	 * @param isVerbose
	 *            true to print detailed progress information.
	 * @throws IOException
	 *             Thrown if unable to load file(s)
	 * @throws InterruptedException
	 *             Thrown if Linux file manipulation or parallel loading threads
	 *             get interrupted.
	 */
	public void importData(boolean isTemporalOffWhileImportingData, boolean isGenerateSchema, int headerRow,
			int startRow, boolean isVerbose) throws IOException, InterruptedException
	{
		String importDirString = csvProperties.getProperty("dir.import", "data/import");
		String schemaDirString = csvProperties.getProperty("dir.schema", "data/schema");
		String splitDirString = csvProperties.getProperty("dir.split", "data/split");
		String processedDirString = csvProperties.getProperty("dir.processed", "data/processed");
		String errorDirString = csvProperties.getProperty("dir.error", "data/error");
		int threadCount = Integer
				.parseInt(csvProperties.getProperty("threads", Runtime.getRuntime().availableProcessors() + ""));
		int rowCountPerThread = Integer.parseInt(csvProperties.getProperty("rowsPerThread", "100000"));
		File importDir = new File(importDirString);
		File schemaDir = new File(schemaDirString);
		File splitDir = new File(splitDirString);
		File processedDir = new File(processedDirString);
		File errorDir = new File(errorDirString);
		DecimalFormat decimalFormat = new DecimalFormat("#,###");
		int processedFileCount = 0;
		int errorFileCount = 0;
		int totalEntryCount = 0;

		// makd dirs
		importDir.mkdirs();
		schemaDir.mkdirs();
		splitDir.mkdirs();
		processedDir.mkdirs();
		errorDir.mkdirs();

		ExecutorService es = Executors.newFixedThreadPool(threadCount, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "Pado-CsvFileImporter");
                t.setDaemon(true);
                return t;
            }
        });

		File[] files = importDir.listFiles();
		if (files == null) {
			return;
		}

		final String fileExt = ".schema";

		final File schemaFiles[] = schemaDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File schemaFile)
			{
				return schemaFile.getName().endsWith(fileExt);
			}
		});
		final List<SchemaFilePrefixPair> schemaFilePrefixPairList = new ArrayList(schemaFiles.length);
		for (File file : schemaFiles) {
			schemaFilePrefixPairList.add(new SchemaFilePrefixPair(file));
		}

		final File[] csvFiles = importDir.listFiles();
		final List<FilePair> filePairList = new ArrayList<FilePair>(csvFiles.length);
		List<File> csvFileList = new ArrayList<File>(Arrays.asList(csvFiles));
		Collections.sort(csvFileList);
		Iterator<File> iterator = csvFileList.iterator();
		while (iterator.hasNext()) {
			File csvFile = iterator.next();
			for (SchemaFilePrefixPair schemaFilePrefixPair : schemaFilePrefixPairList) {
				if (csvFile.isFile() && csvFile.getName().startsWith(schemaFilePrefixPair.prefix)) {
					filePairList.add(new FilePair(schemaFilePrefixPair.schemaFile, csvFile));
					iterator.remove();
					break;
				}
			}
		}

		// Generate schema files if isGenerateSchema == true
		// Iterate all remaining CSV files and determine the schema file
		// properties.
		if (isGenerateSchema && csvFileList.size() > 0) {
			SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			writeLine("The schema files for the following data files are not found in the schema directory:");
			iterator = csvFileList.iterator();
			while (iterator.hasNext()) {
				writeLine("   " + iterator.next().getName());
			}
			writeLine("Generating schema files for the above data files...");
			iterator = csvFileList.iterator();
			while (iterator.hasNext()) {
				File csvFile = iterator.next();
				String fileName = csvFile.getName();
				int index = fileName.lastIndexOf(".");
				String generatedSchemaFileName;
				if (index > 0) {
					String filePrefix = fileName.substring(0, index);
					// Replace '-' and space with underscore
					String gridPath = filePrefix.replaceAll("[\\- ]", "_");
					gridPath = gridPath.toLowerCase();
					generatedSchemaFileName = filePrefix + ".schema";
					File schemaFilePath = new File(schemaDir, generatedSchemaFileName);
					InputStream is = this.getClass().getClassLoader()
							.getResourceAsStream("com/netcrest/pado/tools/SchemaTemplate.txt");
					if (is == null) {
						throw new IOException(
								"com/netcrest/pado/tools/SchemaTemplate.txt not found. Schema files generation aborted.");
					}
					try {
						String schemaStr = SchemaUtil.readFile(is);
						SchemaProp sp = SchemaUtil.determineSchemaProp(csvFile, headerRow);
						if (startRow > 0) {
							sp.startRow = startRow;
						}
						schemaStr = schemaStr.replaceAll("\\$\\{GRID_PATH\\}", gridPath);
						schemaStr = schemaStr.replaceAll("\\$\\{IS_KEY_AUTO_GEN\\}", "true");
						schemaStr = schemaStr.replaceAll("\\$\\{IS_TEMPORAL\\}", "true");
						schemaStr = schemaStr.replaceAll("\\$\\{DELIMITER\\}", sp.delimiter);
						if (sp.delimiter.equals(",")) {
							schemaStr = schemaStr.replaceAll("\\$\\{DELIMITER_COMMENT\\}", sp.delimiter);
						} else {
							schemaStr = schemaStr.replaceAll("\\$\\{DELIMITER_COMMENT\\}", "tab");
						}
						schemaStr = schemaStr.replaceAll("\\$\\{START_ROW\\}", Integer.toString(sp.startRow));
						schemaStr = schemaStr.replaceAll("\\$\\{FIELDS\\}", sp.fieldNames);

						// Write schema file
						FileWriter schemaFileWriter = new FileWriter(schemaFilePath);
						try {
							schemaFileWriter.write("## ============================================================\n");
							schemaFileWriter.write("## Generated: " + timeFormat.format(new Date()) + "\n");
							schemaFileWriter.write("## Data File: " + csvFile.getName() + "\n");
							schemaFileWriter
									.write("## This file was generated by \"import_csv -schema\" as follows:\n");
							schemaFileWriter.write("##    IsKeyAutoGen=true\n");
							schemaFileWriter.write("##    Delimiter=" + sp.delimiter + "\n");
							schemaFileWriter.write("##    IsTemporal=true\n");
							schemaFileWriter.write("##    TemporalType=eternal\n");
							schemaFileWriter.write("##    StartRow=" + sp.startRow + "\n");
							schemaFileWriter.write("##    <Field names extracted from the data file on line "
									+ sp.headerLineNum + ">\n");
							schemaFileWriter.write("## ============================================================\n");
							schemaFileWriter.write("\n");
							schemaFileWriter.write(schemaStr);
							writeLine("   Generated: " +  schemaFilePath.getName());
						} finally {
							if (schemaFileWriter != null) {
								schemaFileWriter.close();
							}
						}
					} catch (URISyntaxException e) {
						throw new IOException(e);
					} finally {
						is.close();
					}
				}
			}
		}

		Collections.sort(filePairList);

		writeLine();
		writeLine("Importing CSV files as follows:");
		writeLine("         Import Dir: " + importDir.getAbsolutePath());
		writeLine("      Processed Dir: " + processedDir.getAbsolutePath());
		writeLine("          Split Dir: " + splitDir.getAbsolutePath());
		writeLine("          Error Dir: " + errorDir.getAbsolutePath());
		writeLine("         File Count: " + filePairList.size());
		writeLine("   Thread Pool Size: " + threadCount);
		writeLine("    Rows Per Thread: " + decimalFormat.format(rowCountPerThread));
		writeLine();

		long totalStartTime = System.currentTimeMillis();
		for (int i = 0; i < filePairList.size(); i++) {
			FilePair filePair = filePairList.get(i);
			File schemaFile = filePair.schemaFile;
			File csvFile = filePair.csvFile;

			writeLine(i + 1 + ". " + csvFile.getAbsolutePath());

			// schemaInfo1 is for the first split file
			// schemaInfo2 is for the rest of the split files.
			SchemaInfo schemaInfo1;
			SchemaInfo schemaInfo2;
			try {
				schemaInfo1 = new SchemaInfo("file", schemaFile);
				schemaInfo2 = new SchemaInfo("file", schemaFile);
			} catch (Exception ex) {
				errorFileCount++;
				File errorCsvFile = new File(errorDir, csvFile.getName());
				csvFile.renameTo(errorCsvFile);
				Logger.error(ex);
				writeLineError("   " + ex.getMessage());
				writeLineError("   ***ERROR: Error encountered. See log file for details. Files moved to: ");
				writeLineError("   " + errorCsvFile.getAbsolutePath());
				continue;
			}
			schemaInfo2.setStartRow(0);

			// Load CSV files
			long startTime = System.currentTimeMillis();
			int threads = 1;
			if (schemaInfo1.isSplit() == false) {
				threads = 1;
			} else {
				long lineCount = FileUtilUnix.getLineCount(csvFile);
				threads = (int) lineCount / rowCountPerThread;
				if (threads == 0) {
					threads = 1;
				}
			}

			String outputFilePrefix = csvFile.getName();
			// Remove any files that begin with outputFilePrefix from
			// the split directory.
			removeFiles(splitDir, outputFilePrefix);

			File splitFiles[];
			ArrayList<ImportTask> taskList = new ArrayList<ImportTask>(threads);
			if (schemaInfo1.isSplit()) {
				int outputFileCount = threads;
				splitFiles = FileUtilUnix.splitByOutputFileCount(csvFile, splitDir, outputFilePrefix, outputFileCount);
			} else {
				splitFiles = new File[1];
				splitFiles[0] = new File(splitDir, csvFile.getName());
				copyFile(csvFile, splitFiles[0]);
			}

			// The first split file may include the header rows but the
			// rest of the split files do not.
			if (splitFiles.length > 0) {
				taskList.add(new ImportTask(1, schemaInfo1, splitFiles[0], isTemporalOffWhileImportingData, isVerbose));
			}

			// The rest split files do not contain the header rows.
			for (int j = 1; j < splitFiles.length; j++) {
				taskList.add(
						new ImportTask(j + 1, schemaInfo2, splitFiles[j], isTemporalOffWhileImportingData, isVerbose));
			}

			List<Future<ImportStatus>> futureList = es.invokeAll(taskList);
			int count = 0;
			int index = 0;
			boolean isError = false;
			for (Future<ImportStatus> future : futureList) {
				ImportStatus is = null;
				try {
					is = future.get();
				} catch (Exception ex) {
					writeLineError("   Error: " + ex.getMessage());
					isError = true;
				} finally {
					if (is != null) {
						is.file.delete();
					}
				}
				if (is != null) {
					count += is.count;
				}
				if (is == null || is.isSuccess == false) {
					isError = true;
				}

				write("   " + ++index + ". ");
				if (is == null) {
					writeLine("No status");
				} else {
					writeLine(is.toString());
				}
			}

			totalEntryCount += count;

			write("     Path entry count: ");
			if (isError) {
				write(">=");
			}
			writeLine(decimalFormat.format(count));
			long elapsedTimeInSec = (System.currentTimeMillis() - startTime) / 1000;
			writeLine("   Time elapsed (sec): " + decimalFormat.format(elapsedTimeInSec));
			if (isError) {
				errorFileCount++;
				File errorCsvFile = new File(errorDir, csvFile.getName());
				csvFile.renameTo(errorCsvFile);
				writeLineError("   ***ERROR: Error encountered. Files moved to: ");
				writeLineError("   " + errorCsvFile.getAbsolutePath());
			} else {
				processedFileCount++;
				File processedCsvFile = new File(processedDir, csvFile.getName());
				csvFile.renameTo(processedCsvFile);
				writeLine("   SUCCESS: Files processed and moved to:");
				writeLine("      " + processedCsvFile.getAbsolutePath());
			}
		}
		writeLine();

		long totalEndTime = System.currentTimeMillis();
		long totalElapsedTimeSec = (totalEndTime - totalStartTime) / 1000;
		writeLine("          Total file count: " + filePairList.size());
		writeLine("Total processed file count: " + processedFileCount);
		writeLine("    Total error file count: " + errorFileCount);
		writeLine("    Total path entry count: " + decimalFormat.format(totalEntryCount));
		writeLine("  Total elapsed time (sec): " + decimalFormat.format(totalElapsedTimeSec));
		writeLine();

		es.shutdown();
	}

	private void removeFiles(File dir, final String filePrefix)
	{
		File files[] = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name)
			{
				return name.startsWith(filePrefix);
			}
		});

		if (files != null) {
			for (File file : files) {
				file.delete();
			}
		}
	}

	private void copyFile(File source, File dest) throws IOException
	{
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}

	class ImportTask implements Callable<ImportStatus>
	{
		int threadNum;
		SchemaInfo schemaInfo;
		File csvFile;
		boolean isTemporalOffWhileImportingData;
		boolean isVerbose;

		ImportTask(int threadNum, SchemaInfo schemaInfo, File csvFile, boolean isTemporalOffWhileImportingData,
				boolean isVerbose)
		{
			this.threadNum = threadNum;
			this.schemaInfo = schemaInfo;
			this.csvFile = csvFile;
			this.isTemporalOffWhileImportingData = isTemporalOffWhileImportingData;
			this.isVerbose = isVerbose;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public ImportStatus call() throws Exception
		{
			ImportStatus is = new ImportStatus();
			ITemporalAdminBiz temporalAdminBiz = null;
			boolean temporalWasEnabled = false;
			try {
				CsvFileLoader fileLoader = new CsvFileLoader(pado);
				fileLoader.setVerbose(isVerbose);
				fileLoader.setVerboseTag("Thread[" + threadNum + "]");

				// Disable temporal indexing temporarily during the import time
				// to speed up.
				if (isTemporalOffWhileImportingData) {
					if (schemaInfo.isHistory() == false) {
						temporalAdminBiz = pado.getCatalog().newInstance(ITemporalAdminBiz.class);
						temporalAdminBiz.setGridPath(schemaInfo.getGridPath());
						temporalWasEnabled = temporalAdminBiz.isEnabled();
						if (temporalWasEnabled) {
							// Block till done
							temporalAdminBiz.setEnabled(false, false /* spawnThread */);
						}
					}
				}

				// Load data
				int count = fileLoader.load(schemaInfo, csvFile);

				is.gridPath = schemaInfo.getGridPath();
				is.count = count;
				is.isSuccess = true;
				is.file = csvFile;
			} finally {
				// Enable temporal indexing if disabled earlier
				if (isTemporalOffWhileImportingData) {
					if (temporalAdminBiz != null && schemaInfo.isHistory() == false) {
						if (temporalWasEnabled) {
							// Block till done
							temporalAdminBiz.setEnabled(true, false /* spawnThread */);
						}
					}
				}
			}
			return is;
		}
	}

	class ImportStatus
	{
		String gridPath;
		int count;
		boolean isSuccess;
		File file;

		@Override
		public String toString()
		{
			return "ImportStatus [path=" + gridPath + ", count=" + count + ", isSuccess=" + isSuccess + ", file="
					+ file.getName() + "]";
		}
	}

	class SchemaFilePrefixPair
	{
		String prefix;
		File schemaFile;

		SchemaFilePrefixPair(File schemaFile)
		{
			this.schemaFile = schemaFile;
			int index = schemaFile.getName().lastIndexOf(".schema");
			prefix = schemaFile.getName().substring(0, index);
		}
	}

	class FilePair implements Comparable<FilePair>
	{
		File schemaFile;
		File csvFile;

		FilePair(File schemaFile, File csvFile)
		{
			this.schemaFile = schemaFile;
			this.csvFile = csvFile;
		}

		@Override
		public int compareTo(FilePair o)
		{
			return csvFile.getName().compareTo(o.csvFile.getName());
		}
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

	private static void writeLineError(String line)
	{
		System.err.println(line);
	}

	private static void writeError(String str)
	{
		System.err.print(str);
	}

	private static void writeLineError()
	{
		System.err.println();
	}

	private static void usage()
	{
		writeLine();
		writeLine("Usage:");
		writeLine("   CsvFileImporter [-temporal] [-verbose] [-?]");
		writeLine();
		writeLine("   Imports all CSV files found in the import directory. Each CSV file");
		writeLine("   must be paired with the schema file with the same name in the import");
		writeLine("   directory. For example, 'foo.csv' must be paired with 'foo.schema'.");
		writeLine();
		writeLine("      -temporal  Keep temporal indexing enabled while importing data.");
		writeLine("                 Default: temporal indexing is turned off while importing data in order to");
		writeLine("                          reduce the load time. Upon completion, it re-enables temporal");
		writeLine("                          which in turn rebuilds temporal indexes.");
		writeLine("      -schema    Generates schema files in the schema directory if the corresponding schema files");
		writeLine("                 do not exist.");
		writeLine("      -headerRow Column header row number in the data file. If -1 or unspecified then");
		writeLine("                 it is automatically determined. This option is only meaningful if -schema");
		writeLine("                 is specified.  Default: -1");
		writeLine("      -startRow  Start row number in the data file. If -1 or unspecified then the row");
		writeLine("                 immediately after the header row is assigned. This option is only meaningful");
		writeLine("                 if -schema is specified. Default: -1");
		writeLine("      -verbose   Prints additional import status.");
		writeLine();
		writeLine("   Default: CsvFileImporter");
		writeLine();
		System.exit(0);
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		String arg;
		boolean isVerbose = false;
		boolean isTemporalOffWhileImportingData = true;
		boolean isGenerateSchema = false;
		int headerRow = -1;
		int startRow = -1;
		String tmp;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-temporal")) {
				isTemporalOffWhileImportingData = false;
			} else if (arg.equals("-schema")) {
				isGenerateSchema = true;
			} else if (arg.equals("-headerRow")) {
				if (i < args.length - 1) {
					tmp = args[++i];
					headerRow = Integer.parseInt(tmp);
				}
			} else if (arg.equals("-startRow")) {
				if (i < args.length - 1) {
					tmp = args[++i];
					startRow = Integer.parseInt(tmp);
				}
			} else if (arg.equals("-verbose")) {
				isVerbose = true;
			}
		}
		CsvFileImporter importer = new CsvFileImporter();
		importer.importData(isTemporalOffWhileImportingData, isGenerateSchema, headerRow, startRow, isVerbose);
		importer.close();
	}

}
