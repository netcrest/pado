package com.netcrest.pado.tools.hazelcast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.TreeSet;

import com.netcrest.pado.biz.file.SchemaInfo;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.SchemaUtil;
import com.netcrest.pado.log.Logger;

public class VersionedPortableClassGenerator {
	Date timestamp = new Date();
	String factoryClassName = "PortableFactoryImpl";

	int factoryId = 1;
	int classId = 100;
	int classVersion = 1;

	/**
	 * Creates a VersionedPortableClassGenerator object that generates
	 * VersionedPortable and portable factoryÏß classes based on domain class
	 * information read from the schema files.
	 * 
	 * @param factoryId    Factory ID.
	 * @param classId      Domain class ID.
	 * @param classVersion If less than 1, then KeyType classes are not versioned
	 *                     and domain classes are not generated.
	 */
	public VersionedPortableClassGenerator(int factoryId, int classId, int classVersion) {
		this.factoryId = factoryId;
		this.classId = classId;
		this.classVersion = classVersion;
		initLog();
	}
	
	private void initLog()
	{
		System.setProperty("pado." + Constants.PROP_CLASS_LOGGER, "com.netcrest.pado.hazelcast.util.HazelcastLogger");
	}

	public File generateDomainClass(SchemaInfo schemaInfo, File schemaFile, File srcDir)
			throws IOException, URISyntaxException {
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream("com/netcrest/pado/tools/hazelcast/DomainClassTemplate.txt");
		String keyTypeStr = SchemaUtil.readFile(is);
		is.close();
		String keyTypeFullClassName = schemaInfo.getKeyTypeClassName();
		if (keyTypeFullClassName == null) {
			return null;
		}

		int index = keyTypeFullClassName.lastIndexOf(".");
		String keyTypePackageName = keyTypeFullClassName.substring(0, index);
		String domainClassName = keyTypeFullClassName.substring(index + 1);
		String domainFullClassName = keyTypePackageName + "." + domainClassName;

		keyTypeStr = keyTypeStr.replaceAll("\\$\\{DOMAIN_PACKAGE\\}", keyTypePackageName);
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{DOMAIN_CLASS_NAME\\}", domainClassName);
//		keyTypeStr = keyTypeStr.replaceAll("\\$\\{DOMAIN_CLASS_NAME_UPPERCASE\\}", domainClassName.toUpperCase());
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{CLASS_VERSION\\}", Integer.toString(classVersion));
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{TIMESTAMP\\}", timestamp.toString());
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{GENERATOR_NAME\\}",
				VersionedPortableClassGenerator.class.getCanonicalName());
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{SCHEMA_FILE_NAME\\}", schemaFile.getName());

		// ${JAVA_IMPORTS}, ${PROPERTY_DEFINITION}
		TreeSet<String> importSet = new TreeSet<String>();
		StringBuffer propDeclarationBuffer = new StringBuffer(2000);
		StringBuffer propMethodsBuffer = new StringBuffer(2000);
		StringBuffer writeBuffer = new StringBuffer(2000);
		StringBuffer readBuffer = new StringBuffer(2000);
		StringBuffer comparableBuffer = new StringBuffer(100);
		StringBuffer compareToBuffer = new StringBuffer(2000);
		StringBuffer toStringBuffer = new StringBuffer(2000);
		String[] names = schemaInfo.getValueColumnNames();
		Class<?>[] types = schemaInfo.getValueColumnTypes();
		boolean isDate = false;
		boolean isLongVarDefined = false;

