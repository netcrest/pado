package com.netcrest.pado.demo.nw.data.domain;


import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Territory implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public Territory()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.demo.nw.data.TerritoryKey.getKeyType());
	}

	public Territory(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setTerritoryId(String territoryId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.TerritoryKey.KTerritoryId, territoryId);
	}

	public String getTerritoryId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.TerritoryKey.KTerritoryId);
	}

	public void setTerritoryDescription(String territoryDescription) {
		this.jl.put(com.netcrest.pado.demo.nw.data.TerritoryKey.KTerritoryDescription, territoryDescription);
	}

	public String getTerritoryDescription() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.TerritoryKey.KTerritoryDescription);
	}

	public void setRegionId(String regionId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.TerritoryKey.KRegionId, regionId);
	}

	public String getRegionId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.TerritoryKey.KRegionId);
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
