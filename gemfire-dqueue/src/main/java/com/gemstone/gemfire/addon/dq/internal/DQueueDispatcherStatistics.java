package com.gemstone.gemfire.addon.dq.internal;

import com.gemstone.gemfire.StatisticDescriptor;
import com.gemstone.gemfire.Statistics;
import com.gemstone.gemfire.StatisticsFactory;
import com.gemstone.gemfire.StatisticsType;
import com.gemstone.gemfire.StatisticsTypeFactory;
import com.gemstone.gemfire.addon.dq.DQueueEventBatch;
import com.gemstone.gemfire.distributed.internal.DistributionStats;
import com.gemstone.gemfire.internal.StatisticsTypeFactoryImpl;

public class DQueueDispatcherStatistics {

  public static final String typeName = "DQueueDispatcherStatistics";

  private static final StatisticsType type;

  private static final String OBJECTS_OFFERED = "objectsOffered";
  private static final String OBJECT_OFFERS_IN_PROGRESS = "objectOffersInProgress";
  private static final String OBJECT_OFFER_TIME = "objectOfferTime";
  private static final String PRIMARY_BUCKETS = "primaryBuckets";
  private static final String SECONDARY_BUCKETS = "secondaryBuckets";
  private static final String TOTAL_BUCKETS = "totalBuckets";
  private static final String FAILOVERS = "failovers";
  private static final String FAILOVER_EVENTS = "failoverEvents";

  private static final String OBJECTS_QUEUED = "objectsQueued";
  private static final String EVENTS_DISPATCHED = "eventsDispatched";
  private static final String EVENT_DISPATCH_TIME = "eventDispatchTime";
  private static final String EVENTS_PROCESSED = "eventsProcessed";
  private static final String EVENT_DISPATCHES_IN_PROGRESS = "eventDispatchesInProgress";
  private static final String FAILED_EVENTS_DISPATCHED = "failedEventsDispatched";

  private static final String BATCHES_DISPATCHED = "batchesDispatched";
  private static final String BATCH_DISPATCH_TIME = "batchDispatchTime";
  private static final String BATCHES_PROCESSED = "batchesProcessed";
  private static final String BATCH_DISPATCHES_IN_PROGRESS = "batchDispatchesInProgress";
  private static final String FAILED_BATCHES_DISPATCHED = "failedBatchesDispatched";

  private static final String TOTAL_QUEUES = "totalQueues";

  private static final String OBJECTS_POLLED = "objectsPolled";
  private static final String OBJECT_POLLS_IN_PROGRESS = "objectPollsInProgress";
  private static final String OBJECT_POLL_TIME = "objectPollTime";

  private static final String OBJECTS_PEEKED = "objectsPeeked";
  private static final String OBJECT_PEEKS_IN_PROGRESS = "objectPeeksInProgress";
  private static final String OBJECT_PEEK_TIME = "objectPeekTime";

  private static final int objectsOfferedId;
  private static final int objectOffersInProgressId;
  private static final int objectOfferTimeId;
  private static final int primaryBucketsId;
  private static final int secondaryBucketsId;
  private static final int totalBucketsId;
  private static final int failoversId;
  private static final int failoverEventsId;

  private static final int objectsQueuedId;
  private static final int eventsDispatchedId;
  private static final int eventDispatchTimeId;
  private static final int eventsProcessedId;
  private static final int eventDispatchesInProgressId;
  private static final int failedEventsDispatchedId;

  private static final int batchesDispatchedId;
  private static final int batchDispatchTimeId;
  private static final int batchesProcessedId;
  private static final int batchDispatchesInProgressId;
  private static final int failedBatchesDispatchedId;

  private static final int totalQueuesId;

  private static final int objectsPolledId;
  private static final int objectPollsInProgressId;
  private static final int objectPollTimeId;

  private static final int objectsPeekedId;
  private static final int objectPeeksInProgressId;
  private static final int objectPeekTimeId;