		for (int i = 0; i < names.length; i++) {
			if (schemaInfo.isSkipColumn(names[i])) {
				continue;
			}

			Class<?> type = types[i];
			String argName = names[i].substring(0, 1).toLowerCase() + names[i].substring(1);

			// members
			propDeclarationBuffer.append("\n\t");
			propDeclarationBuffer.append("private ");
			propDeclarationBuffer.append(type.getSimpleName());
			propDeclarationBuffer.append(" ");
			propDeclarationBuffer.append(argName);
			propDeclarationBuffer.append(";");

			// setter
			propMethodsBuffer.append("\n\t");
			propMethodsBuffer.append("public void set");
			propMethodsBuffer.append(names[i]);
			propMethodsBuffer.append("(");
			propMethodsBuffer.append(type.getSimpleName());
			propMethodsBuffer.append(" ");
			propMethodsBuffer.append(argName);
			propMethodsBuffer.append(") {\n");
			propMethodsBuffer.append("\t\t");
			propMethodsBuffer.append("this.");
			propMethodsBuffer.append(argName);
			propMethodsBuffer.append("=");
			propMethodsBuffer.append(argName);
			propMethodsBuffer.append(";\n");
			propMethodsBuffer.append("\t}\n\n");

			// getter
			propMethodsBuffer.append("\t");
			propMethodsBuffer.append("public ");
			propMethodsBuffer.append(type.getSimpleName());
			propMethodsBuffer.append(" get");
			propMethodsBuffer.append(names[i]);
			propMethodsBuffer.append("() {\n");
			propMethodsBuffer.append("\t\treturn ");
			propMethodsBuffer.append("this.");
			propMethodsBuffer.append(argName);
			propMethodsBuffer.append(";\n");
			propMethodsBuffer.append("\t}\n");

			String importStatement = null;
			if (type.isPrimitive() == false && type.getPackage().getName().equals("java.lang") == false) {
				importStatement = "import " + type.getName() + ";\n";
			}
			if (importStatement != null) {
				importSet.add(importStatement);
			}

			// write/read
			if (type == Date.class) {
				isDate = true;

				// write: if the date object is null, then write -1
				writeBuffer.append("\n\t\t");
				writeBuffer.append("if (");
				writeBuffer.append("this.");
				writeBuffer.append(argName);
				writeBuffer.append(" == null) {");
				writeBuffer.append("\n\t\t\t");
				writeBuffer.append("writer.writeLong(\"");
				writeBuffer.append(argName);
				writeBuffer.append("\", -1L);");
				writeBuffer.append("\n\t\t");

				// write: else write Date.getTime()
				writeBuffer.append("} else {");
				writeBuffer.append("\n\t\t\t");
				writeBuffer.append("writer.write");
				writeBuffer.append("Long(\"");
				writeBuffer.append(argName);
				writeBuffer.append("\", ");
				writeBuffer.append("this.");
				writeBuffer.append(argName);
				writeBuffer.append(".getTime()");
				writeBuffer.append(");");
				writeBuffer.append("\n\t\t}");

				// read: if the date long value is not -1L, then create date object
				readBuffer.append("\n\t\t");
				if (isLongVarDefined == false) {
					readBuffer.append("long ");
					isLongVarDefined = true;
				}
				readBuffer.append("l = reader.readLong(\"");
				readBuffer.append(argName);
				readBuffer.append("\");");
				readBuffer.append("\n\t\t");
				readBuffer.append("if (l != -1L) {");
				readBuffer.append("\n\t\t\t");
				readBuffer.append("this.");
				readBuffer.append(argName);
				readBuffer.append(" = new Date(l);");
				readBuffer.append("\n\t\t");
				readBuffer.append("}");
			} else {
				writeBuffer.append("\n\t\t");
				writeBuffer.append("writer.write");

				readBuffer.append("\n\t\t");
				readBuffer.append("this.");
				readBuffer.append(argName);

				readBuffer.append(" = reader.read");
				if (type == String.class) {
					writeBuffer.append("UTF");
					readBuffer.append("UTF");
				} else if (type.isPrimitive()) {
					String typeName = firstChar2UpperCase(type.getSimpleName());
					writeBuffer.append(typeName);
					readBuffer.append(typeName);
				} else {
					writeBuffer.append(type.getSimpleName());
					readBuffer.append(type.getSimpleName());
				}
				writeBuffer.append("(\"");
				writeBuffer.append(argName);
				writeBuffer.append("\", ");
				writeBuffer.append(argName);
				writeBuffer.append(");");
				readBuffer.append("(\"");
				readBuffer.append(argName);
				readBuffer.append("\");");
			}
		}

