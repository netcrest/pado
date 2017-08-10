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

import javax.annotation.Resource;

import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.internal.impl.GridService;

/**
 * ICatalog contains a catalog of the allowed IBiz classes that the application
 * can create by invoking its newInstance() methods. The catalog is managed by
 * the grid per app ID. The user must have the proper permissions in order to
 * obtain the the catalog with the desired IBiz classes.
 * 
 * @author dpark
 * 
 */
public interface ICatalog
{
	/**
	 * Returns a new IBiz proxy instance or a new {@link IBizLocal} instance of
	 * the specified IBiz class. A new IBizLocal instance is returned if the
	 * local implementation class exists in the standard IBiz package or it is
	 * specified by the annotation &#64;{@link BizClass#localImpl()}. To
	 * override IBizLocal, use
	 * {@link #newInstanceLocal(Class, IBizLocal, Object...)}.
	 * 
	 * @param bizClass
	 *            IBiz class
	 * @param bizLocalArguments
	 *            IBizLocal class initialization arguments. The arguments are
	 *            passed in to {@link IBizLocal#init(IBiz, IPado, Object...)}.
	 */
	<T> T newInstance(Class<T> bizClass, Object... bizLocalArguments);

	/**
	 * Returns an instance of the specified IBizLocal class. The returned
	 * instance has the IBiz proxy instance as a resource if &#64;
	 * {@link Resource} is declared in the IBizLocal class. IBizLocal is
	 * typically used to perform input argument validations before executing the
	 * server methods. This method overrides the default local implementation
	 * class in the standard IBiz package and the implementation class defined
	 * by the annotation &#64; {@link BizClass#localImpl()}.
	 * 
	 * @param bizClass
	 *            IBiz class
	 * @param bizLocal
	 *            IBizLocal instance
	 * @param bizLocalArguments
	 *            IBizLocal class initialization arguments. The arguments are
	 *            passed in to {@link IBizLocal#init(IBiz, IPado, Object...)}.
	 */
	<T> T newInstanceLocal(Class<T> bizClass, IBizLocal bizLocal, Object... bizLocalArguments);

	/**
	 * Returns a new IBiz proxy instance or a new {@link IBizLocal} instance of
	 * the specified IBiz class. A new IBizLocal instance is returned if the
	 * local implementation class exists in the standard IBiz package or it is
	 * specified by the annotation &#64;{@link BizClass#localImpl()}. To
	 * override IBizLocal, use
	 * {@link #newInstanceLocal(String, String, Object...)}.
	 * 
	 * @param bizInterfaceName
	 *            IBiz class name.
	 * @param bizLocalArguments
	 *            IBizLocal class initialization arguments. The arguments are
	 *            passed in to {@link IBizLocal#init(IBiz, IPado, Object...)}.
	 */
	IBiz newInstance(String bizInterfaceName, Object... bizLocalArguments);

	/**
	 * Returns an instance of the specified IBizLocal class. The returned
	 * instance has the IBiz proxy instance as a resource if &#64;
	 * {@link Resource} is declared in the IBizLocal class. IBizLocal is
	 * typically used to perform input argument validations before executing the
	 * server methods. This method overrides the default local implementation
	 * class in the standard IBiz package and the implementation class defined
	 * by the annotation &#64; {@link BizClass#localImpl()}.
	 * 
	 * @param bizInterfaceName
	 *            IBiz class name.
	 * @param bizLocalClassName
	 *            IBizLocal class name.
	 * @param bizLocalArguments
	 *            IBizLocal class initialization arguments. The arguments are
	 *            passed in to {@link IBizLocal#init(IBiz, IPado, Object...)}.
	 */
	IBizLocal newInstanceLocal(String bizInterfaceName, String bizLocalClassName, Object... bizLocalArguments);

	/**
	 * Returns all IBiz class names in the catalog.
	 */
	String[] getAllBizClassNames();

	/**
	 * Returns all IBiz class names in the catalog.
	 * 
	 * @param regex
	 *            Regular expression for filtering IBiz class names. If null, no
	 *            filtering occurs (same as calling
	 *            {@link #getAllBizClassNames()}.
	 */
	String[] getBizClassNames(String regex);

	/**
	 * Returns all IBiz classes in the catalog.
	 */
	Class<?>[] getAllBizClasses();

	/**
	 * Returns all IBiz classes in the catalog.
	 * 
	 * @param regex
	 *            Regular expression for filtering IBiz class names. If null, no
	 *            filtering occurs (same as calling {@link #getAllBizClasses()}.
	 */
	Class<?>[] getBizClasses(String regex);

	/**
	 * Returns all IBizInfo objects containing IBiz class meta data information.
	 */
	IBizInfo[] getAllBizInfos();

	/**
	 * Returns all IBizInfo objects containing IBiz class meta data information.
	 * 
	 * @param regex
	 *            Regular expression for filtering IBiz class names. If null, no
	 *            filtering occurs (same as calling {@link #getAllBizInfos()}.
	 */
	IBizInfo[] getBizInfos(String regex);

	/**
	 * Returns IDs of all participating grids that support the catalog.
	 */
	String[] getGridIds();

	/**
	 * Returns the app ID. Catalog services are always bound to an individual
	 * app.
	 */
	String getAppId();

	/**
	 * Returns the grid service that provides grid information pertaining to
	 * this catalog.
	 */
	GridService getGridService();

	/**
	 * Refreshes the catalog by syncing up with the grid.
	 */
	void refresh();

	/**
	 * Closes the catalog by releasing resources. Once closed, the catalog is no
	 * longer operational.
	 */
	void close();
}
