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

import com.netcrest.pado.internal.Constants;

/**
 * IFileLoader is a plug-in interface for loading data into the grid. Daag
 * invokes IFileLoader methods upon receiving data-load requests from clients.
 * SchemaInfo contains data parsing information obtained from the schema file
 * managed by Daag. Schema files are made available in the directory defined by
 * {@link Constants#PROP_LOADER_DATA_FILE_DIR}.
 * 
 * @author dpark
 * 
 */
public interface IFileLoader
{
	/**
	 * Parses and loads the specified text data into the grid. The provided
	 * schema info provides parsing information. Text data is typically a list
	 * of rows separated by line breaks ('\n'). Each row is comprised of one or
	 * more columns separated by the delimiter found in SchemaInfo.
	 * 
	 * @param schemaInfo
	 *            Schema info that contains text data parsing information.
	 * @param textData
	 *            Text data.
	 * @return Number of entries put into the grid
	 * @throws FileLoaderException
	 *             Thrown if the loader fails.
	 */
	int load(SchemaInfo schemaInfo, String textData) throws FileLoaderException;

	/**
	 * Parses and loads the specified file contents into the grid. The file
	 * contents typically follow the same row format conventions as the
	 * {@link #load(SchemaInfo, String)}.
	 * 
	 * @param schemaInfo
	 *            Schema info that contains text data parsing information.
	 * @param dataFile
	 *            Data file to load.
	 * @return Number of entries put into the grid
	 * @throws FileLoaderException
	 *             Thrown if the loader fails.
	 */
	int load(SchemaInfo schemaInfo, File dataFile) throws FileLoaderException;
}
