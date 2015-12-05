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
package com.netcrest.pado.biz.impl.gemfire;

import java.util.List;

import javax.annotation.Resource;

import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.IPqlBiz;
import com.netcrest.pado.biz.gemfire.proxy.functions.GemfireGridContextServerImpl;
import com.netcrest.pado.index.provider.gemfire.OqlSearch;
import com.netcrest.pado.index.provider.lucene.LuceneSearch;
import com.netcrest.pado.util.GridUtil;

public class PqlBizImpl implements IPqlBiz
{
	@Resource IBizContextServer bizContext;

	@Override
	public IBizContextClient getBizContext()
	{
		// not used
		return null;
	}

	@Override
	@BizMethod
	public List<?> executePql(String pql)
	{
		return null;
	}

	@Override
	@BizMethod
	public List<?> executeGemfireOql(String oql)
	{
		OqlSearch os = OqlSearch.getOqlSearch();
		return os.executeOql(((GemfireGridContextServerImpl)bizContext.getGridContextServer()).getRegionFunctionContext(), oql);
	}

	@Override
	@BizMethod
	public List<?> executeLuceneQuery(String gridPath, String luceneQuery)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		LuceneSearch ls = LuceneSearch.getLuceneSearch(fullPath);
//		return ls.search(luceneQuery);
		return null;
	}
	
	
}
