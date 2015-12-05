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
package com.netcrest.pado.biz.server;

import java.util.List;

import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.ServerInfo;
import com.netcrest.pado.link.IGridBizLink;

/**
 * IGridGiz is a grid-to-grid biz object that provides a means to
 * synchronize grid information.
 * @author dpark
 *
 */
@BizClass(name = "IGridBiz")
public interface IGridBiz extends IGridBizLink
{
	/**
	 * Returns GridInfo from the grid.
	 */
	@BizMethod
	@OnServer(broadcast = false, connectionName = "pado")
	GridInfo getGridInfo();
	
	/**
	 * Publishes GridInfo to the parent grids.
	 * @param gridInfo This server's GridInfo to publish to all parent grids.
	 */
	@BizMethod
	@OnServer(broadcast = true, connectionName = "pado")
	void publishGridInfo(GridInfo gridInfo);
	
	/**
	 * Returns the list of ServerInfo objects for the specified full path.
	 * @param fullPath Full path
	 */
	@BizMethod
	@OnServer
	@WithGridCollector(gridCollectorClass="com.netcrest.pado.biz.collector.CollectionCollector")
	List<ServerInfo> getServerInfoList(String fullPath);
	
}
