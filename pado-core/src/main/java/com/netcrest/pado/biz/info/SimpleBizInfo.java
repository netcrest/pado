package com.netcrest.pado.biz.info;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.netcrest.pado.IBizInfo;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.internal.factory.InfoFactory;

/**
 * SimpleBizInfo is an IBizInfo implementation class that provides convenience
 * methods for creating IBizInfo objects that explicitly expose IBiz object
 * information such as methods, descriptions, return types, and etc.. An
 * implementation class should extend this class and invoke the "create" methods
 * to supply class and method information.
 * 
 * @author dpark
 *
 */
public class SimpleBizInfo implements IBizInfo
{
	protected String name;
	protected String description;
	protected boolean isDepreated;
	protected MethodInfo[] methodInfos;
	protected List<MethodInfo> methodInfoList;
	protected String[] appIds;

	public SimpleBizInfo()
	{
	}

	public SimpleBizInfo(String name)
	{
		setName(name);
	}

	private void initDefaults()
	{
		if (this.name == null) {
			this.description = null;
			this.isDepreated = false;
			this.appIds = null;
			this.methodInfos = null;
			return;
		}

		// Add all methods found in IBiz.
		// No description, defaultValue, example, nestedArgs. isRequired=true
		// always
		ClassLoader classLoader = this.getClass().getClassLoader();
		try {
			Class<?> clazz = classLoader.loadClass(name);
			Method[] methods = clazz.getMethods();
			ArrayList<MethodInfo> list = new ArrayList<MethodInfo>(methods.length);
			for (Method method : methods) {
				String methodName = method.getName();

				// Skip private methods
				if (methodName.startsWith("__") || methodName.equals("getBizContext")) {
					continue;
				}
				MethodInfo methodInfo = createMethod(methodName, null, false, null);
				list.add(methodInfo);

				// Args
				Class<?>[] params = method.getParameterTypes();
				MethodArgInfo[] argInfos = new MethodArgInfo[params.length];
				int i = 0;
				for (Class<?> param : params) {
					argInfos[i++] = createArg(param.getName(), param.getName(), null, null, null, true);
				}
				methodInfo.setArgInfos(argInfos);

				// Return type
				Class<?> retType = method.getReturnType();
				MethodReturnInfo retInfo = createReturn(retType.getName(), null, null);
				methodInfo.setReturnInfo(retInfo);
			}
			this.methodInfos = list.toArray(new MethodInfo[list.size()]);
		} catch (ClassNotFoundException e) {
			// ignore
		}
	}

