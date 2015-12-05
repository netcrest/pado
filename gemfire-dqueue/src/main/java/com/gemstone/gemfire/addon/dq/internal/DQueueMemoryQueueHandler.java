package com.gemstone.gemfire.addon.dq.internal;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import com.gemstone.gemfire.addon.dq.DQueueEvent;
import com.gemstone.gemfire.addon.util.QueueHandler;
import com.gemstone.gemfire.cache.Cache;

public class DQueueMemoryQueueHandler implements QueueHandler {

  private final ConcurrentHashMap<Object,Deque<DQueueEvent>> queues;
  
  private final Cache cache;

  public DQueueMemoryQueueHandler(DQueuePullDispatcherImpl dispatcher) {
    this.cache = dispatcher.getCache();
    this.queues = new ConcurrentHashMap<Object,Deque<DQueueEvent>>();
  }

  @Override
  public boolean enqueue(int queueIndex, Object obj) {
    DQueueEvent event = (DQueueEvent) obj;
    if (this.cache.getLogger().fineEnabled()) {
      this.cache.getLogger().fine(this + ": Queueing event: " + event);
    }
    
    // Get the Queue
    Deque<DQueueEvent> queue = this.queues.get(event.getType());
    if (queue == null) {
      Deque<DQueueEvent> newQueue = new LinkedBlockingDeque<DQueueEvent>();
      queue = this.queues.putIfAbsent(event.getType(), newQueue);
      if (queue == null) {
        queue = newQueue;
      }
    }
    
    // Add the event to the queue
    queue.add(event);
    if (this.cache.getLogger().fineEnabled()) {
      this.cache.getLogger().fine(this + ": Queue for " + event.getType() + " contains  " + queue.size() + " elements");
    }
    
    return true;
  }

  protected void dumpQueues() {
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<Object,Deque<DQueueEvent>> entry : this.queues.entrySet()) {
      builder
        .append("\nQueue for ")
        .append(entry.getKey())
        .append(" contains the following ")
        .append(entry.getValue().size())
        .append(" elements:");
      for (DQueueEvent event : entry.getValue()) {
        builder.append("\n\t").append(event);
      }
      builder.append('\n');
    }
    System.out.println(builder.toString());
    this.cache.getLogger().info(builder.toString());
  }
  
  protected Object poll(Object type) {
    Deque<DQueueEvent> queue = this.queues.get(type);
    DQueueEvent result = null;
    if (queue != null) {
      result = queue.poll();
    }
    return result;
  }
  
  protected List<Object> poll(Object type, int count) {
    Deque<DQueueEvent> queue = this.queues.get(type);
    List<Object> list = null;
    if (queue != null) {
      list = new ArrayList(count);
      while (list.size() < count) {
    	DQueueEvent event = queue.poll();
    	if (event == null) {
    	  break;
    	}
    	list.add(event);
      }
    }
    return list;
  }

  protected boolean take(Object type) {
    Deque<DQueueEvent> queue = this.queues.get(type);
    DQueueEvent result = null;
    if (queue != null) {
      result = queue.poll();
    }
    return result != null;
  }
  
  protected boolean take(Object type, int count) {
    Deque<DQueueEvent> queue = this.queues.get(type);
    int pollCount = 0;
    if (queue != null) {
      while (pollCount < count) {
    	DQueueEvent event = queue.poll();
    	if (event == null) {
    	  break;
    	}
    	pollCount++;
      }
    }
    return pollCount > 0 && count > 0;
  }
  
  protected DQueueEvent peek(Object type) {
    Deque<DQueueEvent> queue = this.queues.get(type);
    DQueueEvent result = null;
    if (queue != null) {
      result = queue.peek();
    }
    return result;
  }
  
  protected List<Object> peek(Object type, int count) {
    Deque<DQueueEvent> queue = this.queues.get(type);
    List<Object> list = null;
    if (queue != null) {
      java.util.Iterator<DQueueEvent> iterator = queue.iterator();
      list = new ArrayList(count);
      while (list.size() < count && iterator.hasNext()) {
		  list.add(iterator.next());
	  }
    }
    return list;
  }
}
