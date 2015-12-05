package com.netcrest.pado.temporal.test.data.domain;

import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class AccountDetail implements IJsonLiteWrapper<Object> {

	private transient JsonLite<Object> jl;

	public AccountDetail() {
		this.jl = new JsonLite<Object>(
				com.netcrest.pado.temporal.test.data.AccountDetail.getKeyType());
	}

	public AccountDetail(JsonLite<Object> jl) {
		this.jl = jl;
	}

	public void setAccountId(String AccountId) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.AccountDetail.KAccountId,
				AccountId);
	}

	public String getAccountId() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.AccountDetail.KAccountId);
	}

	public void setPortfolios(String Portfolios) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.AccountDetail.KPortfolios,
				Portfolios);
	}

	public String getPortfolios() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.AccountDetail.KPortfolios);
	}

	public void setDescription(String Description) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.AccountDetail.KDescription,
				Description);
	}

	public String getDescription() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.AccountDetail.KDescription);
	}

	public void setOqlPortfolios(String OqlPortfolios) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.AccountDetail.KOqlPortfolios,
				OqlPortfolios);
	}

	public String getOqlPortfolios() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.AccountDetail.KOqlPortfolios);
	}

	public JsonLite<Object> toJsonLite() {
		return this.jl;
	}

	public void fromJsonLite(JsonLite<Object> jl) {
		this.jl = jl;
	}
}