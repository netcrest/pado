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
package com.netcrest.pado.index.internal;

import com.netcrest.pado.index.result.MemberResults;
import com.netcrest.pado.index.result.ValueInfo;


public class ResultEntry implements Comparable<ResultEntry>
{
	private ValueInfo valueInfo;
	private MemberResults memberResults;


	/**
	 * @param value
	 * @param memberResults
	 */
	public ResultEntry(ValueInfo valueInfo, MemberResults memberResults)
	{
		super();
		this.valueInfo = valueInfo;
		this.memberResults = memberResults;
	}

	public ValueInfo getValueInfo()
	{
		return valueInfo;
	}

	public void setValueInfo(ValueInfo valueInfo)
	{
		this.valueInfo = valueInfo;
	}

	public MemberResults getMemberResults()
	{
		return memberResults;
	}

	public void setMemberResults(MemberResults memberResults)
	{
		this.memberResults = memberResults;
	
	}

	@Override
	public int compareTo(ResultEntry anotherResultEntry)
	{
		if (valueInfo == null) {
			return -1;
		} else if (anotherResultEntry == null) {
			return 1;
		} else {
			return valueInfo.compareTo(anotherResultEntry.getValueInfo());
		}
	}
}
