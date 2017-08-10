/*
 * Copyright (c) 2013-2017 Netcrest Technologies, LLC. All rights reserved.
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

import com.netcrest.pado.IBizInfo;
import com.netcrest.pado.biz.info.BizInfoFactory;
import com.netcrest.pado.biz.info.MethodInfo;
import com.netcrest.pado.data.jsonlite.JsonLite;

/**
 * BizInfo contains IBiz class information. IBiz class declaration is captured
 * in BizInfo. Each IBiz extended class can be declared in the Pado
 * configuration (pado.xml) file or dynamically declared or edited via the Pado
 * management tools.
 * 
 * @author dpark
 * 
 */
public abstract class BizInfo implements IBizInfo
{
	protected IBizInfo bizInfo;

	/**
	 * Constructs a new BizInfo object.
	 */
	public BizInfo()
	{
	}

	/**
	 * Constructs a new BizInfo object containing the specified IBiz interface
	 * class name.
	 * 
	 * @param bizIntefaceName
	 */
	public BizInfo(String bizIntefaceName)
	{
		// bizInfo must be serializable
		this.bizInfo = BizInfoFactory.createBizInfo(bizIntefaceName);
	}

	/**
	 * Returns the IBiz interface class name.
	 */
	@Override
	public String getBizInterfaceName()
	{
		if (bizInfo == null) {
			return null;
		}
		return bizInfo.getBizInterfaceName();
	}

	/**
	 * Returns the description of the IBiz class.
	 */
	@Override
	public String getDescription()
	{
		if (bizInfo == null) {
			return null;
		}
		return bizInfo.getDescription();
	}

	@Override
	public MethodInfo[] getMethodInfo()
	{
		if (bizInfo == null) {
			return null;
		}
		return bizInfo.getMethodInfo();
	}

	@Override
	public JsonLite<?> toJson()
	{
		if (bizInfo == null) {
			return null;
		}
		return bizInfo.toJson();
	}

	@Override
	public IBizInfo toBizInfo()
	{
		if (bizInfo == null) {
			return null;
		}
		return bizInfo.toBizInfo();
	}

	@Override
	public String[] getAppIds()
	{
		if (bizInfo == null) {
			return null;
		}
		return bizInfo.getAppIds();
	}

	/**
	 * Returns true if the specified app ID is allowed to access this Biz class.
	 * It returns false if the specified app ID is null. It returns true if the
	 * specified app ID is "sys" or IBizInfo is not defined.
	 * 
	 * @param appId
	 *            App ID
	 */
	public boolean isAppIdAllowed(String appId)
	{
		if (appId == null) {
			return false;
		}
		if (appId.equals("sys")) {
			return true;
		}
		if (bizInfo == null || bizInfo.getAppIds() == null) {
			return true;
		}
		String[] appIds = bizInfo.getAppIds();
		for (String aid : appIds) {
			if (aid.equals(appId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the hash code of the IBiz interface class name.
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getBizInterfaceName() == null) ? 0 : getBizInterfaceName().hashCode());
		return result;
	}

	/**
	 * Returns true if the IBiz interface class names match.
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
		BizInfo other = (BizInfo) obj;
		if (getBizInterfaceName() == null) {
			if (other.getBizInterfaceName() != null)
				return false;
		} else if (!getBizInterfaceName().equals(other.getBizInterfaceName()))
			return false;
		return true;
	}

}
