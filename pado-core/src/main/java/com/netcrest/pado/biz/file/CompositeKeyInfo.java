package com.netcrest.pado.biz.file;

import java.io.Serializable;

public class CompositeKeyInfo implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private int[] routingKeyIndexes;
	private String compositeKeyDelimiter;
	
	public CompositeKeyInfo() {}
	
	public CompositeKeyInfo(int[] routingKeyIndexes, String compositeKeyDelimiter)
	{
		this.routingKeyIndexes = routingKeyIndexes;
		this.compositeKeyDelimiter = compositeKeyDelimiter;
	}
	
	public int[] getRoutingKeyIndexes()
	{
		return routingKeyIndexes;
	}
	public void setRoutingKeyIndexes(int[] routingKeyIndexes)
	{
		this.routingKeyIndexes = routingKeyIndexes;
	}
	public String getCompositeKeyDelimiter()
	{
		return compositeKeyDelimiter;
	}
	public void setCompositeKeyDelimiter(String compositeKeyDelimiter)
	{
		this.compositeKeyDelimiter = compositeKeyDelimiter;
	}
}
