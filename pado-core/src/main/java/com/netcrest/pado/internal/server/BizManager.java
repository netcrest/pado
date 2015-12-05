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
package com.netcrest.pado.internal.server;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.exception.BizException;
import com.netcrest.pado.internal.impl.GridService;
import com.netcrest.pado.log.Logger;

/**
 * This class generates all IBiz artifacts required to install a specified
 * service class methods as the underlying data grid services. It configures
 * Gemfire by analyzing annotations declared against the service interface
 * class.
 */
public abstract class BizManager<T>
{
	protected Class<T> ibizClass;
	protected boolean pureClient;
	protected String appId;
	protected GridService gridService;

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
	public BizManager(Class<T> ibizClass, boolean pureClient) throws BizException
	{
		if (ibizClass == null) {
			throw new BizException("Invalid IBiz class (cannot be null)");
		}
		if (IBiz.class.isAssignableFrom(ibizClass) == false) {
			throw new BizException("Invalid IBiz class (must extend IBiz): " + ibizClass.getName());
		}
		this.ibizClass = ibizClass;
		this.pureClient = pureClient;
		if (Logger.isFineEnabled()) {
			Logger.fine("Instrumenting class [" + ibizClass.getName() + "] as an IBiz method consumer only = ["
					+ Boolean.toString(pureClient) + "]");
		}
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
	@SuppressWarnings("unchecked")
	public BizManager(String ibizClassName, boolean pureClient) throws ClassNotFoundException, BizException
	{
		if (ibizClassName == null) {
			throw new BizException("Invalid IBiz class name (cannot be null)");
		}
		this.ibizClass = (Class<T>) Class.forName(ibizClassName);
		if (IBiz.class.isAssignableFrom(ibizClass) == false) {
			throw new BizException("Invalid IBiz class (must extend IBiz): " + ibizClassName);
		}
		this.pureClient = pureClient;
		if (Logger.isFineEnabled()) {
			Logger.fine("Instrumenting class [" + ibizClass.getName() + "] as an IBiz method consumer only = ["
					+ Boolean.toString(pureClient) + "]");
		}
	}

	/**
	 * Installs the corresponding GemFire function for the target class.
	 */
	public void init()
	{
	}

	public void setAppId(String appId)
	{
		this.appId = appId;
	}

	public String getAppId()
	{
		return appId;
	}

	public GridService getGridService()
	{
		return gridService;
	}

	public void setGridService(GridService gridService)
	{
		this.gridService = gridService;
	}

	/**
	 * Unregisters the target class. The target class is no longer usable after
	 * this method call.
	 */
	public void close()
	{
	}

	/**
	 * @return The target service interface
	 */
	public Class<T> getTargetClass()
	{
		return ibizClass;
	}

	/**
	 * @param targetClass
	 *            The target service interface
	 */
	public void setTargetClass(Class<T> targetClass)
	{
		this.ibizClass = targetClass;
	}

	/**
	 * Default proxy generator. The invocation handler will expect to detect
	 * request routing configuration from annotations added to the service
	 * interface class. Using annotations to insert this routing information
	 * fixes the routing at compile time if this is not desired then use one of
	 * the alternative getProxy(...) calls
	 * 
	 * @return Client side IBiz proxy to the specified service interface
	 */
	public abstract T newClientProxy();

}
