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
package com.netcrest.pado.test.biz;

import java.util.List;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.data.jsonlite.JsonLite;

@BizClass
public interface IStressTestBiz extends IBiz
{
	@SuppressWarnings("rawtypes")
	@BizMethod
	@OnServer(broadcast = true)
	List<String> __start(JsonLite request);

	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.BooleanAndCollector")
	boolean isComplete();

	@BizMethod
	@OnServer(broadcast = true)
	List<String> getStatus();

	/**
	 * Starts stress tests. Only one instance of stress test at any given time
	 * is allowed. If the stress test is already in progress then this call is
	 * aborted. See returned status messages.
	 * 
	 * @param request
	 *            Stress test request that contains test configuration info
	 *            typically obtained from JSON file such as
	 *            etc/client/StressTest.json.
	 */
	@SuppressWarnings("rawtypes")
	List<String> start(JsonLite request);
}
