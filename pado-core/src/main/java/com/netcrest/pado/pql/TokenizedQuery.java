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
package com.netcrest.pado.pql;

public class TokenizedQuery
{
	public enum QueryLanguage {
		OQL, LUCENE, PQL
	}
	
	private String gridId;
	private String gridPath;
	private String queryString;
	private QueryLanguage queryLanguage;
	
	public String getGridId()
	{
		return gridId;
	}
	public void setGridId(String gridId)
	{
		this.gridId = gridId;
	}
	public String getGridPath()
	{
		return gridPath;
	}
	public void setGridPath(String gridPath)
	{
		this.gridPath = gridPath;
	}
	public String getQueryString()
	{
		return queryString;
	}
	public void setQueryString(String queryString)
	{
		this.queryString = queryString;
	}
	public QueryLanguage getQueryLanguage()
	{
		return queryLanguage;
	}
	public void setQueryLanguage(QueryLanguage queryLanguage)
	{
		this.queryLanguage = queryLanguage;
	}
	
}
