package com.gemstone.gemfire.addon.dq;

/**
 * Interface <code>DQueueListener</code> provides a callback used to notify the
 * application that a new <code>DQueueEventBatch</code> of events has been
 * received by the <code>DQueueDispatcher</code>.
 */
public interface DQueueListener {

  /**
   * Invoked by the <code>DQueueDispatcher</code> when a new
   * <code>DQueueEventBatch</code> has been received.
   *
   * @param batch The <code>DQueueEventBatch</code> of events
   * @throws DQueueListenerException if thrown will re-attempt the dispatch
   */
  public void dispatch(DQueueEventBatch batch) throws DQueueListenerException;

  /**
   * Invoked by the <code>DQueueDispatcher</code> when an exception occurs
   * while dispatching a <code>DQueueEventBatch</code>.
   *
   * @param batch The <code>DQueueEventBatch</code> of events
   */
  public void dispatchFailed(DQueueEventBatch batch);
}
