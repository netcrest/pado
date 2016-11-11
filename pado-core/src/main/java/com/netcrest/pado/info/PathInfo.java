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
package com.netcrest.pado.info;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.temporal.TemporalType;

/**
 * PathInfo provides grid path information.
 * 
 * @author dpark
 * 
 */
public abstract class PathInfo implements Comparable<PathInfo>
{
	/**
	 * Name of the path, i.e., the leaf part of the path.
	 */
	protected String name;

	/**
	 * Full path that includes the root path (if exists) that is not part of the
	 * grid path. Full paths always begin with "/".
	 */
	protected String fullPath;

	/**
	 * Parent PathInfo object.
	 */
	protected transient PathInfo parent; // transient to avoid a circular graph
											// during serialization

	/**
	 * List of child PathInfo objects.
	 */
	protected List<PathInfo> childList = new ArrayList<PathInfo>(10);

	/**
	 * PathAttributeInfo object containing path-specific attributes.
	 */
	protected PathAttributeInfo attrInfo;

	/**
	 * TemporalType. This maybe null if the path is not temporal-enabled.
	 */
	protected TemporalType temporalType;

	/**
	 * Key type name.
	 */
	protected String keyTypeName;

	/**
	 * Value type name.
	 */
	protected String valueTypeName;

	/**
	 * Content size of the path. Usually the entry count.
	 */
	protected int size;

	/**
	 * true if virtual path, false if physical path
	 */
	protected boolean isVirtualPath;

	// /**
	// * If true, this path is physically or virtually hosted by the grid.
	// * If false, this path is defined by other grids.
	// */
	// protected boolean isReal;

	/**
	 * Constructs an empty PathInfo object.
	 */
	public PathInfo()
	{
	}

	/**
	 * Returns all child grid paths, excluding itself.
	 * 
	 * @param recursive
	 *            true to include all nested paths, false to return only the
	 *            top-level paths
	 */
	public Set<String> getChildGridPathSet(boolean recursive)
	{
		return getChildGridPathSet(this, new HashSet<String>(getChildCount() + 10), recursive);
	}

	private Set<String> getChildGridPathSet(PathInfo pathInfo, Set<String> childGridPathSet, boolean recursive)
	{
		if (pathInfo == null) {
			return childGridPathSet;
		}
		List<PathInfo> childList = pathInfo.getChildList();
		for (PathInfo child : childList) {
			childGridPathSet.add(child.getGridRelativePath());
			if (recursive) {
				getChildGridPathSet(child, childGridPathSet, recursive);
			}
		}
		return childGridPathSet;
	}

	/**
	 * Returns the path name or the leaf part of the path.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the path name or the leaf part of the path.
	 * 
	 * @param name
	 *            Path name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the full path including the root path beginning with "/".
	 */
	public String getFullPath()
	{
		return fullPath;
	}

	/**
	 * Sets the full path that includes the root path.
	 * 
	 * @param fullPath
	 *            Full path
	 */
	public void setFullPath(String fullPath)
	{
		this.fullPath = fullPath;
	}

	/**
	 * Returns true if this path is a virtual path. Returns false if it is a
	 * physical path. Default is physical path.
	 */
	public boolean isVirtualPath()
	{
		return isVirtualPath;
	}

	/**
	 * Sets the path to virtual (true) or physical (false). Default is physical.
	 * 
	 * @param isVirtualPath
	 *            true to set virtual, false to set physical.
	 */
	public void setVirtualPath(boolean isVirtualPath)
	{
		this.isVirtualPath = isVirtualPath;
	}

	/**
	 * Returns grid relative path, i.e., without the top-level path name. The
	 * returned path never begins with "/".
	 */
	public abstract String getGridRelativePath();

	/**
	 * Returns the PathAttributeInfo object that contains path-specific
	 * attributes.
	 */
	public PathAttributeInfo getAttrInfo()
	{
		return attrInfo;
	}

	/**
	 * Sets the PathAttributeInfo object.
	 * 
	 * @param attrInfo
	 *            PathAttributeInfo object
	 */
	public void setAttrInfo(PathAttributeInfo attrInfo)
	{
		this.attrInfo = attrInfo;
	}

	/**
	 * Returns the path content size (entry count). If this path is partitioned
	 * then it returns the size of the local primary data set. Otherwise, it
	 * returns the total size.
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * Sets the path content size.
	 * 
	 * @param size
	 *            Path content size
	 */
	public void setSize(int size)
	{
		this.size = size;
	}

	/**
	 * Returns the list of child PathInfo objects.
	 */
	public List<PathInfo> getChildList()
	{
		return childList;
	}

	/**
	 * Sets the list of child PathInfo objects.
	 * 
	 * @param childList
	 *            List of child PathInfo objects
	 */
	public void setChildList(List<PathInfo> childList)
	{
		this.childList = childList;
	}

