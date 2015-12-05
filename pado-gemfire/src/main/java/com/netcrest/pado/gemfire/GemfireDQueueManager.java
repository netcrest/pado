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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.addon.dq.DQueueAttributes;
import com.gemstone.gemfire.addon.dq.DQueueDispatcher;
import com.gemstone.gemfire.addon.dq.DQueueException;
import com.gemstone.gemfire.addon.dq.DQueueFactory;
import com.gemstone.gemfire.addon.dq.DQueueFilter;
import com.gemstone.gemfire.addon.dq.DQueueListener;
import com.gemstone.gemfire.addon.dq.internal.DQueuePushDispatcherImpl;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;

public class GemfireDQueueManager
{
	private static GemfireDQueueManager dqueueManager = new GemfireDQueueManager();

	private HashMap<String, DQueueDispatcher> dqueueDispatcherMap = new HashMap<String, DQueueDispatcher>(10);

	public static GemfireDQueueManager getDQueueManager()
	{
		return dqueueManager;
	}

	static void initialize()
	{
		ClassLoader classLoader = PadoServerManager.getPadoServerManager().getAppBizClassLoader();
		com.netcrest.pado.internal.config.dtd.generated.Pado padoConfig = PadoServerManager.getPadoServerManager()
				.getPadoConfig();
		synchronized (dqueueManager) {
			List<com.netcrest.pado.internal.config.dtd.generated.Dqueue> list = padoConfig.getGemfire().getDqueue();
			for (com.netcrest.pado.internal.config.dtd.generated.Dqueue dqueue : list) {
				String name = null;
				if (dqueue.getName() != null) {
					name = dqueue.getName();
				}
				if (name == null) {
					continue;
				}
				if (dqueueManager.dqueueDispatcherMap.containsKey(name)) {
					continue;
				}
				DQueueAttributes attr = new DQueueAttributes();
				if (dqueue.getBatchSize() != null) {
					int batchSize = Integer.parseInt(dqueue.getBatchSize());
					attr.setBatchSize(batchSize);
				}
				if (dqueue.getBatchTimeIntervalInMsec() != null) {
					int batchTimeIntervalInMsec = Integer.parseInt(dqueue.getBatchTimeIntervalInMsec());
					attr.setBatchTimeInterval(batchTimeIntervalInMsec);
				}
				if (dqueue.getColocatedWith() != null) {
					attr.setColocatedRegionFullPath(dqueue.getColocatedWith());
				}
				if (dqueue.getPreserveOrder() != null) {
					attr.setPreserveOrder(Boolean.parseBoolean(dqueue.getPreserveOrder()));
				}
				int totalNumBuckets = 113;
				if (dqueue.getTotalNumBuckets() != null) {
					totalNumBuckets = Integer.parseInt(dqueue.getTotalNumBuckets());
				}
				attr.setBuckets(totalNumBuckets);

				// Defer DQueueListener initialization to registerPlugins()
				// which gets invoked after all IBiz classes have been
				// fully initialized. This is required due to a initialization
				// order conflict.
				
				if (dqueue.getFilter() != null) {
					com.netcrest.pado.internal.config.dtd.generated.Bean bean = dqueue.getFilter().getBean();
					if (bean != null) {
						String filter = bean.getClassName();
						try {
							Class<?> clazz = classLoader.loadClass(filter);
							DQueueFilter dqueueFilter = (DQueueFilter) clazz.newInstance();
							attr.setFilter(dqueueFilter);
						} catch (Exception e) {
							Logger.severe(e);
							continue;
						}
					}
				} else {
					Logger.config("DQueue " + name
							+ " has no filter defined. The default filter, SimpleDQueueFilter, has been assigned.");
					attr.setFilter(new SimpleDQueueFilter());
				}

				attr.setStartDispatcher(true);
				String type = dqueue.getType();
				DQueueDispatcher dispatcher;
				if (type != null && type.equalsIgnoreCase("server")) {
					dispatcher = DQueueFactory.createDQueuePushDispatcher(name, attr);
				} else {
					dispatcher = DQueueFactory.createDQueuePullDispatcher(name, attr);
				}
				dqueueManager.dqueueDispatcherMap.put(name, dispatcher);
			}

			Set<Map.Entry<String, DQueueDispatcher>> set = dqueueManager.dqueueDispatcherMap.entrySet();
			if (set.size() > 0) {
				StringBuffer buffer = new StringBuffer(200);
				buffer.append("DQueue(s) registered: [");
				int i = 0;
				for (Map.Entry<String, DQueueDispatcher> entry : set) {
					if (i > 0) {
						buffer.append(" ");
					}
					buffer.append(entry.getKey());
					i++;
				}
				buffer.append("]");
				Logger.config(buffer.toString());
			} else {
				Logger.config("No DQueues configured.");
			}
		}
	}

	/**
	 * Registers DQueueListner and DQueueFilter. Plugin registration is
	 * deferred due to a initialization order conflict.
	 */
	static void registerPlugins()
	{
		com.netcrest.pado.internal.config.dtd.generated.Pado padoConfig = PadoServerManager.getPadoServerManager()
				.getPadoConfig();
		synchronized (dqueueManager) {
			ClassLoader classLoader = PadoServerManager.getPadoServerManager().getAppBizClassLoader();
			List<com.netcrest.pado.internal.config.dtd.generated.Dqueue> list = padoConfig.getGemfire().getDqueue();
			for (com.netcrest.pado.internal.config.dtd.generated.Dqueue dqueue : list) {
				String name = null;
				if (dqueue.getName() != null) {
					name = dqueue.getName();
				}
				if (name == null) {
					continue;
				}
				DQueueDispatcher dispatcher = dqueueManager.getDispatcher(name);
				if (dispatcher == null) {
					continue;
				}
				if (dispatcher instanceof DQueuePushDispatcherImpl) {
					DQueuePushDispatcherImpl pushDispatcher = (DQueuePushDispatcherImpl) dispatcher;
					if (dqueue.getListener() != null) {
						com.netcrest.pado.internal.config.dtd.generated.Bean bean = dqueue.getListener().getBean();
						if (bean != null) {
							try {
								String listener = bean.getClassName();
								classLoader = PadoServerManager.getPadoServerManager().getAppBizClassLoader();
								Class<?> clazz = classLoader.loadClass(listener);
								DQueueListener dqueueListener = (DQueueListener) clazz.newInstance();
								pushDispatcher.__setListener(dqueueListener);
							} catch (Exception e) {
								Logger.error(e);
								continue;
							}
						}
					} else {
						throw new DQueueException("DQueueListener undefined for DQueue " + name
									+ ". DQueue not created.");
					}
				}
			}
		}
	}

	public DQueueDispatcher getDispatcher(String name)
	{
		return dqueueDispatcherMap.get(name);
	}

	/**
	 * Not supported at this time.
	 * 
	 * @param name
	 */
	public void closeDispatcher(String name)
	{
		// DQueueDispatcher dispatcher = getDispatcher(name);
		// if (dispatcher == null) {
		// return;
		// }
		// dispatcher.close();
	}

	/**
	 * Not supported at this time.
	 * 
	 * @param name
	 * @return
	 */
	public DQueueDispatcher removeDispatcher(String name)
	{
		// DQueueDispatcher dispatcher = dqueueDispatcherMap.remove(name);
		// dispatcher.close();
		// return dispatcher;
		return null;
	}

}
