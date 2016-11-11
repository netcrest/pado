package com.netcrest.pado.pql.function;

import java.util.Collections;
import java.util.List;

import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.server.VirtualPathEngine;

public class Identity
{	
	public Object execute(CompiledUnit cu)
	{
		if ("Identity".equals(cu.getFunctionName())) {
			String[] paths = cu.getPaths();
			if (paths == null || paths.length == 0) {
				return null;
			}
			String path = paths[0];
//			if (VirtualPathEngine.getVirtualPathEngine().isVirtualPath(path)) {
//				for (KeyMap km : keyMapCollection) {
//					// TODO: Execute a single query that has all values
//					Object attributes[] = (String[])cu.getAttributes();
//					Object[] args = cu.getArgValues(km);
//					String[] argValues = null;
//					String pql = path + "?";
//					if (args != null && args.length > 0) {
//						argValues = new String[args.length];
//						for (int i = 0; i < argValues.length; i++) {
//							argValues[i] = args[i].toString();
//							pql += attributes[i] + ":(\"" + argValues[i] + "\") ";
//						}
//					}
//					List list = temporalBiz.getQueryValues(pql, validAtTime, asOfTime);
//					if (arg.isOne()) {
//						if (list == null || list.size() == 0) {
//							km.put(arg.getArgName(), null);
//						} else {
//							km.put(arg.getArgName(), list.get(0));
//						}
//					} else {
//						km.put(arg.getArgName(), list);
//					}
//				}
//			} else {
//				for (KeyMap km : keyMapCollection) {
//					// TODO: Execute a single query that has all values
//					String identityKey = cu.getQuery(km);
//					Object value = temporalBiz.get(identityKey, validAtTime, asOfTime);
//					if (arg.isOne() || value == null) {
//						km.put(arg.getArgName(), value);
//					} else {
//						km.put(arg.getArgName(), Collections.singletonList(value));
//					}
//				}
//			}
		}
		
		return null;
	}
}
