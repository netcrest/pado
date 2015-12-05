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
package com.netcrest.pado.info;

import java.util.Set;

/**
 * UserLoginInfo contains security-sensitive user information that is isolated
 * from {@link LoginInfo}.
 * 
 * @author dpark
 * 
 */
public abstract class UserLoginInfo extends LoginInfo
{
	/**
	 * Private key received from the grid for encrypting data
	 */
	protected byte[] privateKey;

	/**
	 * Creates an empty UserLoginInfo object.
	 */
	public UserLoginInfo()
	{
	}

	/**
	 * Creates a UserLoginInfo.
	 * 
	 * @param appId
	 *            App ID
	 * @param domain
	 *            Optional domain name
	 * @param username
	 *            User name
	 * @param token
	 *            User session token
	 * @param bizSet
	 *            Set of BizInfo objects pertaining to the app and user.
	 * @param privateKey
	 *            AES private key for the client to encrypt/decrypt data.
	 */
	public UserLoginInfo(String appId, String domain, String username, Object token, Set<BizInfo> bizSet,
			byte[] privateKey)
	{
		super(appId, domain, username, token, bizSet);
		this.privateKey = privateKey;
	}

	/**
	 * Returns the AES private key for encrypting/decrypting data.
	 */
	public byte[] getPrivateKey()
	{
		return privateKey;
	}
}
