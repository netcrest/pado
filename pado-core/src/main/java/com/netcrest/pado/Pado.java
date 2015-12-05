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
package com.netcrest.pado;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.info.AppInfo;
import com.netcrest.pado.info.ConfigInfo;
import com.netcrest.pado.info.UserLoginInfo;
import com.netcrest.pado.info.message.MessageType;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.impl.PadoClientManager;
import com.netcrest.pado.internal.security.AESCipher;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.link.IPadoBizLink;

/**
 * The Pado class provides the first client entry point to connecting to a grid.
 * Before a client logs in to a grid, it must first invoke
 * {@link #connect(String, boolean, boolean)} once to connect to the grid. Upon
 * successful connection, the client then invokes
 * {@link #login(String, String, String, char[], String, String, char[])} to
 * login to the grid. The login method returns an instance of Pado which
 * provides an IBiz catalog specifically tailored to the specified app ID and
 * user name. A Pado instance is unique per user and maintained internally by
 * Pado to provide the session management service, which may be beneficial to
 * applications such as web containers.
 * <p>
 * The following code snippet shows a typical set of Pado client steps:
 * 
 * <pre>
 * 1. Pado.connect(locator, multiUserEnabled);
 * 2. IPado pado = Pado.login(appId, domain, userName, password);
 * 3. ICatalog catalog = pado.getCatalog();
 * 4. ITemporalBiz temporalBiz = catalog.newInstance(ITemporalBiz.class, "temporal");
 * 5. Object entity = temporalBiz.get(key, validAt, asOf);
 * </pre>
 * 
 * For a single user login, the user name and password can be specified in the
 * client configuration file using the following properties:
 * 
 * <pre>
 * security-appid=
 * security-domain=
 * security-username=
 * security-password=
 * </pre>
 * 
 * The password can be encrypted by running bin_sh/tools/encryptor.
 * <p>
 * A Pado instance should be logged out when it is no longer needed by invoking
 * {@link #logout()}. A user session is automatically logged out if it is idle
 * for more than 15 minutes by default. This default value can be changed from
 * the grid configuration file. It a Pado instance has been logged out, then it
 * is no longer valid. All IBiz objects created by the logged out Pado instance
 * along with the IBiz catalog also effectively become invalid upon logout.
 * Making calls to invalid IBiz objects will raise exceptions.
 * <p>
 * Pado should be closed when the connection to the grid is no longer needed by
 * invoking {@link #close} which first logs out any remaining Pado instances and
 * disconnects the grid connection. To reconnect, simply invoke
 * {@link #connect(String, boolean, boolean)} again.
 * <p>
 * 
 * @author dpark
 * 
 */
public abstract class Pado implements IPado
{
	private static Class padoClass;
	private static String locators;

	// <appId, ConfigInfo>
	protected static Map<String, ConfigInfo> s_configInfoMap = new HashMap(4);

	// <username, token>
	protected static Map<String, Object> s_userMap = new ConcurrentHashMap();

	// <token, Pado>
	protected static Map<Object, IPado> s_tokenMap = new ConcurrentHashMap();

	protected static PadoClientManager s_clientManager;

