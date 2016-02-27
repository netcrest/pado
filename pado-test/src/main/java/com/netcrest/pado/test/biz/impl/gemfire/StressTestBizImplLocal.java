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
package com.netcrest.pado.test.biz.impl.gemfire;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.test.biz.IStressTestBiz;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class StressTestBizImplLocal implements IStressTestBiz, IBizLocal
{
	@Resource
	private IStressTestBiz biz;
	private IPado pado;

	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		this.pado = pado;
	}

	@Override
	public List<String> __start(JsonLite request)
	{
		request.put("Token", UUID.randomUUID().toString());
		return biz.__start(request);
	}

	@Override
	public List<String> start(JsonLite request)
	{
		List<String> list = __start(request);
		if (list != null) {
			Collections.sort(list);
		}
		return list;
	}

	@Override
	public boolean isComplete()
	{
		return biz.isComplete();
	}

	@Override
	public List<String> getStatus()
	{
		return biz.getStatus();
	}
}
