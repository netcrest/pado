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
package com.netcrest.pado.temporal.test.biz;

import java.util.List;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnServer;

@BizClass(name = "ITemporalLoaderBiz", localImpl="com.netcrest.pado.temporal.test.biz.impl.gemfire.TemporalLoaderBizImplLocal")
public interface ITemporalLoaderBiz extends IBiz
{
	@BizMethod
	@OnServer(broadcast=true)
	public List<String> loadTradePerServer(String path, int perServerCount, int batchSize);
	
	public List<String> loadTrades(String path, int totalCount, int batchSize);
}
