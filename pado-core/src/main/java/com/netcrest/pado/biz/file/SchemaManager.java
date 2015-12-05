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
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

/**
 * SchemaManager manages {@link SchemaInfo} objects lazily built upon receiving
 * data loader requests.
 * 
 * @author dpark
 * 
 */
public class SchemaManager
{
	private static SchemaManager manager = new SchemaManager();

	private File schemaFileDir = new File(PadoUtil.getProperty(Constants.PROP_LOADER_SCHEMA_FILE_DIR, "../../data"));

	private Properties dataLoaderSchemanFileMap = new Properties();

	private HashMap<String, SchemaInfo> schemaInfoMap = new HashMap();

	private SchemaManager()
	{
		try {
			refresh();
		} catch (IOException e) {
			Logger.error("Error occurred creating SchemaManager", e);
		}
	}

	/**
	 * Returns the singlton SchemaManager object.
	 */
	public static SchemaManager getSchemaManager()
	{
		return manager;
	}

	/**
	 * Returns the SchemaInfo object pertaining to the specified schema type. It
	 * returned the cached SchemaInfo object if it exists. Otherewise, it
	 * creates a new one base on the schema type. It returns null if the
	 * SchemaInfo object cannot be created.
	 * 
	 * @param schemaType
	 *            Schema type
	 */
	public SchemaInfo getSchemaInfo(String schemaType)
	{
		return getSchemaInfo(schemaType, null);
	}

	/**
	 * Returns the SchemaInfo object pertaining to the specified schema type and
	 * data file name.
	 * 
	 * @param schemaType
	 *            Schema type
	 * @param dataFileName
	 *            Data file name
	 * @throws FileLoaderException
	 *             Thrown if read or parser error occurs while reading the
	 *             schema file
	 */
	public SchemaInfo getSchemaInfo(String schemaType, String dataFileName) throws FileLoaderException
	{
		SchemaInfo schemaInfo = null;
		if (schemaType == null) {
			if (dataFileName != null) {
				schemaType = getSchemaType(dataFileName);
			}
		}
		if (schemaType != null) {
			schemaInfo = schemaInfoMap.get(schemaType);
		}
		if (schemaInfo == null) {
			File schemaFile = getSchemaFile(schemaType);
			if (schemaFile == null) {
				throw new FileLoaderException("Schema file not found: schema-type=" + schemaType);
			}
			schemaInfo = new SchemaInfo(schemaType, schemaFile);
			schemaInfoMap.put(schemaType, schemaInfo);
		}
		return schemaInfo;
	}

	/**
	 * Returns the SchemaInfo object pertaining to the specified data file.
	 * 
	 * @param dataFileName
	 *            Data file name
	 */
	public SchemaInfo getSchemaInfoByDataFileName(String dataFileName)
	{
		return getSchemaInfo(null, dataFileName);
	}

	/**
	 * Extracts and returns the schema type from the data file name. The data
	 * file name must be in the following format:
	 * 
	 * <pre>
	 * file-name[.version][.file-extension]
	 * </pre>
	 * 
	 * where
	 * <p>
	 * <ul>
	 * <li><code>file-name</code> is the file name. It must not contain '.'</li>
	 * <li><code>version</code> is an optional version number.</li>
	 * <li><code>file-extension</code> is an optional file extension.</li>
	 * </ul>
	 * The schema type is <code>file-name</code>.
	 * <p>
	 * 
	 * @param dataFileName
	 *            Data file name. Must not include path.
	 */
	private String getSchemaType(String dataFileName)
	{
		if (dataFileName == null) {
			return null;
		}
		String split[] = dataFileName.split("\\.");
		if (split.length > 0) {
			return split[0];
		} else {
			return dataFileName;
		}
	}

	/**
	 * Returns the schema file pertaining to the specified schema type.
	 * 
	 * @param schemaType
	 *            Schema type
	 */
	public File getSchemaFile(String schemaType)
	{
		String schemaFileName = dataLoaderSchemanFileMap.getProperty(schemaType);
		if (schemaFileName == null) {
			return null;
		}
		return new File(schemaFileDir, schemaFileName);
	}

	/**
	 * Refreshes the SchemaManager by reloading the schema properties file and
	 * clearing the cached SchemaInfo objects.
	 * 
	 * @throws IOException
	 *             Thrown if the schema properties cannot be read
	 */
	public void refresh() throws IOException
	{
		// default paths are relative to the working directory, i.e.,
		// $DAAG_HOME/run/<server-name>
		File file = new File(PadoUtil.getProperty(Constants.PROP_LOADER_DATA_FILE_NAME_MAP_FILE_PATH,
				"../../data/schema.properties"));
		dataLoaderSchemanFileMap = new Properties();
		FileReader reader = new FileReader(file);
		dataLoaderSchemanFileMap.load(reader);
		reader.close();
		schemaInfoMap.clear();
	}
}
