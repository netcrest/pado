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
package com.netcrest.pado.internal.config.dtd;

import java.util.ArrayList;
import java.util.List;

import com.netcrest.pado.internal.config.dtd.generated.Path;
import com.netcrest.pado.internal.config.dtd.generated.PathList;

/**
 * PathListConfig provides information in addition to the generated
 * {@link PathListConfig} class.
 * 
 * @author dpark
 * 
 */
public class PathListConfig
{
	private PathList pathList;
	private List<PathConfig> pathListConfig;

	/**
	 * Constructs a PathListConfig object with the specified path list.
	 * 
	 * @param pathList Path list
	 */
	public PathListConfig(PathList pathList)
	{
		this.pathList = pathList;
		createPathListConfig();
	}

	/**
	 * Creates and assigns a PathListConfig object to pathListConfig.
	 */
	private void createPathListConfig()
	{
		if (pathList != null) {
			List<Path> paths = pathList.getPath();
			pathListConfig = new ArrayList();
			for (Path path : paths) {
				PathConfig pathConfig = new PathConfig(path, null);
				pathListConfig.add(pathConfig);
			}
		}
	}

	/**
	 * Returns the Path ID
	 */
	public String getId()
	{
		if (pathList != null) {
			return pathList.getId();
		} else {
			return null;
		}
	}

	/**
	 * Sets the PathList ID
	 * @param id PathList ID
	 */
	public void setId(String id)
	{
		if (pathList != null) {
			pathList.setId(id);
		}
	}

	/**
	 * Returns the PathConfig object
	 */
	public List<PathConfig> getPathConfig()
	{
		return pathListConfig;
	}

	/**
	 * Returns the PathList description
	 */
	public String getDescription()
	{
		if (pathList != null) {
			return pathList.getDescription();
		} else {
			return null;
		}
	}

	/**
	 * Sets the PathList description
	 * @param description PathList descrition
	 */
	public void setDescription(String description)
	{
		if (pathList != null) {
			pathList.setDescription(description);
		}
	}
}
