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
package com.netcrest.pado.temporal;

/**
 * TemporalHelper provides helper methods for handling temporal data. 
 * @author dpark
 *
 */
public class TemporalHelper
{
	/**
	 * Deserializes the attribute part of the specified temporal value. 
	 * @param temporalValue Temporal value
	 */
	public static void deserializeAttributes(ITemporalValue temporalValue)
	{
		if (temporalValue != null) {
			temporalValue.deserializeAttributes();
		}
	}
	
	/**
	 * Deserializes the ITempoeralData part of the specified temporal value. 
	 * @param temporalValue
	 */
	public static void deserializeData(ITemporalValue temporalValue)
	{
		if (temporalValue != null) {
			temporalValue.deserializeData();
		}
	}
	
	/**
	 * Deserializes the attachment part of the specified temporal value. 
	 */ 
	public static void deserializeAttachments(ITemporalValue temporalValue)
	{
		if (temporalValue != null) {
			temporalValue.deserializeAttachments();
		}
	}
	
	/**
	 * Deserializes all of the specified temporal value. 
	 */
	public static void deserializeAll(ITemporalValue temporalValue)
	{
		if (temporalValue != null) {
			temporalValue.deserializeData();
			temporalValue.deserializeAttachments();
			temporalValue.deserializeAttributes();
		}
	}
}
