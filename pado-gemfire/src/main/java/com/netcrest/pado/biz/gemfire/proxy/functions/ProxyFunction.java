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

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.netcrest.pado.annotation.BizClass.BizStateType;
import com.netcrest.pado.biz.gemfire.proxy.GemfireBizUtil;
import com.netcrest.pado.exception.PadoServerException;

/**
 * Abstract class for proxy Gemfire functions that are installed for each
 * Gemfire annotated method in the service interface class.
 */
public abstract class ProxyFunction implements Function
{
	protected Class<?> targetClass;
	private Class<?> futureClass;
	private final String functionId;
	protected final BizStateType bizStateType;

	private final boolean hasResult;
	private final boolean optimizeForWrite = true;

	/**
	 * @param functionId
	 *            The logical name for this service
	 * @param m
	 *            Used to work out details for GemFire callbacks
	 */
	ProxyFunction(final Class<?> ibizClass) throws PadoServerException
	{
		if (GemfireBizUtil.isFuture(ibizClass)) {
			try {
				this.targetClass = (Class<?>) Class.forName(GemfireBizUtil.getBizInterfaceName(ibizClass));
			} catch (ClassNotFoundException ex) {
				throw new PadoServerException(ex);
			}
			this.futureClass = ibizClass;
		} else {
			this.targetClass = ibizClass;
		}
		this.functionId = GemfireBizUtil.getGemfireFunctionId(this.targetClass);
		this.bizStateType = GemfireBizUtil.getBizStateType(this.targetClass);
		
		// note that all IBiz functions must pretend to gfe that
		// they return a result
		// or else we cannot support sync operation for
		// 'void' return methods.
		// Gemfire does not permit 'waiting' for pure
		// completion of a function,
		// it can only wait for a result to be returned
		hasResult = true; 
	}

	@Override
	public abstract void execute(FunctionContext fc);

	/*
	 * @see com.gemstone.gemfire.cache.execute.Function#getId()
	 */
	@Override
	public String getId()
	{
		return functionId;
	}

	/*
	 * @see com.gemstone.gemfire.cache.execute.Function#hasResult()
	 */
	@Override
	public boolean hasResult()
	{
		return hasResult;
	}

	/*
	 * @see com.gemstone.gemfire.cache.execute.Function#optimizeForWrite()
	 */
	@Override
	public boolean optimizeForWrite()
	{
		return optimizeForWrite;
	}

}
