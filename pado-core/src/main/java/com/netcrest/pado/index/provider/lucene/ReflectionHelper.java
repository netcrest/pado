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
package com.netcrest.pado.index.provider.lucene;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.TemporalAttribute;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ReflectionHelper
{
	public static Field[] getPublicFields(Class clazz)
	{
		if (clazz.isAssignableFrom(String.class) || clazz.isPrimitive()) {
			return null;
		}

		Field[] fields = clazz.getDeclaredFields();
		ArrayList list = new ArrayList(fields.length);

		// scan fields
		for (int i = 0; i < fields.length; i++) {
			int modifiers = fields[i].getModifiers();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && !Modifier.isTransient(modifiers)) {
				list.add(fields[i]);
			}
		}
		return (Field[]) list.toArray(new Field[0]);
	}
	
	public static Method[] getPublicGetters(Class clazz)
	{
		if (clazz.isAssignableFrom(String.class) || clazz.isPrimitive()) {
			return null;
		}

		Method[] methods = clazz.getMethods();
		ArrayList list = new ArrayList(methods.length);

		// scan methods
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			int modifiers = method.getModifiers();
			String methodName = methods[i].getName();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) 
					&& ( methodName.startsWith("get") || methodName.startsWith("is") 
							&& (method.getReturnType() == Boolean.class || method.getReturnType().toString().equals("boolean")))
					&& (method.getParameterTypes().length == 0) && !methodName.equals("getClass")) {
				list.add(methods[i]);
			}
		}
		return (Method[]) list.toArray(new Method[0]);
	}
	
	public static Method[] getAttributeGetters(Class clazz)
	{
		if (isPrimitiveWrapper(clazz)) {
			return null;
		}

		Annotation[] annotations = clazz.getAnnotations();
		ArrayList<Method> list = new ArrayList(annotations.length);

		Field[] fields = clazz.getDeclaredFields();
		
		// scan fields
		ArrayList<Field> fieldList = new ArrayList();
		for (Field field : fields) {
			int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
				Annotation annotation = field.getAnnotation(TemporalAttribute.class);
				if (annotation instanceof TemporalAttribute) {
					fieldList.add(field);
				}
			}
		}
				
		// scan annotations
		for (Field field : fieldList) {
			String name = field.getName();
			if (name.length() == 1) {
				name = "get" + name.toUpperCase();
			} else {
				name = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
			}
			
			try {
				Method method = clazz.getMethod(name, null);
				list.add(method);
			} catch (Exception ex) {
				Logger.warning("LuceneTemporalManager.getAttributeGetters() error", ex);
			}
		}
		return (Method[]) list.toArray(new Method[0]);
	}
	
	public static String getGetterPropertyName(Method getterMethod)
	{
		if (getterMethod == null) {
			return null;
		}
		String name = getterMethod.getName();
		if (name.startsWith("is")) {
			return name.substring(2);
		} else {
			return name.substring(3);
		}
	}
	
	public static boolean isPrimitiveWrapper(Class clazz)
	{
		return clazz == String.class || clazz.isPrimitive() || clazz == Byte.class ||
				clazz == Short.class || clazz == Integer.class || clazz == Long.class || 
				clazz == Float.class || clazz == Double.class || clazz == Character.class || clazz == Boolean.class;
	}
}
