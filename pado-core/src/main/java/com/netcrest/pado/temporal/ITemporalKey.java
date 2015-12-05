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
package com.netcrest.pado.temporal;

import java.io.Serializable;

/**
 * ITemporalKey is the temporal interface that holds the following temporal
 * information:
 * <p>
 * <ul>
 * <li><b>Identity Key</b> - The primary key that identifies a temporal entity.</li>
 * <li><b>Start/End Valid Time</b> - A range of time (inclusive) in which the
 * entity valid. This range is compared with the valid-at time when searching
 * temporal entities.</li>
 * <li><b>Written Time</b> - The transaction or update time. This time is
 * compared with the as-of time when searching temporal entities.</li>
 * </ul>
 * 
 * @author dpark
 * 
 * @param <K>
 *            Identity key class
 */
public interface ITemporalKey<K> extends Serializable
{
	/**
	 * Returns the identity key.
	 */
	K getIdentityKey();
	
	/**
	 * Returns the written time in msec.
	 */
	long getWrittenTime();

	/**
	 * Returns the start valid time in msec.
	 */
	long getStartValidTime();

	/**
	 * Returns the end valid time in msec.
	 */
	long getEndValidTime();
	
	/**
	 * Returns the name of the user who created the temporal entry. It may
	 * return null if the user is undefined.
	 */
	String getUsername();
	
	/**
	 * Returns a string representation in date formatted form of the temporal key.
	 */
	String toStringDate();
}
