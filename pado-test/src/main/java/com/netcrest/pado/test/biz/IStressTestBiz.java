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
package com.netcrest.pado.test.biz;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;

@BizClass
public interface IStressTestBiz extends IBiz
{
	@SuppressWarnings("rawtypes")
	@BizMethod
	@OnServer(broadcast = true)
	List<String> __start(Map<String, JsonLite> pathConfigMap, int threadCountPerServer, int loopCount,
			boolean isIncludeObjectCreationTime);

	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.BooleanAndCollector")
	boolean isComplete();

	@BizMethod
	@OnServer(broadcast = true)
	List<String> getStatus();

	/**
	 * Starts the stress test. Only one instance of stress test at any given
	 * time is allowed. If the stress test is already in progress then this call
	 * is aborted. See status message.
	 * 
	 * @return Status message
	 */
	List<String> start();

	void addPath(String path);

	/**
	 * Adds the specified full path to the stress test driver.
	 * 
	 * @param fullPath
	 *            Full path. Note that unlike other overloaded methods, this
	 *            method requires a full path which must be configured by the
	 *            grid in server.xml. By doing so, a separate client driver grid
	 *            can be created to connect to the grid being tested. This
	 *            allows the servers in the driver grid to connect as clients
	 *            (using GemFire pool, for example) and concurrently perform
	 *            stress tests.
	 * @param payloadSize
	 *            Object size in bytes
	 * @param fieldCount
	 *            Field count in each object
	 * @param updateIntervalInMsec
	 *            Update interval between batches
	 * @param totalEntryCount
	 *            Total number of entries
	 * @param batchSize
	 *            Bulkload batch size
	 */
	void addPath(String fullPath, int payloadSize, int fieldCount, int updateIntervalInMsec, int totalEntryCount,
			int batchSize);

	void addPath(String path, IPathBiz.PathType pathType, int payloadSize, int fieldCount, int updateIntervalInMsec,
			int totalEntryCount, int batchSize);

	void addPath(String path, String refid, boolean isTemporal, boolean isLuceneDynamic, int payloadSize,
			int fieldCount, int updateIntervalInMsec, int totalEntryCount, int batchSize);

	/**
	 * Adds the specified path configuration.
	 * 
	 * @param pathConfig
	 *            JsonLite object that contains one of the the following field
	 *            lists:
	 *            <ul>
	 *            <li>Path, PathType, PayloadSize, FieldCount,
	 *            UpdateIntervalInMsec, TotalEntryCount, BatchSize</li>
	 *            <li>Path, Refid, IsTemporal, IsLuceneDynamic, PayloadSize,
	 *            FieldCount, UpdateIntervalInMsec, TotalEntryCount, BatchSize
	 *            </li>
	 *            <li>Path (full path), PayloadSize, FieldCount,
	 *            UpdateIntervalInMsec, TotalEntryCount, BatchSize</li>
	 *            </ul>
	 */
	@SuppressWarnings({ "rawtypes" })
	void addPath(JsonLite pathConfig);

	int getPathCount();

	Set<String> getPathSet();

	void removePath(String path);

	void removeAllPaths();

	void clearPath(String path);

	void clearAllPaths();

	int getThreadCountPerDriver();

	void setThreadCountPerDriver(int threadCountPerServer);

	int getBatchSize();

	void setBatchSize(int batchSize);
	
	int getUpdateIntervalInMsec();

	void setUpdateIntervalInMsec(int updateIntervalInMsec);

	boolean isIncludeObjectCreationTime();

	void setIncludeObjectCreationTime(boolean isIncludeObjectCreationTime);

	/**
	 * Returns the loop count. All paths are loaded with data before looping or
	 * repeating. The default value is 1. Less than 1 is forced to 1.
	 */
	int getLoopCount();

	void setLoopCount(int loopCount);
}
