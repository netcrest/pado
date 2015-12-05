package com.netcrest.pado.temporal.test.data.domain;

import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Account implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public Account()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.temporal.test.data.Account.getKeyType());
	}

	public Account(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setAccountId(long AccountId)
	{
		this.jl.put(com.netcrest.pado.temporal.test.data.Account.KAccountId, AccountId);
	}

	public String getAccountId()
	{
		return (String) this.jl.get(com.netcrest.pado.temporal.test.data.Account.KAccountId);
	}

	public void setAccountName(String AccountName)
	{
		this.jl.put(com.netcrest.pado.temporal.test.data.Account.KAccountName, AccountName);
	}

	public String getAccountName()
	{
		return (String) this.jl.get(com.netcrest.pado.temporal.test.data.Account.KAccountName);
	}

	public void setLastName(String LastName)
	{
		this.jl.put(com.netcrest.pado.temporal.test.data.Account.KLastName, LastName);
	}

	public String getLastName()
	{
		return (String) this.jl.get(com.netcrest.pado.temporal.test.data.Account.KLastName);
	}

	public void setFirstName(String FirstName)
	{
		this.jl.put(com.netcrest.pado.temporal.test.data.Account.KFirstName, FirstName);
	}

	public String getFirstName()
	{
		return (String) this.jl.get(com.netcrest.pado.temporal.test.data.Account.KFirstName);
	}

	public void setAddress(String Address)
	{
		this.jl.put(com.netcrest.pado.temporal.test.data.Account.KAddress, Address);
	}

	public String getAddress()
	{
		return (String) this.jl.get(com.netcrest.pado.temporal.test.data.Account.KAddress);
	}

	public void setBankId(String BankId)
	{
		this.jl.put(com.netcrest.pado.temporal.test.data.Account.KBankId, BankId);
	}

	public String getBankId()
	{
		return (String) this.jl.get(com.netcrest.pado.temporal.test.data.Account.KBankId);
	}

	public JsonLite<Object> toJsonLite()
	{
		return this.jl;
	}

	public void fromJsonLite(JsonLite<Object> jl)
	{
		this.jl = jl;
	}
}