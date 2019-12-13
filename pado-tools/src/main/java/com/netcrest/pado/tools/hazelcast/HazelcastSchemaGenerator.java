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
package com.netcrest.pado.tools.hazelcast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.internal.util.SchemaUtil;
import com.netcrest.pado.internal.util.SchemaUtil.SchemaProp;

/**
 * HazelcastSchemaGenerator is a client program that generates schema files for
 * all data files found in the specified data directory. The generated schema
 * files may need to be manually edited to include the correct grid paths and
 * primary keys.
 * 
 * @author dpark
 *
 */
public class HazelcastSchemaGenerator {
	public void generateSchemaFiles(File dataDir, File schemaDir, String parentPath, int headerRow, int startRow)
			throws IOException {
		// Check the parent path
		if (parentPath != null) {
			if (parentPath.startsWith("/")) {
				if (parentPath.length() > 1) {
					parentPath = parentPath.substring(1);
				} else {
					throw new PadoException("Error: Invalid parent path: " + parentPath);
				}
			}
		}

		// Generate schema files
		final File[] csvFiles = dataDir.listFiles();
		if (csvFiles == null || csvFiles.length == 0) {
			writeLine("Data files not found: " + dataDir.getAbsolutePath());
			return;
		}
		if (schemaDir.exists() == false) {
			schemaDir.mkdirs();
		}
		List<File> csvFileList = new ArrayList<File>(Arrays.asList(csvFiles));
		if (csvFileList.size() > 0) {
			SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			writeLine("Data Dir: " + dataDir.getAbsolutePath());
			Iterator<File> iterator = csvFileList.iterator();
			while (iterator.hasNext()) {
				writeLine("   " + iterator.next().getName());
			}
			writeLine("Schema Dir: " + schemaDir.getAbsolutePath());
			iterator = csvFileList.iterator();
			while (iterator.hasNext()) {
				File csvFile = iterator.next();
				String fileName = csvFile.getName();
				int index = fileName.lastIndexOf(".");
				String generatedSchemaFileName;
				String filePrefix;
				if (index > 0) {
					filePrefix = fileName.substring(0, index);
				} else {
					filePrefix = fileName;
				}

				// Replace '-' and space with underscore
				String gridPath = filePrefix.replaceAll("[\\- ]", "_");
				gridPath = gridPath.toLowerCase();
				if (parentPath != null) {
					gridPath = parentPath + "/" + gridPath;
				}
				String className = Character.toUpperCase(filePrefix.charAt(0)) + filePrefix.substring(1);
				generatedSchemaFileName = filePrefix + ".schema";
				File schemaFilePath = new File(schemaDir, generatedSchemaFileName);
				InputStream is = this.getClass().getClassLoader()
						.getResourceAsStream("com/netcrest/pado/tools/hazelcast/HazelcastSchemaTemplate.txt");
				if (is == null) {
					throw new IOException(
							"com/netcrest/pado/tools/SchemaTemplate.txt not found. A correct version of Pado required. Schema files generation aborted.");
				}
				try {
					String schemaStr = SchemaUtil.readFile(is);
					SchemaProp sp = SchemaUtil.determineSchemaProp(csvFile, headerRow);
					if (startRow > 0) {
						sp.startRow = startRow;
					}
					schemaStr = schemaStr.replaceAll("\\$\\{GRID_PATH\\}", gridPath);
					schemaStr = schemaStr.replaceAll("\\$\\{IS_KEY_AUTO_GEN\\}", "false");
					schemaStr = schemaStr.replaceAll("\\$\\{IS_TEMPORAL\\}", "false");
					schemaStr = schemaStr.replaceAll("\\$\\{DELIMITER\\}", sp.delimiter);
					if (sp.delimiter.equals(",")) {
						schemaStr = schemaStr.replaceAll("\\$\\{DELIMITER_COMMENT\\}", sp.delimiter);
					} else {
						schemaStr = schemaStr.replaceAll("\\$\\{DELIMITER_COMMENT\\}", "tab");
					}
					schemaStr = schemaStr.replaceAll("\\$\\{START_ROW\\}", Integer.toString(sp.startRow));
					schemaStr = schemaStr.replaceAll("\\$\\{FIELDS\\}", sp.fieldNames);
					schemaStr = schemaStr.replaceAll("\\$\\{CLASS_NAME\\}", className);

					// Write schema file
					FileWriter schemaFileWriter = new FileWriter(schemaFilePath);
					try {
						schemaFileWriter.write("## ============================================================\n");
						schemaFileWriter.write("## Generated: " + timeFormat.format(new Date()) + "\n");
						schemaFileWriter.write("## Data File: " + csvFile.getName() + "\n");
						schemaFileWriter.write("## This file was generated by \"import_csv -schema\" as follows:\n");
						schemaFileWriter.write("##    IsKeyAutoGen=false\n");
						schemaFileWriter.write("##    Delimiter=" + sp.delimiter + "\n");
						schemaFileWriter.write("##    IsTemporal=false\n");
						schemaFileWriter.write("##    TemporalType=eternal\n");
						schemaFileWriter.write("##    StartRow=" + sp.startRow + "\n");
						schemaFileWriter.write(
								"##    <Field names extracted from the data file on line " + sp.headerLineNum + ">\n");
						schemaFileWriter.write("## ============================================================\n");
						schemaFileWriter.write("\n");
						schemaFileWriter.write(schemaStr);
						writeLine("   Generated: " + schemaFilePath.getName());
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

			} // while

			writeLine();
			writeLine("Please note that all generated schema files are configured to auto-generate keys in the grid.");
			writeLine("This means data will always be inserted and no updates will occur. Furthermore, all fields");
			writeLine(
					"default to the String type. To load data using the generated schema files, follow the steps below:");
			writeLine();
			writeLine("   1. Edit the generated schema files to include the correct grid paths and primary keys.");
			writeLine(
					"   2. Create the grid paths specified in the generated schema files using PadoShell or editing pado.xml.");
			writeLine("   3. Move the generated schema files to the schema directory, i.e., data/schema.");
			writeLine("   4. Move the data files to the import directory, i.e., data/import");
			writeLine("   5. Change directory to bin_sh/tools and run 'import_csv' to load the data.");
			writeLine();
		}
	}

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	private static void write(String str) {
		System.out.print(str);
	}

	private static void usage() {
		String padoHome = System.getenv("PADO_HOME");
		if (padoHome == null) {
			padoHome = "$PADO_HOME";
		}
		writeLine();
		writeLine("Usage:");
		writeLine("   HazelcastSchemaGenerator [-dataDir <data file directory>]");
		writeLine("                            [-schemaDir <schema file directory>]");
		writeLine("                            [-parentPath <parent grid path>]");
		writeLine("                            [-headerRow <column name row number>]");
		writeLine("                            [-startRow <start row number]");
		writeLine("                            [-?]");
		writeLine();
		writeLine("   IMPORTANT: This command overwrites the existing schema files. Make sure");
		writeLine("              to back up output directory (-schemaDir) before executing");
		writeLine("              this program.");
		writeLine();
		writeLine("   Generates schema files for all data files found in the specified data directory.");
		writeLine("   The directory paths can be absolute or relative to the directory ");
		writeLine("   " + padoHome);
		writeLine();
		writeLine("      -dataDir    Data file directory. Schema files are generated for all of data files");
		writeLine("                  found in this directory. Default: data/import");
		writeLine("      -schemaDir  Schema file directory to which schema files are generated.");
		writeLine("                  Default: data/schema/generated");
		writeLine("      -parentPath Parent grid path. It must not begin with '/' and include all nested");
		writeLine("                  paths starting from the top path of the grid excluding the root path");
		writeLine("                  assigned to the grid ID.");
		writeLine("      -headerRow  Column header row number in the data file. If -1 or unspecified then");
		writeLine("                  it is automatically determined.");
		writeLine("      -startRow   Start row number in the data file. If -1 or unspecified then the row");
		writeLine("                  immediately after the header row is assigned.");
		writeLine();
		writeLine(
				"   Default: SchemaGenerator -dataDir data/import -schemaDir data/schema/generated -headerRow -1 -startRow -1");
		writeLine();
		System.exit(0);
	}

	/**
	 * @param args
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, URISyntaxException {
		String arg;
		String dataDirPath = "data/import";
		String schemaDirPath = "data/schema/generated";
		String parentPath = null;
		int headerRow = -1;
		int startRow = -1;
		String tmp;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-dataDir")) {
				if (i < args.length - 1) {
					dataDirPath = args[++i];
				}
			} else if (arg.equals("-schemaDir")) {
				if (i < args.length - 1) {
					schemaDirPath = args[++i];
				}
			} else if (arg.equals("-parentPath")) {
				if (i < args.length - 1) {
					parentPath = args[++i];
				}
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
			}
		}
		File schemaDir = new File(schemaDirPath);
		File dataDir = new File(dataDirPath);
		HazelcastSchemaGenerator sg = new HazelcastSchemaGenerator();
		sg.generateSchemaFiles(dataDir, schemaDir, parentPath, headerRow, startRow);
	}

}
