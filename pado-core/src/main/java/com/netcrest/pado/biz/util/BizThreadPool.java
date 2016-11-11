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
package com.netcrest.pado.biz.util;

import java.util.HashMap;
import java.util.Map;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.ICatalog;
import com.netcrest.pado.exception.PadoServerException;
import com.netcrest.pado.log.Logger;

/**
 * BizThreadPool is an IBiz object pool that designates an instance of IBiz
 * class per current thread. This type of object pool is useful when there are a
 * fixed number of threads that require access to IBiz objects.
 * 
 * @author dpark
 * 
 * @param <T>
 *            IBiz type.
 */
public class BizThreadPool<T>
{
	private Map<Thread, IBiz> bizMap = new HashMap<Thread, IBiz>();
	private ICatalog catalog;
	private Class<T> ibizClass;
	private String ibizClassName;
	private Object[] args;

	/**
	 * Creates a BizThreadPool object for the specified parameters.
	 * 
	 * @param poolName
	 *            Pool name uniquely identifying the connection pool.
	 * @param dbUrl
	 *            Database URL
	 * @param driverClassName
	 *            JDBC driver class name.
	 * @param userName
	 *            User name.
	 * @param passwd
	 *            Password
	 */
	public BizThreadPool(ICatalog catalog, Class<T> ibizClass, Object... args)
	{
		this.catalog = catalog;
		this.ibizClass = ibizClass;
		this.args = args;
	}
	
	public BizThreadPool(ICatalog catalog, String ibizClassName, Object... args)
	{
		this.catalog = catalog;
		this.ibizClassName = ibizClassName;
		this.args = args;
	}

	/**
	 * Returns an instance of IBiz class for the current thread.
	 */
	@SuppressWarnings("unchecked")
	public T getBiz()
	{
		IBiz biz = bizMap.get(Thread.currentThread());
		if (biz == null) {
			if (ibizClass != null) {
				biz = (IBiz) catalog.newInstance(ibizClass, args);
			} else {
				biz = catalog.newInstance(ibizClassName, args);
			}
			bizMap.put(Thread.currentThread(), biz);
		}
		return (T) biz;
	}

	/**
	 * Removes the IBiz instance designated for the current thread.
	 */
	public void remove()
	{
		bizMap.remove(Thread.currentThread());
	}

	/**
	 * Removes all IBiz instances.
	 */
	synchronized void clear()
	{
		bizMap.clear();
	}
}
