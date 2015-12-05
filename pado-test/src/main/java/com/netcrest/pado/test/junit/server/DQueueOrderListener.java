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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.gemstone.gemfire.addon.dq.DQueueEvent;
import com.gemstone.gemfire.addon.dq.DQueueEventBatch;
import com.gemstone.gemfire.addon.dq.DQueueListener;
import com.gemstone.gemfire.addon.dq.DQueueListenerException;
import com.gemstone.gemfire.addon.dq.internal.SerializedObject;

@SuppressWarnings({ "rawtypes", "unused" })
public class DQueueOrderListener implements DQueueListener
{
	@Override
	public void dispatch(DQueueEventBatch batch) throws DQueueListenerException
	{
		printEvents(batch);
	}

	@Override
	public void dispatchFailed(DQueueEventBatch batch)
	{
		printEvents(batch);
	}
	
	private void printEvents(DQueueEventBatch batch)
	{
		int inum = 0;
		for (Iterator i = batch.getEvents().entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			String type = (String) entry.getKey();
			List eventsByType = (List) entry.getValue();
			
			inum++;
			int jnum = 0;
			for (Iterator j = eventsByType.iterator(); j.hasNext();) {
				DQueueEvent event = (DQueueEvent) j.next();
				try {
					System.out.println(inum + "." + ++jnum + ". " + ((SerializedObject)event.getValue()).getDeserializedObject());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if (event.getPossibleDuplicate()) {
					System.out.println("DQueueListener processing possible duplicate: " + event.getSequenceNumber());
				}
			}
		}
		System.out.println();
	}
}
