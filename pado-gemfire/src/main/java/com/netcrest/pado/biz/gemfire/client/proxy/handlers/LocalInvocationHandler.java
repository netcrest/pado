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
package com.netcrest.pado.biz.gemfire.client.proxy.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.netcrest.pado.biz.IBizStatistics;
import com.netcrest.pado.biz.gemfire.BizStatisticsManager;

public class LocalInvocationHandler<T> implements InvocationHandler
{
	private Object localObject;
	private IBizStatistics stats;

	public LocalInvocationHandler(Class<T> ibizClass, Object localObject)
	{
		this.localObject = localObject;
		stats = BizStatisticsManager.getBizStatistics(ibizClass, ibizClass.getSimpleName(), "");
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		Object retval = null;

		if (stats.isEnabled() && method.getName().equals("getBizContext") == false) {
			long startTime = stats.startCount(method.getName());
			try {
				retval = method.invoke(localObject, args);
			} catch (Throwable th) {
				throw getCause(method, th);
			} finally {
				stats.endCount(method.getName(), startTime);
			}
		} else {
			try {
				retval = method.invoke(localObject, args);
			} catch (Throwable th) {
				throw getCause(method, th);
			}
		}

		return retval;
	}

	private Throwable getCause(Method method, Throwable th) throws Throwable
	{
		Class<?> exTypes[] = method.getExceptionTypes();
		Throwable cause = th;
		Throwable lastThrowable = th;
		while (cause != null) {
			lastThrowable = cause;
			for (Class<?> exType : exTypes) {
				if (exType == cause.getClass()) {
					RuntimeException ex = new RuntimeException();
					StackTraceElement ste[] = ex.getStackTrace(); 
					StackTraceElement causeSte[] = lastThrowable.getStackTrace();
					StackTraceElement aggregateSte[] = new StackTraceElement[ste.length + causeSte.length];
					System.arraycopy(causeSte, 0, aggregateSte, 0, causeSte.length);
					System.arraycopy(ste, 0, aggregateSte, causeSte.length, ste.length);
					lastThrowable.setStackTrace(aggregateSte);
					throw lastThrowable;
				}
			}
			cause = cause.getCause();
		}
		throw lastThrowable;
	}

}
