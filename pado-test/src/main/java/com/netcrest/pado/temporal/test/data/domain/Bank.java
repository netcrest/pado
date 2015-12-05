package com.netcrest.pado.temporal.test.data.domain;

import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Bank implements IJsonLiteWrapper<Object> {

	private transient JsonLite<Object> jl;

	public Bank() {
		this.jl = new JsonLite<Object>(
				com.netcrest.pado.temporal.test.data.Bank.getKeyType());
	}

	public Bank(JsonLite<Object> jl) {
		this.jl = jl;
	}

	public void setBankId(long BankId) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Bank.KBankId, BankId);
	}

	public long getBankId() {
		return (Long) this.jl
				.get(com.netcrest.pado.temporal.test.data.Bank.KBankId);
	}

	public void setBankName(String BankName) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Bank.KBankName,
				BankName);
	}

	public String getBankName() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Bank.KBankName);
	}

	public void setRoutingNumber(String RoutingNumber) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Bank.KRoutingNumber,
				RoutingNumber);
	}

	public String getRoutingNumber() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Bank.KRoutingNumber);
	}

	public void setAccountNumber(String AccountNumber) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Bank.KAccountNumber,
				AccountNumber);
	}

	public String getAccountNumber() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Bank.KAccountNumber);
	}

	public void setType(byte Type) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Bank.KType, Type);
	}

	public byte getType() {
		return (Byte) this.jl
				.get(com.netcrest.pado.temporal.test.data.Bank.KType);
	}

	public JsonLite<Object> toJsonLite() {
		return this.jl;
	}

	public void fromJsonLite(JsonLite<Object> jl) {
		this.jl = jl;
	}
}