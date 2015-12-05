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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.index.provider.lucene.DateTool;
import com.netcrest.pado.index.provider.lucene.LuceneSearch;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.util.GridUtil;

/**
 * CompiledUnit precompiles the specified PQL (Pado Query Language) to be
 * executed during run-time. PQL is a hybrid language of the following query
 * languages supported by Pado.
 * <ul>
 * <li>Lucene</li>
 * <li>GemFire OQL</li>
 * </ul>
 * <p>
 * PQL has the following format:
 * <p>
 * <b>Lucene</b>
 * <p>
 * 
 * <pre>
 *   &lt;path&gt;?&ltattr1&gt;:&lt;value1&gt; AND|OR &ltattr2&gt;:&lt;value2&gt;...
 * </pre>
 * <p>
 * where
 * <p>
 * 
 * <pre>
 *   &lt;path1&gt; is a Pado grid path. Note that only one grid path is allowed.
 *   &lt;attr1&gt; is a case-sensitive attribute name.
 *   &lt;value1&gt; is the value of the attribute in Lucene format.
 * </pre>
 * <p>
 * <b>OQL</b>
 * <p>
 * 
 * <pre>
 *   &lt;path1&gt;.&ltattr1&gt;&lt;conditional operator&gt;&lt;value1&gt; AND|OR &lt;path2&gt;.&ltattr2&gt;&lt;conditional operator&gt;&lt;value2&gt;...
 * </pre>
 * <p>
 * where
 * <p>
 * 
 * <pre>
 *   &lt;path1&gt; is a Pado grid path. Note that if more than one grid path is specified
 *           then the paths must be co-located for the query to properly execute.
 *   &lt;attr1&gt; is a case-sensitive attribute name.
 *   &lt;conditional operator&gt; is one of =, &lt;, &gt;, &lt;=, &gt;=, &lt;&gt;.
 *   &lt;value1&gt; is the value of the attribute in OQL format.
 * </pre>
 * 
 * @author dpark
 * 
 */
public class CompiledUnit
{
	@Override
	public String toString()
	{
		return "CompiledUnit [dateResolution=" + dateResolution + ", pql=" + pql + ", compiledQuery=" + compiledQuery
				+ ", temporalIdentityQuery=" + temporalIdentityQuery + ", compiledTemporalQuery="
				+ compiledTemporalQuery + ", attributes=" + Arrays.toString(attributes) + ", paths="
				+ Arrays.toString(paths) + ", fullPaths=" + Arrays.toString(fullPaths) + ", isPathOnly=" + isPathOnly
				+ ", queryLanguage=" + queryLanguage + ", resultIndexes=" + Arrays.toString(resultIndexes)
				+ ", isInputOnly=" + isInputOnly + "]";
	}

	private final static String TIME_QUERY_PREDICATE = "e.key.StartValidTime<=%dL AND %dL<e.key.EndValidTime AND e.key.WrittenTime<=%dL";

	private DateTool.Resolution dateResolution = DateTool.Resolution.DAY;
	private String pql;
	private String compiledQuery;
	private String temporalIdentityQuery;
	private String temporalKeyQuery;
	private String compiledTemporalQuery;
	private Object[] attributes;
	private String[] paths;
	private String[] fullPaths;
	private boolean isPathOnly;
	private QueryLanguage queryLanguage = QueryLanguage.NONE;

	public enum QueryLanguage
	{
		OQL, LUCENE, NONE
	}

	/**
	 * Constructs a CompiledUnit object that compiles the specified PQL for the
	 * specified key type. CU is created per KeyType constant. It is not for the
	 * entire KeyType constants.
	 * 
	 * @param pql
	 *            Pado Query Language
	 * @param keyType
	 *            Key type
	 */
	public CompiledUnit(String pql, KeyType keyType)
	{
		compile(pql, keyType);
	}
	
	/**
	 * Invoked by VirtualCompiledUnit
	 * @param pql
	 */
	public CompiledUnit(String pql)
	{
		compileInput(pql);
	}

