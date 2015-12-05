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
package com.netcrest.pado.gemfire.util;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.internal.DSCODE;
import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.data.jsonlite.JsonLiteException;
import com.netcrest.pado.data.jsonlite.internal.JsonLiteSerializer;

public abstract class DataSerializerEx extends DataSerializer
{
	private static final byte NULL_VALUE = -1;
	private static final byte SHORT_ARRAY_LEN = -2;
	private static final byte INT_ARRAY_LEN = -3;
	private static final int MAX_BYTE_ARRAY_LEN = ((byte) -4) & 0xFF;
	
	private static final byte JSON_LITE = 1;
	private static final byte COLLECTION = 14;
	private static final byte ARRAY_LIST = 15;
	private static final byte LINKED_LIST = 16;
	private static final byte HASH_SET = 17;
	private static final byte TREE_SET = 18;
	private static final byte LINKED_HASH_SET = 19;
	private static final byte VECTOR = 20;
	private static final byte STACK = 21;
	private static final byte MAP = 22;
	private static final byte HASH_MAP = 23;
	private static final byte HASHTABLE = 24;
	private static final byte TREE_MAP = 25;
	private static final byte LINKED_HASH_MAP = 26;
	private static final byte PROPERTIES = 27;
	
	/**
	 * Writes the specified byte array to the output stream. This method is 
	 * not thread safe.
	 * 
	 * @param array The byte array to compress
	 * @param buffer The byte array buffer used as input to the deflater. This
	 *               buffer must have enough space to hold the compressed data.
	 * @param compressor java.util.Deflater. 
	 * @param output The data output stream.
	 * @throws IOException Thrown if unable to write to the output stream.
	 */
	public static void writeByteArray(byte array[], byte buffer[], Deflater compressor, DataOutput output) throws IOException
	{
		// Compress the bytes
		 compressor.setInput(array);
		 compressor.finish();
		 int compressedDataLength = compressor.deflate(buffer);
		 DataSerializer.writeByteArray(buffer, compressedDataLength, output);
	}
	
	/**
	 * Reads a byte array from the input stream. This method is not thread safe.
	 * 
	 * @param buffer The buffer to hold the decompressed data. This buffer
	 *               must be large enough to hold the decompressed data.
	 * @param decompressor java.util.Inflater
	 * @param input The data input stream.
	 * @return Returns the actual byte array (not compressed) read from the 
	 *         input stream.
	 * @throws IOException Thrown if unable to read from the input stream or
	 *                     unable to decompress the data.
	 */
	public static byte[] readByteArray(byte buffer[], Inflater decompressor, DataInput input) throws IOException
	{
		byte compressedBuffer[] = DataSerializer.readByteArray(input);	
		// Decompress the bytes
		decompressor.setInput(compressedBuffer, 0, compressedBuffer.length);
		byte retval[] = null;
		try {
			int resultLength = decompressor.inflate(buffer);
			retval = new byte[resultLength];
			System.arraycopy(compressedBuffer, 0, retval, 0, resultLength);
		} catch (DataFormatException e) {
			throw new IOException("Unable to decompress the byte array due to a data format error. " + e.getMessage());
		}
		return retval;
	}
	
	/**
	 * Reads a UTF string from the input. This method is analogous to 
	 * DataInput.readUTF() except that it supports null string.
	 * @param input The data input stream.
	 * @return Returns null or non-null string value.
	 * @throws IOException Thrown if unable to read from the input stream.
	 */
	public static String readUTF(DataInput input) throws IOException
	{
//		byte header = input.readByte();
//		if (header == DSCODE.NULL_STRING) {
//			return null;
//		} 
//		return input.readUTF();
		return readString(input);
	}
	
	/**
	 * Writes the specified sting value to the output stream. This method
	 * is analogous to DataOutput.writeUTF() except that it supports null
	 * string.
	 * @param value The string value to write to the output stream.
	 * @param output The data output stream.
	 * @throws IOException Thrown if unable to write to the output stream. 
	 */
	public static void writeUTF(String value, DataOutput output) throws IOException
	{
//		if (value == null) {
//			output.writeByte(DSCODE.NULL_STRING);
//		} else {
//			output.writeByte(DSCODE.STRING);
//			output.writeUTF(value);
//		}	
		writeString(value, output);
	}
	
