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

/**
 * ILogger provides access to the underlying logging facility. Use
 * {@link Logger} to log messages.
 * 
 * @author dpark
 * 
 */
public interface ILogger
{
	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the warning level.
	 * 
	 * @param message
	 *            Warning message to log
	 * @param th
	 *            Throwable to log
	 */
	public void warning(String message, Throwable th);

	/**
	 * Logs the specified message at the warning level.
	 * 
	 * @param message
	 *            Warning message to log
	 */
	public void warning(String message);

	/**
	 * Logs the specified Throwable exception at the warning level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public void warning(Throwable th);

	/**
	 * Returns true if warning messages can be logged.
	 */
	public boolean isWarningEnabled();

	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the error level.
	 * 
	 * @param message
	 *            Error message to log
	 * @param th
	 *            Throwable to log
	 */
	public void error(String message, Throwable th);

	/**
	 * Logs the specified message at the error level.
	 * 
	 * @param message
	 *            Error message to log
	 */
	public void error(String message);

	/**
	 * Logs the specified Throwable exception at the error level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public void error(Throwable th);

	/**
	 * Returns true if error messages can be logged.
	 */
	public boolean isErrorEnabled();

	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the severe level.
	 * 
	 * @param message
	 *            Severe message to log
	 * @param th
	 *            Throwable to log
	 */
	public void severe(String message, Throwable th);

	/**
	 * Logs the specified Throwable exception at the severe level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public void severe(Throwable th);

	/**
	 * Logs the specified message at the severe level.
	 * 
	 * @param message
	 *            Severe message to log
	 */
	public void severe(String message);

	/**
	 * Returns true if severe messages can be logged.
	 */
	public boolean isSevereEnabled();

	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the info level.
	 * 
	 * @param message
	 *            Info message to log
	 * @param th
	 *            Throwable to log
	 */
	public void info(String message, Throwable th);

	/**
	 * Logs the specified message at the info level.
	 * 
	 * @param message
	 *            Info message to log
	 */
	public void info(String message);

	/**
	 * Logs the specified Throwable exception at the info level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public void info(Throwable th);

	/**
	 * Returns true if info messages can be logged.
	 */
	public boolean isInfoEnabled();

	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the config level.
	 * 
	 * @param message
	 *            Config message to log
	 * @param th
	 *            Throwable to log
	 */
	public void config(String message, Throwable th);

	/**
	 * Logs the specified message at the config level.
	 * 
	 * @param message
	 *            Config message to log
	 */
	public void config(String message);

	/**
	 * Logs the specified Throwable exception at the config level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public void config(Throwable th);

	/**
	 * Returns true if config messages can be logged.
	 */
	public boolean isConfigEnabled();

	/**
	 * Logs the specified message along with the specified Throwable exception
	 * at the fine level.
	 * 
	 * @param message
	 *            Fine message to log
	 * @param th
	 *            Throwable to log
	 */
	public void fine(String message, Throwable th);

	/**
	 * Logs the specified message at the fine level.
	 * 
	 * @param message
	 *            Fine message to log
	 */
	public void fine(String message);

	/**
	 * Logs the specified Throwable exception at the fine level.
	 * 
	 * @param th
	 *            Throwable to log
	 */
	public void fine(Throwable th);

	/**
	 * Returns true if fine messages can be logged.
	 */
	public boolean isFineEnabled();
	
	/**
	 * Logs the specified message without the row header.
	 * @param message Message to log
	 */
	public void log(String message);
}
