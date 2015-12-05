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

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.ICatalog;

/**
 * All interfaces that extend IBizFuture must supply the fully-qualified class
 * names of the IBiz interface via BizFuture.
 * 
 * @author dpark
 *
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface BizFuture {
	/**
	 * Fully-qualified IBiz interface class name. 
	 */
	String bizInterface();
	
	/**
	 * IBizLocal implementation class name. If defined, the IBizLocal class is
	 * responsible for invoking the corresponding IBiz methods. Local implementation
	 * classes can use &#64;Resource to capture the corresponding IBiz proxy object 
	 * injected by {@link ICatalog#newInstance(Class, Object...)}. Note that the IBizLocal
	 * class specified by this annotation is the default local biz class and can
	 * be overridden by {@link ICatalog#newInstanceLocal(Class, IBizLocal, Object...)}.
	 * <ul>
	 * <li>
	 * All local biz classes must implement IBizLocal.
	 * </li>
	 * <li>
	 * Local biz classes implement the same IBiz interface as 
	 * the remote implementation class. For example, IFooBiz in the example
	 * above implements IBiz.
	 * </li>
	 * </ul>
	 * @see IBizLocal
	 * @see IBiz
	 */
	String localImpl() default "";
}