	/**
	 * Reads a BigDecimal value from the input.
	 * @param input The data input stream.
	 * @return Returns null or non-null string value.
	 * @throws IOException Thrown if unable to read from the input stream.
	 */
	public static BigDecimal readBigDecimal(DataInput input) throws IOException
	{
		String val = readUTF(input);
		if (val == null) {
			return null;
		} else {
			return new BigDecimal(val);
		}
	}
	
	/**
	 * Writes the specified BigDecimal value to the output stream.
	 * @param bigDecimal The BigDecimal value to write to the output stream.
	 * @param output The data output stream.
	 * @throws IOException Thrown if unable to write to the output stream. 
	 */
	public static void writeBigDecimal(BigDecimal bigDecimal, DataOutput output) throws IOException
	{
		if (bigDecimal == null) {
			writeUTF(null, output);
		} else {
			writeUTF(bigDecimal.toString(), output);
		}
	}
	
	/**
	 * Reads a BigInteger value from the input.
	 * @param input The data input stream.
	 * @return Returns null or non-null string value.
	 * @throws IOException Thrown if unable to read from the input stream.
	 */
	public static BigInteger readBigInteger(DataInput input) throws IOException
	{
		String val = readUTF(input);
		if (val == null) {
			return null;
		} else {
			return new BigInteger(val);
		}
	}
	
	/**
	 * Writes the specified BigInteger value to the output stream.
	 * @param bigInteger The BigInteger value to write to the output stream.
	 * @param output The data output stream.
	 * @throws IOException Thrown if unable to write to the output stream. 
	 */
	public static void writeBigInteger(BigInteger bigInteger, DataOutput output) throws IOException
	{
		if (bigInteger == null) {
			writeUTF(null, output);
		} else {
			writeUTF(bigInteger.toString(), output);
		}
	}
	
	public static Object read(Class cls, DataInput in) throws IOException, ClassNotFoundException
	{
		Object value = null;
		if (cls == String.class) {
			value = readUTF(in);
		} else if (cls == Boolean.TYPE || cls == Boolean.class) {
			value = readBoolean(in);
		} else if (cls == Byte.TYPE || cls == Byte.class) {
			value = readByte(in);
		} else if (cls == Character.TYPE || cls == Character.class) {
			value = readCharacter(in);
		} else if (cls == Double.TYPE || cls == Double.class) {
			value = readDouble(in);
		} else if (cls == Float.TYPE || cls == Float.class) {
			value = readFloat(in);
		} else if (cls == Integer.TYPE || cls == Integer.class) {
			value = readInteger(in);
		} else if (cls == Long.TYPE || cls == Long.class) {
			value = readLong(in);
		} else if (cls == Short.TYPE || cls == Short.class) {
			value = readShort(in);
		} else if (cls == JsonLite.class) {
			value = checkDomainObject(readJsonLite(in));
		} else if (cls == Date.class) {
			value = readDate(in);
		} else if (cls == BigDecimal.class) {
			value = readBigDecimal(in);
		} else if (cls == BigInteger.class) {
			value = readBigInteger(in);
		} else if (Collection.class.isAssignableFrom(cls)) {
			value = readCollection(in);
		} else if (Map.class.isAssignableFrom(cls)) {
			value = readMap(in);
		} else if (cls.isArray()) {
			if (cls == String[].class) {
				value = readStringArray(in);
			} else if (cls == boolean[].class) {
				value = readBooleanArray(in);
			} else if (cls == byte[].class) {
				value = readByteArray(in);
			} else if (cls == char[].class) {
				value = readCharArray(in);
			} else if (cls == double[].class) {
				value = readDoubleArray(in);
			} else if (cls == float[].class) {
				value = readFloatArray(in);
			} else if (cls == int[].class) {
				value = readIntArray(in);
			} else if (cls == long[].class) {
				value = readLongArray(in);
			} else if (cls == short[].class) {
				value = readShortArray(in);
			} else if (cls == JsonLite[].class) {
				value = readJsonLiteArray(in);
			} else if (cls == Date[].class) {
				value = readDateArray(in);
			} else if (cls == BigDecimal[].class) {
				value = readBigDecimalArray(in);
			} else if (cls == BigInteger[].class) {
				value = readBigIntegerArray(in);
			} else if (cls == Object[].class) {
				value = readObjectArray(in);
			} else {
				value = readCustomObject(in);
			}
		} else {
			value = readCustomObject(in);
		}
		return value;
	}
	
