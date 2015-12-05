package com.gemstone.gemfire.addon.dq.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.gemstone.gemfire.addon.dq.DQueueAttributes;
import com.gemstone.gemfire.addon.dq.DQueueProcessingPolicy;
import com.gemstone.gemfire.addon.util.QueueDispatcherListener;
import com.gemstone.gemfire.addon.util.QueueHandler;
import com.gemstone.gemfire.cache.Cache;

public class DQueueExecutorQueueHandler implements QueueHandler {

  private final Cache cache;

  private final ExecutorService[] executors;

  private final QueueDispatcherListener listener;

  private final DQueueProcessingPolicy queueProcessingPolicy;

  private final boolean preserveOrder;

  private static final int MAX_THREADS = Integer.getInteger("dqueue.max_dispatch_threads", 16).intValue();

  private final int batchSize;

  private final int batchTimeInterval;

  private final List[] batches;

  private final long[] batchStartTimes;

  private final long[] latestMessageTimes;

  private final Timer timer;

  private final AtomicInteger numEventsReceived = new AtomicInteger();

  private final AtomicInteger numEventsDispatched = new AtomicInteger();

  public DQueueExecutorQueueHandler(DQueuePushDispatcherImpl listener) {
    this.listener = listener;
    this.cache = listener.getCache();
    DQueueAttributes attributes = listener.getAttributes();
    this.queueProcessingPolicy = attributes.getQueueProcessingPolicy();
    this.preserveOrder = attributes.getPreserveOrder();
    this.batchSize = attributes.getBatchSize();
    this.batchTimeInterval = attributes.getBatchTimeInterval();
    int numQueues = attributes.getFilter().getTotalQueues();
    this.batches = initializeBatches(numQueues);
    this.batchStartTimes = new long[numQueues];
    this.latestMessageTimes = new long[numQueues];
    this.timer = new Timer("Queue Cleanup Timer", true);
    addQueueCleanupTask();
    this.executors = initializeExecutors(numQueues);
  }

  public boolean enqueue(int queueIndex, Object obj) {
    List batch = this.batches[queueIndex];
    synchronized (batch) {
      // Set the latest message receipt time
      long currentTime = System.currentTimeMillis();
      this.latestMessageTimes[queueIndex] = currentTime;

      // If the batch is empty (new batch), set the start time for batch time interval checking
      if (batch.isEmpty()) {
        this.batchStartTimes[queueIndex] = currentTime;
      }

      // Add the object to the batch
      batch.add(obj);
      //System.out.println("DQueueExecutorQueueHandler received and added " + obj + " to queue " + queueIndex + " size=" + this.batches[queueIndex].size());
      //System.out.println("DQueueExecutorQueueHandler received and added event " + this.numEventsReceived.incrementAndGet() + " to queue " + queueIndex + " size=" + this.batches[queueIndex].size());
      //System.out.println("DQueueExecutorQueueHandler size check: " + (this.batches[queueIndex].size() >= this.batchSize));
      //System.out.println("DQueueExecutorQueueHandler batch start time for " + queueIndex + ": " + this.batchStartTimes[queueIndex]);
      //System.out.println("DQueueExecutorQueueHandler batch send time for " + queueIndex + ": " + (this.batchStartTimes[queueIndex]+this.batchTimeInterval));
      //System.out.println("DQueueExecutorQueueHandler current time for " + queueIndex + ": " + System.currentTimeMillis());
      //System.out.println("DQueueExecutorQueueHandler time check: " + (this.batchStartTimes[queueIndex]+this.batchTimeInterval < System.currentTimeMillis()));

      // Dispatch the batch if the size is greater than the batch size or the time period has expired
      boolean batchSizeLimitReached = batch.size() >= this.batchSize;
      boolean batchTimeIntervalReached = this.batchStartTimes[queueIndex]+this.batchTimeInterval < currentTime;
      if (batchSizeLimitReached || batchTimeIntervalReached) {
        //System.out.println("queueIndex: " + queueIndex + ", batchSizeLimitReached: " + batchSizeLimitReached + ", batchTimeIntervalReached: " + batchTimeIntervalReached);
        //System.out.println("DQueueExecutorQueueHandler dispatching batch of " + batch.size() + " events in queue " + queueIndex);
        dispatch(batch, queueIndex);
      }
      return true;
    }
  }

  private void dispatch(List batch, int queueIndex) {
    final List batchCopy = new ArrayList(batch);
    batch.clear();
    this.batchStartTimes[queueIndex] = 0;
    Runnable runnable = new Runnable() {
      public void run() {
        DQueueExecutorQueueHandler.this.listener.objectDispatched(batchCopy);
      }
    };
    //System.out.println("DQueueExecutorQueueHandler invoking executor " + queueIndex + " with " + batchCopy.size() + " events total dispatched=" + this.numEventsDispatched.addAndGet(batchCopy.size()));
    this.executors[queueIndex].execute(runnable);
  }

  private ExecutorService[] initializeExecutors(int numQueues) {
    ExecutorService[] arr = new ExecutorService[numQueues];
    for (int i=0; i<numQueues; i++) {
      arr[i] = this.preserveOrder
        ? Executors.newSingleThreadExecutor()
        : Executors.newFixedThreadPool(MAX_THREADS);
      //logCreation(i);
    }
    return arr;
  }

  private List[] initializeBatches(int numQueues) {
    List[] lists = new List[numQueues];
    for (int i=0; i<numQueues; i++) {
      lists[i] = new ArrayList(this.batchSize * 2);
    }
    return lists;
  }

  private void addQueueCleanupTask() {
    this.timer.schedule(new QueueCleanupTask(), 1000/*delay*/, 100/*period*/);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer()
      .append("DQueueExecutorQueueHandler[")
      .append("numExecutors=")
      .append(this.executors.length);
    if (!this.preserveOrder) {
      buffer
        .append("; maxThreads=")
        .append(MAX_THREADS);
    }
    buffer.append("]");
    return buffer.toString();
  }

  private void logCreation(int index) {
    StringBuffer buffer = new StringBuffer()
      .append("DQueueExecutorQueueHandler created ")
      .append(this.preserveOrder ? "ordered" : "unordered")
      .append(" queue ")
      .append(index);
    if (!this.preserveOrder) {
      buffer
        .append(" with ")
        .append(MAX_THREADS)
        .append(" threads");
    }
    System.out.println(buffer.toString());
  }

  private class QueueCleanupTask extends TimerTask {
    public void run() {
      for (int i=0; i<DQueueExecutorQueueHandler.this.batches.length; i++) {
        List batch = DQueueExecutorQueueHandler.this.batches[i];
        long latestMessageTime = DQueueExecutorQueueHandler.this.latestMessageTimes[i];
        synchronized (batch) {
          //System.out.println("Checking batch " + i + " of size: " + batch.size());
          if (batch.size() > 0 && latestMessageTime !=0 && latestMessageTime+100 < System.currentTimeMillis()) {
            //System.out.println(this + ": Dispatching " + batch.size() + " events to executor " + i);
            dispatch(batch, i);
          }
        }
      }
    }
  }
}
