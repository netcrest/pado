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

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.PadoQueryParser;
import com.netcrest.pado.pql.TokenizedQuery;
import com.netcrest.pado.temporal.test.TemporalLoader;
import com.netcrest.pado.temporal.test.data.Portfolio;

public class PadoParserTest
{
	static TemporalLoader loader;

	@SuppressWarnings("unused")
	@BeforeClass
	public static void setupBeforeClass()
	{
		loader = new TemporalLoader();
		loader.addBankId("bank_a");
		loader.addBankId("bank_ab");

		// this = account. account has BankId.

		// Join this account's BankId with bank's BankId (Not colocated)
		// If co-located then apply OQL on primary buckets of the regions.
		// TODO: Must provide the list of co-located regions that need to be
		// queried.

		// Case 1
		// ------
		String pql2 = "bank.BankId=this.BankId"; // "this" optional
		String expectedOql2 = "select * from /mock/bank where value.get('BankId')='bank_a'";
		// co-located regions - maybe not needed. use full "select"
		String expectedColocatedOql3 = "value.get('BankId')='bank_a'";

		// Lucene - must return the path, i.e., bank.
		String pql21 = "bank.BankId:this.BankId";
		String expectedLucene = "BankId:bank_a";

		// Case 2
		// ------

		// W/o this
		String pql3 = "bank.BankId=BankId";
		String expectedOql3 = "select * from /mock/bank where value.get('BankId')='bank_a'";

		// Lucene
		String pql31 = "bank.BankId:BankId";
		String expectedLucene3 = "BankId:bank_a";

		// Case 3
		// ------
		// portfolio - multiple queries
		String pql4 = "account.AccountId:this.AccountId position.PositionId:pos_gd position.PositionId:pos_cxh";
		String pql41 = "account.AccountId:this.AccountId position.PositionId in set (pos_gd, pos_cxh)";

		// Case 4
		// ------
		// Compare multiple attributes in the same path (account)
		String pql5 = "account.AccountId:this.AccountId AND account.BankId:this.BankId";
		String pql51 = "account(AccountId:AccountId AND BankId:BankId)";
		// If the attribute names are same
		String pql52 = "account(AccountId AND (BankId OR Name:BankName))";
		String lucene52 = "AccountId:acct_a AND (BankId:bank_a OR Name:XYZ)";
		String oql52 = "select * from /mock/account where AccountId='acct_a' AND (BankId='bank_a' OR Name='XYZ')";

		// Case 5
		// ------
		// Dynamic queries
		String pql6 = "select * from portfolio where get('AccountId')='$AccountId' AND get('BankId')='$BankId'";
		String pql7 = "select p from portfolio p, account a where p.get('AccountId')='$AccountId' AND a.get('AccountId')='$AccountId'";

	}

//	@Test
	public void testDynamicQuery()
	{
		JsonLite jl = (JsonLite) loader.createAccount("acct_a", false);
		KeyType keyType = jl.getKeyType();

		JsonLite jl2 = new JsonLite(Portfolio.getKeyType());
		jl2.put(Portfolio.KDescription, "hello");
		System.out.println(jl.toString(2, false, false));

		// Path
		// String pql = "portfolio";
		// CompiledUnit cu = new CompiledUnit(pql, keyType);
		// System.out.println("Path");
		// System.out.println("----");
		// printCompiledUnit(cu, jl);
		//
		// // OQL
		// pql =
		// "select * from portfolio where get('AccountId')='${AccountId}' AND get('BankId')='${BankId}'";
		// cu = new CompiledUnit(pql, keyType);
		// System.out.println("OQL");
		// System.out.println("---");
		// printCompiledUnit(cu, jl);

		// PQL
		String pql = "portfolio.AccountId=${AccountId} AND portfolio.BankId=${BankId}";
		// select e1.key.IdentityKey from /mock/instrument.entrySet e1 where
		// (e1.value.value.get('instSk')=${e1.value.value.get('Sk}) AND
		// e1.key.StartValidTime<=nullL AND 1404273600000L<e1.key.EndValidTime
		// AND e1.key.WrittenTime<=1404273600000L
		// output: e1.value['AccountId']='acct_a' AND
		// e1.value['BankId']='bank_a'
		// pql = "instrument.instrumentSk=${instSk}";
		// jl = jl2;
		// keyType = CollateralPositionKey.getKeyType();

		pql = "portfolio.AccountId=${AccountId}";

		CompiledUnit cu = new CompiledUnit(pql, keyType);
		System.out.println("PQL");
		System.out.println("---");
		printCompiledUnit(cu, jl);

		// Join
		pql = "portfolio.AccountId=${AccountId} AND account.AccountId=${AccountId}";
		cu = new CompiledUnit(pql, keyType);
		System.out.println("Join");
		System.out.println("----");
		printCompiledUnit(cu, jl);

		// Lucene
		pql = "portfolio?${AccountId} AND BankId:${BankId}";
		// portfolio?${AccountId} AND BankId:${BankId}
		cu = new CompiledUnit(pql, keyType);
		System.out.println("Lucene");
		System.out.println("----");
		printCompiledUnit(cu, jl);
	}