	@SuppressWarnings("rawtypes")
	public static void write(Class cls, Object object, DataOutput out) throws IOException
	{
		if (cls == String.class) {
			writeUTF((String) object, out);
		} else if (cls == Boolean.TYPE || cls == Boolean.class) {
			writeBoolean((Boolean) object, out);
		} else if (cls == Byte.TYPE || cls == Byte.class) {
			if (Number.class.isAssignableFrom(cls)) {
				writeByte(((Number)object).byteValue(), out);
			} else {
				writeByte((Byte) object, out);
			}
		} else if (cls == Character.TYPE || cls == Character.class) {
			writeCharacter((Character) object, out);
		} else if (cls == Double.TYPE || cls == Double.class) {
			writeDouble((Double) object, out);
		} else if (cls == Float.TYPE || cls == Float.class) {
			writeFloat((Float) object, out);
		} else if (cls == Integer.TYPE || cls == Integer.class) {
			writeInteger((Integer) object, out);
		} else if (cls == Long.TYPE || cls == Long.class) {
			writeLong((Long) object, out);
		} else if (cls == Short.TYPE || cls == Short.class) {
			writeShort((Short) object, out);
		} else if (IJsonLiteWrapper.class.isAssignableFrom(cls)) {
			writeJsonLite(((IJsonLiteWrapper)object).toJsonLite(), out);
		} else if (JsonLite.class.isAssignableFrom(cls)) {
			writeJsonLite((JsonLite) object, out);
		} else if (Date.class.isAssignableFrom(cls)) {
			writeDate((Date) object, out);
		} else if (cls == BigDecimal.class) {
			writeBigDecimal((BigDecimal) object, out);
		} else if (cls == BigInteger.class) {
			writeBigInteger((BigInteger) object, out);
		} else if (Collection.class.isAssignableFrom(cls)) {
			writeCollection((Collection) object, out);
		} else if (Map.class.isAssignableFrom(cls)) {
			writeMap((Map) object, out);
		} else if (cls.isArray()) {
			if (cls == String[].class) {
				writeStringArray((String[]) object, out);
			} else if (cls == boolean[].class) {
				writeBooleanArray((boolean[]) object, out);
			} else if (cls == byte[].class) {
				writeByteArray((byte[]) object, out);
			} else if (cls == char[].class) {
				writeCharArray((char[]) object, out);
			} else if (cls == double[].class) {
				writeDoubleArray((double[]) object, out);
			} else if (cls == float[].class) {
				writeFloatArray((float[]) object, out);
			} else if (cls == int[].class) {
				writeIntArray((int[]) object, out);
			} else if (cls == long[].class) {
				writeLongArray((long[]) object, out);
			} else if (cls == short[].class) {
				writeShortArray((short[]) object, out);
			} else if (cls == JsonLite[].class) {
				writeJsonLiteArray((JsonLite[]) object, out);
			} else if (cls == Date[].class) {
				writeDateArray((Date[]) object, out);
			} else if (cls == BigDecimal[].class) {
				writeBigDecimalArray((BigDecimal[]) object, out);
			} else if (cls == BigInteger[].class) {
				writeBigIntegerArray((BigInteger[]) object, out);
			} else if (cls == Object[].class) {
				writeObjectArray((Object[]) object, out);
			} else {
				writeCustomObject(object, out);
			}
		} else {
			writeCustomObject(object, out);
		}
	}
	
