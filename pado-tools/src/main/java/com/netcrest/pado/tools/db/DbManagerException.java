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
 * DBUtilException is thrown when DB errors occur.
 * 
 * @author dpark
 *
 */
public class DbManagerException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	enum ErrorType { 
		UNDEFINED, CONNECTION_CLOSED, CONNECTION_ALREADY_ESTABLISHED, NOT_IDBOBJECT
	}

    private ErrorType errorType = ErrorType.UNDEFINED;

    public DbManagerException()
    {
    }

    public DbManagerException(String message)
    {
    	super(message);
    }
    
    public DbManagerException(ErrorType errorType, String message)
    {
        super(message);
        this.errorType = errorType;
    }

    public DbManagerException(String message, Throwable cause)
    {
        super(cause.getClass() + ": " + cause.getMessage() + ". " + message);
        initCause(cause);
    }

    public DbManagerException(Throwable cause)
    {
        super(cause.getClass() + ": " + cause.getMessage() + ".");
        initCause(cause);
    }
    
    public ErrorType getErrorType()
    {
    	return errorType;
    }
}