		// toString()
		// Sort names
		String[] sortedNames= new String[names.length];
		System.arraycopy(names, 0, sortedNames, 0, names.length);
		Arrays.sort(sortedNames);
		for (int i = 0; i < sortedNames.length; i++) {
			String argName = sortedNames[i].substring(0, 1).toLowerCase() + sortedNames[i].substring(1);
			if (i > 0) {
				toStringBuffer.append("\n\t\t\t + ");
				toStringBuffer.append("\", ");
			} else {
				toStringBuffer.append("\"[");
			}
			toStringBuffer.append(argName);
			toStringBuffer.append("=\" + this.");
			toStringBuffer.append(argName);

		}
		toStringBuffer.append(" + \"]\"");

		// Comparable/compareTo()
		int[] comparableIndexes = schemaInfo.getComparableIndexes();
		
		// Comparable
		if (comparableIndexes.length > 0) {
			comparableBuffer.append(", Comparable<");
			comparableBuffer.append(domainClassName);
			comparableBuffer.append(">");
		}
		
		// compareTo()
		if (comparableIndexes.length > 0) {
			compareToBuffer.append("\n\n\t@Override");
			compareToBuffer.append("\n\tpublic int compareTo(");
			compareToBuffer.append(domainClassName);
			compareToBuffer.append(" o) {");
			compareToBuffer.append("\n\t\t// ");
			compareToBuffer.append(domainClassName);
			compareToBuffer.append("\n\t\tif (o == null) {");
			compareToBuffer.append("\n\t\t\treturn -1;");
			compareToBuffer.append("\n\t\t}");

			boolean cDeclared = false;
			for (int i = 0; i < comparableIndexes.length; i++) {
				index = comparableIndexes[i];
				String argName = names[index].substring(0, 1).toLowerCase() + names[index].substring(1);
				
				Class<?> type = types[index];
				
				compareToBuffer.append("\n\t\t// ");
				compareToBuffer.append(argName);

				// value comparison
				if (type.isPrimitive()) {
					compareToBuffer.append("\n\t\tif (this.");
					compareToBuffer.append(argName);
					compareToBuffer.append(" > o.");
					compareToBuffer.append(argName);
					compareToBuffer.append(") {");
					compareToBuffer.append("\n\t\t\treturn 1;");
					compareToBuffer.append("\n\t\t} else if (this.");
					compareToBuffer.append(argName);
					compareToBuffer.append(" < o.");
					compareToBuffer.append(argName);
					compareToBuffer.append(") {");
					compareToBuffer.append("\n\t\t\treturn -1;");
					compareToBuffer.append("\n\t\t}");
				} else {
					// null comparison
					compareToBuffer.append("\n\t\tif (this.");
					compareToBuffer.append(argName);
					compareToBuffer.append(" == null || o.");
					compareToBuffer.append(argName);
					compareToBuffer.append(" == null) {");
					compareToBuffer.append("\n\t\t\treturn -1;");
					compareToBuffer.append("\n\t\t}");
					
					compareToBuffer.append("\n\t\t");
					if (cDeclared == false) {
						compareToBuffer.append("int ");
						cDeclared = true;
					}
					compareToBuffer.append("c = this.");
					compareToBuffer.append(argName);
					compareToBuffer.append(".compareTo(o.");
					compareToBuffer.append(argName);
					compareToBuffer.append(");");
					compareToBuffer.append("\n\t\tif (c != 0) {");
					compareToBuffer.append("\n\t\t\treturn c;");
					compareToBuffer.append("\n\t\t}");
				}
			}
			compareToBuffer.append("\n\t\treturn 0;");
			compareToBuffer.append("\n\t}");
		}

