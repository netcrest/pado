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
package com.netcrest.pado.security;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.AuthenticationException;

import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.security.AESCipher;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

public class RSACipher
{
	// client
	private static String keystoreAlias;
	private static String keystorePasswordEncrypted;
	private static String keystoreFilePath;

	// server
	private String pubKeyFilePath;
	private String pkpEncrypted;

	// Contains public certificates <alias, Certificate> read
	// from the pubKeyFilePath file. See populateMap.
	private Map<Object, Certificate> aliasCertificateMap;

	/**
	 * Constructs an RSACipher object that provides RSA certificate signature
	 * verification services.
	 * 
	 * @param readAliases
	 *            If true then reads certificate aliases from the public key
	 *            store specified by
	 *            {@link Constants#PROP_SECURITY_AES_PUBLICKEY_FILEPATH} and
	 *            {@link Constants#PROP_SECURITY_AES_PUBLICKEY_PASS}. Both
	 *            properties are required. This argument should be true only if
	 *            the caller, i.e., server, performs client certificate
	 *            verifications, i.e., invokes
	 *            {@link #verifySignature(Properties)}.
	 */
	public RSACipher(boolean readAliases) throws AuthenticationException
	{
		if (readAliases) {
			this.pubKeyFilePath = PadoUtil.getProperty(Constants.PROP_SECURITY_AES_PUBLICKEY_FILEPATH,
					Constants.DEFAULT_SECURITY_AES_PUBLICKEY_FILEPATH);
			this.pkpEncrypted = PadoUtil.getProperty(Constants.PROP_SECURITY_AES_PUBLICKEY_PASS);

			this.aliasCertificateMap = new HashMap<Object, Certificate>();
			populateMap();
		}
	}

