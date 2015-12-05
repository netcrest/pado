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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import javax.naming.AuthenticationException;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AuthenticationFailedException;
import com.gemstone.gemfire.security.Authenticator;
import com.gemstone.gemfire.security.GemFireSecurityException;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.security.RSACipher;

public class PadoAuthenticator implements Authenticator
{
	protected LogWriter systemlog;

	protected LogWriter securitylog;

	private RSACipher cipher;
	private boolean isSecurityEnabled = false;

	public static Authenticator create()
	{
		return new PadoAuthenticator();
	}

	public PadoAuthenticator()
	{
		isSecurityEnabled = PadoUtil.isProperty(Constants.PROP_SECURITY_ENABLED);
	}

	public void init(Properties systemProps, LogWriter systemLogger, LogWriter securityLogger)
			throws AuthenticationFailedException
	{
		this.systemlog = systemLogger;
		this.securitylog = securityLogger;

		try {
			cipher = new RSACipher(true);
		} catch (AuthenticationException ex) {
			throw new AuthenticationFailedException("Exception occurred creating RSACipher. " + ex.getMessage());
		}
	}

	private AuthenticationFailedException getException(String exStr, Exception cause)
	{
		String exMsg = this.getClass().getSimpleName() + ": Authentication of client failed due to: " + exStr;
		if (cause != null) {
			return new AuthenticationFailedException(exMsg, cause);
		} else {
			return new AuthenticationFailedException(exMsg);
		}
	}

	private AuthenticationFailedException getException(String exStr)
	{
		return getException(exStr, null);
	}

	public Principal authenticate(Properties props, DistributedMember member) throws AuthenticationFailedException
	{
		try {
			if (isSecurityEnabled) {
				if (props == null) {
					throw getException("Security properties undefined");
				} else if (cipher.verifySignature(props) == false) {
					throw getException("verification of client signature failed");
				}
			}
		} catch (GemFireSecurityException ex) {
			throw ex;
		} catch (InvalidKeyException ex) {
			throw getException(ex.toString(), ex);
		} catch (NoSuchAlgorithmException ex) {
			throw getException(ex.toString(), ex);
		} catch (InvalidKeySpecException ex) {
			throw getException(ex.toString(), ex);
		} catch (SignatureException ex) {
			throw getException(ex.toString(), ex);
		} catch (UnsupportedEncodingException ex) {
			throw getException(ex.toString(), ex);
		} catch (Exception ex) {
			throw getException(ex.toString(), ex);

		}
		return null;
	}

	public void close()
	{
	}

}
