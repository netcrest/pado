package com.gemstone.gemfire.addon.dq;

import java.util.List;

import com.gemstone.gemfire.cache.CacheListener;

/**
 * Class <code>DQueue</code> provides queue-like semantics to a region.
 */
public interface DQueue
{

	/**
	 * The prefix used for the <code>DQueue</code>'s underlying
	 * <code>Region</code> name
	 */
	public static final String NAME_PREFIX = "__dqueue_";

	/**
	 * Adds the specified value to the <code>DQueue</code>.
	 * 
	 * @param type
	 *            The type used by the underlying dispatcher to load-balance
	 *            consumer threads.
	 * @param value
	 *            The value to add to the DQueue.
	 * @return boolean whether the value was successfully added to the
	 *         <code>DQueue</code>.
	 * @throws DQueueException
	 *             if any underlying exception occurs
	 */
	public boolean offer(Object type, Object value);

	/**
	 * Adds the specified value to the <code>DQueue</code>.
	 * 
	 * @param type
	 *            The type used by the underlying dispatcher to load-balance
	 *            consumer threads.
	 * @param value
	 *            The value to add to the DQueue.
	 * @param userData
	 *            The optional user data passed to the client along with the
	 *            value.
	 * @return boolean whether the value was successfully added to the
	 *         <code>DQueue</code>.
	 * @throws DQueueException
	 *             if any underlying exception occurs
	 */
	public boolean offer(Object type, Object value, Object userData);

	/**
	 * Returns the next available value in the <code>DQueue</code> for the input
	 * type without removing it from the queue. If no value is in the queue for
	 * the input type, it returns null.
	 * 
	 * @param type
	 *            The type of value to peek
	 * @return The next available value in the <code>DQueue</code>
	 */
	public Object peek(Object type);

	/**
	 * Returns one or more next available values in the <code>DQueue</code> for
	 * the input type without removing it from the queue. If no value is in the
	 * queue for the input type, it returns null.
	 * 
	 * @param type
	 *            The type of value to peek
	 * @param count
	 *            The maximum number of values to peek.
	 * @return A list of next available values in the <code>DQueue</code>
	 */
	public List peek(Object type, int count);

	/**
	 * Returns the next available value in the <code>DQueue</code> for the input
	 * type without removing it from the queue. If no value is in the queue for
	 * the input type, a NoSuchElementException is thrown.
	 * 
	 * @param type
	 *            The type of value to retrieve
	 * @return the next available value in the <code>DQueue</code>
	 */
	public Object element(Object type);

	/**
	 * Removes and returns the next available value in the <code>DQueue</code>.
	 * If no value is in the queue for the input type, it returns null.
	 * 
	 * @param type
	 *            The type of value to poll
	 * @return the next available value in the <code>DQueue</code>
	 */
	public Object poll(Object type);

	/**
	 * Removes and returns the next available value in the <code>DQueue</code>.
	 * If no value is in the queue for the input type, it returns null.
	 * 
	 * @param type
	 *            The type of value to poll
	 * @param userData Optional userData passed on to
	 *            {@link CacheListener#afterDestroy(com.gemstone.gemfire.cache.EntryEvent)}
	 *            as a callback argument. This argument is useful if an
	 *            additional action must be carried out upon removal of the
	 *            entry. For example, after trapping the destroy event, the
	 *            server can send a response back to the original caller that
	 *            invoked offer for the given type and value. The caller can
	 *            optionally receive the response from the client that has taken
	 *            the value. To get userData, {@link CacheListener} must be
	 *            implemented and added to the DQueue region. 
	 * @return the next available value in the <code>DQueue</code>
	 */
	public Object poll(Object type, Object userData);

	/**
	 * Removes and returns the next available value in the <code>DQueue</code>.
	 * If no value is in the queue for the input type, it returns null.
	 * 
	 * @param type
	 *            The type of value to poll
	 * @param count
	 *            The maximum number of values to poll
	 * @return A list of next available values in the <code>DQueue</code>
	 */
	public List poll(Object type, int count);

	/**
	 * Removes and returns one or more next available values in the <code>DQueue</code>.
	 * If no value is in the queue for the input type, it returns null.
	 * 
	 * @param type
	 *            The type of value to poll
	 * @param count
	 *            The maximum number of values to poll.
	 * @param userDataList
	 *            Optional list containing userData objects. 
	 * @return A list of next available values in the <code>DQueue</code>
	 */
	public List poll(Object type, int count, List userDataList);

	/**
	 * Removes and returns the next available value in the <code>DQueue</code>.
	 * If no value is in the queue for the input type, a NoSuchElementException
	 * is thrown.
	 * 
	 * @param type
	 *            The type of value to remove
	 * @return the next available value in the <code>DQueue</code>
	 */
	public Object remove(Object type);

	/**
	 * Removes and returns the next available value in the <code>DQueue</code>.
	 * If no value is in the queue for the input type, a NoSuchElementException
	 * is thrown.
	 * 
	 * @param type
	 *            The type of value to remove
	 * @param userData
	 *            Optional userData passed on to
	 *            {@link CacheListener#afterDestroy(com.gemstone.gemfire.cache.EntryEvent)}
	 *            as a callback argument. This argument is useful if an
	 *            additional action must be carried out upon removal of the
	 *            entry. For example, after trapping the destroy event, the
	 *            server can send a response back to the original caller that
	 *            invoked offer for the given type and value. The caller can
	 *            optionally receive the response from the client that has taken
	 *            the value. To get userData, {@link CacheListener} must be
	 *            implemented and added to the DQueue region. 
	 * @return the next available value in the <code>DQueue</code>
	 */
	public Object remove(Object type, Object userData);

	/**
	 * Takes (removes) the next available value in the DQueue. If no value is in
	 * the queue for the input type, it returns null.
	 * 
	 * @param type
	 *            The type of value to remove
	 * @return true if the next available value is removed from the queue, false
	 *         if the queue is empty of the specified type.
	 */
	public boolean take(Object type);

	/**
	 * Takes (removes) the next available value in the DQueue. If no value is in
	 * the queue for the input type, false is returned.
	 * 
	 * @param type
	 *            The type of value to remove
	 * @param userData
	 *            Optional userData passed on to
	 *            {@link CacheListener#afterDestroy(com.gemstone.gemfire.cache.EntryEvent)}
	 *            as a callback argument. This argument is useful if an
	 *            additional action must be carried out upon removal of the
	 *            entry. For example, after trapping the destroy event, the
	 *            server can send a response back to the original caller that
	 *            invoked offer for the given type and value. The caller can
	 *            optionally receive the response from the client that has taken
	 *            the value. 
	 * @return true if the next available value is removed from the queue, false
	 *         if the queue is empty of the specified type.
	 */
	public boolean take(Object type, Object userData);

	/**
	 * Takes (removes) one or more next available values in the DQueue. If no value is in
	 * the queue for the input type, it returns false.
	 * 
	 * @param type
	 *            The type of value to remove
	 * @param count
	 *            The number of values to remove
	 * @return true if one or more next available values are removed from the
	 *         queue, false if the queue is empty of the specified type.
	 */
	public boolean take(Object type, int count);

	/**
	 * Takes (removes) the next available value in the DQueue. If no value is in
	 * the queue for the input type, it returns false.
	 * 
	 * @param type
	 *            The type of value to remove
	 * @param count
	 *            The number of values to remove
	 * @param Optional list containing userData objects. 
	 * @return true if one or more next available values are removed from the
	 *         queue, false if the queue is empty of the specified type.
	 */
	public boolean take(Object type, int count, List userDataList);
}
