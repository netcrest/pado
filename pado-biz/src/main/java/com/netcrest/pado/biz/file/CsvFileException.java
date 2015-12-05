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
package com.netcrest.pado.biz.file;

/**
 * CsvFileException is thrown when an error occurs while loading file contents.
 * 
 * @author dpark
 *
 */
public class CsvFileException extends Exception
{
	private static final long serialVersionUID = 1L;

    public CsvFileException()
    {
    }
    
    public CsvFileException(String message)
	{
		super(message);
	}

    public CsvFileException(String message, Throwable cause)
    {
        super(cause.getClass() + ": " + cause.getMessage() + ". " + message);
        initCause(cause);
    }

    public CsvFileException(Throwable cause)
    {
        super(cause.getClass() + ": " + cause.getMessage() + ".");
        initCause(cause);
    }
}
