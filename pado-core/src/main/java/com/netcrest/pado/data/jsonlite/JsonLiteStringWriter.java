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
package com.netcrest.pado.data.jsonlite;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.internal.JsonLiteHelper;

public class JsonLiteStringWriter<V>
{	
	private KeyType keyType;
	
	/**
	 * Write the contents of the JSONObject as JSON text to a writer. For
	 * compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return The writer.
	 * @throws JsonLiteException
	 */
	Writer write(Writer writer, JsonLite<V> jl, int indentFactor, int indent, boolean isReference, boolean isHeader)
			throws JsonLiteException
	{
		if (jl == null) {
			return writer;
		}
		this.keyType = jl.getKeyType();
		if (keyType == null) {
			writeValueMap(writer, jl, indentFactor, indent, isReference, isHeader);
		} else {
			writeKeyType(writer, jl, indentFactor, indent, isReference, isHeader);
		}
		return writer;
	}

	Writer writeValueMap(Writer writer, JsonLite<V> jl, int indentFactor, int indent, boolean isReference, boolean isHeader)
			throws JsonLiteException
	{
		if (jl == null) {
			return writer;
		}
		try {
			boolean commanate = false;
			final int length = jl.valueMap.size();
			String[] keys = jl.valueMap.keySet().toArray(new String[length]);
			writer.write('{');

			if (length == 1) {
				String key = keys[0];
				writer.write(JsonLiteHelper.quote(key));
				writer.write(':');
				if (indentFactor > 0) {
					writer.write(' ');
				}
				writeJsonStringValue(writer, jl.get(key), indentFactor, indent, isReference, isHeader);
			} else if (length != 0) {
				final int newindent = indent + indentFactor;
				for (String key : keys) {
					if (commanate) {
						writer.write(',');
					}
					if (indentFactor > 0) {
						writer.write('\n');
					}
					indent(writer, newindent);
					writer.write(JsonLiteHelper.quote(key));
					writer.write(':');
					if (indentFactor > 0) {
						writer.write(' ');
					}
					writeJsonStringValue(writer, jl.get(key), indentFactor, newindent, isReference, isHeader);
					commanate = true;
				}
				if (indentFactor > 0) {
					writer.write('\n');
				}
				indent(writer, indent);
			}
			writer.write('}');
			return writer;
		} catch (IOException exception) {
			throw new JsonLiteException(exception);
		}
	}

