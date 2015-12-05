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
 * Allows a logical name of IBiz methods to be configured. This annotation
 * is configured against the service interface definition.
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface BizClass {
	/**
	 * Unique IBiz name. This ID must be unique across all participating grids. If
	 * not specified, then the IBiz class name is assigned.
	 */
	String name() default "";
	
	/**
	 * Server-side implementation class name.
	 * 
	 * @return Class name of the remote implementation
	 */
	String remoteImpl() default "";
	
	/**
	 * IBizLocal implementation class name. If defined, the IBizLocal class is
	 * responsible for invoking the corresponding IBiz methods. Local implementation
	 * classes can use &#64;Resource to capture the corresponding IBiz proxy object 
	 * injected by {@link ICatalog#newInstance(Class, Object...)}. Note that the IBizLocal
	 * class specified by this annotation is the default local biz class and can
	 * be overridden by {@link ICatalog#newInstanceLocal(Class, IBizLocal, Object...)}.
	 * 
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
	 *       
	 * @see IBizLocal
	 * @see IBiz
	 */
	String localImpl() default "";
	
	/**
	 * BizType determines the target grid and the security and compliance
	 * enforcement rules for individual IBiz objects. The default value, 
	 * BizType.APP (BizType.DEFAULT), targets the default grid for all 
	 * methods that have not overridden this value and enforces the application-level 
	 * security and compliance rules that are user roles and data-oriented.
	 * BizType.Pado enforces the system-level security check 
	 * and directs all of the remote method calls to the pado grid that
	 * performed the logins. Note that this is an IBiz object level setting and can be
	 * overridden at the method level by setting {@link BizMethod#bizType()}.
	 * <p>
	 * If this annotation is not set or set to {@link BizType#DEFAULT}, then
	 * {@link BizType#APP} is enforced for all methods. Each method can
	 * override it via {@link BizMethod#bizType()}.
	 * 
	 * @see BizMethod#bizType()
	 */
	BizType bizType() default BizType.DEFAULT;
	
	/**
	 * The default path path relative to the root path. This path can be overwritten
	 * using {@link OnPath#path()}
	 */
	String path() default "";
	
	BizStateType bizStateType() default BizStateType.SINGLETON;
	
	
	public enum BizStateType { SINGLETON, STATELESS};
}