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

import java.util.List;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.link.IDQueueBizLink;


/**
 * IDQueueBiz is a client interface to distributed queues supported by the
 * underlying data grids. Note that not all data grids support distributed
 * queues. Distributed queues must be pre-configured and made available to
 * clients by the grid before IDQueueBiz can be used. 
 * 
 * <p>
 * <b>Arguments:
 * {@link IBizLocal#init(IBiz, com.netcrest.pado.IPado, Object...)}</b>
 * <p>
 * <blockquote> <b>String dqueueName</b> - DQueue name that uniquely identifies
 * the distributed queue to which messages are submitted and retrieved.
 * </blockquote> <blockquote> <b>String gridId</b> - Optional grid ID. If not
 * specified or null, then the default grid ID is assigned. </blockquote>
 * 
 * @author dpark
 * 
 */
@BizClass(name = "IDQueueBiz")
public interface IDQueueBiz<V> extends IDQueueBizLink<V>
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offer(Object type, V value);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V peek(Object type);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<V> peek(Object type, int count);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V element(Object type);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V poll(Object type);

	/**
	 * {@inheritDoc}
	 */
	public List<V> poll(Object type, int count);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V remove(Object type);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean take(Object type);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean take(Object type, int count);
}