	public void setTimePrecision(DateTool.Resolution resolution)
	{
		dateResolution = resolution;
	}

	public DateTool.Resolution getDateResolution()
	{
		return dateResolution;
	}

	public String getPql()
	{
		return pql;
	}

	public String getCompiledQuery()
	{
		return compiledQuery;
	}

	public String getTemporalIdentityQuery()
	{
		return temporalIdentityQuery;
	}

	public Object[] getAttributes()
	{
		return attributes;
	}
	
	public String getQuery(Object inputObject)
	{
		if (inputObject == null) {
			return null;
		}
		return null;
	}

	public String getQuery(KeyMap<?> keyMap)
	{
		Object args[] = new Object[attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			args[i] = keyMap.get((KeyType) attributes[i]);
		}
		return String.format(compiledQuery, args);
	}

	public String getTemporalQuery(KeyMap<?> keyMap)
	{
		Object args[] = new Object[attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			args[i] = keyMap.get((KeyType) attributes[i]);
		}
		return String.format(compiledTemporalQuery, args);
	}
	
	public Object[] getTemporalArgs(KeyMap<?> keyMap, long validAt, long asOf)
	{
		if (paths == null) {
			return null;
		}
		
		if (validAt == -1) {
			validAt = System.currentTimeMillis();
			if (asOf == -1) {
				asOf = validAt;
			}
		}
		if (asOf == -1) {
			asOf = System.currentTimeMillis();
		}
		
		Object args[] = null;
		int index;
		switch (queryLanguage) {
		case OQL:
			args = new Object[paths.length * 3 + attributes.length];
			if (keyMap == null) {
				 index = attributes.length;
			} else {
				for (index = 0; index < attributes.length; index++) {
					args[index] = keyMap.get((KeyType) attributes[index]);
				}
			}
			for (int i = 0; i < paths.length; i++) {
				args[index++] = validAt;
				args[index++] = validAt;
				args[index++] = asOf;
			}
			break;
		case LUCENE:
			args = new Object[paths.length * 4 + attributes.length];
			if (keyMap == null) {
				 index = attributes.length;
			} else {
				for (index = 0; index < attributes.length; index++) {
					args[index] = keyMap.get((KeyType) attributes[index]);
				}
			}
			String validAtStr = DateTool.timeToString(validAt, dateResolution);
			String asOfStr = DateTool.timeToString(asOf, dateResolution);
			for (int i = 0; i < paths.length; i++) {
				args[index++] = validAtStr;
				args[index++] = validAtStr;
				args[index++] = asOfStr;
				args[index++] = asOfStr;
			}
			break;
		default:
			return null;
		}
		
		return args;
	}
	
	public String getTemporalIdentityQuery(long validAt, long asOf)
	{
		return getTemporalIdentityQuery(null, validAt, asOf);
	}

	public String getTemporalIdentityQuery(KeyMap<?> keyMap, long validAt, long asOf)
	{
		Object[] args = getTemporalArgs(keyMap, validAt, asOf);
		if (args == null) {
			return null;
		}
		return String.format(temporalIdentityQuery, args);
	}
	
	public String getTemporalKeyQuery(long validAt, long asOf)
	{
		return getTemporalKeyQuery(null, validAt, asOf);
	}
	
	public String getTemporalKeyQuery(KeyMap<?> keyMap, long validAt, long asOf)
	{
		Object[] args = getTemporalArgs(keyMap, validAt, asOf);
		if (args == null) {
			return null;
		}
		return String.format(temporalKeyQuery, args);
	}
	
