package com.gemstone.gemfire.addon.dq.internal;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.Operation;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.internal.cache.BucketRegion;
import com.gemstone.gemfire.internal.cache.EntryEventImpl;
import com.gemstone.gemfire.internal.cache.LocalRegion;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.cache.RegionEntry;
import com.gemstone.gemfire.internal.cache.partitioned.RegionAdvisor;

public class DQueueFailoverHandler {

  private final Map<String, String> bucketsProcessed;

  private final Map<String, Integer> sequenceNumbers;

  @SuppressWarnings("unused")
  private final Thread failoverThread;

  private final ThreadGroup threadGroup;

  private volatile boolean continueProcessing = true;

  private Cache cache;

  private final DQueueDispatcherStatistics statistics;

  private LogWriter logger;

  private PartitionedRegion parentRegion;

  public DQueueFailoverHandler(String dqueueId, Cache cache,
      DQueueDispatcherStatistics statistics) {
    this.cache = cache;
    this.statistics = statistics;
    this.logger = cache.getLogger();
    this.bucketsProcessed = new ConcurrentHashMap<String, String>();
    this.sequenceNumbers = new ConcurrentHashMap<String, Integer>();
    this.threadGroup = createThreadGroup();
    this.failoverThread = startFailoverThread();
  }

  /**
   * Verify if this either a failover case or the first time this bucket is
   * being processed. This method modifies the value of bucketsProcessed, so if
   * it is called 2x in a row for the same partitionName, it will return
   * different results.
   * 
   * This method should be called under synchronization.
   */
  @SuppressWarnings("rawtypes")
  protected boolean isFailover(String partitionName) {
    boolean isFailover = false;
    if (!this.bucketsProcessed.containsKey(partitionName)) {
      this.bucketsProcessed.put(partitionName, partitionName);
      isFailover = true;
    }
    return isFailover;
  }

  public void close() {
    // Close the failover thread
    this.continueProcessing = false;
  }

  public void setParentRegion(PartitionedRegion parentRegion) {
    this.parentRegion = parentRegion;
    this.statistics.setTotalBuckets(parentRegion.getTotalNumberOfBuckets());
  }

  protected int getAndIncrementSequenceNumber(String partitionName) {
    // Get the current sequence number for the partition
    int sequenceNumber = getSequenceNumbers().get(partitionName);
    if (this.cache.getLogger().fineEnabled()) {
      this.cache.getLogger().fine(
          this + ": Current sequence number for partition " + partitionName
              + ": " + sequenceNumber);
    }

    // Increment the current sequence number to the next sequence number
    getSequenceNumbers().put(partitionName, ++sequenceNumber);
    if (this.cache.getLogger().fineEnabled()) {
      this.cache.getLogger().fine(
          this + ": Next sequence number for partition " + partitionName + ": "
              + getSequenceNumbers().get(partitionName));
    }

    // Return the next sequence number
    return sequenceNumber;
  }

  private Map<String, Integer> getSequenceNumbers() {
    return this.sequenceNumbers;
  }

  private ThreadGroup createThreadGroup() {
    return new ThreadGroup("DQueue Failover Threads") {
      public void uncaughtException(Thread t, Throwable e) {
        System.err.println("Exception occurred in the Failover Thread:" + e);
        e.printStackTrace();
      }
    };
  }

  private Thread startFailoverThread() {
    Thread thread = new Thread(this.threadGroup, new Runnable() {
      public void run() {
        runFailoverThread();
      }
    }, "DQueue Failover Thread");
    thread.setDaemon(true);
    thread.start();
    return thread;
  }

