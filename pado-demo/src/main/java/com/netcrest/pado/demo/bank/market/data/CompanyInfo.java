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
package com.netcrest.pado.demo.bank.market.data;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.gemstone.gemfire.DataSerializable;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.gemfire.util.DataSerializerEx;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CompanyInfo implements DataSerializable
{
	private static final long serialVersionUID = 1L;

	private String secId;
	private String company;
	private String country;
	private String sector;

	private int gics;

	public CompanyInfo()
	{
	}

	public static CompanyInfo[] loadCompnayInfo(File file) throws IOException
	{
		ArrayList companyInfoList = new ArrayList();
		FileReader reader = new FileReader(file);
		BufferedReader fileReader = new BufferedReader(reader);
		// skip first line - header
		String line = fileReader.readLine();
		line = fileReader.readLine();
		while (line != null) {
			if (line.trim().length() == 0) {
				continue;
			}
			CompanyInfo companyInfo = new CompanyInfo();
			companyInfo.setLine(line);
			companyInfoList.add(companyInfo);
			line = fileReader.readLine();
		}
		reader.close();

		return (CompanyInfo[]) companyInfoList.toArray(new CompanyInfo[0]);
	}

	public static CompanyInfo[] loadCompnayInfo(File file, IGridMapBiz gridMapBiz) throws IOException
	{
		ArrayList companyInfoList = new ArrayList();
		FileReader reader = new FileReader(file);
		BufferedReader fileReader = new BufferedReader(reader);
		// skip first line - header
		String line = fileReader.readLine();
		line = fileReader.readLine();
		while (line != null) {
			if (line.trim().length() == 0) {
				continue;
			}
			CompanyInfo companyInfo = new CompanyInfo();
			companyInfo.setLine(line);
			try {
				gridMapBiz.put(companyInfo.secId, companyInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
			companyInfoList.add(companyInfo);
			line = fileReader.readLine();
		}
		reader.close();
		return (CompanyInfo[]) companyInfoList.toArray(new CompanyInfo[0]);
	}

	private static KeyMap newCompanyInfo(KeyType keyType)
	{
		KeyMap companyInfo = new JsonLite(keyType);
		return companyInfo;
	}

	public static KeyMap[] loadCompnayInfoKeyMap(File file, IGridMapBiz gridMapBiz)
			throws IOException, NoSuchFieldException
	{
		ArrayList<KeyMap> companyInfoList = new ArrayList();
		FileReader reader = new FileReader(file);
		BufferedReader fileReader = new BufferedReader(reader);
		// skip first line - header
		String line = fileReader.readLine();
		line = fileReader.readLine();
		while (line != null) {
			if (line.trim().length() == 0) {
				continue;
			}
			KeyMap companyInfo = newCompanyInfo(CompanyInfoKeyType.getKeyType());
			String split[] = line.split("\t+");
			companyInfo.put(CompanyInfoKeyType.KSecId, split[0].trim().toUpperCase());
			companyInfo.put(CompanyInfoKeyType.KCompany, split[1].trim());
			companyInfo.put(CompanyInfoKeyType.KCountry, split[2].trim());
			companyInfo.put(CompanyInfoKeyType.KGics, Integer.parseInt(split[3].trim()));
			companyInfo.put(CompanyInfoKeyType.KSector, split[4].trim());
			gridMapBiz.put(companyInfo.get(CompanyInfoKeyType.KSecId), companyInfo);
			companyInfoList.add(companyInfo);
			line = fileReader.readLine();
		}
		reader.close();
		return (KeyMap[]) companyInfoList.toArray(new KeyMap[0]);
	}

	public CompanyInfo(String secId, String company)
	{
		this.secId = secId;
		this.company = company;
	}

	private void setLine(String line)
	{
		if (line == null) {
			return;
		}
		String split[] = line.split("\t+");
		secId = split[0].trim().toUpperCase();
		company = split[1].trim();
		country = split[2].trim();
		gics = Integer.parseInt(split[3].trim());
		sector = split[4].trim();
	}

	public String getSecId()
	{
		return secId;
	}

	public void setSecId(String secId)
	{
		this.secId = secId;
	}

	public String getCompany()
	{
		return company;
	}

	public void setCompany(String company)
	{
		this.company = company;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getSector()
	{
		return sector;
	}

	public void setSector(String sector)
	{
		this.sector = sector;
	}

	public int getGics()
	{
		return gics;
	}

	public void setGics(int gics)
	{
		this.gics = gics;
	}

	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		secId = DataSerializerEx.readUTF(input);
		company = DataSerializerEx.readUTF(input);
		country = DataSerializerEx.readUTF(input);
		sector = DataSerializerEx.readUTF(input);
		gics = input.readInt();
	}

	public void toData(DataOutput output) throws IOException
	{
		DataSerializerEx.writeUTF(secId, output);
		DataSerializerEx.writeUTF(company, output);
		DataSerializerEx.writeUTF(country, output);
		DataSerializerEx.writeUTF(sector, output);

		output.writeInt(gics);
	}
}
