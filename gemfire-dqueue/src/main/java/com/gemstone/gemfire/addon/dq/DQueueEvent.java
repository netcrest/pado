package com.gemstone.gemfire.addon.dq;

/**
 * Interface <code>DQueueEvent</code> is an event delivered to a
 * <code>DQueueListener</code>s.
 */
public interface DQueueEvent {

  /**
   * Returns the value associated with this <code>DQueueEvent</code>.
   * @return the value associated with this <code>DQueueEvent</code>
   */
  public Object getValue();

  /**
   * Returns the type associated with this <code>DQueueEvent</code>.
   * @return the type associated with this <code>DQueueEvent</code>
   */
  public Object getType();

  /**
   * Returns the sequence number associated with this <code>DQueueEvent</code>.
   * @return the sequence number associated with this <code>DQueueEvent</code>
   */
  public int getSequenceNumber();

  /**
   * Returns the id of the GemFire member that generated this
   * <code>DQueueEvent</code>.
   * @return the id of the GemFire member that generated this
   * <code>DQueueEvent</code>
   */
  public String getMemberId();

  /**
   * Returns whether this <code>DQueueEvent</code> is a possible duplicate.
   * @return whether this <code>DQueueEvent</code> is a possible duplicate
   */
  public boolean getPossibleDuplicate();
}
