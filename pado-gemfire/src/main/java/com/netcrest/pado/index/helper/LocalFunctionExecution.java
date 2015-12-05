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
package com.netcrest.pado.index.helper;

import java.io.Serializable;
import java.util.Set;

import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.cache.execute.ResultSender;
import com.gemstone.gemfire.cache.operations.ExecuteFunctionOperationContext;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LocalFunctionExecution implements Execution
{
	private ResultCollector resultCollector = null;
	private Set filters = null;
	private Object argument = null;
	private boolean done = false;

	@Override
	public ResultCollector<? extends Serializable, ? extends Serializable> execute(String arg0)
			throws FunctionException
			{
		return null;
	}

	@Override
	public ResultCollector<? extends Serializable, ? extends Serializable> execute(final Function arg0)
			throws FunctionException
	{
		TestResultSender resultSender = new TestResultSender(resultCollector);
		final FunctionContext context = new TestFunctionContext(argument, arg0.getId(), resultSender, filters);
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run()
			{
				synchronized (resultCollector) {
					while (!done) {
						try {
							resultCollector.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		thread.setDaemon(true);
		thread = new Thread(new Runnable() {

			@Override
			public void run()
			{
				arg0.execute(context);
			}
		});
		thread.setDaemon(true);
		thread.start();
		synchronized (resultCollector) {
			try {
				while (!done) {
					resultCollector.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return resultCollector;
	}

	@Override
	public ResultCollector<? extends Serializable, ? extends Serializable> execute(String arg0, boolean arg1)
			throws FunctionException
	{
		return null;
	}

	@Override
	public ResultCollector<? extends Serializable, ? extends Serializable> execute(String arg0, boolean arg1,
			boolean arg2) throws FunctionException
	{
		return null;
	}

	@Override
	public ResultCollector<? extends Serializable, ? extends Serializable> execute(String arg0, boolean arg1,
			boolean arg2, boolean arg3) throws FunctionException
	{
		return null;
	}

	// @Override
	// public Execution withArgs(Serializable arg0) {
	//
	// argument = arg0;
	// return this;
	// }
	//
	// @Override
	// public Execution withCollector(
	// ResultCollector<? extends Serializable, ? extends Serializable> arg0) {
	//
	// resultCollector = arg0;
	// return this;
	// }
	//
	@Override
	public Execution withArgs(Object arg0)
	{
		argument = arg0;
		return this;
	}

	@Override
	public Execution withCollector(ResultCollector<?, ?> rc)
	{
		resultCollector = rc;
		return this;
	}

	@Override
	public Execution withFilter(Set<?> arg0)
	{
		filters = arg0;
		return this;
	}

	public static class TestFunctionContext implements FunctionContext
	{
		private Object argument = null;
		private String functionId = null;
		private TestResultSender resultSender = null;
		private Set filters = null;

		public TestFunctionContext(Object argument, String functionId, TestResultSender resultSender, Set filters)
		{
			this.argument = argument;
			this.functionId = functionId;
			this.resultSender = resultSender;
			this.filters = filters;
		}

		@Override
		public Object getArguments()
		{
			return this.argument;
		}

		@Override
		public String getFunctionId()
		{
			return this.functionId;
		}

		@Override
		public ResultSender getResultSender()
		{
			return this.resultSender;
		}

		@Override
		public boolean isPossibleDuplicate()
		{
			return false;
		}

		public Set getFilter()
		{
			return filters;
		}

	}

	private class TestResultSender implements ResultSender<Serializable>
	{
		private ResultCollector resultCollector = null;

		TestResultSender(ResultCollector collector)
		{
			this.resultCollector = collector;
		}

		@Override
		public void lastResult(Serializable arg0)
		{
			this.resultCollector.addResult(null, arg0);
			this.resultCollector.endResults();
			synchronized (this.resultCollector) {
				done = true;
				this.resultCollector.notifyAll();
			}

		}

		@Override
		public void sendException(Throwable arg0)
		{
		}

		@Override
		public void sendResult(Serializable arg0)
		{
			this.resultCollector.addResult(null, arg0);
		}

		public ExecuteFunctionOperationContext getOperationContext()
		{
			return null;
		}

	}

}
