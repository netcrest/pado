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

import com.netcrest.pado.annotation.OnPath;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.internal.impl.CostBasedGridRouter;
import com.netcrest.pado.internal.impl.DefaultGridRouter;
import com.netcrest.pado.internal.impl.LocationBasedGridRouter;
import com.netcrest.pado.internal.impl.PartitionedGridRouter;
import com.netcrest.pado.internal.impl.RandomGridRouter;
import com.netcrest.pado.internal.impl.RoundRobinGridRouter;
import com.netcrest.pado.internal.impl.StickyGridRouter;

/**
 * All grid routers implement IGridRouter to provide IBiz method invocation
 * routing services. Pado has several built-in routers assignable by
 * {@link Type}. A custom router must be assigned to {@link Type#CUSTOM}. Note
 * that if the router type is not specified then it defaults to
 * {@link Type#COST}.
 * 
 * @author dpark
 * 
 */
public interface IGridRouter
{
	/**
	 * Grid router type representing built-in routers. Custom routers must be
	 * set to {@link #CUSTOM} and {@link IGridRouter}.
	 * 
	 * @author dpark
	 * 
	 */
	public static enum Type
	{

		/**
		 * Partitioned path spanning one or more grids. It routes each IBiz call
		 * the grid determined by specified routing keys.
		 */
		PARTITIONED("Partitioned", PartitionedGridRouter.class),

		/**
		 * Cost-based grid router. It routes each IBiz call to the grid with the
		 * least cost metric determined by the average network latency and the
		 * specified weight. This is the default.
		 */
		COST("Cost", CostBasedGridRouter.class),

		/**
		 * Location-based grid router. It routes each IBiz call to the grid in
		 * the specified location.
		 */
		LOCATION("Location", LocationBasedGridRouter.class),

		/**
		 * Load-based grid router. It routes each IBiz call to the grid with the
		 * least load.
		 */
		LOAD("Load", DefaultGridRouter.class),

		/**
		 * Random grid router. It routes each IBiz call to a randomly selected
		 * grid.
		 */
		RANDOM("Random", RandomGridRouter.class),

		/**
		 * Round-robin router. It routes each IBiz call to a grid selected in a
		 * round-robin fashion from the routing table.
		 */
		ROUND_ROBIN("RoundRobin", RoundRobinGridRouter.class),

		/**
		 * Sticky router. It routes each IBiz call to the same grid it initially
		 * randomly selected.
		 */
		STICKY("Sticky", StickyGridRouter.class),

		/**
		 * Custom router. It routes each IBiz call to one or more grids
		 * determined by the custom router supplied by the application. Note
		 * that the custom class must conform to Java Bean spec, i.e., it must
		 * include the no-arg public constructor.
		 */
		CUSTOM("Custom", null);

		private static int keyIndex;

		private static int getNextIndex()
		{
			keyIndex++;
			return keyIndex - 1;
		}

		private Type(String name, Class<?> type)
		{
			this.index = getNextIndex();
			this.name = name;
			this.type = type;
		}

		private int index;
		private String name;
		private Class<?> type;

		/**
		 * Returns the ordinal index value.
		 */
		public int getIndex()
		{
			return index;
		}

		/**
		 * Returns the string value of the enum index.
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * Returns the {@link IGridRouter} implementation class type. Note that
		 * the class must conform to Java Bean spec, i.e., it must include the
		 * no-arg public constructor.
		 */
		public Class<?> getType()
		{
			return type;
		}
	}

	/**
	 * Default type is {@link Type#COST}
	 */
	public final static Type DEFAULT_TYPE = Type.COST;

	/**
	 * Returns the router type.
	 */
	Type getType();

	/**
	 * Returns the ID of a reachable target grid where IBiz method invocation is
	 * routed for the specified key. The implementing class can use the
	 * specified key information to determine the target grid. If it returns
	 * null then the caller may elect to use the the default grid configured by
	 * Pado. Note that IGridRouter applies for both {@link OnPath} and
	 * {@link OnServer}. To get the non-discrimitory grid ID for both reachable
	 * and non-reachable, invoke
	 * {@link #findGridIdForPath(IBizContextClient, Object)} .
	 * 
	 * @param context
	 *            Client context obtained from {@link IBiz#getBizContext()}.
	 * @param key
	 *            The key that identifies the grid ID. This is typically but not
	 *            limited to the actual key that maps the value.
	 */
	String getReachableGridIdForPath(IBizContextClient context, Object key);

	/**
	 * Returns the ID of a reachable or non-reachable target grid where IBiz
	 * method invocation is routed for the specified key. The implementing class
	 * can use the specified key information to determine the target grid. The
	 * returned grid ID may or may not be reachable and therefore the caller
	 * should not use it to invoke IBiz methods.
	 * 
	 * @param context
	 *            Client context obtained from {@link IBiz#getBizContext()}.
	 * @param key
	 *            The key that identifies the grid ID. This is typically but not
	 *            limited to the actual key that maps the value.
	 */
	String findGridIdForPath(IBizContextClient context, Object key);

	/**
	 * Returns the ID of the target grid where IBiz method invocation is routed.
	 * This method is invoked by {@link OnServer} which has no key requirement.
	 * It selects a grid from the specified grid list in conjunction with the
	 * internal routing table based on the OnServer RoutingType annotation
	 * value. If grid Ids are not specified, then it refers to the grid ids in
	 * the routing table. If it returns null then the method will be invoked on
	 * the default grid configured by Pado.
	 * 
	 * @param context
	 *            IBizContextClient provided by the caller
	 */
	String getGridIdForNode(IBizContextClient context, String... gridIds);

	/**
	 * Adds the specified grid ID into the routing table.
	 * 
	 * @param gridId
	 *            Active grid ID
	 */
	void addGridId(String gridId);

	/**
	 * Removes the specified grid ID from the routing table.
	 * 
	 * @param gridId
	 *            grid ID
	 */
	void removeGridId(String gridId);

	/**
	 * Sets the active grid ID set. Grid IDs are normally determined by Pado as
	 * individual grids join the Pado cluster. However, some routers, such as
	 * the partitioned grid router, rely on this method to set the grid IDs that
	 * are pre-configured are not changeable.
	 * 
	 * @param gridIdSet
	 *            Grid ID set. If null then an empty grid ID set is assigned.
	 */
	void setGridIdSet(Set<String> gridIdSet);

	/**
	 * Returns the entire set of active grid IDs managed by this router.
	 */
	Set<String> getGridIdSet();

	/**
	 * Sets the allowed grid ID set. All other grids are ignored.
	 * 
	 * @param allowedGridIdSet
	 *            Set of allowed grid IDs. If null then all active grid IDs are
	 *            allowed.
	 */
	void setAllowedGridIdSet(Set<String> allowedGridIdSet);

	/**
	 * Returns the allowed grid ID set. It always returns a non-null set.
	 */
	Set<String> getAllowedGridIdSet();

}
