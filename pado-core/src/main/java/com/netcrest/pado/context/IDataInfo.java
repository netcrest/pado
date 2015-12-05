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
package com.netcrest.pado.context;

/**
 * IDataInfo provides data information required by the grid to validate
 * access rights.
 * 
 * @author dpark
 *
 */
public interface IDataInfo
{
	/**
	 * Clears all attributes.
	 */
	public void reset();
	
	/**
	 * Returns the value of the specified attribute key.
	 * @param key Attribute name
	 * @return Returns null if the specified key is not defined.
	 */
	public Object getAttribute(String key);
	
	/**
	 * Sets the specified key/value entry.
	 * @param key Attribute name
	 * @param value Atrribute value
	 */
	public void setAttribute(String key, Object value);
}
