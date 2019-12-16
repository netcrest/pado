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
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.SchemaUtil;
import com.netcrest.pado.internal.util.SchemaUtil.SchemaProp;
import com.netcrest.pado.log.ILogger;
import com.netcrest.pado.log.Logger;

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
	
	public HazelcastSchemaGenerator()
	{
		initLog();
	}
	
	private void initLog()
	{
		System.setProperty("pado." + Constants.PROP_CLASS_LOGGER, "com.netcrest.pado.hazelcast.util.HazelcastLogger");
	}
	
	public void generateSchemaFiles(File dataDir, File schemaDir, String parentPath, int headerRow, int startRow, String packageName)
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
					schemaStr = schemaStr.replaceAll("\\$\\{IS_KEY_AUTO_GEN\\}", "true");
					schemaStr = schemaStr.replaceAll("\\$\\{IS_TEMPORAL\\}", "false");
					schemaStr = schemaStr.replaceAll("\\$\\{DELIMITER\\}", sp.delimiter);
					if (sp.delimiter.equals("	")) {
						schemaStr = schemaStr.replaceAll("\\$\\{DELIMITER_COMMENT\\}", "tab");
					} else {
						schemaStr = schemaStr.replaceAll("\\$\\{DELIMITER_COMMENT\\}", sp.delimiter);
					}
					schemaStr = schemaStr.replaceAll("\\$\\{START_ROW\\}", Integer.toString(sp.startRow));
					schemaStr = schemaStr.replaceAll("\\$\\{FIELDS\\}", sp.fieldNames);
					if (packageName == null) {
						packageName = "";
					}
					if (packageName.length() > 0 && packageName.endsWith(".") == false) {
						packageName += ".";
					}
					schemaStr = schemaStr.replaceAll("\\$\\{PACKAGE_NAME\\}", packageName);
					schemaStr = schemaStr.replaceAll("\\$\\{CLASS_NAME\\}", className);

					// Write schema file
					FileWriter schemaFileWriter = new FileWriter(schemaFilePath);
					try {
						schemaFileWriter.write("## ============================================================\n");
						schemaFileWriter.write("## Generated: " + timeFormat.format(new Date()) + "\n");
						schemaFileWriter.write("## Data File: " + csvFile.getName() + "\n");
						schemaFileWriter.write("## This file was generated by \"import_csv -schema\" as follows:\n");
						schemaFileWriter.write("##    IsKeyAutoGen=true\n");
						schemaFileWriter.write("##    Delimiter=" + sp.delimiter + "\n");
						schemaFileWriter.write("##    IsTemporal=false\n");
						schemaFileWriter.write("##    TemporalType=eternal\n");
						schemaFileWriter.write("##    StartRow=" + sp.startRow + "\n");
						schemaFileWriter.write(
								"##    <Field names extracted from the data file on line " + sp.headerLineNum + ">\n");
						schemaFileWriter.write("## ============================================================\n");
						schemaFileWriter.write("\n");
						schemaFileWriter.write(schemaStr);
						writeLine("   Generated: " + schemaFilePath.getName() + " -- Header Row Assumed: " + sp.headerLineNum);
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
			writeLine("IMPORTANT");
			writeLine();
			writeLine("   Please note that the header row number detected for each schema file may not accurate.");
			writeLine("   It is important that you follow the steps shown below to ammend any discrepencies in");
			writeLine("   each schema file before importing data into the grid.");
			writeLine();
			writeLine("   1. Edit each generated schema file and correct the following paramenters as needed:");
			writeLine();
			writeLine("      GridPath");
			writeLine("               By default, the grid path is the CSV file name in lowercase. Rename it to");
			writeLine("               an appropriate name.");
			writeLine();
			writeLine("      IsKeyAutoGen");
			writeLine("               Set this to false if one or more fields can serve as the primary key.");
			writeLine();
			writeLine("      StartRow");
			writeLine("               Correct this value if the header row number is not correct.");
			writeLine();
			writeLine("      field_names");
			writeLine("               The field names are listed at the bottom of the schema file. Correct the");
			writeLine("               names as needed, set the field type for each field, and identify the Primary fields.");
			writeLine();
			writeLine("   2. Move the generated schema files to the schema directory, i.e., data/schema.");
			writeLine();
			writeLine("   3. Generate and compile VersionedPortable classses by executing 'generate_versioned_portable'");
			writeLine("      and 'compile_generated_code'.");
			writeLine();
			writeLine("   4. Move additional data files to the import directory, i.e., data/import. The names of");
			writeLine("      data files that begin with the schema file name will be parsed with that schema file.");
			writeLine();
			writeLine("   5. Run 'import_csv' to load the data.");
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
		String executable = System.getenv("EXECUTABLE");
		if (executable == null) {
			executable = HazelcastSchemaGenerator.class.getSimpleName();
		}
		writeLine();
		writeLine("NAME");
		writeLine("   " + executable + " - Generate schema files for CSV files");
		writeLine("");
		writeLine("SNOPSIS");
		writeLine("   " + executable + " [-dataDir data_file_directory]");
		writeLine("                      [-schemaDir schema_file_directory]");
		writeLine("                      [-parentPath parent_grid_path");
		writeLine("                      [-headerRow column_names_row_number]");
		writeLine("                      [-startRow start_row_number");
		writeLine("                      [-package class_package_name");
		writeLine("                      [-?]");
		writeLine();
		writeLine("IMPORTANT");
		writeLine("   This command overwrites the existing schema files. Make sure to back up the");
		writeLine("   output directory (-schemaDir) before executing this command.");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Generates schema files for all data files found in the specified data directory.");
		writeLine("   The directory paths can be absolute or relative to the directory ");
		writeLine("   " + padoHome);
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -dataDir data_file_directory");
		writeLine("             Data file directory. Schema files are generated for all of data files");
		writeLine("             found in this directory. Default: data/import");
		writeLine("");
		writeLine("   -schemaDir schema_file_directory");
		writeLine("             Schema file directory to which schema files are generated.");
		writeLine("             Default: data/schema/generated");
		writeLine("");
		writeLine("   -parentPath parent_grid_path");
		writeLine("             Parent grid path. It must not begin with '/' and include all nested");
		writeLine("             paths starting from the top path of the grid excluding the root path");
		writeLine("             assigned to the grid ID.");
		writeLine("");
		writeLine("   -headerRow column_names_row_number");
		writeLine("             Column header row number in the data file. If unspecified then it defaults to 1,");
		writeLine("             i.e., the first row is the header row. If 0, then it assumes that the header");
		writeLine("             row does not exist and enumerates the field names starting from 'C1'. If -1,");
		writeLine("             then the header row is searched and determined. Row numbers begin from 1.");
		writeLine("");
		writeLine("   -startRow start_row_number");
		writeLine("             Start row number in the data file. If unspecified or less than 0, then the row");
		writeLine("             immediately after the header row is assigned. Row numbers begin from 1.");
		writeLine("");
		writeLine("   -package class_package_name");
		writeLine("             Java class package name. If not specified then defaults to 'org.hazelcast.data'");
		writeLine();
		writeLine("DEFAULT");
		writeLine("   " + executable + " -dataDir data/import -schemaDir data/schema/generated -headerRow 1 -startRow 2");
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
		int headerRow = 1;
		int startRow = -1;
		String packageName = "org.hazelcast.data";
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
			} else if (arg.equals("-package")) {
				if (i < args.length - 1) {
					packageName = args[++i];
				}
			}
		}
		File schemaDir = new File(schemaDirPath);
		File dataDir = new File(dataDirPath);
		HazelcastSchemaGenerator sg = new HazelcastSchemaGenerator();
		sg.generateSchemaFiles(dataDir, schemaDir, parentPath, headerRow, startRow, packageName);
	}
}
