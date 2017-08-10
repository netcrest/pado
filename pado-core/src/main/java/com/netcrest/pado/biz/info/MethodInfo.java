package com.netcrest.pado.biz.info;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.internal.factory.InfoFactory;

/**
 * MethodInfo contains IBiz method information as part of BizInfo.
 * @author dpark
 *
 */
public class MethodInfo
{
	protected String name;
	protected String description;
	protected boolean isDeprecated = false;
	protected MethodArgInfo[] argInfos;
	protected MethodReturnInfo retInfo;

	public MethodInfo()
	{
	}

	public MethodInfo(String name, String description, MethodArgInfo[] argInfos)
	{
		this.name = name;
		this.description = description;
		this.argInfos = argInfos;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isDeprecated()
	{
		return isDeprecated;
	}

	public void setDeprecated(boolean isDeprecated)
	{
		this.isDeprecated = isDeprecated;
	}

	public MethodArgInfo[] getArgInfos()
	{
		return argInfos;
	}

	public void setArgInfos(MethodArgInfo[] argInfos)
	{
		this.argInfos = argInfos;
	}
	
	public MethodReturnInfo createReturn(String retType, String description, String defaultValue)
	{
		MethodReturnInfo retInfo = InfoFactory.getInfoFactory().createMethodReturnInfo();
		retInfo.setReturnType(retType);
		retInfo.setDescription(description);
		return retInfo;
	}

	public MethodReturnInfo getReturnInfo()
	{
		return retInfo;
	}

	public void setReturnInfo(MethodReturnInfo retInfo)
	{
		this.retInfo = retInfo;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JsonLite<?> toJson()
	{
		JsonLite jl = new JsonLite();
		jl.put("Name", name);
		if (description != null) {
			jl.put("Desc", description);
		}
		if (isDeprecated) {
			jl.put("IsDeprecated", isDeprecated);
		}
		if (argInfos != null && argInfos.length > 0) {
			Object[] jlArray = new Object[argInfos.length];
			int i = 0;
			for (MethodArgInfo methodArgInfo : argInfos) {
				jlArray[i++] = methodArgInfo.toJson();
			}
			jl.put("Args", jlArray);
		}
		if (retInfo != null) {
			jl.put("Return", retInfo.toJson());
		}
		return jl;
	}
}
