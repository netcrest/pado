package com.netcrest.pado.demo.nw.data.domain;


import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Supplier implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public Supplier()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.demo.nw.data.SupplierKey.getKeyType());
	}

	public Supplier(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setSupplierId(String supplierId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KSupplierId, supplierId);
	}

	public String getSupplierId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KSupplierId);
	}

	public void setCompanyName(String companyName) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KCompanyName, companyName);
	}

	public String getCompanyName() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KCompanyName);
	}

	public void setContactName(String contactName) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KContactName, contactName);
	}

	public String getContactName() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KContactName);
	}

	public void setContactTitle(String contactTitle) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KContactTitle, contactTitle);
	}

	public String getContactTitle() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KContactTitle);
	}

	public void setAddress(String address) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KAddress, address);
	}

	public String getAddress() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KAddress);
	}

	public void setCity(String city) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KCity, city);
	}

	public String getCity() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KCity);
	}

	public void setRegion(String region) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KRegion, region);
	}

	public String getRegion() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KRegion);
	}

	public void setPostalCode(String postalCode) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KPostalCode, postalCode);
	}

	public String getPostalCode() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KPostalCode);
	}

	public void setCountry(String country) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KCountry, country);
	}

	public String getCountry() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KCountry);
	}

	public void setPhone(String phone) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KPhone, phone);
	}

	public String getPhone() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KPhone);
	}

	public void setFax(String fax) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KFax, fax);
	}

	public String getFax() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KFax);
	}

	public void setHomePage(String homePage) {
		this.jl.put(com.netcrest.pado.demo.nw.data.SupplierKey.KHomePage, homePage);
	}

	public String getHomePage() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.SupplierKey.KHomePage);
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
