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
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;
import java.util.UUID;

import com.netcrest.pado.biz.file.SchemaInfo;
import com.netcrest.pado.internal.util.SchemaUtil;

public class KeyTypeGenerator
{
	public File[] generateKeyType(SchemaInfo schemaInfo, File srcDir, int version) throws IOException, URISyntaxException
	{
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream("com/netcrest/pado/tools/KeyTypeTemplate.txt");
		String keyTypeStr = SchemaUtil.readFile(is);
		is.close();

		String keyTypeFullClassName = schemaInfo.getKeyTypeClassName();
		if (keyTypeFullClassName == null) {
			return null;
		}

		int index = keyTypeFullClassName.lastIndexOf(".");
		String keyTypePackageName = keyTypeFullClassName.substring(0, index);
		String keyTypeClassName = keyTypeFullClassName.substring(index + 1);
		if (keyTypeClassName.endsWith("Key") == false) {
			keyTypeClassName += "Key";
		}
		index = keyTypeClassName.lastIndexOf("Key");
		String domainClassName = keyTypeClassName.substring(0, index);
		String versionStr;
		if (version <= 0) {
			versionStr = "0";
		} else {
			versionStr = Integer.toString(version);
		}
		UUID uuid = UUID.randomUUID();
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		String user = schemaInfo.getUsername();
		if (user == null) {
			user = System.getProperty("user.name");
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy.HH.mm.ss.z");
		Date date = new Date();
		String createdDateStr = dateFormat.format(date);
		String updatedDateStr = date.toString();

		keyTypeStr = keyTypeStr.replaceAll("\\$\\{KEY_TYPE_PACKAGE\\}", keyTypePackageName);
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{VERSION\\}", versionStr);
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{UUID_MS\\}", msb + "");
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{UUID_LS\\}", lsb + "");
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{USER\\}", user);
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{CREATED_DATE\\}", createdDateStr);
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{UPDTED_DATE\\}", updatedDateStr);
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{KEY_TYPE_CLASS_NAME\\}", keyTypeClassName);
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{UPDTED_DATE\\}", updatedDateStr);
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{DOMAIN_CLASS_NAME\\}", domainClassName);

		TreeSet<String> importSet = new TreeSet<String>();
		StringBuffer buffer = new StringBuffer(2000);
		String[] names = schemaInfo.getValueColumnNames();
		Class<?>[] types = schemaInfo.getValueColumnTypes();
		int count = 0;
		for (int i = 0; i < names.length; i++) {
			if (schemaInfo.isSkipColumn(names[i])) {
				continue;
			}
			if (count > 0) {
				buffer.append(",\n");
			}
			count++;
			buffer.append("\t/**\n");
			buffer.append("\t * ");
			buffer.append("<b>");
			buffer.append(types[i].getSimpleName());
			buffer.append("</b>\n");
			buffer.append("\t */\n");
			buffer.append("\tK");
			buffer.append(names[i]);
			buffer.append("(\"");
			buffer.append(names[i]);
			buffer.append("\", ");
			buffer.append(types[i].getSimpleName());
			buffer.append(".class, false, true, \"\", 0)");

			String importStatement = null;
			if (types[i].isPrimitive() == false && types[i].getPackage().getName().equals("java.lang") == false) {
				importStatement = "import " + types[i].getName() + ";\n";
			}
			if (importStatement != null) {
				importSet.add(importStatement);
			}
		}
		buffer.append(";\n");
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{KEY_DECLARATION\\}", buffer.toString());
		buffer.delete(0, buffer.length());
		for (String importStatement : importSet) {
			buffer.append(importStatement);
		}
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{JAVA_IMPORTS\\}", buffer.toString());

		// Write to file
		srcDir.mkdirs();
		String keyTypeFilePath = keyTypeFullClassName.replaceAll("\\.", "/") + ".java";
		File keyTypeFile = new File(srcDir, keyTypeFilePath);
		keyTypeFile.getParentFile().mkdirs();
		FileWriter writer = null;
		try {
			writer = new FileWriter(keyTypeFile);
			writer.write(keyTypeStr);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}

		File[] files;
		
		if (version > 0) {
			String keyTypePackageName_v = keyTypePackageName + ".v";
			String keyTypeClassName_v = keyTypeClassName + "_v" + version;
			String enumClassName = "enum " + keyTypeClassName;
			String enumClassName_v = "enum " + keyTypeClassName_v;
			String constructorClassName = "private " + keyTypeClassName;
			String constructorClassName_v = "private " + keyTypeClassName_v;
			keyTypeStr = keyTypeStr.replaceAll(keyTypePackageName, keyTypePackageName_v);
			keyTypeStr = keyTypeStr.replaceAll(enumClassName, enumClassName_v);
			keyTypeStr = keyTypeStr.replaceAll(constructorClassName, constructorClassName_v);
			File versionDir = new File(keyTypeFile.getParentFile(), "v");
			versionDir.mkdir();
			File keyTypeFile_v = new File(versionDir, keyTypeClassName_v + ".java");
			try {
				writer = new FileWriter(keyTypeFile_v);
				writer.write(keyTypeStr);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
			files = new File[2];
			files[1] = keyTypeFile_v;
		} else {
			files = new File[1];
		}
		files[0] = keyTypeFile;
		return files;
	}

	public File generateDomainClass(SchemaInfo schemaInfo, File srcDir) throws IOException, URISyntaxException
	{
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream("com/netcrest/pado/tools/DomainClassTemplate.txt");
		String keyTypeStr = SchemaUtil.readFile(is);
		is.close();
		String keyTypeFullClassName = schemaInfo.getKeyTypeClassName();
		if (keyTypeFullClassName == null) {
			return null;
		}

		int index = keyTypeFullClassName.lastIndexOf(".");
		String keyTypePackageName = keyTypeFullClassName.substring(0, index);
		String keyTypeClassName = keyTypeFullClassName.substring(index + 1);
		if (keyTypeClassName.endsWith("Key") == false) {
			keyTypeClassName += "Key";
		}
		index = keyTypeClassName.lastIndexOf("Key");
		String domainClassName = keyTypeClassName.substring(0, index);
		String domainFullClassName = keyTypePackageName + ".domain." + domainClassName;

		keyTypeStr = keyTypeStr.replaceAll("\\$\\{KEY_TYPE_PACKAGE\\}", keyTypePackageName);
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{DOMAIN_CLASS_NAME\\}", domainClassName);
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{KEY_TYPE_CLASS_NAME\\}", keyTypeClassName);

		// ${JAVA_IMPORTS}, ${PROPERTY_DEFINITION}
		TreeSet<String> importSet = new TreeSet<String>();
		StringBuffer buffer = new StringBuffer(2000);
		String[] names = schemaInfo.getValueColumnNames();
		Class<?>[] types = schemaInfo.getValueColumnTypes();
		for (int i = 0; i < names.length; i++) {
			if (schemaInfo.isSkipColumn(names[i])) {
				continue;
			}

			// setter
			String argName = names[i].substring(0, 1).toLowerCase() + names[i].substring(1);
			buffer.append("\n\t");
			buffer.append("public void set");
			buffer.append(names[i]);
			buffer.append("(");
			buffer.append(types[i].getSimpleName());
			buffer.append(" ");
			buffer.append(argName);
			buffer.append(") {\n");
			buffer.append("\t\t");
			buffer.append("this.jl.put(");
			buffer.append(keyTypeFullClassName);
			buffer.append(".K" + names[i]);
			buffer.append(", " + argName);
			buffer.append(");\n");
			buffer.append("\t}\n\n");

			// getter
			buffer.append("\t");
			buffer.append("public ");
			buffer.append(types[i].getSimpleName());
			buffer.append(" get");
			buffer.append(names[i]);
			buffer.append("() {\n");
			buffer.append("\t\treturn (");
			buffer.append(types[i].getSimpleName());
			buffer.append(") this.jl.get(");
			buffer.append(keyTypeFullClassName);
			buffer.append(".K" + names[i]);
			buffer.append(");\n");
			buffer.append("\t}\n");

			String importStatement = null;
			if (types[i].isPrimitive() == false && types[i].getPackage().getName().equals("java.lang") == false) {
				importStatement = "import " + types[i].getName() + ";\n";
			}
			if (importStatement != null) {
				importSet.add(importStatement);
			}
		}
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{PROPERTY_DEFINITION\\}", buffer.toString());
		buffer.delete(0, buffer.length());
		for (String importStatement : importSet) {
			buffer.append(importStatement);
		}
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{JAVA_IMPORTS\\}", buffer.toString());

		// Write to file
		srcDir.mkdirs();
		String domainClassFilePath = domainFullClassName.replaceAll("\\.", "/") + ".java";
		File domainClassFile = new File(srcDir, domainClassFilePath);
		domainClassFile.getParentFile().mkdirs();
		FileWriter writer = null;
		try {
			writer = new FileWriter(domainClassFile);
			writer.write(keyTypeStr);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		
		return domainClassFile;
	}

	/**
	 * Generates KeyType classes for all schema files found in the specified
	 * schema directory.
	 * 
	 * @param schemaDir
	 *            Directory where schema files are kept.
	 * @param srcDir
	 *            Source directory where KeyType classes are generated.
	 * @param version
	 *            If less than 1, then KeyType classes are not versioned and
	 *            domain classes are not generated.
	 */
	public void generateAll(File schemaDir, File srcDir, int version)
	{
		if (schemaDir == null) {
			System.err.println("Schema directory must be specified.");
			return;
		}
		if (srcDir == null) {
			System.err.println("Source directory must be specified.");
			return;
		}
		File schemaFiles[] = schemaDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".schema");
			}

		});

