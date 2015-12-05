package com.gemstone.gemfire.addon.dq;

/**
 * Enumeration <code>DQueueProcessingPolicy</code> encapsulates the processing
 * policies for <code>DQueueDispatcher</code> queues.
 */
public enum DQueueProcessingPolicy {
  ROUND_ROBIN, LARGEST, SMALLEST, FIFO, LIFO
};
