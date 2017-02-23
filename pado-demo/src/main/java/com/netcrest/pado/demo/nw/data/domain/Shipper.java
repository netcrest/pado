package com.netcrest.pado.demo.nw.data.domain;


import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Shipper implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public Shipper()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.demo.nw.data.ShipperKey.getKeyType());
	}

	public Shipper(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setShipperId(String shipperId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ShipperKey.KShipperId, shipperId);
	}

	public String getShipperId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.ShipperKey.KShipperId);
	}

	public void setCompanyName(String companyName) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ShipperKey.KCompanyName, companyName);
	}

	public String getCompanyName() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.ShipperKey.KCompanyName);
	}

	public void setPhone(String phone) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ShipperKey.KPhone, phone);
	}

	public String getPhone() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.ShipperKey.KPhone);
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
