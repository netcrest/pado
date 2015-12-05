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
package com.netcrest.pado.gemfire.security;

import java.security.Principal;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.operations.ExecuteFunctionOperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AccessControl;
import com.gemstone.gemfire.security.NotAuthorizedException;
import com.netcrest.pado.biz.gemfire.proxy.functions.BizArguments;
import com.netcrest.pado.biz.gemfire.proxy.functions.BizContextClientImpl;

/**
 * NoAuthorization allows all operations to all users. It does not validate
 * session token such that it allows staled sessions to continue to operate.
 * Note that idle sessions are still automatically removed from the grid.
 * 
 * @author dpark
 * 
 */
public class NoAuthorization implements AccessControl
{
	private GemfireUserPrincipal principal;
	private DistributedMember remoteMember;
	private LogWriter logger;

	public NoAuthorization()
	{
	}

	public static AccessControl create()
	{
		return new NoAuthorization();
	}

	public void init(Principal principal, DistributedMember remoteMember, Cache cache) throws NotAuthorizedException
	{
		this.principal = (GemfireUserPrincipal) principal;
		this.remoteMember = remoteMember;
		this.logger = cache.getSecurityLogger();
	}

	@Override
	public boolean authorizeOperation(String regionName, OperationContext context)
	{
		OperationCode opCode = context.getOperationCode();

		if (context.isPostOperation() == false) {
			if (opCode == OperationCode.EXECUTE_FUNCTION) {
				ExecuteFunctionOperationContext efoc = (ExecuteFunctionOperationContext) context;
				// Set the principal in the OperationContext so that the
				// Function has access to it
				Object args = efoc.getArguments();
				if (args != null && args instanceof BizArguments) {
					BizArguments bizArgs = (BizArguments) args;
					BizContextClientImpl client = (BizContextClientImpl) bizArgs.getBizContext();
					String targetClassName = efoc.getFunctionId();
					if (client != null && client.getGridId() == null
							&& "com.netcrest.pado.biz.server.IPadoBiz".equals(targetClassName) == false) {
						if (client.getUserContext() != null) {
							client.getUserContext().getUserInfo().setUserPrincipal(principal);
						}
					}
				}
			}
		}

		return true;
	}

	public void close()
	{
	}

}