	public static void writeCustomObject(Object object, DataOutput out) throws IOException
	{
		if (object == null) {
			out.writeByte(DSCODE.NULL);
		} else {
			if (object instanceof IJsonLiteWrapper) {
				writeJsonLite(((IJsonLiteWrapper) object).toJsonLite(), out);
			} else if (object instanceof JsonLite) {
				writeJsonLite((JsonLite) object, out);
			} else {
				// This byte is necessary in order to detect JsonLite objects.
				// It is only a pad that is needed in order to correctly
				// deserialize the object.
				out.writeByte(DSCODE.USER_CLASS);
				writeObject(object, out);
			}
		}
	}
	
	public static JsonLite[] readJsonLiteArray(DataInput in) throws IOException, ClassNotFoundException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			JsonLite[] array = new JsonLite[length];
			for (int i = 0; i < length; i++) {
				array[i] = readJsonLite(in);
			}
			return array;
		}
	}
	
	public static void writeJsonLiteArray(JsonLite[] array, DataOutput out) throws IOException
	{
		int length;
		if (array == null) {
			length = -1;
		} else {
			length = array.length;
		}
		writeArrayLength(length, out);
		if (length >= 0) {
			for (int i = 0; i < length; i++) {
				writeJsonLite(array[i], out);
			}
		}
	}
	
	public static Object[] readObjectArray(DataInput in) throws IOException, ClassNotFoundException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			Class<?> cls = readClass(in);
			Object[] array = (Object[]) Array.newInstance(cls, length);
			for (int i = 0; i < length; i++) {
				array[i] = readCustomObject(in);
			}
			return array;
		}
	}
	
	public static void writeObjectArray(Object[] array, DataOutput out) throws IOException
	{
		int length;
		if (array == null) {
			length = -1;
		} else {
			length = array.length;
		}
		writeArrayLength(length, out);
		if (length >= 0) {
			writeClass(array.getClass().getComponentType(), out);
			for (int i = 0; i < length; i++) {
				writeCustomObject(array[i], out);
			}
		}
	}
	
	public static void writeBoolean(Boolean value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeBoolean(false);
		} else {
			output.writeBoolean(value);
		}
	}
	
	public static void writeCharacter(Character value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeChar(0);
		} else {
			output.writeChar(value);
		}
	}
	
	public static void writeByte(Byte value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeByte(0);
		} else {
			output.writeByte(value);
		}
	}
	
	public static void writeDouble(Double value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeDouble(0);
		} else {
			output.writeDouble(value);
		}
	}
	
	public static void writeFloat(Float value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeFloat(0);
		} else {
			output.writeFloat(value);
		}
	}
	
	public static void writeInteger(Integer value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeInt(0);
		} else {
			output.writeInt(value);
		}
	}
	
	public static void writeLong(Long value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeLong(0);
		} else {
			output.writeLong(value);
		}
	}
	
	public static void writeShort(Short value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeShort(0);
		} else {
			output.writeShort(value);
		}
	}
	
	public static Collection readCollection(DataInput in) throws IOException, ClassNotFoundException
	{
		byte type = in.readByte();
		return readCollection(type, in);
	}

	private static Collection readCollection(byte type, DataInput in) throws IOException, ClassNotFoundException
	{
		Collection value = null;
		int size = readArrayLength(in);
		if (size >= 0) {
			switch (type) {
			case ARRAY_LIST:
				value = new ArrayList(size);
				break;
			case LINKED_LIST:
				value = new LinkedList();
				break;
			case HASH_SET:
				value = new HashSet(size, 1f);
				break;
			case TREE_SET:
				value = new TreeSet();
				break;
			case LINKED_HASH_SET:
				value = new LinkedHashSet(size, 1f);
				break;
			case VECTOR:
				value = new Vector(size);
				break;
			case STACK:
				value = new Stack();
				break;
			case NULL_VALUE:
				break;
			case COLLECTION:
			default:
				String className = readUTF(in);
				Class cls = Class.forName(className);
				try {
					value = (Collection) cls.newInstance();
				} catch (InstantiationException e) {
					throw new JsonLiteException(e);
				} catch (IllegalAccessException e) {
					throw new JsonLiteException(e);
				}
				break;
			}
		}

		if (size > 0) {
			for (int i = 0; i < size; i++) {
				value.add(readCustomObject(in));
			}
		}
		return value;
	}
	
	public static Object readCustomObject(DataInput in) throws IOException, ClassNotFoundException
	{
		Object value;
		byte header = in.readByte();
		switch (header) {
		case DSCODE.NULL:
			value = null;
			break;
		case JSON_LITE:
			JsonLite jl = new JsonLite();
			jl.fromData(in);
			value = checkDomainObject(jl);
			break;
		default:
			value = DataSerializerEx.readObject(in);
			break;
		}
		return value;
	}
	
	public static void writeCollection(Collection value, DataOutput out) throws IOException
	{
		boolean isUndefinedCollection = false;

		// Check class instead of using instanceof to avoid subclasses
		if (value != null) {
			Class cls = value.getClass();
			if (cls == ArrayList.class) {
				out.writeByte(ARRAY_LIST);
			} else if (cls == LinkedList.class) {
				out.writeByte(LINKED_LIST);
			} else if (cls == HashSet.class) {
				out.writeByte(HASH_SET);
			} else if (cls == TreeSet.class) {
				out.writeByte(TREE_SET);
			} else if (cls == LinkedHashSet.class) {
				out.writeByte(LINKED_HASH_SET);
			} else if (cls == Vector.class) {
				out.writeByte(VECTOR);
			} else if (cls == Stack.class) {
				out.writeByte(STACK);
			} else {
				out.writeByte(COLLECTION);
				isUndefinedCollection = true;
			}
		} else {
			out.writeByte(NULL_VALUE);
		}

		int size;
		if (value == null) {
			size = -1;
		} else {
			size = value.size();
		}
		writeArrayLength(size, out);

		// Write class name for undefined collection
		if (isUndefinedCollection) {
			writeUTF(value.getClass().getName(), out);
		}

		if (size > 0) {
			Iterator iterator = value.iterator();
			while (iterator.hasNext()) {
				writeCustomObject(iterator.next(), out);
			}
		}
	}
	
	/**
	 * Convert the given unsigned byte to an int. The returned value will be in
	 * the range [0..255] inclusive
	 */
	private static final int ubyteToInt(byte ub)
	{
		return ub & 0xFF;
	}
	
	public static int readArrayLength(DataInput in) throws IOException
	{
		byte code = in.readByte();
		if (code == NULL_VALUE) {
			return -1;
		} else {
			int result = ubyteToInt(code);
			if (result > MAX_BYTE_ARRAY_LEN) {
				if (code == SHORT_ARRAY_LEN) {
					result = in.readUnsignedShort();
				} else if (code == INT_ARRAY_LEN) {
					result = in.readInt();
				} else {
					throw new IllegalStateException("unexpected array length code=" + code);
				}
			}
			return result;
		}
	}
	
	private static void writeArrayLength(int len, DataOutput out) throws IOException
	{
		if (len == -1) {
			out.writeByte(NULL_VALUE);
		} else if (len <= MAX_BYTE_ARRAY_LEN) {
			out.writeByte(len);
		} else if (len <= 0xFFFF) {
			out.writeByte(SHORT_ARRAY_LEN);
			out.writeShort(len);
		} else {
			out.writeByte(INT_ARRAY_LEN);
			out.writeInt(len);
		}
	}
	
	public static Map readMap(DataInput in) throws IOException, ClassNotFoundException
	{
		byte type = in.readByte();
		return readMap(type, in);
	}

	private static Map readMap(byte type, DataInput in) throws IOException, ClassNotFoundException
	{
		Map map = null;
		int size = readArrayLength(in);
		if (size >= 0) {
			switch (type) {
			case HASH_MAP:
				map = new HashMap(size, 1f);
				break;
			case HASHTABLE:
				map = new Hashtable(size, 1f);
				break;
			case TREE_MAP:
				map = new TreeMap();
				break;
			case LINKED_HASH_MAP:
				map = new LinkedHashMap(size, 1f);
				break;
			case PROPERTIES:
				map = new Properties();
				break;
			case NULL_VALUE:
				break;
			case MAP:
			default:
				String className = readUTF(in);
				Class cls = Class.forName(className);
				try {
					map = (Map) cls.newInstance();
				} catch (InstantiationException e) {
					throw new JsonLiteException(e);
				} catch (IllegalAccessException e) {
					throw new JsonLiteException(e);
				}
				break;
			}
		}

		if (size > 0) {
			for (int i = 0; i < size; i++) {
				map.put(readCustomObject(in), readCustomObject(in));
			}
		}
		return map;
	}
	
	public static void writeMap(Map<?, ?> map, DataOutput out) throws IOException
	{
		boolean isUndefinedMap = false;
		if (map != null) {
			Class cls = map.getClass();
			if (cls == HashMap.class) {
				out.writeByte(HASH_MAP);
			} else if (cls == Hashtable.class) {
				out.writeByte(HASHTABLE);
			} else if (cls == TreeMap.class) {
				out.writeByte(TREE_MAP);
			} else if (cls == LinkedHashMap.class) {
				out.writeByte(LINKED_HASH_MAP);
			} else if (cls == Properties.class) {
				out.writeByte(PROPERTIES);
			} else {
				out.writeByte(MAP);
				isUndefinedMap = true;
			}
		} else {
			out.writeByte(NULL_VALUE);
		}

		int size;
		if (map == null) {
			size = -1;
		} else {
			size = map.size();
		}
		writeArrayLength(size, out);

		// Write class name for undefined map
		if (isUndefinedMap) {
			writeUTF(map.getClass().getName(), out);
		}

		if (size > 0) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				writeCustomObject(entry.getKey(), out);
				writeCustomObject(entry.getValue(), out);
			}
		}
	}
	
	public static Date[] readDateArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			Date[] array = new Date[length];
			for (int i = 0; i < length; i++) {
				array[i] = readDate(in);
			}
			return array;
		}
	}

	public static BigDecimal[] readBigDecimalArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			BigDecimal[] array = new BigDecimal[length];
			for (int i = 0; i < length; i++) {
				array[i] = readBigDecimal(in);
			}
			return array;
		}
	}

	public static BigInteger[] readBigIntegerArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			BigInteger[] array = new BigInteger[length];
			for (int i = 0; i < length; i++) {
				array[i] = readBigInteger(in);
			}
			return array;
		}
	}

	
	public static void writeDateArray(Date[] array, DataOutput out) throws IOException
	{
		int length;
		if (array == null) {
			length = -1;
		} else {
			length = array.length;
		}
		writeArrayLength(length, out);
		if (length > 0) {
			for (int i = 0; i < length; i++) {
				writeDate(array[i], out);
			}
		}
	}

	public static void writeBigDecimalArray(BigDecimal[] array, DataOutput out) throws IOException
	{
		int length;
		if (array == null) {
			length = -1;
		} else {
			length = array.length;
		}
		writeArrayLength(length, out);
		if (length > 0) {
			for (int i = 0; i < length; i++) {
				writeBigDecimal(array[i], out);
			}
		}
	}

	public static void writeBigIntegerArray(BigInteger[] array, DataOutput out) throws IOException
	{
		int length;
		if (array == null) {
			length = -1;
		} else {
			length = array.length;
		}
		writeArrayLength(length, out);
		if (length > 0) {
			for (int i = 0; i < length; i++) {
				writeBigInteger(array[i], out);
			}
		}
	}
	
	public static Object checkDomainObject(JsonLite jl)
	{
		return JsonLiteSerializer.checkDomainObject(jl);
	}
	
	public static JsonLite readJsonLite(DataInput input) throws ClassNotFoundException, IOException
	{
		byte header = input.readByte();
		if (header == DSCODE.NULL) {
			return null;
		}
		JsonLite jl = new JsonLite();
		jl.fromData(input);
		return jl;
	}
	
	public static void writeJsonLite(JsonLite value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeByte(DSCODE.NULL);
		} else {
			output.writeByte(JSON_LITE);
			value.toData(output);
		}
	}
}
 