package com.netcrest.pado.demo.nw.data.domain;


import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class OrderDetail implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public OrderDetail()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.demo.nw.data.OrderDetailKey.getKeyType());
	}

	public OrderDetail(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setOrderId(String orderId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderDetailKey.KOrderId, orderId);
	}

	public String getOrderId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderDetailKey.KOrderId);
	}

	public void setProductId(String productId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderDetailKey.KProductId, productId);
	}

	public String getProductId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderDetailKey.KProductId);
	}

	public void setUnitPrice(double unitPrice) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderDetailKey.KUnitPrice, unitPrice);
	}

	public double getUnitPrice() {
		return (double) this.jl.get(com.netcrest.pado.demo.nw.data.OrderDetailKey.KUnitPrice);
	}

	public void setQuantity(double quantity) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderDetailKey.KQuantity, quantity);
	}

	public double getQuantity() {
		return (double) this.jl.get(com.netcrest.pado.demo.nw.data.OrderDetailKey.KQuantity);
	}

	public void setDiscount(double discount) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderDetailKey.KDiscount, discount);
	}

	public double getDiscount() {
		return (double) this.jl.get(com.netcrest.pado.demo.nw.data.OrderDetailKey.KDiscount);
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
