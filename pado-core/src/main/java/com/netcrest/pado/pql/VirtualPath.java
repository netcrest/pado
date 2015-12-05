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
package com.netcrest.pado.pql;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.IVirtualPath;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.temporal.ITemporalBizLink;

/**
 * VirtualPath is a logical grid path that augments physical grid paths for
 * enabling schema-on-read with embedded entity relationships in run-time.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
public class VirtualPath<T> extends VirtualCompiledUnit implements IVirtualPath<T>
{
	private ICatalog catalog;
	private LinkedBlockingQueue<ITemporalBizLink> temporalBizPool = new LinkedBlockingQueue<ITemporalBizLink>();
	private ExecutorService es = null;
	private int threadPoolSize = -1;

	/**
	 * Constructs a new VirtualPath instance with the specified virtual path
	 * definition (vpd). This constructor automatically determines the thread
	 * pool size.
	 * 
	 * @param vpd
	 *            Virtual Path Definition
	 * @param pado
	 *            Pado instance
	 */
	public VirtualPath(KeyMap vpd, IPado pado)
	{
		this(vpd, pado.getCatalog(), -1);
	}

	/**
	 * Constructs a new VirtualPath instance with the specified virtual path
	 * definition (vpd). This constructor automatically determines the thread
	 * pool size.
	 * 
	 * @param vpd
	 *            Virtual Path Definition
	 * @param catalog
	 *            Pado IBiz catalog
	 */
	public VirtualPath(KeyMap vpd, ICatalog catalog)
	{
		this(vpd, catalog, -1);
	}

	/**
	 * Constructs a new VirtualPath instance with the spcified virtual path
	 * definitions (vpd) and the specified thread pool size.
	 * 
	 * @param vpd
	 *            Virtual Path Definition
	 * @param pado
	 *            Pado instance
	 * @param threadPoolSize
	 *            Thread pool size for this virtual path. If the thread size is
	 *            > 0 then VirtualPath creates a thread pool of that size and
	 *            parallelizes query executions where possible to attain the
	 *            best performance. Note that setting this value to a large
	 *            number can negatively impact the performance especially if the
	 *            caller is already highly multi-threaded. As a rule of thumb,
	 *            the thread pool size should be less than or equal to the max
	 *            number of orthogonal queries defined in the VPD. If < 0 then
	 *            it automatically determines the thread pool size.
	 */
	public VirtualPath(KeyMap vpd, IPado pado, int threadPoolSize)
	{
		this(vpd, pado.getCatalog(), threadPoolSize);
	}

	/**
	 * Constructs a new VirtualPath instance with the spcified virtual path
	 * definitions (vpd) and the specified thread pool size.
	 * 
	 * @param vpd
	 *            Virtual Path Definition
	 * @param catalog
	 *            Pado IBiz catalog
	 * @param threadPoolSize
	 *            Thread pool size for this virtual path. If the thread size is
	 *            > 0 then VirtualPath creates a thread pool of that size and
	 *            parallelizes query executions where possible to attain the
	 *            best performance. Note that setting this value to a large
	 *            number can negatively impact the performance especially if the
	 *            caller is already highly multi-threaded. As a rule of thumb,
	 *            the thread pool size should be less than or equal to the max
	 *            number of orthogonal queries defined in the VPD. If < 0 then
	 *            it automatically determines the thread pool size.
	 */
	public VirtualPath(KeyMap vpd, ICatalog catalog, int threadPoolSize)
	{
		super(vpd);
		this.catalog = catalog;
		if (threadPoolSize < 0) {
			int size = -1;
			for (int i = 0; i < parallelCUOrders.length; i++) {
				if (parallelCUOrders[i].length > size) {
					size = parallelCUOrders[i].length;
				}
			}
			if (size > 1) {
				es = Executors.newFixedThreadPool(size);
				this.threadPoolSize = size;
			} else {
				this.threadPoolSize = 0;
			}
		} else if (threadPoolSize == 0 || threadPoolSize == 1) {
			this.threadPoolSize = 0;
		} else {
			es = Executors.newFixedThreadPool(threadPoolSize);
			this.threadPoolSize = threadPoolSize;
		}
	}

	/**
	 * Returns the thread pool size. The returned value may be different from
	 * the constructor-specified value. VirtualPath may determine the
	 * appropriate value based on the VPD.
	 */
	public int getThreadPoolSize()
	{
		return this.threadPoolSize;
	}

	private ITemporalBizLink pollTemporalBiz()
	{
		ITemporalBizLink temporalBiz = temporalBizPool.poll();
		if (temporalBiz == null) {
			// Use class name to avoid the dependency issue
			temporalBiz = (ITemporalBizLink)catalog.newInstance("com.netcrest.pado.biz.ITemporalBiz");
		}
		return temporalBiz;
	}

	private void putTemporalBiz(ITemporalBizLink temporalBiz)
	{
		temporalBizPool.add(temporalBiz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> execute(Object input)
	{
		return execute(input, -1, -1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> execute(Object input, long validAtTime)
	{
		return execute(input, -1, -1);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<T> execute(Object input, long validAtTime, long asOfTime)
	{
		KeyMap inputKeyMap = null;
		if (input instanceof KeyMap) {
			inputKeyMap = (KeyMap) input;
		}
		if (inputKeyType != null) {
			if (inputKeyMap == null || inputKeyMap.getKeyType() == null) {
				return new ArrayList<T>(0);
			}
			// TODO: validate if the key map's KeyType is inputKeyType.
		}

		List[] results = new List[allCUs.length];

		// First, execute all input queries in parallel
		results = execute(inputCUOrders, input, results, validAtTime, asOfTime);

		// Execute the remaining query attributes
		for (int i = 0; i < parallelCUOrders.length; i++) {
			results = execute(parallelCUOrders[i], input, results, validAtTime, asOfTime);
		}
		
		// Results must not be empty
		for (List list : results) {
			if (list == null || list.size() == 0) {
				return new ArrayList<T>(0); 
			}
		}

		if (isFlatOutput) {
			return fillFlat(inputKeyMap, results);
		} else {
			return fillNested(inputKeyMap, results);
		}
	}

	protected List[] execute(CUOrder cuOrder[], Object input, List[] results, long validAtTime, long asOfTime)
	{
		if (cuOrder.length == 1) {
			
			// Execute in the current thread if only one CUOrder
			CompiledUnit cu = cuOrder[0].cu;
			String executableQuery = cu.getQuery(input, results);
			
			if (cuOrder[0].index != 0 && (executableQuery == null || executableQuery.length() == 0)) {
				// Sub-queries cannot be empty which returns all of data.
				results[cuOrder[0].index] = null;
			} else {
				String path = cu.getPaths()[0];
				TemporalTask temporalTask = new TemporalTask(path, executableQuery, validAtTime, asOfTime);
				results[cuOrder[0].index] = temporalTask.call();
			}

		} else if (es == null) {

			// Execute all in the current thread if the pool is not defined.
			for (CUOrder cuo : cuOrder) {
				CompiledUnit cu = cuo.cu;
				String executableQuery = cu.getQuery(input, results);
				if (cuOrder[0].index != 0 && (executableQuery == null || executableQuery.length() == 0)) {
					// Sub-queries cannot be empty which returns all of data.
					results[cuOrder[0].index] = null;
				} else  {
					String path = cu.getPaths()[0];
					TemporalTask temporalTask = new TemporalTask(path, executableQuery, validAtTime, asOfTime);
					results[cuOrder[0].index] = temporalTask.call();
				}
			}

		} else {

			// Execute in parallel if more than one CUOrder
			ArrayList<TemporalTask> taskList = new ArrayList<TemporalTask>(cuOrder.length);
			for (CUOrder cuo : cuOrder) {
				CompiledUnit cu = cuo.cu;
				String executableQuery = cu.getQuery(input, results);
				if (cuOrder[0].index != 0 && (executableQuery == null || executableQuery.length() == 0)) {
					// Sub-queries cannot be empty which returns all of data.
					results[cuOrder[0].index] = null;
				} else {
					String path = cu.getPaths()[0];
					TemporalTask temporalTask = new TemporalTask(path, executableQuery, validAtTime, asOfTime);
					taskList.add(temporalTask);
				}
			}

			List<Future<List>> futureList;
			try {
				futureList = es.invokeAll(taskList);
				for (int i = 0; i < futureList.size(); i++) {
					Future<List> future = futureList.get(i);
					List result = future.get();
					CUOrder cuo = cuOrder[i];
					results[cuo.index] = result;
				}
			} catch (Exception ex) {
				throw new PadoException(ex);
			}
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public void close()
	{
		if (es != null) {
			es.shutdown();
		}
		es = null;
	}

	class TemporalTask implements Callable<List>
	{
		String executableQuery;
		String path;
		long validAtTime;
		long asOfTime;

		TemporalTask(String path, String executableQuery, long validAtTime, long asOfTime)
		{
			this.path = path;
			this.executableQuery = executableQuery;
			this.validAtTime = validAtTime;
			this.asOfTime = asOfTime;
		}

		@Override
		public List call()
		{
			ITemporalBizLink temporalBiz = pollTemporalBiz();
			String gridIds[] = temporalBiz.getBizContext().getGridService().getGridIds(path);
			temporalBiz.getBizContext().getGridContextClient().setGridIds(gridIds);
			temporalBiz.setGridPath(path);
			List resultList = temporalBiz.getQueryValues(path + "?" + executableQuery, validAtTime, asOfTime);
			putTemporalBiz(temporalBiz);
			return resultList;
		}

	}
}
