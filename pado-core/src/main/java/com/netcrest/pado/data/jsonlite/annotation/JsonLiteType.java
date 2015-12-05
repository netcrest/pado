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
package com.netcrest.pado.data.jsonlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JsonLiteType provides type information for JsonLite to properly determine the
 * data types when parsing JSON strings.
 * 
 * @author dpark
 * 
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonLiteType
{
	/**
	 * Returns the instantiable class that is of the return type. This type
	 * overrides the wrapper method return type. Default is an empty string,
	 * i.e., the wrapper method retury type.
	 */
	String returnType() default "";

	/**
	 * Returns the KeyTypce class that represents the value type. This type is
	 * also used to define the generic type if the value type is a generic
	 * class. Default is an empty string.
	 */
	String componentType() default "";

}
