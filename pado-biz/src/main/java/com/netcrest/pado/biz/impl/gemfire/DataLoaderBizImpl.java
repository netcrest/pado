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
		String regionPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(regionPath);
		File schemaFile = new File(schemaFileDir, schemaFilePath);
		String[] dataFiles = dataFileDir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name)
			{
				return name.startsWith(dataFilePrefix);
			}

		});
//
//		if (dataFiles.length == 0) {
//			message.setMessage("Data files with the prefix \"" + dataFilePrefix + "\" not found in the server.");
//			message.setCode(1);
//		} else {
//			// TODO: Load in parallel using threads
//			for (String dataFileName : dataFiles) {
//				File dataFile = new File(dataFileDir, dataFileName);
//				CsvFileLoader fileLoader = new CsvFileLoader();
//				fileLoader.setDelimiter((char) 29); // GS
//				fileLoader.setDateFormat("MM/dd/yyyy HH:mm:ss");
//				try {
//					Class dataClass = Class.forName(dataClassName);
//					fileLoader.loadFile(schemaFile, ',', 1, dataFile, dataClass, null, null, null,
//							new RegionBulkLoader(region, batchSize), 0, true);
//					message.setMessage(dataFileName + " successfully loaded into the region " + regionPath);
//				} catch (Exception ex) {
//					message.setCode(-1);
//					message.setMessage("Error occurred while loading CSV file " + dataFileName + " to the region "
//							+ regionPath + ". " + ex.getMessage());
//					Logger.error("Error occurred while loading CSV file " + dataFileName + " to the region "
//							+ regionPath, ex);
//				}
//			}
//		}
		
		return null;

	}

}
