package com.gemstone.gemfire.addon.dq.internal;

import java.util.Set;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.internal.Assert;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;

@SuppressWarnings("serial")
public abstract class DQueueAbstractPullFunction implements Function {

  protected final Cache cache;

  protected final String dqueueId;

  protected final DQueuePullDispatcherImpl dispatcher;

  /**
   * Constructor for a DQueue client (either real client of peer client)
   * 
   * @param dqueueId
   *          The DQueue id
   * @param cache
   *          The <code>Cache</code>
   */
  public DQueueAbstractPullFunction(String dqueueId, Cache cache) {
    this.dqueueId = dqueueId;
    this.cache = cache;
    this.dispatcher = null;
  }

  /**
   * Constructor for a DQueue dispatcher (server)
   * 
   * @param dispatcher
   *          The DQueue dispatcher
   */
  public DQueueAbstractPullFunction(DQueuePullDispatcherImpl dispatcher) {
    this.dqueueId = dispatcher.getId();
    this.cache = dispatcher.getCache();
    this.dispatcher = dispatcher;
  }

  @SuppressWarnings("rawtypes")
  protected Object getType(RegionFunctionContext rfc) {
    Set keys = rfc.getFilter();
    Assert.assertTrue(keys != null && keys.size() == 1);
    Object type = keys.iterator().next();
    if (this.cache.getLogger().fineEnabled()) {
      this.cache.getLogger().fine(
          "Executing function " + getId() + " on type " + type);
    }
    return type;
  }

  @SuppressWarnings("rawtypes")
  protected void handleFailover(PartitionedRegion partitionedRegion, Object type) {
    // Get the bucket region for the type to verify that failover has been handled properly
    Region partition = partitionedRegion.getDataStore().getLocalBucketByKey(type);

    // Handle failover if necessary
    String partitionName = partition.getName();
    synchronized (partitionName) {
      if (this.dispatcher.getFailoverHandler().isFailover(partitionName)) {
        logInfo("Handling failover for bucket " + partitionName);
        this.dispatcher.getFailoverHandler().handleFailover(partitionedRegion, partition, partitionName);
      }
    }
  }

  private void logInfo(String message) {
    //System.out.println(message);
    this.cache.getLogger().info(message);
  }

  protected DQueueDispatcherStatistics getStatistics() {
    return this.dispatcher.getStatistics();
  }

  public boolean hasResult() {
    return true;
  }

  public boolean optimizeForWrite() {
    return true;
  }

  public boolean isHA() {
    return true;
  }
}
