package com.gemstone.gemfire.addon.dq.internal;

import java.util.Set;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.internal.Assert;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;

@SuppressWarnings("serial")
public class DQueueOfferFunction implements DQueueOfferFunctionBase {

  private final Cache cache;

  private final String dqueueId;
  
  private final DQueueAbstractDispatcher dispatcher;

  public DQueueOfferFunction(String dqueueId, Cache cache) {
    // Dummy instance for the PR accessor
    this.dqueueId = dqueueId;
    this.cache = cache;
    this.dispatcher = null;
  }

  public DQueueOfferFunction(DQueueAbstractDispatcher dispatcher) {
    this.dqueueId = dispatcher.getId();
    this.cache = dispatcher.getCache();
    this.dispatcher = dispatcher;
  }

  public String getId() {
    return BASE_ID + this.dqueueId;
  }

  @SuppressWarnings("rawtypes")
  public void execute(FunctionContext context) {
    long start = this.dispatcher.getStatistics().startObjectOffer();
    if (context instanceof RegionFunctionContext) {
      // Get the type
      RegionFunctionContext rfc = (RegionFunctionContext) context;
      Set keys = rfc.getFilter();
      Assert.assertTrue(keys != null && keys.size() == 1);
      Object type = keys.iterator().next();
      if (this.cache.getLogger().fineEnabled()) {
        this.cache.getLogger().fine("Executing function " + getId() + " on type " + type);
      }

      // Get the function arguments
      DQueueOfferFunctionArguments arguments = (DQueueOfferFunctionArguments) context.getArguments();
      String memberId = arguments.getMemberId();
      byte[] userData = arguments.getSerializedUserData();
      boolean posDup = arguments.getPossibleDuplicate();
      
      // Get the region and put the value into it at the generated key. Handle failover if necessary.
      Region region = rfc.getDataSet();
      if (PartitionRegionHelper.isPartitionedRegion(region)) {
        PartitionedRegion partitionedRegion = (PartitionedRegion) region;
        Region partition = partitionedRegion.getDataStore().getLocalBucketByKey(type);
        String partitionName = partition.getName();
        // Look at moving the synchronization up to just around the failover
        // and then using an AtomicInteger for the sequence number
        // @dpark - Use a custom class, SerializedObject to support native clients
//        Object value = CachedDeserializableFactory.create(arguments.getSerializedValue());
        Object value = new SerializedObject(arguments.getSerializedValue());
        synchronized (partitionName) {
          if (this.dispatcher.getFailoverHandler().isFailover(partitionName)) {
            logInfo(this + ": Handling failover for bucket " + partitionName);
            this.dispatcher.getFailoverHandler().handleFailover(partitionedRegion, partition, partitionName);
          }
          int seqNum = this.dispatcher.getFailoverHandler().getAndIncrementSequenceNumber(partitionName);
          DQueueKey key = new DQueueKey(memberId, type, seqNum, userData, posDup);
          partitionedRegion.put(key, value);
        }
      }
    }
    // Send the last result back to the caller if necessary
    if (hasResult()) {
      context.getResultSender().lastResult(null);
    }
    this.dispatcher.getStatistics().endObjectOffer(start);
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

  public String toString() {
    StringBuffer buffer = new StringBuffer()
      .append("DQueueOfferFunction[")
      .append("dqueueId=")
      .append(this.dqueueId)
      .append("]");
    return buffer.toString();
  }

  private void logInfo(String message) {
    //System.out.println(message);
    this.cache.getLogger().info(message);
  }
}
