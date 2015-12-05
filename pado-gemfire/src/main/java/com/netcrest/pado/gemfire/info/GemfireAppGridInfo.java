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
package com.netcrest.pado.gemfire.info;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.netcrest.pado.info.AppGridInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.PathInfo;

/**
 * AppGridInfo is a light weight grid metadata class that contains grid specific
 * information. To obtain comprehensive grid details, use {@link GridInfo}. Note
 * that this AppGridInfo objects are created in the server using GridInfo by
 * invoking {@link #update(GridInfo)}.
 * 
 * @author dpark
 * 
 */
public class GemfireAppGridInfo extends AppGridInfo implements DataSerializable
{
	private static final long serialVersionUID = 1L;

	private boolean clientConnectionSingleHopEnabled;
	private boolean clientConnectionMultiuserAuthenticationEnabled;

	public GemfireAppGridInfo()
	{
	}

	public GemfireAppGridInfo(GridInfo gridInfo)
	{
		super(gridInfo);
	}

	/**
	 * Updates this object with contents of the specified GridInfo object
	 * 
	 * @param gridInfo
	 * @return Returns true if the passed in GridInfo object is different than
	 *         this object and this object has been updated.
	 */
	public synchronized boolean update(GridInfo gridInfo)
	{
		boolean different = false;
		if (gridInfo != null) {
			different = equals(gridInfo) == false;
			if (different) {
				setGridId(gridInfo.getGridId());
				setLocation(gridInfo.getLocation());
				setChildGridIds(gridInfo.getChildGridIds());
				setGridRootPath(gridInfo.getGridRootPath());
				setParentGridIds(gridInfo.getParentGridIds());
				setConnectionName(gridInfo.getConnectionName());
				setSharedConnectionName(gridInfo.getSharedConnectionName());
				setIndexMatrixConnectionName(gridInfo.getIndexMatrixConnectionName());
				setLocators(gridInfo.getLocators());
				setClientIndexMatrixConnectionName(gridInfo.getClientIndexMatrixConnectionName());
				setClientConnectionName(gridInfo.getClientConnectionName());
				setClientSharedConnectionName(gridInfo.getClientSharedConnectionName());
				setClientConnectionSingleHopEnabled(((GemfireGridInfo) gridInfo).isClientConnectionSingleHopEnabled());
				setClientConnectionMultiuserAuthenticationEnabled(((GemfireGridInfo) gridInfo)
						.isClientConnectionMultiuserAuthenticationEnabled());
				setClientLocators(gridInfo.getClientLocators());
				rootPathInfo = gridInfo.getCacheInfo().getPathInfo(getGridRootPath());
			}
		}
		return different;
	}

	public boolean isClientConnectionSingleHopEnabled()
	{
		return clientConnectionSingleHopEnabled;
	}

	public void setClientConnectionSingleHopEnabled(boolean clientConnectionSingleHopEnabled)
	{
		this.clientConnectionSingleHopEnabled = clientConnectionSingleHopEnabled;
	}

	public boolean isClientConnectionMultiuserAuthenticationEnabled()
	{
		return clientConnectionMultiuserAuthenticationEnabled;
	}

