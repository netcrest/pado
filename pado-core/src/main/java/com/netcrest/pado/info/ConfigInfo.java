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
package com.netcrest.pado.info;

import java.util.List;
import java.util.Properties;

import com.netcrest.pado.io.IObjectSerializer;

/**
 * ConfigInfo provides client configuration information determined by the server
 * during client login.
 * 
 * @author dpark
 * 
 */
public abstract class ConfigInfo
{
	/**
	 * Server configuration file content (normally in XML file)
	 */
	protected String xmlContent;
	
	/**
	 * Client Index Matrix connection name
	 */
	protected String clientIndexMatrixConnectionName;
	
	/**
	 * Locators that clients used to connect to the grid
	 */
	protected String clientLocators;
	
	/**
	 * Object serializer list. 
	 */
	protected List<IObjectSerializer> objectSerializerList;
	
	/**
	 * Configuration properties
	 */
	protected Properties configProperties = new Properties();

	/**
	 * Constructs an empty ConfigInfo object.
	 */
	public ConfigInfo()
	{
	}

	/**
	 * Sets the client XML-based configuration content.
	 * 
	 * @param xmlContent
	 */
	public void setXmlContent(String xmlContent)
	{
		this.xmlContent = xmlContent;
	}

	/**
	 * Returns the XML content.
	 */
	public String getXmlContent()
	{
		return xmlContent;
	}

	/**
	 * Returns the client index matrix connection name which maps to a
	 * pre-configured connection point to communicate with the index matrix
	 * provider grid which is typically the parent grid.
	 */
	public String getClientIndexMatrixConnectionName()
	{
		return clientIndexMatrixConnectionName;
	}

	/**
	 * Returns the index matrix connection name.
	 * 
	 * @param clientIndexMatrixConnectionName
	 *            Connection name
	 */
	public void setClientIndexMatrixConnectionName(String clientIndexMatrixConnectionName)
	{
		this.clientIndexMatrixConnectionName = clientIndexMatrixConnectionName;
	}

	/**
	 * Returns the client locators. A logged in client internally uses these
	 * locators to connect to the parent grid.
	 */
	public String getClientLocators()
	{
		return clientLocators;
	}

	/**
	 * Sets the client locators.
	 * 
	 * @param clientLocators
	 */
	public void setClientLocators(String clientLocators)
	{
		this.clientLocators = clientLocators;
	}

	/**
	 * Returns the list of IObjectSerializer objects that may depend on the
	 * underlying data grid product for optimally serializing Pado's internal
	 * objects.
	 */
	public List<IObjectSerializer> getObjectSerializerList()
	{
		return objectSerializerList;
	}

	/**
	 * Sets the list of IObjectSerializer objects that serializes objects in the
	 * third party native format.
	 * 
	 * @param objectSerializerList
	 *            A set of IObjectSerializer objects
	 */
	public void setObjectSerializerSet(List<IObjectSerializer> objectSerializerList)
	{
		this.objectSerializerList = objectSerializerList;
	}
	
	public String getPropert(String key)
	{
		return configProperties.getProperty(key);
	}
	
	public void setProperty(String key, String value)
	{
		configProperties.setProperty(key, value);
	}
	
	public Properties getProperties()
	{
		return configProperties;
	}

//	/**
//	 * Returns the user context class name. User context object must be included
//	 * for every IBiz operation.
//	 */
//	public String getUserContextClassName()
//	{
//		return userContextClassName;
//	}
//
//	/**
//	 * Sets the user context class name. User context object must be included
//	 * for every IBiz operation.
//	 * 
//	 * @param userContextClassName
//	 *            Fully-qualified IUserContext implementation class name.
//	 */
//	public void setUserContextClassName(String userContextClassName)
//	{
//		this.userContextClassName = userContextClassName;
//	}
//
//	/**
//	 * Returns the data context class name. This it an optional context class
//	 * that must implement {@link IDataContext}
//	 */
//	public String getDataContextClassName()
//	{
//		return dataContextClassName;
//	}
//
//	/**
//	 * Sets the data context class name. This it an optional context class that
//	 * must implement {@link IDataContext}
//	 * 
//	 * @param dataContextClassName
//	 *            Fully-qualified {@link IDataContext} implementation class
//	 *            name.
//	 */
//	public void setDataContextClassName(String dataContextClassName)
//	{
//		this.dataContextClassName = dataContextClassName;
//	}
}
