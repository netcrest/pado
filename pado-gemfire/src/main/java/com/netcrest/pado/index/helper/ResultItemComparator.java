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
package com.netcrest.pado.index.helper;

import java.util.Comparator;

import com.netcrest.pado.index.result.ResultItem;
import com.netcrest.pado.index.result.ValueInfo;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ResultItemComparator extends DynamicComparator<ResultItem>
{

	private Comparator dynamicComparator = null;

	public ResultItemComparator(Comparator comparator)
	{
		dynamicComparator = comparator;
	}

	@Override
	public int compare(ResultItem o1, ResultItem o2)
	{
		boolean o1Null = o1 == null;
		boolean o2Null = o2 == null;
		if (o1Null && o2Null)
			return 0;
		if (o1Null)
			return 1;
		if (o2Null)
			return -1;
		if (dynamicComparator != null)
			return dynamicComparator.compare(((ValueInfo) o1.getItem()).getValue(),
					((ValueInfo) o2.getItem()).getValue());
		if (((ValueInfo) o1.getItem()).getValue() instanceof Comparable) {
			return ((Comparable) ((ValueInfo) o1.getItem()).getValue()).compareTo(((Comparable) ((ValueInfo) o2
					.getItem()).getValue()));
		}
		return 0;
	}

}
