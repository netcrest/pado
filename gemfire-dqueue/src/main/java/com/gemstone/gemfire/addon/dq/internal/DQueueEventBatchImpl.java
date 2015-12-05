package com.gemstone.gemfire.addon.dq.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.gemstone.gemfire.addon.dq.DQueueEvent;
import com.gemstone.gemfire.addon.dq.DQueueEventBatch;
import com.gemstone.gemfire.cache.Region;

@SuppressWarnings("rawtypes")
public class DQueueEventBatchImpl implements DQueueEventBatch {

  private final Map events;

  private final int numEvents;

  public DQueueEventBatchImpl(List events) {
    this.numEvents = events.size();
    this.events = initializeAllEvents(events);
  }

  public int getNumberOfEvents() {
    return this.numEvents;
  }

  public Map getEvents() {
    return this.events;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer
      .append("DQueueEventBatchImpl[")
      .append("inumEventsd=")
      .append(this.numEvents)
      .append("; events=")
      .append(this.events)
      .append("]");
    return buffer.toString();
  }

  protected void setPossibleDuplicate(boolean possibleDuplicate) {
    for (Iterator i = this.events.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      List eventsByType = (List) entry.getValue();
      for (Iterator j = eventsByType.iterator(); j.hasNext();) {
        DQueueEventImpl event = (DQueueEventImpl) j.next();
        event.setPossibleDuplicate(possibleDuplicate);
      }
    }
  }

  protected void removeEvents(Region region) {
    for (Iterator i = this.events.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      List eventsByType = (List) entry.getValue();
      for (Iterator j = eventsByType.iterator(); j.hasNext();) {
        DQueueEventImpl event = (DQueueEventImpl) j.next();
        region.remove(event.getKey());
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Map initializeAllEvents(List events) {
    Map batchEvents = new HashMap();
    for (Iterator i = events.iterator(); i.hasNext();) {
      DQueueEvent event = (DQueueEvent) i.next();
      Object type = event.getType();
      List eventsByType = (List) batchEvents.get(type);
      if (eventsByType == null) {
        eventsByType = new ArrayList();
        batchEvents.put(type, eventsByType);
      }
      eventsByType.add(event);
    }
    return batchEvents;
  }
}