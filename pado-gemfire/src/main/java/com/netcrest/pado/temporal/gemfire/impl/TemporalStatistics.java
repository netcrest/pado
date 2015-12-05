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
package com.netcrest.pado.temporal.gemfire.impl;

import com.gemstone.gemfire.StatisticDescriptor;
import com.gemstone.gemfire.Statistics;
import com.gemstone.gemfire.StatisticsFactory;
import com.gemstone.gemfire.StatisticsType;
import com.gemstone.gemfire.StatisticsTypeFactory;
import com.gemstone.gemfire.distributed.internal.DistributionStats;
import com.gemstone.gemfire.internal.StatisticsTypeFactoryImpl;

public class TemporalStatistics
{

	public static final String typeName = "TemporalStatistics";

	private static final StatisticsType type;

	private static final String PUT_IN_PROGRESS_COUNT = "putInProgressCount";
	private static final String PUT_COUNT = "putCount";
	private static final String PUT_TIME = "putTime";
	
	private static final String REMOVE_IN_PROGRESS_COUNT = "removeInProgressCount";
	private static final String REMOVE_COUNT = "removeCount";
	private static final String REMOVE_TIME = "removeTime";
	
	private static final String AS_OF_ENTITY_SEARCH_IN_PROGRESS_COUNT = "asOfEntitySearchInProgressCount";
	private static final String AS_OF_ENTITY_SEARCH_COUNT = "asOfEntitySearchCount";
	private static final String AS_OF_ENTITY_SEARCH_TIME = "asOfEntitySearchTime";
	
	private static final String AS_OF_ENTITY_ATTACHMENTS_SEARCH_IN_PROGRESS_COUNT = "asOfEntityAttachmentsSearchInProgressCount";
	private static final String AS_OF_ENTITY_ATTACHMENTS_SEARCH_COUNT = "asOfEntityAttachmentsSearchCount";
	private static final String AS_OF_ENTITY_ATTACHMENTS_SEARCH_TIME = "asOfEntityAttachmentsSearchTime";
	

	private static final int putInProgressCountId;
	private static final int putCountId;
	private static final int putTimeId;
	private static final int removeInProgressCountId;
	private static final int removeCountId;
	private static final int removeTimeId;
	private static final int asOfEntitySearchInProgressCountId;
	private static final int asOfEntitySearchCountId;
	private static final int asOfEntitySearchTimeId;
	private static final int asOfEntityAttachmentsSearchInProgressCountId;
	private static final int asOfEntityAttachmentsSearchCountId;
	private static final int asOfEntityAttachmentsSearchTimeId;

	static {
		// Initialize type
		StatisticsTypeFactory f = StatisticsTypeFactoryImpl.singleton();
		type = f.createType(
				typeName,
				typeName,
				new StatisticDescriptor[] {
						f.createIntGauge(PUT_IN_PROGRESS_COUNT, "The number of put calls currently in progress",
								"operations"),
						f.createIntCounter(PUT_COUNT, "The number of put calls made by client since system start",
								"operations"),
						f.createLongCounter(PUT_TIME, "Total time spent executing temporal put", "nanoseconds"),
						
						f.createIntGauge(REMOVE_IN_PROGRESS_COUNT, "The number of remove calls currently in progress",
								"operations"),
						f.createIntCounter(REMOVE_COUNT, "The number of remove calls made by client since system start",
								"operations"),
						f.createLongCounter(REMOVE_TIME, "Total time spent removing temporal entity", "nanoseconds"),
						
						f.createIntGauge(AS_OF_ENTITY_SEARCH_IN_PROGRESS_COUNT, "The number of as-of entity search calls currently in progress",
								"operations"),
						f.createIntCounter(AS_OF_ENTITY_SEARCH_COUNT, "The number of as-of entity searches made by client since system start",
								"operations"),			
						f.createLongCounter(AS_OF_ENTITY_SEARCH_TIME,
								"Total time spent searching temporal entities", "nanoseconds"),
								
						f.createIntGauge(AS_OF_ENTITY_ATTACHMENTS_SEARCH_IN_PROGRESS_COUNT, "The number of as-of entity attachment search calls currently in progress",
								"operations"),		
						f.createIntCounter(AS_OF_ENTITY_ATTACHMENTS_SEARCH_COUNT, "The number of as-of entity searches made by client since system start",
								"operations"),
						f.createLongCounter(AS_OF_ENTITY_ATTACHMENTS_SEARCH_TIME,
								"Total time spent searching temporal entities with attachments", "nanoseconds"),
				});

		// Initialize id fields
		putInProgressCountId = type.nameToId(PUT_IN_PROGRESS_COUNT);
		putCountId = type.nameToId(PUT_COUNT);
		putTimeId = type.nameToId(PUT_TIME);
		removeInProgressCountId = type.nameToId(REMOVE_IN_PROGRESS_COUNT);
		removeCountId = type.nameToId(REMOVE_COUNT);
		removeTimeId = type.nameToId(REMOVE_TIME);
		asOfEntitySearchInProgressCountId = type.nameToId(AS_OF_ENTITY_SEARCH_IN_PROGRESS_COUNT);
		asOfEntitySearchCountId = type.nameToId(AS_OF_ENTITY_SEARCH_COUNT);
		asOfEntitySearchTimeId = type.nameToId(AS_OF_ENTITY_SEARCH_TIME);
		asOfEntityAttachmentsSearchInProgressCountId = type.nameToId(AS_OF_ENTITY_ATTACHMENTS_SEARCH_IN_PROGRESS_COUNT);
		asOfEntityAttachmentsSearchCountId = type.nameToId(AS_OF_ENTITY_ATTACHMENTS_SEARCH_COUNT);
		asOfEntityAttachmentsSearchTimeId = type.nameToId(AS_OF_ENTITY_ATTACHMENTS_SEARCH_TIME);
	}

