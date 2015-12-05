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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.biz.util.BizThreadPool;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.ITemporalBizLink;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalServerBizImpl
{
	@Resource
	IBizContextServer bizContext;

	private BizThreadPool<ITemporalBizLink> temporalBizThreadPool = new BizThreadPool(PadoServerManager.getPadoServerManager().getCatalog(), ITemporalBiz.class, "account");

	
	@BizMethod
	public List executeQueryFromServer(String pql)
	{
		ITemporalBizLink temporalBiz = temporalBizThreadPool.getBiz();
		IScrollableResultSet<JsonLite> sr = temporalBiz.getValueResultSet(pql, null, true, 100, true);
		List resultList = new ArrayList();
		do {
			resultList.addAll(sr.toList());
		} while (sr.nextSet());
		
		return resultList;
	}
	
	@BizMethod
	public List executeOQLFromServer(String oql)
	{
		QueryService qs = CacheFactory.getAnyInstance().getQueryService();
		Query query = qs.newQuery(oql);
		List resultList;
		try {
			SelectResults sr = (SelectResults)query.execute();
			resultList = sr.asList();
		} catch (Exception e) {
			throw new PadoException(e);
		}
		return resultList;
	}
}
