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
package com.netcrest.pado;

import com.netcrest.pado.info.message.MessageType;

/**
 * IMessageListener traps system-level messages published by Pado grids to an
 * IPado instance. It can be registered via
 * {@link Pado#addMessageListener(IMessageListener)}.
 * 
 * @author dpark
 * 
 */
public interface IMessageListener
{
	/**
	 * Receives system-level messages, i.e., alerts, announcements, and status,
	 * published by Pado grids.
	 * 
	 * @param messageType
	 *            Message type.
	 * @param message Message
	 */
	void messageReceived(MessageType messageType, Object message);
}
