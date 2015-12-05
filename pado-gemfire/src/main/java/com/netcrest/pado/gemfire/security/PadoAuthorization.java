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
import com.gemstone.gemfire.cache.operations.DestroyOperationContext;
import com.gemstone.gemfire.cache.operations.ExecuteFunctionOperationContext;
import com.gemstone.gemfire.cache.operations.GetOperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.cache.operations.QueryOperationContext;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AccessControl;
import com.gemstone.gemfire.security.NotAuthorizedException;
import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.biz.gemfire.proxy.functions.BizArguments;
import com.netcrest.pado.biz.gemfire.proxy.functions.BizContextClientImpl;
import com.netcrest.pado.exception.PadoServerException;
import com.netcrest.pado.info.LoginInfo;
import com.netcrest.pado.server.PadoServerManager;

/**
 * PadoAuthorization authorizes Pado operations.
 * 
 * @author dpark
 * 
 */
public class PadoAuthorization implements AccessControl
{
	private DistributedMember remoteMember;

	private LogWriter logger;

	public PadoAuthorization()
	{
	}

	public static AccessControl create()
	{
		return new PadoAuthorization();
	}

	@Override
	public void init(Principal principal, DistributedMember remoteMember, Cache cache) throws NotAuthorizedException
	{
		// Ignore principal. Due to the GemFire limitation of authentication 
		// performed per connection, we keep track of principal objects 
		// ourselves in PadoServerManager.
		this.remoteMember = remoteMember;
		this.logger = cache.getSecurityLogger();
	}

	@Override
	public boolean authorizeOperation(String regionName, OperationContext context)
	{
		boolean retval = true;
		OperationCode opCode = context.getOperationCode();

		if (context.isPostOperation()) {

			// Post-validation
			if (opCode == OperationCode.EXECUTE_FUNCTION) {
				ExecuteFunctionOperationContext efoc = (ExecuteFunctionOperationContext) context;
				// allows the result to be changed
				// efoc.setResult("foo");
			}
			
		} else {

			// Pre-validation
			if (opCode == OperationCode.GET) {
				GetOperationContext goc = (GetOperationContext) context;
			} else if (opCode == OperationCode.PUT || opCode == OperationCode.PUTALL) {
			} else if (opCode == OperationCode.QUERY) {
				QueryOperationContext qoc = (QueryOperationContext) context;
			} else if (opCode == OperationCode.DESTROY) {
				DestroyOperationContext doc = (DestroyOperationContext) context;
			} else if (opCode == OperationCode.EXECUTE_FUNCTION) {
				ExecuteFunctionOperationContext efoc = (ExecuteFunctionOperationContext) context;
				// Set the principal in the OperationContext so that the
				// Function has access to it
				Object args = efoc.getArguments();
				if (args != null && args instanceof BizArguments) {
					BizArguments bizArgs = (BizArguments) args;
					BizContextClientImpl client = (BizContextClientImpl) bizArgs.getBizContext();
					String targetClassName = efoc.getFunctionId();
					if (client != null && client.getGridId() == null
							&& targetClassName.equals("IPadoBiz") == false) {
						if (client.getUserContext() == null) {
							throw new PadoServerException("Access denied. Invalid session. IUserContext not provided.");
						} else {
							boolean isTokenValid = PadoServerManager.getPadoServerManager().isValidToken(
									client.getUserContext().getToken());
							if (isTokenValid == false) {
								LoginInfo loginInfo = PadoServerManager.getPadoServerManager().getLoginInfo(
										client.getUserContext().getToken());
								String appId = null;
								String username = null;
								if (loginInfo != null) {
									appId = loginInfo.getAppId();
									username = loginInfo.getUsername();
								}

								throw new PadoServerException(
										"Access denied. Invalid session. Please login with a valid account. "
												+ "[app-id=" + appId + ", user=" + username + ", token="
												+ client.getUserContext().getToken() + "]");
							} else {
								IUserPrincipal userPrincipal = PadoServerManager.getPadoServerManager().getUserPrincipal(client.getUserContext().getToken());
								client.getUserContext().getUserInfo().setUserPrincipal(userPrincipal);
							}
						}
					}
				}
			}
		}

		return retval;
	}

	public void close()
	{
	}

}
