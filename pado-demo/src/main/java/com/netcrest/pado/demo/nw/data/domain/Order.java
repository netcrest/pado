package com.netcrest.pado.demo.nw.data.domain;

import java.util.Date;

import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Order implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public Order()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.demo.nw.data.OrderKey.getKeyType());
	}

	public Order(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setOrderId(String orderId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KOrderId, orderId);
	}

	public String getOrderId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KOrderId);
	}

	public void setCustomerId(String customerId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KCustomerId, customerId);
	}

	public String getCustomerId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KCustomerId);
	}

	public void setEmployeeId(String employeeId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KEmployeeId, employeeId);
	}

	public String getEmployeeId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KEmployeeId);
	}

	public void setOrderDate(Date orderDate) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KOrderDate, orderDate);
	}

	public Date getOrderDate() {
		return (Date) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KOrderDate);
	}

	public void setRequiredDate(Date requiredDate) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KRequiredDate, requiredDate);
	}

	public Date getRequiredDate() {
		return (Date) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KRequiredDate);
	}

	public void setShippedDate(Date shippedDate) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KShippedDate, shippedDate);
	}

	public Date getShippedDate() {
		return (Date) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KShippedDate);
	}

	public void setShipVia(String shipVia) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KShipVia, shipVia);
	}

	public String getShipVia() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KShipVia);
	}

	public void setFreight(String freight) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KFreight, freight);
	}

	public String getFreight() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KFreight);
	}

	public void setShipName(String shipName) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KShipName, shipName);
	}

	public String getShipName() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KShipName);
	}

	public void setShipAddress(String shipAddress) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KShipAddress, shipAddress);
	}

	public String getShipAddress() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KShipAddress);
	}

	public void setShipCity(String shipCity) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KShipCity, shipCity);
	}

	public String getShipCity() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KShipCity);
	}

	public void setShipRegion(String shipRegion) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KShipRegion, shipRegion);
	}

	public String getShipRegion() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KShipRegion);
	}

	public void setShipPostalCode(String shipPostalCode) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KShipPostalCode, shipPostalCode);
	}

	public String getShipPostalCode() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KShipPostalCode);
	}

	public void setShipCountry(String shipCountry) {
		this.jl.put(com.netcrest.pado.demo.nw.data.OrderKey.KShipCountry, shipCountry);
	}

	public String getShipCountry() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.OrderKey.KShipCountry);
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
