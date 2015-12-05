package com.gemstone.gemfire.addon.dq;

/**
 * Interface <code>DQueueDispatcher</code> represents the server-side of the
 * <code>DQueue</code> functionality
 */
public interface DQueueDispatcher {

  /**
   * Start this <code>DQueueDispatcher</code>
   */
  public void start();
  
  /**
   * Enqueue the <code>DQueueEvent</code>
   * @param event the <code>DQueueEvent</code> to enqueue
   */
  public void enqueue(final DQueueEvent event);
}
