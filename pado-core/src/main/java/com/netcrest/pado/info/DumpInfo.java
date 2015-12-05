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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.netcrest.pado.biz.file.FileUtilUnix;
import com.netcrest.pado.util.GridUtil;

public abstract class DumpInfo implements Comparable<DumpInfo>
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
	 * Parent DumpInfo object. null if at top-level
	 */
	protected transient DumpInfo parent;

	/**
	 * List of child DumpInfo objects.
	 */
	protected List<DumpInfo> childList = new ArrayList<DumpInfo>(10);
	/**
	 * Content size of the path. Usually the entry count.
	 */
	protected int size;

	/**
	 * Archive file size in bytes
	 */
	protected long fileSize;

	/**
	 * Archive date
	 */
	protected Date date;

	/**
	 * Constructs a DumpInfo object.
	 * 
	 * @param parentDumpInfo
	 *            Parent object.
	 * @param file
	 *            File to examine.
	 * @param date
	 *            File date. If non-null, this date is assigned, otherwise the
	 *            file time stamp is assigned.
	 * @param recursive
	 *            true to include all sub-directories of the specified file,
	 *            false to include only the specified file.
	 */
	public DumpInfo(DumpInfo parentDumpInfo, File file, Date date, boolean recursive)
	{
		this.parent = parentDumpInfo;
		String parentFullPath = null;
		if (this.parent != null) {
			parentFullPath = this.parent.fullPath;
		}
		init(parentFullPath, file, date, recursive);
	}

	public DumpInfo(String parentFullPath, File file, Date date, boolean recursive)
	{
		init(parentFullPath, file, date, recursive);
	}

	private void init(String parentFullPath, File file, Date date, boolean recursive)
	{
		if (file == null) {
			return;
		}
		if (parentFullPath == null) {
			parentFullPath = "";
		}
		String fileName = file.getName();
		int index = fileName.indexOf('.');
		if (index >= 0) {
			this.name = fileName.substring(0, index);
		} else {
			this.name = fileName;
		}
		if (parentFullPath.equals("/")) {
			this.fullPath = parentFullPath + this.name;
		} else {
			this.fullPath = parentFullPath + "/" + this.name;
		}
		this.fileSize = file.length();
		if (date != null) {
			this.date = date;
		} else {
			this.date = new Date(file.lastModified());
		}
		
		if (file.isDirectory() == false) {
			try {
				// Subtract column header. Note that the file could be
				// empty.
				int lineCount = (int)FileUtilUnix.getLineCount(file);
				if (lineCount > 0) {
					this.size = lineCount - 1;
				}
			} catch (Exception e) {
				this.size = -1;
			}
		}
		if (recursive) {
			File files[] = file.listFiles();
			Arrays.sort(files);
			for (File childFile : files) {
//				if (childFile.isDirectory()) {
					createChild(childFile, this);
//				}
			}
		}
	}

	/**
	 * Creates a child DumpInfo object with the date assigned to the specified
	 * child file time stamp.
	 * 
	 * @param child
	 *            Child file.
	 * @param parent
	 *            Parent DumpInfo to specified child file.
	 * @return
	 */
	private DumpInfo createChild(File child, DumpInfo parent)
	{
		// Set date to null for child so that the file time stamp is assigned.
		DumpInfo childInfo = createDumpInfo(parent, child, null, false);
		parent.childList.add(childInfo);
		if (child.isDirectory()) {
			File files[] = child.listFiles();
			Arrays.sort(files);
			for (File childFile : files) {
				createChild(childFile, childInfo);
			}
		}
		return childInfo;
	}

	/**
	 * Returns a set of child grid paths. Always returns a non-null set.
	 * 
	 * @param recursive
	 *            true to include all nested child grid paths.
	 */
	public Set<String> getChildGridPathSet(boolean recursive)
	{
		return getChildGridPathSet(this, new TreeSet<String>(), recursive);
	}

	private Set<String> getChildGridPathSet(DumpInfo dumpInfo, Set<String> childGridPathSet, boolean recursive)
	{
		if (dumpInfo == null) {
			return childGridPathSet;
		}
		List<DumpInfo> childList = dumpInfo.getChildList();
		for (DumpInfo child : childList) {
			childGridPathSet.add(child.getGridPath());
			if (recursive) {
				getChildGridPathSet(child, childGridPathSet, recursive);
			}
		}
		return childGridPathSet;
	}

	/**
	 * Returns a set of child DumpInfo objects. Always returns a non-null set.
	 * 
	 * @param recursive
	 *            true to include all nested child DumpInfo objects.
	 */
	public Set<DumpInfo> getChildDumpInfoSet(boolean recursive)
	{
		return getChilDumpInfoSet(this, new TreeSet<DumpInfo>(), recursive);
	}

	private Set<DumpInfo> getChilDumpInfoSet(DumpInfo dumpInfo, Set<DumpInfo> childGridPathSet, boolean recursive)
	{
		if (dumpInfo == null) {
			return childGridPathSet;
		}
		List<DumpInfo> childList = dumpInfo.getChildList();
		for (DumpInfo child : childList) {
			childGridPathSet.add(child);
			if (recursive) {
				getChilDumpInfoSet(child, childGridPathSet, recursive);
			}
		}
		return childGridPathSet;
	}

	/**
	 * Returns the grid path name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the full path of the grid path name.
	 * 
	 * @return
	 */
	public String getFullPath()
	{
		return fullPath;
	}

	/**
	 * Returns the grid path.
	 */
	public String getGridPath()
	{
		return GridUtil.getChildPath(fullPath);
	}

	/**
	 * Returns the parent object. This may be null if the underlying
	 * serialization mechanism does not support circular references.
	 */
	public DumpInfo getParent()
	{
		return parent;
	}

	/**
	 * Returns the child list. It always returns a non-null list.
	 */
	public List<DumpInfo> getChildList()
	{
		return childList;
	}

	/**
	 * Returns the number of child DumpInfo objects.
	 */
	public int getChildCount()
	{
		if (childList == null) {
			return 0;
		}
		return childList.size();
	}

	/**
	 * Returns the size or entry count.
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * Returns the file size in bytes.
	 */
	public long getFileSize()
	{
		return fileSize;
	}

	/**
	 * Returns the last modified date.
	 */
	public Date getDate()
	{
		return date;
	}

	@Override
	public String toString()
	{
		return "DumpInfo [name=" + name + ", fullPath=" + fullPath + ", childList=" + childList + ", size=" + size
				+ ", fileSize=" + fileSize + ", date=" + date + "]";
	}

	@Override
	public int compareTo(DumpInfo o)
	{
		if (this.fullPath == o.fullPath) {
			return 0;
		} else if (this.fullPath == null) {
			if (o.fullPath != null) {
				return -1;
			}
		} 
		return this.fullPath.compareTo(o.fullPath);
	}

	// ------------------ Abstract methods
	protected abstract DumpInfo createDumpInfo(DumpInfo parentDumpInfo, File file, Date date, boolean recursive);
}
