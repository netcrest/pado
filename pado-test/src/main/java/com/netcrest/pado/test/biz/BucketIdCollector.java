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
package com.netcrest.pado.test.biz;

import java.util.ArrayList;
import java.util.List;

import com.netcrest.pado.IGridCollector;

public class BucketIdCollector implements IGridCollector<List<int[]>, int[]>
{
	List<int[]> list = new ArrayList();
	
	@Override
	public void addResult(String gridId, List<int[]> result)
	{
		list.addAll(result);
	}

	@Override
	public int[] getResult()
	{
		int count = 0;
		for (int[] vals : list) {
			count += vals.length;
		}
		int bucketIds[] = new int[count];
		int startIndex = 0;
		for (int[] vals : list) {
			System.arraycopy(vals, 0, bucketIds, startIndex, vals.length);
			startIndex += vals.length;
		}
		return bucketIds;
	}
	
	@Override
	public void endResults()
	{
	}
	
	@Override
	public void clearResults()
	{
		list.clear();
	}
}