	@SuppressWarnings("rawtypes")
	public String getTemporalIdentityQuery(Object input, List[] results, long validAt, long asOf)
	{
		if (paths == null) {
			return null;
		}
		Object args[];
		int index;
		switch (queryLanguage) {
		case OQL:
			args = new Object[paths.length * 3 + attributes.length];
			if (input instanceof KeyMap) {
				if (fillQueryArgs((KeyMap)input, results, args) == false) {
					return null;
				}
			} else {
				if (fillQueryArgs(input, results, args) == false) {
					return null;
				}
			}
			index = attributes.length;
			for (int i = 0; i < paths.length; i++) {
				args[index++] = validAt;
				args[index++] = validAt;
				args[index++] = asOf;
			}
			break;
		case LUCENE:
			args = new Object[paths.length * 4 + attributes.length];
			if (input instanceof KeyMap) {
				if (fillQueryArgs((KeyMap)input, results, args) == false) {
					return null;
				}
			} else {
				if (fillQueryArgs(input, results, args) == false) {
					return null;
				}
			}
			index = attributes.length;
			String validAtStr = DateTool.timeToString(validAt, dateResolution);
			String asOfStr = DateTool.timeToString(asOf, dateResolution);
			for (int i = 0; i < paths.length; i++) {
				args[index++] = validAtStr;
				args[index++] = validAtStr;
				args[index++] = asOfStr;
				args[index++] = asOfStr;
			}
			break;
		default:
			return null;
		}

		return String.format(temporalIdentityQuery, args);
	}

	public boolean isPathOnly()
	{
		return isPathOnly;
	}

	public String[] getPaths()
	{
		return paths;
	}

	public String[] getFullPaths()
	{
		return fullPaths;
	}

	public QueryLanguage getQueryLanguage()
	{
		return queryLanguage;
	}

	private String getAttribute(String token)
	{
		return token.substring(2, token.length() - 1);
	}
	
	/*
	 * Returns a non-null variable list by parsing the specified pql query
	 * string. It tokenizes all variables in the same order found in pql.
	 */
	private List<String> getVariableList(String pql)
	{
		// Parse. Collect all variables.
		ArrayList<String> variableList = new ArrayList<String>(5);
		String str = pql;
		int index = str.indexOf("${");
		while (index != -1) {
			int closeIndex = str.indexOf("}");
			if (closeIndex != -1) {
				String token = str.substring(index, closeIndex + 1);
				variableList.add(token);
				str = str.substring(closeIndex + 1);
			}
			index = str.indexOf("${");
		}
		return variableList;
	}
	
	// portfolio?(${AccountId} AND BankId:${BankId}) OR (${
	private List<String> getAndList(String pql)
	{
		ArrayList<String> andList = new ArrayList<String>(5);
		String str = pql;
		int index = str.indexOf(" AND ");
		return andList;
	}

	private void compile(String pql, KeyType keyType)
	{
		this.pql = pql;

		// Parse. Collect all variables.
		List<String> variableList = getVariableList(pql);

		// Create compiled query string
		String compiledPql = pql;
		attributes = new KeyType[variableList.size()];
		for (int i = 0; i < variableList.size(); i++) {
			String token = variableList.get(i);
			String attr = getAttribute(token);
			token = "\\$\\{" + attr + "\\}";
			KeyType kt = keyType.getKeyType(attr);
			attributes[i] = kt;
			if (kt != null) {
				Class<?> clazz = kt.getType();
				if (clazz == String.class) {
					compiledPql = compiledPql.replaceAll(token, "%s");
				} else if (clazz == Long.class || clazz == long.class) {
					compiledPql = compiledPql.replaceAll(token, "%dL");
				} else {
					compiledPql = compiledPql.replaceAll(token, "%s");
				}
			}
		}
		compiledQuery = compiledPql;
		isPathOnly = compiledPql.matches(".*[\\.:=].*") == false;
		if (isPathOnly) {
			paths = new String[] { compiledQuery };
		} else {
			if (compiledQuery.matches(".*[=<>].*")) {
				queryLanguage = QueryLanguage.OQL;
			} else if (compiledQuery.matches(".*:.*")) {
				queryLanguage = QueryLanguage.LUCENE;
			} else {
				queryLanguage = QueryLanguage.OQL;
				compiledQuery += "=%s";
			}

			switch (queryLanguage) {
			case OQL:
				compiledQuery = compiledQuery.replaceAll("%s", "'%s'");
				compiledQuery = buildTemporalOql(compiledQuery);
				break;

			case LUCENE:
				compiledQuery = buildTemporalLucene(compiledPql);
				break;
			default:
				break;
			}
		}

		if (paths != null) {
			fullPaths = new String[paths.length];
			for (int i = 0; i < paths.length; i++) {
				fullPaths[i] = GridUtil.getFullPath(paths[i]);
			}
		}
	}

