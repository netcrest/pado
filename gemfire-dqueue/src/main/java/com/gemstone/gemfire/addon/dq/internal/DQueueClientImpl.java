package com.gemstone.gemfire.addon.dq.internal;

import com.gemstone.gemfire.addon.dq.DQueue;
import com.gemstone.gemfire.addon.dq.DQueueAttributes;
import com.gemstone.gemfire.cache.AttributesFactory;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;

/**
 * Class <code>DQueueClientImpl</code> is an implementation of the
 * <code>DQueue</code> interface that uses a client proxy <code>Region</code>.
 */
@SuppressWarnings("deprecation")
public class DQueueClientImpl extends DQueueAbstractClient {

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
  public DQueueClientImpl(Cache cache, String id,
      DQueueAttributes attributes) {
    super(cache, id, attributes);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void initializeRegion() {
    String regionName = DQueue.NAME_PREFIX + this.id;
    Region region = this.cache.getRegion(regionName);
    boolean createdRegion = false;
    
    // Create the dq region if necessary
    if (region == null) {
      GemFireCacheImpl gfci = (GemFireCacheImpl) this.cache;
      if (gfci.isClient()) {
        // can't use the createRegion method if this cache is a client-cache
        ClientRegionFactory factory = gfci.createClientRegionFactory(ClientRegionShortcut.PROXY);
        factory.setPoolName(this.dqAttributes.getPoolName());
        region = factory.create(regionName);
      } else {
        AttributesFactory factory = new AttributesFactory();
        factory.setDataPolicy(DataPolicy.EMPTY);
        factory.setScope(Scope.LOCAL);
        factory.setPoolName(this.dqAttributes.getPoolName());
        region = this.dqRegion = this.cache.createRegion(regionName, factory.create());
        createdRegion = true;
      }
    }    
    
    this.dqRegion = region;

    if (this.cache.getLogger().fineEnabled()) {
      this.cache.getLogger().fine(this + ": " + (createdRegion ? "Created" : "Retrieved") + " DQueue region: " + this.dqRegion.getFullPath());
    }
  }
}
