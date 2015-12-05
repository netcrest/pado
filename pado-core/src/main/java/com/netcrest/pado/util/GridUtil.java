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
package com.netcrest.pado.util;

import com.netcrest.pado.server.PadoServerManager;

public class GridUtil
{
	/**
	 * Returns the child path without the root path. The returned path does not
	 * begin with "/". It returns an empty string if fullPath contains only the
	 * root path. It returns null if fullPath is null.
	 * 
	 * @param fullPath
	 *            Absolute path that includes grid root path.
	 */
	public static String getChildPath(String fullPath)
	{
		if (fullPath == null) {
			return null;
		}
		String childPath = fullPath.replaceFirst("^?/.*?/", "");
		if (childPath.startsWith("/")) {
			childPath = "";
		}
		return childPath;
	}

	/**
	 * Returns the root name of the specified path. The returned value does not
	 * contain "/". It returns null if fullPath is null or does not begin with
	 * "/".
	 * 
	 * @param fullPath
	 *            Absolute path. Must begin with "/".
	 */
	public static String getRootName(String fullPath)
	{
		if (fullPath == null) {
			return null;
		}
		if (fullPath.startsWith("/")) {
			String[] split = fullPath.split("/");
			return split[1];
		} else {
			return null;
		}
	}

	/**
	 * Returns the root path of the specified path. The returned value begins
	 * with "/". It returns null if fullPath is null or does not begin with "/".
	 * 
	 * @param fullPath
	 *            Absolute path. Must begin with "/".
	 */
	public static String getRootPath(String fullPath)
	{
		if (fullPath == null) {
			return null;
		}
		if (fullPath.startsWith("/")) {
			int index = fullPath.indexOf('/', 1);
			if (index == -1) {
				return fullPath;
			}
			return fullPath.substring(0, index);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the full path of the specified grid path. It returns null if the
	 * grid path is undefined or this VM is a pure client. Use this method only
	 * in the server side.
	 * 
	 * @param gridPath
	 *            grid path
	 */
	@SuppressWarnings("rawtypes")
	public static String getFullPath(String gridPath)
	{
		if (gridPath == null) {
			return null;
		}
		if (PadoServerManager.getPadoServerManager() == null) {
			return null;
		}
		String rootPath = PadoServerManager.getPadoServerManager().getGridInfo().getGridRootPath();
		if (rootPath == null) {
			return null;
		}
		if (gridPath.startsWith("/")) {
			return rootPath + gridPath;
		} else {
			return rootPath + "/" + gridPath;
		}
	}
}
