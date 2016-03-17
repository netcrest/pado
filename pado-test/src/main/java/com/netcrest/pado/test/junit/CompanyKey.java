package com.netcrest.pado.test.junit;

import java.io.Serializable;

import com.netcrest.pado.IRoutingKey;

public class CompanyKey implements IRoutingKey, Serializable
{
	private static final long serialVersionUID = 1L;
	private String symbol;
	private int gics;

	public CompanyKey()
	{
	}

	public String getSymbol()
	{
		return symbol;
	}

	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}

	public int getGics()
	{
		return gics;
	}

	public void setGics(int gics)
	{
		this.gics = gics;
	}

	@Override
	public Object getIdentityKey()
	{
		return symbol;
	}

	@Override
	public Object getRoutingKey()
	{
		return gics;
	}

}
