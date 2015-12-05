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
package com.netcrest.pado.biz.mon;

import com.netcrest.pado.exception.PadoException;

/**
 * DeplymentFailedException occurs when jar file deployment fails due to a server error.
 * @author dpark
 *
 */
public class DeploymentFailedException extends PadoException
{
	/** Constructs a new DeploymentFailedException object with <code>null</code> as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
	public DeploymentFailedException()
	{
		super();
	}
	
	public DeploymentFailedException(String message) {
		super(message);
	    }

	    /**
	     * Constructs a new DeploymentFailedException with the specified detail message and
	     * cause.  <p>Note that the detail message associated with
	     * <code>cause</code> is <i>not</i> automatically incorporated in
	     * this pado exception's detail message.
	     *
	     * @param  message the detail message (which is saved for later retrieval
	     *         by the {@link #getMessage()} method).
	     * @param  cause the cause (which is saved for later retrieval by the
	     *         {@link #getCause()} method).  (A <tt>null</tt> value is
	     *         permitted, and indicates that the cause is nonexistent or
	     *         unknown.)
	     */
	    public DeploymentFailedException(String message, Throwable cause) {
	        super(message, cause);
	    }

	    /** Constructs a new DeploymentFailedException with the specified cause and a
	     * detail message of <tt>(cause==null ? null : cause.toString())</tt>
	     * (which typically contains the class and detail message of
	     * <tt>cause</tt>).  This constructor is useful for pado exceptions
	     * that are little more than wrappers for other throwables.
	     *
	     * @param  cause the cause (which is saved for later retrieval by the
	     *         {@link #getCause()} method).  (A <tt>null</tt> value is
	     *         permitted, and indicates that the cause is nonexistent or
	     *         unknown.)
	     */
	    public DeploymentFailedException(Throwable cause) {
	        super(cause);
	    }
}