	/**
	 * Returns true if there are no child PathInfo objects.
	 */
	public boolean isLeaf()
	{
		return childList == null || childList.size() == 0;
	}

	/**
	 * Returns child PathInfo object count.
	 */
	public int getChildCount()
	{
		if (childList == null) {
			return 0;
		}
		return childList.size();
	}

	/**
	 * Returns the parent PathInfo object.
	 */
	public PathInfo getParent()
	{
		return parent;
	}

	/**
	 * Sets the parent PathInfo object.
	 * 
	 * @param parent
	 *            Parent PathInfo object
	 */
	public void setParent(PathInfo parent)
	{
		this.parent = parent;
	}

	/**
	 * Returns the root PathInfo object.
	 * 
	 * @return Root PathInfo object
	 */
	public PathInfo getRootPathInfo()
	{
		PathInfo rootPathInfo = getRootPathInfo(this);
		return rootPathInfo;
	}

	/**
	 * Returns the root PathInfo object of the specified path info.
	 * 
	 * @param pathInfo
	 *            Path info
	 */
	private PathInfo getRootPathInfo(PathInfo pathInfo)
	{
		if (pathInfo != null) {
			pathInfo = getRootPathInfo(pathInfo.getParent());
		}
		return pathInfo;
	}

	/**
	 * Returns the temporal type. It returns null if this path is not
	 * temporal-enabled.
	 */
	public TemporalType getTemporalType()
	{
		return temporalType;
	}

	/**
	 * Sets the temporal type.
	 * 
	 * @param temporalType
	 *            Temporal type
	 */
	public void setTemporalType(TemporalType temporalType)
	{
		this.temporalType = temporalType;
	}

	/**
	 * Returns the key type (class) name. It returns null if not defined.
	 */
	public String getKeyTypeName()
	{
		return keyTypeName;
	}

	/**
	 * Returns the value type (class) name. It returns null if not defined.
	 */
	public String getValueTypeName()
	{
		return valueTypeName;
	}

	/**
	 * Returns true if the value type is KeyMap.
	 * 
	 * @param recursive
	 *            If true, it recursively traverses the path until it finds the
	 *            child path that has the value type of KeyMap.
	 */
	public boolean isValueKeyMap(boolean recursive)
	{
		boolean flag = valueTypeName != null && (valueTypeName.equals(JsonLite.class.getName()))
				|| temporalType != null && temporalType.getDataClassName() != null
						&& (temporalType.getDataClassName().equals(JsonLite.class.getName()));

		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				flag = pathInfo.isValueKeyMap(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * Returns true if this path is the root path, i.e., "/".
	 */
	public boolean isRoot()
	{
		return fullPath.equals("/");
	}
	
	/**
	 * Returns true if this path is the grid root path, i.e., "/mygrid".
	 */
	public boolean isGridRoot()
	{
		return isRoot() == false && getGridRelativePath().length() == 0;
	}

	public boolean isTemporal(boolean recursive)
	{
		boolean flag = temporalType != null;

		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				flag = pathInfo.isTemporal(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	public boolean isHidden(boolean recursive)
	{
		if (name == null) {
			return false;
		}
		boolean flag = name.startsWith("__");

		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				flag = pathInfo.isHidden(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	// public boolean isReal()
	// {
	// return isReal;
	// }

	/**
	 * Returns true if the specified child path exists. This call can be
	 * expensive as it recursively compares path names.
	 * 
	 * @param childPath
	 *            Child path relative to this path.
	 */
	public boolean hasChildPath(String childPath)
	{
		if (childPath == null) {
			return false;
		}
		int index = childPath.indexOf('/');
		String childName;
		if (index == -1) {
			childName = childPath;
			childPath = null;
		} else {
			childName = childPath.substring(0, index);
			index++;
			if (index < childPath.length()) {
				childPath = childPath.substring(index);
			} else {
				childPath = null;
			}
		}

		// Iterate the child list and recursively compare the child names
		for (PathInfo pathInfo : childList) {
			if (pathInfo.getName().equals(childName)) {
				if (childPath == null) {
					return true;
				} else {
					return pathInfo.hasChildPath(childPath);
				}
			}
		}
		return false;
	}

	/**
	 * Returns the full path.
	 */
	public String toString()
	{
		return fullPath;
	}

	/**
	 * Compares the full paths.
	 */
	public int compareTo(PathInfo anotherPathInfo)
	{
		if (anotherPathInfo == null) {
			return 1;
		}
		if (fullPath == null) {
			return -1;
		}
		return fullPath.compareTo(anotherPathInfo.fullPath);
	}

	/**
	 * Returns true only if the specified object has the same full path as this
	 * object.
	 */
	public boolean equals(Object obj)
	{
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof PathInfo == false) {
			return false;
		}
		PathInfo other = (PathInfo) obj;
		if (fullPath.equals(other.fullPath)) {
			return true;
		}
		return false;
	}
}
