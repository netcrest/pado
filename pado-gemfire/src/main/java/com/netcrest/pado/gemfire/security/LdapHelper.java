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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * A simple LDAP helper singleton class for connecting to an LDAP service.
 * 
 * @author dpark
 *
 */
public class LdapHelper
{
	private static final String DEFAULT_CTX = "com.sun.jndi.ldap.LdapCtxFactory";
	
	/**
	 * Sets basic LDAP connection properties in env.
	 * @param env The LDAP security environment
	 * @param url The LDAP URL
	 * @param tracing  LDAP tracing level. Output to System.err
	 * @param referralType  Referral type: follow, ignore, or throw
	 * @param aliasType Alias type: finding, searching, etc.
	 * @throws NamingException Thrown if the passed in values are invalid
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Hashtable setupBasicProperties(Hashtable env, String url, boolean tracing, String referralType,
			String aliasType) throws NamingException
	{
		// sanity check
		if (url == null)
			throw new NamingException("URL not specified in openContext()!");

		// set the tracing level
		if (tracing)
			env.put("com.sun.jndi.ldap.trace.ber", System.err);

		// always use ldap v3
		env.put("java.naming.ldap.version", "3"); 

		// use jndi provider
		if (env.get(Context.INITIAL_CONTEXT_FACTORY) == null)
			env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_CTX); 

		// usually what we want
		env.put("java.naming.ldap.deleteRDN", "false"); 

		// follow, ignore, throw
		env.put(Context.REFERRAL, referralType); 

		// to handle non-standard binary attributes
		env.put("java.naming.ldap.attributes.binary", "photo jpegphoto jpegPhoto"); 

		// finding, searching, etc.
		env.put("java.naming.ldap.derefAliases", aliasType);
		
		// no authentication (may be modified by other code)
		env.put(Context.SECURITY_AUTHENTICATION, "none"); 

		// the ldap url to connect to; e.g. "ldap://foo.com:389"
		env.put(Context.PROVIDER_URL, url); 
		
		return env;
	}

	/**
	 * Sets the environment properties needed for a simple username +
	 * password authenticated jndi connection.
	 * 
	 * @param env  The LDAP security environment
	 * @param userDn The user distinguished name
	 * @param pwd The user password
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void setupSimpleSecurityProperties(Hashtable env, String userDn, char[] pwd)
	{
		// 'simple' = username + password
		env.put(Context.SECURITY_AUTHENTICATION, "simple"); 

		// add the full user dn
		env.put(Context.SECURITY_PRINCIPAL, userDn); 

		// set password 
		env.put(Context.SECURITY_CREDENTIALS, new String(pwd)); 
	}

	/**
	 * Returns valid LDAP URL for the specified URL.
	 * @param url  the URL to convert to the valid URL
	 */
	public static String getValidURL(String url)
	{
		if (url != null && url.length() > 0) {
			// XXX really important that this one happens first!!
			url = url.replaceAll("[%]", "%25");

			url = url.replaceAll(" ", "%20");
			url = url.replaceAll("[<]", "%3c");
			url = url.replaceAll("[>]", "%3e");
			url = url.replaceAll("[\"]", "%3f");
			url = url.replaceAll("[#]", "%23");
			url = url.replaceAll("[{]", "%7b");
			url = url.replaceAll("[}]", "%7d");
			url = url.replaceAll("[|]", "%7c");
			url = url.replaceAll("[\\\\]", "%5c"); // double check this
															// one :-)
			url = url.replaceAll("[\\^]", "%5e");
			url = url.replaceAll("[~]", "%7e");
			url = url.replaceAll("[\\[]", "%5b");
			url = url.replaceAll("[\\]]", "%5d");
			url = url.replaceAll("[']", "%27");

			url = url.replaceAll("[?]", "%3f");
		}

		return url;

	}
}
