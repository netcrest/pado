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
package com.netcrest.pado.gemfire.factory;

import com.netcrest.pado.biz.gemfire.proxy.GemfireBizManager;
import com.netcrest.pado.exception.BizException;
import com.netcrest.pado.internal.factory.BizManagerFactory;
import com.netcrest.pado.internal.server.BizManager;

/**
 * GemfireBizManagerFactory derives BizManagerFactory following the static
 * delegation pattern.
 * 
 * @author dpark
 * 
 */
public class GemfireBizManagerFactory extends BizManagerFactory
{
	static {
		bizManagerFactory = new GemfireBizManagerFactory();
	}
	
	/**
	 * Constructs a BizManager object for the specified IBiz class.
	 * 
	 * @param ibizClass
	 *            The target IBiz interface
	 * @param pureClient
	 *            true if this VM does not ever run the service implementation
	 *            logic (e.g. consumer only). false means that this VM can both
	 *            execute the service logic on its own as well as other VMs
	 *            (consumer and/or provider)
	 * @throws BizException
	 *             Thrown if the specified class is null or not an IBiz class.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BizManager createBizManager(Class ibizClass, boolean pureClient) throws BizException
	{
		return new GemfireBizManager(ibizClass, pureClient);
	}
	
	/**
	 * Constructs a BizManager object for the specified IBiz class name.
	 * 
	 * @param ibizClass
	 *            The target IBiz interface class name
	 * @param pureClient
	 *            true if this VM does not ever run the service implementation
	 *            logic (e.g. consumer only). false means that this VM can both
	 *            execute the service logic on its own as well as other VMs
	 *            (consumer and/or provider)
	 * @throws ClassNotFoundException
	 *             Thrown if the specified class name does not exist.
	 * @throws BizException
	 *             Thrown if the specified class is null or not an IBiz class.
	 */
	@SuppressWarnings({ "rawtypes"})
	public BizManager createBizManager(String ibizClassName, boolean pureClient) throws BizException, ClassNotFoundException
	{
		return new GemfireBizManager(ibizClassName, pureClient);
	}
}
