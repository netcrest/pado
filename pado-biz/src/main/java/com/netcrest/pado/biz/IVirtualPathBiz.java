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
import java.util.Map;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IVirtualPath;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.index.service.IScrollableResultSet;

/**
 * IVirtualPathBiz provides a means to access and manage virtual paths from the
 * application. Virtual paths can be created, updated, and removed.
 * 
 * <p>
 * <b>Arguments:
 * {@link IBizLocal#init(IBiz, com.netcrest.pado.IPado, Object...)}</b>
 * <p>
 * <blockquote> <b>String gridId</b> - Optional grid ID. If not specified or
 * null, then the default grid ID is assigned. </blockquote>
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
@BizClass(name = "IVirtualPathBiz")
public interface IVirtualPathBiz<T> extends IBiz
{
	public static int MAX_DEPTH = 5;

	/**
	 * <b>Internal use only</b>
	 * <p>
	 * Executes the specified virtual path using the specified arguments as
	 * inputs.
	 * 
	 * @param virtualPath
	 *            Virtual path
	 * @param depth
	 *            Depth of the entity object graph. It has meaning only if the
	 *            virtual path is an entity. Valid values are [-1, 5] inclusive.
	 *            If less than -1 then -1 is assumed. If greater than 5 then 5
	 *            is assumed. -1 to start search from the beginning of the
	 *            depth(s) defined by the virtual path, 0 to search with no
	 *            depth, >=1 to search depth.
	 * @param validAt
	 *            Valid at time. -1 for now-relative
	 * @param asOf
	 *            As of time. -1 for now-relative
	 * @param args
	 *            Arguments
	 * @return Results of the virtual path execution
	 */
	@BizMethod
	@OnServer
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	List<T> __execute(String virtualPath, int depth, long validAt, long asOf, String... args);

	/**
	 * <b>Internal use only</b>
	 * <p>
	 * Executes the query on the entity grid path (EntityGridPath) that the
	 * specified virtual path has entity relationships.
	 * 
	 * @param virtualPath
	 *            Virtual path
	 * @param depth
	 *            Depth of the entity object graph. It has meaning only if the
	 *            virtual path is an entity. Valid values are [-1, 5] inclusive.
	 *            If less than -1 then -1 is assumed. If greater than 5 then 5
	 *            is assumed. -1 to start search from the beginning of the
	 *            depth(s) defined by the virtual path, 0 to search with no
	 *            depth, >=1 to search depth.
	 * @param validAt
	 *            Valid at time. -1 for now-relative
	 * @param asOf
	 *            As of time. -1 for now-relative
	 * @param args
	 *            Arguments to EntityGridPath
	 * @return Results of the query on EntityGridPath
	 */
	@BizMethod
	@OnServer
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	List<T> __executeEntity(String virtualPath, int depth, long validAt, long asOf, String... args);

	/**
	 * <b>Internal use only</b>
	 * <p>
	 * Executes the specified virtual definition in the grid. Note that this
	 * method is not optimal as other execution methods as it compiles the
	 * definition before executing the query.
	 * 
	 * @param vpd
	 *            Virtual path definition
	 * @param depth
	 *            Depth of the entity object graph. It has meaning only if the
	 *            virtual path is an entity. Valid values are [-1, 5] inclusive.
	 *            If less than -1 then -1 is assumed. If greater than 5 then 5
	 *            is assumed. -1 to start search from the beginning of the
	 *            depth(s) defined by the virtual path, 0 to search with no
	 *            depth, >=1 to search depth.
	 * @param validAt
	 *            Valid at time. -1 for now-relative
	 * @param asOf
	 *            As of time. -1 for now-relative
	 * @param args
	 *            Arguments to all variables defined in the specified virtual
	 *            path definition.
	 * @return
	 */
	@BizMethod
	@OnServer
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	List<T> __executeVirtualPathDefinition(KeyMap vpd, int depth, long validAt, long asOf, String... args);

	/**
	 * Returns the virtual path definition defined in the server. It returns
	 * null if the specified virtual path is not found.
	 * 
	 * @param virtualPath
	 *            Virtual path. Virtual path must not begin with '/'.
	 */
	@BizMethod
	@OnServer
	public KeyMap getVirtualPathDefinition(String virtualPath);

	/**
	 * Returns a new instance of VirtualPath with the thread pool size
	 * automatically determined. This method call is analogous to
	 * getVirtualPath(virtualPath, -1). Returns null if the virtual path is not
	 * found.
	 * 
	 * @param virtualPath
	 *            Virtual path. Virtual path must not begin with '/'.
	 */
	public IVirtualPath<T> getVirtualPath(String virtualPath);

	/**
	 * Returns a new instance of VirtualPath with the specified thread pool
	 * size. Returns null if the virtual path is not found.
	 * 
	 * @param virtualPath
	 *            Virtual path. Virtual path must not begin with '/'.
	 * @param threadPoolSize
	 *            Thread pool size for this virtual path. If the thread size is
	 *            > 0 then VirtualPath creates a thread pool of that size and
	 *            parallelizes query executions where possible to attain the
	 *            best performance. Note that setting this value to a large
	 *            number can negatively impact the performance especially if the
	 *            caller is already highly multi-threaded. As a rule of thumb,
	 *            the thread pool size should be less than or equal to the max
	 *            number of orthogonal queries defined in the VPD. If < 0 then
	 *            it automatically determines the thread pool size.
	 */
	public IVirtualPath<T> getVirtualPath(String virtualPath, int threadPoolSize);

	/**
	 * Registers the specified virtual path definition in the server. Note that
	 * it overwrites the existing virtual path definition with the same virtual
	 * path.
	 * 
	 * @param virtualPathDefinition
	 *            Virtual Path Definition
	 */
	@BizMethod
	@OnServer(broadcast = true)
	public void addVirtualPathDefinition(KeyMap virtualPathDefinition);

	/**
	 * Removes (unregisters) the specified virtual path from the server.
	 * 
	 * @param virtualPath
	 *            Virtual path. Virtual path must not begin with '/'.
	 */
	@BizMethod
	@OnServer(broadcast = true)
	public void removeVirtualPathDefinition(String virtualPath);

	/**
	 * Returns a sorted array of all server registered virtual paths.
	 */
	@BizMethod
	@OnServer
	public String[] getAllVirtualPaths();

	/**
	 * Returns a map of all server-registered (virtual path, virtual definition)
	 * paired entries.
	 */
	@BizMethod
	@OnServer
	public Map<String, KeyMap> getAllVirtualPathDefinitions();

	/**
	 * Executes the specified virtual path using the specified arguments as
	 * inputs.
	 * 
	 * @param virtualPath
	 *            Virtual path
	 * @param validAt
	 *            Valid at time. -1 for now-relative
	 * @param asOf
	 *            As of time. -1 for now-relative
	 * @param args
	 *            Arguments
	 * @return Results of the virtual path execution
	 */
	public IScrollableResultSet<T> execute(String virtualPath, long validAt, long asOf, String... args);

	/**
	 * Executes the specified virtual path using the specified arguments as
	 * inputs.
	 * 
	 * @param virtualPath
	 *            Virtual path
	 * @param depth
	 *            Depth of the entity object graph. It has meaning only if the
	 *            virtual path is an entity. Valid values are [-1, 5] inclusive.
	 *            If less than -1 then -1 is assumed. If greater than 5 then 5
	 *            is assumed. -1 to start search from the beginning of the
	 *            depth(s) defined by the virtual path, 0 to search with no
	 *            depth, >=1 to search depth.
	 * @param validAt
	 *            Valid at time. -1 for now-relative
	 * @param asOf
	 *            As of time. -1 for now-relative
	 * @param args
	 *            Arguments
	 * @return Results of the virtual path execution
	 */
	public IScrollableResultSet<T> execute(String virtualPath, int depth, long validAt, long asOf, String... args);

	/**
	 * Executes the query on the entity grid path (EntityGridPath) that the
	 * specified virtual path has entity relationships.
	 * 
	 * @param virtualPath
	 *            Virtual path
	 * @param validAt
	 *            Valid at time. -1 for now-relative
	 * @param asOf
	 *            As of time. -1 for now-relative
	 * @param args
	 *            Arguments to EntityGridPath
	 * @return Results of the query on EntityGridPath
	 */
	public IScrollableResultSet<T> executeEntity(String virtualPath, long validAt, long asOf, String... args);

	/**
	 * Executes the query on the entity grid path (EntityGridPath) that the
	 * specified virtual path has entity relationships.
	 * 
	 * @param virtualPath
	 *            Virtual path
	 * @param depth
	 *            Depth of the entity object graph. It has meaning only if the
	 *            virtual path is an entity. Valid values are [-1, 5] inclusive.
	 *            If less than -1 then -1 is assumed. If greater than 5 then 5
	 *            is assumed. -1 to start search from the beginning of the
	 *            depth(s) defined by the virtual path, 0 to search with no
	 *            depth, >=1 to search depth.
	 * 
	 * @param validAt
	 *            Valid at time. -1 for now-relative
	 * @param asOf
	 *            As of time. -1 for now-relative
	 * @param args
	 *            Arguments to EntityGridPath
	 * @return Results of the query on EntityGridPath
	 */
	public IScrollableResultSet<T> executeEntity(String virtualPath, int depth, long validAt, long asOf,
			String... args);

	/**
	 * Executes the specified virtual definition in the grid. Note that this
	 * method is not optimal as other execution methods as it compiles the
	 * definition before executing the query.
	 * 
	 * @param vpd
	 *            Virtual path definition
	 * @param depth
	 *            Depth of the entity object graph. It has meaning only if the
	 *            virtual path is an entity. Valid values are [-1, 5] inclusive.
	 *            If less than -1 then -1 is assumed. If greater than 5 then 5
	 *            is assumed. -1 to start search from the beginning of the
	 *            depth(s) defined by the virtual path, 0 to search with no
	 *            depth, >=1 to search depth.
	 * @param validAt
	 *            Valid at time. -1 for now-relative
	 * @param asOf
	 *            As of time. -1 for now-relative
	 * @param args
	 *            Arguments to all variables defined in the specified virtual
	 *            path definition.
	 * @return
	 */
	public IScrollableResultSet<T> executeVirtualPathDefinition(KeyMap vpd, int depth, long validAt, long asOf,
			String... args);
}
