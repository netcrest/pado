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
package com.netcrest.pado.tools.db;

/**
 * InvalidAttributeException is thrown when an attribute cannot be parsed due
 * to the invalid format.
 * 
 * @author dpark
 *
 */
public class InvalidAttributeException extends Exception
{
	private static final long serialVersionUID = 1L;

    public InvalidAttributeException()
    {
    }

    public InvalidAttributeException(String message)
    {
    	super(message);
    }
    
    public InvalidAttributeException(String message, Throwable cause)
    {
        super(cause.getClass() + ": " + cause.getMessage() + ". " + message);
        initCause(cause);
    }

    public InvalidAttributeException(Throwable cause)
    {
        super(cause.getClass() + ": " + cause.getMessage() + ".");
        initCause(cause);
    }
}
