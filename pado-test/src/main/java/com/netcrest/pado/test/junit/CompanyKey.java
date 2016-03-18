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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + gics;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompanyKey other = (CompanyKey) obj;
		if (gics != other.gics)
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}
}
