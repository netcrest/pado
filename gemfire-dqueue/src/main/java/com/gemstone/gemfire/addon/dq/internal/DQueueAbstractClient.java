package com.gemstone.gemfire.addon.dq.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.gemstone.gemfire.addon.dq.DQueue;
import com.gemstone.gemfire.addon.dq.DQueueAttributes;
import com.gemstone.gemfire.addon.dq.DQueueException;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.internal.cache.CachedDeserializable;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;
import com.gemstone.gemfire.internal.util.BlobHelper;

/**
 * Class <code>DQueueAbstractClient</code> is the abstract client implementation
 * of the <code>DQueue</code> interface.
 */
public abstract class DQueueAbstractClient extends DQueueEntity implements DQueue {

  /**
   * The <code>Function</code> providing the offer behavior
   */
  protected DQueueOfferFunction offerFunction;

  /**
   * The <code>Function</code> providing the poll behavior
   */
  protected DQueuePollFunction pollFunction;
  
  /**
   * The <code>Function</code> providing the take behavior
   */
  protected DQueueTakeFunction takeFunction;

  /**
   * The <code>Function</code> providing the peek behavior
   */
  protected DQueuePeekFunction peekFunction;

  /**
   * Constructor. Creates a new <code>DQueue</code> using the input
   * attributes.
   *
   * @param cache
   *          The <code>Cache</code>
   * @param id
   *          The id
   * @param attributes
   *          The <code>DQueueAttributes</code> used to configure this
   *          instance
   */
  public DQueueAbstractClient(Cache cache, String id,
      DQueueAttributes attributes) {
    super(cache, id, attributes);
    initialize();
  }

  ////////////////////// DQueue Interface Methods ///////////////////

  public boolean offer(Object type, Object value)
  {
	return offer(type, value, null);  
  }
  
  @SuppressWarnings({ "rawtypes" })
  public boolean offer(Object type, Object value, Object userData) {
    long start=0, end=0;
    if (this.cache.getLogger().fineEnabled()) {
      start = System.currentTimeMillis();
    }
    DQueueOfferFunctionArguments arguments = new DQueueOfferFunctionArguments(this.memberId, value, userData);

    int attempt=0;
    while (true) {
      try {
        // Set posDup if necessary
        if (attempt > 0) {
          arguments.setPossibleDuplicate(true);
        }

        // Build and execute the Execution
        Execution execution = FunctionService.onRegion(this.dqRegion).withFilter(Collections.singleton(type)).withArgs(arguments);
        ResultCollector collector = execution.execute(this.offerFunction.getId());

        // Get the result if necessary. This throws any exception that occurs on the server.
        if (this.offerFunction.hasResult()) {
          collector.getResult();
        }
        break;
      } catch (FunctionException e) {
        // Retry if necessary when a FunctionException occurs
        handleException(type, value, e, attempt);
        attempt++;
      }
    }

    if (this.cache.getLogger().fineEnabled()) {
      end = System.currentTimeMillis();
      StringBuffer buffer = new StringBuffer()
        .append(this)
        .append(": Executed function named ")
        .append(this.offerFunction.getId())
        .append(" for ")
        .append(this.memberId)
        .append("->")
        .append(value)
        .append(" in ")
        .append(end - start)
        .append(" ms");
      this.cache.getLogger().fine(buffer.toString());
    }
    return true;
  }

  public Object peek(Object type) {
    return execute(this.peekFunction, type);
  }
  
  public List peek(Object type, int count) {
    return (List)execute(this.peekFunction, type, count);
  }

  public Object poll(Object type) {
    return execute(this.pollFunction, type);
  }
  
  public List poll(Object type, int count) {
    return (List)execute(this.pollFunction, type, count);
  }
  
  public Object poll(Object type, Object userData) {
	  return execute(this.pollFunction, type, -1, userData);
  }
  
  public List poll(Object type, int count, List userDataList) {
    return (List)execute(this.pollFunction, type, count, userDataList);
  }
  
  public boolean take(Object type) {
    return take(type, -1);
  }
  
  public boolean take(Object type, int count) {
    return (Boolean)execute(this.takeFunction, type, count);
  }
  
  public boolean take(Object type, Object userData) {
	  return (Boolean)execute(this.takeFunction, type, -1, userData);
  }
  
  public boolean take(Object type, int count, List userDataList) {
    return (Boolean)execute(this.takeFunction, type, count, userDataList);
  }