  private void runFailoverThread() {
    Set<Integer> primaryBucketsSeen = new HashSet<Integer>();
    Set<Integer> secondaryBucketsSeen = new HashSet<Integer>();
    LogWriter logger = this.cache.getLogger();
    while (this.continueProcessing) {
      try {
        Thread.sleep(100);
        if (this.parentRegion != null) {
          RegionAdvisor regionAdvisor = this.parentRegion.getRegionAdvisor();
          // Check all bucket regions
          for (BucketRegion br : this.parentRegion.getDataStore()
              .getAllLocalBucketRegions()) {
            int bucketId = br.getId();
            String partitionName = br.getName();
            if (logger.fineEnabled()) {
              logger.fine(this + ": Checking bucket " + bucketId + " status");
            }
            if (primaryBucketsSeen.contains(bucketId)) {
              if (logger.fineEnabled()) {
                logger
                    .fine(this + ": This VM is already the primary for bucket "
                        + bucketId);
              }
              if (!regionAdvisor.isPrimaryForBucket(bucketId)) {
                if (logger.fineEnabled()) {
                  logger.fine(this + ": Bucket " + bucketId
                      + " was primary and is now secondary in this VM");
                }

                // Remove the bucket from the primaryBucketsSeen set
                primaryBucketsSeen.remove(bucketId);

                // Remove the bucket from the bucketsProcessed (under
                // synchonization on the partitionName)
                synchronized (br.getName()) {
                  this.bucketsProcessed.remove(partitionName);
                }
              }
              continue;
            } else {
              // If this VM is the primary for this bucket and it was not
              // previously
              // a secondary bucket, add it to the set of primary buckets seen.
              // Otherwise this is a failover condition.
              if (regionAdvisor.isPrimaryForBucket(bucketId)) {
                if (secondaryBucketsSeen.contains(bucketId)) {
                  // Failover condition. Do failover processing.
                  // Remove the bucket from the secondary buckets seen and add
                  // it
                  // to the primary buckets seen
                  secondaryBucketsSeen.remove(bucketId);
                  this.statistics.decSecondaryBuckets();
                  primaryBucketsSeen.add(bucketId);
                  this.statistics.incPrimaryBuckets();

                  // Do failover processing on the BucketRegion
                  synchronized (partitionName) {
                    if (isFailover(partitionName)) {
                      if (logger.fineEnabled()) {
                        logger.fine(this + ": First time processing bucket "
                            + partitionName);
                      }
                      handleFailover(this.parentRegion, br, partitionName);
                    }
                  }
                  if (logger.fineEnabled()) {
                    logger
                        .fine(this + ": This VM is now the primary for bucket "
                            + bucketId);
                  }
                } else {
                  primaryBucketsSeen.add(bucketId);
                  this.statistics.incPrimaryBuckets();
                  if (logger.fineEnabled()) {
                    logger.fine(this + ": Added bucket " + bucketId
                        + " to the set of primary buckets seen");
                  }
                  continue;
                }
              } else {
                // It is a secondary bucket. Add it to the set of secondary
                // buckets seen
                if (secondaryBucketsSeen.add(bucketId)) {
                  this.statistics.incSecondaryBuckets();
                  if (logger.fineEnabled()) {
                    logger.fine(this + ": Added bucket " + bucketId
                        + " to the set of secondary buckets seen");
                  }
                } else {
                  if (logger.fineEnabled()) {
                    logger.fine(this
                        + ": This VM is still the secondary for bucket "
                        + bucketId);
                  }
                }
              }
            }
          }
        }
      } catch (Exception e) {
        this.logger
            .warning(
                "Failover thread caught the following exception and will exit: ",
                e);
        this.continueProcessing = false;
      }
    }
  }

  /**
   * Handle failover.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void handleFailover(Region parent, Region partition,
      String partitionName) {
    logInfo(this + ": Initiating failover processing for bucket "
        + partitionName);
    this.sequenceNumbers.put(partitionName, 0);
    if (partition.size() == 0) {
      // This is the first time this bucket is being processed. Just store the
      // initial
      // sequence number
      logInfo(this + ": No failover processing necessary for bucket "
          + partitionName + " since it contains no entries");
    } else {
      // Iterate the entries in the region in order. For each entry:
      // - mark it as possible duplicate
      // - invoke the listener on it
      Set sortedEntries = sort(partition);

      // Increment number of failover entries processed

      logInfo(this + ": Processing " + sortedEntries.size()
          + " existing entries in bucket " + partitionName);

      if (sortedEntries.size() > 0) {
        // This can be zero when the dummy entries are counted (at the beginning
        // of
        // processing)
        // Increment number of failovers processed
        this.statistics.incFailovers();
      }

      LocalRegion lr = (LocalRegion) partition;
      for (Iterator i = sortedEntries.iterator(); i.hasNext();) {
        Region.Entry entry = (Region.Entry) i.next();
        RegionEntry re = ((LocalRegion.NonTXEntry) entry).getRegionEntry();

        if (this.cache.getLogger().fineEnabled()) {
          this.cache.getLogger().fine(
              this + ": Processing entry in bucket " + partitionName + ": "
                  + entry.getKey() + "->" + entry.getValue());
        }

        // Set possible duplicate
        DQueueKey key = (DQueueKey) entry.getKey();
        key.setPossibleDuplicate(true);

        // Create an entry event
        EntryEventImpl event = new EntryEventImpl(lr, Operation.CREATE, key,
            DQueueInternalPort.getInternalPort().getValueInVM(re, lr), null, true, null, true, false);

        // Invoke the cache listeners (directly).
        CacheListener[] listeners = parent.getAttributes().getCacheListeners();
        for (int j = 0; j < listeners.length; j++) {
          listeners[j].afterCreate(event);
        }

        // Increment the failover events
        this.statistics.incFailoverEvents();

        // Store the initial sequence number based on the sequence number of the
        // last event.
        // The next request will increment it.
        if (!i.hasNext()) {
          logInfo(this + ": Initial sequence number for bucket "
              + partitionName + ": " + key.getSequenceNumber());
          this.sequenceNumbers.put(partitionName, key.getSequenceNumber());
        }
      }
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private Set sort(Region partition) {
    // Create TreeSet using the sequence number Comparator
    Set sortedEntries = new TreeSet(new SequenceNumberComparator());

    // Add all the DQueueKey entries. Skip initialization entries
    for (Iterator i = partition.entrySet().iterator(); i.hasNext();) {
      Region.Entry entry = (Region.Entry) i.next();
      if (!(entry.getKey() instanceof String)) {
        sortedEntries.add(entry);
      }
    }
    return sortedEntries;
  }

  private void logInfo(String message) {
    // System.out.println(message);
    this.cache.getLogger().info(message);
  }

  @SuppressWarnings("rawtypes")
  private class SequenceNumberComparator implements Comparator<Region.Entry> {

    public int compare(Region.Entry obj1, Region.Entry obj2) {
      DQueueKey key1 = (DQueueKey) obj1.getKey();
      DQueueKey key2 = (DQueueKey) obj2.getKey();
      return key1.getSequenceNumber() - key2.getSequenceNumber();
    }
  }
}
