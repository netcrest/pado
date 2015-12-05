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

/**
 * BucketInfo contains partitioned region bucket information. Note that the
 * server ID is not set by the server. It is caller's responsibility to set the
 * server ID obtained from {@link CacheInfo#getId()} or
 * {@link ServerInfo#getId()}.
 * 
 * @author dpark
 * 
 */
public abstract class BucketInfo implements Comparable<BucketInfo>
{
	private static final long serialVersionUID = 1L;

	private transient String serverName;
	private transient Object serverId;

	/**
	 * Bucket ID
	 */
	protected int bucketId;

	/**
	 * Primary bucket?
	 */
	protected boolean isPrimary;

	/**
	 * Bucket size
	 */
	protected int size;

	/**
	 * Total bucket size in bytes
	 */
	protected long totalBytes;

	/**
	 * Constructs a BucketInfo object.
	 */
	public BucketInfo()
	{
	}

	/**
	 * Constructs a BucketInfo object containing the specified bucket ID, size,
	 * and total number of bytes in the bucket.
	 * 
	 * @param bucketId
	 *            Bucket ID
	 * @param size
	 *            Bucket size or number of entries in the bucket
	 * @param totalBytes
	 *            Total number of bytes in the bucket
	 */
	public BucketInfo(int bucketId, boolean isPrimary, int size, long totalBytes)
	{
		this.bucketId = bucketId;
		this.isPrimary = isPrimary;
		this.size = size;
		this.totalBytes = totalBytes;
	}

	/**
	 * Returns the server name.
	 */
	public String getServerName()
	{
		return serverName;
	}

	/**
	 * Sets the server name.
	 * 
	 * @param serverName
	 *            Server name
	 */
	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}

	/**
	 * Returns the server ID
	 */
	public Object getServerId()
	{
		return serverId;
	}

	/**
	 * Sets the server ID
	 * 
	 * @param serverId
	 *            Server ID
	 */
	public void setServerId(Object serverId)
	{
		this.serverId = serverId;
	}

	/**
	 * Returns the bucket ID
	 */
	public int getBucketId()
	{
		return bucketId;
	}

	/**
	 * Sets the bucket ID
	 * 
	 * @param bucketId
	 *            Bucket ID
	 */
	public void setBucketId(int bucketId)
	{
		this.bucketId = bucketId;
	}

	/**
	 * Returns true if this is a primary bucket.
	 */
	public boolean isPrimary()
	{
		return isPrimary;
	}

	/**
	 * Sets this bucket primary or redundant.
	 * 
	 * @param isPrimary
	 *            true if primary, false if redundant
	 */
	public void setPrimary(boolean isPrimary)
	{
		this.isPrimary = isPrimary;
	}

	/**
	 * Returns the number of entries in the bucket
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * Sets the number of entries in the bucket
	 * 
	 * @param size
	 *            number of entries
	 */
	public void setSize(int size)
	{
		this.size = size;
	}

	/**
	 * Returns the total number of bytes in the bucket
	 */
	public long getTotalBytes()
	{
		return totalBytes;
	}

	/**
	 * Sets the total number of bytes in the bucket
	 * 
	 * @param totalBytes
	 *            Total number of bytes
	 */
	public void setTotalBytes(long totalBytes)
	{
		this.totalBytes = totalBytes;
	}

	/**
	 * Compare the bucket ID.
	 */
	@Override
	public int compareTo(BucketInfo anotherBucketInfo)
	{
		if (anotherBucketInfo == null) {
			return 1;
		}
		return (bucketId < anotherBucketInfo.bucketId ? -1 : (bucketId == anotherBucketInfo.bucketId ? 0 : 1));
	}

	/**
	 * Returns the bucket ID hash code
	 */
	@Override
	public int hashCode()
	{
		return this.bucketId;
	}

	/**
	 * Returns true if the bucket IDs match.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BucketInfo other = (BucketInfo) obj;
		return bucketId == other.bucketId;
	}

	@Override
	public String toString()
	{
		return "BucketInfo [bucketId=" + bucketId + ", size=" + size + ", totalBytes=" + totalBytes + "]";
	}

}
