package com.netcrest.pado.biz.info;

import com.netcrest.pado.data.jsonlite.JsonLite;

/**
 * MethodReturnInfo contains method return information as part of BizInfo.
 * 
 * @author dpark
 *
 */
public class MethodReturnInfo
{
	protected String returnType;
	protected String description;
	protected MethodArgInfo[] argInfos;

	public MethodReturnInfo()
	{
	}

	public MethodReturnInfo(String returnType, String description, MethodArgInfo[] argInfos)
	{
		this.returnType = returnType;
		this.description = description;
	}

	public String getReturnType()
	{
		return returnType;
	}

	public void setReturnType(String returnType)
	{
		this.returnType = returnType;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public MethodArgInfo[] getArgInfos()
	{
		return argInfos;
	}

	public void setArgInfos(MethodArgInfo[] argInfos)
	{
		this.argInfos = argInfos;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JsonLite<?> toJson()
	{
		JsonLite jl = new JsonLite();
		jl.put("Type", returnType);
		jl.put("Desc", description);
		if (argInfos != null && argInfos.length > 0) {
			Object[] jlArray = new Object[argInfos.length];
			int i = 0;
			for (MethodArgInfo methodArgInfo : argInfos) {
				jlArray[i++] = methodArgInfo.toJson();
			}
			jl.put("Args", jlArray);
		}
		return jl;
	}
}