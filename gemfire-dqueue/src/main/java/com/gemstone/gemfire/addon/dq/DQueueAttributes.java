package com.gemstone.gemfire.addon.dq;

/**
 * Class <code>DQueueAttributes</code> contains the configuration for a
 * <code>DQueue</code>.
 *
 * @see DQueue
 */
public class DQueueAttributes {

  /**
   * Whether to automatically start the <code>DQDispatcher</code>
   */
  private boolean startDispatcher;

  /**
   * Whether this <code>DQueueAttributes</code> is partitioned
   */
  private boolean isPartitioned = true;

  /**
   * Whether this <code>DQueueAttributes</code> is a client
   */
  private boolean isClient = false;
  
  /**
   * The name of the <code>Pool</code> for this <code>DQueueAttributes</code>
   */
  private String poolName;
  
  /**
   * The <code>DQueueListener</code> for this <code>DQueueAttributes</code>
   */
  private DQueueListener listener;

  /**
   * The <code>DQueueFilter</code> for this <code>DQueueAttributes</code>
   */
  private DQueueFilter filter;

  /**
   * The number of buckets for this partitioned <code>DQueueAttributes</code>
   */
  private int buckets;

  /**
   * Whether this <code>DQueueAttributes</code> must preserve order. Default is true.
   */
  private boolean preserveOrder = true;
  
  /**
   * The full path of coloated region.
   */
  private String colocatedRegionFullPath = null;

  /**
   * The default batch size.
   * The default value is 100.
   */
  private static final int DEFAULT_BATCH_SIZE = 100;

  /**
   * The batch size for this partitioned <code>DQueueAttributes</code>
   */
  private int batchSize = DEFAULT_BATCH_SIZE;

  /**
   * The default batch time interval in milliseconds.
   * The default value is 100.
   */
  private static final int DEFAULT_BATCH_TIME_INTERVAL = 100;

  /**
   * The batch time interval for this partitioned <code>DQueueAttributes</code>
   */
  private int batchTimeInterval = DEFAULT_BATCH_TIME_INTERVAL;

  /**
   * The <code>DQueueProcessingPolicy</code> for this
   * <code>DQueueAttributes</code> notifies the <code>DQueueDispatcher</code>
   * how to process the backend queues.
   *
   * Valid options are:
   * <ul>
   * <li>round-robin (default)</li>
   * <li>largest</li>
   * <li>smallest</li>
   * <li>fifo</li>
   * <li>lifo</li>
   * </ul>
   */
  private DQueueProcessingPolicy queueProcessingPolicy = DQueueProcessingPolicy.ROUND_ROBIN;

  /**
   * The default number of times to retry dispatching an event to a
   * <code>DQueueListener</code> before giving up and calling dispatchFailed.
   * The default value is 0.
   */
  private static final int DEFAULT_LISTENER_RETRY_ATTEMPTS = 0;

  /**
   * The number of times to retry dispatching an event to a
   * <code>DQueueListener</code> before giving up and calling dispatchFailed.
   */
  private int listenerRetryAttempts = DEFAULT_LISTENER_RETRY_ATTEMPTS;

  /**
   * Convenience method to create the following <code>DQueueAttributes</code>:
   * <ul>
   * <li> setStartDispatcher(false)
   * <li> setPartitioned(true)
   * <li> setBuckets(numBuckets)
   * </ul>
   *
   * @param numBuckets The number of buckets to create
   *
   * @return A <code>DQueueAttributes</code>
   */
  public static DQueueAttributes createPartitionedQueueAttributes(int numBuckets) {
    DQueueAttributes attributes = new DQueueAttributes();
    attributes.setStartDispatcher(false);
    attributes.setPartitioned(true);
    attributes.setBuckets(numBuckets);
    return attributes;
  }

