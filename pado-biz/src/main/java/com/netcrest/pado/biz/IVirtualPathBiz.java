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

import java.util.Map;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IVirtualPath;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.data.KeyMap;

/**
 * IVirtualPathBiz provides a means to access and manage virtual paths from the
 * application. Virtual paths can be created, updated, and removed.
 * 
 * <p>
 * <b>Arguments:
 * {@link IBizLocal#init(IBiz, com.netcrest.pado.IPado, Object...)}</b>
 * <p>
 * <blockquote> <b>String gridId</b> - Optional grid ID. If not
 * specified or null, then the default grid ID is assigned. </blockquote>
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
@BizClass(name = "IVirtualPathBiz")
public interface IVirtualPathBiz<T> extends IBiz
{
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
}
