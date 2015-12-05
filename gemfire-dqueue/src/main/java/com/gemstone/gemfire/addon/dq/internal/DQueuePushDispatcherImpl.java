package com.gemstone.gemfire.addon.dq.internal;

import java.util.List;

import com.gemstone.gemfire.addon.dq.DQueueAttributes;
import com.gemstone.gemfire.addon.dq.DQueueEvent;
import com.gemstone.gemfire.addon.dq.DQueueFilter;
import com.gemstone.gemfire.addon.dq.DQueueListener;
import com.gemstone.gemfire.addon.util.QueueDispatcherListener;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionService;

public class DQueuePushDispatcherImpl extends DQueueAbstractDispatcher implements QueueDispatcherListener
{

	private DQueueListener dqListener;

	private DQueueFilter dqFilter;

	private int listenerRetryAttempts;

	/**
	 * Constructor. Creates a new <code>DQueue</code> using the input
	 * attributes.
	 * 
	 * @param cache
	 *            The <code>Cache</code>
	 * @param id
	 *            The id
	 * @param dqAttributes
	 *            The <code>DQueueAttributes</code> used to configure this
	 *            instance
	 */
	public DQueuePushDispatcherImpl(Cache cache, String id, DQueueAttributes dqAttributes)
	{
		super(cache, id, dqAttributes);
	}

	public void start()
	{
		// this.queueHandler = new DQueueMultipleQueueHandler(this.cache, this,
		// this.dqFilter.getTotalQueues(), this.queueProcessingPolicy);
		this.queueHandler = new DQueueExecutorQueueHandler(this);
		if (this.cache.getLogger().fineEnabled()) {
			this.cache.getLogger().fine(this + ": Started " + this.queueHandler);
		}
	}

	public void enqueue(final DQueueEvent event)
	{
		// Increment number of objects queued
		this.statistics.incObjectsQueued();

		// Enqueue the object
		int queueNum = this.dqFilter.getQueue(event);
		if (queueNum < 0 || queueNum >= this.dqFilter.getTotalQueues()) {
			StringBuffer buffer = new StringBuffer().append("The filter returned a queue outside the range (0..")
					.append(this.dqFilter.getTotalQueues() - 1).append("): ").append(queueNum);
			System.out.println(buffer.toString());
			queueNum = 0;
		}
		this.queueHandler.enqueue(queueNum, event);
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getSimpleName()).append("[").append("id=").append(this.id).append("; attributes=")
				.append(this.dqAttributes).append("; handler=").append(this.queueHandler).append("]");
		return buffer.toString();
	}

	@SuppressWarnings("rawtypes")
	public void objectDispatched(Object obj)
	{
		// Create DQEventBatch for the object
		List events = (List) obj;
		DQueueEventBatchImpl batch = new DQueueEventBatchImpl(events);

		// Dispatch batch of events and increment stats
		// System.out.println("Dispatching: " + batch);
		int retryCount = 0;
		do {
			try {
				retryCount++;
				long start = this.statistics.startBatchDispatch(batch);
				this.dqListener.dispatch(batch);
				this.statistics.endBatchDispatch(start, batch);
				break;
			} catch (Exception e1) {
				StringBuffer buffer1 = new StringBuffer().append(this)
						.append(": Caught the following exception sending dispatch for ").append(batch);
				buffer1.append(retryCount > this.listenerRetryAttempts ? ". The dispatchFailed method will be invoked, and all the events in the batch will be removed from the queue."
						: ". The dispatch method will be tried again.");
				this.cache.getLogger().warning(buffer1.toString(), e1);

				// Increment the event dispatch failed stat
				this.statistics.incFailedBatchesDispatched(batch);

				// Set the possible duplicate flag for each event in the batch
				batch.setPossibleDuplicate(true);

				// Retry a set number of times, then give up and invoke the
				// dispatchFailed callback.
				if (retryCount > this.listenerRetryAttempts) {
					try {
						this.dqListener.dispatchFailed(batch);
					} catch (Exception e2) {
						StringBuffer buffer2 = new StringBuffer().append(this)
								.append(": Caught the following exception sending dispatchFailed for ").append(batch)
								.append(". All the events in the batch will be removed from the queue.");
						this.cache.getLogger().warning(buffer2.toString(), e2);
					}
					break;
				}
			}
		} while (true);

		// Remove entries in batch from region and increment stats
		batch.removeEvents(this.dqRegion);
		this.statistics.incBatchesProcessed(batch);
	}

	protected void initialize()
	{
		// Create the region
		this.dqListener = this.dqAttributes.getListener();
		this.dqFilter = this.dqAttributes.getFilter();
		this.listenerRetryAttempts = this.dqAttributes.getListenerRetryAttempts();
		if (this.dqFilter != null) {
			this.statistics.setTotalQueues(this.dqFilter.getTotalQueues());
		}
		initializeRegion(new DQueueCacheListenerImpl(this));

		// Create and register the offer function
		Function offerFunction = new DQueueOfferFunction(this);
		if (!FunctionService.isRegistered(offerFunction.getId())) {
			FunctionService.registerFunction(offerFunction);
		}

		// Start the dispatcher
		if (dqAttributes.getStartDispatcher()) {
			start();
		}
	}

	/**
	 * Sets the DQueue listener. This is provided to defer DQueueListener 
	 * creation. This method must not be invoked by application.
	 * 
	 * @param dqListener DQueue listener
	 */
	public void __setListener(DQueueListener dqListener)
	{
		this.dqListener = dqListener;
	}
}
