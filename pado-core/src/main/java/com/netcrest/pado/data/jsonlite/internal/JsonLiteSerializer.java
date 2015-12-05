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
package com.netcrest.pado.data.jsonlite.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.data.jsonlite.JsonLiteException;

/**
 * JsonLiteSerializer serializes JsonLite specifics. It is for internal use
 * only.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class JsonLiteSerializer
{
	private static final byte NULL_ARRAY = -1;
	private static final byte SHORT_ARRAY_LEN = -2;
	private static final byte INT_ARRAY_LEN = -3;
	private static final int MAX_BYTE_ARRAY_LEN = ((byte) -4) & 0xFF;

	private static final byte STRING = 42;
	private static final byte NULL = 41;
	private static final byte NULL_STRING = 69;
	private static final byte HUGE_STRING = 89;

	private static final byte JSON_LITE = 1;
	private static final byte DATE = 2;
	private static final byte BIG_DECIMAL = 3;
	private static final byte BIG_INTEGER = 4;
	private static final byte BOOLEAN = 5;
	private static final byte BYTE = 6;
	private static final byte CHARACTER = 7;
	private static final byte DOUBLE = 8;
	private static final byte FLOAT = 9;
	private static final byte INTEGER = 10;
	private static final byte LONG = 11;
	private static final byte SHORT = 12;
	private static final byte VOID = 13;
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
	private static final byte OBJECT = 28;
	private static final byte ARRAY_STRING = 29;
	private static final byte ARRAY_BOOLEAN = 30;
	private static final byte ARRAY_BYTE = 31;
	private static final byte ARRAY_CHARACTER = 32;
	private static final byte ARRAY_DOUBLE = 33;
	private static final byte ARRAY_FLOAT = 34;
	private static final byte ARRAY_INTEGER = 35;
	private static final byte ARRAY_LONG = 36;
	private static final byte ARRAY_SHORT = 37;
	private static final byte ARRAY_DATE = 38;
	private static final byte ARRAY_BIG_DECIMAL = 39;
	private static final byte ARRAY_BIG_INTEGER = 40;
	private static final byte ARRAY_OBJECT = 43;
	private static final byte ARRAY_JSON_LITE = 44;
	private static final byte DATA_CONTAINER = 45;

	public static void write(Class cls, Object object, DataOutput out) throws IOException
	{
		if (cls == String.class) {
			writeUTF((String) object, out);
		} else if (cls == Boolean.TYPE || cls == Boolean.class) {
			writeBoolean((Boolean) object, out);
		} else if (cls == Byte.TYPE || cls == Byte.class) {
			writeByte((Byte) object, out);
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
		} else if (object instanceof IJsonLiteWrapper) {
			writeJsonLite(((IJsonLiteWrapper) object).toJsonLite(), out);
		} else if (cls == JsonLite.class) {
			writeJsonLite((JsonLite) object, out);
		} else if (cls == Date.class) {
			writeDate((Date) object, out);
		} else if (cls == BigDecimal.class) {
			writeBigDecimal((BigDecimal) object, out);
		} else if (cls == BigInteger.class) {
			writeBigInteger((BigInteger) object, out);
		} else if (Collection.class.isAssignableFrom(cls)) {
			writeCollection(cls, (Collection) object, out);
		} else if (Map.class.isAssignableFrom(cls)) {
			writeMap(cls, (Map) object, out);
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
				writeSerializable(object, out);
			}
		} else {
			writeSerializable(object, out);
		}
	}

	/**
	 * Writes JsonLite supported types only. null, all primitive types, String,
	 * all primitive array types, String[], Object[], java.util.Collection,
	 * java.util.Map, java.util.Date, java.math.BigDecimal,
	 * java.math.BigInteger, and JsonLite.
	 * 
	 * @param object
	 * @param out
	 * @throws IOException
	 */
	public static void writeObject(Object object, DataOutput out) throws IOException
	{
		if (object == null) {
			out.writeByte(NULL);
		} else {
			Class cls = object.getClass();
			if (object instanceof String) {
				out.writeByte(STRING);
				out.writeUTF((String) object);
			} else if (cls == Boolean.TYPE || cls == Boolean.class) {
				out.writeByte(BOOLEAN);
				writeBoolean((Boolean) object, out);
			} else if (cls == Byte.TYPE || cls == Byte.class) {
				out.writeByte(BYTE);
				writeByte((Byte) object, out);
			} else if (cls == Character.TYPE || cls == Character.class) {
				out.writeByte(CHARACTER);
				writeCharacter((Character) object, out);
			} else if (cls == Double.TYPE || cls == Double.class) {
				out.writeByte(DOUBLE);
				writeDouble((Double) object, out);
			} else if (cls == Float.TYPE || cls == Float.class) {
				out.writeByte(FLOAT);
				writeFloat((Float) object, out);
			} else if (cls == Integer.TYPE || cls == Integer.class) {
				out.writeByte(INTEGER);
				writeInteger((Integer) object, out);
			} else if (cls == Long.TYPE || cls == Long.class) {
				out.writeByte(LONG);
				writeLong((Long) object, out);
			} else if (cls == Short.TYPE || cls == Short.class) {
				out.writeByte(SHORT);
				writeShort((Short) object, out);
			} else if (object instanceof IJsonLiteWrapper) {
				writeJsonLite(((IJsonLiteWrapper) object).toJsonLite(), out);
			} else if (object instanceof JsonLite) {
				writeJsonLite((JsonLite) object, out);
			} else if (object instanceof Date) {
				out.writeByte(DATE);
				writeDate((Date) object, out);
			} else if (object instanceof BigDecimal) {
				out.writeByte(BIG_DECIMAL);
				writeBigDecimal((BigDecimal) object, out);
			} else if (object instanceof BigInteger) {
				out.writeByte(BIG_INTEGER);
				writeBigInteger((BigInteger) object, out);
			} else if (Collection.class.isAssignableFrom(cls)) {
				out.writeByte(COLLECTION);
				writeCollection(cls, (Collection) object, out);
			} else if (Map.class.isAssignableFrom(cls)) {
				out.writeByte(MAP);
				writeMap(cls, (Map) object, out);
			} else if (object.getClass().isArray()) {
				if (object instanceof String[]) {
					out.writeByte(ARRAY_STRING);
					writeStringArray((String[]) object, out);
				} else if (object instanceof boolean[]) {
					out.writeByte(ARRAY_BOOLEAN);
					writeBooleanArray((boolean[]) object, out);
				} else if (object instanceof byte[]) {
					out.writeByte(ARRAY_BYTE);
					writeByteArray((byte[]) object, out);
				} else if (object instanceof char[]) {
					out.writeByte(ARRAY_CHARACTER);
					writeCharArray((char[]) object, out);
				} else if (object instanceof double[]) {
					out.writeByte(ARRAY_DOUBLE);
					writeDoubleArray((double[]) object, out);
				} else if (object instanceof float[]) {
					out.writeByte(ARRAY_FLOAT);
					writeFloatArray((float[]) object, out);
				} else if (object instanceof int[]) {
					out.writeByte(ARRAY_INTEGER);
					writeIntArray((int[]) object, out);
				} else if (object instanceof long[]) {
					out.writeByte(ARRAY_LONG);
					writeLongArray((long[]) object, out);
				} else if (object instanceof short[]) {
					out.writeByte(ARRAY_SHORT);
					writeShortArray((short[]) object, out);
				} else if (object instanceof JsonLite[]) {
					out.writeByte(ARRAY_JSON_LITE);
					writeJsonLiteArray((JsonLite[]) object, out);
				} else if (object instanceof Date[]) {
					out.writeByte(ARRAY_DATE);
					writeDateArray((Date[]) object, out);
				} else if (object instanceof BigDecimal[]) {
					out.writeByte(ARRAY_BIG_DECIMAL);
					writeBigDecimalArray((BigDecimal[]) object, out);
				} else if (object instanceof BigInteger[]) {
					out.writeByte(ARRAY_BIG_INTEGER);
					writeBigIntegerArray((BigInteger[]) object, out);
				} else if (object instanceof Object[]) {
					out.writeByte(ARRAY_OBJECT);
					writeObjectArray((Object[]) object, out);
				} else {
					out.writeByte(OBJECT);
					writeSerializable(object, out);
				}
			} else {
				out.writeByte(OBJECT);
				writeSerializable(object, out);
			}
		}
	}

	private static void writeSerializable(Object object, DataOutput out) throws IOException
	{
		if (out instanceof ObjectOutputStream) {
			((ObjectOutputStream) out).writeObject(object);
		} else {
			OutputStream stream;
			if (out instanceof OutputStream) {
				stream = (OutputStream) out;
				ObjectOutputStream oos = new ObjectOutputStream(stream);
				oos.writeObject(object);
				oos.flush();
				// Do not close oos. The caller is responsible for closing
				// stream.
			} else {
				final DataOutput out2 = out;
				stream = new OutputStream() {
					@Override
					public void write(int b) throws IOException
					{
						out2.write(b);
					}
				};
				ObjectOutputStream oos = new ObjectOutputStream(stream);
				oos.writeObject(object);
				oos.close();
			}
		}
	}

	public static void writeDate(Date date, DataOutput out) throws IOException
	{
		long v;
		if (date == null) {
			v = -1L;
		} else {
			v = date.getTime();
		}
		out.writeLong(v);
	}

	public static void writeBigDecimal(BigDecimal bigDecimal, DataOutput out) throws IOException
	{
		if (bigDecimal == null) {
			writeUTF(null, out);
		} else {
			writeUTF(bigDecimal.toString(), out);
		}
	}

	public static void writeBigInteger(BigInteger bigInteger, DataOutput out) throws IOException
	{
		if (bigInteger == null) {
			out.writeByte(NULL);
		} else {
			out.writeByte(BIG_INTEGER);
			writeByteArray(bigInteger.toByteArray(), out);
		}
	}

	public static BigInteger readBigInteger(DataInput in) throws IOException
	{
		byte header = in.readByte();
		if (header == NULL) {
			return null;
		}
		byte bytes[] = readByteArray(in);
		return new BigInteger(bytes);
	}

	public static BigDecimal readBigDecimal(DataInput in) throws IOException
	{
		String val = readUTF(in);
		if (val == null) {
			return null;
		}
		return new BigDecimal(val);
	}

	public static String[] readStringArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			String[] value = new String[length];
			for (int i = 0; i < length; i++) {
				value[i] = readUTF(in);
			}
			return value;
		}
	}

	public static void writeStringArray(String[] array, DataOutput out) throws IOException
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
				writeUTF(array[i], out);
			}
		}
	}

	public static void writeBooleanArray(boolean[] array, DataOutput out) throws IOException
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
				out.writeBoolean(array[i]);
			}
		}
	}

	public static void writeByteArray(byte[] array, DataOutput out) throws IOException
	{
		int len = 0;
		if (array != null) {
			len = array.length;
		}
		writeByteArray(array, len, out);
	}

	public static void writeCharArray(char[] array, int length, DataOutput out) throws IOException
	{
		if (array == null) {
			length = -1;
		}
		writeArrayLength(length, out);
		if (length > 0) {
			for (int i = 0; i < length; i++) {
				out.writeChar(array[i]);
			}
		}
	}

	public static void writeCharArray(char[] array, DataOutput out) throws IOException
	{
		writeCharArray(array, array != null ? array.length : -1, out);
	}

	public static void writeDoubleArray(double[] array, DataOutput out) throws IOException
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
				out.writeDouble(array[i]);
			}
		}
	}

	public static void writeFloatArray(float[] array, DataOutput out) throws IOException
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
				out.writeFloat(array[i]);
			}
		}
	}

	public static void writeIntArray(int[] array, DataOutput out) throws IOException
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
				out.writeInt(array[i]);
			}
		}
	}

	public static void writeLongArray(long[] array, DataOutput out) throws IOException
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
				out.writeLong(array[i]);
			}
		}
	}

	public static void writeShortArray(short[] array, DataOutput out) throws IOException
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
				out.writeShort(array[i]);
			}
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
				writeObject(array[i], out);
			}
		}
	}

	public static Class<?> readClass(DataInput in) throws IOException, ClassNotFoundException
	{
		byte type = in.readByte();
		if (type == OBJECT) {
			String className = readUTF(in);
			return Class.forName(className);
		} else {
			switch (type) {
			case BOOLEAN:
				return Boolean.TYPE;
			case BYTE:
				return Byte.TYPE;
			case CHARACTER:
				return Character.TYPE;
			case DOUBLE:
				return Double.TYPE;
			case FLOAT:
				return Float.TYPE;
			case INTEGER:
				return Integer.TYPE;
			case LONG:
				return Long.TYPE;
			case SHORT:
				return Short.TYPE;
			case NULL:
			default:
				return null;
			}
		}
	}

	public static void writeClass(Class<?> c, DataOutput out) throws IOException
	{
		if (c == null || c.isPrimitive()) {
			writePrimitiveClass(c, out);
		} else {
			out.writeByte(OBJECT);
			writeUTF(c.getName(), out);
		}
	}

	public static final void writePrimitiveClass(Class c, DataOutput out) throws IOException
	{
		if (c == Boolean.TYPE) {
			out.writeByte(BOOLEAN);
		} else if (c == Character.TYPE) {
			out.writeByte(CHARACTER);
		} else if (c == Byte.TYPE) {
			out.writeByte(BYTE);
		} else if (c == Short.TYPE) {
			out.writeByte(SHORT);
		} else if (c == Integer.TYPE) {
			out.writeByte(INTEGER);
		} else if (c == Long.TYPE) {
			out.writeByte(LONG);
		} else if (c == Float.TYPE) {
			out.writeByte(FLOAT);
		} else if (c == Double.TYPE) {
			out.writeByte(DOUBLE);
		} else if (c == Void.TYPE) {
			out.writeByte(VOID);
		} else if (c == null) {
			out.writeByte(NULL);
		} else {
			throw new JsonLiteException("Unknown primitive type: " + c.getName());
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
				value = readSerializable(in);
			}
		} else {
			value = readSerializable(in);
		}
		return value;
	}

	private static Object readSerializable(final DataInput input) throws IOException, ClassNotFoundException
	{
		Object value = null;
		if (input instanceof ObjectInputStream) {
			value = ((ObjectInputStream) input).readObject();
		} else {
			if (input instanceof InputStream) {
				InputStream stream = (InputStream) input;
				ObjectInputStream ois = new ObjectInputStream(stream);
				value = ois.readObject();
			} else {
				InputStream stream = new InputStream() {
					@Override
					public int read() throws IOException
					{
						return input.readByte();
					}
				};
				ObjectInputStream ois = new ObjectInputStream(stream);
				value = ois.readObject();
				ois.close();
			}
		}
		return value;
	}

	public static Object readObject(DataInput input) throws ClassNotFoundException, IOException
	{
		Object value;
		byte type = input.readByte();
		switch (type) {
		case NULL:
			value = null;
			break;
		case STRING:
			value = input.readUTF();
			break;
		case BOOLEAN:
			value = readBoolean(input);
			break;
		case BYTE:
			value = readByte(input);
			break;
		case CHARACTER:
			value = readCharacter(input);
			break;
		case DOUBLE:
			value = readDouble(input);
			break;
		case FLOAT:
			value = readFloat(input);
			break;
		case INTEGER:
			value = readInteger(input);
			break;
		case LONG:
			value = readLong(input);
			break;
		case SHORT:
			value = readShort(input);
			break;
		case JSON_LITE:
			JsonLite jl = new JsonLite();
			jl.readData(input);
			value = checkDomainObject(jl);
			break;
		case DATE:
			value = readDate(input);
			break;
		case BIG_DECIMAL:
			value = readBigDecimal(input);
			break;
		case BIG_INTEGER:
			value = readBigInteger(input);
			break;
		case COLLECTION:
			value = readCollection(input);
			break;
		case MAP:
			value = readMap(input);
			break;
		case ARRAY_STRING:
			value = readStringArray(input);
			break;
		case ARRAY_BOOLEAN:
			value = readBooleanArray(input);
			break;
		case ARRAY_BYTE:
			value = readByteArray(input);
			break;
		case ARRAY_CHARACTER:
			value = readCharArray(input);
			break;
		case ARRAY_DOUBLE:
			value = readDoubleArray(input);
			break;
		case ARRAY_FLOAT:
			value = readFloatArray(input);
			break;
		case ARRAY_INTEGER:
			value = readIntArray(input);
			break;
		case ARRAY_LONG:
			value = readLongArray(input);
			break;
		case ARRAY_SHORT:
			value = readShortArray(input);
			break;
		case ARRAY_JSON_LITE:
			value = readJsonLiteArray(input);
			break;
		case ARRAY_DATE:
			value = readDateArray(input);
			break;
		case ARRAY_BIG_DECIMAL:
			value = readBigDecimalArray(input);
			break;
		case ARRAY_BIG_INTEGER:
			value = readBigIntegerArray(input);
			break;
		case ARRAY_OBJECT:
			value = readObjectArray(input);
			break;
		case OBJECT:
		default:
			value = readSerializable(input);
			break;
		}
		return value;
	}

	public static Date readDate(DataInput in) throws IOException
	{
		long time = in.readLong();
		Date date = null;
		if (time != -1L) {
			date = new Date(time);
		}
		return date;
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
		if (code == NULL_ARRAY) {
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

	public static boolean[] readBooleanArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			boolean[] array = new boolean[length];
			for (int i = 0; i < length; i++) {
				array[i] = in.readBoolean();
			}
			return array;
		}
	}

	public static byte[] readByteArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			byte[] array = new byte[length];
			in.readFully(array, 0, length);
			return array;
		}
	}

	public static char[] readCharArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			char[] array = new char[length];
			for (int i = 0; i < length; i++) {
				array[i] = in.readChar();
			}
			return array;
		}
	}

	public static double[] readDoubleArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			double[] array = new double[length];
			for (int i = 0; i < length; i++) {
				array[i] = in.readDouble();
			}
			return array;
		}
	}

	public static float[] readFloatArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			float[] array = new float[length];
			for (int i = 0; i < length; i++) {
				array[i] = in.readFloat();
			}
			return array;
		}
	}

	public static int[] readIntArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			int[] array = new int[length];
			for (int i = 0; i < length; i++) {
				array[i] = in.readInt();
			}

			return array;
		}
	}

	public static long[] readLongArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			long[] array = new long[length];
			for (int i = 0; i < length; i++) {
				array[i] = in.readLong();
			}
			return array;
		}
	}

	public static short[] readShortArray(DataInput in) throws IOException
	{
		int length = readArrayLength(in);
		if (length == -1) {
			return null;
		} else {
			short[] array = new short[length];
			for (int i = 0; i < length; i++) {
				array[i] = in.readShort();
			}
			return array;
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
				array[i] = readObject(in);
			}
			return array;
		}
	}

	/**
	 * Writes the specified byte array to the output stream. This method is not
	 * thread safe.
	 * 
	 * @param array
	 *            The byte array to compress
	 * @param buffer
	 *            The byte array buffer used as input to the deflater. This
	 *            buffer must have enough space to hold the compressed data.
	 * @param compressor
	 *            java.util.Deflater.
	 * @param output
	 *            The data output stream.
	 * @throws IOException
	 *             Thrown if unable to write to the output stream.
	 */
	public static void writeByteArray(byte array[], byte buffer[], Deflater compressor, DataOutput output)
			throws IOException
	{
		// Compress the bytes
		compressor.setInput(array);
		compressor.finish();
		int compressedDataLength = compressor.deflate(buffer);
		writeByteArray(buffer, compressedDataLength, output);
	}

	public static void writeByteArray(byte[] array, int len, DataOutput out) throws IOException
	{
		int length = len;

		if (array == null) {
			length = -1;
		} else {
			if (length > array.length) {
				length = array.length;
			}
		}
		writeArrayLength(length, out);
		if (length > 0) {
			out.write(array, 0, length);
		}
	}

	private static void writeArrayLength(int len, DataOutput out) throws IOException
	{
		if (len == -1) {
			out.writeByte(NULL_ARRAY);
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

	/**
	 * Reads byte array from the input stream. This method is not thread safe.
	 * 
	 * @param buffer
	 *            The buffer to hold the decompressed data. This buffer must be
	 *            large enough to hold the decompressed data.
	 * @param decompressor
	 *            java.util.Inflater
	 * @param input
	 *            The data input stream.
	 * @return Returns the actual byte array (not compressed) read from the
	 *         input stream.
	 * @throws IOException
	 *             Thrown if unable to read from the input stream or unable to
	 *             decompress the data.
	 */
	public static byte[] readByteArray(byte buffer[], Inflater decompressor, DataInput input) throws IOException
	{
		byte compressedBuffer[] = readByteArray(input);
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
	 * Reads UTF string from the input. This method is analogous to
	 * DataInput.readUTF() except that it supports null string.
	 * 
	 * @param input
	 *            The data input stream.
	 * @return Returns null or non-null string value.
	 * @throws IOException
	 *             Thrown if unable to read from the input stream.
	 */
	public static String readUTF(DataInput input) throws IOException
	{
		byte header = input.readByte();
		if (header == NULL_STRING) {
			return null;
		}
		return input.readUTF();
	}

	/**
	 * Writes the specified sting value to the output stream. This method is
	 * analogous to DataOutput.writeUTF() except that it supports null string.
	 * 
	 * @param value
	 *            The string value to write to the output stream.
	 * @param output
	 *            The data output stream.
	 * @throws IOException
	 *             Thrown if unable to write to the output stream.
	 */
	public static void writeUTF(String value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeByte(NULL_STRING);
		} else {
			output.writeByte(STRING);
			output.writeUTF(value);
		}
	}

	/**
	 * Reads boolean value;
	 */
	public static Boolean readBoolean(DataInput input) throws IOException
	{
		return input.readBoolean();
	}

	/**
	 * Writes the specified boolean value to the stream. If the value is null
	 * then it write false.
	 * 
	 * @param value
	 *            The value to write
	 * @param output
	 * @throws IOException
	 */
	public static void writeBoolean(Boolean value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeBoolean(false);
		} else {
			output.writeBoolean(value);
		}
	}

	public static Byte readByte(DataInput input) throws IOException
	{
		return input.readByte();
	}

	public static void writeByte(Byte value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeByte(0);
		} else {
			output.writeByte(value);
		}
	}

	public static Character readCharacter(DataInput input) throws IOException
	{
		return input.readChar();
	}

	public static void writeCharacter(Character value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeChar(0);
		} else {
			output.writeChar(value);
		}
	}

	public static Double readDouble(DataInput input) throws IOException
	{
		return input.readDouble();
	}

	public static void writeDouble(Double value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeDouble(0);
		} else {
			output.writeDouble(value);
		}
	}

	public static Float readFloat(DataInput input) throws IOException
	{
		return input.readFloat();
	}

	public static void writeFloat(Float value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeFloat(0);
		} else {
			output.writeFloat(value);
		}
	}

	public static Integer readInteger(DataInput input) throws IOException
	{
		return input.readInt();
	}

	public static void writeInteger(Integer value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeInt(0);
		} else {
			output.writeInt(value);
		}
	}

	public static Long readLong(DataInput input) throws IOException
	{
		return input.readLong();
	}

	public static void writeLong(Long value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeLong(0);
		} else {
			output.writeLong(value);
		}
	}

	public static Short readShort(DataInput input) throws IOException
	{
		return input.readShort();
	}

	public static void writeShort(Short value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeShort(0);
		} else {
			output.writeShort(value);
		}
	}

	public static int readUnsignedShort(DataInput input) throws IOException
	{
		return input.readUnsignedShort();
	}

	public static void writeUnsignedShort(int value, DataOutput out) throws IOException
	{
		out.writeShort(value);
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
				value.add(readObject(in));
			}
		}
		return value;
	}

	public static void writeCollection(Class cls, Collection value, DataOutput out) throws IOException
	{
		boolean isUndefinedCollection = false;

		// Check class instead of using instance of to avoid subclasses
		if (value != null) {
			cls = value.getClass();
		}

		if (cls == List.class || cls == ArrayList.class) {
			out.writeByte(ARRAY_LIST);
		} else if (cls == LinkedList.class) {
			out.writeByte(LINKED_LIST);
		} else if (cls == Set.class || cls == HashSet.class) {
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
				writeObject(iterator.next(), out);
			}
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
			case MAP:
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
				map.put(readObject(in), readObject(in));
			}
		}
		return map;
	}

	public static void writeMap(Class cls, Map<?, ?> map, DataOutput out) throws IOException
	{
		boolean isUndefinedMap = false;
		if (map != null) {
			cls = map.getClass();
		}
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
				writeObject(entry.getKey(), out);
				writeObject(entry.getValue(), out);
			}
		}
	}

	public static void writeLinkedList(LinkedList<?> list, DataOutput out) throws IOException
	{
		int size;
		if (list == null) {
			size = -1;
		} else {
			size = list.size();
		}
		writeArrayLength(size, out);
		if (size > 0) {
			for (Object e : list) {
				writeObject(e, out);
			}
		}
	}

	public static HashMap<String, Object> readHashMap(DataInput in) throws IOException, ClassNotFoundException
	{
		int size = readArrayLength(in);
		if (size == -1) {
			return null;
		} else {
			HashMap<String, Object> map = new HashMap<String, Object>(size, 1f);
			for (int i = 0; i < size; i++) {
				try {
					String key = readUTF(in);
					Object value = readObject(in);
					map.put(key, value);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return map;
		}
	}

	public static void writeHashMap(HashMap<String, ?> map, DataOutput out) throws IOException
	{
		int size;
		if (map == null) {
			size = -1;
		} else {
			size = map.size();
		}
		writeArrayLength(size, out);
		if (size > 0) {
			for (Map.Entry<String, ?> entry : map.entrySet()) {
				writeUTF(entry.getKey(), out);
				writeObject(entry.getValue(), out);
			}
		}
	}

	public static Object checkDomainObject(JsonLite jl)
	{
		if (jl == null) {
			return null;
		}
		if (JsonLiteDataSerializer.isServer() == false) {
			KeyType kt = jl.getKeyType();
			if (kt != null) {
				Class clazz = kt.getDomainClass();
				if (clazz != null) {
					try {
						IJsonLiteWrapper dataObject = (IJsonLiteWrapper) clazz.newInstance();
						dataObject.fromJsonLite(jl);
						return dataObject;
					} catch (Exception ex) {
						throw new JsonLiteException(ex);
					}
				}
			}
		}
		return jl;
	}

	public static JsonLite readJsonLite(DataInput input) throws ClassNotFoundException, IOException
	{
		byte header = input.readByte();
		if (header == NULL) {
			return null;
		}
		JsonLite jl = new JsonLite();
		jl.readData(input);
		return jl;
	}

	public static void writeJsonLite(JsonLite value, DataOutput output) throws IOException
	{
		if (value == null) {
			output.writeByte(NULL);
		} else {
			output.writeByte(JSON_LITE);
			value.writeData(output);
		}
	}
}
