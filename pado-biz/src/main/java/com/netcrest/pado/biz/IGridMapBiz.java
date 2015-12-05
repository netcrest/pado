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
package com.netcrest.pado.biz;

import java.util.Collection;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IGridMapBizLink;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.exception.GridNotAvailableException;

/**
 * IGridMapBiz provides access to key/value entries stored in a grid path.
 * 
 * <p>
 * <b>Arguments:
 * {@link IBizLocal#init(IBiz, com.netcrest.pado.IPado, Object...)}</b>
 * <p>
 * <blockquote> <b>String gridPath</b> - Grid path </blockquote>
 * 
 * @author dpark
 * 
 */
@BizClass(name = "IGridMapBiz")
public interface IGridMapBiz<K, V> extends IGridMapBizLink<K, V>
{
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer
	@Override
	public void create(K key, V value);
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer
	@Override
	public V put(K key, V value);
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@Override
	public void removeAll(Collection<? extends K> keyCollection) throws GridNotAvailableException;

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer
	@Override
	public int size();
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.BooleanAndCollector")
	@Override
	public boolean isEmpty();

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.BooleanOrCollector")
	@Override
	public boolean containsKey(Object key);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.BooleanOrCollector")
	@Override
	public boolean containsValue(Object value);
}
