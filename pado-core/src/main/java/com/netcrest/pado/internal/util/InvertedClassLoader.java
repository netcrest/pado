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
package com.netcrest.pado.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class InvertedClassLoader extends URLClassLoader
{
	@SuppressWarnings("rawtypes")
	HashMap classMap = new HashMap();
	URL[] urls;

	public InvertedClassLoader(URL[] urls)
	{
		super(urls);
		this.urls = urls;
	}
	
	public Class loadClass(String name) throws ClassNotFoundException {  
        return findClass(name);  
    } 

	protected Class<?> findClass(final String name) throws ClassNotFoundException
	{
		
		Class result = findLoadedClass(name);
		if (result != null) {
			return result;
		}
		
		result = (Class) classMap.get(name);
		if (result != null) {
			return result;
		}

		JarFile jar = null;
		try {
			byte classBytes[];
			for (URL url : urls) {
				File file = new File(url.toURI());
				jar = new JarFile(file);
				JarEntry entry = jar.getJarEntry(name.replace('.', '/') + ".class");
				if (entry != null) {
					InputStream is = jar.getInputStream(entry);
					ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
					int nextValue = is.read();
					while (-1 != nextValue) {
						byteStream.write(nextValue);
						nextValue = is.read();
					}
					classBytes = byteStream.toByteArray();
					byteStream.close();
					try {
						result = super.defineClass(name, classBytes, 0, classBytes.length);
					} catch (Throwable th) {
						// ignore
					}
					if (result == null) {
						result = super.findClass(name);
					} else {
						classMap.put(name, result);
					}
				}
				jar.close();
			}
		} catch (Throwable e) {
			result = findSystemClass(name);  
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (IOException e) {
					// ignore
				}
			}
			
			if (result == null) {
				result = findSystemClass(name);  
			}
		}

		return result;
	}
}
