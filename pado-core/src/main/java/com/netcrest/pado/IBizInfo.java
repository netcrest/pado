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

import com.netcrest.pado.biz.info.MethodInfo;
import com.netcrest.pado.biz.info.SimpleBizInfo;
import com.netcrest.pado.data.jsonlite.JsonLite;

/**
 * IBizInfo provides IBiz class information. Every IBizInfo implementation class
 * should extend {@link SimpleBizInfo} and must conform to the following requirements: 
 * <ul>
 * <li>IBizInfo implementation class name must begin with the IBiz
 * implementation class name without the leading "I" and end with "Info". For
 * example, if the IBiz class name is <b>IFooBiz</b>, the IBizInfo implementation class
 * name must be <b>FooBizInfo</b>.</li>
 * <li>The package name must be a sub-package of the IBiz class ending with
 * "info". For example, given <b>com.foo.biz.IFooBiz</b>, the IBizInfo implementation class
 * has the fully-qualified class name of <b>com.foo.biz.info.FooBizInfo</b>.</li>
 * <li>The IBizInfo implementation class must provide the default (no-arg) constructor
 * that creates an instance with all necessary information in place.
 * For example, if {@link SimpleBizInfo} is extended by FooBizInfo then it would invoke
 * the "create" methods in the default constructor as follows:
 * 
 * <pre>
 * package com.foo.biz.info.FooBizInfo;
 *
 * import com.netcrest.pado.biz.info.SimpleBizInfo;
 * import com.foo.biz.IFooBiz;
 * 
 * public class FooBizInfo extends SimpleBizInfo
 * {
 *     public FooBizInfo()
 *     {
 *         setName(IFooBiz.class.getName());
 *         setDescription("IFooBiz performs ...");
 *         setAppIds("FooApp");
 *         setMethodInfos(
 *                 createMethod("getStatus", "Returns foo status", false, createReturn("JSON", "Foo status info", null,
 *                         createReturnArg("Code", "int", "Status code. < 0 for error, >= 0 for success", null, null,
 *                                 true),
 *                         createReturnArg("Message", "String", "Status message", null, null, true))),
 *                 createMethod("insertStatus", "Inserts status", false, createReturn(),
 *                         createArg("Code", "int", "Status code. < 0 for error, >= 0 for success", null, null, true),
 *                         createArg("Message", "String", "Status message", null, null, true)));
 *     }
 * }
 * </pre>
 * 
 * </li>
 * </ul>
 * The above example produces the following JSON representation:
 * 
 * <pre>
 * {
 *   "Name": "com.foo.biz.IFooBiz",
 *   "Desc": "IFooBiz performs ...",
 *   "AppIds": ["FooApp"],
 *   "Methods": [
 *       {
 *           "Return": {
 *               "Args": [
 *                   {
 *                       "Name": "Code",
 *                       "Desc": "Status code. < 0 for error, >= 0 for success",
 *                       "Type": "int",
 *                       "IsRequired": true
 *                   },
 *                   {
 *                       "Name": "Message",
 *                       "Desc": "Status message",
 *                       "Type": "String",
 *                       "IsRequired": true
 *                   }
 *               ],
 *               "Desc": "Foo status info",
 *               "Type": "JSON"
 *           },
 *           "Name": "getStatus",
 *           "Desc": "Returns foo status"
 *       },
 *       {
 *           "Args": [
 *               {
 *                   "Name": "Code",
 *                   "Desc": "Status code. < 0 for error, >= 0 for success",
 *                   "Type": "int",
 *                   "IsRequired": true
 *               },
 *               {
 *                   "Name": "Message",
 *                   "Desc": "Status message",
 *                   "Type": "String",
 *                   "IsRequired": true
 *               }
 *           ],
 *           "Name": "insertStatus",
 *           "Desc": "Inserts status"
 *       }
 *   ]
 * }
 * </pre>
 * 
 * @see SimpleBizInfo
 * @author dpark
 *
 */
public interface IBizInfo
{
	/**
	 * Returns the IBiz interface class name.
	 */
	String getBizInterfaceName();

	/**
	 * Returns the IBiz class description.
	 */
	String getDescription();

	/**
	 * Returns the detailed information of all methods exposed by IBiz.
	 */
	MethodInfo[] getMethodInfo();

	/**
	 * Returns the JSON representation of this object.
	 */
	JsonLite<?> toJson();

	/**
	 * Returns the network neutral representation of this object. Non-network
	 * neutral IBizInfo objects are those that are created by the server and not
	 * transferable to clients due to the IBizInfo implementation classes that
	 * may not be available in the client side. This method essentially clones
	 * this object using a class that is guaranteed to be available in the
	 * client side.
	 */
	IBizInfo toBizInfo();

	/**
	 * Returns the allowed app IDs that have access to the IBiz class.
	 * 
	 * @return null if all app IDs have access to the IBiz class.
	 */
	String[] getAppIds();
}
