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

import com.netcrest.pado.IBizInfo;

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
	/**
	 * IBiz interface class anem
	 */
	protected String bizIntefaceName;
	
	/**
	 * IBix description
	 */
	protected String description;

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
		this.bizIntefaceName = bizIntefaceName;
	}

	/**
	 * Returns the IBiz interface class name.
	 */
	public String getBizInterfaceName()
	{
		return bizIntefaceName;
	}

	/**
	 * Sets the description of the IBiz class.
	 * @param description IBiz class description
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Returns the description of the IBiz class.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Returns the hash code of the IBiz interface class name.
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bizIntefaceName == null) ? 0 : bizIntefaceName.hashCode());
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
		if (bizIntefaceName == null) {
			if (other.bizIntefaceName != null)
				return false;
		} else if (!bizIntefaceName.equals(other.bizIntefaceName))
			return false;
		return true;
	}

}
