package com.gemstone.gemfire.addon.util;

public interface QueueHandler {

  public boolean enqueue(int queueIndex, Object obj);
}