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
package com.netcrest.pado.hazelcast.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.logging.ILogger;

public class HazelcastLogger implements com.netcrest.pado.log.ILogger, HazelcastInstanceAware
{
	private final static HazelcastLogger logger = new HazelcastLogger();
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS zzz");
	
	private static HazelcastInstance hzInstance;
	
	private static ILogger getLogWriter()
	{
		try {
			if (hzInstance == null) {
				return null;
			}
			return hzInstance.getLoggingService().getLogger(HazelcastLogger.class);
		} catch (Exception ex) {
			return null;
		}
	}
	
	private static String getMethodTrace()
	{
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		// [0] getStackTrace()
		// [1] getMethodTrace()
		// [2] getMethodName()
		// [3] GemfireLogger method
		// [4] Logger method
		// [5] the caller's method
	    StackTraceElement e = stacktrace[5]; 
	    return e.toString();
	}
	
	private static String getLogMessage()
	{
		return getMethodTrace();
	}
	
	private static String getLogMessage(String message)
	{
		return getMethodTrace() + ": " + message;
	}
	
	public static com.netcrest.pado.log.ILogger getLogger()
	{
		return logger;
	}
	
	private HazelcastLogger() 
	{}

	@Override
	public void warning(String message, Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("warning", getLogMessage(message));
			th.printStackTrace(System.out);
		} else {
			logger.warning(getLogMessage(message), th);
		}
	}
	
	@Override
	public void warning(String message)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("warning", getLogMessage(message));
		} else {
			logger.warning(getLogMessage(message));
		}
	}
	
	@Override
	public void warning(Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("warning", getLogMessage());
			th.printStackTrace(System.out);
		} else {
			logger.warning(getLogMessage(), th);
		}
	}
	
	@Override
	public boolean isWarningEnabled()
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			return false;
		} else {
			return logger.isWarningEnabled();
		}
		
	}
	
	@Override
	public void error(String message, Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			System.err.println(getLogMessage(message));
			th.printStackTrace(System.err);
		} else {
			logger.severe(message, th);
		}
	}
	
	@Override
	public void error(String message)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			System.err.println(getLogMessage(message));
		} else {
			logger.severe(message);
		}
	}
	
	@Override
	public void error(Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			System.err.println(getLogMessage());
			th.printStackTrace(System.err);
		} else {
			logger.severe(getLogMessage(), th);
		}
	}
	
	@Override
	public boolean isErrorEnabled()
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			return false;
		} else {
			return logger.isSevereEnabled();
		}
		
	}
	
	@Override
	public void severe(String message, Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			System.err.println(getLogMessage(message));
			th.printStackTrace(System.err);
		} else {
			logger.severe(getLogMessage(message), th);
		}
	}
	
	@Override
	public void severe(Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			System.err.println(getLogMessage());
			th.printStackTrace(System.err);
		} else {
			logger.severe(getLogMessage(), th);
		}
	}
	
	@Override
	public void severe(String message)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			System.err.println(getLogMessage(message));
		} else {
			logger.severe(getLogMessage(message));
		}
	}
	
	@Override
	public boolean isSevereEnabled()
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			return false;
		} else {
			return logger.isSevereEnabled();
		}
	}
	
	@Override
	public void info(String message, Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("info", getLogMessage(message));
			th.printStackTrace(System.out);
		} else {
			logger.info(getLogMessage(message), th);
		}
	}
	
	@Override
	public void info(String message)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("info", getLogMessage(message));
		} else {
			logger.info(getLogMessage(message));
		}
	}
	
	@Override
	public void info(Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("info", getLogMessage());
			th.printStackTrace(System.out);
		} else {
			logger.info(getLogMessage(), th);
		}
	}
	
	@Override
	public boolean isInfoEnabled()
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			return false;
		} else {
			return logger.isInfoEnabled();
		}
	}
	
	@Override
	public void config(String message, Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("config", getLogMessage(message));
			th.printStackTrace(System.out);
		} else {
			logger.info(getLogMessage(message), th);
		}
	}
	
	@Override
	public void config(String message)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("config", getLogMessage(message));
		} else {
			logger.info(getLogMessage(message));
		}
	}
	
	@Override
	public void config(Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("config", getLogMessage());
			th.printStackTrace(System.out);
		} else {
			logger.info(getLogMessage(), th);
		}
	}
	
	@Override
	public boolean isConfigEnabled()
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			return false;
		} else {
			return logger.isInfoEnabled();
		}
		
	}
	
	@Override
	public void fine(String message, Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("fine", getLogMessage(message));
			th.printStackTrace(System.out);
		} else {
			logger.fine(getLogMessage(message), th);
		}
	}
	
	@Override
	public void fine(String message)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("fine", getLogMessage(message));
		} else {
			logger.fine(getLogMessage(message));
		}
	}
	
	@Override
	public void fine(Throwable th)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("fine", getLogMessage());
			th.printStackTrace(System.out);
		} else {
			logger.fine(getLogMessage(), th);
		}
	}
	
	@Override
	public boolean isFineEnabled()
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			return false;
		} else {
			return logger.isFineEnabled();
		}
		
	}
	
	@Override
	public void log(String message)
	{
		ILogger logger = getLogWriter();
		if (logger == null) {
			printMessage("info", message);
		} else {
			logger.info(message);
		}
	}
	
	private void printMessage(String type, String message)
	{
		StringBuffer buffer = new StringBuffer(40);
		buffer.append("[");
		buffer.append(type);
		buffer.append(" ");
		buffer.append(dateFormat.format(new Date()));
		buffer.append(" <");
		if (Thread.currentThread().getThreadGroup() != null) {
			buffer.append(Thread.currentThread().getThreadGroup().getName());
			buffer.append(" ");
		} 
		buffer.append(Thread.currentThread().getName());
		buffer.append("> tid=");
		buffer.append(Thread.currentThread().getId());
		buffer.append("] ");
		buffer.append(message);
		System.out.println(buffer.toString());
	}

	@Override
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		hzInstance = hazelcastInstance;
	}
}

