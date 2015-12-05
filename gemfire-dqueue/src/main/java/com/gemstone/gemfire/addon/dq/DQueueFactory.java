package com.gemstone.gemfire.addon.dq;

import com.gemstone.gemfire.addon.dq.internal.DQueueClientImpl;
import com.gemstone.gemfire.addon.dq.internal.DQueuePartitionedImpl;
import com.gemstone.gemfire.addon.dq.internal.DQueuePullDispatcherImpl;
import com.gemstone.gemfire.addon.dq.internal.DQueuePushDispatcherImpl;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;

/**
 * Class <code>DQueueFactory</code> is a factory that creates
 * <code>DQueueEntity</code> instances like <code>DQueue</code>s and
 * <code>DQueueDispatcher</code>s
 *
 * @see DQueue
 * @see DQueueDispatcher
 */
public class DQueueFactory {

  /**
   * The <code>Cache</code>
   */
  private static Cache cache;

  /**
   * Creates a <code>DQueue</code>.
   * @param id the id of the <code>DQueue</code>
   * @param attributes the <code>DQueueAttributes</code> from which to configure the <code>DQueue</code>
   * @return a <code>DQueue</code>
   */
  public static DQueue createDQueue(String id, DQueueAttributes attributes) {
    DQueue queue = null;
    if (attributes.isClient()) {
      queue = new DQueueClientImpl(getCache(), id, attributes);
    } else {
      if (attributes.isPartitioned()) {
        queue = new DQueuePartitionedImpl(getCache(), id, attributes);
      } else {
        throw new UnsupportedOperationException("Replicated DQueues are not supported");
      }
    }
    getCache().getLogger().info("Created " + queue);
    return queue;
  }

  /**
   * Creates a <code>DQueueDispatcher</code> that pushes
   * <code>DQueueEventBatches</code> to <code>DQueueListeners</code> for
   * processing.
   * 
   * @param id
   *          the id of the <code>DQueue</code>
   * @param attributes
   *          the <code>DQueueAttributes</code> from which to configure the
   *          <code>DQueueDispatcher</code>
   * @return a <code>DQueueDispatcher</code>
   */
  public static DQueueDispatcher createDQueuePushDispatcher(String id, DQueueAttributes attributes) {
    DQueueDispatcher dispatcher = new DQueuePushDispatcherImpl(getCache(), id, attributes);
    getCache().getLogger().info("Created " + dispatcher);
    return dispatcher;
  }

  /**
   * Creates a <code>DQueueDispatcher</code> that enqueues
   * <code>DQueueEvents</code> for processing.
   * 
   * @param id
   *          the id of the <code>DQueue</code>
   * @param attributes
   *          the <code>DQueueAttributes</code> from which to configure the
   *          <code>DQueueDispatcher</code>
   * @return a <code>DQueueDispatcher</code>
   */
  public static DQueueDispatcher createDQueuePullDispatcher(String id, DQueueAttributes attributes) {
    DQueueDispatcher dispatcher = new DQueuePullDispatcherImpl(getCache(), id, attributes);
    getCache().getLogger().info("Created " + dispatcher);
    return dispatcher;
  }

  private static Cache getCache() {
    if (cache == null) {
      cache = CacheFactory.getAnyInstance();
    }
    return cache;
  }

  private DQueueFactory() {}
}
