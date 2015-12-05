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

import java.util.List;

public abstract class CacheDumpInfo extends CacheHeaderInfo
{
	protected List<DumpInfo> dumpInfoList;

	public CacheDumpInfo()
	{
	}
	
	public CacheDumpInfo(String gridId, List<DumpInfo> dumpInfoList)
	{
		super(gridId);
		this.dumpInfoList = dumpInfoList;
	}
	
	public List<DumpInfo> getDumpInfoList()
	{
		return dumpInfoList;
	}

	@Override
	public String toString()
	{
		return "CacheDumpInfo [dumpInfoList=" + dumpInfoList + ", gridId=" + gridId + ", name=" + name + ", id=" + id
				+ ", host=" + host + ", processId=" + processId + "]";
	}
}
