package com.gemstone.gemfire.addon.dq;

import java.util.Map;

public interface DQueueEventBatch {
  /**
   * Returns the number of events in this <code>DQueueEventBatch</code>
   *
   * @return the number of events in this <code>DQueueEventBatch</code>
   */
  public int getNumberOfEvents();

  /**
   * Returns the events in this <code>DQueueEventBatch</code>
   *
   * @return the events in this <code>DQueueEventBatch</code>
   */
  public Map getEvents();
}