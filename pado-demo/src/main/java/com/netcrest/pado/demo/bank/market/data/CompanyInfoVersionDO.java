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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.gemstone.gemfire.DataSerializable;
import com.netcrest.pado.gemfire.util.DataSerializerEx;

/**
 * CompanyInfoVersionDO uses versionID to handle class versions by
 * correctly reading and writing attributes.
 */
public class CompanyInfoVersionDO implements DataSerializable
{
	/**
	 * Version ID
	 */
	private static final long serialVersionUID = 1L;
	
	private String secId;
	private String company;
	private String country;
	private String sector;
	private int gics;
	
	// attributes added to versionID=2
	private String url;
	
	private short versionID = 2;
	
	public CompanyInfoVersionDO()
	{
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



	public String getUrl()
	{
		return url;
	}



	public void setUrl(String url)
	{
		this.url = url;
	}



	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		versionID = input.readShort();
		
		switch (versionID) {
		case 1:
			secId = DataSerializerEx.readUTF(input);
			company = DataSerializerEx.readUTF(input);
			country = DataSerializerEx.readUTF(input);
			sector = DataSerializerEx.readUTF(input);
			gics = input.readInt();
			break;
		case 2:
			secId = DataSerializerEx.readUTF(input);
			company = DataSerializerEx.readUTF(input);
			country = DataSerializerEx.readUTF(input);
			sector = DataSerializerEx.readUTF(input);
			gics = input.readInt();
			url = DataSerializerEx.readUTF(input); // add a new field for version 2
			break;
		}
	}

	public void toData(DataOutput output) throws IOException
	{
		output.writeShort(versionID);
		switch (versionID) {
		case 1:
			DataSerializerEx.writeUTF(secId, output);
			DataSerializerEx.writeUTF(company, output);
			DataSerializerEx.writeUTF(country, output);
			DataSerializerEx.writeUTF(sector, output);
			output.writeInt(gics);
			break;
		case 2:
			DataSerializerEx.writeUTF(secId, output);
			DataSerializerEx.writeUTF(company, output);
			DataSerializerEx.writeUTF(country, output);
			DataSerializerEx.writeUTF(sector, output);
			output.writeInt(gics);
			DataSerializerEx.writeUTF(url, output); // added in version 2
			break;
		}
	}
}