	/**
	 * Populates {@link #aliasCertificateMap} with public certificates read from
	 * the {@link #pubKeyFilePath} file.
	 */
	private void populateMap() throws AuthenticationException
	{
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			String pkp = AESCipher.decryptUserTextToText(pkpEncrypted);
			char[] passPhrase = (pkp != null ? pkp.toCharArray() : null);
			FileInputStream keystorefile = new FileInputStream(this.pubKeyFilePath);
			try {
				ks.load(keystorefile, passPhrase);
			} catch (Exception ex) {
				Logger.severe(ex);
			} finally {
				keystorefile.close();
			}
			Enumeration e = ks.aliases();
			while (e.hasMoreElements()) {
				Object alias = e.nextElement();
				Certificate cert = ks.getCertificate((String) alias);
				if (cert instanceof X509Certificate) {
					this.aliasCertificateMap.put(alias, cert);
				}
			}
		} catch (Exception e) {
			throw new AuthenticationException("Exception while getting public keys: " + e.getMessage());
		}
	}

	/**
	 * Returns the digital signature in a newly created properties object. This
	 * method should be invoked by clients that need to authenticate against
	 * Pado. The digital signature is constructed by reading the client's key
	 * store which contains the private and public key pairs. Prior to
	 * connecting to Pado, the public key must be first imported in Pado.
	 * 
	 * @param props
	 *            Properties of
	 *            {@link AuthInitialize#getCredentials(Properties, com.gemstone.gemfire.distributed.DistributedMember, boolean)}
	 *            . The following properties are expected:
	 *            <ul>
	 *            <li>{@link #KEYSTORE_FILE_PATH}</li>
	 *            <li>{@link #KEYSTORE_ALIAS}</li>
	 *            <li>{@link #KEYSTORE_PASSWORD}</li>
	 *            </ul>
	 * @return A new properties object with the following properties:
	 *         <ul>
	 *         <li>{@link #KEYSTORE_ALIAS}</li>
	 *         <li>{@link #SIGNATURE_DATA}</li>
	 *         </ul>
	 * @throws Exception
	 */
	public Properties getSignature(Properties props) throws Exception
	{
		// TODO: see if GemStone would lift String only properties restriction
		// signature is binary. non-string properties are inconsistently
		// supported
		// if (true) {
		// credentials = props;
		// return props;
		// }

		String keystorePath = props.getProperty(Constants.PROP_SECURITY_KEYSTORE_FILE_PATH);
		if (keystorePath == null) {
			keystorePath = keystoreFilePath;
		}
		String padoHome = PadoUtil.getProperty(Constants.PROP_HOME_DIR);
		if (keystorePath.startsWith("/") == false) {
			keystorePath = padoHome + "/" + keystorePath;
		}
		String alias = props.getProperty(Constants.PROP_SECURITY_KEYSTORE_ALIAS);
		if (alias == null) {
			throw new AuthenticationException(this.getClass().getSimpleName() + ": key alias name property ["
					+ Constants.PROP_SECURITY_KEYSTORE_ALIAS + "] not set.");
		}
		keystorePasswordEncrypted = props.getProperty(Constants.PROP_SECURITY_KEYSTORE_PASS);
		String keystorePass = AESCipher.decryptUserTextToText(keystorePasswordEncrypted);

		KeyStore ks = KeyStore.getInstance("PKCS12");
		char[] passPhrase = (keystorePass != null ? keystorePass.toCharArray() : null);

		InputStream certificateInputStream = null;
		try {
			certificateInputStream = new FileInputStream(keystorePath);
			ks.load(certificateInputStream, passPhrase);
		} finally {
			if (certificateInputStream != null) {
				certificateInputStream.close();
			}
		}

		Key key = ks.getKey(alias, passPhrase);

		if (key instanceof PrivateKey) {

			PrivateKey privKey = (PrivateKey) key;
			X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
			Signature sig = Signature.getInstance(cert.getSigAlgName());

			sig.initSign(privKey);
			sig.update(alias.getBytes("UTF-8"));
			byte[] signatureBytes = sig.sign();

			Properties newprops = new Properties();
			newprops.put(Constants.PROP_SECURITY_KEYSTORE_ALIAS, alias);
			// TODO: GemFire does not support non-string. Well, sometimes it
			// does.
			// For pool it doesn't. What gives? binary for now but may need to
			// change
			newprops.put(Constants.PROP_SECURITY_SIGNATURE_DATA, signatureBytes);
			// String appId = props.getProperty(APPID);
			// if (appId != null) {
			// newprops.put(APPID, appId);
			// }
			// String domain = props.getProperty(DOMAIN);
			// if (domain != null) {
			// newprops.put(DOMAIN, domain);
			// }
			// if (props.getProperty(USER_NAME) != null) {
			// newprops.put(USER_NAME, props.getProperty(USER_NAME));
			// }
			// if (props.getProperty(PASSWORD) != null) {
			// String epw = props.getProperty(PASSWORD);
			// String pw = AESCipher.decryptUserTextToText(epw);
			// newprops.put(PASSWORD, pw);
			// }

			// Use the passed-in properties as default for subsequent logins
			keystoreFilePath = keystorePath;
			keystoreAlias = alias;

			return newprops;
		} else {
			throw new AuthenticationException("PKCSAuthInit: " + "Failed to load private key from the given file: "
					+ keystorePath);
		}
	}

	/**
	 * Returns credential properties with encrypted password.
	 * 
	 * @param appId
	 *            App ID
	 * @param domain
	 *            Domain name (optional)
	 * @param username
	 *            User name
	 * @param password
	 *            Clear password
	 * @param keystoreFilePath
	 *            Key store file path
	 * @param keystoreAlias
	 *            Key store alias
	 * @param keystorePassword
	 *            Key store password
	 * @return Credential properties required by the server to authenticate the
	 * @throws Exception
	 *             Thrown if the required crendential information is not
	 *             provided or encryption fails.
	 */
	public static Properties createCredentialProperties(String appId, String domain, String username, char[] password,
			String keystoreFilePath, String keystoreAlias, char[] keystorePassword) throws Exception
	{
		Properties props = new Properties();
		if (keystoreFilePath == null) {
			keystoreFilePath = PadoUtil.getProperty(Constants.PROP_SECURITY_KEYSTORE_FILE_PATH);
		}
		if (keystoreAlias == null) {
			keystoreAlias = PadoUtil.getProperty(Constants.PROP_SECURITY_KEYSTORE_ALIAS);
		}
		String kpw;
		if (keystorePassword == null) {
			kpw = PadoUtil.getProperty(Constants.PROP_SECURITY_KEYSTORE_PASS);
		} else {
			kpw = new String(keystorePassword);
			kpw = AESCipher.encryptUserTextToText(kpw);
		}

		if (keystoreFilePath == null) {
			throw new SecurityException("Keystore file path undefined: " + Constants.PROP_SECURITY_KEYSTORE_FILE_PATH);
		}
		if (keystoreAlias == null) {
			throw new SecurityException("Keystore alias undefined: " + Constants.PROP_SECURITY_KEYSTORE_ALIAS);
		}
		if (kpw == null) {
			throw new SecurityException("Keystore password undefined: " + Constants.PROP_SECURITY_KEYSTORE_PASS);
		}

		props.put(Constants.PROP_SECURITY_KEYSTORE_FILE_PATH, keystoreFilePath);
		props.put(Constants.PROP_SECURITY_KEYSTORE_ALIAS, keystoreAlias);
		props.put(Constants.PROP_SECURITY_KEYSTORE_PASS, kpw);

		return props;
	}

	public static Properties createCredentialProperties(Object token)
	{
		Properties props = new Properties();
		props.put(Constants.PROP_SECURITY_TOKEN, token);
		if (keystoreFilePath != null) {
			props.put(Constants.PROP_SECURITY_KEYSTORE_FILE_PATH, keystoreFilePath);
		}
		if (keystoreAlias != null) {
			props.put(Constants.PROP_SECURITY_KEYSTORE_ALIAS, keystoreAlias);
		}
		if (keystorePasswordEncrypted != null) {
			props.put(Constants.PROP_SECURITY_KEYSTORE_PASS, keystorePasswordEncrypted);
		}
		return props;
	}

	/**
	 * Creates and returns a new credential properties object that contains
	 * security properties extracted by invoking
	 * {@link PadoUtil#getProperty(String)}.
	 */
	public static Properties createCredentialProperties()
	{
		return createCredentialProperties(new Properties());
	}

	/**
	 * Fills and returns the specified properties with security properties
	 * extracted by invoking {@link PadoUtil#getProperty(String)}.
	 */
	private static Properties createCredentialProperties(Properties props)
	{
		String keystoreFilePath = PadoUtil.getProperty(Constants.PROP_SECURITY_KEYSTORE_FILE_PATH);
		String keystoreAlias = PadoUtil.getProperty(Constants.PROP_SECURITY_KEYSTORE_ALIAS);
		String kpw = PadoUtil.getProperty(Constants.PROP_SECURITY_KEYSTORE_PASS);

		if (keystoreFilePath == null) {
			throw new SecurityException("Keystore file path undefined: " + Constants.PROP_SECURITY_KEYSTORE_FILE_PATH);
		}
		if (keystoreAlias == null) {
			throw new SecurityException("Keystore alias undefined: " + Constants.PROP_SECURITY_KEYSTORE_ALIAS);
		}
		if (kpw == null) {
			throw new SecurityException("Keystore password undefined: " + Constants.PROP_SECURITY_KEYSTORE_PASS);
		}

		props.put(Constants.PROP_SECURITY_KEYSTORE_FILE_PATH, keystoreFilePath);
		props.put(Constants.PROP_SECURITY_KEYSTORE_ALIAS, keystoreAlias);
		props.put(Constants.PROP_SECURITY_KEYSTORE_PASS, kpw);
		return props;
	}

	private AuthenticationException getException(String exStr, Exception cause)
	{

		String exMsg = "PKCSAuthenticator: Authentication of client failed due to: " + exStr;
		if (cause != null) {
			return new AuthenticationException(exMsg + " " + cause.getMessage());
		} else {
			return new AuthenticationException(exMsg);
		}
	}

	private AuthenticationException getException(String exStr)
	{
		return getException(exStr, null);
	}

	private X509Certificate getCertificate(String alias) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		if (this.aliasCertificateMap.containsKey(alias)) {
			return (X509Certificate) this.aliasCertificateMap.get(alias);
		}
		return null;
	}

	/**
	 * Verifies the digital signature ({@link #SIGNATURE_DATA} found in the
	 * specified properties. The digital signature is sent by a client, which
	 * invokes {@link #getSignature(Properties)} to create one during
	 * authentication.
	 * 
	 * @param props
	 *            The properties object received from the client during
	 *            authentication. The following properties are expected:
	 *            <ul>
	 *            <li>{@link #KEYSTORE_ALIAS}</li>
	 *            <li>{@link #SIGNATURE_DATA}</li>
	 *            </ul>
	 * @return Returns true if digital signature verification is successful,
	 *         false otherwise. If false, the caller must reject authentication.
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public boolean verifySignature(Properties props) throws NoSuchAlgorithmException, InvalidKeySpecException,
			InvalidKeyException, SignatureException, UnsupportedEncodingException, AuthenticationException
	{
		// TODO: see if GemStone would lift String only properties restriction
		// signature is binary. non-string properties are inconsistently
		// supported
		// if (true) {
		// return true;
		// }
		//

		String alias = (String) props.get(Constants.PROP_SECURITY_KEYSTORE_ALIAS);
		if (alias == null || alias.length() <= 0) {
			throw new AuthenticationException("No alias received");
		}
		X509Certificate cert = getCertificate(alias);
		if (cert == null) {
			throw getException("No certificate found for alias:" + alias);
		}
		byte[] signatureBytes = (byte[]) props.get(Constants.PROP_SECURITY_SIGNATURE_DATA);
		if (signatureBytes == null) {
			throw getException("signature data property [" + Constants.PROP_SECURITY_SIGNATURE_DATA + "] not provided");
		}
		Signature sig = Signature.getInstance(cert.getSigAlgName());
		sig.initVerify(cert);
		sig.update(alias.getBytes("UTF-8"));
		return sig.verify(signatureBytes);
	}

	public byte[] encrypt(X509Certificate certificate, byte[] data) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());
		return cipher.doFinal(data);
	}

	public byte[] decrypt(PrivateKey privateKey, byte[] data) throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		Cipher cipher = Cipher.getInstance("RSA");
		byte[] encryptedData = new byte[100];
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(encryptedData);
	}
}
