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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.netcrest.pado.biz.file.CompositeKeyInfo;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.config.dtd.generated.CompositeKey;
import com.netcrest.pado.internal.config.dtd.generated.Path;

/**
 * PathConfig provides information in addition to the generated
 * {@link PathConfig} class.
 * 
 * @author dpark
 * 
 */
public class PathConfig
{
	public final static String DEFAULT_DATA_TYPE = "replicated";
	public final static String DEFAULT_ACCESS_TYPE = "private";

	private Path path;
	private PathConfig parentPathConfig;
	private List<PathConfig> pathConfigList;
	String gridPath;

	/**
	 * Constructs a PathConfig object with the specified path and its parent
	 * path.
	 * 
	 * @param path
	 *            Path
	 * @param parent
	 *            Parent path
	 */
	public PathConfig(Path path, PathConfig parentPathConfig)
	{
		this.path = path;
		this.parentPathConfig = parentPathConfig;
		if (parentPathConfig == null) {
			gridPath = path.getName();
		} else {
			gridPath = parentPathConfig.gridPath + "/" + path.getName();
		}
		createPathConfig();
	}

	private void createPathConfig()
	{
		if (path != null) {
			List<Path> paths = path.getPath();
			pathConfigList = new ArrayList(paths.size());
			for (Path child : paths) {
				PathConfig pathConfig = new PathConfig(child, this);
				pathConfigList.add(pathConfig);
			}
		}
	}

	/**
	 * Returns the parent path
	 */
	public Path getParent()
	{
		if (parentPathConfig == null) {
			return null;
		}
		return parentPathConfig.getPath();
	}

	/**
	 * Sets the parent path config
	 * 
	 * @param parent
	 *            Parent path config
	 */
	public void setParentPathConfig(PathConfig parentPathConfig)
	{
		this.parentPathConfig = parentPathConfig;
	}

	/**
	 * Returns the path
	 */
	public Path getPath()
	{
		return path;
	}

	/**
	 * Sets the path
	 */
	public void setPath(Path path)
	{
		this.path = path;
	}

	/**
	 * Returns the list of PathConfig objects
	 */
	public List<PathConfig> getPathConfig()
	{
		return pathConfigList;
	}

	/**
	 * Returns the name of the path
	 */
	public String getName()
	{
		if (path != null) {
			return path.getName();
		} else {
			return null;
		}
	}

	/**
	 * Sets the path name
	 * 
	 * @param name
	 *            Path name
	 */
	public void setName(String name)
	{
		if (path != null) {
			path.setName(name);
		}
	}

	/**
	 * Returns the grid path
	 * 
	 * @return
	 */
	public String getGridPath()
	{
		return gridPath;
	}

	/**
	 * Returns true if this grid path inherits its parent grid path config.
	 * Unlike {@link #isTemporalEnabled()}, the default value is true.
	 */
	public boolean isInherit()
	{
		return path == null || path.getInherit() == null || path.getInherit().equals("true");
	}

	/**
	 * Returns "true" if this grid path inherits its parent's config info. It
	 * returns null if undefined.
	 */
	public String getInherit()
	{
		if (path != null) {
			return path.getInherit();
		} else {
			return null;
		}
	}

	/**
	 * Enables or disables inheritance of this grid path's parent config info.
	 * 
	 * @param inherit
	 *            "true" to inherit, else to disable.
	 */
	public void setInherit(String inherit)
	{
		if (path != null) {
			path.setInherit(inherit);
		}
	}

	/**
	 * Returns the data type
	 */
	public String getDataType()
	{
		String dataType = path.getDataType();
		if (dataType == null) {
			if (parentPathConfig != null && isInherit()) {
				dataType = parentPathConfig.getDataType();
			} else {
				dataType = DEFAULT_DATA_TYPE;
			}
		}
		return dataType;
	}

	/**
	 * Sets the daata type
	 * 
	 * @param dataType
	 *            Data type
	 */
	public void setDataType(String dataType)
	{
		if (path != null) {
			path.setDataType(dataType);
		}
	}

	/**
	 * Returns the access type
	 */
	public String getAccessType()
	{
		String accessType = path.getAccessType();
		if (accessType == null) {
			if (parentPathConfig != null && isInherit()) {
				accessType = parentPathConfig.getAccessType();
			} else {
				accessType = DEFAULT_ACCESS_TYPE;
			}
		}
		return accessType;
	}

	/**
	 * Sets the access type
	 * 
	 * @param accessType
	 *            Access type
	 */
	public void setAccessType(String accessType)
	{
		if (path != null) {
			path.setAccessType(accessType);
		}
	}

	/**
	 * Sets the grid IDs separated by a space.
	 * 
	 * @param grids
	 */
	public void setGrids(String grids)
	{
		if (path != null) {
			path.setGrids(grids);
		}
	}

	/**
	 * Returns the grid ID set. It returns null if undefined.
	 */
	public Set<String> getGrids()
	{
		String gridIds = path.getGrids();
		if (gridIds == null) {
			return null;
		}
		String split[] = gridIds.split(" ");
		HashSet<String> gridIdSet = new HashSet<String>(split.length, 1f);
		for (String gridId : split) {
			gridIdSet.add(gridId);
		}
		return gridIdSet;
	}

	/**
	 * Returns true if temporal data is enabled for this grid path. Unlike
	 * {@link #isInherit()}, if undefined then the default value is false.
	 */
	public boolean isTemporalEnabled()
	{
		return path != null && path.getTemporalEnabled() != null && path.getTemporalEnabled().equals("true");
	}

