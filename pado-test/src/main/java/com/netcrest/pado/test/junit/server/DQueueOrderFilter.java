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
package com.netcrest.pado.test.junit.server;

import com.gemstone.gemfire.addon.dq.DQueueEvent;
import com.gemstone.gemfire.addon.dq.DQueueFilter;

public class DQueueOrderFilter implements DQueueFilter
{

	@Override
	public int getQueue(DQueueEvent event)
	{
		return 0;
	}

	@Override
	public int getTotalQueues()
	{
		return 1;
	}

}
