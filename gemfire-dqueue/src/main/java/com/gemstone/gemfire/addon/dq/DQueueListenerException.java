package com.gemstone.gemfire.addon.dq;

/**
 * Class <code>DQueueListenerException</code> encapsulates exceptions occurring
 * during <code>DQueueListener</code> processing.
 */
public class DQueueListenerException extends RuntimeException {
  
  /**
   * Constructs a new instance of <code>DQueueListenerException</code>.
   */
  public DQueueListenerException() {
  }
  
  /**
   * Constructs an instance of <code>DQueueListenerException</code> with the 
   * specified detail message.
   * @param msg the detail message
   */
  public DQueueListenerException(String msg) {
    super(msg);
  }
  
  /**
   * Constructs an instance of <code>DQueueListenerException</code> with the 
   * specified detail message
   * and cause.
   * @param msg the detail message
   * @param cause the causal Throwable
   */
  public DQueueListenerException(String msg, Throwable cause) {
    super(msg, cause);
  }
  
  /**
   * Constructs an instance of <code>DQueueListenerException</code> with the 
   * specified cause.
   * @param cause the causal Throwable
   */
  public DQueueListenerException(Throwable cause) {
    super(cause);
  }
}
