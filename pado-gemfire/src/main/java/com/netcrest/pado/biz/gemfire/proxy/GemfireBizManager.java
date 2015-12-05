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
package com.netcrest.pado.biz.gemfire.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Set;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.netcrest.pado.biz.gemfire.client.proxy.handlers.DefaultInvocationHandler;
import com.netcrest.pado.biz.gemfire.client.proxy.handlers.MembersInvocationHandler;
import com.netcrest.pado.biz.gemfire.client.proxy.handlers.RegionInvocationHandler;
import com.netcrest.pado.biz.gemfire.client.proxy.handlers.ServerPoolInvocationHandler;
import com.netcrest.pado.biz.gemfire.proxy.functions.ClientProxyFunction;
import com.netcrest.pado.biz.gemfire.proxy.functions.ServerProxyFunction;
import com.netcrest.pado.exception.BizException;
import com.netcrest.pado.internal.biz.util.BizUtil;
import com.netcrest.pado.internal.server.BizManager;
import com.netcrest.pado.log.Logger;

/**
 * This class generates all Gemfire artifacts required to install a
 * specified service class methods as Gemfire functions. It configures Gemfire
 * by analyzing annotations declared against the service interface class.
 * 
 * This class also provides a number of factory methods for creating client
 * proxy classes that can be used to invoke the service methods, by default the
 * routing of these calls is taken from the annotations declared in the service
 * interface class, however a number of methods are provided in order to
 * override this routing and provide the opportunity to route calls based on
 * parameters that are better determined at runtime.
 * 
 */
public class GemfireBizManager<T> extends BizManager<T>
{
	private Function function;