  /**
   * Convenience method to create the following <code>DQueueAttributes</code>:
   * <ul>
   * <li> setStartDispatcher(false)
   * <li> setPartitioned(false)
   * <li> setClient(true)
   * <li> setPoolName(poolName)
   * </ul>
   *
   * @param poolName The client pool name
   *
   * @return A <code>DQueueAttributes</code>
   */
  public static DQueueAttributes createClientQueueAttributes(String poolName) {
    DQueueAttributes attributes = new DQueueAttributes();
    attributes.setStartDispatcher(false);
    attributes.setPartitioned(false);
    attributes.setClient(true);
    attributes.setPoolName(poolName);
    return attributes;
  }

  /**
   * Convenience method to create the following <code>DQueueAttributes</code>:
   * <ul>
   * <li> setDispatcher(true)
   * <li> setStartDispatcher(true)
   * <li> setPartitioned(true)
   * <li> setListener(listener)
   * <li> setFilter(filter)
   * <li> setBuckets(numBuckets)
   * </ul>
   *
   * @param listener The <code>DQueueListener</code>
   * @param filter The <code>DQueueFilter</code>
   * @param numBuckets The number of buckets to create
   *
   * @return A <code>DQueueAttributes</code>
   */
  public static DQueueAttributes createPushDispatcherAttributes(
      DQueueListener listener, DQueueFilter filter, int numBuckets) {
      return createPushDispatcherAttributes(listener, filter, numBuckets, true);
  }

  /**
   * Convenience method to create the following <code>DQueueAttributes</code>:
   * <ul>
   * <li> setStartDispatcher(true)
   * <li> setPartitioned(true)
   * <li> setListener(listener)
   * <li> setFilter(filter)
   * <li> setBuckets(numBuckets)
   * <li> setPreserveOrder(preserveOrder)
   * </ul>
   *
   * @param listener The <code>DQueueListener</code>
   * @param filter The <code>DQueueFilter</code>
   * @param numBuckets The number of buckets to create
   *
   * @return A <code>DQueueAttributes</code>
   */
  public static DQueueAttributes createPushDispatcherAttributes(
      DQueueListener listener, DQueueFilter filter, int numBuckets,
      boolean preserveOrder) {
      return createPushDispatcherAttributes(listener, filter, numBuckets, true,
        DEFAULT_BATCH_SIZE, DEFAULT_BATCH_TIME_INTERVAL, null);
  }

  public static DQueueAttributes createPushDispatcherAttributes(
      DQueueListener listener, DQueueFilter filter, int numBuckets,
      boolean preserveOrder, int batchSize, int batchTimeInterval, 
      String colocatedRegionFullPath) {
    DQueueAttributes attributes = new DQueueAttributes();
    attributes.setStartDispatcher(true);
    attributes.setPartitioned(true);
    attributes.setListener(listener);
    attributes.setFilter(filter);
    attributes.setBuckets(numBuckets);
    attributes.setPreserveOrder(preserveOrder);
    attributes.setBatchSize(batchSize);
    attributes.setBatchTimeInterval(batchTimeInterval);
    attributes.setColocatedRegionFullPath(colocatedRegionFullPath);
    return attributes;
  }

  /**
   * Convenience method to create the following <code>DQueueAttributes</code>:
   * <ul>
   * <li> setStartDispatcher(true)
   * <li> setPartitioned(true)
   * <li> setBuckets(numBuckets)
   * </ul>
   *
   * @param numBuckets The number of buckets to create
   *
   * @return A <code>DQueueAttributes</code>
   */
  public static DQueueAttributes createPullDispatcherAttributes(int numBuckets) {
    DQueueAttributes attributes = new DQueueAttributes();
    attributes.setStartDispatcher(true);
    attributes.setPartitioned(true);
    attributes.setBuckets(numBuckets);
    return attributes;
  }

  /**
   * Returns this <code>DQueueAttributes</code> configured
   * <code>DQueueListener</code>
   *
   * @return this <code>DQueueAttributes</code> configured
   * <code>DQueueListener</code>
   */
  public DQueueListener getListener() {
    return this.listener;
  }

