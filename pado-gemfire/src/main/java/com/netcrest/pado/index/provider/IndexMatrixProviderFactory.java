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
package com.netcrest.pado.index.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netcrest.pado.index.exception.GridQueryProviderConfigurationException;
import com.netcrest.pado.index.helper.ComparatorFactory;
import com.netcrest.pado.index.internal.ServiceFinder;

/**
 * Use the services API (as detailed in the JAR specification) to register all
 * available <code>{@link IIndexMatrixProvider}</code> The services API will
 * look for a class name in the file
 * <code>META-INF/services/com.netcrest.pado.index.provider.IIndexMatrixProvider</code>
 * in the jars in runtime class loader and register them using
 * <code>{@link IIndexMatrixProvider#getProviderId()}</code>
 * 
 */
public class IndexMatrixProviderFactory
{

	private static volatile IndexMatrixProviderFactory instance;
	private Map<String, Class<?>> indexMatrixProviderMap = new HashMap<String, Class<?>>();
	private ComparatorFactory compFactory = null;

	private IndexMatrixProviderFactory()
	{
		init();
	}

	private void init()
	{
		List<String> allProviderNames = ServiceFinder.findJarServiceProvider(IIndexMatrixProvider.class.getName());
		for (String clsName : allProviderNames) {
			try {
				IIndexMatrixProvider provider = (IIndexMatrixProvider) Class.forName(clsName, true,
						Thread.currentThread().getContextClassLoader()).newInstance();
				indexMatrixProviderMap.put(provider.getProviderId(), provider.getClass());
			} catch (Exception ex) {
				throw new GridQueryProviderConfigurationException("Can not instantiate IndexMatrixProvider", ex);
			}
		}
		compFactory = new ComparatorFactory();
	}

	public static IndexMatrixProviderFactory getInstance()
	{
		if (instance == null) {
			synchronized (IndexMatrixProviderFactory.class) {
				if (instance == null)
					instance = new IndexMatrixProviderFactory();
			}
		}
		return instance;
	}

	public ComparatorFactory getComparatorFactory()
	{
		return compFactory;
	}

	public IIndexMatrixProvider getProviderInstance(final String providerId)
	{
		Class<?> cls = indexMatrixProviderMap.get(providerId);
		try {
			if (cls != null) {
				return (IIndexMatrixProvider) cls.newInstance();
			}
		} catch (Exception ex) {
			throw new GridQueryProviderConfigurationException("Can not instantiate IndexMatrixProvider:" + providerId,
					ex);
		}
		throw new GridQueryProviderConfigurationException("Can not instantiate IndexMatrixProvider:" + providerId);
	}
}
