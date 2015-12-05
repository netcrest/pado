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
package com.netcrest.pado.index.internal;

public interface Constants {

	public static final String CLIENT_POOL = "client";
	public static final String INDEX_POOL = System.getProperty("indexMatrixPoolName", "index");
	
	public static final String PROP_REGION_SYSTEM = "systemRegionPath";
	public static final String PROP_REGION_INDEX = "indexMatrixRegionPath";
	public static final String PROP_REGION_RESULTS = "indexMatrixResultsRegionPath";
	public static final String PROP_REGION_LUCENE = "luceneRegionPath";
	
	public static final String COMPARATOR_PROP_NAME = "ComparatorFactory";
	
	public static final int BEGINNING_OF_LIST = -1;
	public static final int END_OF_LIST = -1;	
	
	public static final int DEFAULT_INDEX_FETCH_SIZE = 500;
	public static final int WAIT_FOR_INDEX = 200;
	
	public static final String OQL_PROVIDER_KEY = "OQL";
	public static final String SERVER_PROVIDER_KEY = "SERVER";
	public static final String PQL_PROVIDER_KEY = "PQL";
	public static final String TEMPORAL_PROVIDER_KEY = "TEMPORAL";
	public static final String TEMPORAL_ENTRY_PROVIDER_KEY = "TEMPORAL_ENTRY";
	
	public static final String TOPN = "TOPN";
	public static final String TEXT_SEARCH_PROVIDER = "TEXT_SEARCH_PROVIDER";
	public static final String TEXT_SEARCH_TARGET_FIELD = "TEXT_SEARCH_TARGET_FIELD";
	public static final String TEXT_SEARCH_PHRASE = "TEXT_SEARCH_PHRASE";
	public static final String TEXT_SEARCH_SCORE = "TEXT_SEARCH_SCORE";
	
}
