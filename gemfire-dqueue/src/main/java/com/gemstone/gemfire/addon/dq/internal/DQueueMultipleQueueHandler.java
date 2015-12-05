package com.gemstone.gemfire.addon.dq.internal;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.gemstone.gemfire.addon.dq.DQueueProcessingPolicy;
import com.gemstone.gemfire.addon.util.QueueDispatcherListener;
import com.gemstone.gemfire.addon.util.QueueHandler;
import com.gemstone.gemfire.cache.GemFireCache;

public class DQueueMultipleQueueHandler implements QueueHandler {

  private final GemFireCache cache;

  private final ConcurrentLinkedQueue[] queues;

  private final QueueDispatcherListener listener;

  private DQueueProcessingPolicy queueProcessingPolicy;

  private volatile boolean continueProcessing = true;

  private volatile boolean isWaitingOnEmptyQueues = false;

  private final ThreadGroup queueThreadGroup;

  private final Thread queueThread;

  private final Object emptyQueuesSync = new Object();

  public DQueueMultipleQueueHandler(GemFireCache cache, QueueDispatcherListener listener,
      int numQueues, DQueueProcessingPolicy queueProcessingPolicy) {
    this.cache = cache;
    this.listener = listener;
    this.queueProcessingPolicy = queueProcessingPolicy;
    this.queues = initializeQueues(numQueues);
    this.queueThreadGroup = createThreadGroup();
    this.queueThread = startQueueThread();
  }

  public boolean enqueue(int queueIndex, Object obj) {
    boolean success = this.queues[queueIndex].offer(obj);
    if (success) {
      //System.out.println("Enqueued into queue " + queueIndex + ": " + obj);
      synchronized (this.emptyQueuesSync) {
        if (this.isWaitingOnEmptyQueues) {
          this.isWaitingOnEmptyQueues = false;
          //System.out.println("Notifying");
          this.emptyQueuesSync.notifyAll();
        }
      }
    }
    return success;
  }

  private ConcurrentLinkedQueue[] initializeQueues(int numQueues) {
    ConcurrentLinkedQueue[] arr = new ConcurrentLinkedQueue[numQueues];
    for (int i=0; i<numQueues; i++) {
      arr[i] = new ConcurrentLinkedQueue();
    }
    return arr;
  }

  private ThreadGroup createThreadGroup() {
    return new ThreadGroup("DQueueMultipleQueueHandler Threads") {
      public void uncaughtException(Thread t, Throwable e) {
        System.err.println("Exception occurred in the DQueueMultipleQueueHandler Thread:" + e);
        e.printStackTrace();
      }
    };
  }

  private Thread startQueueThread() {
    Thread thread = new Thread(this.queueThreadGroup, new Runnable() {
      public void run() {
        runQueueThread();
      }
    }, "DQueueMultipleQueueHandler Thread");
    thread.setDaemon(true);
    thread.start();
    return thread;
  }

  private void runQueueThread() {
    int currentQueue = -1;
    int queuesEmpty = 0;
    while (this.continueProcessing) {
      try {
        currentQueue = getNextQueueIndex(currentQueue);
        ConcurrentLinkedQueue clq = this.queues[currentQueue];
        //System.out.println(this + ": Processing queue: " + clq + " containing " + clq.size() + " entries");
        Object obj = clq.poll();
        if (obj == null) {
          queuesEmpty++;
          if (queuesEmpty == this.queues.length) {
            // If all queues are empty, wait for notification that one contains entries
            synchronized (this.emptyQueuesSync) {
              //System.out.println("All queues are empty. Waiting for notification or timeout.");
              this.isWaitingOnEmptyQueues = true;
              this.emptyQueuesSync.wait(1000);
            }
            // Reset queuesEmpty
            queuesEmpty = 0;
          }
        }
        else {
          // Reset queuesEmpty
          queuesEmpty = 0;
          this.listener.objectDispatched(obj);
        }
      }
      catch (Exception e) {
        this.cache.getLogger().warning("DQueueMultipleQueueHandler thread caught the following exception and will exit: ", e);
        this.continueProcessing = false;
      }
    }
  }

  private int getNextQueueIndex(int currentQueue) {
    int nextQueue = 0;
    switch (queueProcessingPolicy) {
    case ROUND_ROBIN:
    case LARGEST:
    case SMALLEST:
    case FIFO:
    case LIFO:
      // Only round-robin policy is currently supported
      nextQueue = currentQueue + 1;
      if (nextQueue == this.queues.length) {
        nextQueue = 0;
      }
    }
    if (this.cache.getLogger().fineEnabled()) {
      this.cache.getLogger().fine(this + ": Using next queue index: " + nextQueue);
    }
    return nextQueue;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer()
      .append("DQueueMultipleQueueHandler[")
      .append("numQueues=")
      .append(this.queues.length)
      .append("]");
    return buffer.toString();
  }
}
