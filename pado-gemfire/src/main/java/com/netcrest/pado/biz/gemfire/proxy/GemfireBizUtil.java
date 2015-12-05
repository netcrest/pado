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
package com.netcrest.pado.biz.gemfire.proxy;

import java.lang.reflect.Method;

import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.internal.biz.util.BizUtil;

public class GemfireBizUtil extends BizUtil
{
	private static String getGemfireFunctionId(Class<?> bizClass, Method bizMethod, boolean isClientFunction)
	{
		String methodName = getMethodName(bizMethod);
		if (methodName == null) {
			return null;
		}
		if (isClientFunction) {
			return getBizClassName(bizClass) + '_' + methodName;
		} else {
			return getBizClassName(bizClass) + '_' + methodName + ".s";
		}
	}

	public static String getGemfireFunctionId(Class<?> bizClass, Method bizMethod)
	{
		return getGemfireFunctionId(bizClass, bizMethod, true);
	}

	public static String getGemfireFunctionId(Class<?> bizClass)
	{
		return getBizClassName(bizClass);
	}
	
	/**
	 * Returns the parameter part of the GemFire function ID for the specified
	 * method.
	 * 
	 * @param m
	 *            IBiz method
	 */
	public static String getParamPartOfGfeFunctionId(Method m)
	{
		StringBuilder sb = new StringBuilder("");
		for (Class<?> param : m.getParameterTypes()) {
			sb.append(param.getName());
			sb.append('_');
		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	private static String generateMethodName(Method m)
	{
		return m.getName() + '_' + getParamPartOfGfeFunctionId(m);
	}
	
	/**
	 * Returns @{@link BizMethod#methodName()} of the specified class if it is
	 * defined. Otherwise, it generates the standard method name.
	 * 
	 * @param bizMethod @{@link BizMethod} annotated method
	 */
	public static String getMethodName(Method bizMethod)
	{
		BizMethod bm = (BizMethod) bizMethod.getAnnotation(BizMethod.class);
		if (bm == null) {
			return null;
		}
		return bm.methodName().equals("") ? generateMethodName(bizMethod) : bm.methodName();
	}
}
