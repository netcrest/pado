package com.netcrest.pado.test.junit.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.netcrest.pado.data.jsonlite.JsonLite;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JsonFileLoaderTest
{
	private static String filePath = "tmp/jsonlite_dump.json";
	
	private JsonLite createJsonLite(int num)
	{
		JsonLite jl = new JsonLite();
		jl.put("Str" + num, "Value" + num);
		jl.put("Num" + num, num);
		return jl;
	}
	
	private void dump(List<JsonLite> list) throws FileNotFoundException
	{
		int indent = 4;
		File file = new File(filePath);
		if (file.getParentFile().exists() == false) {
			file.getParentFile().mkdirs();
		}
		PrintWriter jsonListWriter = new PrintWriter(file);
		jsonListWriter.println("[");
		int i = 0;
		for (JsonLite jl : list) {
			if (i > 0) {
				jsonListWriter.println(",");
			}
			jsonListWriter.print(jl.toString(indent, false, false));
			i++;
		}
		if (list.size() > 0) {
			jsonListWriter.println();
		}
		jsonListWriter.println("]");
		jsonListWriter.close();
	}
	
	@Test
	public void testWriteJsonLiteObjects() throws FileNotFoundException
	{
		List<JsonLite> list = new ArrayList();
		int count = 10;
		for (int j = 0; j < count; j++) {
			JsonLite jl = createJsonLite(j);
			list.add(jl);
		}
		
		dump(list);
	}
	
	@Test
	public void testReadJsonLiteObjects() throws IOException
	{
		int BUFFER_SIZE = 10;
		File file = new File(filePath);
		FileReader reader = new FileReader(file);
		char[] cbuf = new char[BUFFER_SIZE];
		int offset = 0;
		int length = BUFFER_SIZE;
		int charsRead = -1;
		while ((charsRead = reader.read(cbuf, offset, length)) > 0) {
			for (int i = 0; i < charsRead; i++) {
				
			}
		}
		
		
		
		List<JsonLite> list = new ArrayList();
		int count = 10;
		for (int j = 0; j < count; j++) {
			JsonLite jl = createJsonLite(j);
			list.add(jl);
		}
		
		dump(list);
	}
}
