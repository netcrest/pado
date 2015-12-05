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

import java.util.concurrent.ConcurrentHashMap;

import com.netcrest.pado.index.provider.lucene.LuceneSearch;
import com.netcrest.pado.index.provider.lucene.TopNLuceneSearch;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.CompiledUnit.QueryLanguage;
import com.netcrest.pado.pql.PqlParser;

/**
 * 
 * Dynamically registers TextSearchProviders, provides specific configured text 
 * search provider
 *
 */
public class TextSearchProviderFactory
{
	private static TextSearchProviderFactory me = new TextSearchProviderFactory ();
	
	private ConcurrentHashMap<String, ITextSearchProvider> textSearchProvidersMap = new ConcurrentHashMap<String, ITextSearchProvider>();
	
	public static TextSearchProviderFactory getInstance () {
		return me;
	}
	
	/**
	 * @TODO The gridQuery will be supply a plugin provider class in the future
	 * 
	 * @param language
	 * @param criteria
	 * @return
	 */
	public ITextSearchProvider getProvider (CompiledUnit.QueryLanguage language, GridQuery criteria) {
		if (language == QueryLanguage.LUCENE) {
			if (PqlParser.isTopN(criteria)) {
				//@TODO
				//criteria may contain another provider name which we should use to retrieve
				//dynamic register providers at each deployment
				String searchMapper = PqlParser.getTextSearchProvider(criteria);
				if (searchMapper != null) {
					if (!textSearchProvidersMap.containsKey(searchMapper)) {
						Logger.error(String.format("%s is not registered with %s", searchMapper, this.getClass().getName()));
						return null;
					} else {
						return textSearchProvidersMap.get (searchMapper);
					}
				}
				return TopNLuceneSearch.getTopLuceneSearch(criteria.getFullPath());
			}
			return LuceneSearch.getLuceneSearch(criteria.getFullPath());
		} 
		return null;
	}
	
	/**
	 * Called in deployment to register TextSearchProvider
	 * @param providerKey
	 * @param provider
	 */
	public void registerTextSearchProvider (String providerKey, ITextSearchProvider provider) {
		if (providerKey != null) {
			this.textSearchProvidersMap.put(providerKey, provider);
		}
	}
	
	/**
	 * Remove an installed TextSearchProvider
	 * @param providerKey
	 */
	public void removeTextSearchProvider (String providerKey) {
		if (providerKey != null) {
			this.textSearchProvidersMap.remove(providerKey);
		}		
		
	}
}
