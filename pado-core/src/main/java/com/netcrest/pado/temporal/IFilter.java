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
package com.netcrest.pado.temporal;

import java.io.Serializable;

/**
 * IFilter filters the result set from the server side.
 * 
 * @author dpark
 * 
 * @param <K>
 */
public interface IFilter<K> extends Serializable
{
	/**
	 * Returns true if the specified key/data pair is valid. Valid pairs are
	 * included in the result set.
	 * 
	 * @param key Temporal key
	 * @param data Temporal data
	 */
	boolean isValid(ITemporalKey<K> key, ITemporalData<K> data);
}