  static {
    // Initialize type
    StatisticsTypeFactory f = StatisticsTypeFactoryImpl.singleton();
    type = f.createType(typeName, typeName,
      new StatisticDescriptor[] {
        f.createIntCounter(OBJECTS_OFFERED, "The number of objects offered to the dqueue", "operations"),
        f.createIntGauge(OBJECT_OFFERS_IN_PROGRESS, "The number of object offers currently in progress", "operations"),
        f.createLongCounter(OBJECT_OFFER_TIME, "Total time spent offering objects to the dqueue", "nanoseconds"),
        f.createIntGauge(PRIMARY_BUCKETS, "The number of primary buckets handled by this dqueue dispatcher", "operations"),
        f.createIntGauge(SECONDARY_BUCKETS, "The number of secondary buckets handled by this dqueue dispatcher", "operations"),
        f.createIntGauge(TOTAL_BUCKETS, "The total number of buckets handled by this dqueue dispatcher", "operations"),
        f.createIntCounter(FAILOVERS, "The total number of failovers performed by this dqueue dispatcher", "operations"),
        f.createIntCounter(FAILOVER_EVENTS, "The total number of event failovers performed by this dqueue dispatcher", "operations"),

        f.createIntCounter(OBJECTS_QUEUED, "The number of objects queued by this dqueue dispatcher", "operations"),
        f.createIntCounter(EVENTS_DISPATCHED, "The number of events dispatched by this dqueue dispatcher", "operations"),
        f.createLongCounter(EVENT_DISPATCH_TIME, "Total time spent dispatching events to the dqueue listener", "nanoseconds"),
        f.createIntCounter(EVENTS_PROCESSED, "The number of events processed by this dqueue dispatcher", "operations"),
        f.createIntGauge(EVENT_DISPATCHES_IN_PROGRESS, "The number of event dispatches currently in progress", "operations"),
        f.createIntCounter(FAILED_EVENTS_DISPATCHED, "The number of events that failed to be dispatched by this dqueue dispatcher", "operations"),

        f.createIntCounter(BATCHES_DISPATCHED, "The number of batches dispatched by this dqueue dispatcher", "operations"),
        f.createLongCounter(BATCH_DISPATCH_TIME, "Total time spent dispatching batches to the dqueue listener", "nanoseconds"),
        f.createIntCounter(BATCHES_PROCESSED, "The number of batches processed by this dqueue dispatcher", "operations"),
        f.createIntGauge(BATCH_DISPATCHES_IN_PROGRESS, "The number of batch dispatches currently in progress", "operations"),
        f.createIntCounter(FAILED_BATCHES_DISPATCHED, "The number of batches that failed to be dispatched by this dqueue dispatcher", "operations"),

        f.createIntGauge(TOTAL_QUEUES, "The total number of queues handled by this dqueue dispatcher", "operations"),

        f.createIntCounter(OBJECTS_POLLED, "The number of objects polled from the dqueue", "operations"),
        f.createIntGauge(OBJECT_POLLS_IN_PROGRESS, "The number of object polls currently in progress", "operations"),
        f.createLongCounter(OBJECT_POLL_TIME, "Total time spent polling objects from the dqueue", "nanoseconds"),

        f.createIntCounter(OBJECTS_PEEKED, "The number of objects peeked from the dqueue", "operations"),
        f.createIntGauge(OBJECT_PEEKS_IN_PROGRESS, "The number of object peeks currently in progress", "operations"),
        f.createLongCounter(OBJECT_PEEK_TIME, "Total time spent peeking objects from the dqueue", "nanoseconds"),
      }
    );

    // Initialize id fields
    objectsOfferedId = type.nameToId(OBJECTS_OFFERED);
    objectOffersInProgressId = type.nameToId(OBJECT_OFFERS_IN_PROGRESS);
    objectOfferTimeId = type.nameToId(OBJECT_OFFER_TIME);
    primaryBucketsId = type.nameToId(PRIMARY_BUCKETS);
    secondaryBucketsId = type.nameToId(SECONDARY_BUCKETS);
    totalBucketsId = type.nameToId(TOTAL_BUCKETS);
    failoversId = type.nameToId(FAILOVERS);
    failoverEventsId = type.nameToId(FAILOVER_EVENTS);

    objectsQueuedId = type.nameToId(OBJECTS_QUEUED);
    eventsDispatchedId = type.nameToId(EVENTS_DISPATCHED);
    eventDispatchTimeId = type.nameToId(EVENT_DISPATCH_TIME);
    eventsProcessedId = type.nameToId(EVENTS_PROCESSED);
    eventDispatchesInProgressId = type.nameToId(EVENT_DISPATCHES_IN_PROGRESS);
    failedEventsDispatchedId = type.nameToId(FAILED_EVENTS_DISPATCHED);

    batchesDispatchedId = type.nameToId(BATCHES_DISPATCHED);
    batchDispatchTimeId = type.nameToId(BATCH_DISPATCH_TIME);
    batchesProcessedId = type.nameToId(BATCHES_PROCESSED);
    batchDispatchesInProgressId = type.nameToId(BATCH_DISPATCHES_IN_PROGRESS);
    failedBatchesDispatchedId = type.nameToId(FAILED_BATCHES_DISPATCHED);

    totalQueuesId = type.nameToId(TOTAL_QUEUES);

    objectsPolledId = type.nameToId(OBJECTS_POLLED);
    objectPollsInProgressId = type.nameToId(OBJECT_POLLS_IN_PROGRESS);
    objectPollTimeId = type.nameToId(OBJECT_POLL_TIME);

    objectsPeekedId = type.nameToId(OBJECTS_PEEKED);
    objectPeeksInProgressId = type.nameToId(OBJECT_PEEKS_IN_PROGRESS);
    objectPeekTimeId = type.nameToId(OBJECT_PEEK_TIME);
  }

