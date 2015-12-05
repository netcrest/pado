package com.gemstone.gemfire.addon.dq;

/**
 * Class <code>DQueueException</code> encapsulates exceptions occurring
 * during <code>DQueue</code> operations.
 */
public class DQueueException extends RuntimeException {

  /**
   * Constructs a new instance of <code>DQueueException</code>.
   */
  public DQueueException() {
  }

  /**
   * Constructs an instance of <code>DQueueException</code> with the
   * specified detail message.
   * @param msg the detail message
   */
  public DQueueException(String msg) {
    super(msg);
  }

  /**
   * Constructs an instance of <code>DQueueException</code> with the
   * specified detail message and cause.
   * @param msg the detail message
   * @param cause the causal Throwable
   */
  public DQueueException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * Constructs an instance of <code>DQueueException</code> with the
   * specified cause.
   * @param cause the causal Throwable
   */
  public DQueueException(Throwable cause) {
    super(cause);
  }
}
