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

import java.io.IOException;
import java.text.SimpleDateFormat;

import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.demo.bank.market.data.Level2KeyType;
import com.netcrest.pado.demo.bank.market.data.Level2Publisher;
import com.netcrest.pado.internal.util.QueueDispatcherListener;

public class Level2KeyMapPublisher extends Level2Publisher
{
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	public Level2KeyMapPublisher() throws IOException
	{
		super();
	}

	/**
	 * Symbol,Date,Time,Source Date,Source
	 * Time,Bid,BidSize,Ask,AskSize,MMID,QuoteCondition,Source 0 1 2 3 4 5 6 7 8
	 * 9 10 11
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void run() throws Exception
	{
		if (queueDispatcher.getQueueDispatcherListener() == null) {
			throw new Exception("Unabled to dispatch Level2 data. No listener registered.");
		}

		// ignore the first line
		String line = level2Reader.readLine();
		line = level2Reader.readLine();
		String split[];

		KeyMap level2Data = new JsonLite(Level2KeyType.getKeyType());
		
		int linesSkipped;
		int seqNum = 0;
		while (line != null) {
			linesSkipped = 0;
			while (linesSkipped <= lineSkipCount) {
				line = level2Reader.readLine();
				if (line == null) {
					return;
				}
				linesSkipped++;
			}
			split = line.split(",");
			level2Data.put(Level2KeyType.KSeqNum, ++seqNum);
			level2Data.put(Level2KeyType.KId, seqNum + "");
			level2Data.put(Level2KeyType.KSymbol, split[0]);
			level2Data.put(Level2KeyType.KDate, dateFormat.parse(split[3] + " " + split[4]));
			level2Data.put(Level2KeyType.KBid, Double.parseDouble(split[5]));
			level2Data.put(Level2KeyType.KBidSize, Double.parseDouble(split[6]));
			level2Data.put(Level2KeyType.KAsk, Double.parseDouble(split[7]));
			level2Data.put(Level2KeyType.KAskSize, Double.parseDouble(split[8]));
			level2Data.put(Level2KeyType.KMmid, split[9]);
			level2Data.put(Level2KeyType.KQuoteConditions, Integer.parseInt(split[10]));
			level2Data.put(Level2KeyType.KSource, split[11]);
			queueDispatcher.enqueue(level2Data);
			Thread.sleep(delayBetweenLines);
		}
	}

	public void start() throws Exception
	{
		run();
	}

	public void setQueueDispatchListener(QueueDispatcherListener listener)
	{
		queueDispatcher.setQueueDispatcherListener(listener);
	}

	public QueueDispatcherListener getQueueDispatchListener()
	{
		return queueDispatcher.getQueueDispatcherListener();
	}
}
