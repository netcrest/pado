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
package com.netcrest.pado.demo.bank.market;

import com.netcrest.pado.EntryEvent;
import com.netcrest.pado.IEntryListener;
import com.netcrest.pado.demo.bank.market.data.Level2Data;
import com.netcrest.pado.demo.bank.market.data.OrderInfo;

/**
 * ConsumerEntryListenerImpl is a stub listener that traps all updates
 * to the cache. 
 * 
 * @author dpark
 *
 */
public class ConsumerEntryListenerImpl implements IEntryListener
{

	public ConsumerEntryListenerImpl()
	{
	}

	/**
	 * Prints the specified message.
	 * @param message
	 */
	public void printMessage(Object message)
	{
		if (message instanceof OrderInfo) {
			OrderInfo orderInfo = (OrderInfo) message;
			System.out.println(orderInfo);
		} else if (message instanceof Level2Data) {
			Level2Data level2Data = (Level2Data)message;
			System.out.println(level2Data.getSeqNum() + ". " + level2Data.toString());
		} else {
			System.out.println(message);
		}
	}


	@Override
	public void onCreate(EntryEvent event)
	{
		printMessage(event.getValue());
	}


	@Override
	public void onUpdate(EntryEvent event)
	{
		printMessage(event.getValue());
	}


	@Override
	public void onRemove(EntryEvent event)
	{
		printMessage(event.getValue());
	}


	@Override
	public void onInvalidate(EntryEvent event)
	{
		printMessage(event.getValue());
	}

}
