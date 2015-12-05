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
 * GatewayInfo provides information of a gateway that sends data to a gateway
 * hub in another grid. A gateway belongs to a gateway hub and provides a
 * queue-based store-and-forward service to select data grid events. This class
 * applies to data grid products that support the WAN topology.
 * 
 * @author dpark
 * 
 */
public abstract class GatewayInfo
{
	/**
	 * Gateway hub ID
	 */
	protected String gatewayHubId;
	
	/**
	 * Gateway ID
	 */
	protected String id;
	
	/**
	 * Gateway queue attribute info
	 */
	protected GatewayQueueAttributesInfo queueAttributesInfo;
	
	/**
	 * Gateway queue size
	 */
	protected int queueSize;

	/**
	 * Constructs an empty GatewayInfo object.
	 */
	public GatewayInfo()
	{
	}

	/**
	 * Returns this gateway's parent ID, the gateway hub ID.
	 */
	public String getGatewayHubId()
	{
		return gatewayHubId;
	}

	/**
	 * Sets the gateway hub ID.
	 * 
	 * @param gatewayHubId
	 *            Gateway hub ID
	 */
	public void setGatewayHubId(String gatewayHubId)
	{
		this.gatewayHubId = gatewayHubId;
	}

	/**
	 * Returns this gateway's ID.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Sets this gateway's ID.
	 * 
	 * @param id
	 *            Gateway ID
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * Returns the QueueAttributeInfo object which contains the gateway's queue
	 * setting information.
	 */
	public GatewayQueueAttributesInfo getQueueAttributesInfo()
	{
		return queueAttributesInfo;
	}

	/**
	 * Sets the QueueAttributeInfo object.
	 * 
	 * @param queueAttributesInfo
	 *            Queue attribute information
	 */
	public void setQueueAttributesInfo(GatewayQueueAttributesInfo queueAttributesInfo)
	{
		this.queueAttributesInfo = queueAttributesInfo;
	}

	/**
	 * Returns the gateway queue size.
	 */
	public int getQueueSize()
	{
		return queueSize;
	}

	/**
	 * Sets the gateway queue size.
	 * 
	 * @param queueSize
	 *            Gateway queue size
	 */
	public void setQueueSize(int queueSize)
	{
		this.queueSize = queueSize;
	}
}
