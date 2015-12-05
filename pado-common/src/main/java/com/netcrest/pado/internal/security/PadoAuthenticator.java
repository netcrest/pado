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
package com.netcrest.pado.internal.security;

public class PadoAuthenticator
{
	public static LoginStatus authenticate(String appId, String domain, String username, char[] pw)
	{
		LoginStatus loginStatus = new LoginStatus();
		loginStatus.authenticated = true;
		loginStatus.message = "Login successful";
		return loginStatus;
	}

	public static class LoginStatus
	{
		boolean authenticated = false;
		String message;

		public boolean isAuthenticated()
		{
			return authenticated;
		}

		public void setAuthenticated(boolean authenticated)
		{
			this.authenticated = authenticated;
		}

		public String getMessage()
		{
			return message;
		}

		public void setMessage(String message)
		{
			this.message = message;
		}
	}
}
