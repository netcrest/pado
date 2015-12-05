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
package com.netcrest.pado.info;

/**
 * GatewayQueueAttributeInfo provides gateway configuration information. This
 * class applies to data grid products that support the WAN topology.
 * 
 * @author dpark
 * 
 */
public abstract class GatewayQueueAttributesInfo
{
	/**
	 * Gateway alert threshold
	 */
	protected int alertThreshold;
	
	/**
	 * Gateway batch conflation flag
	 */
	protected boolean batchConflation;
	
	/**
	 * Gateway queue batch size
	 */
	protected int batchSize;
	
	/**
	 * Gateway queue batch delivery time interval
	 */
	protected int batchTimeInterval;
	
	/**
	 * Disk store name
	 */
	protected String diskStoreName;
	
	/**
	 * Gateway persistence flag
	 */
	protected boolean persistenceEanbled;
	
	/**
	 * Maximum memory of gateway queue before spilling over to the disk
	 */
	protected int maximumQueueMemory;

	/**
	 * Constructs an empty GatewayQueueAttributesInfo object.
	 */
	public GatewayQueueAttributesInfo()
	{
	}

	/**
	 * Returns the alert threshold. Upon reaching this threshold, the underlying
	 * data grid sends an alert notification message.
	 */
	public int getAlertThreshold()
	{
		return alertThreshold;
	}

	/**
	 * Sets the alert threshold.
	 * 
	 * @param alertThreshold
	 *            Alert threshold value
	 */
	public void setAlertThreshold(int alertThreshold)
	{
		this.alertThreshold = alertThreshold;
	}

	/**
	 * Returns true if event batches are conflated.
	 */
	public boolean isBatchConflation()
	{
		return batchConflation;
	}

	/**
	 * Sets batch conflation. If true, the underlying data grid discards
	 * duplicate key-based events and sends only the lastest key-based update
	 * events.
	 * 
	 * @param batchConflation
	 *            true to conflate, false to send all
	 */
	public void setBatchConflation(boolean batchConflation)
	{
		this.batchConflation = batchConflation;
	}

	/**
	 * Returns the batch size.
	 */
	public int getBatchSize()
	{
		return batchSize;
	}

	/**
	 * Sets the batch size.
	 * 
	 * @param batchSize
	 *            Batch size
	 */
	public void setBatchSize(int batchSize)
	{
		this.batchSize = batchSize;
	}

	/**
	 * Returns the batch time interval in milliseconds. The underlying data grid
	 * accumulates the data events in the gateway queue during the batch time
	 * interval. If the queue fills up to the batch size or more then it sends a
	 * batch immediately. Otherwise, it waits till when the batch time interval
	 * ends before sending a batch.
	 */
	public int getBatchTimeInterval()
	{
		return batchTimeInterval;
	}

	/**
	 * Sets the batch time interval in milliseconds.
	 * 
	 * @param batchTimeInterval
	 *            Batch time interval in milliseconds.
	 */
	public void setBatchTimeInterval(int batchTimeInterval)
	{
		this.batchTimeInterval = batchTimeInterval;
	}

	/**
	 * Returns the disk store name, which is an alias to the disk store that has
	 * been configured by the underlying data grid product.
	 */
	public String getDiskStoreName()
	{
		return diskStoreName;
	}

	/**
	 * Sets the disk store name.
	 * 
	 * @param diskStoreName
	 *            Disk store name
	 */
	public void setDiskStoreName(String diskStoreName)
	{
		this.diskStoreName = diskStoreName;
	}

	/**
	 * Returns true if queue persistence is enabled. If queue persistence is
	 * enabled then the queue is stored in the disk store specified by the disk
	 * store name. Persistent queue guarantees no data loss at the expense of
	 * higher latency. Note that if persistence is not enabled, the underlying
	 * data grid product typically offers in-memory HA to provide a guaranteed
	 * message delivery service as long as at least one gateway is running.
	 * 
	 */
	public boolean isPersistenceEnabled()
	{
		return persistenceEanbled;
	}

	/**
	 * Enables or disables queue persistence.
	 * 
	 * @param persistenceEanbled
	 */
	public void setPersistenceEnabled(boolean persistenceEanbled)
	{
		this.persistenceEanbled = persistenceEanbled;
	}

	/**
	 * Returns the maximum memory size of the queue in MB. When the maximum reaches
	 * the underlying data grid overflows the queue to the file system. 
	 */
	public int getMaximumQueueMemory()
	{
		return maximumQueueMemory;
	}

	/**
	 * Sets the maximum memory size of the queue in MB. When the maximum reaches
	 * the underlying data grid overflows the queue to the file system. 
	 * @param maximumQueueMemory Maximu memory size in MB.
	 */
	public void setMaximumQueueMemory(int maximumQueueMemory)
	{
		this.maximumQueueMemory = maximumQueueMemory;
	}
}
