package com.gemstone.gemfire.addon.dq.internal;

import com.gemstone.gemfire.addon.dq.DQueueAttributes;
import com.gemstone.gemfire.addon.dq.DQueueEvent;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionService;

public class DQueuePullDispatcherImpl extends DQueueAbstractDispatcher {

  /**
   * Constructor. Creates a new <code>DQueue</code> using the input
   * attributes.
   *
   * @param cache
   *          The <code>Cache</code>
   * @param id
   *          The id
   * @param dqAttributes
   *          The <code>DQueueAttributes</code> used to configure this
   *          instance
   */
  public DQueuePullDispatcherImpl(Cache cache, String id,
      DQueueAttributes dqAttributes) {
    super(cache, id, dqAttributes);
  }

  @Override
  public void start() {
    this.queueHandler = new DQueueMemoryQueueHandler(this);
    if (this.cache.getLogger().fineEnabled()) {
      this.cache.getLogger().fine(this + ": Started " + this.queueHandler);
    }
  }

  @Override
  public void enqueue(DQueueEvent event) {
    this.queueHandler.enqueue(0, event);
  }

  protected Object poll(Object type) {
    return ((DQueueMemoryQueueHandler) this.queueHandler).poll(type);
  }
  
  protected Object poll(Object type, int count) {
    return ((DQueueMemoryQueueHandler) this.queueHandler).poll(type, count);
  }
  
  protected boolean take(Object type) {
    return ((DQueueMemoryQueueHandler) this.queueHandler).take(type);
  }
  
  protected boolean take(Object type, int count) {
    return ((DQueueMemoryQueueHandler) this.queueHandler).take(type, count);
  }
  
  protected Object peek(Object type) {
    return ((DQueueMemoryQueueHandler) this.queueHandler).peek(type);
  }
  
  protected Object peek(Object type, int count) {
    return ((DQueueMemoryQueueHandler) this.queueHandler).peek(type, count);
  }

  protected void dumpQueues() {
    ((DQueueMemoryQueueHandler) this.queueHandler).dumpQueues();
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer
      .append(getClass().getSimpleName())
      .append("[")
      .append("id=").append(this.id)
      .append("; attributes=").append(this.dqAttributes)
      .append("; handler=").append(this.queueHandler)
      .append("]");
    return buffer.toString();
  }

  protected void initialize() {
    // Create the region
    initializeRegion(new DQueueCacheListenerImpl(this));
    
    // Create and register the offer function
    Function offerFunction = new DQueueOfferFunction(this);
    if (!FunctionService.isRegistered(offerFunction.getId())) {
      FunctionService.registerFunction(offerFunction);
    }
    
    // Create and register the poll function
    Function pollFunction = new DQueuePollFunction(this);
    if (!FunctionService.isRegistered(pollFunction.getId())) {
      FunctionService.registerFunction(pollFunction);
    }
    
 // Create and register the take function
    Function takeFunction = new DQueueTakeFunction(this);
    if (!FunctionService.isRegistered(takeFunction.getId())) {
      FunctionService.registerFunction(takeFunction);
    }
    
    // Create and register the peek function
    Function peekFunction = new DQueuePeekFunction(this);
    if (!FunctionService.isRegistered(peekFunction.getId())) {
      FunctionService.registerFunction(peekFunction);
    }
    
    // Create and register the dump queues function
    Function dumpQueuesFunction = new DQueueDumpQueuesFunction(this);
    if (!FunctionService.isRegistered(dumpQueuesFunction.getId())) {
      FunctionService.registerFunction(dumpQueuesFunction);
    }

    // Start the dispatcher
    if (dqAttributes.getStartDispatcher()) {
      start();
    }
  }
}
