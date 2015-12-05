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
package com.netcrest.pado.index.exception;

import com.netcrest.pado.index.service.IScrollableResultSet;

/**
 * GridQueryResultSetExpiredException is thrown when an
 * {@link IScrollableResultSet} operation detects the result set has been
 * expired.
 * 
 * @author dpark
 * 
 */
public class GridQueryResultSetExpiredException extends GridQueryException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new GridQueryResultSetExpiredException exception with
	 * <code>null</code> as its detail message. The cause is not initialized,
	 * and may subsequently be initialized by a call to {@link #initCause}.
	 */
	public GridQueryResultSetExpiredException()
	{
		super();
	}

	/**
	 * Constructs a new GridQueryResultSetExpiredException with the specified
	 * detail message.
	 * <p>
	 * Note that the detail message associated with <code>cause</code> is
	 * <i>not</i> automatically incorporated in this exception's detail message.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 */
	public GridQueryResultSetExpiredException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a new GridQueryResultSetExpiredException with the specified
	 * detail message and cause.
	 * <p>
	 * Note that the detail message associated with <code>cause</code> is
	 * <i>not</i> automatically incorporated in this exception's detail message.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public GridQueryResultSetExpiredException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new GridQueryResultSetExpiredException exception with the
	 * specified cause and a detail message of
	 * <tt>(cause==null ? null : cause.toString())</tt> (which typically
	 * contains the class and detail message of <tt>cause</tt>).
	 * 
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public GridQueryResultSetExpiredException(Throwable cause)
	{
		super(cause);
	}
}
