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
package com.netcrest.pado.link;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.BizType;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.info.ConfigInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.LoginInfo;
import com.netcrest.pado.info.PathInfo;

/**
 * IPadoBizLink is an internal IBiz class for creating login sessions and providing
 * metadata services. This class is not allowed outside of internal use.
 * This is a class loader link class that links the main class loader to
 * an IBiz class loader.
 * @author dpark
 * 
 */
@BizClass(bizType = BizType.PADO, name="IPadoBiz")
public interface IPadoBizLink extends IBiz
{
	/**
	 * Logs in the specified user to the specified app.
	 * 
	 * @param appId
	 *            App ID
	 * @param username
	 *            User name
	 * @param domain
	 *            Optional domain name
	 * @param password
	 *            Password
	 * @return Upon successful login, returns user login info.
	 * @throws PadoLoginException
	 *             Thrown if login fails
	 */
	@BizMethod(bizType = BizType.PADO)
	@OnServer(broadcast = false, connectionName = "pado")
	LoginInfo login(String appId, String domain, String username, char[] password) throws PadoLoginException;

	/**
	 * Returns ConfigInfo specific to the specified app ID.
	 * 
	 * @param appId
	 *            App ID
	 * @param multiAppsEnabled
	 *            If true then ConfigInfo includes XML configuration content
	 *            suited for a client hosting multiple applications (app Ids)
	 *            such as app servers. If false then ConfigInfo includes XML
	 *            configuration content for a client hosting a single
	 *            application, i.e., a single app ID.
	 * @param includeCacheXmlContent
	 *            If true then includes XML configuration content. If false then
	 *            it is analogous to {@link #getAppInfo(String)}.
	 */
	@BizMethod
	@OnServer(broadcast = false, connectionName = "pado")
	ConfigInfo getConfigInfo(String appId, boolean multiAppsEnabled, boolean includeCacheXmlContent);

	/**
	 * Returns ConfigInfo without the XML configuration content, i.e.,
	 * {@link ConfigInfo#getXmlContent()} is always null.
	 * 
	 * @param appId
	 *            App ID
	 */
	@BizMethod
	@OnServer(broadcast = false, connectionName = "pado")
	ConfigInfo getConfigInfo(String appId);

	/**
	 * Returns GridInfo of this grid.
	 * 
	 * @return
	 */
	@BizMethod
	@OnServer(broadcast = false, connectionName = "pado")
	GridInfo getGridInfo();
	
	
	/**
	 * Returns the number of running servers in the specified grid.
	 * @param gridId Grid ID
	 */
	@BizMethod
	@OnServer
	int getServerCount(String gridId);
	
	/**
	 * Returns all server IDs that uniquely identify the servers in the specified grid.
	 * @param gridId Grid ID
	 */
	@BizMethod
	@OnServer
	Object[] getServerIds(String gridId);

	/**
	 * Returns AppInfo pertaining to the specified app ID.
	 * 
	 * @param appId
	 *            App ID
	 */
	@BizMethod
	@OnServer(broadcast = false, connectionName = "pado")
	AppInfo getAppInfo(String appId);

	/**
	 * Returns PathInfo of the root of the grid containing all of its children.
	 */
	@BizMethod
	@OnServer(broadcast = false, connectionName = "pado")
	PathInfo getPathInfo();

	/**
	 * Returns all registered KeyType main class names.
	 */
	@BizMethod
	@OnServer(broadcast = false, connectionName = "pado")
	String[] getRegisteredMainKeyTypeNames();

	/**
	 * Echos back the specified payload. This method is useful for determining
	 * the round trip time.
	 * 
	 * @param payload
	 *            Any byte array payload
	 * @return Returns the same byte array payload that was passed in as the
	 *         input argument.
	 */
	@BizMethod
	@OnServer
	byte[] echo(byte[] payload);
}
