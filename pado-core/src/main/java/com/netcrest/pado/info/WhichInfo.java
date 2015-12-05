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
 * WhichInfo contains the location info of the key in the Pado cluster.
 * 
 * @author dpark
 * 
 */
public class WhichInfo
{
	protected String gridId;
	protected String serverName;
	protected String serverId;
	protected String host;

	protected Object key;
	protected Object actualKey;
	protected Object value;
	protected boolean isPartitioned;
	protected BucketInfo bucketInfo;

	public String getGridId()
	{
		return gridId;
	}

	public void setGridId(String gridId)
	{
		this.gridId = gridId;
	}

	public String getServerName()
	{
		return serverName;
	}

	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}

	public String getServerId()
	{
		return serverId;
	}

	public void setServerId(String serverId)
	{
		this.serverId = serverId;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public Object getKey()
	{
		return key;
	}

	public void setKey(Object key)
	{
		this.key = key;
	}

	public Object getActualKey()
	{
		return actualKey;
	}

	public void setActualKey(Object actualKey)
	{
		this.actualKey = actualKey;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public boolean isPartitioned()
	{
		return isPartitioned;
	}

	public void setPartitioned(boolean isPartitioned)
	{
		this.isPartitioned = isPartitioned;
	}

	public BucketInfo getBucketInfo()
	{
		return bucketInfo;
	}

	public void setBucketInfo(BucketInfo bucketInfo)
	{
		this.bucketInfo = bucketInfo;
	}
	
	public boolean isPrimary()
	{
		return isPartitioned && bucketInfo != null && bucketInfo.isPrimary();
	}

	@Override
	public String toString()
	{
		return "WhichInfo [gridId=" + gridId + ", serverName=" + serverName + ", serverId=" + serverId + ", host="
				+ host + ", key=" + key + ", actualKey=" + actualKey + ", value=" + value + ", isPartitioned="
				+ isPartitioned + ", isPrimary=" + isPrimary() + ", bucketInfo=" + bucketInfo + "]";
	}
}
