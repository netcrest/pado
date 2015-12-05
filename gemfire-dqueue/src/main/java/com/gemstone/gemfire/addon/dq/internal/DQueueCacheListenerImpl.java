package com.gemstone.gemfire.addon.dq.internal;

import com.gemstone.gemfire.addon.dq.DQueueDispatcher;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.SerializedCacheValue;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;

public class DQueueCacheListenerImpl extends CacheListenerAdapter {

  private DQueueDispatcher dispatcher;

  public DQueueCacheListenerImpl(DQueueDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  public void afterCreate(EntryEvent event) {
    // Ignore the event if the callback argument contains a value
    // (it is a dummy entry)
    if(event.getCallbackArgument() != null) {
      return;
    }
    addEntry(event);
  }

  public void afterUpdate(EntryEvent event) {
    addEntry(event);
  }

  private void addEntry(EntryEvent event) {
    // Enqueue the entry in the dispatcher
    DQueueKey key = (DQueueKey) event.getKey();
    SerializedCacheValue value = event.getSerializedNewValue();
    this.dispatcher.enqueue(new DQueueEventImpl(key, value));
  }
}
