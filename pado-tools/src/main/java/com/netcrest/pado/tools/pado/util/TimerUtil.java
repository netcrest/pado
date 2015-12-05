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
package com.netcrest.pado.tools.pado.util;

import com.netcrest.pado.tools.pado.PadoShell;

/**
 * 
 */
public class TimerUtil
{
	private static long startTime;
	private static long stopTime;
	private static boolean isTimerStarted;

	/**
	 * Resets all Instance variables
	 */

	public static void reset()
	{
		startTime = 0;
		stopTime = 0;
		isTimerStarted = false;
	}

	/**
	 * Initializes startTime instance variable
	 * 
	 */

	public static void startTimer()
	{
		reset();
		isTimerStarted = true;
		startTime = System.currentTimeMillis();
	}

	/**
	 * Initializes stopTime instance variable
	 * 
	 * @throw IllegalTimerStateException
	 */

	private static void stopTimer()
	{
		if (isTimerStarted) {
			stopTime = System.currentTimeMillis();
			isTimerStarted = false;
		} 
	}

	/**
	 * Prints the time elapse in millisec since the startTimer() is called
	 */

	public static void printExecutionTime()
	{
		if (isTimerStarted) {
			stopTimer();
			PadoShell.println("elapsed(msec) " + (stopTime - startTime));
		}
	}

}
