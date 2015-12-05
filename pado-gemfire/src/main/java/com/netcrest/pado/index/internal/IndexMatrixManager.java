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
package com.netcrest.pado.index.internal;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;

public class IndexMatrixManager
{
	private final static IndexMatrixManager manager = new IndexMatrixManager();
	
	public static IndexMatrixManager getIndexMatrixManager()
	{
		return manager;
	}
	
	private IndexMatrixManager()
	{
	}
	
	/**
	 * Returns the index region. Always freshly get it from the cache to ensure
	 * returning the valid region. This is to allow Pado to re-initialize
	 * cache. 
	 */
	private Region<Object, IndexMatrix> getRegion()
	{
		return CacheFactory.getAnyInstance().getRegion(IndexMatrixUtil.getProperty(Constants.PROP_REGION_INDEX));
	}
	
	public Region<Object, IndexMatrix> getIndexMatrixRegion()
	{
		return getRegion();
	}
	
	public IndexMatrix getIndexMatrix(Object id)
	{
		return getRegion().get(id);
	}
	
	public void putIndexMatrix(Object id, IndexMatrix indexMatrix)
	{
		getRegion().put(id, indexMatrix);
	}
	
	public void removeIndexMatrix(Object id)
	{
		getRegion().remove(id);
	}
	
	public void clear()
	{
		getRegion().clear();
	}
}

