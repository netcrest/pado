package com.gemstone.gemfire.addon.dq.internal;

import com.gemstone.gemfire.cache.execute.FunctionContext;

@SuppressWarnings("serial")
public class DQueueDummyOfferFunction implements DQueueOfferFunctionBase {

  private String dqueueId;

  private final DQueueDispatcherStatistics statistics;

  public DQueueDummyOfferFunction(String dqueueId) {
    this(dqueueId, null);
  }

  public DQueueDummyOfferFunction(String dqueueId, DQueueDispatcherStatistics statistics) {
    this.dqueueId = dqueueId;
    this.statistics = statistics;
  }

  public boolean hasResult() {
    return true;
  }

  public boolean optimizeForWrite() {
    return true;
  }

  public boolean isHA() {
    return true;
  }

  public String getId() {
    return BASE_ID + this.dqueueId;
  }

  public void close() {
  }

  public void execute(FunctionContext context) {
    long start = this.statistics.startObjectOffer();
    if (hasResult()) {
      context.getResultSender().lastResult(null);
    }
    this.statistics.endObjectOffer(start);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer()
      .append("DQueueDummyOfferFunction[")
      .append("dqueueId=")
      .append(this.dqueueId)
      .append("]");
    return buffer.toString();
  }
}