	class ResultIndex
	{
		ResultIndex(int index, String attributeName)
		{
			this.index = index;
			this.attributeName = attributeName;
		}

		// if -1, then Input
		int index;
		String attributeName;
	}

	private ResultIndex[] resultIndexes;

	private boolean isInputOnly;

	/**
	 * Returns true if only ${Input} or static value is referenced.
	 */
	public boolean isInputOnly()
	{
		return isInputOnly;
	}

	@SuppressWarnings("rawtypes")
	private boolean fillQueryArgs(Object input, List[] results, Object[] args)
	{
		if (input == null) {
			return false;
		}
		if (KeyMap.class.isAssignableFrom(input.getClass())) {
			return fillQueryArgs((KeyMap)input, results, args);
		}
		
		for (int i = 0; i < attributes.length; i++) {
			AttributeIndex ai = (AttributeIndex) attributes[i];
			if (ai.toResultIndex == -1) {
				if (input != null) {
					args[i] = input.toString();
				}
			} else {
				List list = results[ai.toResultIndex];
				if (list != null && list.size() > 0) {
					// Collect unique values only
					HashSet<String> set = new HashSet<String>(list.size(), 1f);
					for (Object result : list) {
						if (result instanceof KeyMap) {
							Object val = ((KeyMap) result).get(ai.toAttributeName);
							if (val != null) {
								if (val instanceof Collection) {
									Collection col = (Collection)val;
									for (Object item : col) {
										if (item != null) {
											String strVal = item.toString().trim();
											if (strVal.length() > 0) {
												set.add(strVal);
											}
										}
									}
								} else {
									String strVal = val.toString().trim();
									if (strVal.length() > 0) {
										set.add(strVal);
									}
								}
							}
						}
					}
					
					// Build attribute value list separated by space
					if (set.size() > 0) {
						StringBuffer buffer = new StringBuffer();
						buffer.append("(");
						for (String val : set) {
							buffer.append(val.toString());
							buffer.append(" ");
						}
						buffer.append(")");
						String attributeValues = buffer.toString();
						if (attributeValues.length() > 0) {
							args[i] = attributeValues;
						} else {
							return false;
						}
					} else {
						return false;
					}
				}
			}
		}
		for (Object arg : args) {
			if (arg == null) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns compiled PQL string. It returns null if the results do not
	 * contain the values needed to construct the query.
	 * @param input Supports only primitive types.
	 * @param results
	 */
	public String getQuery(Object input, List[] results)
	{
		if (input == null) {
			return null;
		}
		if (KeyMap.class.isAssignableFrom(input.getClass())) {
			return getQuery((KeyMap)input, results);
		}
		
		Object args[] = new Object[attributes.length];
		if (fillQueryArgs(input, results, args) == false) {
			return null;
		}
		return String.format(compiledQuery, args);
	}
	
	@SuppressWarnings("rawtypes")
	public boolean fillQueryArgs(KeyMap<?> inputKeyMap, List[] results, Object[] args)
	{
		for (int i = 0; i < attributes.length; i++) {
			AttributeIndex ai = (AttributeIndex) attributes[i];
			if (ai.toResultIndex == -1) {
				Object attributeValue = inputKeyMap.get(ai.toAttributeName);
				if (attributeValue == null) {
					return false;
				}
				args[i] = attributeValue.toString();
			} else {
				List list = results[ai.toResultIndex];
				if (list != null) {
					StringBuffer buffer = new StringBuffer();
					for (Object result : list) {
						if (result instanceof KeyMap) {
							Object val = ((KeyMap) result).get(ai.toAttributeName);
							if (val != null) {
								if (val instanceof Collection) {
									Collection col = (Collection)val;
									for (Object item : col) {
										if (item != null) {
											String strVal = item.toString().trim();
											if (strVal.length() > 0) {
												buffer.append(strVal);
												buffer.append(" ");
											}
										}
									}
								} else {
									String strVal = val.toString().trim();
									if (strVal.length() > 0) {
										buffer.append(val);
									}
								}
								buffer.append(" ");
							}
						}
					}
					String attributeValues = buffer.toString().trim();
					if (attributeValues.length() > 0) {
						args[i] = attributeValues;
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Returns the executable (final) query.
	 * @param inputKeyMap
	 * @param results
	 * @return null if query cannot be built due to unavailable data. This means
	 *         the query returns no results.
	 */
	public String getQuery(KeyMap<?> inputKeyMap, List[] results)
	{
		Object args[] = new Object[attributes.length];
		if (fillQueryArgs(inputKeyMap, results, args) == false) {
			return null;
		}
//		for (int i = 0; i < attributes.length; i++) {
//			AttributeIndex ai = (AttributeIndex) attributes[i];
//			if (ai.resultIndex == -1) {
//				Object attributeValue = inputKeyMap.get(ai.attributeName);
//				if (attributeValue == null) {
//					return null;
//				}
//				args[i] = attributeValue.toString();
//			} else {
//				List list = results[ai.resultIndex];
//				if (list != null) {
//					StringBuffer buffer = new StringBuffer();
//					for (Object result : list) {
//						if (result instanceof KeyMap) {
//							Object val = ((KeyMap) result).get(ai.attributeName);
//							if (val != null) {
//								buffer.append(val.toString());
//								buffer.append(" ");
//							}
//						}
//					}
//					String attributeValues = buffer.toString().trim();
//					if (attributeValues.length() > 0) {
//						args[i] = attributeValues;
//					} else {
//						return null;
//					}
//				}
//			}
//		}
		return String.format(compiledQuery, args);
	}
	
	private List<AttributeIndex> getAttributeIndexList(String pql)
	{
		// Parse. Collect all variables.
		List<AttributeIndex> aiList = new ArrayList<AttributeIndex>();
		int index = pql.indexOf("?");
		String str;
		if (index != -1 && pql.length() > index + 1) {
			str = pql.substring(index + 1);
		} else {
			str = pql;
		}
		index = str.indexOf("${");
		while (index != -1) {
			String fromAttributeName = null;
			int fieldIndex = str.indexOf(":");
			if (fieldIndex != -1 && fieldIndex < index) {
				fromAttributeName = str.substring(0, fieldIndex).trim();
				fieldIndex = fromAttributeName.lastIndexOf(" ");
				if (fieldIndex != -1) {
					fromAttributeName = fromAttributeName.substring(fieldIndex).trim();
				}
			}
			int closeIndex = str.indexOf("}");
			if (closeIndex != -1) {
				String token = str.substring(index, closeIndex + 1);
				String variable = getAttribute(token);
				int resultIndex;
				if (variable.equals("Input")) {
					resultIndex = -1;
				} else {
					// TODO: throw exception if parseInt() fails
					resultIndex = Integer.parseInt(variable);
				}
				str = str.substring(closeIndex + 1);
				if (str.startsWith(".")) {
					String variableAttributeName;
					index = str.indexOf(" ");
					if (index == -1) {
						variableAttributeName = str.substring(1);
					} else {
						variableAttributeName = str.substring(1, index).trim();
					}
					AttributeIndex ai = new AttributeIndex(fromAttributeName, variableAttributeName, resultIndex);
					aiList.add(ai);
				} else {
					// Primitive variable
					AttributeIndex ai = new AttributeIndex(fromAttributeName, null, resultIndex);
					aiList.add(ai);
				}
			}
			index = str.indexOf("${");
		}
		return aiList;
	}

	private void compileInput(String pql)
	{
		if (pql == null) {
			return;
		}
		this.pql = pql;

		// Parse, collect attributes from variables, i.e., ${attr}.
		List<AttributeIndex> list = getAttributeIndexList(pql);

		// Create compiled query string. Replace all variables with
		// String substitution.
		// item_location?ShortItemNo:${ShortItemNo}
		// compiledPql = item_location?ShortItemNo:%s
		// ${Input}.CustomerNumber
		// inputMap.put("CustomerNumber", "CustomerNumber")
		// compiledPql = null
		// customer?CustomerNumber:${1}.PrmsCustomerNumber
		// refMaps[1].put(CustomerNumber", "PrmsCustomerNumber");
		// compiledPql = customer?CustomerNumber:%s
		String compiledPql = pql;
		isInputOnly = true;
		for (int i = 0; i < list.size(); i++) {
			AttributeIndex ai = list.get(i);
			String token;
			if (ai.isInput()) {
				token = "\\$\\{Input\\}";
			} else {
				token = "\\$\\{" + ai.toResultIndex + "\\}";
				isInputOnly = false;
			}
			if (ai.toAttributeName != null) {
				token += "\\." + ai.toAttributeName;
			}
			compiledPql = compiledPql.replaceAll(token, "%s");
		}
		attributes = list.toArray(new AttributeIndex[list.size()]);
		
		compiledQuery = compiledPql;
		isPathOnly = compiledPql.matches(".*[\\?\\.:=].*") == false;
		if (isPathOnly) {
			paths = new String[] { compiledQuery };
		} else {
			if (compiledQuery.matches(".*[=<>].*")) {
				queryLanguage = QueryLanguage.OQL;
			} 
			else if (PqlParser.findTopNClause(compiledQuery) != null) {
				queryLanguage = QueryLanguage.LUCENE;
				//This is topN query for lucene
				//Since GridQuery should have the fetchSize and topN flag
				//we can just replace the topN clause as empty string
				String topNClause =  PqlParser.findTopNClause(compiledQuery);
				compiledPql = compiledQuery.replace(topNClause, "");
				compiledPql = compiledPql.trim();
			}
			else {
				queryLanguage = QueryLanguage.LUCENE;
//			} else if (compiledQuery.matches(".*:.*") || compiledQuery.matches(".*\\?%.*")) {
//				queryLanguage = QueryLanguage.LUCENE;
//			} else {
//				queryLanguage = QueryLanguage.OQL;
//				compiledQuery += "=%s";
			}

			switch (queryLanguage) {
			case OQL:
				compiledQuery = compiledQuery.replaceAll("%s", "'%s'");
				compiledQuery = buildTemporalOql(compiledQuery);
				break;

			case LUCENE:
				compiledQuery = buildTemporalLucene(compiledPql);
				break;
			default:
				break;
			}
		}

		if (paths != null) {
			fullPaths = new String[paths.length];
			for (int i = 0; i < paths.length; i++) {
				fullPaths[i] = GridUtil.getFullPath(paths[i]);
			}
		}
	}

	private String buildTemporalOql(String pql)
	{
		if (pql == null) {
			return null;
		}

		// "/mock" is for testing only
		String rootPath;
		if (PadoServerManager.getPadoServerManager() == null) {
			rootPath = "/mygrid";
		} else {
			rootPath = PadoServerManager.getPadoServerManager().getGridInfo().getGridRootPath();
		}

		String compiledQuery = null;
		temporalIdentityQuery = null;
		try {
			StringReader reader = new StringReader(pql);
			int c = reader.read();
			StringBuffer processingBuffer = new StringBuffer(pql.length());
			ArrayList<String> pathList = new ArrayList<String>(3);
			String token;
			int index;
//			"portfolio.SecId='pos_e.test' AND portfolio.PortfolioName='pos_e' AND portfolio.Num=123.5";
			boolean openQuote = false;
			String value = "";
			boolean openValue = false;
			while (c != -1) {
				switch (c) {
				case '\'':
					openQuote = !openQuote;
					if (!openQuote) {
						processingBuffer.append(c);
//						value = processingBuffer.toString();
						processingBuffer.delete(0, processingBuffer.length());
					}
					break;
				case '(':
				case ')':
					// compiledBuffer.append(c);
					break;
				case '.':
					if (openQuote) {
						break;
					}
					if (openValue) {
						openValue = false;
						break;
					}
					token = processingBuffer.toString();
					index = token.lastIndexOf(' ');
					if (index >= 0) {
						token = token.substring(index).trim();
					}
					if (token.indexOf('.') == -1) {
						// path
						if (pathList.contains(token) == false) {
							pathList.add(token);
							processingBuffer.delete(0, processingBuffer.length());
						}
					} else {
						processingBuffer.append((char) c);
					}
					break; 
				case '=':
				case '>':
				case '<':
					if (!openQuote) {
						openValue=true;
					}
					processingBuffer.delete(0, processingBuffer.length());
					break;
				case ' ':
					if (!openQuote) {
						processingBuffer.delete(0, processingBuffer.length());
						openValue = false;
					}
					break;
				default:
					processingBuffer.append((char) c);
					break;
				}
				c = reader.read();
			}
			reader.close();

			paths = pathList.toArray(new String[pathList.size()]);
			int i = 0;
			String fromClause = "";
			String whereClause = pql;
			String identityFromClause = "";
			String identityWhereClause = pql;
			String timeWhereClause = "";
			for (String path : pathList) {
				String var = "p" + ++i;
				String identityVar = "e" + i;
				if (i > 1) {
					fromClause += ", ";
					identityFromClause += ", ";
					timeWhereClause += " AND ";
				}
				fromClause += rootPath + "/" + path + " " + var;
				whereClause = whereClause.replaceAll(path + "\\.", var + ".value.get('");
				whereClause = whereClause.replaceAll("=", "')=");
				identityFromClause += rootPath + "/" + path + ".entrySet " + identityVar;
				identityWhereClause = identityWhereClause.replaceAll(path + "\\.", identityVar + ".value.value.get('");
				timeWhereClause += TIME_QUERY_PREDICATE.replaceAll("e\\.", identityVar + ".");
			}
			identityWhereClause = identityWhereClause.replaceAll("=", "')=");
			compiledQuery = "select distinct * from " + fromClause + " where " + whereClause;
			temporalIdentityQuery = "select distinct e1.key.IdentityKey from " + identityFromClause + " where " + "("
					+ identityWhereClause + ") AND " + timeWhereClause;
			temporalKeyQuery = "select distinct e1.key from " + identityFromClause + " where " + "("
					+ identityWhereClause + ") AND " + timeWhereClause;

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return compiledQuery;
	}

	private String buildTemporalLucene(String pql)
	{
		if (pql == null) {
			return null;
		}

		String compiledQuery = pql;
		int index = compiledQuery.indexOf('?');
		if (index != -1) {
			paths = new String[] { compiledQuery.substring(0, index) };
			compiledQuery = compiledQuery.substring(index + 1);
		}
		temporalIdentityQuery = "(" + compiledQuery + ") AND " + LuceneSearch.TIME_QUERY_PREDICATE;
		return compiledQuery;
	}
	
	public class AttributeIndex
	{
		String fromAttributeName;
		String toAttributeName;
		int toResultIndex;
		
		AttributeIndex(String fromAttributeName, String toAttributeName, int toResultIndex)
		{
			this.fromAttributeName = fromAttributeName;
			this.toAttributeName = toAttributeName;
			this.toResultIndex = toResultIndex;
		}
		
		boolean isInput()
		{
			return toResultIndex < 0;
		}

		@Override
		public String toString()
		{
			return "AttributeIndex [fromAttributeName=" + fromAttributeName + ", toAttributeName=" + toAttributeName
					+ ", toResultIndex=" + toResultIndex + "]";
		}
	}
}
