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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.netcrest.pado.util.GridUtil;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.internal.impl.GridService;
import com.netcrest.pado.pql.CompiledUnit.QueryLanguage;

public class PqlParser
{
	/*
	 * For a TopN query, the pattern is to have "rank by fieldName top N using providerKeyName" clause in the pql
	 *<ul>
	 * <li> fieldName is the fieldName of the target region/path where the text is to be mined from </li>
	 * <li> "top N" is optional, if not specified, 10 will be used for N, which returns maximum top 10 matches </li>
	 * <li> "using providerKeyName" is optional, if specified, a specific <code>ITextSearchProvider</code> will be used,
	 * 		if not, <code>TopNLuceneSearch</code> is used as default topN search provider </li>
	 *</ul>
	 * whitespace is ignored within the pattern. The clause can appear only once in pql
	 * 
	 */
	public static  final Pattern TOPN_PATTERN = Pattern.compile(".*(rank\\s+by\\s+(\\S*)(\\s+top\\s+(\\d*))?(\\s+using\\s*(\\S*))?).*");
	private static final String ESCAPED = "[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\*\\?]";
	private static final int DEFAULT_TOP_N = 10;
	
	private String pql;
	private String parsedQuery;
	private String gridIds[];
	private String paths[];
	private String fullPath;
	private QueryLanguage queryLanguage = QueryLanguage.NONE;

	public PqlParser(String pql)
	{
		this((String) null, pql);
	}

	public PqlParser(String fullPath, String pql)
	{
		if (pql == null) {
			return;
		}
		this.pql = pql;

		if (pql.matches(".*[=<>].*")) {
			queryLanguage = QueryLanguage.OQL;
		} else if (pql.matches(".*[?:].*")) {
			queryLanguage = QueryLanguage.LUCENE;
		} else {
			queryLanguage = QueryLanguage.OQL;
		}

		switch (queryLanguage) {
		case OQL:
			parsedQuery = parseOql(pql);
			if (paths != null && paths.length > 0) {
				this.fullPath = GridUtil.getFullPath(paths[0]);
			}
			if (this.fullPath == null) {
				this.fullPath = fullPath;
			}
			break;
		case LUCENE:
			parsedQuery = parseLucene(pql);
			if (paths != null && paths.length > 0) {
				this.fullPath = GridUtil.getFullPath(paths[0]);
			}
			if (this.fullPath == null) {
				this.fullPath = fullPath;
			}
			break;
		default:
			break;
		}
	}

	public PqlParser(GridService gridService, String pql, String defaultGridPath)
	{
		this((String) null, pql);
		if (gridService == null) {
			return;
		}
		String gridPath;
		if (paths == null || paths.length == 0) {
			gridPath = defaultGridPath;
		} else {
			gridPath = paths[0];
		}
		gridIds = gridService.getGridIds(gridPath);

		// Use the default grid ID if grid path is not defined.
		// This is to support VirtualPath
		if (gridIds.length == 0) {
			gridIds = new String[] { gridService.getDefaultGridId() };
		}
		String gridId = gridIds[0];
		fullPath = gridService.getFullPath(gridId, gridPath);
	}

	private String parseOql(String pql)
	{
		if (pql == null) {
			return null;
		}

		String parsedQuery = pql;
		int index = parsedQuery.indexOf('.');
		if (index != -1) {
			paths = new String[] { parsedQuery.substring(0, index) };
		}
		return parsedQuery;
	}

	private String parseLucene(String pql)
	{
		if (pql == null) {
			return null;
		}

		String parsedQuery = pql;
		int index = parsedQuery.indexOf('?');
		if (index != -1) {
			paths = new String[] { parsedQuery.substring(0, index) };
			parsedQuery = parsedQuery.substring(index + 1);
		}
		return parsedQuery;
	}

	public String getPql()
	{
		return pql;
	}

	public String getParsedQuery()
	{
		return parsedQuery;
	}

	public String[] getGridIds()
	{
		return this.gridIds;
	}

	public String[] getPaths()
	{
		return paths;
	}

	public String getFullPath()
	{
		return fullPath;
	}

	public String getFullPql()
	{
		if (parsedQuery == null) {
			return fullPath;
		} else {
			return fullPath + "?" + parsedQuery;
		}
	}

	public QueryLanguage getQueryLanguage()
	{
		return queryLanguage;
	}

	public static class OrderByQueryString
	{
		public String queryString;
		public String orderBy;
		public boolean isAscending = true;
	}

