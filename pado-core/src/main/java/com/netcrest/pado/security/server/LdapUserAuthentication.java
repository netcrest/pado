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
package com.netcrest.pado.security.server;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import com.netcrest.pado.IUserAuthentication;
import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.factory.LdapFactory;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.security.RSACipher;

/**
 * LdapUserAuthentication is a simple server-side LDAP authentication that
 * connects to an LDAP server, authenticates the user, and extracts user role
 * information. The following Pado properties must be specified preferable in
 * <i>pado.properities</i>:
 * <ul>
 * <li>security.ldap.url - LDAP URL in the form of
 * ldaps://&lt;host&gt;:<&lt;port&gt;</li>
 * <li>security.ldap.base - LDAP base name</li>
 * <li>security.ldap.user.filter - User filter for a given user name. Default:
 * (&(objectClass=inetOrgPerson)(uid={0}))</li>
 * <li>security.ldap.memberof.base - LDAP member-of base name</li>
 * <li>security.ldap.memberof.filter - Member-of filter for a given member.
 * Default: (&(objectClass=groupOfNames)(member={0}))</li>
 * </ul>
 * <b>Example:</b>
 * 
 * <pre>
 * security.ldap.url=ldaps://localhost:10636
 * security.ldap.base=dc=newco,dc=com
 * security.ldap.user.filter=(&(objectClass=inetOrgPerson)(uid={0}))
 * security.ldap.memberof.base=ou=groups,dc=newco,dc=com
 * security.ldap.memberof.filter=(&(objectClass=groupOfNames)(member={0}))
 * </pre>
 * <p>
 * To enable this class, it must be specified as a Pado property preferably in
 * <i>pado.properties</i> as follows:
 * 
 * <pre>
 * class.userAuthentication=com.netcrest.pado.security.server.LdapUserAuthentication
 * </pre>
 * 
 * @author dpark
 * 
 */
public class LdapUserAuthentication implements IUserAuthentication
{
	private RSACipher cipher;
	private String ldapUrl;
	private String base;
	private String userFilter;
	private String memberOfBase;
	private String memberOfFilter;

	public LdapUserAuthentication() throws AuthenticationException
	{
		if (PadoUtil.isProperty(Constants.PROP_SECURITY_ENABLED)) {
			if (PadoUtil.getProperty(Constants.PROP_SECURITY_AES_PUBLICKEY_PASS) == null) {
				Logger.severe("SSL/RSA error. The public key password not supplied in pado.properties: "
						+ Constants.PROP_SECURITY_AES_PUBLICKEY_PASS + ". System aborted.");
				System.exit(-1);
			} else {
				cipher = new RSACipher(true);
			}
		}

		ldapUrl = PadoUtil.getProperty("security.ldap.url", "ldaps://localhost:10636");
		base = PadoUtil.getProperty("security.ldap.base", "dc=newco,dc=com");
		userFilter = PadoUtil.getProperty("security.ldap.user.filter", "(&(objectClass=inetOrgPerson)(uid={0}))");
		memberOfBase = PadoUtil.getProperty("security.ldap.memberof.base", "ou=groups,ou=Pado,dc=newco,dc=com");
		memberOfFilter = PadoUtil.getProperty("security.ldap.memberof.filter",
				"(&(objectClass=groupOfNames)(member={0}))");
		Logger.config("LdapUserAuthentication plugin installed: [URL=" + ldapUrl + ", base=" + base + "userFilter="
				+ userFilter + ", memberOfBase=" + memberOfBase + ", memberOfFilter=" + memberOfFilter + "]");

		// See if LDAP connection can be established. Log appropriate messages.
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, ldapUrl);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			DirContext ctx = new InitialDirContext(env);
			Logger.config("LDAP server connection successful: " + ctx);
			ctx.close();
			
		} catch (Exception ex) {
			Logger.severe("Unable to establish connection to the LDAP server: [" + Context.PROVIDER_URL + "=" + ldapUrl + "]", ex);
		}

	}

	private AuthenticationException createAuthenticationException(Exception ex)
	{
		String exMsg = this.getClass().getSimpleName() + ": Authentication of client failed due to "
				+ ex.getClass().getSimpleName();
		if (ex.getMessage() != null) {
			exMsg += ": " + ex.getMessage();
		}
		return new AuthenticationException(exMsg);
	}

	private AuthenticationException createAuthenticationException(String exStr)
	{
		String exMsg = this.getClass().getSimpleName() + ": " + exStr;
		return new AuthenticationException(exMsg);
	}

	@SuppressWarnings("rawtypes")
	public IUserPrincipal authenticate(String appId, String domain, String username, char[] password, Properties props)
			throws AuthenticationException
	{
		if (username == null) {
			throw new AuthenticationException("User name cannot be null");
		}

		if (cipher != null) {
			try {
				if (props == null) {
					throw createAuthenticationException("Security properties undefined. Make sure the client has "
							+ Constants.PROP_SECURITY_ENABLED + "=true and security properties defined.");
				} else if (cipher.verifySignature(props) == false) {
					throw createAuthenticationException("Verification of client signature failed");
				}
			} catch (Exception ex) {
				throw createAuthenticationException(ex);
			}
		}

		Hashtable<String, String> env = new Hashtable<String, String>();

		try {
			// Step 1: Bind anonymously to see if the dn exists

			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, ldapUrl);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			DirContext ctx = new InitialDirContext(env);

			// Step 2: Search the directory for the user DN
			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setReturningAttributes(new String[0]);
			ctls.setReturningObjFlag(true);
			NamingEnumeration enm = ctx.search(base, userFilter, new String[] { username }, ctls);

			String dn = null;

			if (enm.hasMore()) {
				SearchResult result = (SearchResult) enm.next();
				dn = result.getNameInNamespace();
			}

			if (dn == null || enm.hasMore()) {
				// uid not found or not unique
				throw new AuthenticationException("Authentication failed. User does not exist: username=" + username);
			}

			// Step 3: Bind with found DN and given password

			ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
			if (password != null) {
				String pw = new String(password);
				ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, pw);
			}
			// Perform a lookup in order to force a bind operation with JNDI
			Object obj = ctx.lookup(dn);
			if (obj instanceof LdapContext) {
				LdapContext userContext = (LdapContext) obj;
			}
			enm.close();

			// Setp 4: Build memberOf. memberOf is not available in ApacheDS.
			// Need to iterate.
			enm = ctx.search(memberOfBase, memberOfFilter, new String[] { dn }, ctls);
			HashSet<String> memberOfSet = new HashSet<String>(3);
			if (enm.hasMore()) {
				SearchResult result = (SearchResult) enm.next();
				String name = result.getName();
				String split[] = name.split("=");
				String group;
				if (split.length == 2 && split[0].equalsIgnoreCase("cn")) {
					group = split[1];
					memberOfSet.add(group);
				}
			} else {
				throw new AuthenticationException("User does not belong to a valid group: " + username);
			}
			ctx.close();
			return LdapFactory.getLdapFactory().createUserPrincipal(domain, username, dn, memberOfSet);

		} catch (Exception ex) {
			AuthenticationException ae = createAuthenticationException(ex);
			Logger.warning("LDAP Authentication error: " + ex);
			throw ae;
		}
	}
}
