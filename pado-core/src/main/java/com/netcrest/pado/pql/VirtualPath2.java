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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.IVirtualPath;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.internal.pql.antlr4.PqlEvalDriver;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.server.VirtualPathEngine;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.util.GridUtil;

/**
 * VirtualPath is a logical grid path that augments physical grid paths for
 * enabling schema-on-read with embedded entity relationships in run-time.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
public class VirtualPath2<T> implements IVirtualPath<T>
{
	private ICatalog catalog;
	private LinkedBlockingQueue<ITemporalBizLink> temporalBizPool = new LinkedBlockingQueue<ITemporalBizLink>();
	private ExecutorService es = null;
	private int threadPoolSize = -1;
	private KeyMap vpd;
	private String pathName;
	private String gridPath;
	private String fullPath;
	private String[] argNames;
	private List<VirtualPath2> childVirtualPathList = new ArrayList<VirtualPath2>(2);

	private static VirtualPath2 rootVirtualPath;

	/**
	 * Initializes if the if the root virtual path is undefined and returns the
	 * root virtual path. Due to a cyclic call during the grid startup, the grid
	 * root path is required for the first call of this method.
	 * 
	 * @param gridRootPath
	 *            Grid root path. Must be non-null for the first call.
	 */
	public final static synchronized VirtualPath2 initializeRootVirtualPath(String gridRootPath)
	{
		if (rootVirtualPath == null) {
			rootVirtualPath = new VirtualPath2();
			String gridRootPathName;
			if (gridRootPath == null) {
				// This should never occur.
				gridRootPath = PadoServerManager.getPadoServerManager().getGridInfo().getGridRootPath();
			}
			gridRootPathName = GridUtil.getPathName(gridRootPath);
			rootVirtualPath.addChild(gridRootPathName);
		}
		return rootVirtualPath;
	}

	public final static VirtualPath2 getRootVirtualPath()
	{
		return rootVirtualPath;
	}

	/**
	 * Resets virtual paths by removing all virtual paths.
	 */
	public final static void reset()
	{
		if (rootVirtualPath != null) {
			VirtualPath2 gridRootVp = (VirtualPath2) rootVirtualPath.childVirtualPathList.get(0);
			gridRootVp.childVirtualPathList.clear();
		}
	}

	private VirtualPath2()
	{
		this.fullPath = "/";
		this.gridPath = null;
		this.pathName = null;
	}

	private VirtualPath2(String pathName)
	{
		this.pathName = pathName;
	}

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
	public VirtualPath2(KeyMap vpd, IPado pado)
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
	public VirtualPath2(KeyMap vpd, ICatalog catalog)
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
	public VirtualPath2(KeyMap vpd, IPado pado, int threadPoolSize)
	{
		this(vpd, pado.getCatalog(), threadPoolSize);
	}

	/**
	 * Constructs a new VirtualPath instance with the specified virtual path
	 * definition (vpd) and the specified thread pool size.
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
	public VirtualPath2(KeyMap vpd, ICatalog catalog, int threadPoolSize)
	{
		this.vpd = vpd;
		this.catalog = catalog;
		this.threadPoolSize = threadPoolSize;
		this.gridPath = (String) vpd.get("VirtualPath");
		this.pathName = GridUtil.getPathName(getGridPath());
		this.fullPath = GridUtil.getFullPath(gridPath);
		if (vpd != null) {
			Boolean isEntity = (Boolean)vpd.get("IsEntity");
			if (isEntity != null && isEntity) {
				// TODO: Get arg names for entity path. Not needed for now.
			} else {
				String query = (String)vpd.get("Query");
				if (query != null) {
					this.argNames = PqlEvalDriver.getArgNames(query);
				}
			}
		}

		buildGridPathHierarchy();

		// if (threadPoolSize < 0) {
		// int size = -1;
		// for (int i = 0; i < parallelCUOrders.length; i++) {
		// if (parallelCUOrders[i].length > size) {
		// size = parallelCUOrders[i].length;
		// }
		// }
		// if (size > 1) {
		// es = Executors.newFixedThreadPool(size, new ThreadFactory() {
		// private int threadNum = 1;
		// public Thread newThread(Runnable r) {
		// Thread t = new Thread(r, "Pado-VirtualPath-" + threadNum);
		// t.setDaemon(true);
		// return t;
		// }
		// });
		// this.threadPoolSize = size;
		// } else {
		// this.threadPoolSize = 0;
		// }
		// } else if (threadPoolSize == 0 || threadPoolSize == 1) {
		// this.threadPoolSize = 0;
		// } else {
		// es = Executors.newFixedThreadPool(threadPoolSize, new ThreadFactory()
		// {
		// public Thread newThread(Runnable r) {
		// Thread t = new Thread(r, "VirtualPathThread");
		// t.setDaemon(true);
		// return t;
		// }
		// });
		// this.threadPoolSize = threadPoolSize;
		// }
	}
	
	public String[] getArgNames()
	{
		return argNames;
	}

	/**
	 * Builds the virtual path hierarchy by breaking down the grid path into
	 * children.
	 */
	private synchronized void buildGridPathHierarchy()
	{
		if (PadoUtil.isPureClient()) {
			return;
		}
		String gridPath = getGridPath();
		String split[] = gridPath.split("/");

		VirtualPath2 vp = (VirtualPath2) rootVirtualPath.getChildVirtualPathList().get(0);
		for (int i = 0; i < split.length - 1; i++) {
			vp = vp.addChild(split[i]);
		}
		vp.putChild(this);
	}

	/**
	 * Adds if new or overwrites the existing child if exists. It moves all of
	 * the existing virtual path's children to the specified child.
	 * 
	 * @param childVp
	 *            Child virtual path
	 */
	@SuppressWarnings("unchecked")
	private void putChild(VirtualPath2 childVp)
	{
		// If child already exists then move its children to
		// the new child VP.
		int index = childVirtualPathList.indexOf(childVp);
		if (index != -1) {
			VirtualPath2 vp = childVirtualPathList.get(index);
			for (VirtualPath2 vp2 : (List<VirtualPath2>) vp.childVirtualPathList) {
				childVp.childVirtualPathList.add(vp2);
			}
			childVirtualPathList.set(index, childVp);
		} else {
			childVirtualPathList.add(childVp);
		}
	}

	/**
	 * Adds a new child if new. Unlike {@linkplain #putChild(VirtualPath2)},
	 * this method does not overwrite the existing child.
	 * 
	 * @param childPathName
	 *            Child path name
	 * @return Child virtual path
	 */
	private VirtualPath2 addChild(String childPathName)
	{
		VirtualPath2 childVp = null;
		for (VirtualPath2 vp : childVirtualPathList) {
			if (childPathName.equals(vp.pathName)) {
				childVp = vp;
			}
		}
		if (childVp == null) {
			childVp = new VirtualPath2(childPathName);
			if (isRootPath()) {
				childVp.fullPath = fullPath + childPathName;
				childVp.gridPath = "";
			} else {
				childVp.fullPath = this.fullPath + "/" + childPathName;
				if (isGridRootPath()) {
					childVp.gridPath = childPathName;
				} else {
					childVp.gridPath = this.gridPath + "/" + childPathName;
				}
			}
			childVirtualPathList.add(childVp);
		}
		return childVp;
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
			// Use class name to avoid the class loader dependency issue
			temporalBiz = (ITemporalBizLink) catalog.newInstance("com.netcrest.pado.biz.ITemporalBiz");
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
	public List<T> execute(long validAtTime, long asOfTime, String... args)
	{
		Boolean isEntity = (Boolean)vpd.get("IsEntity");
		if (isEntity != null && isEntity) {
			return executeEntity(validAtTime, asOfTime, args);
		} else {
			String joinQueryString = (String) vpd.get("Query");
			ITemporalBizLink temporalBiz = pollTemporalBiz();
			try {
				return PqlEvalDriver.executeValues(temporalBiz, validAtTime, asOfTime, joinQueryString, args);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public List<T> executeEntity(long validAtTime, long asOfTime, String... args)
	{
		Boolean isEntity = (Boolean)vpd.get("IsEntity");
		if (isEntity != null && isEntity) {
			String virtualPath = (String)vpd.get("VirtualPath");
			VirtualCompiledUnit2 vcu = VirtualPathEngine.getVirtualPathEngine().getVirtualCompiledUnit(virtualPath);
			if (vcu == null) {
				return null;
			}
			String entityPql = vcu.getEntityPql(args);
			ITemporalBizLink temporalBiz = pollTemporalBiz();
			return temporalBiz.getQueryValues(entityPql, validAtTime, asOfTime);
			
		} else {
//			String queryString = (String) vpd.get("Query");
//			ITemporalBizLink temporalBiz = pollTemporalBiz();
//			try {
//				return PqlEvalDriver.executeValues(temporalBiz, validAtTime, asOfTime, queryString, args);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPathName()
	{
		return pathName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getGridPath()
	{
		return gridPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFullPath()
	{
		return fullPath;
	}

	private boolean isRootPath()
	{
		return fullPath.equals("/");
	}

	private boolean isGridRootPath()
	{
		return gridPath != null && gridPath.length() == 0;
	}

	public List<VirtualPath2> getChildVirtualPathList()
	{
		return childVirtualPathList;
	}

	public KeyMap getVirtualPathDefinition()
	{
		return vpd;
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

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof VirtualPath2 == false) {
			return false;
		}
		VirtualPath2 vp = (VirtualPath2) o;
		return pathName == null ? vp.pathName == null : pathName.equals(vp.pathName);
	}

	@Override
	public List<T> execute(Object input, long validAtTime, long asOfTime)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public VirtualPath2 removeVirtualPath(String gridPath)
	{
		return removeVirtualPath(rootVirtualPath, gridPath);
	}

	@SuppressWarnings("unchecked")
	private VirtualPath2 removeVirtualPath(VirtualPath2 vp, String gridPath)
	{
		VirtualPath2 removedVp = null;
		Iterator<VirtualPath2> iterator = vp.getChildVirtualPathList().iterator();
		while (iterator.hasNext()) {
			VirtualPath2 vp2 = iterator.next();
			if (gridPath.equals(vp2.getGridPath())) {
				iterator.remove();
				removedVp = vp2;
				break;
			} else {
				removedVp = removeVirtualPath(vp2, gridPath);
			}
		}
		return removedVp;
	}

	/**
	 * Removes itself from the hierarchy
	 * 
	 * @return null if this object does not exist in the hierarchy, this object
	 *         if exists in the hierarchy and removed.
	 */
	public VirtualPath2 remove()
	{
		return removeVirtualPath(getGridPath());
	}
}
