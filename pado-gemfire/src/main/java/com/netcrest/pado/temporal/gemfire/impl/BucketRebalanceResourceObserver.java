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
package com.netcrest.pado.temporal.gemfire.impl;

import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.internal.cache.control.InternalResourceManager;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.ITemporalAdminBizLink;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.util.GridUtil;

/**
 * BucketRebalanceResourceObserver rebuilds temporal lists upon completion of
 * recovery or rebalancing.
 * 
 * @author dpark
 */
public class BucketRebalanceResourceObserver extends InternalResourceManager.ResourceObserverAdapter implements
		Declarable
{
	@Override
	public void init(Properties p)
	{
		InternalResourceManager.setResourceObserver(this);
	}

	/**
	 * Rebuilds temporal lists and Lucene indexes for all servers by invoking
	 * ILuceneBiz.
	 * 
	 * @param region
	 *            Temporal region
	 */
	@SuppressWarnings("rawtypes")
	private void rebuildTemporalLucene(Region<?, ?> region)
	{
		String gridPath = GridUtil.getChildPath(region.getFullPath());

		// Build temporal and Lucene indexes
		// Use the class name to avoid the dependency issue.
		ITemporalAdminBizLink temporalAdminBiz = (ITemporalAdminBizLink)PadoServerManager.getPadoServerManager().getCatalog()
				.newInstance("com.netcrest.pado.biz.ITemporalAdminBiz");
		temporalAdminBiz.setGridPath(gridPath);
		temporalAdminBiz.setEnabled(true /* enabled */, false /* spawnThread */);
	}

	/**
	 * Rebuild temporal and Lucene indexes locally.
	 * 
	 * @param region
	 */
	private void rebuildTemporalLuceneLocal(Region<?, ?> region)
	{
		TemporalManager tm = TemporalManager.getTemporalManager(region.getFullPath());
		if (tm == null) {
			return;
		}
		tm.setEnabled(true /* enabled */, true /* buildLucene */, false /* spawnThread */);
	}

	/**
	 * Rebuilds temporal and Lucence indexes for local data only. GemFire
	 * invokes this method for all servers for each region recovered.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void recoveryFinished(Region region)
	{
		// It seems this method never gets invoked for startup-recovery-delay>=0
		// (member-joined).
		// We rebuild temporal and Lucene indexes when a member-left event
		// is received in order to sync up with the primary promotion of
		// redundant data performed by GemFire. If member is joined then
		// we do nothing and rely on a manual rebalancing performed by
		// the operator, which would invoke the rebalancingFinished() method.

		// Partitioned regions should be configured as follows:
		// startup-recovery-delay="-1" Default: "0"
		// recovery-delay="<any value>" Default: "-1"

		Logger.info("Recovery finished and rebuilding temporal and Lucene indexes: " + region.getFullPath());
		rebuildTemporalLuceneLocal(region);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void rebalancingFinished(Region region)
	{
		// When a manual rebalancing is performed, this method eventually gets
		// invoked. We rebuild temporal & Lucene indexes here.
		Logger.info("Rebalancing finished and rebuilding temporal and Lucene indexes: " + region.getFullPath());
		rebuildTemporalLucene(region);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void movingPrimary(Region region, int bucketId, DistributedMember source, DistributedMember target)
	{
		// We don't care about moving primary buckets. We must wait till
		// all primary buckets are removed before rebuilding temporal & Lucene
		// indexes. This is done in recoveryFinished() or rebalancingFinished().

		// Logger.info("Moving primary bucket [bucketId=" + bucketId +
		// ", region=" + region.getFullPath());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void movingBucket(Region region, int bucketId, DistributedMember source, DistributedMember target)
	{
		// We don't care about moving buckets. We must wait till
		// all primary buckets are removed before rebuilding temporal & Lucene
		// indexes. This is done in recoveryFinished() or rebalancingFinished().

		// Logger.info("Moving bucket [bucketId=" + bucketId + ", region=" +
		// region.getFullPath());
	}
}