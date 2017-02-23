package com.netcrest.pado.demo.nw.data.domain;


import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Product implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public Product()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.demo.nw.data.ProductKey.getKeyType());
	}

	public Product(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setProductID(String productID) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ProductKey.KProductID, productID);
	}

	public String getProductID() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.ProductKey.KProductID);
	}

	public void setProductName(String productName) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ProductKey.KProductName, productName);
	}

	public String getProductName() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.ProductKey.KProductName);
	}

	public void setSupplierId(String supplierId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ProductKey.KSupplierId, supplierId);
	}

	public String getSupplierId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.ProductKey.KSupplierId);
	}

	public void setCategoryId(String categoryId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ProductKey.KCategoryId, categoryId);
	}

	public String getCategoryId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.ProductKey.KCategoryId);
	}

	public void setQuantityPerUnit(double quantityPerUnit) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ProductKey.KQuantityPerUnit, quantityPerUnit);
	}

	public double getQuantityPerUnit() {
		return (double) this.jl.get(com.netcrest.pado.demo.nw.data.ProductKey.KQuantityPerUnit);
	}

	public void setUnitPrice(double unitPrice) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ProductKey.KUnitPrice, unitPrice);
	}

	public double getUnitPrice() {
		return (double) this.jl.get(com.netcrest.pado.demo.nw.data.ProductKey.KUnitPrice);
	}

	public void setUnitsInStock(String unitsInStock) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ProductKey.KUnitsInStock, unitsInStock);
	}

	public String getUnitsInStock() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.ProductKey.KUnitsInStock);
	}

	public void setUnitsOnOrder(String unitsOnOrder) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ProductKey.KUnitsOnOrder, unitsOnOrder);
	}

	public String getUnitsOnOrder() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.ProductKey.KUnitsOnOrder);
	}

	public void setReorderLevel(String reorderLevel) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ProductKey.KReorderLevel, reorderLevel);
	}

	public String getReorderLevel() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.ProductKey.KReorderLevel);
	}

	public void setDiscontinued(String discontinued) {
		this.jl.put(com.netcrest.pado.demo.nw.data.ProductKey.KDiscontinued, discontinued);
	}

	public String getDiscontinued() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.ProductKey.KDiscontinued);
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
