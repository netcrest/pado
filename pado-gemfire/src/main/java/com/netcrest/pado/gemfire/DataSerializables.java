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
package com.netcrest.pado.gemfire;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.Instantiator;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalDataNull;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalKey;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalValue;

/**
 * GemFire DataSeralizable class IDs for Pado data classes. By default, Pado
 * reserves [1001, 1100] class IDs. The default can be changed using
 * the "class.id.start" property which as the default value of 1001.
 * 
 * @author dpark
 *
 */
public class DataSerializables
{
	static {

		int startId = Integer.parseInt(PadoUtil.getProperty(Constants.PROP_CLASS_ID_START,
				Constants.DEFAULT_CLASS_ID_START_DEFAULT));
		int classId = startId;

		Instantiator.register(new Instantiator(GemfireTemporalKey.class, classId++) {
			public DataSerializable newInstance()
			{
				return new GemfireTemporalKey();
			}
		});

		Instantiator.register(new Instantiator(GemfireTemporalValue.class, classId++) {
			public DataSerializable newInstance()
			{
				return new GemfireTemporalValue();
			}
		});

		Instantiator.register(new Instantiator(GemfireTemporalData.class, classId++) {
			public DataSerializable newInstance()
			{
				return new GemfireTemporalData();
			}
		});

		Instantiator.register(new Instantiator(GemfireTemporalDataNull.class, classId++) {
			public DataSerializable newInstance()
			{
				return new GemfireTemporalDataNull();
			}
		});
		
		Instantiator.register(new Instantiator(JsonLite.class, classId++) {
			public DataSerializable newInstance()
			{
				return new JsonLite();
			}
		});

		Logger.config("Pado DataSerializable classes registered. Class IDs [" + startId + ", " + (classId - 1) + "]");
	}
}
