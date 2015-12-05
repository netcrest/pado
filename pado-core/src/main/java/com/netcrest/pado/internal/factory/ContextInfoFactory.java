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

import com.netcrest.pado.context.IDataInfo;
import com.netcrest.pado.context.IUserInfo;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

/**
 * ContextInfoFactory creates context objects.
 * 
 * @author dpark
 *
 */
public class ContextInfoFactory
{
	private static Class userInfoClass;
	private static Class dataInfoClass;
	
	static {
		try {
			userInfoClass = Class.forName(PadoUtil.getProperty(Constants.PROP_CLASS_USER_INFO, Constants.DEFAULT_CLASS_USER_INFO));
		} catch (ClassNotFoundException e) {
			Logger.error(e);
		}
		try {
			dataInfoClass = Class.forName(PadoUtil.getProperty(Constants.PROP_CLASS_DATA_INFO, Constants.DEFAULT_CLASS_DATA_INFO));
		} catch (ClassNotFoundException e) {
			Logger.error(e);
		}
	}
	
	/**
	 * Returns a new empty instance of IUserInfo object.
	 */
	public static IUserInfo createUserInfo()
	{
		try {
			return (IUserInfo)userInfoClass.newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}
	
	/**
	 * Returns a new empty instance of IDataInfo object.
	 */
	public static IDataInfo createDataInfo()
	{
		try {
			return (IDataInfo)dataInfoClass.newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}
}
