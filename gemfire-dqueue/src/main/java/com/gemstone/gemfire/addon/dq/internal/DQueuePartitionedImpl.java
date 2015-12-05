package com.gemstone.gemfire.addon.dq.internal;

import com.gemstone.gemfire.addon.dq.DQueue;
import com.gemstone.gemfire.addon.dq.DQueueAttributes;
import com.gemstone.gemfire.cache.AttributesFactory;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.PartitionAttributesFactory;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;

/**
 * Class <code>DQueuePartitionedImpl</code> is an implementation of the
 * <code>DQueue</code> interface that uses a <code>PartitionedRegion</code>.
 */
@SuppressWarnings("deprecation")
public class DQueuePartitionedImpl extends DQueueAbstractClient {

  /**
   * Constructor. Creates a new <code>DQueue</code> using the input
   * attributes.
   *
   * @param cache
   *          The <code>Cache</code>
   * @param id
   *          The id
   * @param attributes
   *          The <code>DQueueAttributes</code> used to configure this
   *          instance
   */
  public DQueuePartitionedImpl(Cache cache, String id,
      DQueueAttributes attributes) {
    super(cache, id, attributes);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void initializeRegion() {
    // Retrieve the dq region
    String pRegionName = DQueue.NAME_PREFIX + this.id;
    PartitionedRegion region = (PartitionedRegion) this.cache.getRegion(pRegionName);
    boolean createdRegion = false;

    // Create the dq region if necessary
    if (region == null) {
      // Add the cache listener if necessary
      AttributesFactory factory = new AttributesFactory();

      // Define the partitioned attributes
      PartitionAttributesFactory pf = new PartitionAttributesFactory();
      pf.setRedundantCopies(1);
      pf.setTotalNumBuckets(this.dqAttributes.getBuckets());
      pf.setLocalMaxMemory(0);

      // Register the partition resolver
      pf.setPartitionResolver(new DQueuePartitionResolver());

      // Register the colocated region path
      pf.setColocatedWith(this.dqAttributes.getColocatedRegionFullPath());

      // Add the partition attributes to the attributes factory
      factory.setPartitionAttributes(pf.create());

      // Create partitioned region
      region = (PartitionedRegion) this.cache.createRegion(pRegionName, factory.create());
      createdRegion = true;
    }

    this.dqRegion = region;

    if (this.cache.getLogger().fineEnabled()) {
      this.cache.getLogger().fine(this + ": " + (createdRegion ? "Created" : "Retrieved") + " DQueue region: " + this.dqRegion.getFullPath());
    }
  }
}
