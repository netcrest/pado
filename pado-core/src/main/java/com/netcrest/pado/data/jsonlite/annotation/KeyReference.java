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

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyReference
{
	/**
	 * Returns the grid path. Default is an empty string indicating the object's
	 * default grid path.
	 */
	String path() default "";

	/**
	 * Returns the maximum depth of the object graph to traverse when searching
	 * the object references. This annotation is provided to handle circular
	 * references. The default value is 0 indicating no object references to
	 * search. Note that the depth is always initiated from the top-level object
	 * and decremented onwards. The nested objects' depths are never used during
	 * search.
	 */
	int depth() default 0;
}
