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
package com.netcrest.pado.index.service;

import java.io.Serializable;
import java.util.List;

import com.netcrest.pado.index.exception.GridQueryResultSetExpiredException;

/**
 * IScrollableResultSet contains a set of data retrieved from the grid upon
 * execution of {@link GridQuery}. The cursor can be moved up or down a set at a
 * time by invoking {@link #previousSet()} or {@link #nextSet()}, respectively.
 * The cursor can also move to an absolute position by invoking
 * {@link #goToSet(int)}.
 * <p>
 * The fetch size can be changed at any time allowing the set to dynamically
 * grow or shrink. The set number and count are recalculated if the fetch size
 * changes.
 * 
 * @author dpark
 * 
 * @param <T>
 *            Record (row) type
 */
public interface IScrollableResultSet<T> extends Serializable
{
	// /**
	// * Moves the cursor to the given row number in
	// * this <code>ResultSet</code> object.
	// *
	// * <p>If the row number is positive, the cursor moves to
	// * the given row number with respect to the
	// * beginning of the result set. The first row is row 1, the second
	// * is row 2, and so on.
	// *
	// * <p>If the given row number is negative, the cursor moves to
	// * an absolute row position with respect to
	// * the end of the result set. For example, calling the method
	// * <code>absolute(-1)</code> positions the
	// * cursor on the last row; calling the method <code>absolute(-2)</code>
	// * moves the cursor to the next-to-last row, and so on.
	// *
	// * <p>An attempt to position the cursor beyond the first/last row in
	// * the result set leaves the cursor before the first row or after
	// * the last row.
	// *
	// * <p><B>Note:</B> Calling <code>absolute(1)</code> is the same
	// * as calling <code>first()</code>. Calling <code>absolute(-1)</code>
	// * is the same as calling <code>last()</code>.
	// *
	// * @param row the number of the row to which the cursor should move.
	// * A positive number indicates the row number counting from the
	// * beginning of the result set; a negative number indicates the
	// * row number counting from the end of the result set
	// * @return <code>true</code> if the cursor is moved to a position in this
	// * <code>ResultSet</code> object;
	// * <code>false</code> No Opt because cursor already moved to the first row
	// or at the
	// * last row
	// */
	// boolean absolute(int row);

	/**
	 * Moves the cursor to the next set.
	 * 
	 * @return true if the next set exists
	 * @throws GridQueryResultSetExpiredException
	 *             Thrown if the result set is expired and
	 *             {@link GridQuery#isThrowExceptionOnExpire()} is true.
	 */
	boolean nextSet() throws GridQueryResultSetExpiredException;

	/**
	 * Moves the cursor to the previous set.
	 * 
	 * @return true if the previous set exists
	 * @throws GridQueryResultSetExpiredException
	 *             Thrown if the result set is expired and
	 *             {@link GridQuery#isThrowExceptionOnExpire()} is true.
	 * @throws GridQueryResultSetExpiredException
	 *             Thrown if the result set is expired and
	 *             {@link GridQuery#isThrowExceptionOnExpire()} is true.
	 */
	boolean previousSet() throws GridQueryResultSetExpiredException;

	/**
	 * Moves the cursor to the specified index of the set.
	 * 
	 * @param index
	 *            The starting index of the set.
	 * 
	 * @return <code>true</code> if the cursor is moved to the specified index
	 *         and the set is scrollable, <code>false</code> the set is not
	 *         scrollable because it reached the end of the result set or the
	 *         result set has been expired.
	 * @throws GridQueryResultSetExpiredException
	 *             Thrown if the result set is expired and
	 *             {@link GridQuery#isThrowExceptionOnExpire()} is true.
	 */
	boolean goToSet(int index) throws GridQueryResultSetExpiredException;

	/**
	 * Sets the fetch size. The fetch size can be changed dynamically.
	 * 
	 * @param fetchSize
	 *            Fetch size
	 */
	void setFetchSize(int fetchSize);

	/**
	 * Returns the fetch size.
	 */
	int getFetchSize();

	/**
	 * Returns the total size of this result set. The returned value is the
	 * total number of rows in the entire result set.
	 */
	int getTotalSize();

	/**
	 * Returns the set count or the number of sets.
	 */
	int getSetCount();

	/**
	 * Returns the current set number. The set number begins from 1.
	 */
	int getSetNumber();

	/**
	 * Returns the current set size.
	 */
	int getSetSize();

	/**
	 * Returns the start index of the paginated result set.
	 * 
	 * @return Start index
	 */
	int getStartIndex();

	/**
	 * Returns the end index of the paginated result set.
	 */
	int getEndIndex();

	/**
	 * Returns the start index of the internal partial result set view index.
	 */
	int getViewStartIndex();

	/**
	 * Returns the end index of the internal partial result set view.
	 * 
	 * @return
	 */
	int getViewEndIndex();

	/**
	 * Returns the current index position. It returns -1 if the result set is
	 * empty or the end of the result set is reached.
	 */
	int getCurrentIndex();

	/**
	 * Returns the grid query that initiated this result set.
	 */
	GridQuery getQuery();

	/**
	 * Returns the results in the form of grid object list.
	 */
	List<T> toList();

	/**
	 * Returns domain object list.
	 */
	List<T> toDomainList();

	/**
	 * Returns value object list.
	 */
	List<T> toValueList();

	/**
	 * Dumps the result set to the console.
	 */
	void dump();

	/**
	 * Closes the result set. If closed, the result set is no longer valid.
	 * Although Index Matrix automatically removes resources upon result set
	 * expiration, it is a good practice to invoke this method to immediately
	 * release system resources when the this result set is no longer needed.
	 */
	void close();

	/**
	 * Returns true if the result set has not been created or has been closed.
	 */
	boolean isClosed();

}
