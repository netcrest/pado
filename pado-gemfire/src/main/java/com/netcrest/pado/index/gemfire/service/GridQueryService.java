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
package com.netcrest.pado.index.gemfire.service;

import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.client.Pool;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.service.IGridQueryService;
import com.netcrest.pado.index.service.impl.GridQueryServiceImpl;

public final class GridQueryService
{
	private static volatile IGridQueryService querySvc = null;

	public static IGridQueryService getGridQueryService()
	{
		if (querySvc == null) {
			synchronized (GridQueryService.class) {
				if (querySvc == null)
					querySvc = new GridQueryServiceImpl();
			}
		}
		return querySvc;
	}

	/**
	 * Initializes IGridQueryService specific to the specified region service.
	 * It creates and returns a new instance of IGridQuerySerice which uses the
	 * passed-in region service. All subsequent calls to
	 * {@link #getGridQueryService()} return the same IGridQuerySerice instance
	 * that this method returned. As such, this method call should be restricted
	 * to initialization only.
	 * <p>
	 * If this method is never invoked then {@link #getGridQueryService()}
	 * returns an instance that does not depend on RegionService.
	 * 
	 * @param pool
	 *            Pool to use for client to server communications. regionService
	 *            takes precedence. If null, then the default pool,
	 *            {@link Constants#INDEX_POOL} is assigned.
	 * @param regionService
	 *            RegonSerivce to use for client to server communications. If
	 *            null then the pool is used instead.
	 * @return Returns a grid query service instance.
	 */
	public static IGridQueryService initialize(Pool pool, RegionService regionService)
	{
		synchronized (GridQueryService.class) {
			querySvc = new GridQueryServiceImpl(pool, regionService);
		}
		return querySvc;
	}

}
