package com.gemstone.gemfire.addon.dq.internal;

import com.gemstone.gemfire.addon.dq.DQueueEvent;
import com.gemstone.gemfire.cache.SerializedCacheValue;

@SuppressWarnings("rawtypes")
public class DQueueEventImpl implements DQueueEvent {

  private final DQueueKey key;
  private final SerializedCacheValue value;

  protected DQueueEventImpl(DQueueKey key, SerializedCacheValue value) {
    this.key = key;
    this.value = value;
  }

  public Object getValue() {
    return this.value.getDeserializedValue();
  }

  public String getMemberId() {
    return this.key.getMemberId();
  }

  public Object getType() {
    return this.key.getType();
  }

  public int getSequenceNumber() {
    return this.key.getSequenceNumber();
  }

  public boolean getPossibleDuplicate() {
    return this.key.getPossibleDuplicate();
  }

  protected void setPossibleDuplicate(boolean possibleDuplicate) {
    this.key.setPossibleDuplicate(possibleDuplicate);
  }

  protected DQueueKey getKey() {
    return this.key;
  }
  
  protected Object getRawValue() {
    return this.value;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer
      .append("DQueueEventImpl[")
      .append("memberId=").append(getMemberId())
      .append("; type=").append(getType())
      .append("; sequenceNumber=").append(getSequenceNumber())
      .append("; possibleDuplicate=").append(getPossibleDuplicate())
      .append("; value=").append(this.value)
      .append("]");
    return buffer.toString();
  }
}
