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
package com.netcrest.pado;

import com.netcrest.pado.data.jsonlite.JsonLiteArray;

public interface IBizJson
{
	/**
	 * Arguments passed in by {@link ICatalog#newInstance(Class, Object...)}.
	 * This method initializes the IBiz object using the elements in the
	 * specified JSON array.
	 * 
	 * @param biz
	 *            The remote IBiz object. This should be type-casted with the
	 *            remote IBiz class.
	 * @param pado
	 *            The Pado instance obtained from invoking
	 *            <cod>Pado.login()</code>.
	 * @param args
	 *            The local object initialization arguments passed in via
	 *            {@link ICatalog#newInstance(Class, Object...)}.
	 * @see ICatalog
	 */
	void init(IBiz biz, IPado pado, JsonLiteArray args);

	/**
	 * Invokes the specified IBiz method with the elements of the specified
	 * JsonLite array as its arguments.
	 * 
	 * @param bizMethodName
	 *            IBiz method name
	 * @param argArray
	 *            JsonLite array as method arguments
	 * @return The method return value transformed to JSON element.
	 */
	Object invoke(String bizMethodName, JsonLiteArray argArray);
}
