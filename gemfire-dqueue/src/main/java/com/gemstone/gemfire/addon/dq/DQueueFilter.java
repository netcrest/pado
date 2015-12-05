package com.gemstone.gemfire.addon.dq;

/**
 * Interface <code>DQueueFilter</code> provides a callback used to determine
 * both the total number of queues and the queue for a given type and value.
 */
public interface DQueueFilter {

	/**
   * Returns the queue associated with the given DQueueEvent. This method is
   * invoked when the <code>DQueueDispatcher</code> receives a new value. This
   * method should return a number between 0 and total queues -1.
   * @param event The DQueueEvent from which to derive the queue
	 * @return the queue number in which to insert the input value
	 */
  public int getQueue(DQueueEvent event);

  /**
   * Returns the total number of queues
   *
   * @return the total number of queues
   */
  public int getTotalQueues();
}