	private final Statistics stats;

	public TemporalStatistics(StatisticsFactory f, String temporalId)
	{
		this.stats = f.createAtomicStatistics(type, "temporalStatistics-" + temporalId);
	}

	public void close()
	{
		this.stats.close();
	}

	public int getPutCount()
	{
		return this.stats.getInt(putCountId);
	}
	
	public long getPutTime()
	{
		return this.stats.getLong(putTimeId);
	}

	public int getRemoveCount()
	{
		return this.stats.getInt(removeCountId);
	}
	
	public long getRemoveTime()
	{
		return this.stats.getLong(putTimeId);
	}
	
	public int getAsOfSearchCount()
	{
		return this.stats.getInt(asOfEntitySearchCountId);
	}
	
	public long getAsOfSearchTime()
	{
		return this.stats.getLong(asOfEntitySearchTimeId);
	}

	public long startPutCount()
	{
		stats.incInt(putInProgressCountId, 1);
		return getTime();
	}

	public void endPutCount(long start)
	{
		long end = getTime();

		// Increment number of objects offered
		this.stats.incInt(putCountId, 1);

		// Increment object offer time
		this.stats.incLong(putTimeId, end - start);
		
		// Decrement the number of put calls in progress
	    this.stats.incInt(putInProgressCountId, -1);
	}
	
	public long startRemoveCount()
	{
		stats.incInt(removeInProgressCountId, 1);
		return getTime();
	}

	public void endRemoveCount(long start)
	{
		long end = getTime();

		// Increment number of remove calls made
		this.stats.incInt(removeCountId, 1);

		// Increment remove time
		this.stats.incLong(removeTimeId, end - start);
		
		// Decrement the number of remove calls in progress
	    this.stats.incInt(removeInProgressCountId, -1);
	}

	public long startAsOfEntitySearchCount()
	{
		stats.incInt(asOfEntitySearchInProgressCountId, 1);
		return getTime();
	}

	public void endAsOfEntitySearchCount(long start)
	{
		long end = getTime();

		// Increment number of objects offered
		this.stats.incInt(asOfEntitySearchCountId, 1);

		// Increment as-of search time
		this.stats.incLong(asOfEntitySearchTimeId, end - start);
		
		// Decrement the number of as-of search calls in progress
	    this.stats.incInt(asOfEntitySearchInProgressCountId, -1);
	}
	
	public long startAsOfEntityAttachmentsSearchCount()
	{
		stats.incInt(asOfEntityAttachmentsSearchInProgressCountId, 1);
		return getTime();
	}

	public void endAsOfEntityAttachmentsSearchCount(long start)
	{
		long end = getTime();

		// Increment number of objects offered
		this.stats.incInt(asOfEntityAttachmentsSearchCountId, 1);

		// Increment as-of search time
		this.stats.incLong(asOfEntityAttachmentsSearchTimeId, end - start);
		
		// Decrement the number of as-of search attachments calls in progress
	    this.stats.incInt(asOfEntityAttachmentsSearchInProgressCountId, -1);
	}

	protected long getTime()
	{
		return DistributionStats.getStatTime();
	}
}