  private final Statistics stats;

  public DQueueDispatcherStatistics(StatisticsFactory f, String dqueueId) {
    this.stats = f.createAtomicStatistics(type, "dqueueStatistics-"+dqueueId);
  }

  public void close() {
    this.stats.close();
  }

  public int getObjectsOffered() {
    return this.stats.getInt(objectsOfferedId);
  }

  public int getObjectsPolled() {
    return this.stats.getInt(objectsPolledId);
  }

  public int getObjectsPeeked() {
    return this.stats.getInt(objectsPeekedId);
  }

  public int getPrimaryBuckets() {
    return this.stats.getInt(primaryBucketsId);
  }

  public void incPrimaryBuckets() {
    this.stats.incInt(primaryBucketsId, 1);
  }

  public void decPrimaryBuckets() {
    this.stats.incInt(primaryBucketsId, -1);
  }

  public int getSecondaryBuckets() {
    return this.stats.getInt(secondaryBucketsId);
  }

  public void incSecondaryBuckets() {
    this.stats.incInt(secondaryBucketsId, 1);
  }

  public void decSecondaryBuckets() {
    this.stats.incInt(secondaryBucketsId, -1);
  }

  public int getTotalBuckets() {
    return this.stats.getInt(totalBucketsId);
  }

  public void setTotalBuckets(int totalBuckets) {
    this.stats.setInt(totalBucketsId, totalBuckets);
  }

  public int getFailovers() {
    return this.stats.getInt(failoversId);
  }

  public void incFailovers() {
    this.stats.incInt(failoversId, 1);
  }

  public int getFailoverEvents() {
    return this.stats.getInt(failoverEventsId);
  }

  public void incFailoverEvents() {
    this.stats.incInt(failoverEventsId, 1);
  }

  public int getObjectsQueued() {
    return this.stats.getInt(objectsQueuedId);
  }

  public void incObjectsQueued() {
    this.stats.incInt(objectsQueuedId, 1);
  }

  public long getEventDispatchTime() {
    return this.stats.getLong(eventDispatchTimeId);
  }

  public long startObjectOffer() {
    stats.incInt(objectOffersInProgressId, 1);
    return getTime();
  }

