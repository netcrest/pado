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
package com.netcrest.pado.gemfire.port.v8;

import com.gemstone.gemfire.internal.HeapDataOutputStream;
import com.gemstone.gemfire.internal.Version;
import com.netcrest.pado.gemfire.factory.GemfireVersionSpecifics;

public class GemfireInternalFactory_v8 extends GemfireVersionSpecifics
{
	public GemfireInternalFactory_v8()
	{
		super();
	}

	@Override
	public HeapDataOutputStream createHeapDataOutpuStream()
	{
		return new HeapDataOutputStream(Version.CURRENT);
	}
}
