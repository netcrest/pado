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

/**
 * MessageType specifies the types of grid messages that clients can listen on.
 * 
 * @author dpark
 * 
 */
public enum MessageType
{
	/**
	 * Announcement indicates that the message contains general information
	 * about the grid such as grid configuration changes, rebalancing, etc. that
	 * may be importance to clients.
	 */
	Announcement,

	/**
	 * Alert indicates that the message contains grid health deterioration
	 * information such as capacity overload, data corruption, etc.
	 */
	Alert,

	/**
	 * GridStatus indicates that the message contains the grid management
	 * information such as server started, stopped, etc.
	 */
	GridStatus
}