	/**
	 * {@inheritDoc}
	 */
	public GemfireBizManager(Class<T> ibizClass, boolean pureClient) throws BizException
	{
		super(ibizClass, pureClient);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public GemfireBizManager(String ibizClassName, boolean pureClient) throws BizException, ClassNotFoundException
	{
		super(ibizClassName, pureClient);
	}

	/**
	 * Installs the corresponding GemFire function for the target class.
	 */
	public void init()
	{
		if (pureClient) {
			function = new ClientProxyFunction(ibizClass);
		} else {
			Class<?> bizImplClass = null;
			Class<?> bizImplLocalClass = null;
			try {
				bizImplClass = ibizClass.getClassLoader().loadClass(
						GemfireBizUtil.getBizImplClassName(ibizClass));				
			} catch (ClassNotFoundException ex) {
				// ignore
			} 
			String localImplClassName = BizUtil.getExplicitLocalImplClassName(ibizClass);
			if (localImplClassName == null || localImplClassName.length() == 0) {
				localImplClassName = BizUtil.getImplicitBizLocalImplClassName(ibizClass);
			}
			try {
				bizImplLocalClass = ibizClass.getClassLoader().loadClass(localImplClassName);
			} catch (ClassNotFoundException e) {
				// ignore
			}
			
			if (bizImplClass == null && bizImplLocalClass == null) {
				Logger.warning(ibizClass.getCanonicalName() + ": Registration failed. Neither server nor local IBiz implemenation class found.");
			} else {
				if (bizImplClass != null) {
					function = new ServerProxyFunction(ibizClass);
					FunctionService.registerFunction(function);
				}
				StringBuffer buffer = new StringBuffer(100);
				buffer.append("Registered ");
				if (bizImplClass != null) {
					buffer.append(bizImplClass.getName());
				}
				if (bizImplLocalClass != null) {
					if (bizImplClass != null) {
						buffer.append(", ");
					}
					buffer.append(bizImplLocalClass.getName());
				}
				Logger.config(ibizClass.getCanonicalName() + ": " + buffer.toString());
			}
		}
	}

	/**
	 * Unregisters the target class. The target class is no longer usable
	 * after this method call.
	 */
	public void close()
	{
		if (function == null) {
			return;
		}
		FunctionService.unregisterFunction(function.getId());
		function = null;
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
	@SuppressWarnings("unchecked")
	public T newClientProxy()
	{
		InvocationHandler handler = new DefaultInvocationHandler<T>(ibizClass, gridService);
		return (T) Proxy.newProxyInstance(ibizClass.getClassLoader(), new Class[] { ibizClass }, handler);
	}

	/**
	 * Member broadcast based proxy generator. The proxy will forward requests
	 * to all members in the specified Distributed System. This routing will
	 * override any annotation based routing specifications for the methods
	 * invoked via the returned proxy. This method is useful since the DS is not
	 * available until runtime and therefore this routing cannot be configured
	 * by annotations.
	 * 
	 * @param ds
	 *            Target distributed system
	 * 
	 * @return Client side IBiz proxy to the specified service interface
	 */
	@SuppressWarnings("unchecked")
	public T newClientProxy(DistributedSystem ds)
	{
		InvocationHandler handler = new MembersInvocationHandler<T>(ibizClass, ds, gridService);
		return (T) Proxy.newProxyInstance(ibizClass.getClassLoader(), new Class[] { ibizClass }, handler);

	}

	/**
	 * Member based proxy generator. The proxy will forward requests to the
	 * specified member set. This routing will override any annotation based
	 * routing specifications for the methods invoked via the returned proxy.
	 * This method is useful since the DS and member details are not available
	 * until runtime and therefore this routing cannot be configured by
	 * annotations.
	 * 
	 * @param filterSet
	 *            Set of keys used to route request
	 * 
	 * @return Client side IBiz proxy to the specified service interface
	 */
	@SuppressWarnings("unchecked")
	public T newClientProxy(DistributedSystem ds, Set<DistributedMember> members)
	{
		InvocationHandler handler = new MembersInvocationHandler<T>(ibizClass, ds, members, appId, gridService);
		return (T) Proxy.newProxyInstance(ibizClass.getClassLoader(), new Class[] { ibizClass }, handler);

	}

	/**
	 * Region based proxy generator. The proxy will forward requests to the
	 * members hosting the specified region. This routing will override any
	 * annotation based routing specifications for the methods invoked via the
	 * returned proxy.
	 * 
	 * @param region
	 *            Region path
	 * 
	 * @return Client side IBiz proxy to the specified service interface
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public T newClientProxy(Region region)
	{
		InvocationHandler handler = new RegionInvocationHandler<T>(ibizClass, region, gridService);
		return (T) Proxy.newProxyInstance(ibizClass.getClassLoader(), new Class[] { ibizClass }, handler);

	}

	/**
	 * Region and key based proxy generator. The proxy will forward requests to
	 * the members hosting the specified region that contain the specified keys.
	 * This routing will override any annotation based routing specifications
	 * for the methods invoked via the returned proxy.
	 * 
	 * @param region
	 *            Region used to route the request
	 * @param filterSet
	 *            Set of keys used to route request
	 * 
	 * @return Client side IBiz proxy to the specified service interface
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public T newClientProxy(Region region, Set<Object> filterSet)
	{
		InvocationHandler handler = new RegionInvocationHandler<T>(ibizClass, region, filterSet, gridService);
		return (T) Proxy.newProxyInstance(ibizClass.getClassLoader(), new Class[] { ibizClass }, handler);
	}

	/**
	 * Server pool based proxy generator. The proxy will forward requests to
	 * server members belonging to the specified pool. This routing will
	 * override any annotation based routing specifications for the methods
	 * invoked via the returned proxy.
	 * 
	 * @param pool
	 *            Pool used to route the request
	 * 
	 * @return Client side IBiz proxy to the specified service interface
	 */
	@SuppressWarnings("unchecked")
	public T newClientProxy(Pool pool, boolean broadcast)
	{
		InvocationHandler handler = new ServerPoolInvocationHandler<T>(ibizClass, pool, broadcast, gridService);
		return (T) Proxy.newProxyInstance(ibizClass.getClassLoader(), new Class[] { ibizClass }, handler);
	}

}
