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
package com.netcrest.pado.demo.bank.market;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.Instantiator;
import com.netcrest.pado.demo.bank.market.data.CompanyInfo;
import com.netcrest.pado.demo.bank.market.data.Level2Data;
import com.netcrest.pado.demo.bank.market.data.OrderInfo;

public class DataSerializables
{
	static {
		Instantiator.register(new Instantiator(Level2Data.class, 43)
		{
			public DataSerializable newInstance()
			{
				return new Level2Data();
			}
		});

		Instantiator.register(new Instantiator(OrderInfo.class, 10)
		{
			public DataSerializable newInstance()
			{
				return new OrderInfo();
			}
		});

		Instantiator.register(new Instantiator(CompanyInfo.class, 42)
		{
			public DataSerializable newInstance()
			{
				return new CompanyInfo();
			}
		});
	}
}
