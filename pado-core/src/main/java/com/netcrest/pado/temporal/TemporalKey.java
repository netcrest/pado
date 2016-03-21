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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.netcrest.pado.IRoutingKey;

/**
 * TemporalKey is the composite key class that holds the following temporal
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
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class TemporalKey<K> implements ITemporalKey<K>, Comparable<TemporalKey>
{
	private static final long serialVersionUID = 1L;

	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");

	/**
	 * Identity key
	 */
	protected K identityKey;

	/**
	 * Start valid time in msec
	 */
	protected long startValidTime;

	/**
	 * End valid time in msec
	 */
	protected long endValidTime;

	/**
	 * (Start) Written time in msec
	 */
	protected long writtenTime;

	/**
	 * End written time in msec.
	 */
	protected long endWrittenTime;

	/**
	 * User name
	 */
	protected String username;

	/**
	 * Constructs an empty TemporalKey object.
	 */
	public TemporalKey()
	{
	}

	/**
	 * Constructs a new TemporalKey object with all of the required temporal
	 * information.
	 * 
	 * @param identityKey
	 *            Identity key
	 * @param startValidTime
	 *            Start valid time in msec
	 * @param endValidTime
	 *            End valid time in msec
	 * @param writtenTime
	 *            Written time in msec
	 * @param username
	 *            User name
	 */
	public TemporalKey(K identityKey, long startValidTime, long endValidTime, long writtenTime, String username)
	{
		this.identityKey = identityKey;
		this.startValidTime = startValidTime;
		this.endValidTime = endValidTime;
		this.writtenTime = writtenTime;
		this.username = username;
	}

	/**
	 * Returns the identity key.
	 */
	@Override
	public K getIdentityKey()
	{
		return this.identityKey;
	}

	/**
	 * Returns the written time in msec.
	 */
	@Override
	public long getWrittenTime()
	{
		return this.writtenTime;
	}

	/**
	 * Returns the end written time. It returns -1 if it is not set.
	 */
	public long getEndWrittenTime()
	{
		return this.endValidTime;
	}

	/**
	 * Sets the end written time. The end written time is typically set by the
	 * server and useful for external data sources that need to reflect the
	 * temporal data in their format.
	 * 
	 * @param endWrittenTime
	 *            End written time in msec.
	 */
	public void setEndWrittenTime(long endWrittenTime)
	{
		this.endWrittenTime = endWrittenTime;
	}

	/**
	 * Returns the start valid time in msec.
	 */
	@Override
	public long getStartValidTime()
	{
		return this.startValidTime;
	}

	/**
	 * Returns the end valid time in msec.
	 */
	@Override
	public long getEndValidTime()
	{
		return this.endValidTime;
	}

	/**
	 * Returns the user name. It may return null if user name is undefined,
	 * i.e., authentication is disabled. Note that the user name is not part of
	 * the hash code.
	 */
	@Override
	public String getUsername()
	{
		return username;
	}

	/**
	 * Returns true if the specified object has the same temporal information.
	 */
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		TemporalKey that = (TemporalKey) obj;
		return this.writtenTime == that.getWrittenTime()
				&& this.startValidTime == that.getStartValidTime()
				&& this.endValidTime == that.getEndValidTime()
				&& (this.identityKey == that.getIdentityKey() || this.identityKey != null
						&& this.identityKey.equals(that.getIdentityKey()));
	}

	/**
	 * Returns the hash code representing all temporal information, i.e.,
	 * identity key, start valid time, end valid time, and written time. Note
	 * that because the hash code is limited to the temporal information, the
	 * existing temporal entity can potentially overwritten if the temporal key
	 * has the same temporal information.
	 */
	public int hashCode()
	{
		int hash = 7;
		hash = 31 * hash + (int) this.writtenTime;
		hash = 31 * hash + (int) this.startValidTime;
		hash = 31 * hash + (int) this.endValidTime;
		hash = 31 * hash + (this.identityKey == null ? 0 : this.identityKey.hashCode());
		return hash;
	}

	/**
	 * Returns a string representation of the temporal key.
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer(100).append("TemporalKey[").append("identityKey=")
				.append(this.identityKey).append(", writtenTime=")
				.append(this.writtenTime).append(", startValidTime=").append(this.startValidTime)
				.append(", endValidTime=").append(this.endValidTime).append("]");
		return buffer.toString();
	}

	/**
	 * Returns a string representation in date formatted form of the temporal key.
	 */
	public String toStringDate()
	{
		StringBuffer buffer = new StringBuffer(100).append("TemporalKey[").append("identityKey=")
				.append(this.identityKey).append(", writtenTime=")
				.append(dateFormatter.format(new Date(this.writtenTime))).append(", startValidTime=")
				.append(dateFormatter.format(new Date(this.startValidTime))).append(", endValidTime=")
				.append(dateFormatter.format(new Date(this.endValidTime))).append("]");
		return buffer.toString();
	}

	/**
	 * Compares the identity key of the specified temporal key with this object.
	 * If the identity key does not implement {@link Comparable}, then it
	 * compares the string values.
	 */
	@Override
	public int compareTo(TemporalKey anotherTemporalKey)
	{
		if (identityKey instanceof Comparable) {
			return ((Comparable) identityKey).compareTo(anotherTemporalKey.getIdentityKey());
		}
		if (this == anotherTemporalKey || identityKey.equals(anotherTemporalKey.getIdentityKey())) {
			return 0;
		}
		return identityKey.toString().compareTo(anotherTemporalKey.getIdentityKey().toString());

	}
}