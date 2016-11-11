package com.netcrest.pado.info;

import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.internal.pql.antlr4.PqlEvalDriver;

@SuppressWarnings("rawtypes")
public abstract class VirtualPathInfo extends PathInfo
{
	protected KeyMap vpd;

	public VirtualPathInfo()
	{
	}

	public KeyMap getVirtualPathDefinition()
	{
		return vpd;
	}

	public String[] getArgs()
	{
		if (vpd == null) {
			return null;
		}
		String queryStatement = (String) vpd.get("Query");
		if (queryStatement == null) {
			return null;
		}
		return PqlEvalDriver.getArgNames(queryStatement);
	}
}
