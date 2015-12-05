package com.gemstone.gemfire.addon.dq.internal;

import com.gemstone.gemfire.addon.dq.DQueue;
import com.gemstone.gemfire.addon.dq.DQueueAttributes;
import com.gemstone.gemfire.addon.dq.DQueueDispatcher;
import com.gemstone.gemfire.addon.util.QueueHandler;
import com.gemstone.gemfire.cache.AttributesFactory;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.PartitionAttributesFactory;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;

@SuppressWarnings("deprecation")
public abstract class DQueueAbstractDispatcher extends DQueueEntity implements
    DQueueDispatcher {

  protected final DQueueDispatcherStatistics statistics;

  protected final DQueueFailoverHandler failoverHandler;

  protected QueueHandler queueHandler;

  /**
   * Constructor. Creates a new <code>DQueue</code> using the input attributes.
   * 
   * @param cache
   *          The <code>Cache</code>
   * @param id
   *          The id
   * @param dqAttributes
   *          The <code>DQueueAttributes</code> used to configure this instance
   */
  public DQueueAbstractDispatcher(Cache cache, String id,
      DQueueAttributes dqAttributes) {
    super(cache, id, dqAttributes);
    this.statistics = new DQueueDispatcherStatistics(this.distributedSystem, id);
    this.failoverHandler = new DQueueFailoverHandler(id, cache, this.statistics);
    initialize();
  }

  protected DQueueDispatcherStatistics getStatistics() {
    return this.statistics;
  }
  
  protected DQueueFailoverHandler getFailoverHandler() {
    return this.failoverHandler;
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected boolean initializeRegion(CacheListener listener) {
    // Retrieve the dq region
    String pRegionName = DQueue.NAME_PREFIX + this.id;
    PartitionedRegion region = (PartitionedRegion) this.cache.getRegion(pRegionName);
    boolean createdRegion = false;

    // Create the dq region if necessary
    if (region == null) {
      AttributesFactory factory = new AttributesFactory();
      
      // Add the cache listener
      factory.addCacheListener(listener);

      // Define the partitioned attributes
      PartitionAttributesFactory pf = new PartitionAttributesFactory();
      pf.setRedundantCopies(1);
      pf.setTotalNumBuckets(this.dqAttributes.getBuckets());

      // Register the partition resolver
      pf.setPartitionResolver(new DQueuePartitionResolver());

      // Register the colocated region path
      pf.setColocatedWith(this.dqAttributes.getColocatedRegionFullPath());

      // Add the partition attributes to the attributes factory
      factory.setPartitionAttributes(pf.create());

      // Create partitioned region
      region = (PartitionedRegion) this.cache.createRegion(pRegionName, factory.create());

      createdRegion = true;
    } else {
      // A dqueuedispatcher cannot be created after a dqueue because the PR would have already been initialized
      // as an accessor.
      throw new IllegalStateException("The queue for " + this.id + " already exists. If this VM is to be both a DQueue and a DQueueDispatcher, the DQueueDispatcher must be created first.");
    }

    // Set the failover handler's parent region.
    this.failoverHandler.setParentRegion(region);

    this.dqRegion = region;

    if (this.cache.getLogger().fineEnabled()) {
      this.cache.getLogger().fine(this + ": " + (createdRegion ? "Created" : "Retrieved") + " DQueue region: " + this.dqRegion.getFullPath());
    }

    return createdRegion;
  }

  protected abstract void initialize();
}
