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

import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;

/**
 * TaskFunction delegates function calls in the form of tasks. The executing
 * task must return the last value, which is sent by this function. Task objects 
 * are free to send interim values, i.e., it is free to invoke 
 * FunctionContext.getResultSender().sendResult(). They must not, however,
 * invoke FunctionContext.getResultSender().lastResult(). Instead return the 
 * last result.
 * <p>
 * It has the following function configuration:
 * <ul>
 * <li>hasResult=true</li>
 * <li>optimizeForWrite=true</li>
 * <li>isHA=true</li>
 * </li>
 * @author dpark
 *
 */
public class TaskFunction implements Function, Declarable
{
	private static final long serialVersionUID = 1L;
	
	public final static String Id = TaskFunction.class.getSimpleName();
	
	@Override
	public boolean hasResult()
	{
		return true;
	}

	@Override
	public void execute(FunctionContext context)
	{
		ITask task = (ITask)context.getArguments();
		Object obj = task.call(context);
		context.getResultSender().lastResult(obj);
	}

	@Override
	public String getId()
	{
		return Id;
	}

	@Override
	public boolean optimizeForWrite()
	{
		return true;
	}

	@Override
	public boolean isHA()
	{
		return true;
	}

	@Override
	public void init(Properties props)
	{
	}

}
