package com.netcrest.pado.biz.info;

import com.netcrest.pado.data.jsonlite.JsonLite;

/**
 * MethodArgInfo contains method argument information as part of BizInfo.
 * @author dpark
 *
 */
public class MethodArgInfo
{
	protected String argName;
	protected String argType;
	protected String description;
	protected String defaultValue;
	protected String example;
	protected MethodArgInfo[] nestedMethodArgInfos;
	protected boolean isRequired = true;

	public MethodArgInfo()
	{
	}
	
	public MethodArgInfo(String argName, String argType, String description, String defaultValue, String example,
			 boolean isRequired, MethodArgInfo[] nestedMethodArgInfos)
	{
		this.argName = argName;
		this.argType = argType;
		this.description = description;
		this.defaultValue = defaultValue;
		this.example = example;
		this.isRequired = isRequired;
		this.nestedMethodArgInfos = nestedMethodArgInfos;
	}

	public String getArgName()
	{
		return argName;
	}

	public void setArgName(String argName)
	{
		this.argName = argName;
	}

	public String getArgType()
	{
		return argType;
	}

	public void setArgType(String argType)
	{
		this.argType = argType;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public String getExample()
	{
		return example;
	}

	public void setExample(String example)
	{
		this.example = example;
	}

	public MethodArgInfo[] getNestedMethodArgInfos()
	{
		return nestedMethodArgInfos;
	}

	public void setNestedMethodArgInfo(MethodArgInfo[] nestedMethodArgInfos)
	{
		this.nestedMethodArgInfos = nestedMethodArgInfos;
	}

	public boolean isRequired()
	{
		return isRequired;
	}

	public void setRequired(boolean isRequired)
	{
		this.isRequired = isRequired;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JsonLite<?> toJson()
	{
		JsonLite jl = new JsonLite();
		jl.put("Name", argName);
		if (argType != null) {
			jl.put("Type", argType);
		}
		if (description != null) {
			jl.put("Desc", description);
		}
		if (defaultValue != null) {
			jl.put("Default", defaultValue);
		}
		if (example != null) {
			jl.put("Example", example);
		}
		if (isRequired) {
			jl.put("IsRequired", isRequired);
		}
		if (nestedMethodArgInfos != null && nestedMethodArgInfos.length > 0) {
			Object[] jlArray = new Object[nestedMethodArgInfos.length];
			int i = 0;
			for (MethodArgInfo methodArgInfo : nestedMethodArgInfos) {
				jlArray[i++] = methodArgInfo.toJson();
			}
			jl.put("Args", jlArray);
		}
		return jl;
	}
}