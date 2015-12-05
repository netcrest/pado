package com.gemstone.gemfire.addon.dq.internal;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;

@SuppressWarnings("serial")
public class DQueueDumpQueuesFunction implements Function {

  private final DQueuePullDispatcherImpl dispatcher;

  public static final String ID = "dump_queues";

  public DQueueDumpQueuesFunction(DQueuePullDispatcherImpl dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public void execute(FunctionContext context) {
    this.dispatcher.dumpQueues();
  }

  public String getId() {
    return ID;
  }

  public boolean hasResult() {
    return false;
  }

  public boolean optimizeForWrite() {
    return true;
  }

  public boolean isHA() {
    return false;
  }
}
