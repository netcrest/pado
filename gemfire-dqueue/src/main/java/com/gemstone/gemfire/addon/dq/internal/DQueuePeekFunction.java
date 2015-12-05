package com.gemstone.gemfire.addon.dq.internal;

import java.util.ArrayList;
import java.util.List;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.internal.cache.CachedDeserializable;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;

@SuppressWarnings("serial")
public class DQueuePeekFunction extends DQueueAbstractPullFunction {

  public static final String BASE_ID = "peek_";

  public DQueuePeekFunction(String dqueueId, Cache cache) {
    super(dqueueId, cache);
  }

  public DQueuePeekFunction(DQueuePullDispatcherImpl dispatcher) {
    super(dispatcher);
  }

  @Override
  public void execute(FunctionContext context) {
    long start = getStatistics().startObjectPeek();
    RegionFunctionContext rfc = (RegionFunctionContext) context;
    
    // Get the type
    Object type = getType(rfc);
    
    // Get arg
    Object args[] = (Object[])rfc.getArguments();
    int count = -1;
    if (args != null && args.length > 0) {
      count = (Integer)args[0];
    }

    // Do failover processing
    handleFailover((PartitionedRegion) rfc.getDataSet(), type);

    // Peek the head of the queue for the type
    if (count == -1) {
    	DQueueEventImpl event = (DQueueEventImpl) this.dispatcher.peek(type);
	    if (this.cache.getLogger().fineEnabled()) {
	      this.cache.getLogger().fine("Function " + getId() + " on type " + type + " peeked: " + event);
	    }
	    // Return the result (serialized)
	    Object raw = null;
	    if (event != null) {
	    	raw = event.getRawValue();
		    if (raw instanceof CachedDeserializable) {
		    	CachedDeserializable pbcd = (CachedDeserializable)raw;
		    	raw = pbcd.getSerializedValue();
		    }
	    }
	    context.getResultSender().lastResult(raw == null ? null : raw);
    } else {
    	List<DQueueEventImpl> list = (List<DQueueEventImpl>)this.dispatcher.peek(type, count);
    	if (this.cache.getLogger().fineEnabled()) {
    		int listSize = 0;
    		if (list != null) {
    			listSize = list.size();
    		}
  	      this.cache.getLogger().fine("Function " + getId() + " on type " + type + " peeked: " + listSize + " objects, requested " + count);
  	    }
    	List resultList = null;
    	if (list != null) {
    		resultList = new ArrayList(list.size());
    		for (DQueueEventImpl event : list) {
    			Object raw = event.getRawValue();
    		    if (raw instanceof CachedDeserializable) {
    		    	CachedDeserializable pbcd = (CachedDeserializable)raw;
    		    	raw = pbcd.getSerializedValue();
    		    }
    		    if (raw != null) {
    		    	resultList.add(raw);
    		    };
			}
    	}
    	// Return the result list (serialized)
	    context.getResultSender().lastResult(resultList);
    }
    
    getStatistics().endObjectPeek(start);
  }

  public String getId() {
    return BASE_ID + this.dqueueId;
  }
}
