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
package com.netcrest.pado.gemfire.util;

import java.io.Serializable;

import com.gemstone.gemfire.cache.execute.FunctionContext;

/**
 * ITask is invoked by TaskFunction which expects it in the form
 * of arguments. It typically includes input arguments and 
 * server-side business logic in the call() method.
 *  
 * @author dpark
 *
 */
public interface ITask extends Serializable
{
	/**
	 * Invoked by TaskFunction and returns a result object that
	 * is sent back to the caller (typically a client program). 
	 * FunctionContext.getResultSender().lastResult() must not be 
	 * invoked in this method. Instead it must return the last result, 
	 * which is in turn sent by TaskFunction. It is free to invoke
	 * FunctionContext.getResultSender().sendResult() for sending
	 * interim results.
	 * 
	 * @param context Function context.
	 * @return A result object to return to the caller. It can be null.
	 */
	Object call(FunctionContext context);
}
