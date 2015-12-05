package com.gemstone.gemfire.addon.dq.internal;

import java.util.List;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;

@SuppressWarnings("serial")
public class DQueueTakeFunction extends DQueueAbstractPullFunction {

  public static final String BASE_ID = "take_";

  public DQueueTakeFunction(String dqueueId, Cache cache) {
    super(dqueueId, cache);
  }

  public DQueueTakeFunction(DQueuePullDispatcherImpl dispatcher) {
    super(dispatcher);
  }

  @Override
  public void execute(FunctionContext context) {
	    long start = getStatistics().startObjectPoll();
	    RegionFunctionContext rfc = (RegionFunctionContext) context;
	    
	    // Get the type
    Object type = getType(rfc);
    
    // Get args
    Object[] args = (Object[])rfc.getArguments();
    
    // args[0]=count, args[1]=userData(optional)
    int count = -1;
    Object userData = null;
    if (args != null) {
    	if (args.length > 0) {
    		count = (Integer)args[0];
    	}
	    // userData is a list of objects if count >= 0
	    if (args.length > 1) {
			userData = args[1];
		}
    }
  
    // if count < 0 then poll a single item
    // else a list of items

    // Do failover processing
    handleFailover((PartitionedRegion) rfc.getDataSet(), type);
    
    // Poll the head of the queue for the type
    if (count < 0) {
	    DQueueEventImpl event = (DQueueEventImpl) this.dispatcher.poll(type);
	    if (this.cache.getLogger().fineEnabled()) {
	      this.cache.getLogger().fine("Function " + getId() + " on type " + type + " taken: " + event);
	    }
	
	    // Return the result
	    context.getResultSender().lastResult(event == null ? false : true);
	    
	    // Remove the entry from the region, return userData to the caller
	    if (event != null) {
	     rfc.getDataSet().destroy(event.getKey(), userData);
	    }
    } else {
    	List<DQueueEventImpl> list = (List<DQueueEventImpl>) this.dispatcher.poll(type, count);
    	if (this.cache.getLogger().fineEnabled()) {
    		int listSize = 0;
    		if (list != null) {
    			listSize = list.size();
    		}
  	      this.cache.getLogger().fine("Function " + getId() + " on type " + type + " taken: " + listSize + " objects, requested " + count);
  	    }
 
    	// Return the result list (serialized)
	    context.getResultSender().lastResult(list == null ? false : true);
	    
	    // Remove the entry from the region, return all userData objects to the caller
	    if (list != null) {
	    	if (userData == null) {
		    	for (DQueueEventImpl event : list) {
		    		rfc.getDataSet().destroy(event.getKey());
				}
	    	} else {
	    		List userDataList = (List)userData;
	    		int index = 0;
	    		for (DQueueEventImpl event : list) {
	    			if (index < userDataList.size()) {
	    				rfc.getDataSet().destroy(event.getKey(), userDataList.get(index++));
	    			} else {
	    				rfc.getDataSet().destroy(event.getKey());
	    			}
				}
	    	}
	    }
    }
    
    getStatistics().endObjectPoll(start);
  }

  public String getId() {
    return BASE_ID + this.dqueueId;
  }
}
