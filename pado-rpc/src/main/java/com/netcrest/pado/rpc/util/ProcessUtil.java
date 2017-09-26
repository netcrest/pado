package com.netcrest.pado.rpc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * FileUtil provides methods for handling {@link Process} objects.
 * 
 * @author dpark
 *
 */
public class ProcessUtil
{
	/**
	 * Prints the process output to sysout.
	 * 
	 * @param proc
	 *            Process object
	 * @throws IOException
	 *             Thrown if an I/O error occurs
	 */
	public static void printProcess(Process proc) throws IOException
	{
		InputStreamReader isr = new InputStreamReader(proc.getInputStream());
		BufferedReader reader = new BufferedReader(isr);
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
	}

	/**
	 * Returns the process execution output.
	 * 
	 * @param proc
	 *            Process object
	 * @throws IOException
	 *             Thrown if an I/O error occurs
	 */
	public static String getProcessOutput(Process proc) throws IOException
	{
		InputStreamReader isr = new InputStreamReader(proc.getInputStream());
		BufferedReader reader = new BufferedReader(isr);
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
			buffer.append('\n');
		}
		reader.close();
		return buffer.toString();
	}
}