	Writer writeKeyType(Writer writer, JsonLite jl, int indentFactor, int indent, boolean isReference, boolean isHeader)
			throws JsonLiteException
	{
		if (jl == null) {
			return writer;
		}
		try {
			boolean commanate = false;
			final int length = jl.size();
			KeyType keyTypes[] = jl.getKeyType().getValues();

			if (isHeader) {
				int headerIndent = indent;
				if (indentFactor > 0) {
					headerIndent += indentFactor;
				}
				writeHeaderOpen(writer, indentFactor, headerIndent);
				writer.write("\"c\":");
				if (indentFactor > 0) {
					writer.write(' ');
				}
				writer.write("\"L");
				writer.write(jl.getKeyType().getClass().getCanonicalName());
				writer.write('"');
				writeHeaderClose(writer, indentFactor, headerIndent);
			} else {
				writer.write('{');
			}

			if (length == 1) {
				if (isHeader) {
					writer.write(',');
				}
				KeyType key = keyTypes[0];
				writer.write(JsonLiteHelper.quote(key.toString()));
				writer.write(':');
				if (indentFactor > 0) {
					writer.write(' ');
				}
				Object value;
				if (isReference && key.isReference()) {
					value = jl.getReference(key);
					if (value == null) {
						value = jl.get(key);
					}
				} else {
					value = jl.get(key);
				}
				writeJsonStringValue(writer, value, indentFactor, indent, isReference, isHeader);
			} else if (length != 0) {
				if (isHeader) {
					writer.write(',');
				}
				final int newindent = indent + indentFactor;
				for (KeyType keyType : keyTypes) {
					if (commanate) {
						writer.write(',');
					}
					if (indentFactor > 0) {
						writer.write('\n');
					}
					indent(writer, newindent);
					writer.write(JsonLiteHelper.quote(keyType.getName()));
					writer.write(':');
					if (indentFactor > 0) {
						writer.write(' ');
					}
					Object value;
					if (isReference && keyType.isReference()) {
						value = jl.getReference(keyType);
						if (value == null) {
							value = jl.get(keyType);
						}
					} else {
						value = jl.get(keyType);
					}
					writeJsonStringValue(writer, value, indentFactor, newindent, isReference, isHeader);
					commanate = true;
				}
				if (indentFactor > 0) {
					writer.write('\n');
				}
				indent(writer, indent);
			}
			writer.write('}');
			return writer;
		} catch (IOException exception) {
			throw new JsonLiteException(exception);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	Writer writeJsonStringValue(Writer writer, Object value, int indentFactor, int indent, boolean isReference,
			boolean isHeader) throws JsonLiteException, IOException
	{
		if (value == null || value.equals(null)) {
			writer.write("null");
		} else if (value instanceof JsonLite) {
			((JsonLite) value).deserialize();
			write(writer, (JsonLite)value, indentFactor, indent, isReference, isHeader);
		} else if (value.getClass().isEnum()) {
			JsonLiteHelper.quote(value.toString(), writer);
		} else if (value instanceof Map) {
			writeMap(writer, indentFactor, indent, (Map) value, isReference, isHeader);
		} else if (value instanceof Collection) {
			writeCollection(writer, indentFactor, indent, (Collection) value, isReference, isHeader);
		} else if (value.getClass().isArray()) {
			writeArray(writer, indentFactor, indent, value, isReference, isHeader);
		} else if (value instanceof Number) {
			writer.write(JsonLiteHelper.numberToString((Number) value));
		} else if (value instanceof Boolean) {
			writer.write(value.toString());
		} else if (value instanceof Date) {
			writer.write(JsonLiteHelper.quote(JsonLiteHelper.iso8601DateFormat.format((Date) value)));
		} else if (value instanceof IJsonString) {
			Object o;
			try {
				o = ((IJsonString) value).toJsonString();
			} catch (Exception e) {
				throw new JsonLiteException(e);
			}
			writer.write(o != null ? o.toString() : JsonLiteHelper.quote(value.toString()));
		} else if (value instanceof String || value instanceof Byte || value instanceof Character) {
			JsonLiteHelper.quote(value.toString(), writer);
		} else {
			writeJsonStringObject(writer, indentFactor, indent, value, isReference, isHeader);
		}
		return writer;
	}

	@SuppressWarnings("rawtypes")
	Writer writeType(Writer writer, Class type) throws JsonLiteException, IOException
	{
		if (type == null) {
			return writer;
		} else if (type.isArray()) {
			return writeArrayType(writer, type);
		}

		writer.write('"');
		writer.write(JsonLiteTokenizer.getTypeName(type));
		writer.write('"');
		return writer;
	}

	@SuppressWarnings("rawtypes")
	Writer writeArrayType(Writer writer, Class type) throws JsonLiteException, IOException
	{
		if (type == null || type.isArray() == false) {
			return writer;
		}
		type = type.getComponentType();
		writer.write("\"[");
		writer.write(JsonLiteTokenizer.getTypeName(type));
		writer.write('"');
		return writer;
	}

	static final void indent(Writer writer, int indent) throws IOException
	{
		for (int i = 0; i < indent; i += 1) {
			writer.write(' ');
		}
	}

	/**
	 * Writes the first part of the header, i.e., {"__h":{.
	 * 
	 * @param writer
	 * @param indentFactor
	 * @param indent
	 * @return writer
	 * @throws IOException
	 */
	private Writer writeHeaderOpen(Writer writer, int indentFactor, int indent) throws IOException
	{
		// {
		writer.write('{');
		if (indentFactor > 0) {
			writer.write('\n');
			indent(writer, indent);
		}

		// "__h": {
		writer.write(JsonLiteHelper.quote("__h"));
		writer.write(':');
		if (indentFactor > 0) {
			writer.write(' ');
		}
		writer.write('{');
		if (indentFactor > 0) {
			writer.write('\n');
			indent += indentFactor;
		}
		indent(writer, indent);

		return writer;
	}

	Writer writeHeaderClose(Writer writer, int indentFactor, int indent) throws IOException
	{
		if (indentFactor > 0) {
			writer.write('\n');
			indent(writer, indent);
		}
		writer.write('}');
		return writer;
	}

	@SuppressWarnings("rawtypes")
	private String getGenericType(Collection collection)
	{
		if (collection == null) {
			return null;
		}
		Iterator iterator = collection.iterator();
		if (iterator.hasNext() == false) {
			return null;
		}
		Object value = iterator.next();
		Class type = value.getClass();
		while (iterator.hasNext()) {
			value = iterator.next();
			if (value.getClass() != type) {
				return null;
			}
		}
		if (value instanceof IJsonLiteWrapper) {
			String className = ((IJsonLiteWrapper) value).toJsonLite().getKeyType().getClass().getCanonicalName();
			return className;
		}
		return JsonLiteTokenizer.getTypeName(type);
	}

	/**
	 * Write the contents of the JSONArray as JSON text to a writer. For
	 * compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @param indent
	 *            The indentation of the top level.
	 * @return The writer.
	 * @throws JsonLiteException
	 */
	Writer writeArray(Writer writer, int indentFactor, int indent, Object array, boolean isReference,
			boolean isHeader) throws JsonLiteException
	{
		try {
			boolean commanate = false;
			int length = Array.getLength(array);
			int newindent = indent + indentFactor;

			if (keyType != null && isHeader) {
				// {
				// "__h": {
				// "c": "[L",
				// "t": ["Z", "B", "C", "D", "F", "I", "J", "L", "S", "["],
				// "d": ["true", 1, \u2f3a, 1.1, 2.0, 1, 1, {}, 1, []]
				// }
				// }

				// {
				writeHeaderOpen(writer, indentFactor, newindent);
				newindent += indentFactor;

				// "c": "[L",
				writer.write("\"c\"");
				writer.write(':');
				if (indentFactor > 0) {
					writer.write(' ');
				}
				writeArrayType(writer, array.getClass());
				writer.write(',');
				if (indentFactor > 0) {
					writer.write('\n');
					indent(writer, newindent);
				}

				// "t": ["Z", "B", "C", "D", "F", "I", "J", "L", "S", "["],
				if (array.getClass() == Object[].class) {
					writer.write("\"t\":");
					if (indentFactor > 0) {
						writer.write(' ');
					}
					writer.write('[');
					int len = Array.getLength(array);
					commanate = false;
					for (int i = 0; i < len; i++) {
						if (commanate) {
							writer.write(',');
						}
						Object value = Array.get(array, i);
						writeType(writer, value.getClass());
						commanate = true;
					}
					writer.write(']');
					writer.write(',');
					if (indentFactor > 0) {
						writer.write('\n');
						indent(writer, newindent);
					}

				}

				// "d":
				writer.write("\"d\":");
				if (indentFactor > 0) {
					writer.write(' ');
					newindent += indentFactor;
				}
			}

			writer.write('[');
			if (length == 1) {
				writeJsonStringValue(writer, Array.get(array, 0), indentFactor, newindent, isReference, isHeader);
			} else if (length != 0) {
				commanate = false;
				for (int i = 0; i < length; i += 1) {
					if (commanate) {
						writer.write(',');
					}
					if (indentFactor > 0) {
						writer.write('\n');
					}
					indent(writer, newindent);
					writeJsonStringValue(writer, Array.get(array, i), indentFactor, newindent, isReference, isHeader);
					commanate = true;
				}
				if (indentFactor > 0) {
					writer.write('\n');
				}
				indent(writer, newindent - indentFactor);
			}
			writer.write(']');

			// }
			if (keyType != null && isHeader) {
				if (indentFactor > 0) {
					writer.write('\n');
					indent(writer, indent + indentFactor);
				}
				writer.write('}');
				writeHeaderClose(writer, indentFactor, indent);
			}

			return writer;
		} catch (IOException e) {
			throw new JsonLiteException(e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	Writer writeCollection(Writer writer, int indentFactor, int indent, Collection collection, boolean isReference,
			boolean isHeader) throws JsonLiteException
	{
		try {
			boolean commanate = false;
			int length = collection.size();
			int newindent = indent + indentFactor;
			Iterator iterator = collection.iterator();
			Class type = collection.getClass();

			if (keyType != null && isHeader) {
				// {
				// "__h": {
				// "c": "List",
				// "t": ["Z", "B", "C", "D", "F", "I", "J", "L", "S", "["],
				// "d": ["true", 1, \u2f3a, 1.1, 2.0, 1, 1, {}, 1, []]
				// }
				// }

				// {
				writeHeaderOpen(writer, indentFactor, newindent);
				newindent += indentFactor;

				// "c": "List",
				writer.write("\"c\"");
				writer.write(':');
				if (indentFactor > 0) {
					writer.write(' ');
				}
				writer.write('"');
				if (type == ArrayList.class) {
					writer.write("List");
				} else {
					writer.write('L');
					writer.write(type.getCanonicalName());
				}
				String genericType = getGenericType(collection);
				if (genericType != null) {
					writer.write('<');
					writer.write(genericType);
					writer.write('>');
				}
				writer.write('"');
				writer.write(',');
				if (indentFactor > 0) {
					writer.write('\n');
					indent(writer, newindent);
				}

				// "t": ["Z", "B", "C", "D", "F", "I", "J", "L", "S", "["],
				if (genericType == null) {
					writer.write("\"t\":");
					if (indentFactor > 0) {
						writer.write(' ');
					}
					writer.write('[');
					commanate = false;
					Iterator iterator2 = collection.iterator();
					while (iterator2.hasNext()) {
						if (commanate) {
							writer.write(',');
						}
						Object value = iterator2.next();
						writeType(writer, value.getClass());
						commanate = true;
					}
					writer.write(']');
					writer.write(',');
					if (indentFactor > 0) {
						writer.write('\n');
						indent(writer, newindent);
					}
				}

				// "d":
				writer.write("\"d\":");
				if (indentFactor > 0) {
					writer.write(' ');
					newindent += indentFactor;
				}
			}

			writer.write('[');

			if (length == 1) {
				writeJsonStringValue(writer, iterator.next(), indentFactor, indent, isReference, isHeader);
			} else if (length != 0) {
				commanate = false;
				while (iterator.hasNext()) {
					if (commanate) {
						writer.write(',');
					}
					if (indentFactor > 0) {
						writer.write('\n');
					}
					indent(writer, newindent);
					writeJsonStringValue(writer, iterator.next(), indentFactor, newindent, isReference, isHeader);
					commanate = true;
				}
				if (indentFactor > 0) {
					writer.write('\n');
				}
				indent(writer, newindent - indentFactor);
			}
			writer.write(']');

			// }
			if (keyType != null && isHeader) {
				if (indentFactor > 0) {
					writer.write('\n');
					indent(writer, indent + indentFactor);
				}
				writer.write('}');
				writeHeaderClose(writer, indentFactor, indent);
			}

			return writer;
		} catch (IOException e) {
			throw new JsonLiteException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private Writer writeMap(Writer writer, int indentFactor, int indent, Map map, boolean isReference, boolean isHeader)
			throws JsonLiteException
	{
		try {
			boolean commanate = false;
			final int length = map.size();
			int newindent = indent + indentFactor;
			Iterator keys = map.keySet().iterator();
			Class type = map.getClass();

			if (keyType != null && isHeader) {
				// {
				// "__h": {
				// "c": "Map",
				// }
				// }

				// {
				writeHeaderOpen(writer, indentFactor, newindent);

				// "c": "Map",
				writer.write("\"c\"");
				writer.write(':');
				if (indentFactor > 0) {
					writer.write(' ');
				}
				writer.write('"');
				if (type == HashMap.class) {
					writer.write("Map");
				} else {
					writer.write('L');
					writer.write(type.getCanonicalName());
				}
				writer.write('"');
				writeHeaderClose(writer, indentFactor, newindent);
				if (length != 0) {
					writer.write(',');
				}
			} else {
				writer.write('{');
			}

			if (length == 1) {
				Object key = keys.next();
				writer.write(JsonLiteHelper.quote(key.toString()));
				writer.write(':');
				if (indentFactor > 0) {
					writer.write(' ');
				}
				writeJsonStringValue(writer, map.get(key), indentFactor, indent, isReference, isHeader);
			} else if (length != 0) {
				while (keys.hasNext()) {
					Object key = keys.next();
					if (commanate) {
						writer.write(',');
					}
					if (indentFactor > 0) {
						writer.write('\n');
					}
					indent(writer, newindent);
					writer.write(JsonLiteHelper.quote(key.toString()));
					writer.write(':');
					if (indentFactor > 0) {
						writer.write(' ');
					}
					writeJsonStringValue(writer, map.get(key), indentFactor, newindent, isReference, isHeader);
					commanate = true;
				}
				if (indentFactor > 0) {
					writer.write('\n');
				}
				indent(writer, indent);
			}
			writer.write('}');

			return writer;
		} catch (IOException exception) {
			throw new JsonLiteException(exception);
		}
	}

	@SuppressWarnings("rawtypes")
	private Writer writeJsonStringObject(Writer writer, int indentFactor, int indent, Object object,
			boolean isReference, boolean isHeader) throws JsonLiteException
	{
		try {
			int newindent = indent + indentFactor;

			if (isHeader) {
				// ----- Header
				// {
				// "__h": {
				// "c": "[Lcom.netcrest.test.data.Foo"
				// }

				// {
				// "__h": {
				writeHeaderOpen(writer, indentFactor, newindent);

				// "c": "[Lcom.netcrest.test.data.Foo"
				writer.write("\"c\"");
				writer.write(':');
				if (indentFactor > 0) {
					writer.write(' ');
				}
				writer.write('"');
				writer.write('L');
				if (object instanceof IJsonLiteWrapper) {
					writer.write(((IJsonLiteWrapper) object).toJsonLite().getKeyType().getClass().getCanonicalName());
				} else {
					writer.write(object.getClass().getCanonicalName());
				}
				writer.write('"');

				// "}"
				writeHeaderClose(writer, indentFactor, newindent);
			} else {
				writer.write('{');
			}

			// --------- Body
			Method[] methods = object.getClass().getMethods();
			int count = 0;
			for (Method method : methods) {
				if (method.getParameterTypes().length == 0 && method.getReturnType() != void.class
						&& (method.getName().startsWith("get") || method.getName().startsWith("is"))) {
					if (method.getName().equals("getClass")) {
						continue;
					}
					if (isHeader || count > 0) {
						writer.write(',');
					}
					count++;
					if (indentFactor > 0) {
						writer.write('\n');
					}
					indent(writer, newindent);

					Object value;
					try {
						value = method.invoke(object);
					} catch (Exception e) {
						throw new JsonLiteException(e);
					}
					String propertyName;
					if (method.getName().startsWith("get")) {
						propertyName = method.getName().substring(3);
					} else {
						propertyName = method.getName().substring(2);
					}
					writer.write('"');
					writer.write(propertyName);
					writer.write("\":");
					if (indentFactor > 0) {
						writer.write(' ');
					}
					writeJsonStringValue(writer, value, indentFactor, newindent, isReference, isHeader);
				}
			}

			if (indentFactor > 0) {
				writer.write('\n');
				indent(writer, indent);
			}
			writer.write('}');

			return writer;
		} catch (IOException e) {
			throw new JsonLiteException(e);
		}
	}
}