  /**
   * Sets this <code>DQueueAttributes</code> configured
   * <code>DQueueListener</code>
   *
   * @param listener The <code>DQueueListener</code>
   */
  public void setListener(DQueueListener listener) {
    this.listener = listener;
  }

  /**
   * Returns this <code>DQueueFilter</code> configured
   * <code>DQueueListener</code>
   *
   * @return this <code>DQueueFilter</code> configured
   * <code>DQueueListener</code>
   */
  public DQueueFilter getFilter() {
    return this.filter;
  }

  /**
   * Sets this <code>DQueueAttributes</code> configured
   * <code>DQueueFilter</code>
   *
   * @param filter The <code>DQueueFilter</code>
   */
  public void setFilter(DQueueFilter filter) {
    this.filter = filter;
  }

  /**
   * Returns whether this <code>DQueueAttributes</code> should automatically
   * start the <code>DQDispatcher</code>
   *
   * @return whether this <code>DQueueAttributes</code> should automatically
   * start the <code>DQDispatcher</code>
   */
  public boolean getStartDispatcher() {
    return this.startDispatcher;
  }

  /**
   * Sets whether this <code>DQueueAttributes</code> should automatically
   * start the <code>DQDispatcher</code>
   *
   * @param startDispatcher Whether this <code>DQueueAttributes</code> should
   * automatically start the <code>DQDispatcher</code>
   */
  public void setStartDispatcher(boolean startDispatcher) {
    this.startDispatcher = startDispatcher;
  }

  /**
   * Returns whether this <code>DQueueAttributes</code> is partitioned
   *
   * @return whether this <code>DQueueAttributes</code> is partitioned
   */
  public boolean isPartitioned() {
    return this.isPartitioned;
  }

  /**
   * Sets whether this <code>DQueueAttributes</code> is partitioned
   *
   * @param isPartitioned Whether this <code>DQueueAttributes</code> is
   * partitioned
   */
  public void setPartitioned(boolean isPartitioned) {
    this.isPartitioned = isPartitioned;
  }

  /**
   * Returns whether this <code>DQueueAttributes</code> is a client
   *
   * @return whether this <code>DQueueAttributes</code> is a client
   */
  public boolean isClient() {
    return this.isClient;
  }

  /**
   * Sets whether this <code>DQueueAttributes</code> is a client
   *
   * @param isClient Whether this <code>DQueueAttributes</code> is
   * a client
   */
  public void setClient(boolean isClient) {
    this.isClient = isClient;
  }

  /**
   * Returns this <code>DQueueAttributes</code>'s pool name
   *
   * @return this <code>DQueueAttributes</code>'s pool name
   */
  public String getPoolName() {
    return this.poolName;
  }

  /**
   * Sets this <code>DQueueAttributes</code>'s pool nae
   *
   * @param poolName This <code>DQueueAttributes</code>'s pool name
   */
  public void setPoolName(String poolName) {
    this.poolName = poolName;
  }

  /**
   * Returns this <code>DQueueAttributes</code>'s number of buckets
   *
   * @return this <code>DQueueAttributes</code>'s number of buckets
   */
  public int getBuckets() {
    return this.buckets;
  }

  /**
   * Sets this <code>DQueueAttributes</code>'s number of buckets
   *
   * @param buckets This <code>DQueueAttributes</code>'s number of buckets
   */
  public void setBuckets(int buckets) {
    this.buckets = buckets;
  }

  /**
   * Returns this <code>DQueueAttributes</code>'s listener retry attempts
   *
   * @return this <code>DQueueAttributes</code>'s listener retry attempts
   */
  public int getListenerRetryAttempts() {
    return this.listenerRetryAttempts;
  }