		// ------------

		keyTypeStr = keyTypeStr.replaceAll("\\$\\{PROPERTY_DECLARATION\\}", propDeclarationBuffer.toString());
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{PROPERTY_DEFINITION\\}", propMethodsBuffer.toString());
		propMethodsBuffer.delete(0, propMethodsBuffer.length());
		for (

		String importStatement : importSet) {
			propMethodsBuffer.append(importStatement);
		}
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{JAVA_IMPORTS\\}", propMethodsBuffer.toString());
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{WRITE_PORTABLE\\}", writeBuffer.toString());
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{READ_PORTABLE\\}", readBuffer.toString());
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{TO_STRING\\}", toStringBuffer.toString());
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{COMPARABLE\\}", comparableBuffer.toString());
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{COMPARE_TO_METHOD\\}", compareToBuffer.toString());

		String importPackages = "";
		if (isDate) {
			importPackages = "import java.util.Date;";
		}
		keyTypeStr = keyTypeStr.replaceAll("\\$\\{IMPORT_PACKAGES\\}", importPackages);

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
	
	class FactoryClassInfo
	{
		String keyTypeFullClassName;
		String keyTypePackageName;
		String keyTypeClassName;
		String factoryFullClassName;
		
		FactoryClassInfo(SchemaInfo schemaInfo) 
		{
			keyTypeFullClassName = schemaInfo.getKeyTypeClassName();
			if (keyTypeFullClassName == null) {
				return;
			}
			int index = keyTypeFullClassName.lastIndexOf(".");
			keyTypePackageName = keyTypeFullClassName.substring(0, index);
			keyTypeClassName = keyTypeFullClassName.substring(index + 1);
			factoryFullClassName = keyTypePackageName + "." + factoryClassName;
		}
	}
	
	public File generateFactoryClass(SchemaInfo schemaInfo, File schemaFile, File srcDir)
			throws IOException, URISyntaxException 
	{
		FactoryClassInfo info = new FactoryClassInfo(schemaInfo);
		if (info.keyTypeFullClassName == null) {
			return null;
		}

		String factoryClassFilePath = info.factoryFullClassName.replaceAll("\\.", "/") + ".java";
		File factoryClassFile = new File(srcDir, factoryClassFilePath);
		InputStream is;
		String factoryStr;
		if (factoryClassFile.exists()) {
			is = new FileInputStream(factoryClassFile);
		} else {
			is = this.getClass().getClassLoader()
					.getResourceAsStream("com/netcrest/pado/tools/hazelcast/PortableFactoryImplTemplate.txt");
		}
		factoryStr = SchemaUtil.readFile(is);
		is.close();

		if (factoryClassFile.exists()) {
			String classId = info.keyTypeClassName + "_CLASS_ID";

			// See if this class ID already exists
			int index = factoryStr.indexOf(classId);
			boolean classIdExists = index != -1 && Character.isWhitespace(factoryStr.charAt(index - 1));
			if (classIdExists) {
				writeLine("   Ignored: " + classId
						+ " already defined in the factory class. No modifications made to the factory class.");
				return null;
			} else {

				// Get the existing last class ID.
				index = factoryStr.indexOf("__LAST_CLASS_ID = ");
				if (index != -1) {
					int startIndex = index + 18;
					char c = 0;
					int i = startIndex;
					StringBuffer buffer = new StringBuffer(20);
					do {
						c = factoryStr.charAt(i++);
						if (c != ';') {
							buffer.append(c);
						}
					} while (c != ';');
					String lastClassId = buffer.toString();

					if (factoryClassFile.exists()) {
						factoryStr = factoryStr.replaceAll("static final int __LAST_CLASS_ID = " + lastClassId,
								"static final int " + classId + " = " + lastClassId
										+ " + 1;\n\tstatic final int __LAST_CLASS_ID = " + classId);
						factoryStr = factoryStr.replaceAll("\\} else \\{",
								"} else if (classId == " + classId + ") \\{\n\t\t\treturn new " + info.keyTypeClassName + "();\n\t\t\\} else \\{");
					} else {
						factoryStr = factoryStr.replaceAll("static final int __LAST_CLASS_ID = " + lastClassId,
								"static final int __LAST_CLASS_ID = " + classId);
					}
				}
			}
		} else {
			factoryStr = factoryStr.replaceAll("\\$\\{DOMAIN_PACKAGE\\}", info.keyTypePackageName);
			factoryStr = factoryStr.replaceAll("\\$\\{CLASS_NAME\\}", info.keyTypeClassName);
			String factoryIdStr = "Integer.getInteger(\"" + info.keyTypePackageName + ".PortableFactoryImpl.factoryId\", " + factoryId + ")";
			factoryStr = factoryStr.replaceAll("\\$\\{FACTORY_ID\\}", factoryIdStr);
			String classIdStr = "Integer.getInteger(\"" + info.keyTypePackageName + ".PortableFactoryImpl.firstClassId\", " + classId + ")";
			factoryStr = factoryStr.replaceAll("\\$\\{FIRST_CLASS_ID\\}", classIdStr);
		}

		factoryStr = factoryStr.replaceAll("\\$\\{TIMESTAMP\\}", timestamp.toString());
		factoryStr = factoryStr.replaceAll("\\$\\{GENERATOR_NAME\\}",
				VersionedPortableClassGenerator.class.getCanonicalName());

		// Write to file
		srcDir.mkdirs();

		factoryClassFile.getParentFile().mkdirs();
		FileWriter writer = null;
		try {
			writer = new FileWriter(factoryClassFile);
			writer.write(factoryStr);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}

		return factoryClassFile;
	}

