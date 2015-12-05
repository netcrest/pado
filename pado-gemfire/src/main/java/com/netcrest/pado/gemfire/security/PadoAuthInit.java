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

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AuthInitialize;
import com.gemstone.gemfire.security.AuthenticationFailedException;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.security.RSACipher;

public class PadoAuthInit implements AuthInitialize
{
	protected LogWriter securitylog;

	protected LogWriter systemlog;

	public void close()
	{
	}

	public static AuthInitialize create()
	{
		return new PadoAuthInit();
	}

	public PadoAuthInit()
	{
	}

	private AuthenticationFailedException getException(String exStr, Exception cause)
	{
		String exMsg = this.getClass().getSimpleName() + ": Authentication failed: " + exStr;
		if (cause != null) {
			return new AuthenticationFailedException(exMsg, cause);
		} else {
			return new AuthenticationFailedException(exMsg);
		}
	}
	
	public void init(LogWriter systemLogger, LogWriter securityLogger) throws AuthenticationFailedException
	{
		this.systemlog = systemLogger;
		this.securitylog = securityLogger;
	}

	public Properties getCredentials(Properties props, DistributedMember server, boolean isPeer)
			throws AuthenticationFailedException
	{
		if (PadoUtil.isProperty(Constants.PROP_SECURITY_ENABLED)) {
			try {
				RSACipher cipher = new RSACipher(false);
				return cipher.getSignature(RSACipher.createCredentialProperties());
			} catch (InvalidKeyException ex) {
				throw getException(ex.getMessage(), ex);
			} catch (UnrecoverableKeyException ex) {
				throw getException(ex.getMessage(), ex);
			} catch (KeyStoreException ex) {
				throw getException(ex.getMessage(), ex);
			} catch (NoSuchAlgorithmException ex) {
				throw getException(ex.getMessage(), ex);
			} catch (CertificateException ex) {
				throw getException(ex.getMessage(), ex);
			} catch (SignatureException ex) {
				throw getException(ex.getMessage(), ex);
			} catch (IOException ex) {
				throw getException(ex.getMessage(), ex);
			} catch (Exception ex) {
				throw getException(ex.getMessage(), ex);
			}
		} else {
			return null;
		}
	}
}
