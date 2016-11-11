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

import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.internal.pql.antlr4.PqlEvalDriver;

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
@SuppressWarnings({ "rawtypes" })
public class VirtualCompiledUnit2
{
	private KeyMap vpd;
	private Argument args[];
	private String attributes[];
	private String entityPql;

	/**
	 * Constructs a CompiledUnit object that compiles the specified PQL for the
	 * specified key type. Currently, supports only entity PQLs. Stitch is handled by {@link PqlEvalDriver}
	 * 
	 * @param pql
	 *            Pado Query Language
	 * @param keyType
	 *            Key type
	 * @throws ClassNotFoundException
	 */
	public VirtualCompiledUnit2(KeyMap vpd)
	{
		this.vpd = vpd;
		compile(vpd);
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
		String entityGridPath = (String) vpd.get("EntityGridPath");
		if (entityGridPath == null) {
			return;
		}

		Object[] ers = (Object[]) vpd.get("EntityRelationships");
		args = new Argument[ers.length];
		int i = 0;
		for (Object er : ers) {
			KeyMap km = (KeyMap) er;
			String argName = (String) km.get("Arg");
			Boolean isOne = (Boolean) km.get("IsOne");
			if (isOne == null) {
				isOne = false;
			}
			Integer depth = (Integer) km.get("Depth");
			if (depth == null) {
				depth = 0;
			}
			String pql = (String) km.get("Query");
			args[i++] = new Argument(argName, isOne, depth, pql);
		}

		// entityPql for an entity lookup
		entityPql = entityGridPath + "?";
		i = 0;
		ArrayList<String> attrList = new ArrayList<String>(args.length);
		for (Argument arg : args) {
			Object attributes[] = arg.getCu().getAttributes();
			for (Object attr : attributes) {
				if (attrList.contains(attr) == false) {
					if (i > 0) {
						entityPql += " AND ";
					}
					attrList.add((String)attr);
					entityPql += attr + ":${" + attr + "}";
					i++;
				}
			}
		}
		attributes = attrList.toArray(new String[attrList.size()]);
	}

	public String getEntityGridPath()
	{
		return (String) vpd.get("EntityGridPath");
	}

	public String getVirtualPath()
	{
		return (String) vpd.get("VirtualPath");
	}

	public String getCompiledEntityPql()
	{
		return entityPql;
	}

	public String getEntityPql(String... args)
	{
		String compiledPql = getCompiledEntityPql();
		int i = 0;
		for (Object attr : attributes) {
			if (args.length <= i) {
				break;
			}
			String token = "\\$\\{" + attr + "\\}";
			compiledPql = compiledPql.replaceAll(token, args[i]);
			i++;
		}
		return compiledPql;
	}

	public Argument[] getArguments()
	{
		return args;
	}
	
	public boolean isEntity()
	{
		if (vpd == null) {
			return false;
		}
		Boolean isEntity = (Boolean)vpd.get("IsEntity");
		return isEntity != null && isEntity;
	}

	public class Argument
	{
		Argument(String argName, boolean isOne, int depth, String query)
		{
			this.argName = argName;
			this.isOne = isOne;
			this.depth = depth;
			this.query = query;
			cu = new CompiledUnit(query);
		}

		String argName;
		boolean isOne;
		int depth;
		String query;
		CompiledUnit cu;

		public String getArgName()
		{
			return argName;
		}

		public boolean isOne()
		{
			return isOne;
		}

		public int getDepth()
		{
			return depth;
		}

		public String getQuery()
		{
			return query;
		}

		public CompiledUnit getCu()
		{
			return cu;
		}

	}
}
