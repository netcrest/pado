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

import java.io.IOException;

public class Encryptor
{
	private void interact() throws Exception
	{
		writeLine();
		writeLine("Encryptor allows you to encrypt any text such as password that");
		writeLine("can be safely used to set properties in the configuration files.");
		writeLine();
		String line;
		do {
			writeLine("Enter 'e' to encrypt, 'd' to decrypt, 'q' to quit: [e]");
			line = readLine();
			if (line.equalsIgnoreCase("e")) {
				encryptUser();
			} else if (line.equalsIgnoreCase("d")) {
				decryptUser();
			}
		} while (line.equalsIgnoreCase("q") == false);
		writeLine("Exit");
	}

	private void encryptUser() throws Exception
	{
		writeLine("Enter clear text to encrypt: ");
		String line = readLine();
		String estr = AESCipher.encryptUserTextToText(line);
		writeLine();
		writeLine("Encrypted:");
		writeLine(estr);	
	}
	
	private void decryptUser() throws Exception
	{
		writeLine("Enter encrypted text to decrypt: ");
		String line = readLine();
		String value = AESCipher.decryptUserTextToText(line);
		writeLine();
		writeLine("Decrypted:");
		writeLine(value);
		writeLine();
	}

	private void writeLine()
	{
		System.out.println();
	}

	private void writeLine(String line)
	{
		System.out.println(line);
	}

	private void write(String str)
	{
		System.out.print(str);
	}

	/**
	 * Returns user inputs
	 * 
	 * @throws IOException
	 *             Thrown if it encounters an I/O error.
	 */
	private String readLine() throws IOException
	{
		byte data[] = new byte[256];
		byte b;
		int offset = 0;
		while ((b = (byte) System.in.read()) != 0xa) {
			data[offset++] = b;
		}
		return new String(data, 0, offset).trim();
	}

	private static void usage()
	{
		System.out.println();
		System.out.println("Usage:");
		System.out.println("   Encryptor [-?]");
		System.exit(0);
	}

	public static void main(String args[])
	{
		Encryptor encryptor = new Encryptor();
		try {
			encryptor.interact();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
