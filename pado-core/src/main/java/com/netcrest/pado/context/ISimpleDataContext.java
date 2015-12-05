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
package com.netcrest.pado.context;

import com.netcrest.pado.IDataContext;

/**
 * ISimpleDataContext is the default data context class supplied by Pado.
 * Applications typically override this class and provide context classes that
 * are tailored to business requirements.
 * 
 * @author dpark
 * 
 */
public interface ISimpleDataContext extends IDataContext
{
	/**
	 * Returns data information.
	 */
	IDataInfo getDataInfo();
}
