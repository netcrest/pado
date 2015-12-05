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
package com.netcrest.pado.biz.gemfire.proxy.functions;

import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.netcrest.pado.biz.gemfire.IGemfireGridContextServer;

public class GemfireGridContextServerImpl implements IGemfireGridContextServer
{
	private FunctionContext functionContext;
	private Object[] additionalArgs;
	private transient Object[] transientData;
	
	public GemfireGridContextServerImpl(FunctionContext functionContext, Object[] additionalArgs, Object[] transientData) 
	{
		this.functionContext = functionContext;
		this.additionalArgs = additionalArgs;
		this.transientData = transientData;
	}

	@Override
	public FunctionContext getFunctionContext()
	{
		return functionContext;
	}
	
	@Override
	public RegionFunctionContext getRegionFunctionContext()
	{
		FunctionContext fc = getFunctionContext();
		if (fc != null && fc instanceof RegionFunctionContext) {
			return (RegionFunctionContext)fc;
		} else {
			return null;
		}
	}

	@Override
	public Object[] getAdditionalArguments()
	{
		return additionalArgs;
	}
	
	@Override
	public Object[] getTransientData()
	{
		return transientData;
	}
}
