package com.netcrest.pado.rpc.util;

/**
 * StringUtil provides convenience methods for manipulating String values.
 * 
 * @author dpark
 *
 */
public class StringUtil
{
	/**
	 * Converts the specified string array to string with the specified
	 * character as a delimiter.
	 * 
	 * @param strArray
	 *            String array
	 */
	public static String arrayToString(String[] strArray, char delimiter)
	{
		if (strArray == null) {
			return null;
		}
		String str = "";
		int i = 0;
		for (String token : strArray) {
			if (i > 0) {
				str += delimiter;
			}
			str += token;
			i++;
		}
		return str;
	}

	/**
	 * Converts the specified string to array by splitting it by the applying
	 * the specified regular expression. It trims white spaces in each item.
	 * 
	 * @param str
	 *            String to be converted to array
	 * @param regex
	 *            Regular expression
	 * @return null if the specified str is null, array of one item (str) if
	 *         regex is null.
	 */
	public static String[] string2Array(String str, String regex)
	{
		if (str == null) {
			return null;
		}
		if (regex == null) {
			return new String[] { str };
		}
		String[] split = str.split(regex);
		for (int i = 0; i < split.length; i++) {
			split[i] = split[i].trim();
		}
		return split;
	}
}
