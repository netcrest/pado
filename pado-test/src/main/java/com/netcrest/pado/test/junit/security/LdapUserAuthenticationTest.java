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
package com.netcrest.pado.test.junit.security;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.security.server.LdapUserAuthentication;

/**
 * LdapUserAuthentiationTest tests the LdapUserAuthentication plug-in by 
 * direct invoation without going thru the Pado grid.
 * 
 * @author dpark
 *
 */
public class LdapUserAuthenticationTest
{
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("javax.net.ssl.trustStore", "ldap/example/ssl/trusted.keystore");
		System.setProperty("pado.security.ldap.url", "ldaps://localhost:10636");
		System.setProperty("pado.security.ldap.base", "dc=newco,dc=com");
		System.setProperty("pado.security.ldap.user.filter", "(&(objectClass=inetOrgPerson)(uid={0}))");
		System.setProperty("pado.security.ldap.memberof.base", "ou=groups,ou=Pado,dc=newco,dc=com");
		System.setProperty("pado.security.ldap.memberof.filter", "(&(objectClass=groupOfNames)(member={0}))");
	}

	@AfterClass
	public static void closePado()
	{
	}
	
	@Test
	public void testAuthenticator_jnj() throws Exception
	{
		LdapUserAuthentication lua = new LdapUserAuthentication();
		String appId = "sys";
		String domain = "pado";
		String username = "test1";
		String password = "test123";
		Properties props = new Properties();
		IUserPrincipal userPrincipal = lua.authenticate(appId, domain, username, password.toCharArray(), props);
		System.out.println(userPrincipal);
	}
}