	public void setClientConnectionMultiuserAuthenticationEnabled(boolean clientConnectionMultiuserAuthenticationEnabled)
	{
		this.clientConnectionMultiuserAuthenticationEnabled = clientConnectionMultiuserAuthenticationEnabled;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(childGridIds);
		result = prime * result
				+ ((clientIndexMatrixConnectionName == null) ? 0 : clientIndexMatrixConnectionName.hashCode());
		result = prime * result + ((clientLocators == null) ? 0 : clientLocators.hashCode());
		result = prime * result + (clientConnectionMultiuserAuthenticationEnabled ? 1231 : 1237);
		result = prime * result + ((clientConnectionName == null) ? 0 : clientConnectionName.hashCode());
		result = prime * result + (clientConnectionSingleHopEnabled ? 1231 : 1237);
		result = prime * result + ((clientSharedConnectionName == null) ? 0 : clientSharedConnectionName.hashCode());
		result = prime * result + ((gridId == null) ? 0 : gridId.hashCode());
		result = prime * result + ((gridRootPath == null) ? 0 : gridRootPath.hashCode());
		result = prime * result + ((indexMatrixConnectionName == null) ? 0 : indexMatrixConnectionName.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((locators == null) ? 0 : locators.hashCode());
		result = prime * result + Arrays.hashCode(parentGridIds);
		result = prime * result + ((connectionName == null) ? 0 : connectionName.hashCode());
		result = prime * result + ((rootPathInfo == null) ? 0 : rootPathInfo.hashCode());
		result = prime * result + ((sharedConnectionName == null) ? 0 : sharedConnectionName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof GemfireGridInfo) {
			GemfireGridInfo gridInfo = (GemfireGridInfo) obj;
			if (clientConnectionMultiuserAuthenticationEnabled != gridInfo
					.isClientConnectionMultiuserAuthenticationEnabled())
				return false;
			if (clientConnectionSingleHopEnabled != gridInfo.isClientConnectionSingleHopEnabled())
				return false;
			if (gridRootPath == null) {
				if (gridInfo.getGridRootPath() != null)
					return false;
			} else if (!gridRootPath.equals(gridInfo.getGridRootPath()))
				return false;
			if (rootPathInfo == null) {
				if (gridInfo.getRootPathInfo() != null)
					return false;
			} else {
				// Iterate rootPathInfo to determine child regions
				List<PathInfo> list = rootPathInfo.getChildList();
				if (isPathInfoListsEqual(list, gridInfo.getRootPathInfo().getChildList()) == false) {
					return false;
				}
			}
			return true;
		}

		if (obj instanceof GemfireAppGridInfo == false) // @dpark
			return false;
		if (super.equals(obj) == false) {
			return false;
		}
		GemfireAppGridInfo other = (GemfireAppGridInfo) obj;

		if (clientConnectionMultiuserAuthenticationEnabled != other.clientConnectionMultiuserAuthenticationEnabled)
			return false;
		if (clientConnectionSingleHopEnabled != other.clientConnectionSingleHopEnabled)
			return false;
		if (gridRootPath == null) {
			if (other.gridRootPath != null)
				return false;
		} else if (!gridRootPath.equals(other.gridRootPath))
			return false;
		if (rootPathInfo == null) {
			if (other.rootPathInfo != null)
				return false;
		}
		// else if (!rootPathInfo.equals(other.rootPathInfo))
		// return false;

		return true;
	}

	private boolean isPathInfoListsEqual(List<PathInfo> list1, List<PathInfo> list2)
	{
		ListIterator<PathInfo> e1 = list1.listIterator();
		ListIterator<PathInfo> e2 = list2.listIterator();
		while (e1.hasNext() && e2.hasNext()) {
			PathInfo o1 = e1.next();
			PathInfo o2 = e2.next();
			if (!(o1 == null ? o2 == null : o1.equals(o2)))
				return false;
			if (!isPathInfoListsEqual(o1.getChildList(), o2.getChildList())) {
				return false;
			}
		}
		return !(e1.hasNext() || e2.hasNext());
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		gridId = DataSerializer.readString(input);
		location = DataSerializer.readString(input);
		gridRootPath = DataSerializer.readString(input);
		connectionName = DataSerializer.readString(input);
		sharedConnectionName = DataSerializer.readString(input);
		indexMatrixConnectionName = DataSerializer.readString(input);
		locators = DataSerializer.readString(input);
		parentGridIds = DataSerializer.readStringArray(input);
		childGridIds = DataSerializer.readStringArray(input);
		clientIndexMatrixConnectionName = DataSerializer.readString(input);
		clientConnectionName = DataSerializer.readString(input);
		clientSharedConnectionName = DataSerializer.readString(input);
		clientLocators = DataSerializer.readString(input);
		clientConnectionSingleHopEnabled = DataSerializer.readPrimitiveBoolean(input);
		clientConnectionMultiuserAuthenticationEnabled = DataSerializer.readPrimitiveBoolean(input);
		rootPathInfo = DataSerializer.readObject(input);
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void toData(DataOutput output) throws IOException
	{
		DataSerializer.writeString(gridId, output);
		DataSerializer.writeString(location, output);
		DataSerializer.writeString(gridRootPath, output);
		DataSerializer.writeString(connectionName, output);
		DataSerializer.writeString(sharedConnectionName, output);
		DataSerializer.writeString(indexMatrixConnectionName, output);
		DataSerializer.writeString(locators, output);
		DataSerializer.writeStringArray(parentGridIds, output);
		DataSerializer.writeStringArray(childGridIds, output);
		DataSerializer.writeString(clientIndexMatrixConnectionName, output);
		DataSerializer.writeString(clientConnectionName, output);
		DataSerializer.writeString(clientSharedConnectionName, output);
		DataSerializer.writeString(clientLocators, output);
		DataSerializer.writePrimitiveBoolean(clientConnectionSingleHopEnabled, output);
		DataSerializer.writePrimitiveBoolean(clientConnectionMultiuserAuthenticationEnabled, output);
		DataSerializer.writeObject(rootPathInfo, output);
	}

}