		if (schemaFiles == null) {
			writeLine();
			writeLine("Schema directory not valid: " + schemaDir.getAbsolutePath());
			writeLine();
		} else if (schemaFiles.length == 0) {
			writeLine();
			writeLine("Schema files not found in directory " + schemaDir.getAbsolutePath());
			writeLine();
		} else {
			srcDir.mkdirs();
			writeLine();
			for (int i = 0; i < schemaFiles.length; i++) {
				writeLine((i + 1) + ". " + schemaFiles[i].getAbsolutePath());

				SchemaInfo schemaInfo = new SchemaInfo("csv", schemaFiles[i]);
				try {
					File files[] = generateKeyType(schemaInfo, srcDir, version);
					if (files == null || files.length == 0) {
						writeLine("   Skipped: KeyTypeClass not defined in the schema file.");
					} else {
						writeLine("   Generated: " + files[0].getAbsolutePath());
					}
					if (files != null && files.length > 1) {
						writeLine("   Generated: " + files[1].getAbsolutePath());
						File domainClassFile = generateDomainClass(schemaInfo, srcDir);
						if (domainClassFile != null) {
							writeLine("   Generated: " + domainClassFile.getAbsolutePath());
						}
					}
				} catch (Exception e) {
					writeLine("   Error: " + e.getMessage());
					e.printStackTrace();
				}
			}
			writeLine();
			writeLine("Source code generation complete.");
			writeLine();
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

	private static void usage()
	{
		String padoHome = System.getenv("PADO_HOME");
		if (padoHome == null) {
			padoHome = "$PADO_HOME";
		}
		writeLine();
		writeLine("Usage:");
		writeLine(
				"   KeyTypeGenerator [-schemaDir <schema-directory] [-srcDir <output-directory] [-v [<version number>]] [-?]");
		writeLine();
		writeLine("   IMPORTANT: This command overwrites the existing source code. Make sure");
		writeLine("              to back up the output source directory first before running");
		writeLine("              this program in case if you need to revert to the existing code.");
		writeLine();
		writeLine("   Generates the key type classes declared in all of the CSV schema");
		writeLine("   files found in the schema directory. The directory paths can be");
		writeLine("   absolute or relative to the " + padoHome + " directory.");
		writeLine();
		writeLine("      -schemaDir  Import directory path that contains *.schema files");
		writeLine("      -srcDir     Source directory path where the key type classes are to be generated");
		writeLine("      -v          Versions the generated KeyType class. If version number is not specified");
		writeLine("                  then it defaults to 1.");
		writeLine();
		writeLine("   Default: KeyTypeGenerator -schemaDir data/schema -srcDir src/generated");
		writeLine();
		System.exit(0);
	}

	/**
	 * @param args
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, URISyntaxException
	{
		String arg;
		String schemaDirPath = "data/schema";
		String srcDirPath = "src/generated";
		int version = 0;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-schemaDir")) {
				if (i < args.length - 1) {
					schemaDirPath = args[++i];
				}
			} else if (arg.equals("-srcDir")) {
				if (i < args.length - 1) {
					srcDirPath = args[++i];
				}
			} else if (arg.equals("-v")) {
				if (i < args.length - 1) {
					version = Integer.parseInt(args[++i]);
				} else {
					version = 1;
				}
			}
		}
		KeyTypeGenerator generator = new KeyTypeGenerator();
		File schemaDir = new File(schemaDirPath);
		File srcDir = new File(srcDirPath);
		generator.generateAll(schemaDir, srcDir, version);
	}

}