	static {
		try {
			padoClass = PadoUtil.getClass(Constants.PROP_CLASS_PADO, Constants.DEFAULT_CLASS_PADO);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * IPadoBiz that provides user session services.
	 */
	protected IPadoBizLink padoBiz;

	/**
	 * Catalog specific to the logged in user.
	 */
	protected ICatalog catalog;

	/**
	 * User login information.
	 */
	protected UserLoginInfo loginInfo;

	/**
	 * Message listeners for listening on messages published to clients by grids
	 */
	private List<IMessageListener> messageListenerList = new ArrayList<IMessageListener>(3);

	/**
	 * Session specific user data.
	 */
	private Object userData;

	/**
	 * Returns the AppInfo object for the specified app ID.
	 * 
	 * @param appId
	 *            App ID
	 */
	private AppInfo getAppInfo(String appId)
	{
		return s_clientManager.getAppInfo(appId);
	}

	/**
	 * Returns client configuration information which may be used to dynamically
	 * configure the client app.
	 * 
	 * @param appId
	 *            App ID
	 */
	protected synchronized ConfigInfo getConfigInfo(String appId)
	{
		return s_configInfoMap.get(appId);
	}

	/**
	 * Caches the specified ConfigInfo object.
	 * 
	 * @param appId
	 *            App ID
	 * @param configInfo
	 *            Client configuration information
	 */
	protected synchronized void putConfigInfo(String appId, ConfigInfo configInfo)
	{
		s_configInfoMap.put(appId, configInfo);
	}

	/**
	 * Returns true if the ConfigInfo object pertaining to the specified app ID
	 * exists in the cache.
	 * 
	 * @param appId
	 *            App ID
	 */
	protected synchronized boolean isConfigInfoExist(String appId)
	{
		return s_configInfoMap.containsKey(appId);
	}

	/**
	 * Returns the IPado instance pertaining to the specified user name. It
	 * returns null if the instance does not exist or the session has been
	 * expired. Note that this method resets the session expiration (idle)
	 * timeout.
	 * 
	 * @param username
	 *            User name
	 * @see IPado#resetSessionIdleTimeout()
	 */
	public static IPado getPado(String username)
	{
		if (username == null) {
			return null;
		}
		Object token = s_userMap.get(username);
		if (token == null) {
			return null;
		}
		return getPado(token);
	}

	/**
	 * Returns the IPado instance pertaining to the specified token. It returns
	 * null if the instance does not exist. Note that Pado automatically removes
	 * Pado instances (or sessions) that have expired. This method returns null
	 * if the token-mapping session has been expired. Note that this method
	 * resets the session expiration (idle) timeout.
	 * 
	 * @param token
	 *            Session token
	 * @see IPado#resetSessionIdleTimeout()
	 */
	public static IPado getPado(Object token)
	{
		if (token == null) {
			return null;
		}
		IPado pado = s_tokenMap.get(token);
		if (pado != null) {
			pado.resetSessionIdleTimeout();
		}
		return pado;
	}

	/**
	 * Caches the specified IPado instance pertaining to the specified user
	 * name.
	 * 
	 * @param username
	 *            User name
	 * @param pado
	 *            IPado instance
	 */
	protected static void putPado(String username, IPado pado)
	{
		if (username == null || pado == null) {
			return;
		}
		s_userMap.put(username, pado.getToken());
		s_tokenMap.put(pado.getToken(), pado);
	}

	/**
	 * Logs in to Pado using the login info found in the security properties
	 * file.
	 * 
	 * @return Returns an instance of Pado for the login account.
	 */
	public static IPado login() throws PadoException, PadoLoginException
	{
		return login(null, null, null, null);
	}

	/**
	 * Logs in to Pado using the specified login account info. If encryption is
	 * enabled then it picks up the public key information from the security
	 * properties file. Note that the specified login account info overrides the
	 * security properties file.
	 * 
	 * @param appId
	 *            App ID
	 * @param domain
	 *            Optional domain name
	 * @param username
	 *            User name
	 * @param password
	 *            Password
	 * @return Returns an instance of Pado for the login account.
	 * @throws PadoException
	 *             Thrown if a connection error occurs
	 * @throws PadoLoginException
	 *             Thrown if invalid login occurs
	 */
	public static IPado login(String appId, String domain, String username, char[] password) throws PadoException,
			PadoLoginException
	{
		return login(appId, domain, username, password, null, null, null);
	}

	/**
	 * Logs in to Pado using the specified login account and public key info,
	 * which override the security properties file.
	 * 
	 * @param appId
	 *            App ID
	 * @param domain
	 *            Optional domain name
	 * @param username
	 *            User name
	 * @param password
	 *            Password
	 * @param keystoreFilePath
	 *            Key store file path
	 * @param keystoreAlias
	 *            Key store alias
	 * @param keystorePassword
	 *            Key store password
	 * @return IPdo instance representing a user session if login is successufl
	 * @throws PadoException
	 *             Thrown if grid communications fail
	 * @throws PadoLoginException
	 *             Thrown if login fails
	 */
	@SuppressWarnings("unchecked")
	public static IPado login(String appId, String domain, String username, char[] password, String keystoreFilePath,
			String keystoreAlias, char[] keystorePassword) throws PadoException, PadoLoginException
	{
		try {
			if (username == null) {
				if (appId == null) {
					appId = PadoUtil.getProperty(Constants.PROP_SECURITY_CLIENT_APPID);
					if (appId == null) {
						appId = "sys";
					}
				}
				if (domain == null) {
					domain = PadoUtil.getProperty(Constants.PROP_SECURITY_CLIENT_DOMAIN);
				}
				if (username == null) {
					username = PadoUtil.getProperty(Constants.PROP_SECURITY_CLIENT_USER);
					if (username == null) {
						username = System.getProperty("user.name");
					}
				}
				if (password == null) {
					String epw = PadoUtil.getProperty(Constants.PROP_SECURITY_CLIENT_PASS);
					if (epw != null) {
						try {
							String pw = AESCipher.decryptUserTextToText(epw);
							password = pw.toCharArray();
						} catch (Exception ex) {
							throw new PadoLoginException(
									"Error occurred while accessing encrypted password from pado.properties.", ex);
						}
					}
				}
			}
			Method method = padoClass.getMethod("login", String.class, String.class, String.class, char[].class,
					String.class, String.class, char[].class);
			return (Pado) method.invoke(null, appId, domain, username, password, keystoreFilePath, keystoreAlias,
					keystorePassword);
		} catch (Exception ex) {
			throw new PadoException("Pado login error", ex);
		}
	}

	/**
	 * Logs out the specified user from all apps and domains.
	 * 
	 * @param username
	 *            User name
	 */
	public static void logoutUser(String username)
	{
		Object token = s_userMap.get(username);
		logoutToken(token);
	}

	/**
	 * Logs out the specified session token from all apps and domains.
	 * 
	 * @param tokne
	 *            Session token
	 */
	public static void logoutToken(Object token)
	{
		IPado pado = s_tokenMap.remove(token);
		if (pado == null) {
			return;
		}
		s_userMap.remove(pado.getUsername());
		pado.logout();
	}

	/**
	 * Returns all IPado instances.
	 */
	public static Collection<IPado> getAllPados()
	{
		return Collections.unmodifiableCollection(s_tokenMap.values());
	}

	/**
	 * Returns the user name.
	 */
	public String getUsername()
	{
		return loginInfo.getUsername();
	}

	/**
	 * Logs out the user from this particular instance of login session.
	 */
	public void logout()
	{
		// TODO: need better logout logic. send a message to the pado grid
		s_userMap.remove(this.loginInfo.getUsername());
		s_tokenMap.remove(this.loginInfo.getToken());
		loginInfo = null;
		catalog.close();
		catalog = null;
		userData = null;
	}

	@Override
	public boolean isLoggedOut()
	{
		return loginInfo == null;
	}

	/**
	 * Connects the Pado grid with using the value of the Pado property
	 * "security.client.locators". If that value is not set (null) then it
	 * connects to the default locators "localhost:20000". This method call is
	 * equivalent to connect(null, false, false).
	 */
	public static void connect()
	{
		connect(null, false, false);
	}

	/**
	 * Connects to the Pado grid specified by locators for a client that hosts a
	 * single application, i.e., a single app ID throughout life of client. This
	 * call is analogous to passing false to multiAppsEnabled to
	 * {@link #connect(String, boolean, boolean)}
	 * 
	 * @param locators
	 *            Pado locators. Format:
	 *            host1:port1@server-group,host2:port2@server-group,... If this
	 *            argument null, then it assigns the value of the Pado property
	 *            "security.client.locators". If it is still null, then it
	 *            assigns "localhost:20000".
	 * @param multiuserAuthenticationEnabled
	 *            true to enable multi-user authentication.
	 */
	public static void connect(String locators, boolean multiuserAuthenticationEnabled)
	{
		connect(locators, multiuserAuthenticationEnabled, false);
	}

	/**
	 * Connects to the Pado grid specified by locators.
	 * 
	 * @param locators
	 *            Pado locators. Format:
	 *            host1:port1@server-group,host2:port2@server-group,... If this
	 *            argument null, then it assigns the value of the Pado property
	 *            "security.client.locators". If it is still null, then it
	 *            assigns "localhost:20000".
	 * @param multiuserAuthenticationEnabled
	 *            true to enable multi-user authentication.
	 * @param multiAppsEnabled
	 *            If true then the client is expected to host multiple
	 *            applications, i.e., multiple app IDs. Clients such as app
	 *            servers that host many applications typically set this to true
	 *            to allow multiple applications to connect to Pado.
	 */
	public static void connect(String locators, boolean multiuserAuthenticationEnabled, boolean multiAppsEnabled)
	{
		try {
			String padoHome = System.getenv("PADO_HOME");
			if (padoHome == null) {
				padoHome = PadoUtil.getProperty(Constants.PROP_HOME_DIR, System.getProperty("user.dir"));
			}
			PadoUtil.getPadoProperties().setProperty(Constants.PROP_HOME_DIR, padoHome);

			if (locators == null) {
				locators = PadoUtil.getProperty(Constants.PROP_SECURITY_CLIENT_LOCATORS);
				if (locators == null) {
					locators = "localhost:20000";
				}
			}
			Method method = padoClass.getMethod("connect", String.class, boolean.class, boolean.class);
			method.invoke(null, locators, multiuserAuthenticationEnabled, multiAppsEnabled);
			Pado.locators = locators;
		} catch (Exception ex) {
			throw new PadoException("Pado connection error", ex);
		}
	}

	/**
	 * Returns the locators that it is currently connected to. It returns null
	 * it is not connected.
	 */
	public static String getLocators()
	{
		return locators;
	}

	/**
	 * Resets the entire client-side Pado by removing all users and
	 * configuration information.
	 */
	protected static void reset()
	{
		if (isClosed() == false) {
			Collection<IPado> col = s_tokenMap.values();
			for (IPado pado : col) {
				((Pado) pado).resetUser();
			}
		}
		s_userMap.clear();
		s_tokenMap.clear();
		if (s_clientManager != null) {
			s_clientManager.clear();
		}
		s_configInfoMap.clear();
	}

	protected void resetUser()
	{
		logout();
	}

	/**
	 * Closes Pado. After this method call, Pado is no longer connected to the
	 * grid and all user sessions are invalid. Pado can be reconnected by
	 * invoking a connect() method.
	 * 
	 * @see #connect(String, boolean)
	 * @see #connect(String, boolean, boolean)
	 */
	public static void close()
	{
		reset();
		try {
			Method method = padoClass.getMethod("close");
			method.invoke(null);
			locators = null;
		} catch (Exception ex) {
			throw new PadoException("Pado close() error", ex);
		}
	}

	/**
	 * Returns true if Pado is closed. Pado is closed if a connect() method is
	 * never invoked or the {@link #close()} method has been invoked but a
	 * connect() method has not been invoked thereafter. Pado can be reconnected
	 * without restarting the client app by invoking a connect() method.
	 */
	public static boolean isClosed()
	{
		try {
			Method method = padoClass.getMethod("isClosed");
			return (Boolean) method.invoke(null);
		} catch (Exception ex) {
			throw new PadoException("Pado isClosed() error", ex);
		}
	}

	/**
	 * Returns the catalog pertaining to this Pado session.
	 */
	public ICatalog getCatalog()
	{
		if (isLoggedOut()) {
			return null;
		}
		return catalog;
	}

	/**
	 * Returns the session token. It returns null if the Pado session has been
	 * terminated due to logout or expiration.
	 */
	public Object getToken()
	{
		if (isLoggedOut()) {
			return null;
		}
		return loginInfo.getToken();
	}

	/**
	 * Returns the private key obtained from the grid upon successful login. The
	 * private key can be used to encrypt data before sending it to the grid.
	 */
	public byte[] getPrivateKey()
	{
		return loginInfo.getPrivateKey();
	}

	/**
	 * Returns the app ID pertaining to this Pado session.
	 */
	@Override
	public String getAppId()
	{
		if (catalog == null) {
			return null;
		}
		return catalog.getAppId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getGridId()
	{
		if (padoBiz == null) {
			return null;
		}
		return padoBiz.getGridInfo().getGridId();
	}

	/**
	 * Fires the specified a message event containing the specified message type
	 * and message to all message listeners.
	 * 
	 * @param messageType
	 *            Message type
	 * @param message
	 *            Message
	 */
	public void fireMessageEvent(MessageType messageType, Object message)
	{
		for (IMessageListener messageListener : messageListenerList) {
			messageListener.messageReceived(messageType, message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addMessageListener(IMessageListener listener)
	{
		if (messageListenerList.contains(listener) == false) {
			messageListenerList.add(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeMessageListener(IMessageListener listener)
	{
		messageListenerList.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUserData(Object userData)
	{
		this.userData = userData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getUserData()
	{
		return this.userData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void refresh();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract boolean isVirtualPath(String virtualPath);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void resetSessionIdleTimeout();
}
