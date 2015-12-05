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
package com.netcrest.pado.internal.factory;

import java.lang.reflect.Method;

import com.netcrest.pado.exception.BizException;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.server.BizManager;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

/**
 * BizManagerFactory creates {@link BizManager} objects. It adheres to the
 * static delegation pattern.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class BizManagerFactory
{
	protected static BizManagerFactory bizManagerFactory;

	static {
		try {
			Class clazz = PadoUtil.getClass(Constants.PROP_CLASS_BIZ_MANAGER_FACTORY,
					Constants.DEFAULT_CLASS_BIZ_MANAGER_FACTORY);

			Method method = clazz.getMethod("getBizManagerFactory");
			try {
				bizManagerFactory = (BizManagerFactory) method.invoke(null);
			} catch (Exception e) {
				Logger.severe("BizManagerFactory creation error", e);
			}
		} catch (Exception e) {
			Logger.severe("BizManagerFactory creation error", e);
		}
	}

	/**
	 * Returns the singleton BizManagaerFactory object that delegates static
	 * method calls to the underlying data grid factory implementation object.
	 */
	public static BizManagerFactory getBizManagerFactory()
	{
		return bizManagerFactory;
	}

	/**
	 * Creates and returns a BizManager object for the specified IBiz class.
	 * This is a delegation method.
	 * 
	 * @param ibizClass
	 *            IBiz class
	 * @param pureClient
	 *            true if pure client, false if server as a client. This may or
	 *            may not be required by the underlying data grid.
	 * @throws BizException
	 *             Thrown if the specified class is null or not an IBiz class.
	 */
	public BizManager createBizManager(Class ibizClass, boolean pureClient) throws BizException
	{
		return bizManagerFactory.createBizManager(ibizClass, pureClient);
	}

	/**
	 * Creates and returns a BizManager object for the specified IBiz class name
	 * This is a delegation method.
	 * 
	 * @param ibizClass
	 *            IBiz class
	 * @param pureClient
	 *            true if pure client, false if server as a client. This may or
	 *            may not be required by the underlying data grid. * @throws
	 *            ClassNotFoundException Thrown if the specified class name does
	 *            not exist.
	 * @throws ClassNotFoundException
	 *             Thrown if the specified class name does not exist.
	 * @throws BizException
	 *             Thrown if the specified class is null or not an IBiz class.
	 */
	public BizManager createBizManager(String ibizClassName, boolean pureClient)
			throws ClassNotFoundException, BizException
	{
		return bizManagerFactory.createBizManager(ibizClassName, pureClient);
	}
}
