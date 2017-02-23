package com.netcrest.pado.demo.nw.data.domain;


import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Category implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public Category()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.demo.nw.data.CategoryKey.getKeyType());
	}

	public Category(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setCategoryID(String categoryID) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CategoryKey.KCategoryID, categoryID);
	}

	public String getCategoryID() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CategoryKey.KCategoryID);
	}

	public void setCategoryName(String categoryName) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CategoryKey.KCategoryName, categoryName);
	}

	public String getCategoryName() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CategoryKey.KCategoryName);
	}

	public void setDescription(String description) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CategoryKey.KDescription, description);
	}

	public String getDescription() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CategoryKey.KDescription);
	}

	public void setPicture(String picture) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CategoryKey.KPicture, picture);
	}

	public String getPicture() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CategoryKey.KPicture);
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