	private void printCompiledUnit(CompiledUnit cu, JsonLite jl)
	{
		String pql = cu.getPql();
		String query = cu.getQuery(jl);
		long validAt = System.currentTimeMillis();
		long asOf = System.currentTimeMillis();

		Object attributes[] = cu.getAttributes();
		System.out.println("          pql=" + pql);
		System.out.println("   isPathOnly=" + cu.isPathOnly());
		for (int i = 0; i < attributes.length; i++) {
			System.out.println("attributes[" + i + "]=" + attributes[i]);
		}
		System.out.println("compiledQuery=" + cu.getCompiledQuery());
		System.out.println("        query=" + query);
		System.out.println("identityQuery=" + cu.getTemporalIdentityQuery());
		String temporalIdentityQuery = cu.getTemporalIdentityQuery(jl, validAt, asOf);
		System.out.println("        query=" + temporalIdentityQuery);
	}

	public void testDynamicQuery2()
	{
		// Case 5
		// ------
		// Dynamic queries
		String pql6 = "select * from portfolio where get('AccountId')='${AccountId}' AND get('BankId')='${BankId}'";
		String pql7 = "select p from portfolio p, account a where p.get('AccountId')='$AccountId' AND a.get('AccountId')='$AccountId'";

		// Parse
		ArrayList<String> list = new ArrayList<String>();
		String str = pql6;
		int index = str.indexOf("${");
		while (index != -1) {
			int closeIndex = str.indexOf("}");
			if (closeIndex != -1) {
				String token = str.substring(index, closeIndex + 1);
				list.add(token);
				str = str.substring(closeIndex + 1);
			}
			index = str.indexOf("${");
		}

		JsonLite jl = (JsonLite) loader.createAccount("acct_a", false);
		System.out.println(jl.toString(2, false, false));

		String query = pql6;
		String compiledPql = pql6;

		for (String token : list) {
			String attr = PadoQueryParser.getAttribute_NotUsed(token);
			token = "\\$\\{" + attr + "\\}";
			Object value = jl.get(attr);
			if (value instanceof String) {
				query = query.replaceAll(token, (String) value);
			}
		}
		System.out.println("input=" + pql6);
		System.out.println("query=" + query);
	}

	// @Test
	public void testOqlThisQuery()
	{
		System.out.println("PadoParserTest.testOqlThisQuery()");
		System.out.println("------------------------------");
		PadoQueryParser parser = new PadoQueryParser();
		String rootPath = "/mock";
		String path = "account";
		String key = "acct_a";
		String attr = "bankId";

		JsonLite jl = (JsonLite) loader.createAccount("acct_a", false);
		String pql = "this.BankId=this.AccountId";
		TokenizedQuery tq = parser.toOql_NotUsed(attr, path, key, jl, rootPath, pql, true);
		System.out.println(jl.toString(2, false, false));
		System.out.println("pql=" + pql);
		System.out.println("oql=" + tq.getQueryString());
		System.out.println();

		pql = "bank.BankId=this.BankId";
		String pql2 = "select * from bank where value.get('BankId')=this.BankId";
		String pql3 = "select a, b from account a, bank b where a.value.get('BankId')=this.BankId";
		String pql4 = "bank.BankId:this.BankId"; // BankId:'bank_a' from bank
		tq = parser.toOql_NotUsed(attr, path, key, jl, rootPath, pql, true);
		System.out.println("pql=" + pql);
		System.out.println("oql=" + tq.getQueryString());
		System.out.println();
	}

//	@Test
	public void testOqlQuery()
	{
		System.out.println("PadoParserTest.testOqlQuery()");
		System.out.println("-----------------------------");
		PadoQueryParser parser = new PadoQueryParser();
		String rootPath = "/mock";
		String path = "account";
		String key = "acct_a";
		String attr = "bankId";

		JsonLite jl = (JsonLite) loader.createAccount("acct_a", false);

		String pql = "bank.BankId=bank.AccountId";
		String expectedOql = "select * from /mock/bank where bankId=accountId";

		TokenizedQuery tq = parser.toOql_NotUsed(attr, path, key, jl, rootPath, pql, true);
		System.out.println(jl.toString(2, false, false));
		System.out.println("pql=" + pql);
		System.out.println("oql=" + tq.getQueryString());
		System.out.println();
	}

	@Test
	public void testLuceneQuery()
	{
		// Lucene
		String pql = "portfolio?${AccountId} AND BankId:${BankId}";
		// portfolio?${AccountId} AND BankId:${BankId}
		JsonLite jl = (JsonLite) loader.createAccount("acct_a", false);
		KeyType keyType = jl.getKeyType();
		CompiledUnit cu = new CompiledUnit(pql, keyType);
		System.out.println("Lucene");
		System.out.println("----");
		printCompiledUnit(cu, jl);
	}
}
