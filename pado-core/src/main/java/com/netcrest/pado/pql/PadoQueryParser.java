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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.pql.TokenizedQuery.QueryLanguage;

@SuppressWarnings("rawtypes")
public class PadoQueryParser
{
	private final static Map<KeyType, CompiledUnit> compiledUnitMap = new HashMap<KeyType, CompiledUnit>();

	private final static Map<String, VirtualCompiledUnit2> vcuMap = new HashMap<String, VirtualCompiledUnit2>();

	public TokenizedQuery parseQuery_NotUsed(String thisAttributeName, String thisPath, Object thisKey,
			KeyMap thisKeyMap, String rootPath, String pql, boolean isTemporal)
	{
		if (pql == null) {
			return null;
		}
		TokenizedQuery tq;
		if (pql.matches("*.[=><].*")) {
			tq = toOql_NotUsed(thisAttributeName, thisPath, thisKey, thisKeyMap, rootPath, pql, isTemporal);
		} else {
			tq = toLucene_NotUsed(thisAttributeName, thisPath, thisKey, thisKeyMap, rootPath, pql, isTemporal);
		}
		return tq;
	}

	public TokenizedQuery toOql_NotUsed(String thisAttributeName, String thisPath, Object thisKey, KeyMap thisKeyMap,
			String rootPath, String pql, boolean isTemporal)
	{
		if (pql == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(pql, "=");
		String leftAssign = null;
		String rightAssign = null;
		String path1 = null;
		String path2 = null;
		String path1Attributes[] = null;
		String path2Attributes[] = null;
		if (st.hasMoreTokens()) {
			leftAssign = st.nextToken();
		} else {
			return null;
		}
		if (st.hasMoreTokens()) {
			rightAssign = st.nextToken();
		}

		// Left-hand side
		String split[] = leftAssign.split("\\.");
		if (split.length > 0) {
			// path1
			path1 = split[0];
		}
		if (split.length > 1) {
			// attributes
			path1Attributes = new String[split.length - 1];
			System.arraycopy(split, 1, path1Attributes, 0, path1Attributes.length);
		}

		// Right-hand side
		if (rightAssign != null) {
			split = rightAssign.split("\\.");
			if (split.length > 0) {
				// path1
				path2 = split[0];
			}
			if (split.length > 1) {
				// attributes
				path2Attributes = new String[split.length - 1];
				System.arraycopy(split, 1, path2Attributes, 0, path2Attributes.length);
			}
		}

		// Create OQL string
		String queryString = null;
		String leftQuery = "";
		String rightQuery = "";
		String temporalPrefix;
		String selectProjection = "select * from " + rootPath;
		if (isTemporal) {
			temporalPrefix = "value.";
		} else {
			temporalPrefix = "";
		}

		if (path1.equals("this")) {

			// this.mrgnNode=this.parentNode
			for (int i = 0; i < path1Attributes.length; i++) {
				if (i > 0) {
					leftQuery += ".";
				}
				leftQuery += temporalPrefix + "get('" + path1Attributes[i] + "')";
			}
		} else {
			// bank.BankId=this.BankId
			// select * from /mock/bank a where a.BankId='bank_a'
			// selectProject +=
			for (int i = 0; i < path1Attributes.length; i++) {
				if (i > 0) {
					leftQuery += ".";
				}
				leftQuery += temporalPrefix + "get('" + path1Attributes[i] + "')";
			}
		}
		if (path2.equals("this")) {
			KeyMap keyMap = thisKeyMap;
			Object rightObject = null;
			for (int i = 0; i < path2Attributes.length; i++) {
				rightObject = keyMap.get(path2Attributes[i]);
				if (rightObject instanceof KeyMap) {
					keyMap = (KeyMap) rightObject;
				} else if (i < path2Attributes.length - 1) {
					// break if KeyMap is not nested.
					rightObject = null;
					break;
				}
			}
			if (rightObject != null) {
				if (rightObject instanceof String) {
					rightQuery = "'" + (String) rightObject + "'";
				} else if (rightObject instanceof Long) {
					rightQuery = rightObject.toString() + "L";
				} else {
					rightQuery = rightObject.toString();
				}
			}
		} else {

		}
		if (leftQuery.length() > 0 && rightQuery.length() > 0) {
			queryString = leftQuery + "=" + rightQuery;
		}

		TokenizedQuery tq = new TokenizedQuery();
		tq.setQueryLanguage(QueryLanguage.OQL);
		tq.setQueryString(queryString);
		return tq;
	}

	public TokenizedQuery toLucene_NotUsed(String thisAttributeName, String thisPath, Object thisKey, KeyMap thisKeyMap,
			String rootPath, String pql, boolean isTemporal)
	{
		if (pql == null) {
			return null;
		}
		// "account(AccoundId AND (BankId OR Name:BankName))"

		StringTokenizer st = new StringTokenizer(pql, "=");
		String leftAssign = null;
		String rightAssign = null;
		String path1 = null;
		String path2 = null;
		String path1Attributes[] = null;
		String path2Attributes[] = null;
		if (st.hasMoreTokens()) {
			leftAssign = st.nextToken();
		} else {
			return null;
		}
		if (st.hasMoreTokens()) {
			rightAssign = st.nextToken();
		}

		// Left-hand side
		String split[] = leftAssign.split("\\.");
		if (split.length > 0) {
			// path1
			path1 = split[0];
		}
		if (split.length > 1) {
			// attributes
			path1Attributes = new String[split.length - 1];
			System.arraycopy(split, 1, path1Attributes, 0, path1Attributes.length);
		}

		// Right-hand side
		if (rightAssign != null) {
			split = rightAssign.split("\\.");
			if (split.length > 0) {
				// path1
				path2 = split[0];
			}
			if (split.length > 1) {
				// attributes
				path2Attributes = new String[split.length - 1];
				System.arraycopy(split, 1, path2Attributes, 0, path2Attributes.length);
			}
		}

		// Create OQL string
		String queryString = null;
		String leftQuery = "";
		String rightQuery = "";
		String temporalPrefix;
		String selectProjection = "select * from " + rootPath;
		if (isTemporal) {
			temporalPrefix = "value.";
		} else {
			temporalPrefix = "";
		}

		if (path1.equals("this")) {

			// this.mrgnNode=this.parentNode
			for (int i = 0; i < path1Attributes.length; i++) {
				if (i > 0) {
					leftQuery += ".";
				}
				leftQuery += temporalPrefix + "get('" + path1Attributes[i] + "')";
			}
		} else {
			// bank.BankId=this.BankId
			// select * from /mock/bank a where a.BankId='bank_a'
			// selectProject +=
			for (int i = 0; i < path1Attributes.length; i++) {
				if (i > 0) {
					leftQuery += ".";
				}
				leftQuery += temporalPrefix + "get('" + path1Attributes[i] + "')";
			}
		}
		if (path2.equals("this")) {
			KeyMap keyMap = thisKeyMap;
			Object rightObject = null;
			for (int i = 0; i < path2Attributes.length; i++) {
				rightObject = keyMap.get(path2Attributes[i]);
				if (rightObject instanceof KeyMap) {
					keyMap = (KeyMap) rightObject;
				} else if (i < path2Attributes.length - 1) {
					// break if KeyMap is not nested.
					rightObject = null;
					break;
				}
			}
			if (rightObject != null) {
				if (rightObject instanceof String) {
					rightQuery = "'" + (String) rightObject + "'";
				} else if (rightObject instanceof Long) {
					rightQuery = rightObject.toString() + "L";
				} else {
					rightQuery = rightObject.toString();
				}
			}
		} else {

		}
		if (leftQuery.length() > 0 && rightQuery.length() > 0) {
			queryString = leftQuery + "=" + rightQuery;
		}

		TokenizedQuery tq = new TokenizedQuery();
		tq.setQueryLanguage(QueryLanguage.LUCENE);
		tq.setQueryString(queryString);
		return tq;
	}

	public static String getAttribute_NotUsed(String token)
	{
		return token.substring(2, token.length() - 1);
	}

	/**
	 * Compiles the specified PQL query string by expanding all attribute
	 * variables in the query by mapping the attribute types defined by the
	 * specified key type.
	 * 
	 * @param pql
	 *            PQL query string
	 * @param keyType
	 *            Key type
	 * @return Compiled PQL that can be readily executed.
	 */
	public static String compileQuery_NotUsed(String pql, KeyType keyType)
	{
		// Parse
		ArrayList<String> list = new ArrayList<String>();
		String str = pql;
		int index = str.indexOf("${");
		while (index != -1) {
			int closeIndex = str.indexOf("}");
			if (closeIndex != -1) {
				String token = str.substring(index, closeIndex + 1);
				list.add(token);
				str = str.substring(closeIndex + 1);
			}
			index = str.indexOf("${");
		}

		// Create compiled query string
		String compiledPql = pql;
		for (String token : list) {
			String attr = getAttribute_NotUsed(token);
			token = "\\$\\{" + attr + "\\}";
			KeyType kt = keyType.getKeyType(attr);
			if (kt != null) {
				Class clazz = kt.getType();
				if (clazz == String.class) {
					compiledPql = compiledPql.replaceAll(token, "%s");
				} else if (clazz == Long.class || clazz == long.class) {
					compiledPql = compiledPql.replaceAll(token, "%dL");
				} else {
					compiledPql = compiledPql.replaceAll(token, "%s");
				}
			}
		}

		return compiledPql;
	}

	/**
	 * Returns the compiled unit that represents the specified key type. It
	 * creates a new one if it doesn't exist. It returns null if the key type
	 * does not define a reference.
	 * 
	 * @param keyType
	 *            Key type Key type with reference defined. This is for the
	 *            specified key type, and not for the entire KeyType constants.
	 */
	public static CompiledUnit getCompiledUnit(KeyType keyType)
	{
		if (keyType == null) {
			return null;
		}

		CompiledUnit cu = compiledUnitMap.get(keyType);
		if (cu == null && keyType.isReference()) {
			String pql = keyType.getQuery();
			if (pql != null) {
				cu = new CompiledUnit(pql, keyType);
				compiledUnitMap.put(keyType, cu);
			}
		}
		return cu;
	}

	/**
	 * Removes the specified key type's CU f
	 * 
	 * @param keyType
	 *            Key type
	 */
	public synchronized static void removeCompiledUnit(KeyType keyType)
	{
		if (keyType == null) {
			return;
		}
		compiledUnitMap.remove(keyType);
	}

	/**
	 * Returns the virtual compiled unit that represents the specified virtual
	 * path definition. It creates a new one if it doesn't exist. It returns
	 * null if the specified vpd is null or VirtualPath is not defined in vpd.
	 * 
	 * @param vpd
	 *            Virtual path definition
	 */
	public static VirtualCompiledUnit2 getVirtualCompiledUnit(KeyMap vpd)
	{
		if (vpd == null) {
			return null;
		}

		String virtualPath = (String) vpd.get("VirtualPath");
		if (virtualPath == null) {
			return null;
		}
		VirtualCompiledUnit2 vcu = vcuMap.get(vpd.get("VirtualPath"));
		if (vcu == null) {
			vcu = new VirtualCompiledUnit2(vpd);
			vcuMap.put(virtualPath, vcu);

		}
		return vcu;
	}

	/**
	 * Removes the specified virtual path
	 * 
	 * @param virtualPath
	 *            Virtual path
	 */
	public synchronized static void removeVirtualCompiledUnit(String virtualPath)
	{
		if (virtualPath == null) {
			return;
		}
		compiledUnitMap.remove(virtualPath);
	}

	/**
	 * Removes all cached CUs.
	 */
	public synchronized static void reset()
	{
		compiledUnitMap.clear();
		vcuMap.clear();
	}
}
