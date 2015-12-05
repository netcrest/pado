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
package com.netcrest.pado.test.junit;

import java.io.Serializable;

public class CacheKey implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String identityKey;
	private String routingKey;
	
	public CacheKey() {}
	
	public CacheKey(String identityKey, String routingKey)
	{
		this.identityKey = identityKey;
		this.routingKey = routingKey;
	}
	
	public String getIdentityKey()
	{
		return identityKey;
	}
	
	public String getRoutingKey()
	{
		return routingKey;
	}

	@Override
	public int hashCode()
	{
		if (identityKey == null) {
			return 0;
		} else {
			return identityKey.hashCode();
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass()) {
			if (obj instanceof String) {
				String otherIdentityKey = (String)obj;
				if (identityKey == null) {
					if (otherIdentityKey != null)
						return false;
				} else if (!identityKey.equals(otherIdentityKey)) {
					return false;
				}
				return true;
			}
			return false;
		}
		CacheKey other = (CacheKey) obj;
		if (identityKey == null) {
			if (other.identityKey != null)
				return false;
		} else if (!identityKey.equals(other.identityKey))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "CacheKey [identityKey=" + identityKey + ", routingKey=" + routingKey + "]";
	}
}