  public void endObjectOffer(long start) {
    long end = getTime();

    // Increment number of objects offered
    this.stats.incInt(objectsOfferedId, 1);

    // Increment object offer time
    this.stats.incLong(objectOfferTimeId, end-start);

    // Decrement the number of object offers in progress
    this.stats.incInt(objectOffersInProgressId, -1);
  }

  public long startObjectPoll() {
    stats.incInt(objectPollsInProgressId, 1);
    return getTime();
  }

  public void endObjectPoll(long start) {
    long end = getTime();

    // Increment number of objects polled
    this.stats.incInt(objectsPolledId, 1);

    // Increment object poll time
    this.stats.incLong(objectPollTimeId, end-start);

    // Decrement the number of object polls in progress
    this.stats.incInt(objectPollsInProgressId, -1);
  }

  public long startObjectPeek() {
    stats.incInt(objectPeeksInProgressId, 1);
    return getTime();
  }

  public void endObjectPeek(long start) {
    long end = getTime();

    // Increment number of objects peeked
    this.stats.incInt(objectsPeekedId, 1);

    // Increment object peek time
    this.stats.incLong(objectPeekTimeId, end-start);

    // Decrement the number of object peeks in progress
    this.stats.incInt(objectPeeksInProgressId, -1);
  }

  public long startEventDispatch() {
    stats.incInt(eventDispatchesInProgressId, 1);
    return getTime();
  }

  public void endEventDispatch(long start) {
    long end = getTime();

    // Increment number of events dispatched
    this.stats.incInt(eventsDispatchedId, 1);

    // Increment event dispatch time
    this.stats.incLong(eventDispatchTimeId, end-start);

    // Decrement the number of event dispatches in progress
    this.stats.incInt(eventDispatchesInProgressId, -1);
  }

  public int getEventsProcessed() {
    return this.stats.getInt(eventsProcessedId);
  }

  public void incEventsProcessed() {
    this.stats.incInt(eventsProcessedId, 1);
  }

  public int getFailedEventsDispatched() {
    return this.stats.getInt(failedEventsDispatchedId);
  }

  public void incFailedEventsDispatched() {
    this.stats.incInt(failedEventsDispatchedId, 1);
  }

  public long startBatchDispatch(DQueueEventBatch batch) {
    stats.incInt(batchDispatchesInProgressId, 1);
    stats.incInt(eventDispatchesInProgressId, batch.getNumberOfEvents());
    return getTime();
  }

  public void endBatchDispatch(long start, DQueueEventBatch batch) {
    long end = getTime();
    int numEvents = batch.getNumberOfEvents();

    // Increment number of batches and events dispatched
    this.stats.incInt(batchesDispatchedId, 1);
    this.stats.incInt(eventsDispatchedId, numEvents);

    // Increment batch dispatch time
    this.stats.incLong(batchDispatchTimeId, end-start);

    // Decrement the number of batch and event dispatches in progress
    this.stats.incInt(batchDispatchesInProgressId, -1);
    this.stats.incInt(eventDispatchesInProgressId, -numEvents);
  }

  public int getBatchesProcessed() {
    return this.stats.getInt(batchesProcessedId);
  }

  public void incBatchesProcessed(DQueueEventBatch batch) {
    this.stats.incInt(batchesProcessedId, 1);
    this.stats.incInt(eventsProcessedId, batch.getNumberOfEvents());
  }

  public int getFailedBatchesDispatched() {
    return this.stats.getInt(failedBatchesDispatchedId);
  }

  public void incFailedBatchesDispatched(DQueueEventBatch batch) {
    this.stats.incInt(failedBatchesDispatchedId, 1);
    this.stats.incInt(failedEventsDispatchedId, batch.getNumberOfEvents());
  }

  public int getTotalQueues() {
    return this.stats.getInt(totalQueuesId);
  }

  public void setTotalQueues(int totalQueues) {
    this.stats.setInt(totalQueuesId, totalQueues);
  }

  protected long getTime() {
    return DistributionStats.getStatTime();
  }
}
