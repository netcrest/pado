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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

public class AESCipher
{
	private static byte[] privateKeyEncrypted;
	private static byte[] userPrivateKeyEncrypted;

	static {
		// Get the KeyGenerator
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			int keySize;
			try {
				keySize = Integer.parseInt(PadoUtil.getProperty(Constants.PROP_SECURITY_AES_KEY_SIZE));
			} catch (Exception ex) {
				keySize = 128;
			}
			kgen.init(keySize); // 192 and 256 bits may not be available

			// Generate the secret key specs.
			SecretKey skey = kgen.generateKey();
			privateKeyEncrypted = skey.getEncoded();
			userPrivateKeyEncrypted = getUserPrivateKey();
		} catch (Exception ex) {
			Logger.error("AESCipher init error", ex);
		}
	}

	public static void setPrivateKey(byte[] privateKey)
	{
		privateKeyEncrypted = privateKey;
	}

	public static void setUserPrivateKey(byte[] userPrivateKey)
	{
		userPrivateKeyEncrypted = userPrivateKey;
	}

	/**
	 * Turns array of bytes into string
	 * 
	 * @param buf
	 *            Array of bytes to convert to hex string
	 * @return Generated hex string
	 */
	public static String asHex(byte buf[])
	{
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
		int i;

		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)
				strbuf.append("0");

			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}
		return strbuf.toString();
	}

	public static byte[] getPrivateKey()
	{
		return privateKeyEncrypted;
	}

	public static byte[] encryptBinaryToBinary(byte[] clearBinary) throws Exception
	{
		return encryptBinaryToBinary(privateKeyEncrypted, clearBinary);
	}

	public static byte[] encryptTextToBinary(String clearText) throws Exception
	{
		return encryptTextToBinary(privateKeyEncrypted, clearText);
	}

	public static byte[] encryptUserTextToBinary(String clearText) throws Exception
	{
		return encryptTextToBinary(userPrivateKeyEncrypted, clearText);
	}

	public static String encryptUserTextToText(String clearText) throws Exception
	{
		if (clearText == null) {
			return null;
		}
		byte[] encrypted = AESCipher.encryptUserTextToBinary(clearText);
		Base64 base64 = new Base64(0); // no line breaks
		return base64.encodeAsString(encrypted);
	}

	public static String encryptUserTextToHex(String clearText) throws Exception
	{
		byte[] buf = encryptUserTextToBinary(clearText);
		String hexEncrypted = Hex.encodeHexString(buf);
		return hexEncrypted.toUpperCase();
	}

	public static String decryptUserHexToText(String hexStr) throws Exception
	{
		byte[] buf = Hex.decodeHex(hexStr.toCharArray());
		return decryptUserBinaryToText(buf);
	}

	private static byte[] encryptBinaryToBinary(byte[] privateKeyEncrypted, byte[] clearBinary) throws Exception
	{
		SecretKeySpec skeySpec = new SecretKeySpec(privateKeyEncrypted, "AES");

		// Instantiate the cipher
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clearBinary);
		return encrypted;
	}

	private static byte[] encryptTextToBinary(byte[] privateKeyEncrypted, String clearText) throws Exception
	{
		return encryptBinaryToBinary(privateKeyEncrypted, clearText.getBytes());
	}

	public static String decryptUserTextToText(String encryptedText) throws Exception
	{
		if (encryptedText == null) {
			return null;
		}
		Base64 base64 = new Base64(0); // no line breaks
		byte[] e = base64.decode(encryptedText);
		return decryptUserBinaryToText(e);
	}

	public static String encryptTextToHex(String clearText) throws Exception
	{
		byte[] buf = encryptTextToBinary(clearText);
		String hexEncrypted = Hex.encodeHexString(buf);
		return hexEncrypted.toUpperCase();
	}

	public static String decryptHexToText(String hexStr) throws Exception
	{
		byte[] buf = Hex.decodeHex(hexStr.toCharArray());
		return decryptBinaryToText(buf);
	}

	public static String decryptBinaryToText(byte[] encrypted) throws Exception
	{
		return decryptBinaryToText(privateKeyEncrypted, encrypted);
	}

	public static String decryptUserBinaryToText(byte[] encrypted) throws Exception
	{
		return decryptBinaryToText(userPrivateKeyEncrypted, encrypted);
	}

	private static byte[] decryptBinaryToBinary(byte[] pke, byte[] encrypted) throws Exception
	{
		SecretKeySpec skeySpec = new SecretKeySpec(pke, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		return cipher.doFinal(encrypted);
	}

	private static String decryptBinaryToText(byte[] pke, byte[] encrypted) throws Exception
	{
		byte[] original = decryptBinaryToBinary(pke, encrypted);
		String originalString = new String(original);
		return originalString;
	}

	private static byte[] getUserPrivateKey() throws IOException
	{
		byte[] privateKey = null;

		String estr;
		String certificateFilePath = PadoUtil.getProperty(Constants.PROP_SECURITY_AES_USER_CERTIFICATE, "security/user.cer");
		if (certificateFilePath.startsWith("/") == false) {
			
			// TODO: Make server files relative to PADO_HOME also.
			if (PadoUtil.isPureClient()) {
				String padoHome = PadoUtil.getProperty(Constants.PROP_HOME_DIR);
				certificateFilePath = padoHome + "/" + certificateFilePath;
			}
		}
		File file = new File(certificateFilePath);
		if (file.exists() == false) {
			FileWriter writer = null;
			try {
				privateKey = AESCipher.getPrivateKey();
				Base64 base64 = new Base64(0); // no line breaks
				estr = base64.encodeToString(privateKey);
				writer = new FileWriter(file);
				writer.write(estr);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		} else {
			FileReader reader = null;
			try {
				reader = new FileReader(file);
				StringBuffer buffer = new StringBuffer(2048);
				int c;
				while ((c = reader.read()) != -1) {
					buffer.append((char) c);
				}
				estr = buffer.toString();
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		}
		Base64 base64 = new Base64(0); // no line breaks
		privateKey = base64.decode(estr);
		return privateKey;
	}

	public static byte[] decryptBinaryToBinary(byte[] encrypted) throws Exception
	{
		return decryptBinaryToBinary(privateKeyEncrypted, encrypted);
	}
}
