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
package com.netcrest.pado.tools.pado;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.PadoInfo;
import com.netcrest.pado.info.PathInfo;

public class RootPathInfo extends PathInfo
{
	public RootPathInfo(PadoInfo padoInfo)
	{
		this.fullPath = "/";
		this.name = "";
		this.parent = null;
		GridInfo gridInfo = padoInfo.getGridInfo();
		childList.add(gridInfo.getRootPathInfo());
		GridInfo[] childGridInfos = padoInfo.getChildGridInfos();
		for (GridInfo childGridInfo : childGridInfos) {
			childList.add(childGridInfo.getRootPathInfo());
		}
	}
	
	@Override
	public String getGridRelativePath()
	{
		return ".";
	}
	
	@Override
	public Set<String> getChildGridPathSet(boolean recursive)
	{
		return getChildGridPathSet(this, new HashSet(getChildCount() + 10), recursive, null, 0);
	}

	private Set<String> getChildGridPathSet(PathInfo pathInfo, Set<String> childGridPathSet, boolean recursive, String rootName, int level)
	{
		if (pathInfo == null) {
			return childGridPathSet;
		}
		level++;
		List<PathInfo> childList = pathInfo.getChildList();
		for (PathInfo child : childList) {
			String relativePath = child.getGridRelativePath();
			if (relativePath == null || relativePath.isEmpty()) {
				relativePath = child.getName();
			}
			if (level > 1) {
				relativePath = rootName + "/" + relativePath;
			} else {
				rootName = relativePath;
			}
			childGridPathSet.add(relativePath);
			if (recursive) {
				getChildGridPathSet(child, childGridPathSet, recursive, rootName, level);
			}
		}
		return childGridPathSet;
	}

}
