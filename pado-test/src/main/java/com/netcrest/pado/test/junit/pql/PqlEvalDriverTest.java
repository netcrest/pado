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
package com.netcrest.pado.test.junit.pql;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.internal.pql.antlr4.PqlEvalDriver;

@SuppressWarnings({ "rawtypes" })
public class PqlEvalDriverTest
{
	static IPado pado;
	static ITemporalBiz temporalBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException, Exception
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "test1", "test123".toCharArray());
		ICatalog catalog = pado.getCatalog();
		temporalBiz = catalog.newInstance(ITemporalBiz.class);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProductJoinQuery() throws IOException
	{
		// String joinQueryString = "select o.OrderID, o.OrderDate,
		// o.ShippedDate, o.CustomerID, c.CompanyName, "
		// + "c.CompanyName, c.Address, c.City, c.Region "
		// + "from nw/orders o "
		// + "join nw/customers c ON o.CustomerID:c.CustomerID";

		// String joinQueryString = "select p.ProductID, p.ProductName,
		// s.SupplierID, "
		// + "s.CompanyName, s.Address, s.City, s.Region "
		// + "from nw/products p "
		// + "join nw/suppliers s ON p.SupplierID:s.SupplierID";

		// String joinQueryString = "select p.ProductID, p.ProductName,
		// s.SupplierID, "
		// + "s.CompanyName, s.Address, s.City, s.Region, "
		// + "cat.CategoryID, cat.CategoryName, cat.Description "
		// + "from nw/products p "
		// + "join nw/suppliers s ON p.SupplierID:s.SupplierID "
		// + "join nw/categories cat ON cat.CategoryID:p.CategoryID";

		// String joinQueryString = "select p.ProductID, p.ProductName,
		// s.SupplierID, "
		// + "s.CompanyName, s.Address, s.City, s.Region, "
		// + "cat.CategoryID, cat.CategoryName, cat.Description "
		// + "from nw/products p "
		// + "join nw/products p ON p.ProductID:\"(20 21 22)\" "
		// + "join nw/suppliers s ON s.SupplierID:p.SupplierID "
		// + "join nw/categories cat ON cat.CategoryID:p.CategoryID";

		// String joinQueryString = "select p.ProductID, p.ProductName,
		// s.SupplierID, "
		// + "s.CompanyName, s.Address, s.City, s.Region, "
		// + "cat.CategoryID, cat.CategoryName, cat.Description "
		// + "from nw/products p "
		// + "join nw/products p ON p?\"(20 10)\" "
		// + "join nw/suppliers s ON s.SupplierID:p.SupplierID "
		// + "join nw/categories cat ON cat.CategoryID:p.CategoryID";

		// String joinQueryString = "select p.ProductID, p.ProductName,
		// s.SupplierID, "
		// + "s.CompanyName, s.Address, s.City, s.Region, "
		// + "cat.CategoryID, cat.CategoryName, cat.Description "
		// + "from nw/products p "
		// + "join nw/products p ON p?\"(Gula Choco*)\" "
		// + "join nw/suppliers s ON s.SupplierID:p.SupplierID "
		// + "join nw/categories cat ON cat.CategoryID:p.CategoryID";

		// String joinQueryString = "select * "
		// + "from nw/products p "
		// + "join nw/products p ON p?\"(Gula Choco*)\" "
		// + "join nw/suppliers s ON s.SupplierID:p.SupplierID "
		// + "join nw/categories cat ON cat.CategoryID:p.CategoryID";

		String joinQueryString = "select * from nw/products p "
				// + "left join nw/products p ON p?${ProductInfo} "
				+ "left join nw/suppliers s ON s.SupplierID:p.SupplierID "
				+ "left join nw/categories cat ON cat.CategoryID:p.CategoryID " + "nest by inheritance";

		// select * from nw/products p
		// left join nw/suppliers s on s.SupplierID:p.SupplierID
		// left join nw/categories cat on cat.CategoryID:p.CategoryID
		// nest by inheritance

		// String joinQueryString = "select * from nw/products p join
		// nw/products p ON p?${ProductInfo} join nw/suppliers s on
		// s.SupplierID:${SupplierID} join nw/categories cat ON
		// cat.CategoryID:p.CategoryID";

		// nw/vp_product
		String productInfo = "Gula Choco*";
		String supplierID = "8";
		// String productInfo = "Chai";
		// String supplierID = "1";
		List<JsonLite> results = PqlEvalDriver.executeValues(temporalBiz, -1, -1, joinQueryString, productInfo, supplierID);
		System.out.println("PqlEvalDriverTest.testProductJoinQuery()");
		System.out.println("----------------------------------------");
		printResults(results);

		// List<TemporalEntry<ITemporalKey, ITemporalData>> teResults =
		// PqlEvalDriver.executeEntries(temporalBiz, joinQueryString,
		// productInput, supplierInput);
		// i = 0;
		// for (TemporalEntry<ITemporalKey, ITemporalData> te : teResults) {
		// ++i;
		// System.out.println(i + ". " + te);
		// }
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCustomerJoinQuery() throws IOException
	{
		List<JsonLite> results;

		// nw/vp_customer
		// String joinQueryString = "select * " + "from nw/customers c "
		// + "join nw/customers c ON c?${CustomerInfo} "
		// + "join nw/orders o on o.CustomerID:c.CustomerID "
		// + "join nw/shippers ship ON ship.ShipperID:o.ShipVia "
		// + "join nw/order_details od ON od.OrderID:o.OrderID "
		// + "join nw/products p ON p.ProductID:od.ProductID "
		// + "join nw/suppliers s ON s.SupplierID:p.SupplierID "
		// + "join nw/categories cat ON cat.CategoryID:p.CategoryID";

		String joinQueryString = "select * from nw/customers c " + "join nw/customers c ON c?${CustomerInfo} "
				+ "join nw/orders o TO MANY OrderList ON o.CustomerID:c.CustomerID "
				+ "join nw/shippers ship TO ONE Shipper ON ship.ShipperID:o.ShipVia "
				// + "join nw/order_details od TO MANY OrderDetailList ON
				// od.OrderID:o.OrderID "
				+ "join nw/order_details od ON od.OrderID:o.OrderID "
				+ "join nw/products p TO ONE Product ON p.ProductID:od.ProductID "
				+ "join nw/suppliers s TO ONE Supplier ON s.SupplierID:p.SupplierID "
				+ "join nw/categories cat TO ONE Category ON cat.CategoryID:p.CategoryID ";
		// + "NEST BY INHERITANCE";
		// + "NEST BY AGGREGATION";
		//
		// String customerInfo = "LILAS";
		String customerInfo = "CENTC";
		results = PqlEvalDriver.executeValues(temporalBiz, -1, -1, joinQueryString, customerInfo);
		printResults(results);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCustomerOrderJoinQuery() throws IOException
	{
		String joinQueryString = "select * " + "from nw/customers c "
				+ "join nw/orders o on o.CustomerID:c.CustomerID ";
		List<JsonLite> results = PqlEvalDriver.executeValues(temporalBiz, -1, -1, joinQueryString);
		printResults(results);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCustomerOrderJoinQueryScrolled() throws IOException
	{
		String joinQueryString = "select * " + "from nw/customers c "
				+ "join nw/orders o on o.CustomerID:c.CustomerID ";
		GridQuery gridQuery = new GridQuery();
		// AsOfTemporalEntry entry = new AsOfTemporalEntry(gq.getGridIds()[0],
		// GridUtil.getChildPath(gq
		// .getFullPath()), resultPanel.getValidAt(), resultPanel.getAsOf(),
		// null);
		gridQuery.setGridIds("mygrid");
		gridQuery.setFullPath("");
		gridQuery.setId(joinQueryString);
		IScrollableResultSet<JsonLite> rs = PqlEvalDriver.executeValuesScrolled(temporalBiz, gridQuery,
				joinQueryString);
		do {
			List<JsonLite> results = rs.toList();
			printResults(results);
		} while (rs.nextSet());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProductOrderJoinQueryScrolled() throws IOException
	{
		// String joinQueryString = "select * from nw/order_details od join
		// nw/vp_product p on p.ProductID:od.ProductID";
		// String joinQueryString = "select * from nw/order_details od,
		// nw/vp_product p where od.ProductID:53 and p.ProductID:53";
		String joinQueryString = "select p.ProductID, p.ProductName, od.OrderID, od.Quantity, od.UnitPrice, od.Discount as CompanyDiscount, p.UnitsInStock  from nw/order_details od, nw/vp_product p where p?'Came*' AND od.ProductID:p.ProductID";

		// String joinQueryString = "select * from nw/products p join
		// nw/products p ON p?(13 68 11 14) join nw/suppliers s on
		// s.SupplierID:p.SupplierID join nw/categories cat ON
		// cat.CategoryID:p.CategoryID";

		GridQuery gridQuery = new GridQuery();
		gridQuery.setGridIds("mygrid");
		gridQuery.setFullPath("");
		gridQuery.setId(joinQueryString);
		IScrollableResultSet<JsonLite> rs = PqlEvalDriver.executeValuesScrolled(temporalBiz, gridQuery,
				joinQueryString);
		do {
			List<JsonLite> results = rs.toList();
			printResults(results);
		} while (rs.nextSet());
	}

	@Test
	public void testJsonLiteReference() throws IOException
	{
		// nw/vp_customer
		// TO MANY | TO ONE - Default: TO MANY (Ignored if IS)
		// TO MANY - Returns list of objects
		// TO ONE - Returns a single object
		// NEST BY COMPOSITE | INHERITANCE - Default: COMPOSITE (Ignored if the
		// select projection is other than *)
		// COMPOSITE - "Has" (composition) relationship. Returned value contains
		// searched object as is.
		// INHERITANCE - "Is" (inheritance) relationship. Returned object
		// contains all searched object's fields.
		String joinQueryString = "select * " + "from nw/customers c "
				+ "join nw/orders o TO MANY OrderList ON o.CustomerID:WHITC "
				+ "join nw/shippers ship TO ONE Shipper ON ship.ShipperID:o.ShipVia "
				+ "join nw/order_details od TO MANY OrderDetailList ON od.OrderID:o.OrderID "
				+ "join nw/products p TO ONE Product ON p.ProductID:od.ProductID "
				+ "join nw/suppliers s TO ONE Supplier ON s.SupplierID:p.SupplierID "
				+ "join nw/categories cat TO ONE Category ON cat.CategoryID:p.CategoryID " + "nest by aggregation";

		String joinQueryString2 = "select * " + "from nw/customers c "
				+ "join nw/orders o TO MANY OrderList ON c?${CustomerInfo} AND o.CustomerID:c.CustomerID "
				+ "join nw/shippers ship TO ONE Shipper ON ship.ShipperID:o.ShipVia "
				// how to capture d.Abc:ship.Xyz? - Always use the object
				// returned by
				// the joined path, i.e., od.
				+ "join nw/order_details od TO MANY OrderDetailList ON od.OrderID:o.OrderID AND od.Abc:ship.Xyz "
				+ "join nw/products p TO ONE Product ON p.ProductID:od.ProductID "
				+ "join nw/suppliers s TO ONE Supplier ON s.SupplierID:p.SupplierID "
				+ "join nw/categories cat TO ONE Category ON cat.CategoryID:p.CategoryID" + "nest by aggregation";

		String joinQueryString2_1 = "select * " + "from nw/customers c "
				+ "join nw/orders o ON c?${CustomerInfo} AND o.CustomerID:c.CustomerID TO MANY OrderList "
				+ "join nw/shippers ship ON ship.ShipperID:o.ShipVia TO ONE Shipper "
				// how to capture d.Abc:ship.Xyz? - Always use the object
				// returned by
				// the joined path, i.e., od.
				+ "join nw/order_details od ON od.OrderID:o.OrderID AND od.Abc:ship.Xyz TO MANY OrderDetailList "
				+ "join nw/products p ON p.ProductID:od.ProductID TO ONE Product "
				+ "join nw/suppliers s ON s.SupplierID:p.SupplierID TO ONE Supplier "
				+ "join nw/categories cat ON cat.CategoryID:p.CategoryID TO ONE Category " + "nest by aggregation";

		String joinQueryString3 = "select * " + "from nw/customers c, nw/orders o TO MANY OrderList "
				+ "where c?${CustomerInfo} AND o.CustomerID:c.CustomerID "
				+ "join nw/shippers ship TO ONE Shipper ON ship.ShipperID:o.ShipVia "
				// how to capture d.Abc:ship.Xyz? - Always use the object
				// returned by
				// the joined path, i.e., od.
				+ "join nw/order_details od TO MANY OrderDetailList ON od.OrderID:o.OrderID AND od.Abc:ship.Xyz "
				+ "join nw/products p TO ONE Product ON p.ProductID:od.ProductID "
				+ "join nw/suppliers s TO ONE Supplier ON s.SupplierID:p.SupplierID "
				+ "join nw/categories cat TO ONE Category ON cat.CategoryID:p.CategoryID " + "nest by inheritance";

//		joinQueryString = "select g.AccountNumber, g.CompanyCode, g.FiscalYear,g.BusinessArea, g.TransactionCurrency,g.LocalCurrency,g.GlAccount,g.AccountNumber "
//				+ "from lynx/glt0 g, map/mapped_tb m "
//				+ "where g.FiscalYear:2016 AND g.CompanyCode:7355 AND g.BusinessArea:1142 AND g.TransactionCurrency:GBP "
//				+ "AND m.GlAccountNumber:g.AccountNumber";
//		
//		joinQueryString = "select g.AccountNumber, g.CompanyCode, g.FiscalYear,g.BusinessArea, g.TransactionCurrency,g.LocalCurrency,g.GlAccountNumber,g.AccountNumber "
//				+ "from lynx/glt0 g "
//				+ "join lynx/glt0 g ON g.FiscalYear:2016 AND g.CompanyCode:7355 AND g.BusinessArea:1142 AND g.TransactionCurrency:GBP "
//				+ "join map/mapped_tb m TO MANY MappedTb ON m.GlAccountNumber:g.AccountNumber ";
		
//				+ "where g.FiscalYear:2016 AND g.CompanyCode:7355 AND g.BusinessArea:1142 AND g.TransactionCurrency:GBP";
				
		joinQueryString = "select * from nw/products p "
				+ "join nw/products p TO ONE Product on p.ProductName:Chai AND p.UnitsOnOrder:0 "
				+ "join nw/suppliers s TO ONE Supplier on  p.UnitsOnOrder:0 AND s.SupplierID:p.SupplierID "
				+ "join nw/categories cat TO ONE Category on cat.CategoryID:p.CategoryID "
				+ "nest by inheritance";
		System.out.println(joinQueryString);

		String customerInfo = "";
		List<JsonLite> results = PqlEvalDriver.executeValues(temporalBiz, -1, -1, joinQueryString, customerInfo);
		printResults(results);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMultiplePredicatesOnPath() throws IOException
	{
		String joinQueryString = "select g.AccountNumber, g.CompanyCode, g.FiscalYear,g.BusinessArea, g.TransactionCurrency,g.LocalCurrency,g.GlAccount "
				+ "from lynx/glt0 g, map/mapped_tb m "
				+ "where g.FiscalYear:2016 AND g.CompanyCode:'7355' AND g.BusinessArea:\"(1142)\" AND g.TransactionCurrency:GBP "
				+ "AND g.Abc:m.Abc";
		List<JsonLite> results = PqlEvalDriver.executeValues(temporalBiz, -1, -1, joinQueryString);
		printResults(results);
	}

	private static void printResults(List<JsonLite> results)
	{
		int i = 0;
		for (JsonLite jl : results) {
			++i;
			System.out.println(i + ". " + jl.getKeyCount() + " " + jl.toString(4, false, false));
		}
	}
}
