package com.netcrest.pado.data.jsonlite;

import java.lang.reflect.Array;

import com.netcrest.pado.data.jsonlite.JsonLite;

/**
 * JsonLiteType is an enum class providing all of the JSON supported types in
 * JsonLite representation.
 * 
 * @author dpark
 *
 */
public enum JsonLiteType 
{	
	STRING("String", String.class), 
	NUMBER("Number", Number.class), 
	JSON("Json", JsonLite.class), 
	ARRAY("Array", Array.class), 
	BOOLEAN("Boolean", Boolean.class), 
	NULL("Null", null);

	private JsonLiteType(String name, Class<?> type)
	{
		this.name = name;
		this.type = type;
	}

	private String name;
	private Class<?> type;

	public String getName()
	{
		return name;
	}

	public Class<?> getType()
	{
		return type;
	}

	public String toString()
	{
		return name;
	}
}
