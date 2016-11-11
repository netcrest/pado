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

import java.util.Set;

import com.netcrest.pado.annotation.BizType;
import com.netcrest.pado.annotation.OnPath;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.RouterType;

/**
 * IGridContextClient provides IBiz grid context information.
 * 
 * @author dpark
 * 
 */
public interface IGridContextClient
{
	/**
	 * Sets the routing keys that determine the grid nodes that will execute the
	 * remote method. {@link #reset()} clears this value.
	 * 
	 * @param routingKeys
	 *            The routing keys handled by GemFire PartitionedResolover. If
	 *            null and &#64;{@link OnPath}, then the method will be invoked
	 *            on all grid nodes that have the region defined. &#64;
	 *            {@link OnServer} has no effect. The default is null.
	 */
	void setRoutingKeys(Set<?> routingKeys);

	/**
	 * Returns the routing keys set by the client application. The default is
	 * null. {@link #reset()} clears this value.
	 * 
	 * @see #setRoutingKeys(Set)
	 */
	Set<?> getRoutingKeys();

	/**
	 * Sets the path of the target grid's data store. This is analogous to the
	 * data store property of individual vendors listed below.
	 * 
	 * <ul>
	 * <li>GemFire - relative region path. It must not begin with "/" and must
	 * be relative to the root path which is configurable in Pado. The default
	 * root path is the grid ID.</li>
	 * </ul>
	 * <p>
	 * Note that the gridPath is typically set once and never changes during the
	 * life of the IBiz object. Hence the target path is not cleared if
	 * {@link #reset()} is invoked. The caller must explicitly invoke this
	 * method with the null argument to clear the target path. In that case, the
	 * default target path configured will be used.
	 * 
	 * @param gridPath
	 *            grid path
	 */
	void setGridPath(String gridPath);

	/**
	 * Returns the grid path set using {@link #setGridPath(String)}.
	 * {@link #reset()} clears this value.
	 */
	String getGridPath();

	/**
	 * Sets the IDs of the grids to invoke. This method overrides the primoridal
	 * grid ID. To invoke the primordial grid, its grid ID must also be included
	 * in the specified gridIds. {@link #reset()} clears this value.
	 * 
	 * @param gridIds
	 *            The unique grid IDs obtained from Pado during login. If null,
	 *            then only the primordial grid is invoked. The default is null.
	 */
	void setGridIds(String... gridIds);

	/**
	 * Returns the grid IDs set by invoking {@link #setGridIds(String...)}. The
	 * default is null. {@link #reset()} clears this value.
	 * 
	 * @see #setGridIds(String...)
	 */
	String[] getGridIds();

	/**
	 * Sets the grid collector that collects results from one or more grids.
	 * {@link #reset()} clears this value.
	 * 
	 * @param gridCollector
	 *            The grid collector that collects results from one or more
	 *            grids.
	 */
	void setGridCollector(IGridCollector gridCollector);

	/**
	 * Returns the grid collector. The default is null. {@link #reset()} clears
	 * this value.
	 */
	IGridCollector getGridCollector();

	/**
	 * If true, then the the IBiz method is executed on the pado that the user
	 * logged on. If false, the IBiz method is executed on the default grid.
	 * Default is false. This method overrides {@link BizType#PADO}.
	 * 
	 * @param padoAsTarget
	 *            true to execute on the pado, false to execute on the default
	 *            grid.
	 */
	void setPadoAsTarget(boolean padoAsTarget);

	/**
	 * Returns true if the the IBiz method is to be executed on the pado that
	 * the user logged on. Otherwise, the IBiz method is executed on the default
	 * grid. Default is false.
	 */
	boolean isPadoAsTarget();

	/**
	 * Sets additional arguments that are used as supplement to the actual IBiz
	 * method arguments. Some IBiz methods may require internal information that
	 * cannot be part of the method signature. This method allows that
	 * information to be sent to the server as part of the remote method call.
	 * This method is typically useful for IBizLocal classes that can provide
	 * additional information. All of the arguments must be Serializable.
	 * {@link #reset()} clears this value.
	 * 
	 * @param args
	 *            Additional arguments.
	 */
	void setAdditionalArguments(Object... args);

	/**
	 * Returns additional arguments set prior to invoking an IBiz method.
	 * 
	 * @return Additional arguments
	 * @see #setAdditionalArguments(Object...)
	 */
	Object[] getAttionalArguments();
	
	/**
	 * Sets transient data used within JVM process space.
	 * @param transientData Transient data
	 */
	void setTransientData(Object... transientData);
	
	/**
	 * Returns transient data used within JMV process space.
	 */
	Object[] getTransientData();

	/**
	 * Sets the grid location in which the target grid to be determined. This
	 * method call is valid only if router type is RouterType.LOCATION for
	 * {@link OnServer}. {@link #reset()} clears this value.
	 * 
	 * @param location
	 *            The geographical location of the target grid.
	 */
	void setGridLocation(String location);

	/**
	 * Returns the grid location set via {@link #setGridLocation(String)}.
	 * {@link #reset()} clears this value.
	 */
	String getGridLocation();

	/**
	 * Sets the router type that determines the grid(s) in which the IBiz method
	 * to be executed. The router type only applies to the grid paths that are
	 * appropriately configured. {@link #reset()} clears this value.
	 * 
	 * @param routerType
	 *            The routing type
	 */
	void setRouterType(RouterType routerType);

	/**
	 * Returns the router type set by {@link #setRouterType(RouterType)}.
	 * {@link #reset()} clears this value.
	 */
	RouterType getRouterType();

	/**
	 * Resets to the default settings by clearing all attributes set by the
	 * setter methods. As a best practice, this method should always be invoked
	 * first before invoking setters to ensure the removal of prior settings;
	 * otherwise, IGridContextClient retains and uses prior settings in
	 * subsequent IBiz method calls. The values listed under the "See Also:" tag
	 * are cleared by this method.
	 * 
	 * @see #setGridIds(String...)
	 * @see #setAdditionalArguments(Object...)
	 * @see #setGridCollector(IGridCollector)
	 * @see #setGridLocation(String)
	 * @see #setProductSpecificData(Object)
	 * @see #setRouterType(RouterType)
	 * @see #setRoutingKeys(Set)
	 * @see #setPadoAsTarget(boolean)
	 * @see #setTransientData(Object...)
	 */
	void reset();

	/**
	 * Sets the vendor product specific data value used internally.
	 * {@link #reset()} clears this value.
	 * 
	 * @param productData
	 */
	void setProductSpecificData(Object productData);

	/**
	 * Returns vendor product specific data set by
	 * {@link #setProductSpecificData(Object)}. {@link #reset()} clears this
	 * value.
	 */
	Object getProductSpecificData();
}
