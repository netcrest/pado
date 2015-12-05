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
package com.netcrest.pado.biz.gemfire.client.proxy.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Set;

import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.netcrest.pado.internal.impl.GridService;

/**
 * Invocation handler that manages invocations on specific DS member(s)
 *
 */
public class MembersInvocationHandler <T>
	extends GemfireClientBizInvocationHandler<T>
	implements InvocationHandler {

	private final DistributedSystem ds;
	private Set<DistributedMember> members;

	/**
	 * @param targetClass	Service interface class
	 * @param ds			Distributed System
	 */
	public MembersInvocationHandler(Class<T> targetClass, DistributedSystem ds, GridService gridService) {
		super (targetClass, gridService);
		this.ds = ds;
	}

	/**
	 * @param targetClass	Service interface
	 * @param ds			Distributed System
	 * @param members		Target member set
	 */
	public MembersInvocationHandler(Class<T> targetClass, DistributedSystem ds,
			Set<DistributedMember> members, String appId, GridService gridService) {
		this (targetClass, ds, gridService);
		this.members = members;
	}

	
	/* 
	 * Performs invocation routing based on the configured distributed 
	 * system and optionally an set of members. Will make use of any
	 * additional annotations associated with the class to tailor the
	 * invocation
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Execution exec= null;
		if (members == null || members.size() == 0) {
			exec = FunctionService.onMembers (ds);
		} else {
			exec = FunctionService.onMembers (ds, members);
		}
		
		return doInvocation(null, exec, method, args);
		
		
	}

}
