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
package com.netcrest.pado.internal.impl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.data.ServerLoad;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.link.IUtilBizLink;

/**
 * GridRoutingTable determines the target grid based on its built-in routers and
 * user-supplied custom routers for a given app ID. The built-in routers are
 * cost-based, location-based, load-based, random, round-robin, and sticky
 * routers. The default router is the cost-based router.
 * 
 * @author dpark
 * 
 */
public class GridRoutingTable implements Externalizable, Cloneable
{
	private static final long serialVersionUID = 1L;

	/**
	 * The number of ping calls to determine the average latency between this
	 * client and a given grid. The sampling algorithm drops the low and the
	 * high values and averages the remaining ones.
	 */
	private final static int PING_SAMPLE_SIZE = 5;

	/**
	 * The default weight used in latency cost calculations. The valid weight
	 * values are [1, 10]. 1 affects the least cost and 10 affects the highest
	 * cost. The default value is 5.
	 */
	private final static byte DEFAULT_WEIGHT = 5;

	/**
	 * The current status of the grid. {@link #LIVE} means the grid is reachable
	 * from Pado. {@link #INACCESSIBLE} means Pado is not detecting the grid's
	 * heartbeats. The grid may be down or it is in configuration transition.
	 */
	public enum Status {
		LIVE, INACCESSIBLE
	}

	/**
	 * Application ID.
	 */
	protected String appId;

	/**
	 * &lt;appId, GridRoutingTable&gt; routingTableMap contains GRTs of all
	 * apps.
	 */
	private final static TreeMap<String, GridRoutingTable> routingTableMap = new TreeMap();

	/**
	 * costBasedTable contains &lt;gridId, Grid&gt; entries.
	 */
	protected TreeMap<String, Grid> costBasedTable = new TreeMap();

	/**
	 * costBasedTable contains Grid objects sorted by Grid.cost
	 */
	private transient TreeSet<Grid> costBasedSet = new TreeSet<Grid>();

	/**
	 * loadBasedTable contains Grid objects sorted by Grid.load
	 */
	private transient TreeSet<Grid> loadBasedSet = new TreeSet<Grid>(new GridLoadComparator());

	/**
	 * locationBasedTable contains &lt;location, TreeMap&lt;gridId,
	 * Grid&gt;&gt;. The top-level map is not sorted but the TreeMap is sorted
	 * by Grid.cost
	 */
	private transient HashMap<String, TreeMap<String, Grid>> locationBasedTable = new HashMap<String, TreeMap<String, Grid>>(
			20);

	/**
	 * PriorityType is currently always COST.
	 */
	public enum PriorityType {
		PRIMARY, COST, LOAD, LOCATION;
	}

	public GridRoutingTable()
	{
	}

	private GridRoutingTable(String appId)
	{
		this.appId = appId;
	}

	/**
	 * Creates a new GridRoutingTable if it doesn't exist. It returns the
	 * existing GridRoutingTable otherwise. <b>This method is internal use only.
	 * It must not be invoked directly.</b>
	 * 
	 * @param appId
	 *            Application ID
	 * @return The returned routing table contains only live grids, i.e.,
	 *         {@link Grid#getStatus()} == {@link Status#LIVE}.
	 * @see #createGridRoutingTableAllGrids(String)
	 */
	public synchronized static GridRoutingTable initializeGridRoutingTable(String appId)
	{
		GridRoutingTable grp = routingTableMap.get(appId);
		if (grp == null) {
			grp = new GridRoutingTable(appId);
			routingTableMap.put(appId, grp);
			grp.refresh();
		}
		return grp;
	}

	/**
	 * Returns the routing table for the specified appId. It returns null if the
	 * routing table has not yet been created. The routing table is created
	 * during login session and guaranteed to exist after successful login. That
	 * is, this method always returns a non-null value if it is invoked after a
	 * login.
	 * 
	 * @param appId
	 *            Application ID
	 * @return The returned routing table contains only live grids, i.e.,
	 *         {@link Grid#getStatus()} == {@link Status#LIVE}.
	 * @see #createPreviewGridRoutingTable(String)
	 */
	public static GridRoutingTable getGridRoutingTable(String appId)
	{
		return routingTableMap.get(appId);
	}

