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
package com.netcrest.pado.log;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

import com.netcrest.pado.internal.Constants;

/**
 * Logger delegates log messages to the underlying logging facility.
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Logger
{
	private static ILogger logger;
	
	private static WeakHashMap<String, ILogger> loggerMap = new WeakHashMap<String, ILogger>();
	
	static {
		try {
			// TODO: Unable to use PadoUtil due to dependency problems. See if we
			// can also support pado.properties.
//			Class clazz = PadoUtil.getClass(PROP_CLASS_LOGGER, DEFAULT_CLASS_LOGGER);
			String className = System.getProperty("pado." + Constants.PROP_CLASS_LOGGER, Constants.DEFAULT_CLASS_LOGGER);
			Class clazz = Class.forName(className);
			Method method = clazz.getMethod("getLogger");
			logger = (ILogger)method.invoke(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the warning level.
	 * 
	 * @param message
	 *            Warning message to log
	 * @param th
	 *            Throwable to log
	 */
	public static void warning(String message, Throwable th)
	{
		logger.warning(message, th);
	}

	/**
	 * Logs the specified message at the warning level.
	 * 
	 * @param message
	 *            Warning message to log
	 */
	public static void warning(String message)
	{
		logger.warning(message);
	}

	/**
	 * Logs the specified Throwable exception at the warning level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public static void warning(Throwable th)
	{
		logger.warning(th);
	}

	/**
	 * Returns true if warning messages can be logged.
	 */
	public static boolean isWarningEnabled()
	{
		return logger.isWarningEnabled();
	}

	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the error level.
	 * 
	 * @param message
	 *            Error message to log
	 * @param th
	 *            Throwable to log
	 */
	public static void error(String message, Throwable th)
	{
		logger.error(message, th);
	}

	/**
	 * Logs the specified message at the error level.
	 * 
	 * @param message
	 *            Error message to log
	 */
	public static void error(String message)
	{
		logger.error(message);
	}

	/**
	 * Logs the specified Throwable exception at the error level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public static void error(Throwable th)
	{
		logger.error(th);
	}

	/**
	 * Returns true if error messages can be logged.
	 */
	public static boolean isErrorEnabled()
	{
		return logger.isConfigEnabled();
	}

	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the severe level.
	 * 
	 * @param message
	 *            Severe message to log
	 * @param th
	 *            Throwable to log
	 */
	public static void severe(String message, Throwable th)
	{
		logger.severe(message, th);
	}

	/**
	 * Logs the specified Throwable exception at the severe level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public static void severe(Throwable th)
	{
		logger.severe(th);
	}

	/**
	 * Logs the specified message at the severe level.
	 * 
	 * @param message
	 *            Severe message to log
	 */
	public static void severe(String message)
	{
		logger.severe(message);
	}

	/**
	 * Returns true if severe messages can be logged.
	 */
	public static boolean isSevereEnabled()
	{
		return logger.isSevereEnabled();
	}

	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the info level.
	 * 
	 * @param message
	 *            Info message to log
	 * @param th
	 *            Throwable to log
	 */
	public static void info(String message, Throwable th)
	{
		logger.info(message, th);
	}

	/**
	 * Logs the specified message at the info level.
	 * 
	 * @param message
	 *            Info message to log
	 */
	public static void info(String message)
	{
		logger.info(message);
	}

	/**
	 * Logs the specified Throwable exception at the info level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public static void info(Throwable th)
	{
		logger.info(th);
	}

	/**
	 * Returns true if info messages can be logged.
	 */
	public static boolean isInfoEnabled()
	{
		return logger.isInfoEnabled();
	}

	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the config level.
	 * 
	 * @param message
	 *            Config message to log
	 * @param th
	 *            Throwable to log
	 */
	public static void config(String message, Throwable th)
	{
		logger.config(message, th);
	}

	/**
	 * Logs the specified message at the config level.
	 * 
	 * @param message
	 *            Config message to log
	 */
	public static void config(String message)
	{
		logger.config(message);
	}

	/**
	 * Logs the specified Throwable exception at the config level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public static void config(Throwable th)
	{
		logger.config(th);
	}

	/**
	 * Returns true if config messages can be logged.
	 */
	public static boolean isConfigEnabled()
	{
		return logger.isConfigEnabled();
	}

	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the fine level.
	 * 
	 * @param message
	 *            Fine message to log
	 * @param th
	 *            Throwable to log
	 */
	public static void fine(String message, Throwable th)
	{
		logger.fine(message, th);
	}

	/**
	 * Logs the specified message at the fine level.
	 * 
	 * @param message
	 *            Fine message to log
	 */
	public static void fine(String message)
	{
		logger.fine(message);
	}

	/**
	 * Logs the specified Throwable exception at the fine level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public static void fine(Throwable th)
	{
		logger.fine(th);
	}

	/**
	 * Returns true if fine messages can be logged.
	 */
	public static boolean isFineEnabled()
	{
		return logger.isFineEnabled();
	}
	
	/**
	 * Logs the specified message without the row header.
	 * @param message Message to log
	 */
	public static void log(String message)
	{
		logger.log(message);
	}
	
	/**
	 * Returns the underlying logger that logs messages.
	 */
	public static ILogger getLogger()
	{
		return logger;
	}
}
