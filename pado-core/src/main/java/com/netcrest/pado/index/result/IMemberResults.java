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
package com.netcrest.pado.index.result;

import java.io.Serializable;
import java.util.List;

/**
 * IMemberResults provides Index Matrix member information.
 * 
 * @author dpark
 * 
 */
public interface IMemberResults extends Serializable
{
	/**
	 * Returns the bucket ID used to determine data affinity, i.e., the target
	 * member.
	 */
	int getBucketId();

	/**
	 * Returns the current batch index on the server (member).
	 * 
	 * @return
	 */
	int getCurrentBatchIndexOnServer();

	/**
	 * Returns the total size of the result set stored in the member's L2 cache.
	 * 
	 * @return
	 */
	int getTotalSizeOnServer();

	/**
	 * Returns the next batch index number.
	 */
	int getNextBatchIndexOnServer();

	/**
	 * Sets the next batch index number.
	 * 
	 * @param index
	 *            Index number
	 */
	void setNextBatchIndexOnServer(int index);

	/**
	 * Sets the member bucket ID.
	 * 
	 * @param bucketId
	 *            Bucket ID
	 */
	void setBucketId(int bucketId);

	/**
	 * Returns the results (set or page).
	 */
	List<? extends Object> getResults();

}