	/**
	 * Creates a new routing table that can be safely modified and/or destroyed.
	 * It clones the "live" routing table and also includes all grids that are
	 * not reachable, i.e., {@link Grid#getStatus()} ==
	 * {@link Status#INACCESSIBLE}. This method is useful for viewing all grids
	 * that make up the application as well as staging configuration changes. To
	 * make the changes into effect, invoke
	 * {@link ISysBizLink#updateGridRoutingTable(GridRoutingTable)}, which
	 * immediately publishes the changes to all participants including clients.
	 * 
	 * @param appId
	 *            Application ID
	 */
	public static GridRoutingTable createGridRoutingTableAllGrids(String appId)
	{
		GridRoutingTable liveGrp = getGridRoutingTable(appId);
		if (liveGrp == null) {
			return null;
		}

		GridRoutingTable grp = (GridRoutingTable) liveGrp.clone();
		AppInfo appInfo = PadoClientManager.getPadoClientManager().getAppInfo(appId);
		if (appInfo != null) {
			for (Grid allowedGrid : appInfo.getAllowedGridSet()) {
				if (grp.containsGrid(allowedGrid.getGridId()) == false) {
					grp.addGrid((Grid) allowedGrid.clone());
				}
			}
		}
		return grp;
	}

	/**
	 * Clones the routing table. The returned object can be safely modified.
	 */
	@Override
	public Object clone()
	{
		GridRoutingTable grp = new GridRoutingTable(appId);
		Grid[] grids = costBasedTable.values().toArray(new Grid[costBasedTable.size()]);
		for (int i = 0; i < grids.length; i++) {
			grp.addGrid((Grid) grids[i].clone());
		}
		return grp;
	}

	/**
	 * Creates a new routing table without registering it. The returned routing
	 * table can be used to preview configuration before permanently updating
	 * the routing table.
	 * 
	 * @param appId
	 *            Application ID
	 */
	public static GridRoutingTable createPreviewGridRoutingTable(String appId)
	{
		return new GridRoutingTable(appId);
	}

	/**
	 * Returns the app ID set that contains all registered app Ids.
	 */
	public static Set<String> getAppIdSet()
	{
		return routingTableMap.keySet();
	}

	public static GridRoutingTable removeGridRoutingTable(String appId)
	{
		return routingTableMap.remove(appId);
	}

	/**
	 * Returns the default weight.
	 */
	public static byte getDefaultWeight()
	{
		return DEFAULT_WEIGHT;
	}

	/**
	 * Returns the app ID.
	 */
	public String getAppId()
	{
		return appId;
	}

	/**
	 * Returns true if the specified grid ID is part of the routing table.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public boolean containsGrid(String gridId)
	{
		return costBasedTable.containsKey(gridId);
	}

	/**
	 * Returns the grid ID set.
	 */
	public Set<String> getGridIdSet()
	{
		return costBasedTable.keySet();
	}

	/**
	 * Adds a grid in the routing table.
	 * 
	 * @param grid
	 *            Grid
	 */
	public void addGrid(Grid grid)
	{
		grid.computeCost();
		costBasedTable.put(grid.gridId, grid);
		costBasedSet.add(grid);
		loadBasedSet.add(grid);
		TreeMap<String, Grid> locationCostBasedTable = locationBasedTable.get(grid.location);
		if (locationCostBasedTable == null) {
			locationCostBasedTable = new TreeMap();
			locationBasedTable.put(grid.location, locationCostBasedTable);
		}
		locationCostBasedTable.put(grid.gridId, grid);
	}

	/**
	 * Removes the specified grid ID from the routing table.
	 * 
	 * @param gridId
	 *            Grid ID
	 */
	public void removeGrid(String gridId)
	{
		Grid grid = costBasedTable.remove(gridId);
		if (grid != null) {
			costBasedSet.remove(grid);
			loadBasedSet.remove(grid);
		}
		Collection<TreeMap<String, Grid>> col = locationBasedTable.values();
		for (TreeMap<String, Grid> treeMap : col) {
			treeMap.remove(gridId);
		}
	}

	/**
	 * Returns the grid with the least routing cost. The returned grid has the
	 * fastest route amongst all of the grids.
	 */
	public Grid getLeastCostGrid()
	{
		if (costBasedSet.size() == 0) {
			return null;
		}
		return costBasedSet.first();
	}

