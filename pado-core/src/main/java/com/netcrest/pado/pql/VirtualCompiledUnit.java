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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.pql.CompiledUnit.AttributeIndex;

/**
 * CompiledUnit pre-compiles the specified PQL (Pado Query Language) to be
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
@SuppressWarnings({ "rawtypes", "unchecked" })
abstract class VirtualCompiledUnit
{
	/**
	 * <ul>
	 * <li>Queries are executed in parallel if dependencies have be resolved.
	 * Those with previous query result dependencies are executed after the
	 * dependent results have been obtained.</li>
	 * <li>The input is referred by ${Input}. The previous results are
	 * referenced by the query array index, i.e., ${0} refers to the first query
	 * results, ${1} refers to the second query results, etc.
	 * </ul>
	 * 
	 * [ "ucn_master?IndUcn:${Input}.Ucn",
	 * "prms_master?BillToUcn:${0}.ShipToUcn",
	 * "consignment_inventory?CustomerNumber:${1}.PrmsCustomerNumber" ]
	 * queryCUs[0] -> ucn_master?%s -- get Ucn from input object queryCUs[1] ->
	 * prms_master?BillToUcn:%s --- get ShipToUcn from queryCus[0] results
	 * queryCUs[2] -> consignment_inventory?CustomerNumber:%s -- get
	 * PrmsCustomerNumber from queryCus[1] results
	 */
	protected CompiledUnit allCUs[];

	private KeyType returnKeyType;
	protected KeyType inputKeyType;
	private Class inputClass;
	private String input;
	private int anchorIndex;
	private boolean isCopyOnRead;
	private boolean isLazyCopy;
	private String inputDescription;
	private String queryDescription;

	protected CUOrder inputCUOrders[];

	/**
	 * Contains CUOrders that can be executed in parallel. The index is the
	 * result index of the previous result lists.
	 */
	protected CUOrder parallelCUOrders[][];

	// true to return nested output
	// false to return flat output
	protected boolean isFlatOutput = true;

	private Map<String, CompiledUnit> referenceCUMap = new HashMap<String, CompiledUnit>(10);

	class CUOrder
	{
		CUOrder(int index, CompiledUnit cu)
		{
			this.index = index;
			this.cu = cu;
		}

		/**
		 * Index of allCUs[]
		 */
		int index;
		CompiledUnit cu;
	}

	class OutputCU
	{
		OutputCU(String outputAttributeName, CompiledUnit cu)
		{
			this.outputAttributeName = outputAttributeName;
			this.cu = cu;
		}

		String outputAttributeName;
		CompiledUnit cu;
	}

	/**
	 * Constructs a CompiledUnit object that compiles the specified PQL for the
	 * specified key type.
	 * 
	 * @param pql
	 *            Pado Query Language
	 * @param keyType
	 *            Key type
	 * @throws ClassNotFoundException
	 */
	public VirtualCompiledUnit(KeyMap vpd)
	{
		compile(vpd);
	}

	private Class getClass(String className) throws ClassNotFoundException
	{
		if (className.equalsIgnoreCase("String")) {
			return String.class;
		} else if (className.equalsIgnoreCase("Boolean")) {
			return boolean.class;
		} else if (className.equalsIgnoreCase("Byte")) {
			return byte.class;
		} else if (className.equalsIgnoreCase("Short")) {
			return short.class;
		} else if (className.equalsIgnoreCase("Integer")) {
			return int.class;
		} else if (className.equalsIgnoreCase("Long")) {
			return long.class;
		} else if (className.equalsIgnoreCase("Float")) {
			return float.class;
		} else if (className.equalsIgnoreCase("Float")) {
			return double.class;
		} else if (className.equalsIgnoreCase("Double")) {
			return double.class;
		} else {
			return Class.forName(className);
		}
	}

	/**
	 * Compiles the specified virtual path definition. The following keys must
	 * be defined.
	 * 
	 * @param vpd
	 *            Virtual path definition
	 * @throws ClassNotFoundException
	 */
	private void compile(KeyMap vpd)
	{
		if (vpd == null) {
			return;
		}

		String virtualPath = (String) vpd.get("VirtualPath");
		if (virtualPath == null) {
			return;
		}

		// Virtual KeyType - Virtual KeyType defines this virtual path's
		// schema. If undefined, then non-schema KeyMap (JsonLite) is assumed.
		String keyTypeClassName = (String) vpd.get("KeyType");
		if (keyTypeClassName != null && keyTypeClassName.length() > 0) {
			try {
				Class clazz = getClass(keyTypeClassName);
				Object enums[] = clazz.getEnumConstants();
				if (enums != null && enums.length > 0) {
					this.returnKeyType = (KeyType) enums[0];
				}
			} catch (ClassNotFoundException e) {
				throw new PadoException(e);
			}
		}

		// Input - Input can be KeyType or Object, i.e., String, Long, POJO,
		// etc.
		// It must be defined.
		this.input = (String) vpd.get("Input");
		if (input == null) {
			return;
		}

		inputClass = null;
		try {
			inputClass = getClass(input);
			if (KeyType.class.isAssignableFrom(inputClass)) {
				Object enums[] = inputClass.getEnumConstants();
				if (enums != null && enums.length > 0) {
					inputKeyType = (KeyType) enums[0];
				}
			}
		} catch (ClassNotFoundException e) {
			throw new PadoException(e);
		}
		this.inputDescription = (String) vpd.get("InputDescription");

		// Query - Query at the top level defines a set of queries
		// for obtaining the results needed to satisfy attribute-level
		// queries. These queries are executed in parallel where possible
		// appropriately isolating query dependencies. All queries are
		// represented as CompiledUnit objects as follows:
		// allCUs[] - collection of all CUs in the order the queries
		// are defined.
		// inputCUOrders - collection of all ${Input} only queries. They are
		// executed in parallel and the results are stored in
		// the same order as allCUs[].
		// parallelCUOrders - collection of the remaining queries. These
		// queries include non-Input attributes. As with
		// inputCUOrders they are executed in parallel and the
		// results are stored in the same order as allCUs[].
		// It is important to note that result references,
		// i.e., ${0}, ${1}, etc., dictate the sequence in which
		// the queries must be executed. This is what we call
		// "result dependency resolution" or RDR. RDR must be
		// satisfied in all VCPs with no exceptions.
		Object[] queries = (Object[]) vpd.get("Query");
		this.queryDescription = (String) vpd.get("QueryDescription");
		if (queries != null) {
			allCUs = new CompiledUnit[queries.length];
			for (int i = 0; i < queries.length; i++) {
				String pql = (String) queries[i];
				allCUs[i] = new CompiledUnit(pql);
			}
		}

		// Isolate Input queries into inputCUOrders[] and the rest to
		// parallelCUOrders[]
		List<CUOrder> inputCUOrderList = new ArrayList<CUOrder>(allCUs.length);
		List[] parallelCUOrderLists = new List[allCUs.length];
		for (int j = 0; j < allCUs.length; j++) {
			CompiledUnit cu = allCUs[j];
			if (cu.isInputOnly()) {
				// inputCUOrders
				CUOrder cuOrder = new CUOrder(j, cu);
				inputCUOrderList.add(cuOrder);
			} else {
				// parallelCUOrders
				CompiledUnit.AttributeIndex attributes[] = (CompiledUnit.AttributeIndex[]) cu.getAttributes();
				if (attributes != null) {
					// find the largest ai.index
					int index = -1;
					for (CompiledUnit.AttributeIndex ai : attributes) {
						if (index < ai.toResultIndex) {
							index = ai.toResultIndex;
						}
					}
					if (parallelCUOrderLists[index] == null) {
						parallelCUOrderLists[index] = new ArrayList<CUOrder>();
					}
					parallelCUOrderLists[index].add(new CUOrder(j, cu));
				}
			}
		}
		inputCUOrders = inputCUOrderList.toArray(new CUOrder[inputCUOrderList.size()]);
		// collect non-null parallel list
		List<CUOrder[]> list = new ArrayList<CUOrder[]>();
		for (List<CUOrder> plist : parallelCUOrderLists) {
			if (plist != null) {
				list.add(plist.toArray(new CUOrder[plist.size()]));
			}
		}
		this.parallelCUOrders = list.toArray(new CUOrder[list.size()][]);

		// Anchor
		Integer anchor = (Integer) vpd.get("Anchor");
		// TODO: pick the last one for now. must pick the right one.
		if (anchor == null) {
			anchorIndex = list.size() - 1;
		} else {
			anchorIndex = anchor;
		}

		// CopyOnRead/LazyCopy
		Boolean isCopyOnRead = (Boolean) vpd.get("CopyOnRead");
		Boolean isLazyCopy = (Boolean) vpd.get("LazyCopy");
		this.isCopyOnRead = isCopyOnRead != null && isCopyOnRead;
		this.isLazyCopy = isLazyCopy != null && isLazyCopy;

		// Reference
		KeyMap reference = (KeyMap) vpd.get("Reference");
		if (reference == null) {
			// Map all Input attributes as they are
			// TODO: write logic

		} else {

			// Reference may have the following attributes:
			// "Query" - defines the PQL
			// "Type" - defines the return type
			// "Depth" - defines the object graph depth
			Set<Map.Entry<String, KeyMap>> entrySet = reference.entrySet();
			for (Map.Entry<String, KeyMap> entry : entrySet) {
				String referenceAttributeName = entry.getKey();
				KeyMap keyMap = entry.getValue();
				String refQuery = (String) keyMap.get("Query");
				String refType = (String) keyMap.get("Type");
				Integer refDepth = (Integer) keyMap.get("Depth");
				refDepth = refDepth == null ? 0 : refDepth;
				CompiledUnit cu = new CompiledUnit(refQuery);
				referenceCUMap.put(referenceAttributeName, cu);
			}
		}
	}

	public CompiledUnit[] getQueryCompiledUnits()
	{
		return allCUs;
	}

	private KeyMap combineKeyMap(KeyMap keyMap1, KeyMap keyMap2)
	{
		HashMap map = new HashMap(keyMap1);
		map.putAll(keyMap2);
		return new JsonLite(map);
	}

	List<KeyMap> joinResultLists(List<KeyMap> sourceResultList, String[] sourceKeys,
			Map<List<Object>, List<KeyMap>> joiningMap)
	{
		List joinedList = new ArrayList();
		String key = "";
		for (String sourceKey : sourceKeys) {
			if (key.length() > 0) {
				key += ".";
			}
			key += sourceKey;
		}
		key = "[" + key + "]";
		for (KeyMap sourceKeyMap : sourceResultList) {
			List<Object> keyList = createKeyList(sourceKeyMap, sourceKeys);
			List<KeyMap> joiningResultList = joiningMap.get(keyList);
			if (joiningResultList != null) {
				// Create a joined key map that includes content of sourceKeyMap
				// and an entry with key and joiningResultList
				KeyMap joinedKeyMap = createResultKeyMap(sourceKeyMap, key, joiningResultList);
				joinedList.add(joinedKeyMap);
			}
		}
		return joinedList;
	}

	List<KeyMap> joinResultLists_old(List<KeyMap> sourceResultList, String[] sourceKeys,
			Map<List<Object>, List<KeyMap>> joiningMap)
	{
		List joinedList = new ArrayList();
		for (KeyMap sourceKeyMap : sourceResultList) {
			List<Object> keyList = createKeyList(sourceKeyMap, sourceKeys);
			List<KeyMap> joiningResultList = joiningMap.get(keyList);
			if (joiningResultList != null) {
				// Create joined records by combining the record and joining map
				// records.
				for (KeyMap joiningKeyMap : joiningResultList) {
					KeyMap joinedRecord = combineKeyMap(sourceKeyMap, joiningKeyMap);
					joinedList.add(joinedRecord);
				}
			}
		}
		return joinedList;
	}

	KeyMap createResultKeyMap(KeyMap keyMap, String key, List<KeyMap> joinedList)
	{
		HashMap map = new HashMap(keyMap);
		map.put(key, joinedList);
		return new JsonLite(map);
	}

	List<Object> createKeyList(KeyMap keyMap, String[] keys)
	{
		List<Object> keyList = new ArrayList(keys.length);
		for (String key : keys) {
			Object value = keyMap.get(key);
			if (value != null) {
				keyList.add(value);
			}
		}
		return keyList;
	}

	Map<List<Object>, List<KeyMap>> createJoiningMap(List<KeyMap> resultList, String[] keys)
	{
		HashMap<List<Object>, List<KeyMap>> map = new HashMap<List<Object>, List<KeyMap>>();
		for (KeyMap keyMap : resultList) {
			List<Object> keyList = createKeyList(keyMap, keys);
			if (keyList.size() > 0) {
				List<KeyMap> list = map.get(keyList);
				if (list == null) {
					list = new ArrayList<KeyMap>();
					map.put(keyList, list);
				}
				list.add(keyMap);
			}
		}
		return map;
	}

	protected List fillFlat(KeyMap inputKeyMap, List[] results)
	{
		// Reverse the order of joins. First, pair up the order of
		// result variables. Then join each pair in reverse order.
		int[] variables = new int[results.length];
		for (int i = allCUs.length - 1; i >= 0; i--) {
			CompiledUnit cu = allCUs[i];
			Object[] attributes = cu.getAttributes();
			// TODO: Supports only a single variable. Add support for multiple.
			int variable = -1;
			for (int j = 0; j < attributes.length; j++) {
				AttributeIndex ai = (AttributeIndex) attributes[j];
				variable = ai.toResultIndex;
				break;
			}
			variables[i] = variable;
		}

		// Get the last result set referenced.
		List<KeyMap> resultList = null;
		int lastReferencedVariableIndex = -1;
		for (int i = variables.length - 1; i >= 0; i--) {
			if (variables[i] != -1) {
				resultList = results[variables[i]];
				lastReferencedVariableIndex = i;
				break;
			}
		}
		if (resultList == null) {
			return null;
		}

		// Join results in reverse order
		List[] joinedResultLists = new List[results.length]; // debug
		List<KeyMap> joinedList = null;
		for (int i = lastReferencedVariableIndex; i >= 0; i--) {
			int variable = variables[i];
			if (variable == -1) {
				continue;
			}
			CompiledUnit cu = allCUs[i];
			Object[] attributes = cu.getAttributes();
			String fromKeys[] = new String[attributes.length];
			String toKeys[] = new String[attributes.length];
			for (int j = 0; j < attributes.length; j++) {
				AttributeIndex ai = (AttributeIndex) attributes[j];
				fromKeys[j] = ai.fromAttributeName;
				toKeys[j] = ai.toAttributeName;
			}
			// TODO: This implementation supports only AND conditions, i.e., all
			// from and to keys must match.
			if (results[i] != null) {
				List<KeyMap> joiningList = joinedResultLists[i];
				if (joiningList == null) {
					joiningList = results[i];
				}
				Map<List<Object>, List<KeyMap>> joiningMap = createJoiningMap(joiningList, fromKeys);

				List<KeyMap> sourceList = joinedResultLists[variable];
				if (sourceList == null) {
					sourceList = results[variable];
				}

				// toKeys are for resultList
				// TODO: This implementation expects toKeys are keys from the
				// same result list. Handle keys from multiple result lists.
				joinedList = joinResultLists(sourceList, toKeys, joiningMap);
				joinedResultLists[variable] = joinedList; // debug
			}
		}

		// // Join results
		// List<KeyMap> resultList = results[0];
		// List[] joinedResultLists = new List[results.length]; // debug
		// joinedResultLists[0] = resultList;
		// for (int i = 1; i < results.length; i++) {
		// CompiledUnit cu = allCUs[i];
		// Object[] attributes = cu.getAttributes();
		// String fromKeys[] = new String[attributes.length];
		// String toKeys[] = new String[attributes.length];
		// for (int j = 0; j < attributes.length; j++) {
		// AttributeIndex ai = (AttributeIndex) attributes[j];
		// fromKeys[j] = ai.fromAttributeName;
		// toKeys[j] = ai.toAttributeName;
		// }
		//
		// // TODO: This implementation supports only AND conditions, i.e., all
		// // from and to keys must match.
		// if (results[i] != null) {
		// Map<List<Object>, List<KeyMap>> joiningMap =
		// createJoiningMap((List<KeyMap>) results[i], fromKeys);
		// joinedResultLists[i] = resultList; // debug
		// // toKeys are for resultList
		// // TODO: This implementation expects toKeys are keys from the
		// // same result list. Handle keys from multiple result lists.
		// resultList = joinResultLists(resultList, toKeys, joiningMap);
		// }
		// }

		// Get references from the joinedList
		List<KeyMap> outputList = null;
		Set<Map.Entry<String, CompiledUnit>> refSet = referenceCUMap.entrySet();
		if (refSet.size() == 0) {
			outputList = joinedList;
		} else if (results.length > 0) {

			outputList = createReferenceRecords(joinedList);

			// outputList = new ArrayList();
			// for (KeyMap keyMap : joinedList) {
			// JsonLite output = new JsonLite();
			// for (Map.Entry<String, CompiledUnit> entry : refSet) {
			// String key = entry.getKey();
			// CompiledUnit refCu = entry.getValue();
			// CompiledUnit.AttributeIndex attributes[] =
			// (CompiledUnit.AttributeIndex[]) refCu.getAttributes();
			// if (attributes == null || attributes.length == 0) {
			// // static (literal) value
			// output.put(key, refCu.getPql());
			// } else {
			// CompiledUnit.AttributeIndex ai = attributes[0];
			// Object value = keyMap.get(ai.toAttributeName);
			// output.put(key, value);
			// }
			// }
			// outputList.add(output);
			// }
		}

		return outputList;
	}

	private List<KeyMap> createReferenceRecords(List<KeyMap> joinedList)
	{
		List<KeyMap> refList = new ArrayList<KeyMap>();
		buildReferenceInfos();
		KeyMap inputKeyMap = null;
		Set<Map.Entry<String, CompiledUnit>> refSet = referenceCUMap.entrySet();

		for (KeyMap joinedKeyMap : joinedList) {
			JsonLite jl = new JsonLite();
			for (int i = 0; i < inputReferenceInfo.inputKeyList.size(); i++) {
				Object value = inputReferenceInfo.getValue(i);
				if (value != null) {
					jl.put(inputReferenceInfo.getReferenceKey(i), value);
				} else {
					String inputKey = inputReferenceInfo.getInputKey(i);
					if (inputKey != null) {
						// jl.put(inputReferenceInfo.getKey(i),
						// inputKeyMap.get(inputKey));
					}
				}
			}
			refList.add(jl);
			addReferenceValues(joinedKeyMap, refList, 1, true);
			// addReferenceValues_nested(joinedKeyMap, jl, 1, true);

		}
		return refList;
	}

	// List<KeyMap> addReferenceValues(KeyMap joinedKeyMap, List<KeyMap>
	// refList)
	// {
	// for (KeyMap refKeyMap : refList) {
	// addReferenceValues(refKeyMap, )
	// }
	// }

	List<KeyMap> addReferenceValues(KeyMap joinedKeyMap, List<KeyMap> refList, int index, boolean isClone)
	{
		List<KeyMap> outputList = refList;

		if (index < referenceInfos.length) {
			ReferenceInfo refInfo = referenceInfos[index];

			for (String toKey : refInfo.toKeyList) {
				String joinedKey = "[" + toKey + "]";
				List<KeyMap> nestedKeyMapList = (List<KeyMap>) joinedKeyMap.get(joinedKey);
				if (refInfo.variableAIList != null) {
					if (isClone) {
						outputList = new ArrayList<KeyMap>();
					}
					for (KeyMap refKeyMap : refList) {
						for (KeyMap keyMap : nestedKeyMapList) {
							KeyMap refKeyMap2;
							if (isClone) {
								refKeyMap2 = (KeyMap) ((JsonLite) refKeyMap).clone();
							} else {
								refKeyMap2 = refKeyMap;
							}
							for (int j = 0; j < refInfo.variableAIList.size(); j++) {
								AttributeIndex ai = refInfo.variableAIList.get(j);
								String refKey = refInfo.referenceKeyList.get(j);
								String toKey2 = ai.toAttributeName;
								Object value = keyMap.get(toKey2);
								refKeyMap2.put(refKey, value);
							}
							if (isClone) {
								outputList.add(refKeyMap2);
							}
						}
					}
				}
				int nextIndex = index + 1;
				if (nextIndex < referenceInfos.length) {
					ReferenceInfo nextRefInfo = referenceInfos[nextIndex];
					if (nextRefInfo.toIndex == refInfo.toIndex) {
						addReferenceValues(joinedKeyMap, outputList, nextIndex, false);
					} else {
						if (nestedKeyMapList != null) {
							int x = 0;
							for (KeyMap keyMap : nestedKeyMapList) {
								x++;
								System.out.println(x);
								addReferenceValues(keyMap, outputList, nextIndex, true);
							}
						}
					}
				}
			}
		}

		return outputList;
	}

	// List<KeyMap> addReferenceValues_nested(KeyMap joinedKeyMap, KeyMap
	// refKeyMap, int index, boolean isReplace)
	// {
	// if (index < referenceInfos.length) {
	// ReferenceInfo refInfo = referenceInfos[index];
	// for (String toKey : refInfo.toKeyList) {
	// String joinedKey = "[" + toKey + "]";
	// List<KeyMap> nestedKeyMapList = (List<KeyMap>)
	// joinedKeyMap.get(joinedKey);
	// if (refInfo.variableAIList != null) {
	// List<KeyMap> nestedKeyMapResultList = new
	// ArrayList<KeyMap>(nestedKeyMapList.size());
	// for (KeyMap keyMap : nestedKeyMapList) {
	// JsonLite nestedKeyMap = new JsonLite();
	// for (int j = 0; j < refInfo.variableAIList.size(); j++) {
	// AttributeIndex ai = refInfo.variableAIList.get(j);
	// String refKey = refInfo.referenceKeyList.get(j);
	// String toKey2 = ai.toAttributeName;
	// Object value = keyMap.get(toKey2);
	// nestedKeyMap.put(refKey, value);
	// }
	// nestedKeyMapResultList.add(nestedKeyMap);
	// }
	// // Replace nested list
	// joinedKeyMap.put(joinedKey, nestedKeyMapResultList);
	// }
	// int nextIndex = index + 1;
	// if (nextIndex < referenceInfos.length) {
	// ReferenceInfo nextRefInfo = referenceInfos[nextIndex];
	// if (nextRefInfo.toIndex == refInfo.toIndex) {
	// addReferenceValues_nested(joinedKeyMap, refKeyMap, nextIndex, false);
	// } else {
	// if (nestedKeyMapList != null) {
	// int x = 0;
	// for (KeyMap keyMap : nestedKeyMapList) {
	// x++;
	// System.out.println(x);
	// addReferenceValues_nested(keyMap, , nextIndex, true);
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// return outputList;
	// }

	InputReferenceInfo inputReferenceInfo;
	ReferenceInfo referenceInfos[];

	private void buildReferenceInfos()
	{
		inputReferenceInfo = new InputReferenceInfo();
		referenceInfos = new ReferenceInfo[allCUs.length];

		// Determine the depths
		Set<Map.Entry<String, CompiledUnit>> refSet = referenceCUMap.entrySet();

		for (int i = 0; i < referenceInfos.length; i++) {
			referenceInfos[i] = new ReferenceInfo();
		}
		for (Map.Entry<String, CompiledUnit> entry : refSet) {
			String key = entry.getKey();
			CompiledUnit refCu = entry.getValue();
			CompiledUnit.AttributeIndex attributes[] = (CompiledUnit.AttributeIndex[]) refCu.getAttributes();
			if (refCu.isInputOnly()) {
				if (attributes != null && attributes.length > 0) {
					// Handle ${Input}
					CompiledUnit.AttributeIndex ai = attributes[0];
					inputReferenceInfo.addInputKey(key, ai.toAttributeName);
				} else {
					inputReferenceInfo.addValue(key, refCu.getPql());
				}
			} else if (attributes != null && attributes.length > 0) {
				CompiledUnit.AttributeIndex ai = attributes[0];
				if (ai.toResultIndex != -1) {
					List<CompiledUnit.AttributeIndex> variableAIList = referenceInfos[ai.toResultIndex].variableAIList;
					if (variableAIList == null) {
						variableAIList = new ArrayList<CompiledUnit.AttributeIndex>(4);
						referenceInfos[ai.toResultIndex].variableAIList = variableAIList;
					}
					variableAIList.add(ai);
					List<String> referenceKeyList = referenceInfos[ai.toResultIndex].referenceKeyList;
					if (referenceKeyList == null) {
						referenceKeyList = new ArrayList<String>(4);
						referenceInfos[ai.toResultIndex].referenceKeyList = referenceKeyList;
					}
					referenceKeyList.add(key);
				}
			}
		}

		// Create variable link list
		for (int i = 0; i < allCUs.length; i++) {
			CompiledUnit cu = allCUs[i];
			Object[] attributes = cu.getAttributes();
			List<String> fromKeyList = new ArrayList(attributes.length);
			List<String> toKeyList = new ArrayList(attributes.length);
			for (int j = 0; j < attributes.length; j++) {
				AttributeIndex ai = (AttributeIndex) attributes[j];
				fromKeyList.add(ai.fromAttributeName);
				toKeyList.add(ai.toAttributeName);
				referenceInfos[i].toIndex = ai.toResultIndex;
			}
			referenceInfos[i].fromKeyList = fromKeyList;
			referenceInfos[i].toKeyList = toKeyList;
		}
	}

	private void buildReferenceInfos_old()
	{
		inputReferenceInfo = new InputReferenceInfo();
		referenceInfos = new ReferenceInfo[allCUs.length];

		// Determine the depths
		Set<Map.Entry<String, CompiledUnit>> refSet = referenceCUMap.entrySet();

		for (int i = 0; i < referenceInfos.length; i++) {
			referenceInfos[i] = new ReferenceInfo();
		}
		for (Map.Entry<String, CompiledUnit> entry : refSet) {
			String key = entry.getKey();
			CompiledUnit refCu = entry.getValue();
			CompiledUnit.AttributeIndex attributes[] = (CompiledUnit.AttributeIndex[]) refCu.getAttributes();
			if (refCu.isInputOnly()) {
				if (attributes != null && attributes.length > 0) {
					// Handle ${Input}
					CompiledUnit.AttributeIndex ai = attributes[0];
					inputReferenceInfo.addInputKey(key, ai.toAttributeName);
				} else {
					inputReferenceInfo.addValue(key, refCu.getPql());
				}
			} else if (attributes != null && attributes.length > 0) {
				CompiledUnit.AttributeIndex ai = attributes[0];
				if (ai.toResultIndex != -1) {
					List<CompiledUnit.AttributeIndex> variableAIList = referenceInfos[ai.toResultIndex].variableAIList;
					if (variableAIList == null) {
						variableAIList = new ArrayList<CompiledUnit.AttributeIndex>(4);
						referenceInfos[ai.toResultIndex].variableAIList = variableAIList;
					}
					variableAIList.add(ai);
				}
			}
		}

		// Create variable link list
		for (int i = 0; i < allCUs.length; i++) {
			CompiledUnit cu = allCUs[i];
			Object[] attributes = cu.getAttributes();
			List<String> fromKeyList = new ArrayList(attributes.length);
			List<String> toKeyList = new ArrayList(attributes.length);
			for (int j = 0; j < attributes.length; j++) {
				AttributeIndex ai = (AttributeIndex) attributes[j];
				fromKeyList.add(ai.fromAttributeName);
				toKeyList.add(ai.toAttributeName);
				referenceInfos[i].toIndex = ai.toResultIndex;
			}
			referenceInfos[i].fromKeyList = fromKeyList;
			referenceInfos[i].toKeyList = toKeyList;
		}
	}

	class InputReferenceInfo
	{
		List<String> referenceKeyList = new ArrayList<String>(10);
		List<String> inputKeyList = new ArrayList<String>(10);
		List<Object> valueList = new ArrayList<Object>(10);

		void addInputKey(String key, String inputKey)
		{
			referenceKeyList.add(key);
			inputKeyList.add(inputKey);
			valueList.add(null);
		}

		void addValue(String key, Object value)
		{
			referenceKeyList.add(key);
			inputKeyList.add(null);
			valueList.add(value);
		}

		boolean isInputKey(int index)
		{
			return index < inputKeyList.size() && inputKeyList.get(index) != null;
		}

		boolean isValue(int index)
		{
			return index < valueList.size() && valueList.get(index) != null;
		}

		String getReferenceKey(int index)
		{
			if (index < 0 || index >= referenceKeyList.size()) {
				return null;
			}
			return referenceKeyList.get(index);
		}

		String getInputKey(int index)
		{
			if (index < 0 || index >= inputKeyList.size()) {
				return null;
			}
			return inputKeyList.get(index);
		}

		Object getValue(int index)
		{
			if (index < 0 || index >= valueList.size()) {
				return null;
			}
			return valueList.get(index);
		}
	}

	class ReferenceInfo
	{
		List<CompiledUnit.AttributeIndex> variableAIList;
		List<String> fromKeyList;
		List<String> toKeyList;
		int toIndex;

		// referenceKeyList matches with toKeyList
		List<String> referenceKeyList;
	}

	KeyMap getFirstParentJoinedKeyMap(KeyMap joinedKeyMap, ReferenceInfo[] referenceInfos)
	{
		List<KeyMap> list = new ArrayList<KeyMap>(referenceInfos.length);

		// First, find the first index that is not null
		int index = -1;
		for (int i = 0; i < referenceInfos.length; i++) {
			if (referenceInfos[i].variableAIList != null) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			return joinedKeyMap;
		}

		// Get the parent
		for (int i = index; i >= 0; i--) {
			// List<CompiledUnit.AttributeIndex> aiList =
			// referenceInfos[i].variableAIList;
			int parentIndex = referenceInfos[i].toIndex;

		}
		return null;
	}

	private List<KeyMap> createReferencedOutput(KeyMap joinedKeyMap, List<KeyMap> outputList,
			ReferenceInfo referenceInfo)
	{
		List<CompiledUnit.AttributeIndex> aiList = referenceInfo.variableAIList;
		if (aiList == null) {
			return outputList;
		}
		JsonLite output = new JsonLite();
		for (AttributeIndex ai : aiList) {
			Object value = joinedKeyMap.get("[" + ai.fromAttributeName + "]");
			output.put(ai.toAttributeName, value);
		}
		outputList.add(output);
		return outputList;
	}

	// private void assignOutputKeyMap(List<CompiledUnit.AttributeIndex> aiList,
	// List<KeyMap> outputList, KeyMap baseOutput)
	// {
	// for (AttributeIndex ai : aiList) {
	// String joinedKey = "[" + ai.fromAttributeName + "]";
	// List<KeyMap> list = (List<KeyMap>)keyMap.get(joinedKey);
	// if (list == null) {
	// Object value = keyMap.get(ai.fromAttributeName);
	// output.put(ai.toAttributeName, value);
	// } else{
	// for (KeyMap keyMap2 : list) {
	//
	// }
	// }
	// }
	// }

	protected List fillNested_new(KeyMap inputKeyMap, List[] results)
	{
		List<KeyMap> outputList = null;

		HashMap<Object, List<KeyMap>> resultMap[] = new HashMap[results.length];
		for (int i = 0; i < resultMap.length; i++) {
			if (results[i] != null) {
				resultMap[i] = new HashMap<Object, List<KeyMap>>(results[i].size(), 1f);
			}
		}
		// i is same as CUOrder.cu.attributes[].resultIndex
		// CUOrder.index is the index of the query list, i.e., "Query" from
		// the VPD. Construct parallelResultMap for reverse lookup.
		// <value, KeyMap>
		for (int i = 0; i < parallelCUOrders.length; i++) {
			CUOrder[] cuOrders = parallelCUOrders[i];
			for (CUOrder cuo : cuOrders) {
				CompiledUnit.AttributeIndex[] attributes = (CompiledUnit.AttributeIndex[]) cuo.cu.getAttributes();
				String attributeName = attributes[0].toAttributeName;
				// attribute index is always 0 for OR conditions.
				// TODO: support AND conditions

				int index = cuo.index;
				int resultIndex = attributes[0].toResultIndex;

				if (index <= anchorIndex) {
					// Result sets above the anchor must be backward-chained
					List resultList = results[resultIndex];
					if (resultList != null) {
						for (int j = 0; j < resultList.size(); j++) {
							KeyMap keyMap = (KeyMap) resultList.get(j);
							Object value = keyMap.get(attributeName);
							if (value != null) {
								HashMap<Object, List<KeyMap>> map = resultMap[resultIndex];
								if (map != null) {
									if (value instanceof Collection) {
										Collection col = (Collection) value;
										for (Object val2 : col) {
											List<KeyMap> list = map.get(val2);
											if (list == null) {
												list = new ArrayList<KeyMap>();
												map.put(val2, list);
											}
											list.add(keyMap);
										}
									} else {
										List<KeyMap> list = map.get(value);
										if (list == null) {
											list = new ArrayList<KeyMap>();
											map.put(value, list);
										}
										list.add(keyMap);
									}
								}
							}
						}
					}
				} else {
					// Results sets below the anchor must be forward-chained
					List resultList = results[index];
					if (resultList != null) {
						attributeName = attributes[0].fromAttributeName;
						for (int j = 0; j < resultList.size(); j++) {
							KeyMap keyMap = (KeyMap) resultList.get(j);
							Object value = keyMap.get(attributeName);
							if (value != null) {
								HashMap<Object, List<KeyMap>> map = resultMap[resultIndex];
								if (map != null) {
									if (value instanceof Collection) {
										Collection col = (Collection) value;
										for (Object val2 : col) {
											List<KeyMap> list = map.get(val2);
											if (list == null) {
												list = new ArrayList<KeyMap>();
												map.put(val2, list);
											}
											list.add(keyMap);
										}
									} else {
										List<KeyMap> list = map.get(value);
										if (list == null) {
											list = new ArrayList<KeyMap>();
											map.put(value, list);
										}
										list.add(keyMap);
									}
								}
							}
						}
					}
				}
			}
		}

		// Apply results to references
		Set<Map.Entry<String, CompiledUnit>> refSet = referenceCUMap.entrySet();
		if (refSet.size() == 0) {
			outputList = results[anchorIndex];
		} else if (results.length > 0) {
			List<KeyMap> anchorList = results[anchorIndex];
			outputList = new ArrayList(anchorList.size());
			for (KeyMap anchorKeyMap : anchorList) {
				JsonLite output = new JsonLite();
				for (Map.Entry<String, CompiledUnit> entry : refSet) {
					String key = entry.getKey();
					CompiledUnit refCu = entry.getValue();
					CompiledUnit.AttributeIndex attributes[] = (CompiledUnit.AttributeIndex[]) refCu.getAttributes();
					if (attributes == null || attributes.length == 0) {
						// static value
						output.put(key, refCu.getPql());
					} else {
						CompiledUnit.AttributeIndex ai = attributes[0];
						if (attributes.length == 1 && attributes[0].isInput()) {
							if (ai.toAttributeName == null) {
								output.put(key, inputKeyMap);
							} else {
								output.put(key, inputKeyMap.get(ai.toAttributeName));
							}
						} else if (attributes[0].toResultIndex == anchorIndex) {
							output.put(key, anchorKeyMap.get(attributes[0].toAttributeName));
						} else {
							Object value = getOutputValueListKeyMap(resultMap, refCu, anchorKeyMap);
							output.put(key, value);
						}
					}
				}
				outputList.add(output);
			}
		}

		return outputList;
	}

	protected List fillNested(KeyMap inputKeyMap, List[] results)
	{
		List<KeyMap> outputList = null;

		HashMap<Object, KeyMap> resultMap[] = new HashMap[results.length];
		for (int i = 0; i < resultMap.length; i++) {
			if (results[i] != null) {
				resultMap[i] = new HashMap<Object, KeyMap>(results[i].size(), 1f);
			}
		}
		// i is same as CUOrder.cu.attributes[].resultIndex
		// CUOrder.index is the index of the query list, i.e., "Query" from
		// the VPD. Construct parallelResultMap for reverse lookup.
		// <value, KeyMap>
		for (int i = 0; i < parallelCUOrders.length; i++) {
			CUOrder[] cuOrders = parallelCUOrders[i];
			for (CUOrder cuo : cuOrders) {
				CompiledUnit.AttributeIndex[] attributes = (CompiledUnit.AttributeIndex[]) cuo.cu.getAttributes();
				String attributeName = attributes[0].toAttributeName;
				// attribute index is always 0 for OR conditions.
				// TODO: support AND conditions

				int index = cuo.index;
				int resultIndex = attributes[0].toResultIndex;

				if (index <= anchorIndex) {
					// Result sets above the anchor must be backward-chained
					List resultList = results[resultIndex];
					if (resultList != null) {
						for (int j = 0; j < resultList.size(); j++) {
							KeyMap keyMap = (KeyMap) resultList.get(j);
							Object value = keyMap.get(attributeName);
							if (value != null) {
								HashMap<Object, KeyMap> map = resultMap[resultIndex];
								if (map != null) {
									if (value instanceof Collection) {
										Collection col = (Collection) value;
										for (Object val2 : col) {
											map.put(val2, keyMap);
										}
									} else {
										map.put(value, keyMap);
									}
								}
							}
						}
					}
				} else {
					// Results sets below the anchor must be forward-chained
					List resultList = results[index];
					if (resultList != null) {
						attributeName = attributes[0].fromAttributeName;
						for (int j = 0; j < resultList.size(); j++) {
							KeyMap keyMap = (KeyMap) resultList.get(j);
							Object value = keyMap.get(attributeName);
							if (value != null) {
								HashMap<Object, KeyMap> map = resultMap[resultIndex];
								if (map != null) {
									if (value instanceof Collection) {
										Collection col = (Collection) value;
										for (Object val2 : col) {
											map.put(val2, keyMap);
										}
									} else {
										map.put(value, keyMap);
									}
								}
							}
						}
					}
				}
			}
		}

		// Apply results to references
		Set<Map.Entry<String, CompiledUnit>> refSet = referenceCUMap.entrySet();
		if (refSet.size() == 0) {
			outputList = results[anchorIndex];
		} else if (results.length > 0) {
			List<KeyMap> anchorList = results[anchorIndex];
			outputList = new ArrayList(anchorList.size());
			for (KeyMap anchorKeyMap : anchorList) {
				JsonLite output = new JsonLite();
				for (Map.Entry<String, CompiledUnit> entry : refSet) {
					String key = entry.getKey();
					CompiledUnit refCu = entry.getValue();
					CompiledUnit.AttributeIndex attributes[] = (CompiledUnit.AttributeIndex[]) refCu.getAttributes();
					if (attributes == null || attributes.length == 0) {
						// static value
						output.put(key, refCu.getPql());
					} else {
						CompiledUnit.AttributeIndex ai = attributes[0];
						if (attributes.length == 1 && attributes[0].isInput()) {
							if (ai.toAttributeName == null) {
								output.put(key, inputKeyMap);
							} else {
								output.put(key, inputKeyMap.get(ai.toAttributeName));
							}
						} else if (attributes[0].toResultIndex == anchorIndex) {
							output.put(key, anchorKeyMap.get(attributes[0].toAttributeName));
						} else {
							Object value = getOutputValueKeyMap(resultMap, refCu, anchorKeyMap);
							output.put(key, value);
						}
					}
				}
				outputList.add(output);
			}
		}

		return outputList;
	}

	private List<Object> getOutputValueListKeyMap(Map<Object, List<KeyMap>> resultMap[], CompiledUnit refCu,
			KeyMap anchorKeyMap)
	{
		Object value = null;
		CompiledUnit anchorCu = allCUs[anchorIndex];
		CompiledUnit.AttributeIndex[] anchorAttributes = (CompiledUnit.AttributeIndex[]) anchorCu.getAttributes();
		CompiledUnit.AttributeIndex anchorAi = anchorAttributes[0];
		CompiledUnit.AttributeIndex[] refAttributes = (CompiledUnit.AttributeIndex[]) refCu.getAttributes();
		CompiledUnit.AttributeIndex refAi = refAttributes[0];

		List<Object> valueList = new ArrayList<Object>();
		if (refAi.toResultIndex < anchorIndex) {
			// Work way up from the anchor
			value = anchorKeyMap.get(anchorAi.fromAttributeName);
			List<KeyMap> keyMapList = resultMap[anchorAi.toResultIndex].get(value);

			CompiledUnit cu = anchorCu;
			CompiledUnit.AttributeIndex ai = anchorAi;

			while (ai.toResultIndex != refAi.toResultIndex) {
				keyMapList = resultMap[ai.toResultIndex].get(value);
				if (keyMapList == null) {
					return null;
				}
				cu = allCUs[ai.toResultIndex];
				ai = (CompiledUnit.AttributeIndex) cu.getAttributes()[0];
				for (KeyMap keyMap : keyMapList) {
					value = keyMap.get(ai.fromAttributeName);
					valueList.add(value);
				}
			}
			for (KeyMap keyMap : keyMapList) {
				value = keyMap.get(refAi.toAttributeName);
				valueList.add(value);
			}

		} else {
			// Work way down from the anchor
			KeyMap keyMap = anchorKeyMap;
			List<KeyMap> keyMapList = new ArrayList<KeyMap>();

			CompiledUnit cu = refCu;
			CompiledUnit.AttributeIndex ai = refAi;
			int index = anchorIndex;
			int endIndex = index;
			do {
				// Determine the last CU from bottom up that refers to the
				// anchor CU
				while (ai.toResultIndex != endIndex) {
					index = ai.toResultIndex;
					cu = allCUs[index];
					ai = (CompiledUnit.AttributeIndex) cu.getAttributes()[0];
				}
				value = keyMap.get(ai.toAttributeName);
				keyMapList = resultMap[ai.toResultIndex].get(value);
				if (keyMapList != null) {
					for (KeyMap keyMap2 : keyMapList) {
						value = keyMap2.get(ai.toAttributeName);
						valueList.add(value);
					}
				}
				endIndex = index;
				ai = refAi;
			} while (ai.toResultIndex != endIndex);

			if (keyMapList != null) {
				for (KeyMap keyMap2 : keyMapList) {
					value = keyMap2.get(refAi.toAttributeName);
					valueList.add(value);
				}
			}
		}
		return valueList;
	}

	private Object getOutputValueKeyMap(Map<Object, KeyMap> resultMap[], CompiledUnit refCu, KeyMap anchorKeyMap)
	{
		Object value = null;
		CompiledUnit anchorCu = allCUs[anchorIndex];
		CompiledUnit.AttributeIndex[] anchorAttributes = (CompiledUnit.AttributeIndex[]) anchorCu.getAttributes();
		CompiledUnit.AttributeIndex anchorAi = anchorAttributes[0];
		CompiledUnit.AttributeIndex[] refAttributes = (CompiledUnit.AttributeIndex[]) refCu.getAttributes();
		CompiledUnit.AttributeIndex refAi = refAttributes[0];

		if (refAi.toResultIndex < anchorIndex) {
			// Work way up from the anchor
			KeyMap keyMap = anchorKeyMap;
			value = anchorKeyMap.get(anchorAi.fromAttributeName);
			keyMap = resultMap[anchorAi.toResultIndex].get(value);

			CompiledUnit cu = anchorCu;
			CompiledUnit.AttributeIndex ai = anchorAi;

			while (ai.toResultIndex != refAi.toResultIndex) {
				keyMap = resultMap[ai.toResultIndex].get(value);
				if (keyMap == null) {
					return null;
				}
				cu = allCUs[ai.toResultIndex];
				ai = (CompiledUnit.AttributeIndex) cu.getAttributes()[0];
				value = keyMap.get(ai.fromAttributeName);
			}
			value = keyMap.get(refAi.toAttributeName);

		} else {
			// Work way down from the anchor
			KeyMap keyMap = anchorKeyMap;

			CompiledUnit cu = refCu;
			CompiledUnit.AttributeIndex ai = refAi;
			int index = anchorIndex;
			int endIndex = index;
			do {
				// Determine the last CU from bottom up that refers to the
				// anchor CU
				while (ai.toResultIndex != endIndex) {
					index = ai.toResultIndex;
					cu = allCUs[index];
					ai = (CompiledUnit.AttributeIndex) cu.getAttributes()[0];
				}
				value = keyMap.get(ai.toAttributeName);
				keyMap = resultMap[ai.toResultIndex].get(value);
				endIndex = index;
				ai = refAi;
			} while (ai.toResultIndex != endIndex);

			if (keyMap != null) {
				value = keyMap.get(refAi.toAttributeName);
			}
		}
		return value;
	}

	@Override
	public String toString()
	{
		return "VirtualCompiledUnit [allCUs=" + Arrays.toString(allCUs) + ", returnKeyType=" + returnKeyType
				+ ", inputKeyType=" + inputKeyType + ", inputClass=" + inputClass + ", input=" + input
				+ ", anchorIndex=" + anchorIndex + ", isCopyOnRead=" + isCopyOnRead + ", isLazyCopy=" + isLazyCopy
				+ ", inputDescription=" + inputDescription + ", queryDescription=" + queryDescription
				+ ", inputCUOrders=" + Arrays.toString(inputCUOrders) + ", parallelCUOrders="
				+ Arrays.toString(parallelCUOrders) + ", isFlatOutput=" + isFlatOutput + ", referenceCUMap="
				+ referenceCUMap + "]";
	}
}
