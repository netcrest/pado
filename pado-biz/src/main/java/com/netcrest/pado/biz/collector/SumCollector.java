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
package com.netcrest.pado.biz.collector;

import java.util.List;

import com.netcrest.pado.IGridCollector;

/**
 * Returns a sum of all numerical data received.
 * 
 * @author dpark
 * 
 * @param <T>
 */
public class SumCollector<T extends List<Number>, S> implements IGridCollector<List<Number>, Number>
{
	private Number sum = 0;

	@Override
	public void addResult(String gridId, List<Number> result)
	{
		if (result == null) {
			return;
		}
		for (Number val : result) {
			if (val instanceof Integer) {
				sum = sum.intValue() + val.intValue();
			} else if (val instanceof Long) {
				sum = sum.longValue() + val.longValue();
			} else if (val instanceof Short) {
				sum = sum.shortValue() + val.shortValue();
			} else if (val instanceof Float) {
				sum = sum.floatValue() + val.floatValue();
			} else if (val instanceof Double) {
				sum = sum.doubleValue() + val.doubleValue();
			} else if (val instanceof Byte) {
				sum = sum.byteValue() + val.byteValue();
			}
		}
	}

	@Override
	public Number getResult()
	{
		return sum;
	}

	@Override
	public void endResults()
	{
	}

	@Override
	public void clearResults()
	{
		sum = 0;
	}
}
