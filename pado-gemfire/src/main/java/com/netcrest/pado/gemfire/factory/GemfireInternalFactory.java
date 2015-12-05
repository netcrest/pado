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
package com.netcrest.pado.gemfire.factory;

import java.util.Properties;

import com.netcrest.pado.gemfire.GemfireGridService;
import com.netcrest.pado.internal.factory.InternalFactory;
import com.netcrest.pado.internal.impl.GridService;
import com.netcrest.pado.temporal.TemporalClientMetadata;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalClientMetadata;

public class GemfireInternalFactory extends InternalFactory
{	
	protected static GemfireInternalFactory internalFactory = new GemfireInternalFactory();
	
	private final static TemporalClientMetadata DEFAULT_TEMPORAL_CLIENT_METADATA = 
			new GemfireTemporalClientMetadata("com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalKey", 
				"com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalValue",
				"com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData",
				"com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalList"
				);

	public static GemfireInternalFactory getInternalFactory()
	{
		return internalFactory;
	}
	
	protected GemfireInternalFactory()
	{
	}
	
	@Override
	public GridService createGridService(String gridId, String appId, Properties credentials, Object token, String username, boolean isGridParent)
	{
		return new GemfireGridService(gridId, appId, credentials, token, username, null, isGridParent);
	}
	
	@Override
	public TemporalClientMetadata createTemporalClientMetadata(String temporalName, String temporalKeyClassName, String temporalValueClassName, String temporalDataClassName)
	{
		return new GemfireTemporalClientMetadata(temporalName, temporalKeyClassName, temporalValueClassName, temporalDataClassName);
	}
	
	@Override
	public TemporalClientMetadata getDefaultTemporalClientMetadata()
	{
		return DEFAULT_TEMPORAL_CLIENT_METADATA;
		
	}
}
