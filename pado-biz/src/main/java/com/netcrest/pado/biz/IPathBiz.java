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
package com.netcrest.pado.biz;

import java.util.List;
import java.util.Set;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.data.Entry;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.IncompatibleTypeException;
import com.netcrest.pado.exception.NestedPathExistsException;
import com.netcrest.pado.info.PathInfo;

/**
 * IPathBiz manages individual grid paths.
 * <p>
 * <b>Arguments: None</b>
 * <p>
 * 
 * @author dpark
 * 
 */
@BizClass(name = "IPathBiz")
public interface IPathBiz extends IBiz
{
	public static enum PathType
	{
		LOCAL, LOCAL_PERSISTENT, LOCAL_OVERFLOW, LOCAL_PERSISTENT_OVERFLOW, NORMAL, PRELOADED, GLOBAL, REPLICATE, REPLICATE_PERSISTENT, REPLICATE_OVERFLOW, REPLICATE_PERSISTENT_OVERFLOW, REPLICATE_EMPTY, PARTITION, PARTITION_PERSISTENT, PARTITION_PERSISTENT_OVERFLOW, PARTITION_OVERFLOW, TEMPORAL, TEMPORAL_PERSISTENT, TEMPORAL_PERSISTENT_OVERFLOW, TEMPORAL_OVERFLOW, TEMPORAL_LUCENE, TEMPORAL_LUCENE_PERSISTENT, TEMPORAL_LUCENE_PERSISTENT_OVERFLOW, TEMPORAL_LUCENE_OVERFLOW, NOT_SUPPORTED
	};

	/**
	 * Clears the path. The behavior of this method is defined by the underlying
	 * data grid product. For example, GemFire clears only non-partitioned
	 * region.
	 * 
	 * @param gridId
	 *            null to clear the path in the default grid
	 * @param path
	 *            Grid path
	 */
	void clear(String gridId, String gridPath);

	/**
	 * Clears the path forcefully if specified.
	 * 
	 * @param gridId
	 *            null to clear the path in the default grid
	 * @param path
	 *            Grid path.
	 * @param force
	 *            If true, clears the map regardless of the underlying data grid
	 *            restrictions if any. For example, it clears GemFire
	 *            partitioned region.
	 */
	@BizMethod
	@OnServer(broadcast = true)
	void clear(String gridId, String path, boolean force);

	/**
	 * Creates the specified grid path.
	 * 
	 * @param gridId
	 *            Grid ID. If null, the default grid ID is assigned.
	 * @param gridPath
	 *            Grid path
	 * @param pathType
	 *            Path type. Default: {@link PathType#TEMPORAL}
	 * @param recursive
	 *            Recursively create nested paths if they do not exist. Nested
	 *            paths are created with the REPLICATE path type.
	 * @return true if the path is successfully created or already exists.
	 */
	boolean createPath(String gridId, String gridPath, PathType pathType, boolean recursive);

	/**
	 * Creates the specified path with the path attributes identified by the
	 * specified reference ID.
	 * 
	 * @param gridId
	 *            Grid ID. If null, the default grid ID is assigned.
	 * @param gridPath
	 *            Grid path
	 * @param refid
	 *            Path attributes reference ID. This is typically defined in the
	 *            server configuration file (server.xml). For GemFire, this is
	 *            equivalent to refid of region-attributes.
	 * @param isTemporal
	 *            true to configure temporal settings for the specified path.
	 * @param isLuceneDynamic
	 *            true to dynamically update Lucene indexes. Note that dynamic
	 *            indexing is very expensive and will degrade write performance.
	 * @param recursive
	 *            Recursively create nested paths if they do not exist. Nested
	 *            paths are created with the REPLICATE path type.
	 * @return true if the path is successfully created or already exists.
	 */
	@BizMethod
	@OnServer(broadcast = true)
	boolean createPath(String gridId, String gridPath, String refid, boolean isTemporal, boolean isLuceneDynamic,
			boolean recursive);

	/**
	 * Creates the specified path.
	 * 
	 * @param gridId
	 *            Grid ID. If null, the default grid ID is assigned.
	 * @param gridPath
	 *            Grid path
	 * @param pathType
	 *            Path type. Default: {@link PathType#TEMPORAL}
	 * @param diskStoreName
	 *            Disk store name defined in the grid. If null or undefined,
	 *            then the default disk store name is used. This applies to
	 *            persistent paths only.
	 * @param gatewaySenderIds
	 *            Comma-separated gateway sender IDs. If sender IDs are valid
	 *            then gateways are enabled for the specified path. If null,
	 *            empty string, or invalid then gateways are disabled for the
	 *            specified path.
	 * @param colocatedWithGridPath
	 *            Colocated grid path. If null, then ignored. This is only valid
	 *            for partitioned an temporal paths. Default: none
	 * @param redundantCopies
	 *            Number of redundant temporal object copies. This is only valid
	 *            for partitioned and temporal paths. Default: 1
	 * @param totalBucketCount
	 *            Total number of buckets. This is only valid for partitioned
	 *            and temporal paths. Default: 113
	 * @param recursive
	 *            Recursively create nested paths if they do not exist. Nested
	 *            paths are created with the REPLICATE path type.
	 * @return true if the path is successfully created or already exists.
	 */
	@BizMethod
	@OnServer(broadcast = true)
	boolean __createPath(String gridId, String gridPath, String pathType, String diskStoreName,
			String gatewaySenderIds, String colocatedWithGridPath, int redundantCopies, int totalBucketCount,
			boolean recursive);

