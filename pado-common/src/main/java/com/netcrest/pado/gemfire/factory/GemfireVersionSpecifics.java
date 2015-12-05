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

import com.gemstone.gemfire.internal.HeapDataOutputStream;
import com.netcrest.pado.gemfire.port.GemfireVersionDetail;
import com.netcrest.pado.log.Logger;

public abstract class GemfireVersionSpecifics
{	
	private static GemfireVersionSpecifics gemfireVersionSpecifics;
	
	static {
		GemfireVersionDetail version = new GemfireVersionDetail();
		int major = version.getMajor();
		if (major <= 7) {
			major = 7;
		} else {
			major = 8;
		}
		String factoryClassName = "com.netcrest.pado.gemfire.port.v" + major + ".GemfireInternalFactory_v" + major;
		try {
			Class<?> clazz = Class.forName(factoryClassName);
			gemfireVersionSpecifics = (GemfireVersionSpecifics) clazz.newInstance();
		} catch (Exception e) {
			Logger.severe(e);
		}
	}

	public static GemfireVersionSpecifics getGemfireVersionSpecifics()
	{
		return gemfireVersionSpecifics;
	}
	
	protected GemfireVersionSpecifics()
	{
	}
	
	public abstract HeapDataOutputStream createHeapDataOutpuStream();
}