	private String firstChar2UpperCase(String str) {
		return Character.toString(str.charAt(0)).toUpperCase() + str.substring(1);
	}

	/**
	 * Generates KeyType classes for all schema files found in the specified schema
	 * directory.
	 * 
	 * @param schemaDir Directory where schema files are kept.
	 * @param srcDir    Source directory where KeyType classes are generated.
	 */
	public void generateAll(File schemaDir, File srcDir) {
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
			public boolean accept(File dir, String name) {
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
			FactoryClassInfo factoryInfo = null;
			srcDir.mkdirs();
			writeLine();
			for (int i = 0; i < schemaFiles.length; i++) {
				writeLine((i + 1) + ". " + schemaFiles[i].getAbsolutePath());

				SchemaInfo schemaInfo = new SchemaInfo("csv", schemaFiles[i]);
				factoryInfo = new FactoryClassInfo(schemaInfo);
				try {
					File domainClassFile = generateDomainClass(schemaInfo, schemaFiles[i], srcDir);
					if (domainClassFile != null) {
						writeLine("   Generated: " + domainClassFile.getAbsolutePath());
					}
					File factoryClassFile = generateFactoryClass(schemaInfo, schemaFiles[i], srcDir);
					if (factoryClassFile != null) {
						writeLine("   Generated: " + factoryClassFile.getAbsolutePath());
					}
				} catch (Exception e) {
					writeLine("   Error: " + e.getMessage());
					Logger.error(e);
				}
			}
			
			writeLine();
			writeLine("DESCRIPTION");
			writeLine("          The generated classes are set to the default values shown below. The");
			writeLine("          class IDs are incremented starting from first class ID.");
			writeLine();
			writeLine("DEFAULT");
			writeLine("              Factory ID: " + factoryId);
			writeLine("          First Class ID: " + classId);
			writeLine();
			writeLine("SYSTEM PROPERTIES");
			writeLine("          You can change the default IDs using the following system properties.");
			writeLine("          If you change the factory ID then you must also set the same value in");
			writeLine("          the configuration files.");
			writeLine();
			writeLine("          Factory ID");
			writeLine("                    -D" + factoryInfo.factoryFullClassName + ".factoryId=" + factoryId);
			writeLine();
			writeLine("          First Class ID");
			writeLine("                    -D" + factoryInfo.factoryFullClassName + ".firstClassId=" + classId);
			writeLine();
			writeLine("CONFIGURATION");
			writeLine("          If you will be executing queries then the Portable factory class must");
			writeLine("          be registered by Hazelcast members and clients as follows:");
			writeLine();
			writeLine("          hazelcast.xml/hazelcast-client.xml");
			writeLine();
			writeLine(
					"             <serialization>\n" + 
					"                 <portable-factories>\n" + 
					"                     <portable-factory factory-id=\"" + factoryId + "\">\n" + 
					"                          " + factoryInfo.factoryFullClassName + "\n" + 
					"                     </portable-factory>\n" + 
					"                 </portable-factories>\n" + 
					"             </serialization>");
			writeLine();
			writeLine("Source code generation complete.");
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
			executable = VersionedPortableClassGenerator.class.getSimpleName();
		}
		writeLine();
		writeLine("NAME");
		writeLine("   " + executable + " - Generate VersionedPortable classes based on schema files");
		writeLine();
		writeLine("SNOPSIS");
		writeLine("   " + executable +" [-schemaDir schema_directory] [-srcDir output_directory] [-fid portable_factory_class_id] [-cid data_class_id] [-v version_number] [-?]");
		writeLine();
		writeLine("IMPORTANT");
		writeLine("   This command overwrites the existing source code. Make sure to back up the");
		writeLine("   output source directory first before running this command in case if you need");
		writeLine("   to revert to the existing code.");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Generates the key type classes declared in all of the CSV schema");
		writeLine("   files found in the schema directory. The directory paths can be");
		writeLine("   absolute or relative to the following directory.");
		writeLine();
		writeLine("   " + padoHome);
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -schemaDir schema_directory");
		writeLine("             Import directory path that contains *.schema files");
		writeLine();
		writeLine("   -srcDir output_directory");
		writeLine("             Source directory path where the key type classes are to be generated");
		writeLine();
		writeLine("   -fid");
		writeLine("             PortableFactory ID. If the portable factory ID is not specified");
		writeLine("             then it defaults to 1.");
		writeLine();
		writeLine("   -cid");
		writeLine("             Class ID. If the class ID is not specified then it defaults to 100.");
		writeLine();
		writeLine("   -v version_number");
		writeLine("             Versions the generated KeyType (data) class. If version number is not specified");
		writeLine("             then it defaults to 1.");
		writeLine();
		writeLine("DEFAULT");
		writeLine("   " + executable + " -schemaDir data/schema -srcDir src/generated -fid 1 -cid 100 -v 1");
		writeLine();
		System.exit(0);
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		String arg;
		String schemaDirPath = "data/schema";
		String srcDirPath = "src/generated";
		int fid = 1;
		int cid = 100;
		int version = 1;
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
			} else if (arg.equals("-fid")) {
				if (i < args.length - 1) {
					fid = Integer.parseInt(args[++i]);
				} else {
					fid = 1;
				}
			} else if (arg.equals("-cid")) {
				if (i < args.length - 1) {
					cid = Integer.parseInt(args[++i]);
				} else {
					cid = 100;
				}
			} else if (arg.equals("-v")) {
				if (i < args.length - 1) {
					version = Integer.parseInt(args[++i]);
				} else {
					version = 1;
				}
			}
		}
		VersionedPortableClassGenerator generator = new VersionedPortableClassGenerator(fid, cid, version);
		File schemaDir = new File(schemaDirPath);
		File srcDir = new File(srcDirPath);
		generator.generateAll(schemaDir, srcDir);
	}
}
