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
package com.netcrest.pado.gemfire;

import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.netcrest.pado.info.LoginInfo;
import com.netcrest.pado.server.PadoServerManager;

/**
 * LoginServerCacheListenerImpl cleans up the user session resources upon
 * receiving the entry destroy event by removing the user session from
 * {@link PadoServerManager}.
 * 
 * @author dpark
 * 
 */
public class LoginServerCacheListenerImpl extends CacheListenerAdapter<Object, LoginInfo>
{
	@Override
	public void afterDestroy(EntryEvent<Object, LoginInfo> event)
	{
		Object token = event.getKey();
		PadoServerManager.getPadoServerManager().removeUserSession(token);
	}
}