  /**
   * Sets this <code>DQueueAttributes</code>'s retry attempts
   *
   * @param listenerRetryAttempts This <code>DQueueAttributes</code>'s listener retry attempts
   */
  public void setListenerRetryAttempts(int listenerRetryAttempts) {
    this.listenerRetryAttempts = listenerRetryAttempts;
  }

  /**
   * Returns this <code>DQueueAttributes</code>'s
   * <code>QueueProcessingPolicy</code>
   *
   * @return this <code>DQueueAttributes</code>'s
   *         <code>QueueProcessingPolicy</code>
   */
  public DQueueProcessingPolicy getQueueProcessingPolicy() {
    return this.queueProcessingPolicy;
  }

  /**
   * Sets this <code>DQueueAttributes</code>'s
   * <code>QueueProcessingPolicy</code>
   *
   * @param queueProcessingPolicy
   *          This <code>DQueueAttributes</code>'s
   *          <code>QueueProcessingPolicy</code>
   */
  public void setQueueProcessingPolicy(
      DQueueProcessingPolicy queueProcessingPolicy) {
    this.queueProcessingPolicy = queueProcessingPolicy;
  }

  /**
   * Sets this <code>DQueueAttributes</code>'s preserveOrder flag
   *
   * @param preserveOrder
   *          This <code>DQueueAttributes</code>'s preserveOrder flag
   */
  public void setPreserveOrder(boolean preserveOrder) {
    this.preserveOrder = preserveOrder;
  }

  /**
   * Returns this <code>DQueueAttributes</code>'s preserveOrder flag
   *
   * @return this <code>DQueueAttributes</code>'s preserveOrder flag
   */
  public boolean getPreserveOrder() {
    return this.preserveOrder;
  }

  /**
   * Returns this <code>DQueueAttributes</code>'s batch size
   *
   * @return this <code>DQueueAttributes</code>'s batch size
   */
  public int getBatchSize() {
    return this.batchSize;
  }

  /**
   * Sets this <code>DQueueAttributes</code>'s batch size
   *
   * @param batchSize This <code>DQueueAttributes</code>'s batch size
   */
  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  /**
   * Returns this <code>DQueueAttributes</code>'s batch time interval
   *
   * @return this <code>DQueueAttributes</code>'s batch time interval
   */
  public int getBatchTimeInterval() {
    return this.batchTimeInterval;
  }

  /**
   * Sets this <code>DQueueAttributes</code>'s batch time interval
   *
   * @param batchTimeInterval This <code>DQueueAttributes</code>'s batch time interval
   */
  public void setBatchTimeInterval(int batchTimeInterval) {
    this.batchTimeInterval = batchTimeInterval;
  }

  /**
   * Returns this <code>DQueueAttributes</code>'s colocated region full path
   * 
   * @return this <code>DQueueAttributes</code>'s colocated region full path
   */
  public String getColocatedRegionFullPath() {
	return this.colocatedRegionFullPath;
  }
  
  /**
   * Sets this <code>DQueueAttributes</code>'s colocated region full path.
   * 
   * @param colocatedRegionFullPath This <code>DQueueAttributes</code>'s colocated region full path
   */
  public void setColocatedRegionFullPath(String colocatedRegionFullPath) {
	 this.colocatedRegionFullPath = colocatedRegionFullPath;
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer
      .append("DQueueAttributes[")
      .append("isPartitioned=").append(this.isPartitioned)
      .append("; isClient=").append(this.isClient)
      .append("; listener=").append(this.listener)
      .append("; filter=").append(this.filter)
      .append("; buckets=").append(this.buckets)
      .append("; listenerRetryAttempts=").append(this.listenerRetryAttempts)
      .append("; preserveOrder=").append(this.preserveOrder)
      .append("; batchSize=").append(this.batchSize)
      .append("; batchTimeInterval=").append(this.batchTimeInterval)
      .append("; colocatedRegionFullPath=").append(this.colocatedRegionFullPath)
      .append("; poolName=").append(this.poolName)
      .append("]");
    return buffer.toString();
  }
}
