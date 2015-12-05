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
package com.netcrest.pado.demo.bank.market.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.netcrest.pado.internal.util.QueueDispatcher;
import com.netcrest.pado.internal.util.QueueDispatcherListener;

public class Level2Publisher
{
	protected BufferedReader level2Reader;
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	protected QueueDispatcher queueDispatcher = new QueueDispatcher();
	protected int lineSkipCount;
	protected long delayBetweenLines = 0;
	
	public Level2Publisher() throws IOException
	{
		init();
	}
	
	private void init() throws IOException
	{
		String level2FilePath = System.getProperty("level2File", "data/demo/level2_060806.csv");
		File level2File= new File(System.getProperty("user.dir"), level2FilePath);
		FileReader reader = new FileReader(level2File);
		level2Reader = new BufferedReader(reader);
		
		lineSkipCount = Integer.getInteger("lineSkipCount", 0).intValue();
		delayBetweenLines = Integer.getInteger("delayBetweenLines", 0).intValue();
		System.out.println();
		System.out.println("       lineSkipCount = " + lineSkipCount);
		System.out.println("   delayBetweenLines = " + delayBetweenLines + " msec");
		System.out.println();
		queueDispatcher.start();
	}
	
	/**
	 * Symbol,Date,Time,Source Date,Source Time,Bid,BidSize,Ask,AskSize,MMID,QuoteCondition,Source
	 *   0     1   2      3           4           5  6       7    8      9    10             11
	 * @throws IOException
	 */
	protected void run() throws Exception
	{
		if (queueDispatcher.getQueueDispatcherListener() == null) {
			throw new Exception("Unabled to dispatch Level2 data. No listener registered.");
		}
		
		// ignore the first line
		String line = level2Reader.readLine();
		line = level2Reader.readLine();
		String split[];

		Level2Data level2Data = new Level2Data();
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
			level2Data.setSeqNum(++seqNum);
			level2Data.setId(seqNum + "");
			level2Data.setSymbol(split[0]);
			level2Data.setDate(dateFormat.parse(split[3] + " " + split[4]));
			level2Data.setBid(Double.parseDouble(split[5]));
			level2Data.setBidSize(Double.parseDouble(split[6]));
			level2Data.setAsk(Double.parseDouble(split[7]));
			level2Data.setAskSize(Double.parseDouble(split[8]));
			level2Data.setMmid(split[9]);
			level2Data.setQuoteConditions(Integer.parseInt(split[10]));
			level2Data.setSource(split[11]);
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
