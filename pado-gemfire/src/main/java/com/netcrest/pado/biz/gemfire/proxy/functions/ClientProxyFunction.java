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

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.execute.FunctionContext;

/**
 * Proxy function installed for each IBiz method
 * when the current VM will never actually execute the method logic and will
 * call the method for execution in another VM
 */
public class ClientProxyFunction extends ProxyFunction
{
	private static final long serialVersionUID = 1L;
	
	private static LogWriter logger = CacheFactory.getAnyInstance().getLogger();

	public ClientProxyFunction(Class<?> ibizClass)
	{
		super(ibizClass);
	}

	/*
	 * Throws exception
	 * 
	 * @see ProxyFunction
	 */
	@Override
	public void execute(FunctionContext fc)
	{
		logger.error("Illegal IBiz method call - an attempt has been made to run an IBiz method in a member configured as a pure consumer");
		throw new IllegalStateException("Unable to execute method [" + fc.getFunctionId()
				+ "] within a pure consumer member");
	}

	@Override
	public boolean isHA()
	{
		return false;
	}

	@Override
	public boolean optimizeForWrite()
	{
		return true;
	}
}