	/**
	 * Creates the specified path.
	 * 
	 * @param gridId
	 *            Grid ID. If null, the default grid ID is assigned.
	 * @param gridPath
	 *            Grid path
	 * @param pathType
	 *            Path type. Default: {@link PathType#TEMPORAL}
	 * @param diskStoreName
	 *            Disk store name defined in the grid. If null or undefined,
	 *            then the default disk store name is used. This applies to
	 *            persistent paths only.
	 * @param colocatedWithGridPath
	 *            Colocated grid path. If null, then ignored. This is only valid
	 *            for partitioned an temporal paths. Default: none
	 * @param redundantCopies
	 *            Number of redundant temporal object copies. This is only valid
	 *            for partitioned and temporal paths. Default: 1
	 * @param totalBucketCount
	 *            Total number of buckets. This is only valid for partitioned
	 *            and temporal paths. Default: 113
	 * @param gatewaySenderIds
	 *            Comma-separated gateway sender IDs. If sender IDs are valid
	 *            then gateways are enabled for the specified path. If null,
	 *            empty string, or invalid then gateways are disabled for the
	 *            specified path.
	 * @param recursive
	 *            Recursively create nested paths if they do not exist. Nested
	 *            paths are created with the REPLICATE path type.
	 * @return true if the path is successfully created or already exists.
	 */
	boolean createPath(String gridId, String gridPath, PathType pathType, String diskStoreName,
			String gatewaySenderIds, String colocatedWithGridPath, int redundantCopies, int totalBucketCount,
			boolean recursive);

	/**
	 * Removes the specified grid path from all of the servers in the specified
	 * grid.
	 * 
	 * @param gridId
	 *            null to remove the path in the default grid.
	 * @param gridPath
	 *            Grid path
	 * @param recursive
	 *            true to recursively remove all nested paths including the
	 *            specified grid path.
	 * @throws NestedPathExistsException
	 */
	@BizMethod
	@OnServer(broadcast = true)
	void remove(String gridId, String gridPath, boolean recursive) throws NestedPathExistsException;

	/**
	 * Returns all temporal paths defined in the specified grid. Note that this
	 * method targets only a single server. It is assumed all servers in the
	 * grid define the identical temporal paths.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @return A non-null list of temporal paths.
	 */
	@BizMethod
	@OnServer
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	List<PathInfo> getAllTemporalPathInfoList(String gridId);

	/**
	 * Returns a list of path sizes from all servers.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @param gridPath
	 *            Grid path
	 */
	@BizMethod
	@OnServer(broadcast = true)
	List<Integer> getSize(String gridId, String gridPath);

	/**
	 * Copies the specified soruceGridPath to the specified targetGridPath. If
	 * the targetGridPath does not exist then it is created with the same path
	 * type as the sourceGridPath.
	 * 
	 * @param sourceGridPath
	 *            Copy-from source grid path.
	 * @param targetGridPath
	 *            Copy-to target grid path.
	 */
	@BizMethod
	@OnServer(broadcast = true)
	public void copy(String gridId, String sourceGridPath, String targetGridPath) throws IncompatibleTypeException;

	/**
	 * Returns the path type of the specified grid path.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @param gridPath
	 *            Grid path
	 * @return null if the specified grid path does not exist.
	 */
	@BizMethod
	@OnServer
	public PathType getPathType(String gridId, String gridPath);

	/**
	 * Returns true if the specified grid path exists in the specified grid ID.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @param gridPath
	 *            Grid path
	 */
	@BizMethod
	@OnServer
	public boolean exists(String gridId, String gridPath);
	
	/**
	 * Returns an entry randomly selected from the specified grid path.
	  * @param gridId
	 *            Grid ID
	 * @param gridPath
	 *            Grid path
	 * @return null if the path is undefined or empty.
	 */
	@SuppressWarnings("rawtypes")
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass="com.netcrest.pado.biz.collector.SingleObjectCollector")
	public Entry getRandomEntry(String gridId, String gridPath);

	/**
	 * Rebalances the specified grid paths. Non-partitioned paths are ignored.
	 * If both includeGraidPathSet and excludePathSet are null, then all of
	 * partitioned paths are rebalanced.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @param includeGridPathSet
	 *            Set of paths to rebalance. If null, then all partitioned
	 *            paths are rebalanced.
	 * @param excludeGridPathSet
	 *            Set of paths to exclude from rebalancing. If null, none 
	 *            of the partitioned paths are excluded.
	 * @param timeout
	 *            Time out in millisecond. Once rebalancing starts, it will be
	 *            completed in background regardless of timeout.
	 * @param isSimulate
	 *            true to simulate rebalancing, false to rebalance.
	 * @return Results of rebalance execution,
	 */
	@BizMethod
	@OnServer
	@SuppressWarnings("rawtypes")
	public JsonLite rebalance(String gridId, Set<String> includeGridPathSet, Set<String> excludeGridPathSet,
			long timeout, boolean isSimulate);
	
//	@BizMethod
//	@OnServer(broadcast=true)
//	public void hintPrimaryKeys(String gridId, String gridPath);
}
