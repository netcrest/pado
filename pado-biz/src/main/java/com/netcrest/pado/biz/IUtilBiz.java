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

import java.util.Date;
import java.util.List;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnPath;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.RouterType;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.biz.data.ServerLoad;
import com.netcrest.pado.info.CacheDumpInfo;
import com.netcrest.pado.info.WhichInfo;
import com.netcrest.pado.link.IUtilBizLink;

/**
 * IUtilBiz provides utility methods for interacting with the grid.
 * <p>
 * <b>Arguments:
 * {@link IBizLocal#init(IBiz, com.netcrest.pado.IPado, Object...)}</b>
 * <p>
 * <blockquote> <b>boolean gridPath</b> - Optional grid path.</blockquote>
 * 
 * @author dpark
 * 
 */
@BizClass(name = "IUtilBiz")
public interface IUtilBiz extends IUtilBizLink
{
	/**
	 * Pings a server with a payload. The server returns the same payload. This
	 * method is useful for determining the round trip time.
	 * 
	 * @param payload
	 *            Any byte array payload
	 * @return Returns the same byte array payload that was passed in as the
	 *         input argument.
	 */
	@BizMethod
	@OnServer
	byte[] ping(byte[] payload);

	/**
	 * Echos the specified message. This method is similar to
	 * {@link #ping(byte[])} except that it delivers a String message and the
	 * receiving server logs the message as info demarcated with the method
	 * name.
	 * 
	 * @param message
	 *            Message to deliver to a server.
	 * @return The same message returned by the server.
	 */
	@BizMethod
	@OnServer(routerType = RouterType.COST)
	String echo(String message);

	/**
	 * Returns a ServerLoad object that contain server load information.
	 */
	@BizMethod
	@OnServer
	ServerLoad getServerLoad();

	/**
	 * Returns the current system time of one of the servers.
	 */
	@BizMethod
	@OnServer
	Date getServerTime();

	/**
	 * Dumps the contents of all of the valid paths to the directory designated
	 * by the server startup scripts.
	 * 
	 * @return Server dump directory paths
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.NonNullCollector")
	List<String> dumpAll();

	/**
	 * Dumps the contents of the specified paths in all servers to the directory
	 * designated by the server startup scripts.
	 * 
	 * @param gridPaths
	 *            Grid paths
	 * @return Server CSV file paths
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.NonNullCollector")
	List<String> dumpServers(String... gridPaths);

	/**
	 * Imports data from the latest dump files created by {@link #dumpAll()} in
	 * each server.
	 * 
	 * @return Server dump directory paths
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.NonNullCollector")
	List<String> importAll();

	/**
	 * Imports data from the dump files created by {@link #dumpAll()} from each
	 * server with time stamp less than or equal to the specified as-of date.
	 * 
	 * @param asOfDate
	 *            As-of date. If null, then it imports the latest dump.
	 * @return Server dump directory paths
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.NonNullCollector")
	List<String> importAll(Date asOfDate);

	/**
	 * Imports data from the latest dump files created by either
	 * {@link #dumpAll()} or {@link #dumpServers(String...)} from each server.
	 * 
	 * @param gridPaths
	 *            Grid paths
	 * @param isAll
	 *            true to import data from the "all" archive, i.e., archive
	 *            files created by {@link #dumpAll()}, false to import data from
	 *            the "path" archive, i.e., archive files created by
	 *            {@link #dumpServers(String...)}.
	 * @return Server CSV file paths
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.NonNullCollector")
	List<String> importServers(boolean isAll, String... gridPaths);

	/**
	 * Imports data from the dump files created by either {@link #dumpAll()} or
	 * {@link #dumpServers(String...)} from each server with time stamp less
	 * than or equal to the specified as-of date.
	 * 
	 * @param asOfDate
	 *            As-of date. If null, then it imports the latest dump.
	 * @param isAll
	 *            true to import data from the "all" archive, i.e., archive
	 *            files created by {@link #dumpAll()}, false to import data from
	 *            the "path" archive, i.e., archive files created by
	 *            {@link #dumpServers(String...)}.
	 * @param gridPaths
	 *            Grid paths
	 * @return Imported CSV file paths
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.NonNullCollector")
	List<String> importServers(Date asOfDate, boolean isAll, String... gridPaths);

	/**
	 * Returns a list of all top-level DumpInfo objects in each server. Always
	 * returns a non-null list.
	 * 
	 * @param isAll
	 *            true to list files from the "all" archive, i.e., archive files
	 *            created by {@link #dumpAll()}, false to list files from the
	 *            "path" archive, i.e., archive files created by
	 *            {@link #dumpServers(String...)}.
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.NonNullCollector")
	public List<CacheDumpInfo> getCacheDumpInfoList(boolean isAll);

	/**
	 * Returns a single server's CacheDumpInfo object that contains that
	 * server's archived file information. The returned object reflects archived
	 * files in all servers as each server typically has identical files except
	 * for their sizes.
	 * 
	 * @param isAll
	 *            true to list files from the "all" archive, i.e., archive files
	 *            created by {@link #dumpAll()}, false to list files from the
	 *            "path" archive, i.e., archive files created by
	 *            {@link #dumpServers(String...)}.
	 */
	@BizMethod
	@OnServer
	public CacheDumpInfo getCacheDumpInfo(boolean isAll);

	/**
	 * Executes the specified query on the server that is targeted by the
	 * specified routing key.
	 * 
	 * @param queryString
	 *            Query string. Pado supports a number of query languages.
	 *            Please the the User's Guide for supported query languages.
	 * @param routingKey
	 *            Routing key that targets the server that has the data to query
	 *            on.
	 * @return Results in the form of List. Always returns a non-null List.
	 */
	@SuppressWarnings("rawtypes")
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	List executeRoutingQuery(String queryString, Object routingKey);

	/**
	 * Executes the specified query on all of the servers. It performs a
	 * "scatter-gather" query.
	 * 
	 * @param queryString
	 *            Query string. Pado supports a number of query languages.
	 *            Please the the User's Guide for supported query languages.
	 * @return Aggregated results in the form of List. Always returns a non-null
	 *         List.
	 */
	@SuppressWarnings("rawtypes")
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	List executeQuery(String queryString);

	/**
	 * Finds the servers that have the specified key in the specified grid path.
	 * 
	 * @param gridPath
	 *            Grid path
	 * @param key
	 *            Key to find. A key can be in any form as long as it follows
	 *            the Map conventions in which it must implements hashCode() and
	 *            equals().
	 * @return A list of WhichInfo objects that contain the value and server
	 *         info of primary and redundant copies. If the specified grid path
	 *         is not partitioned then it returns all WhichInfo also includes
	 *         bucket info if the underlying data grid supports the distributed
	 *         bucket concept.
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.NonNullCollector")
	public List<WhichInfo> which(String gridPath, Object key);

	/**
	 * Finds the server that is targeted by the specified routing key.
	 * 
	 * @param gridPath
	 *            Grid path
	 * @param routingKey
	 *            Routing key that determines the server in which the pertient
	 *            data exists.
	 * @return A WhichInfo that contains the server info. Note that the returned
	 *         object does not include the value and bucket info. To get a
	 *         complete info, invoke {@link #which(String, Object)}, instead.
	 */
	@BizMethod
	@OnPath
	public WhichInfo whichRoutingKey(String gridPath, Object routingKey);
}
