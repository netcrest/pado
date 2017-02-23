package com.netcrest.pado.demo.nw.data.domain;


import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Customer implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public Customer()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.demo.nw.data.CustomerKey.getKeyType());
	}

	public Customer(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setCustomerId(String customerId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CustomerKey.KCustomerId, customerId);
	}

	public String getCustomerId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CustomerKey.KCustomerId);
	}

	public void setCompanyName(String companyName) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CustomerKey.KCompanyName, companyName);
	}

	public String getCompanyName() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CustomerKey.KCompanyName);
	}

	public void setContactName(String contactName) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CustomerKey.KContactName, contactName);
	}

	public String getContactName() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CustomerKey.KContactName);
	}

	public void setContactTitle(String contactTitle) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CustomerKey.KContactTitle, contactTitle);
	}

	public String getContactTitle() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CustomerKey.KContactTitle);
	}

	public void setAddress(String address) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CustomerKey.KAddress, address);
	}

	public String getAddress() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CustomerKey.KAddress);
	}

	public void setCity(String city) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CustomerKey.KCity, city);
	}

	public String getCity() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CustomerKey.KCity);
	}

	public void setRegion(String region) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CustomerKey.KRegion, region);
	}

	public String getRegion() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CustomerKey.KRegion);
	}

	public void setPostalCode(String postalCode) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CustomerKey.KPostalCode, postalCode);
	}

	public String getPostalCode() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CustomerKey.KPostalCode);
	}

	public void setCountry(String country) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CustomerKey.KCountry, country);
	}

	public String getCountry() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CustomerKey.KCountry);
	}

	public void setPhone(String phone) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CustomerKey.KPhone, phone);
	}

	public String getPhone() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CustomerKey.KPhone);
	}

	public void setFax(String fax) {
		this.jl.put(com.netcrest.pado.demo.nw.data.CustomerKey.KFax, fax);
	}

	public String getFax() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.CustomerKey.KFax);
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
