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
package com.netcrest.pado.biz.impl.gemfire;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ExecutorServiceThreadPool
{
	private static Map<Thread, ExecutorService> map = new HashMap<Thread, ExecutorService>();
	
	public static ExecutorService getExecutorService()
	{
		ExecutorService es = map.get(Thread.currentThread());
		if (es == null) {
			es = Executors.newCachedThreadPool(new ThreadFactory() {
	            public Thread newThread(Runnable r) {
	                Thread t = new Thread(r, "Pado-ExecutorServiceCached");
	                t.setDaemon(true);
	                return t;
	            }
	        });
			map.put(Thread.currentThread(), es);
		}
		return es;
	}
}
