package com.netcrest.pado.demo.nw.data.domain;


import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Region implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public Region()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.demo.nw.data.RegionKey.getKeyType());
	}

	public Region(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setRegionId(String regionId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.RegionKey.KRegionId, regionId);
	}

	public String getRegionId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.RegionKey.KRegionId);
	}

	public void setRegionDescription(String regionDescription) {
		this.jl.put(com.netcrest.pado.demo.nw.data.RegionKey.KRegionDescription, regionDescription);
	}

	public String getRegionDescription() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.RegionKey.KRegionDescription);
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
