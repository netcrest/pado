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

import java.util.Collection;

import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.info.message.MessageType;
import com.netcrest.pado.log.Logger;

public class MessageCacheListenerImpl extends CacheListenerAdapter
{
	private void update(EntryEvent event)
	{
		int ordinal = (Integer)event.getKey();
		// allow only if the ordinal value is in the range
		if (0 <= ordinal && ordinal < MessageType.values().length) {
			MessageType messageType = MessageType.values()[ordinal];
			Object message = event.getNewValue();
//			PadoClientManager scm = PadoClientManager.getPadoClientManager();
			Collection<IPado> collection = Pado.getAllPados();
			for (IPado pado : collection) {
				((Pado)pado).fireMessageEvent(messageType, message);
			}
			Logger.info("Message received: [messageType=" + messageType + "].");
		}
		Logger.info("Message received: [ordinal=" + ordinal + "].");
	}

	public void afterCreate(EntryEvent event)
	{
		update(event);
	}

	public void afterUpdate(EntryEvent event)
	{
		update(event);
	}
}
