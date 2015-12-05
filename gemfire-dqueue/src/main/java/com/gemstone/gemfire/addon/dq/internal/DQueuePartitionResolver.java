package com.gemstone.gemfire.addon.dq.internal;

import java.io.Serializable;

import com.gemstone.gemfire.cache.EntryOperation;
import com.gemstone.gemfire.cache.PartitionResolver;

public class DQueuePartitionResolver implements PartitionResolver {

  public String getName() {
    return "DQueuePartitionResolver";
  }

  public Serializable getRoutingObject(EntryOperation opDetails) {
    // The key could be either:
    // - a type object <or>
    // - a DQueueKey
    // If it is a DQueueKey, return its type; otherwise just return it
    Object key =  opDetails.getKey();
    Serializable routingObject = (Serializable) (key instanceof DQueueKey ? ((DQueueKey) key).getType() : key);
    return routingObject;
  }

  public void close() {}
}
