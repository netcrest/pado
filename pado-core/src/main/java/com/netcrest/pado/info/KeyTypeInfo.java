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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * KeyTypeInfo provides KeyMap KeyType registration information maintained by
 * the grid.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
public class KeyTypeInfo implements Comparable, Externalizable
{
	/**
	 * Fully-qualified KeyType class name
	 */
	protected String keyTypeClassName;
	
	/**
	 * KeyType version
	 */
	protected int version;
	
	/**
	 * KeyType merge point version
	 */
	protected int mergePoint;
	
	/**
	 * The number of instances of this version of KeyType
	 */
	protected int instanceCount;

	/**
	 * Constructs an empty KeyTypeInfo object.
	 */
	public KeyTypeInfo()
	{
	}

	/**
	 * Returns the KeyType class name.
	 */
	public String getKeyTypeClassName()
	{
		return keyTypeClassName;
	}

	/**
	 * Sets the KeyType class name.
	 * 
	 * @param keyTypeClassName
	 */
	public void setKeyTypeClassName(String keyTypeClassName)
	{
		this.keyTypeClassName = keyTypeClassName;
	}

	/**
	 * Returns the KeyType version.
	 */
	public int getVersion()
	{
		return version;
	}

	/**
	 * Sets the KeyType version.
	 * 
	 * @param version
	 */
	public void setVersion(int version)
	{
		this.version = version;
	}

	/**
	 * Returns the merge point from which this version is based.
	 */
	public int getMergePoint()
	{
		return mergePoint;
	}

	/**
	 * Sets the merge point from which this version is based.
	 * 
	 * @param mergePoint Merge point version.
	 */
	public void setMergePoint(int mergePoint)
	{
		this.mergePoint = mergePoint;
	}

	/**
	 * Returns the number of instances of this particular version in the grid.
	 */
	public int getInstanceCount()
	{
		return instanceCount;
	}

	/**
	 * Sets the number of instances of this particular version in the grid.
	 * 
	 * @param instanceCount
	 *            Number of KeyMap instances with this particular version.
	 */
	public void setInstanceCount(int instanceCount)
	{
		this.instanceCount = instanceCount;
	}

	/**
	 * Returns true if the specified KeyTypeInfo has the same main class type,
	 * i.e., class type without the version tag.
	 * 
	 * @param anotherKeyTypeInfo
	 *            Another KeyTypeInfo to compare
	 * @return Returns false if anotherKeyTypeInfo is null or
	 *         {@link #getKeyTypeClassName()} is null.
	 */
	public boolean isSameMainClass(KeyTypeInfo anotherKeyTypeInfo)
	{
		if (keyTypeClassName == null) {
			return false;
		}
		return getMainClassName().equals(anotherKeyTypeInfo.getMainClassName());
	}

	/**
	 * Returns the main class name without the version extension. It returns
	 * null if {@link #getKeyTypeClassName()} is null.
	 */
	public String getMainClassName()
	{
		if (keyTypeClassName == null) {
			return null;
		}
		return keyTypeClassName.replaceAll("_v[0-9]*$", "");
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyTypeClassName == null) ? 0 : keyTypeClassName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyTypeInfo other = (KeyTypeInfo) obj;
		if (keyTypeClassName == null) {
			if (other.keyTypeClassName != null)
				return false;
		} else if (!keyTypeClassName.equals(other.keyTypeClassName))
			return false;
		return true;
	}

	@Override
	public int compareTo(Object o)
	{
		if (o == null) {
			return 1;
		} else if (((KeyTypeInfo) o).keyTypeClassName == null) {
			return 1;
		} else if (keyTypeClassName == null) {
			return -1;
		} else {
			return keyTypeClassName.compareTo(((KeyTypeInfo) o).keyTypeClassName);
		}
	}

	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException
	{
		keyTypeClassName = input.readUTF();
		version = input.readInt();
		mergePoint = input.readInt();
		instanceCount = input.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException
	{
		output.writeUTF(keyTypeClassName);
		output.writeInt(version);
		output.writeInt(mergePoint);
		output.writeInt(instanceCount);
	}
}