	/**
	 * Returns "true" if temporal data for this grid path is enabled in the
	 * config file. It returns null if undefined.
	 */
	public String getTemporalEnabled()
	{
		if (path != null) {
			return path.getTemporalEnabled();
		} else {
			return null;
		}
	}

	/**
	 * Enables or disables temporal data for this grid path.
	 * 
	 * @param enabled
	 *            "true" to enable, else to disable.
	 */
	public void setTemporalEnabled(String enabled)
	{
		if (path != null) {
			path.setTemporalEnabled(enabled);
		}
	}

	/**
	 * Returns true if Lucene indexes are to be built for this grid path. Unlike
	 * {@link #isInherit()}, if undefined then the default value is false.
	 */
	public boolean isLuceneEnabled()
	{
		return path != null && path.getLuceneEnabled() != null && path.getLuceneEnabled().equals("true");
	}

	/**
	 * Returns "true" if Lucene indexes are to be built for this grid path. It
	 * returns null if undefined.
	 */
	public String getLuceneEnabled()
	{
		if (path != null) {
			return path.getLuceneEnabled();
		} else {
			return null;
		}
	}

	/**
	 * Enables or disables Lucene indexes for this grid path.
	 * 
	 * @param temporal
	 *            "true" to enable, else to disable.
	 */
	public void setLuceneEnabled(String enabled)
	{
		if (path != null) {
			path.setLuceneEnabled(enabled);
		}
	}

	/**
	 * Returns the fully-qualified primitive class name if the specified class
	 * name is a primitive class. Otherwise, it returns the specified class name
	 * unchanged.
	 * 
	 * @param className
	 *            Class name
	 */
	private String getClassName(String className)
	{
		if (className == null) {
			return null;
		}
		if (className.equals("String")) {
			return "java.lang.String";
		} else if (className.equals("boolean")) {
			return "java.lang.Boolean";
		} else if (className.equals("byte")) {
			return "java.lang.Byte";
		} else if (className.equals("short")) {
			return "java.lang.Short";
		} else if (className.equals("int")) {
			return "java.lang.Integer";
		} else if (className.equals("float")) {
			return "java.lang.Float";
		} else if (className.equals("long")) {
			return "java.lang.Long";
		} else if (className.equals("double")) {
			return "java.lang.Double";
		} else {
			return className;
		}
	}

	/**
	 * Returns the key class name
	 */
	public String getKeyClassName()
	{
		if (path == null) {
			return null;
		}
		return getClassName(path.getKeyClassName());
	}

	/**
	 * Sets the key class name
	 * 
	 * @param keyClassName
	 *            Key class name
	 */
	public void setKeyClassName(String keyClassName)
	{
		if (path != null) {
			path.setKeyClassName(keyClassName);
		}
	}

	/**
	 * Returns the value class name
	 */
	public String getValueClassName()
	{
		if (path == null) {
			return null;
		}
		return getClassName(path.getValueClassName());
	}

	/**
	 * Sets the value class name
	 * 
	 * @param valueClassName
	 *            Value class name
	 */
	public void setValueClassName(String valueClassName)
	{
		if (path != null) {
			path.setValueClassName(valueClassName);
		}
	}

	/**
	 * Returns the router class name
	 */
	public String getRouterClassName()
	{
		String routerClassName = path.getRouterClassName();
		if (routerClassName == null) {
			if (parentPathConfig != null && isInherit()) {
				routerClassName = parentPathConfig.getRouterClassName();
			} else {
				routerClassName = Constants.DEFAULT_CLASS_ROUTER;
			}
		}
		return routerClassName;
	}

	/**
	 * Sets the router class name
	 * 
	 * @param routerClassName
	 *            Router class name
	 */
	public void setRouterClassName(String routerClassName)
	{
		if (path != null) {
			path.setRouterClassName(routerClassName);
		}
	}

	/**
	 * Returns the composite key info
	 */
	public CompositeKeyInfo getCompositeKeyInfo()
	{
		if (path != null) {
			CompositeKey ck = path.getCompositeKey();
			if (ck == null) {
				return null;
			}
			CompositeKeyInfo ckInfo = new CompositeKeyInfo();
			String indexesStr = ck.getIndexesCommaSeparated();
			try {
				if (indexesStr != null && indexesStr.length() > 0) {
					String split[] = indexesStr.split(",");
					int[] indexes = new int[split.length];
					for (int i = 0; i < split.length; i++) {
						indexes[i] = Integer.parseInt(split[i]);
					}
					ckInfo.setRoutingKeyIndexes(indexes);
				}
			} catch (Exception ex) {
				throw new PadoException("Error parsing composite key indexes: path=" + this.gridPath, ex);
			}
			if (ck.getDelimiter() != null && ck.getDelimiter().length() > 0) {
				ckInfo.setCompositeKeyDelimiter(ck.getDelimiter());
			}
			return ckInfo;
		} else {
			return null;
		}
	}

	/**
	 * Sets the composite key
	 * 
	 * @param compositeKey
	 *            Composite key
	 */
	public void setCompositeKey(CompositeKey compositeKey)
	{
		if (path != null) {
			path.setCompositeKey(compositeKey);
		}
	}

	/**
	 * Returns the type description
	 */
	public String getDescription()
	{
		if (path != null) {
			return path.getDescription();
		} else {
			return null;
		}
	}

	/**
	 * Sets the type description
	 * 
	 * @param description
	 *            Type description
	 */
	public void setDescription(String description)
	{
		if (path != null) {
			path.setDescription(description);
		}
	}
}
