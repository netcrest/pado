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
package com.netcrest.pado.temporal.test.biz.impl.gemfire;

import java.util.List;
import java.util.concurrent.Future;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.mon.ISysBiz;
import com.netcrest.pado.info.ServerInfo;
import com.netcrest.pado.temporal.test.biz.ITemporalLoaderBizFuture;

public class TemporalLoaderBizFutureImplLocal implements ITemporalLoaderBizFuture, IBizLocal
{
	ITemporalLoaderBizFuture temporalLoaderBiz;
	ISysBiz sysBiz;
	IPado pado;
	
	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		temporalLoaderBiz = (ITemporalLoaderBizFuture)biz;
		this.pado = pado;
		sysBiz = pado.getCatalog().newInstance(ISysBiz.class);
	}
	
	@Override
	public IBizContextClient getBizContext()
	{
		return temporalLoaderBiz.getBizContext();
	}

	@Override
	public Future<List<String>> loadTradePerServer(String path, int perServerCount, int batchSize)
	{
		return (Future<List<String>>)temporalLoaderBiz.loadTradePerServer(path, perServerCount, batchSize);
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	@Override
	public Future<List<String>> loadTrades(String path, int totalCount, int batchSize)
	{
		String gridIds[] = temporalLoaderBiz.getBizContext().getGridContextClient().getGridIds();
		if (gridIds == null || gridIds.length < 0) {
			gridIds = pado.getCatalog().getGridIds();
		}
		if (gridIds == null || gridIds.length < 0) {
			return null;
		}
		List<ServerInfo> list = sysBiz.getServerInfoList(temporalLoaderBiz.getBizContext().getGridService().getFullPath(gridIds[0], path));
		int serverCount = list.size();
		IGridMapBiz gridMapBiz = pado.getCatalog().newInstance(IGridMapBiz.class, path);
		int size = gridMapBiz.size(true);
		int perServerCount = totalCount / serverCount;
		return loadTradePerServer(path, perServerCount, batchSize);
	}

	

}