	/**
	 * Returns the grid with the least routing cost amongst the specified grid
	 * Ids. It returns null if none of the specified grid Ids are in the table.
	 * 
	 * @param gridIds
	 *            the list of grid Ids to compare
	 */
	public Grid getLeastCostGrid(String... gridIds)
	{
		if (gridIds == null) {
			return null;
		}

		// If one of the specified grids is the least-cost grid then
		// return it immediately.
		Grid grid = getLeastCostGrid();
		if (grid != null) {
			for (String gridId : gridIds) {
				if (gridId.equals(grid.gridId)) {
					return grid;
				}
			}
		}

		// Search for the least-cost grid
		Grid selectedGrid = null;
		for (String gridId : gridIds) {
			grid = costBasedTable.get(gridId);
			if (grid != null) {
				if (selectedGrid == null || selectedGrid.cost > grid.cost) {
					selectedGrid = grid;
				}
			}
		}
		return selectedGrid;
	}

	/**
	 * Returns the grid with the least routing cost. The returned grid has the
	 * fastest route amongst all of the grids.
	 */
	public Grid getLeastLoadGrid()
	{
		if (loadBasedSet.size() == 0) {
			return null;
		}
		return loadBasedSet.first();
	}

	/**
	 * Returns the grid with the least load among the specified grid IDs.
	 * 
	 * @param gridIds
	 *            Grid IDs.
	 */
	public Grid getLeastLoadGrid(String... gridIds)
	{
		if (gridIds == null) {
			return null;
		}

		// If one of the specified grids is the least-load grid then
		// return it immediately.
		Grid grid = getLeastLoadGrid();
		for (String gridId : gridIds) {
			if (gridId.equals(grid.gridId)) {
				return grid;
			}
		}

		// Search for the least-load grid
		Grid selectedGrid = null;
		for (String gridId : gridIds) {
			grid = costBasedTable.get(gridId);
			if (grid != null) {
				if (selectedGrid == null) {
					selectedGrid = grid;
				} else {
					selectedGrid = getLesserLoad(selectedGrid, grid);
				}
			}
		}
		return selectedGrid;
	}

	/**
	 * Returns the grid in the specified location that has the least routing
	 * cost. Each location may have one or more grids. It returns null if the
	 * specified location is invalid or has no grids running.
	 * 
	 * @param location
	 *            the location to search
	 */
	public Grid getLeastCostLocation(String location)
	{
		TreeMap<String, Grid> locationCostTable = locationBasedTable.get(location);
		if (location == null) {
			return null;
		}
		Map.Entry<String, Grid> entry = locationCostTable.firstEntry();
		if (entry == null) {
			return null;
		} else {
			return entry.getValue();
		}
	}

	/**
	 * Returns the grid selected from the specified list of grid IDs in the
	 * specified location that has the least routing cost. If the grid IDs are
	 * not specified then the least cost grid from the allowed grids maintained
	 * by the routing table is selected. If an invalid location is specified
	 * then the least-cost grid maintained by the routing table is returned.
	 * 
	 * @param location
	 *            The geographical location of the grid
	 * @param gridIds
	 *            Array of grids IDs from which the least-cost grid to be
	 *            selected. Any grids not in the specified location are ignored.
	 */
	public Grid getLeastCostLocation(String location, String... gridIds)
	{
		TreeMap<String, Grid> locationCostTable = locationBasedTable.get(location);
		if (locationCostTable == null) {
			return getLeastCostGrid(gridIds);
		}

		if (gridIds == null || gridIds.length == 0) {
			gridIds = locationCostTable.keySet().toArray(new String[locationCostTable.size()]);
			return getLeastCostGrid(gridIds);
		}

		Grid selectedGrid = null;
		if (gridIds != null) {
			for (String gridId : gridIds) {
				Grid grid = locationCostTable.get(gridId);
				if (grid != null) {
					if (selectedGrid == null || selectedGrid.cost > grid.cost) {
						selectedGrid = grid;
					}
				}
			}
		}
		if (selectedGrid == null) {
			selectedGrid = getLeastCostGrid();
		}
		return selectedGrid;
	}

	/**
	 * Returns the cost-based routing table. The returned map contains
	 * &lt;gridId, {@link Grid}&gt; entries.
	 */
	public Map<String, Grid> getCostBasedTable()
	{
		return costBasedTable;
	}

	/**
	 * Returns the location-based routing table sorted by the lateny costs. The
	 * returned map contains &lt;gridId, {@link Grid}&gt; entries.
	 * 
	 * @param location
	 *            Location
	 */
	public Map<String, Grid> getLocationCostBasedTable(String location)
	{
		return locationBasedTable.get(location);
	}

	/**
	 * Returns all of the locations in the location-based routing table.
	 * 
	 * @return
	 */
	public String[] getLocations()
	{
		return locationBasedTable.keySet().toArray(new String[locationBasedTable.size()]);
	}

