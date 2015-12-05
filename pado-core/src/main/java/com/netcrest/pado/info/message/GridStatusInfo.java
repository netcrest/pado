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
package com.netcrest.pado.info.message;

import java.util.List;

import com.netcrest.pado.Pado;
import com.netcrest.pado.info.ServerInfo;

/**
 * GridStatusInfo contains the current grid status published by a grid in real
 * time. Clients can listen on grid status via
 * {@link Pado#addMessageListener(com.netcrest.pado.IMessageListener)}.
 * 
 * @author dpark
 * 
 */
public abstract class GridStatusInfo
{
	/**
	 * Grid-level status types.
	 * 
	 * @author dpark
	 * 
	 */
	public enum Status
	{
		/**
		 * Grid may broadcast general announcements.
		 */
		GENERAL,
		/**
		 * A new server has joined the grid.
		 */
		SERVER_STARTED,
		/**
		 * A server has terminated.
		 */
		SERVER_TERMINATED,
		/**
		 * GRID_NOT_AVAILABLE indicates that the grid has no servers responding.
		 * This typically means the grid is down.
		 */
		GRID_NOT_AVAILABLE
	}

	/**
	 * Grid status.
	 */
	protected Status status = Status.GENERAL;

	/**
	 * ID of the grid that published the status.
	 */
	protected String gridId;

	/**
	 * The master server ID.
	 */
	protected Object masterId;

	/**
	 * ID of the server that published the status.
	 */
	protected Object serverId;

	/**
	 * Status message.
	 */
	protected String message;

	/**
	 * List of ServerInfo objects.
	 */
	protected List<ServerInfo> serverInfoList;

	/**
	 * Constructs an empty GridStatusInfo objects.
	 */
	public GridStatusInfo()
	{
	}

	/**
	 * Constructs a GridStatusInfo object.
	 * 
	 * @param status
	 *            Grid status
	 * @param gridId
	 *            ID of the grid that published this status
	 * @param masterId
	 *            Master server ID
	 * @param serverId
	 *            ID of the server that published this status
	 * @param message
	 *            Status message
	 */
	public GridStatusInfo(Status status, String gridId, String masterId, String serverId, String message)
	{
		this.status = status;
		this.gridId = gridId;
		this.masterId = masterId;
		this.serverId = serverId;
		this.message = message;
	}

	/**
	 * Returns the grid status.
	 */
	public Status getStatus()
	{
		return status;
	}

	/**
	 * Sets the grid status.
	 * 
	 * @param status
	 *            Grid status
	 */
	public void setStatus(Status status)
	{
		this.status = status;
	}

	/**
	 * Returns the ID of the grid that published this status.
	 */
	public String getGridId()
	{
		return gridId;
	}

	/**
	 * Sets the ID of the grid that is publishing this status.
	 * 
	 * @param gridId
	 */
	public void setGridId(String gridId)
	{
		this.gridId = gridId;
	}

	/**
	 * Returns the master server ID. The master server is the one that manages
	 * the entire grid. It returns null if Status is
	 * {@link Status#GRID_NOT_AVAILABLE}.
	 */
	public Object getMasterId()
	{
		return masterId;
	}

	/**
	 * Sets the master server ID.
	 * 
	 * @param masterId
	 *            Master server ID
	 */
	public void setMasterId(Object masterId)
	{
		this.masterId = masterId;
	}

	/**
	 * Returns the ID of the server that published this status. It returns null
	 * if Status is {@link Status#GRID_NOT_AVAILABLE}.
	 */
	public Object getServerId()
	{
		return serverId;
	}

	/**
	 * Sets the server ID.
	 * 
	 * @param serverId
	 *            Server ID
	 */
	public void setServerId(Object serverId)
	{
		this.serverId = serverId;
	}

	/**
	 * Returns the status message.
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * Sets the status message.
	 * @param message Status message
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}

	/**
	 * Returns the list of ServerInfo objects. This list represents all
	 * of the servers in the grid.
	 */
	public List<ServerInfo> getServerInfoList()
	{
		return serverInfoList;
	}

	/**
	 * Sets the list of ServerInfo objects.
	 * @param serverInfoList List of ServerInfo objects
	 */
	public void setServerInfoList(List<ServerInfo> serverInfoList)
	{
		this.serverInfoList = serverInfoList;
	}
}
