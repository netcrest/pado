package com.netcrest.pado.internal.pql.antlr4;

public class PathItem
{
	private JoinType joinType = JoinType.INNER;
	private String path;
	private String alias;

	public PathItem()
	{
	}

	public PathItem(String path, String alias, JoinType joinType)
	{
		this.path = path;
		this.alias = alias;
		this.joinType = joinType;
	}

	public JoinType getJoinType()
	{
		return joinType;
	}

	public void setType(JoinType type)
	{
		this.joinType = type;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getAlias()
	{
		return alias;
	}

	public void setAlias(String alias)
	{
		this.alias = alias;
	}

	public enum JoinType
	{
		INNER, LEFT, RIGHT, FULL;
	}
}
