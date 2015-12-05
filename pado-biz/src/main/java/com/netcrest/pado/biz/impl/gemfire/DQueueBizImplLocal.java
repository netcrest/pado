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
package com.netcrest.pado.biz.impl.gemfire;

import java.util.List;

import javax.annotation.Resource;

import com.gemstone.gemfire.addon.dq.DQueue;
import com.gemstone.gemfire.addon.dq.DQueueAttributes;
import com.gemstone.gemfire.addon.dq.DQueueFactory;
import com.gemstone.gemfire.cache.client.Pool;
import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.biz.IDQueueBiz;
import com.netcrest.pado.exception.InvalidArgument;
import com.netcrest.pado.gemfire.GemfireGridService;

@SuppressWarnings("unchecked")
public class DQueueBizImplLocal<V> implements IDQueueBiz<V>, IBizLocal
{
	@Resource
	private IDQueueBiz<V> biz;
	private DQueue queue;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		if (args == null || args.length < 1) {
			throw new InvalidArgument(
					"DQueueBiz requires two arguments: String gridId, String queueId. If gridId is null then the default grid ID is assigned.");
		}
		if (args[0] == null) {
			throw new InvalidArgument("DQueueBiz requires a non-null queueId argument.");
		}
		String queueId = args[0].toString();
		String gridId;
		if (args.length == 1 || args[1] == null) {
			gridId = biz.getBizContext().getGridService().getDefaultGridId();
		} else {
			gridId = args[1].toString();
		}

		// Create the attributes
		DQueueAttributes attributes;
		if (biz.getBizContext().getGridService().isPureClient()) {
			GemfireGridService ggs = (GemfireGridService) biz.getBizContext().getGridService();
			Pool pool = ggs.getPool(gridId);
			if (pool == null) {
				throw new InvalidArgument("Connection pool undefined for the grid " + gridId
						+ ". Unable to create a DQueue instance.");
			}
			attributes = DQueueAttributes.createClientQueueAttributes(pool.getName());
		} else {
			attributes = DQueueAttributes.createPartitionedQueueAttributes(40);
		}

		// Create the dqueue
		this.queue = DQueueFactory.createDQueue(queueId, attributes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	@Override
	public boolean offer(Object type, V value)
	{
		return queue.offer(type, value);
	}

	@Override
	public V peek(Object type)
	{
		return (V) queue.peek(type);
	}

	@Override
	public List<V> peek(Object type, int count)
	{
		return (List<V>) queue.peek(type, count);
	}

	@Override
	public V element(Object type)
	{
		return (V) queue.element(type);
	}

	@Override
	public V poll(Object type)
	{
		return (V) queue.peek(type);
	}

	@Override
	public List<V> poll(Object type, int count)
	{
		return (List<V>) queue.peek(type, count);
	}

	@Override
	public V remove(Object type)
	{
		return (V) queue.remove(type);
	}

	@Override
	public boolean take(Object type)
	{
		return queue.take(type);
	}

	@Override
	public boolean take(Object type, int count)
	{
		return queue.take(type, count);
	}
}
