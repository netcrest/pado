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

import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.netcrest.pado.biz.info.MethodArgInfo;
import com.netcrest.pado.biz.info.MethodInfo;
import com.netcrest.pado.biz.info.MethodReturnInfo;
import com.netcrest.pado.biz.info.SimpleBizInfo;
import com.netcrest.pado.info.BizInfo;
import com.netcrest.pado.info.BucketInfo;
import com.netcrest.pado.info.CacheDumpInfo;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.CacheServerInfo;
import com.netcrest.pado.info.ConfigInfo;
import com.netcrest.pado.info.DumpInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.GridPathInfo;
import com.netcrest.pado.info.GridRouterInfo;
import com.netcrest.pado.info.LoginInfo;
import com.netcrest.pado.info.PadoInfo;
import com.netcrest.pado.info.ServerInfo;
import com.netcrest.pado.info.message.GridStatusInfo;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

/**
 * InfoFactory creates Pado specific Info objects. It adheres to the static
 * delegation pattern.
 * 
 * @author dpark
 * 
 */
public abstract class InfoFactory
{
	private static InfoFactory infoFactory;

	static {
		try {
			Class clazz = PadoUtil.getClass(Constants.PROP_CLASS_INFO_FACTORY, Constants.DEFAULT_CLASS_INFO_FACTORY);

			Method method = clazz.getMethod("getInfoFactory");
			try {
				infoFactory = (InfoFactory) method.invoke(null);
			} catch (Exception e) {
				Logger.severe("InfoFactory creation error", e);
			}
		} catch (Exception e) {
			Logger.severe("InfoFactory creation error", e);
		}
	}

	/**
	 * Returns the singleton InfoFactory object that delegates static method
	 * calls to the underlying data grid factory implementation object.
	 */
	public static InfoFactory getInfoFactory()
	{
		return infoFactory;
	}

	/**
	 * Creates a LoginInfo object that provides user login information.
	 * 
	 * @param appId
	 *            App ID
	 * @param domain
	 *            Optional domain name
	 * @param username
	 *            User name
	 * @param token
	 *            Token
	 * @param bizSet
	 *            A set of BizInfo objects providing allowed IBiz information
	 */
	public LoginInfo createLoginInfo(String appId, String domain, String username, Object token, Set<BizInfo> bizSet)
	{
		return infoFactory.createLoginInfo(appId, domain, username, token, bizSet);
	}

	/**
	 * Creates a ConfigInfo object.
	 */
	public ConfigInfo createConfigInfo()
	{
		return infoFactory.createConfigInfo();
	}

	/**
	 * Creates a PadoInfo object for the specified app ID.
	 * 
	 * @param appId
	 *            App ID
	 */
	public PadoInfo createPadoInfo(String appId)
	{
		return infoFactory.createPadoInfo(appId);
	}

	/**
	 * Creats a BizInfo object for the specified IBiz interface class name.
	 * 
	 * @param ibizClassName
	 *            IBiz class name
	 */
	public BizInfo createBizInfo(String ibizClassName)
	{
		return infoFactory.createBizInfo(ibizClassName);
	}

	/**
	 * Creates a SimpleBizInfo object for the specified IBiz interface class
	 * name. Returned object is serializable.
	 * 
	 * @param ibizClassName
	 *            IBiz class name
	 */
	public SimpleBizInfo createSimpleBizInfo(String ibizClassName)
	{
		return infoFactory.createSimpleBizInfo(ibizClassName);
	}

	/**
	 * Creates a MethodIfo object. Returned object is serializable.
	 */
	public MethodInfo createMethodInfo()
	{
		return infoFactory.createMethodInfo();
	}

	/**
	 * Creates a MethodArgInfo object. Returned object is serializable.
	 */
	public MethodArgInfo createMethodArgInfo()
	{
		return infoFactory.createMethodArgInfo();
	}
	
	/**
	 * Creates a MethodReturnInfo object. Returned object is serializable.
	 */
	public MethodReturnInfo createMethodReturnInfo()
	{
		return infoFactory.createMethodReturnInfo();
	}

	/**
	 * Creates a BucketInfo object. The bucket concept may or may not be
	 * available depending on the underlying data grid product.
	 * 
	 * @param bucketId
	 *            Bucket ID
	 * @param size
	 *            Bucket size
	 * @param totalBytes
	 *            Total number of bytes
	 */
	public BucketInfo createBucketInfo(int bucketId, int size, long totalBytes)
	{
		return infoFactory.createBucketInfo(bucketId, size, totalBytes);
	}

	/**
	 * Creates a CacheServerInfo object.
	 */
	public CacheServerInfo createCacheServerInfo()
	{
		return infoFactory.createCacheServerInfo();
	}

	/**
	 * Creates a GridRouterInfo object for the specified grid path.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	public GridRouterInfo createGridRouterInfo(String gridPath)
	{
		return infoFactory.createGridRouterInfo(gridPath);
	}

	/**
	 * Creates a ServerInfo object which provides a single server (or JVM)
	 * status.
	 * 
	 * @param gridInfo
	 *            Grid info
	 * @param cacheInfo
	 *            Cache info
	 * @param cacheServerInfo
	 *            Cache server info
	 * @param fullPath
	 *            Full path specific to the grid. This is different from the
	 *            grid path which may be relative to the root path or mapped to
	 *            naming conventions depending on the underlying data grid
	 *            product.
	 * @return
	 */
	public ServerInfo createServerInfo(GridInfo gridInfo, CacheInfo cacheInfo, CacheServerInfo cacheServerInfo,
			String fullPath)
	{
		return infoFactory.createServerInfo(gridInfo, cacheInfo, cacheServerInfo, fullPath);
	}

	/**
	 * Creates a GridStatusInfo object.
	 * 
	 * @param status
	 *            Grid status info
	 * @param gridId
	 *            Grid ID
	 * @param masterId
	 *            Master server ID
	 * @param serverId
	 *            Server ID uniquely identifying a server
	 * @param message
	 *            Status message
	 * @return
	 */
	public GridStatusInfo createGridStatusInfo(GridStatusInfo.Status status, String gridId, String masterId,
			String serverId, String message)
	{
		return infoFactory.createGridStatusInfo(status, gridId, masterId, serverId, message);
	}

	/**
	 * Creates a GridPathInfo object for the specified grid path.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	public GridPathInfo createGridPathInfo(String gridPath)
	{
		return infoFactory.createGridPathInfo(gridPath);
	}

	/**
	 * Creates a CacheDumpInfo object that contains this VM's dump file
	 * information.
	 * 
	 * @param dumpInfoList
	 *            List of top-level DumpInfo objects that represent the dump
	 *            files held by this VM.
	 */
	public CacheDumpInfo createCacheDumpInfo(List<DumpInfo> dumpInfoList)
	{
		return infoFactory.createCacheDumpInfo(dumpInfoList);
	}

	/**
	 * Creates a DumpInfo object that contains archived data file information.
	 * 
	 * @param parentFullPath
	 *            Grid full path (not file system path) that is to be parent to
	 *            the specified file.
	 * @param file
	 *            File to be examined.
	 * @param date
	 *            If non-null then this date is assigned, otherwise, the file
	 *            date is assigned.
	 * @param recursive
	 *            true to include all archived files in sub-directories.
	 */
	public DumpInfo createDumpInfo(String parentFullPath, File file, Date date, boolean recursive)
	{
		return infoFactory.createDumpInfo(parentFullPath, file, date, recursive);
	}
}