	/**
	 * Refreshes the routing table by recomputing the routing table.
	 */
	public void refresh()
	{
		PadoClientManager sm = PadoClientManager.getPadoClientManager();

		// Retain only the live grids
		GridRoutingTable routingTable = GridRoutingTable.getGridRoutingTable(appId);
		Set<String> routingTableGridIdSet = routingTable.getGridIdSet();
		Set<String> liveGridIdSet = sm.getLiveGridIdSet(appId);
		ArrayList<String> removalList = new ArrayList(20);
		for (String gridId : routingTableGridIdSet) {
			if (liveGridIdSet.contains(gridId) == false) {
				removalList.add(gridId);
			}
		}
		for (String gridId : removalList) {
			routingTable.removeGrid(gridId);
		}

		// Update the routing table. Add new live ones as the cost is computed.
		Pado pado = sm.getPado(appId);
		if (pado != null) {
			IUtilBizLink utilBiz = (IUtilBizLink) pado.getCatalog().newInstance("com.netcrest.pado.biz.IUtilBiz");
			AppInfo appInfo = PadoClientManager.getPadoClientManager().getAppInfo(appId);
			if (appInfo != null) {
				// live grids
				String gridIds[] = appInfo.getGridIds();
				byte[] payload = new byte[100];
				for (String gridId : gridIds) {
					Grid grid = appInfo.getAllowedGrid(gridId);
					if (grid != null) {
						try {
							int latency = getAveragePingLatency(utilBiz, grid.getGridId(), payload);

							grid.setLatency(latency);
							ServerLoad serverLoad = utilBiz.getServerLoad();
							grid.setServerLoad(serverLoad);
							grid.setStatus(Status.LIVE);
							addGrid(grid);
						} catch (Exception ex) {
							// TODO: Exception may occur if the grid has been
							// detached but the grid info has not been properly
							// updated. Ignore it for now.
						}
					}
				}
			}
		}
	}

	/**
	 * Returns an average latency in microsec.
	 * 
	 * @param gridId
	 *            Grid id
	 * @param payload
	 *            Payload used in ping
	 */
	private int getAveragePingLatency(IUtilBizLink utilBiz, String gridId, byte[] payload)
	{
		utilBiz.getBizContext().reset();
		long deltas[] = new long[PING_SAMPLE_SIZE];
		for (int i = 0; i < 5; i++) {
			utilBiz.getBizContext().getGridContextClient().setGridIds(gridId);
			long startTime = System.nanoTime();
			byte[] retval = utilBiz.ping(payload);
			deltas[i] = System.nanoTime() - startTime;
		}

		// Drop the low and high then average the remaining
		Arrays.sort(deltas);
		int end = PING_SAMPLE_SIZE - 1;
		long sum = 0;
		for (int i = 1; i < end; i++) {
			sum += deltas[i];
		}
		long averageLatency = sum / (PING_SAMPLE_SIZE - 2);
		return (int) (averageLatency / 1000);
	}

	private Grid getLesserLoad(Grid grid1, Grid grid2)
	{
		Grid selectedGrid = grid1;
		if (grid1 == null) {
			selectedGrid = grid2;
		} else if (grid2 == null) {
			selectedGrid = grid1;
		} else if (grid1.serverLoad == null) {
			selectedGrid = grid2;
		} else if (grid2.serverLoad == null) {
			selectedGrid = grid1;
		} else if (grid1.serverLoad.getUsageLoad() > grid2.serverLoad.getUsageLoad()) {
			if (grid1.serverLoad.getGcPauses() >= grid2.serverLoad.getGcPauses()) {
				selectedGrid = grid2;
			} else if (grid1.serverLoad.getGcAverageTimeInMsec() >= grid2.serverLoad.getGcAverageTimeInMsec()) {
				selectedGrid = grid2;
			}
		}
		return selectedGrid;
	}

	@Override
	public String toString()
	{
		return "GridRoutingTable [costBaseTable=" + costBasedTable + ", locationBaseTable=" + locationBasedTable + "]";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeUTF(appId);
		out.writeObject(costBasedTable);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		appId = in.readUTF();
		costBasedTable = (TreeMap) in.readObject();
		// The following is redundant for costBasedTable
		// but reuse addGrid() for computing cost and building
		// costBasedSet, loadBasedSet, locationBasedTable
		for (Grid grid : costBasedTable.values()) {
			addGrid(grid);
		}
	}

	public static class Grid implements Comparable<Grid>, Externalizable, Cloneable
	{
		private static final long serialVersionUID = 1L;

		private String gridId;
		private String location;
		private PriorityType priorityType = PriorityType.COST;

