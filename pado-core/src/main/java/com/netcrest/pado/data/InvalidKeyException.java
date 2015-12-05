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
package com.netcrest.pado.data;


/**
 * InvalidKeyException is a runtime exception thrown if the key type 
 * is invalid. This can occur when the incorrect type value is put in
 * the message (MapLite) class.
 *   
 * @author dpark
 *
 */
public class InvalidKeyException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new <code>InvalidKeyException</code> with the specified 
	 * message.
	 */
	public InvalidKeyException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new <code>InvalidKeyException</code> wit the specified
	 * message and exception.
	 */
	public InvalidKeyException(String message, Throwable ex)
	{
		super(message, ex);
	}
}
