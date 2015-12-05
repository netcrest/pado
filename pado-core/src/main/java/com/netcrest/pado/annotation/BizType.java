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
package com.netcrest.pado.annotation;

/**
 * BizType determines the target grid as well as the security scope for
 * ambiguous IBiz method invocations.
 * 
 * @author dpark
 * 
 */
public enum BizType
{
	/**
	 * BizType is always DEFAULT if not specified. The default is APP.
	 */
	DEFAULT,

	/**
	 * APP targets the default grid determined by Pado for method executions and
	 * enforces application-level security and compliance rules.
	 */
	APP,

	/**
	 * PADO targets the pado that has logged on the application and enforces
	 * system-level security and compliance rules.
	 */
	PADO
}