		private int latency; // in usec
		private byte weight = DEFAULT_WEIGHT; // 1 - 10, the lower the better

		// cost is a metric determined by latency and weight. The lower the
		// better.
		private int cost;

		private ServerLoad serverLoad;

		private String description;

		private transient Status status = Status.INACCESSIBLE;

		public Grid()
		{
		}

		public Grid(String gridId)
		{
			this(gridId, DEFAULT_WEIGHT);
		}

		public Grid(String gridId, byte weight)
		{
			this(gridId, null, weight);
		}

		public Grid(String gridId, String location, byte weight)
		{
			this.gridId = gridId;
			this.location = location;
			if (weight <= 0) {
				this.weight = 1;
			} else if (weight >= 10) {
				this.weight = 10;
			} else {
				this.weight = weight;
			}
		}

		@Override
		public Object clone()
		{
			Grid grid = new Grid();
			grid.gridId = gridId;
			grid.location = location;
			grid.priorityType = priorityType;
			grid.latency = latency;
			grid.weight = weight;
			grid.serverLoad = serverLoad;
			grid.cost = cost;
			grid.description = description;
			grid.status = status;
			return grid;
		}

		private void computeCost()
		{
			cost = latency * weight;
		}

		public String getGridId()
		{
			return gridId;
		}

		public String getLocation()
		{
			return location;
		}

		public void setLocation(String location)
		{
			this.location = location;
		}

		public PriorityType getPriorityType()
		{
			return priorityType;
		}

		public void setPriorityType(PriorityType priorityType)
		{
			this.priorityType = priorityType;
		}

		public int getLatency()
		{
			return latency;
		}

		public void setLatency(int latency)
		{
			this.latency = latency;
		}

		public byte getWeight()
		{
			return weight;
		}

		public void setWeight(byte weight)
		{
			this.weight = weight;
		}

		public ServerLoad getServerLoad()
		{
			return serverLoad;
		}

		public void setServerLoad(ServerLoad serverLoad)
		{
			this.serverLoad = serverLoad;
		}

		public int getCost()
		{
			return cost;
		}

		public void setGridId(String gridId)
		{
			this.gridId = gridId;
		}

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public Status getStatus()
		{
			return status;
		}

		public void setStatus(Status status)
		{
			this.status = status;
		}

		@Override
		public String toString()
		{
			return "Grid [gridId=" + gridId + ", location=" + location + ", priorityType=" + priorityType + ", latency="
					+ latency + ", weight=" + weight + ", cost=" + cost + ", serverLoad=" + serverLoad
					+ ", description=" + description + ", status=" + status + "]";
		}

		@Override
		public int compareTo(Grid anotherGrid)
		{
			if (anotherGrid == null) {
				return -1;
			}
			return this.cost - anotherGrid.cost;
		}

		/**
		 * Reads the state of this object from the given <code>DataInput</code>.
		 * 
		 * @gfcodegen This code is generated by gfcodegen.
		 */
		public void fromData(DataInput input) throws IOException, ClassNotFoundException
		{

		}

		/**
		 * Writes the state of this object to the given <code>DataOutput</code>.
		 * 
		 * @gfcodegen This code is generated by gfcodegen.
		 */
		public void toData(DataOutput output) throws IOException
		{

		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException
		{
			out.writeUTF(gridId);
			if (location == null) {
				out.writeUTF("");
			} else {
				out.writeUTF(location);
			}
			out.writeInt(latency);
			out.writeByte(weight);
			out.writeObject(serverLoad);
			out.writeInt(priorityType.ordinal());
			if (description == null) {
				out.writeUTF("");
			} else {
				out.writeUTF(description);
			}
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
		{
			gridId = in.readUTF();
			location = in.readUTF();
			if (location.length() == 0) {
				location = null;
			}
			latency = in.readInt();
			weight = in.readByte();
			serverLoad = (ServerLoad) in.readObject();
			priorityType = PriorityType.values()[in.readInt()];
			description = in.readUTF();
			if (description.length() == 0) {
				description = null;
			}
		}
	}

	class GridLoadComparator implements Comparator<Grid>
	{
		@Override
		public int compare(Grid grid1, Grid grid2)
		{
			Grid selectedGrid = getLesserLoad(grid1, grid2);
			return selectedGrid == grid1 ? -1 : 1;
		}

		@Override
		public boolean equals(Object obj)
		{

			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GridLoadComparator other = (GridLoadComparator) obj;
			return this == other;
		}
	}
}
