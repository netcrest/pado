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
package com.netcrest.pado.biz.impl.gemfire;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.IDataLoaderBiz;
import com.netcrest.pado.biz.file.CsvFileLoader;
import com.netcrest.pado.biz.file.SchemaInfo;
import com.netcrest.pado.info.message.MessageType;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.util.GridUtil;

public class DataLoaderBizImpl implements IDataLoaderBiz
{
	@Resource IBizContextServer bizContext;
	
	private static File schemaFileDir = new File("../../data");
	private static File dataFileDir = new File("data");
	private static int batchSize = 5000;
	
	/**
	 * Ignored. This method is never used in the server side. It always returns
	 * null.
	 */
	@Override
	public IBizContextClient getBizContext()
	{
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	@BizMethod
	@Override
	public List loadData(String gridPath, String schemaFilePath, final String dataFilePrefix, String dataClassName)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		File schemaFile = new File(schemaFileDir, schemaFilePath);
		if (schemaFile.exists() == false) {
			return null;
		}
		String[] dataFiles = dataFileDir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name)
			{
				return name.startsWith(dataFilePrefix);
			}

		});

		PadoServerManager psm = PadoServerManager.getPadoServerManager();
		if (dataFiles.length == 0) {
			psm.putMessage(MessageType.Error, "Data files with the prefix \"" + dataFilePrefix + "\" not found in the server.");
		} else {
			SchemaInfo schemaInfo = new SchemaInfo("file", schemaFile);
			for (String dataFileName : dataFiles) {
				File dataFile = new File(dataFileDir, dataFileName);
				CsvFileLoader fileLoader = new CsvFileLoader(PadoServerManager.getPadoServerManager().getPado());
				try {
					fileLoader.load(schemaInfo, dataFile);
					psm.putMessage(MessageType.Info, "Successfully loaded into the path " + fullPath);
				} catch (Exception ex) {
					psm.putMessage(MessageType.Error, "Error occurred while loading CSV file " + dataFileName + " to the path "
							+ fullPath + ". " + ex.getMessage());
					Logger.error("Error occurred while loading CSV file " + dataFileName + " to the path "
							+ fullPath, ex);
				}
			}
		}
		
		return null;

	}

}
