/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.link;

import java.util.List;

import com.netcrest.pado.IBiz;

/**
 * IDQueuBizLink links the main class loader to {@link IDQueueBiz}
 * @author dpark
 *
 * @param <V>
 */
public interface IDQueueBizLink<V> extends IBiz
{
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
	 */
	public boolean offer(Object type, V value);

	/**
	 * Returns the next available value in the <code>DQueue</code> for the input
	 * type without removing it from the queue. If no value is in the queue for
	 * the input type, it returns null.
	 * 
	 * @param type
	 *            The type of value to peek
	 * @return The next available value in the <code>DQueue</code>
	 */
	public V peek(Object type);

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
	public List<V> peek(Object type, int count);

	/**
	 * Returns the next available value in the <code>DQueue</code> for the input
	 * type without removing it from the queue. If no value is in the queue for
	 * the input type, a NoSuchElementException is thrown.
	 * 
	 * @param type
	 *            The type of value to retrieve
	 * @return the next available value in the <code>DQueue</code>
	 */
	public V element(Object type);

	/**
	 * Removes and returns the next available value in the <code>DQueue</code>.
	 * If no value is in the queue for the input type, it returns null.
	 * 
	 * @param type
	 *            The type of value to poll
	 * @return the next available value in the <code>DQueue</code>
	 */
	public V poll(Object type);

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
	public List<V> poll(Object type, int count);

	/**
	 * Removes and returns the next available value in the <code>DQueue</code>.
	 * If no value is in the queue for the input type, a NoSuchElementException
	 * is thrown.
	 * 
	 * @param type
	 *            The type of value to remove
	 * @return the next available value in the <code>DQueue</code>
	 */
	public V remove(Object type);

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
	 * Takes (removes) one or more next available values in the DQueue. If no
	 * value is in the queue for the input type, it returns false.
	 * 
	 * @param type
	 *            The type of value to remove
	 * @param count
	 *            The number of values to remove
	 * @return true if one or more next available values are removed from the
	 *         queue, false if the queue is empty of the specified type.
	 */
	public boolean take(Object type, int count);
}