	@Override
	public String getBizInterfaceName()
	{
		return name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public MethodInfo[] getMethodInfo()
	{
		return methodInfos;
	}

	public MethodInfo[] getMethodInfos()
	{
		return methodInfos;
	}

	public void setMethodInfos(MethodInfo... methodInfos)
	{
		this.methodInfos = methodInfos;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		if (name != null && name.equals(this.name)) {
			return;
		}
		this.name = name;
		initDefaults();
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isDepreated()
	{
		return isDepreated;
	}

	public void setDepreated(boolean isDepreated)
	{
		this.isDepreated = isDepreated;
	}

	public List<MethodInfo> getMethodInfoList()
	{
		return methodInfoList;
	}

	public void setMethodInfoList(List<MethodInfo> methodInfoList)
	{
		this.methodInfoList = methodInfoList;
	}

	public String[] getAppIds()
	{
		return appIds;
	}

	public void setAppIds(String... appIds)
	{
		if (appIds == null) {
			this.appIds = null;
		} else {
			HashSet<String> set = new HashSet<String>(appIds.length, 1f);
			for (String appId : appIds) {
				set.add(appId);
			}
			set.remove("sys");
			this.appIds = set.toArray(new String[set.size()]);
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JsonLite toJson()
	{
		JsonLite jl = new JsonLite();
		jl.put("Name", name);
		if (description != null) {
			jl.put("Desc", description);
		}
		if (isDepreated) {
			jl.put("IsDeprecated", isDepreated);
		}
		if (appIds != null) {
			jl.put("AppIds", appIds);
		}
		if (methodInfos != null && methodInfos.length > 0) {
			JsonLite methods[] = new JsonLite[methodInfos.length];
			jl.put("Methods", methods);
			int i = 0;
			for (MethodInfo methodInfo : methodInfos) {
				methods[i++] = methodInfo.toJson();
			}
		}
		return jl;
	}

	@Override
	public IBizInfo toBizInfo()
	{
		SimpleBizInfo bizInfo = InfoFactory.getInfoFactory().createSimpleBizInfo(name);
		bizInfo.description = this.description;
		bizInfo.isDepreated = this.isDepreated;
		bizInfo.appIds = this.appIds;
		bizInfo.methodInfos = this.methodInfos;
		return bizInfo;
	}

	/**
	 * Creates a MethodInfo object that contains the specified parameters.
	 * 
	 * @param name
	 *            Method name
	 * @param description
	 *            Method description
	 * @param isDeprecated
	 *            true to deprecate the method
	 * @param retInfo
	 *            MethosReturnInfo object containing return information
	 * @param argInfos
	 *            Method argument information in array
	 * @return MethodInfo instance containing the specified parameters
	 */
	public MethodInfo createMethod(String name, String description, boolean isDeprecated, MethodReturnInfo retInfo,
			MethodArgInfo... argInfos)
	{
		MethodInfo methodInfo = InfoFactory.getInfoFactory().createMethodInfo();
		methodInfo.setName(name);
		methodInfo.setDescription(description);
		methodInfo.setArgInfos(argInfos);
		methodInfo.setDeprecated(isDeprecated);
		methodInfo.setReturnInfo(retInfo);
		return methodInfo;
	}

	/**
	 * Creates a MethodArgInfo object describing a return type argument that
	 * contains the specified parameters.
	 * 
	 * @param argName
	 *            Argument name
	 * @param argType
	 *            Argument type
	 * @param description
	 *            Argument description
	 * @param defaultValue
	 *            Default value
	 * @param example
	 *            Value example
	 * @param isRequired
	 *            true if this argument is required
	 * @param nestedMethodArgInfos
	 *            Nested arguments
	 * @return MethodArgInfo instance containing the specified parameters
	 */
	public MethodArgInfo createReturnArg(String argName, String argType, String description, String defaultValue,
			String example, boolean isRequired, MethodArgInfo... nestedMethodArgInfos)
	{
		return createArg(argName, argType, description, defaultValue, example, isRequired, nestedMethodArgInfos);
	}

	/**
	 * Creates a MethodArgInfo object describing a method argument that contains
	 * the specified parameters.
	 * 
	 * @param argName
	 *            Argument name
	 * @param argType
	 *            Argument type
	 * @param description
	 *            Argument description
	 * @param defaultValue
	 *            Default value
	 * @param example
	 *            Value example
	 * @param isRequired
	 *            true if this argument is required
	 * @param nestedMethodArgInfos
	 *            Nested arguments
	 * @return MethodArgInfo instance containing the specified parameters
	 */
	public MethodArgInfo createArg(String argName, String argType, String description, String defaultValue,
			String example, boolean isRequired, MethodArgInfo... nestedMethodArgInfos)
	{
		MethodArgInfo methodArgInfo = InfoFactory.getInfoFactory().createMethodArgInfo();
		methodArgInfo.setArgName(argName);
		methodArgInfo.setArgType(argType);
		methodArgInfo.setDescription(description);
		methodArgInfo.setDefaultValue(defaultValue);
		methodArgInfo.setExample(example);
		methodArgInfo.setRequired(isRequired);
		methodArgInfo.setNestedMethodArgInfo(nestedMethodArgInfos);
		return methodArgInfo;
	}

	/**
	 * This method has no effect and always returns null. Its purpose is to make the IBizInfo
	 * implementation code more legible.
	 * 
	 * @return
	 */
	public MethodReturnInfo createReturn()
	{
		return null;
	}

	/**
	 * Creates a MethodReturnInfo object describing the return type information.
	 * 
	 * @param retType
	 *            Return type
	 * @param description
	 *            Return type decription
	 * @param defaultValue
	 *            Default value
	 * @param argInfos
	 *            Return type arguments
	 * @return MethodReturnInfo instance containing the specified parameters
	 */
	public MethodReturnInfo createReturn(String retType, String description, String defaultValue,
			MethodArgInfo... argInfos)
	{
		MethodReturnInfo retInfo = InfoFactory.getInfoFactory().createMethodReturnInfo();
		retInfo.setReturnType(retType);
		retInfo.setDescription(description);
		retInfo.setArgInfos(argInfos);
		return retInfo;
	}
}
