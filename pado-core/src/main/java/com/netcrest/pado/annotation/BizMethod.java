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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method should be processed as an IBiz Method. Note: If a
 * method is not marked with this annotation then it will not be made available
 * within the IBiz framework.
 * 
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface BizMethod
{

	/**
	 * Returns the logical name of the method - this can be useful to override
	 * the default name of the IBiz method with a user defined name.
	 */
	String methodName() default "";

	/**
	 * Returns the biz type that determines the grid target and the security and
	 * compliance enforcement. Setting this annotation overrides the
	 * object-level biz type set by {@link BizClass#bizType()}.
	 * 
	 * @see BizClass#bizType()
	 */
	BizType bizType() default BizType.DEFAULT;

}
