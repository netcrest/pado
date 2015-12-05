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
package com.netcrest.pado.biz;

import java.util.List;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.OnServer;

/**
 * <i><font color="red">IDataLoaderBiz is being overhauled to simplify the API.
 * As such, the API is subject to change without notice.</font></i>
 * <p>
 * IDataLoaderBiz loads data by splitting the specified file evenly into the
 * number of servers and deploy them to individual servers to load them
 * concurrently.
 * 
 * @author dpark
 * 
 */

@BizClass(name = "IDataLoaderBiz")
public interface IDataLoaderBiz extends IBiz
{
	/**
	 * Loads the specified data file into the grid.
	 * 
	 * @param path
	 *            File path
	 * @param schemaFilePath
	 *            Schema file path
	 * @param dataFilePrefix
	 *            Data file prefix
	 * @param dataClassName
	 *            Data class name
	 * @return Returns server responses
	 */
	@OnServer(broadcast = true)
	public List loadData(String path, String schemaFilePath, String dataFilePrefix, String dataClassName);
}