	public static OrderByQueryString getOrderBy(String queryString)
	{
		OrderByQueryString orderByQueryString = new OrderByQueryString();
		if (queryString != null) {
			queryString = queryString.trim();
			int index = queryString.toLowerCase().indexOf("order by");
			if (index == -1) {
				orderByQueryString.queryString = queryString;
			} else {
				StringBuffer buffer = new StringBuffer(queryString.length());
				buffer.append(queryString.substring(0, index));
				index = index + 8;
				String orderBy = queryString.substring(index).trim();
				index = orderBy.indexOf(' ');
				if (index != -1) {

					String rest = orderBy.substring(index).trim();
					orderBy = orderBy.substring(0, index);
					if (rest.toLowerCase().startsWith("desc")) {
						orderByQueryString.isAscending = false;
						rest = rest.substring(4);
					} else if (rest.toLowerCase().startsWith("asc")) {
						rest = rest.substring(3);
					}
					if (rest.length() > 0) {
						buffer.append(' ');
						buffer.append(rest);
					}
				}
				orderBy = orderBy.trim();
				orderByQueryString.queryString = buffer.toString().trim();
				
				if (queryString.toLowerCase().startsWith("select")) {
					// This is to support Map objects only
					int quoteIndex = orderBy.indexOf('\'');
					if (quoteIndex != -1) {
						orderBy = orderBy.substring(quoteIndex+1);
						quoteIndex = orderBy.indexOf('\'');
						if (quoteIndex != -1) {
							orderBy = orderBy.substring(0, quoteIndex);
							orderByQueryString.orderBy = orderBy;
						}
					}
				} else {
					orderByQueryString.orderBy = orderBy;
				}
			}
		}
		return orderByQueryString;
	}
	
	
	public static void processTopN (String queryString, GridQuery gridQuery) {
		if (queryString != null) {
			Matcher matcher = TOPN_PATTERN.matcher(queryString);
			if (matcher.find()) {
				gridQuery.setParameter(Constants.TOPN, true);
				String topNStr  = matcher.group(4);
				if (topNStr != null) {
					gridQuery.setFetchSize(Integer.valueOf(topNStr));
					gridQuery.setAggregationPageSize(Integer.valueOf(topNStr));
				} else {
					gridQuery.setFetchSize(Integer.valueOf(DEFAULT_TOP_N));
					gridQuery.setAggregationPageSize(DEFAULT_TOP_N);
				}

				gridQuery.setOrdered(false);
				gridQuery.setParameter(Constants.TEXT_SEARCH_TARGET_FIELD, matcher.group(2));
				
				String providerKey =  matcher.group(6);
				if (providerKey != null) {
					gridQuery.setParameter(Constants.TEXT_SEARCH_PROVIDER, providerKey);
				}
				
				//get the plain keyword
				String originalQS = queryString;
				originalQS = originalQS.replace(findTopNClause (originalQS), "");
				int index = originalQS.indexOf('?');
				if (index != -1) {
					originalQS = originalQS.substring(index + 1);
				}				//Escape any Lucene special symbols
				String escaped = originalQS.replaceAll(ESCAPED, "");
				gridQuery.setParameter(Constants.TEXT_SEARCH_PHRASE, escaped);
			}
		}
	}	
	
	public static String findTopNClause (String queryString) {
		String matchedClause = null;
		int i = 0;
		if (queryString != null) {
			Matcher matcher = TOPN_PATTERN.matcher(queryString);
			while (matcher.find()) {
				matchedClause = matcher.group(1);
				i ++;
			}
		}
		if (i == 1) return matchedClause;
		if (i > 1) {
			throw new RuntimeException("rank by can only appear once in the query");
		}
		return null;
	}
	
	public static boolean isTopN (GridQuery gridQuery) {
		if ((gridQuery.getParam(Constants.TOPN) != null)  && (Boolean) gridQuery.getParam(Constants.TOPN)) {
			return true;
		} else {
			if (findTopNClause (gridQuery.getQueryString()) != null) {
				return true;
			}
		}
		return false;
	}
	
	public static String getTextSearchProvider (GridQuery gridQuery) {
		if (gridQuery.getParam(Constants.TEXT_SEARCH_PROVIDER) != null) {
			return (String) gridQuery.getParam(Constants.TEXT_SEARCH_PROVIDER);
		}
		return null;
	}
	
	public static String getTextSearchTargetField (GridQuery gridQuery) {
		if (gridQuery.getParam(Constants.TEXT_SEARCH_TARGET_FIELD) != null) {
			return (String) gridQuery.getParam(Constants.TEXT_SEARCH_TARGET_FIELD);
		}
		return null;
	}	
	
	public static String getTextSearchPHRASE (GridQuery gridQuery) {
		if (gridQuery.getParam(Constants.TEXT_SEARCH_PHRASE) != null) {
			return (String) gridQuery.getParam(Constants.TEXT_SEARCH_PHRASE);
		}
		return null;
	}	
		
}
