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
package com.netcrest.pado.biz.gemfire.client.proxy.functionrouters;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.gemstone.gemfire.cache.execute.Execution;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.biz.gemfire.client.proxy.handlers.GemfireClientBizInvocationHandler;

/**
 * Defines an interface for classes that add the specific routing 
 * discriminator to the function execution context.
 *
 */
public interface FunctionRouter {
	
	public Execution addRoutingContext (Annotation a, GemfireClientBizInvocationHandler handler, String gridId, 
			IBizContextClient bizContext, Set routingKeySet, Object[] args);

}