  @SuppressWarnings("rawtypes")
  protected Object execute(Function function, Object type, Object...args) {
    long start=0, end=0;
    if (this.cache.getLogger().fineEnabled()) {
      start = System.currentTimeMillis();
    }

    // Build and execute the Execution
    Execution execution = FunctionService.onRegion(this.dqRegion).withFilter(Collections.singleton(type)).withArgs(args);
    ResultCollector collector = null;
    try {
      collector = execution.execute(function.getId());
    } catch (FunctionException e) {
      // If the function hasn't been registered on the server, let the caller
      // know to verify the correct dispatcher has been created. If the exception message is one of the potential ones below, 
      Throwable exceptionToValidate = e;
      if (e.getCause() != null) {
        exceptionToValidate = e.getCause();
      }
      String potentialMessage1 = LocalizedStrings.ExecuteRegionFunction_THE_FUNCTION_0_HAS_NOT_BEEN_REGISTERED
          .toLocalizedString(function.getId());
      String potentialMessage2 = LocalizedStrings.ExecuteFunction_FUNCTION_NAMED_0_IS_NOT_REGISTERED
          .toLocalizedString(function.getId());
      throw (exceptionToValidate.getMessage().equals(potentialMessage1)
            || exceptionToValidate.getMessage().equals(potentialMessage2)) 
              ? new IllegalStateException(
                  "Function "
                      + function.getId()
                      + " failed due to the following exception: "
                      + exceptionToValidate.getMessage()
                      + ". Verify that a pull dispatcher has been created on the server")
              : e;
    }

    // Get the result. This will return a list of 0 or 1 elements.
    List result = (List) collector.getResult();
    Object obj = null;
    if (result.size() == 1) {
      // If it returns 1 element, get the element. If it is a
      // CachedDeserializable, deserialize it before returning it.
      obj = result.get(0);
      if (obj instanceof CachedDeserializable) {
        obj = ((CachedDeserializable) obj).getDeserializedForReading();
      } else if (obj instanceof byte[]) {
    	try {
    		SerializedObject so = (SerializedObject)BlobHelper.deserializeBlob((byte[])obj);
    		obj = so.getDeserializedObject();
		} catch (Exception ex) {
		  this.cache.getLogger().error(ex);
		  obj = null;
		}
      }
    } else {
      List list = new ArrayList(result.size());
      for (Object object : result) {
        if (object instanceof CachedDeserializable) {
          object = ((CachedDeserializable) object).getDeserializedForReading();
        } else if (object instanceof byte[]) {
        	try {
        	  SerializedObject so = (SerializedObject)BlobHelper.deserializeBlob((byte[])obj);
        	  list.add(so.getDeserializedObject());
          } catch (Exception ex) {
      	    this.cache.getLogger().error(ex);
      	  }
        }
      }
      obj = list;
    }

    if (this.cache.getLogger().fineEnabled()) {
      end = System.currentTimeMillis();
      StringBuffer buffer = new StringBuffer()
        .append(this)
        .append(": Function ")
        .append(function.getId())
        .append(" for ")
        .append(this.memberId)
        .append(" returned ")
        .append(obj)
        .append(" in ")
        .append(end - start)
        .append(" ms");
      this.cache.getLogger().fine(buffer.toString());
    }

    return obj;
  }

  public Object remove(Object type) {
    Object obj = poll(type);
    if (obj == null) {
      throw new NoSuchElementException("No value exists in DQueue " + getId() + " for type " + type);
    } else {
      return obj;
    }
  }
  
  public Object remove(Object type, Object userData) {
    Object obj = poll(type, userData);
    if (obj == null) {
      throw new NoSuchElementException("No value exists in DQueue " + getId() + " for type " + type);
    } else {
      return obj;
    }
  }

  public Object element(Object type) {
    Object obj = peek(type);
    if (obj == null) {
      throw new NoSuchElementException("No value exists in DQueue " + getId() + " for type " + type);
    } else {
      return obj;
    }
  }

  public void dumpQueues() {
    FunctionService.onRegion(this.dqRegion).execute(DQueueDumpQueuesFunction.ID);
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer
      .append(getClass().getSimpleName())
      .append("[")
      .append("id=").append(this.id)
      .append("; attributes=").append(this.dqAttributes)
      .append("]");
    return buffer.toString();
  }

  public String toShortString() {
    StringBuffer buffer = new StringBuffer();
    buffer
      .append(getClass().getSimpleName())
      .append("[")
      .append("id=").append(this.id)
      .append("]");
    return buffer.toString();
  }

  protected void initialize() {
    // Create the region
    initializeRegion();
    
    // Create and register the offer function
    this.offerFunction = new DQueueOfferFunction(this.id, this.cache);
    if (!FunctionService.isRegistered(this.offerFunction.getId())) {
      FunctionService.registerFunction(this.offerFunction);
    }
    
    // Create and register the poll function
    this.pollFunction = new DQueuePollFunction(this.id, this.cache);
    if (!FunctionService.isRegistered(this.pollFunction.getId())) {
      FunctionService.registerFunction(this.pollFunction);
    }
    
    // Create and register the take function
    this.takeFunction = new DQueueTakeFunction(this.id, this.cache);
    if (!FunctionService.isRegistered(this.takeFunction.getId())) {
      FunctionService.registerFunction(this.takeFunction);
    }
    
    // Create and register the peek function
    this.peekFunction = new DQueuePeekFunction(this.id, this.cache);
    if (!FunctionService.isRegistered(this.peekFunction.getId())) {
      FunctionService.registerFunction(this.peekFunction);
    }
  }
  
  private void handleException(Object type, Object value, FunctionException e, int attempt) {
    Throwable cause = e.getCause();
    if (cause == null) {
      throw new DQueueException(e);
    }
    String message = toShortString() + ": Caught exception attempting to offer " + type + "->" + value + ". Retrying.";
    System.out.println(message + e);
    this.cache.getLogger().warning(message, e);
  }

  protected abstract void initializeRegion();
}
