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
package com.netcrest.pado.index.provider.lucene;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import com.gemstone.gemfire.internal.util.BlobHelper;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.ITemporalKey;

public class LuceneField
{
	public LuceneField()
	{
	}

	public Document createDocument()
	{
		Document doc = new Document();
		return doc;
	}

	public IndexableField createIdentityKeyField(Object identityKey) throws IOException
	{
		if (identityKey == null) {
			return null;
		}
		StoredField field = new StoredField("IdentityKey", BlobHelper.serializeToBlob(identityKey));
//		Field field = new Field("identity", BlobHelper.serializeToBlob(identityKey));
		return field;
	}

	public IndexableField createField(String name, String value)
	{
		if (name == null || value == null) {
			return null;
		}
		TextField field = new TextField(name, value.toLowerCase(), Field.Store.YES);
//		Field field = new Field(name, value, Field.Store.YES, Field.Index.ANALYZED);
		return field;
	}
	
	public IndexableField creteDateField(String name, Date date)
	{
		return createDateField(name, date, DateTool.Resolution.MILLISECOND);
	}
	
	public IndexableField createDateField(String name, Date date, DateTool.Resolution res)
	{
		if (name == null || date == null) {
			return null;
		}
//		String str = DateTool.dateToString(date, res);
		SimpleDateFormat sdf = (SimpleDateFormat)res.format.clone();
		String str = sdf.format(date);
		StringField field = new StringField(name, str, Field.Store.YES);
//		Field field = new Field(name, str, Field.Store.YES, Field.Index.ANALYZED);
		return field;
	}
	
	public IndexableField createDateField(String name, Date date, SimpleDateFormat format)
	{
		if (name == null || date == null || format == null) {
			return null;
		}
		String str = format.format(date);
		StringField field = new StringField(name, str, Field.Store.YES);
//		Field field = new Field(name, str, Field.Store.YES, Field.Index.ANALYZED);
		return field;
	}
	
	public IndexableField createDateField(String name, long time)
	{
		return createDateField(name, time, DateTool.Resolution.MILLISECOND);
	}
	
	public IndexableField createDateField(String name, long time, SimpleDateFormat format)
	{
		if (name == null || format == null) {
			return null;
		}
		String str = format.format(new Date(time));
		StringField field = new StringField(name, str, Field.Store.YES);
//		Field field = new Field(name, str, Field.Store.YES, Field.Index.ANALYZED);
		return field;	
	}
	
	public IndexableField createDateField(String name, long time, DateTool.Resolution res)
	{
		if (name == null) {
			return null;
		}
		try {
			SimpleDateFormat sdf = (SimpleDateFormat)res.format.clone();
		String str = sdf.format(new Date(time));
		StringField field = new StringField(name, str, Field.Store.YES);
//		Field field = new Field(name, str, Field.Store.YES, Field.Index.ANALYZED);
		return field;
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	
	public IndexableField createField(String name, int value)
	{
		if (name == null) {
			return null;
		}
		StoredField field = new StoredField(name, value);
//		NumericField field = new NumericField(name, Field.Store.YES, true);
//		field.setIntValue(value);
		return field;
	}
	
	public IndexableField createField(String name, long value)
	{
		if (name == null) {
			return null;
		}
		StoredField field = new StoredField(name, value);
//		NumericField field = new NumericField(name, Field.Store.YES, true);
//		field.setLongValue(value);
		return field;
	}
	
	public IndexableField createField(String name, float value)
	{
		if (name == null) {
			return null;
		}
		StoredField field = new StoredField(name, value);
//		NumericField field = new NumericField(name, Field.Store.YES, true);
//		field.setFloatValue(value);
		return field;
	}
	
	public IndexableField createField(String name, double value)
	{
		if (name == null) {
			return null;
		}
		StoredField field = new StoredField(name, value);
//		NumericField field = new NumericField(name, Field.Store.YES, true);
//		field.setDoubleValue(value);
		return field;
	}
	
	public IndexableField createField(String name, byte[] binary)
	{
		if (name == null) {
			return null;
		}
		StoredField field = new StoredField(name, binary);
//		Field field = new Field(name, binary);
		return field;
	}

	public void addField(Document doc, IndexableField field)
	{
		doc.add(field);
	}
	
	@SuppressWarnings("rawtypes")
	public ITemporalKey getTemporalKey(Document doc)
	{
		if (doc == null) {
			return null;
		}
		IndexableField field = doc.getField("TemporalKey");
		if (field == null) {
			return null;
		}
		ITemporalKey temporalKey = null;
		BytesRef br = field.binaryValue();
		if (br != null) {
			byte[] blob = br.bytes;
			try {
				temporalKey = (ITemporalKey) BlobHelper.deserializeBlob(blob);
			} catch (Exception ex) {
				Logger.warning("TemporalKey key deserialization error", ex);
			}
		} else {
			Logger.warning("Invalid temporal key type found in Lucene index. Must be binary. [field=" + field
					+ "]");
		}
		
		return temporalKey;
	}
}
