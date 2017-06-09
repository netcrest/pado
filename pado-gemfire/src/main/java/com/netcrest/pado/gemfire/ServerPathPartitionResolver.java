package com.netcrest.pado.gemfire;

import java.util.Properties;
import java.util.Set;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryOperation;
import com.gemstone.gemfire.cache.FixedPartitionAttributes;
import com.gemstone.gemfire.cache.FixedPartitionResolver;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.cache.PartitionedRegionHelper;
import com.netcrest.pado.server.PadoServerManager;

/**
 * ServerPathPartitionResolver resolves partitions for the __pado/server path
 * which is used to obtain the routing key required by all Pado IBiz operations.
 * It uses the server ID to fix the partition per server (data node).
 * 
 * @author dpark
 *
 */
@SuppressWarnings("rawtypes")
public class ServerPathPartitionResolver implements FixedPartitionResolver, Declarable
{
	private int bucketId = -1;

	public int getBucketId()
	{
		return bucketId;
	}

	public void setBucketId(int bucketId)
	{
		this.bucketId = bucketId;
	}

	@Override
	public void close()
	{
	}

	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}

	@Override
	public void init(Properties props)
	{
		// Required even if not needed - GemFire bug
	}

	@Override
	public Object getRoutingObject(EntryOperation opDetails)
	{
		return opDetails.getKey();
	}

	@Override
	public String getPartitionName(EntryOperation opDetails, Set targetPartitions)
	{
		if (bucketId == -1) {
			return PadoServerManager.getPadoServerManager().getServerName();
		} else {
			Object key = opDetails.getKey();
			int id;
			if (key instanceof Integer) {
				id = (Integer)key;
			} else {
				id = bucketId;
			}
			FixedPartitionAttributes fpa = PartitionedRegionHelper
					.getFixedPartitionAttributesForBucket((PartitionedRegion) opDetails.getRegion(), id);
			return fpa.getPartitionName();
		}
	}
}
