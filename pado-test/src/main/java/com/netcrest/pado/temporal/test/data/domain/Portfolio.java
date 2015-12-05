package com.netcrest.pado.temporal.test.data.domain;

import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;
import java.util.List;

public class Portfolio implements IJsonLiteWrapper<Object> {

	private transient JsonLite<Object> jl;

	public Portfolio() {
		this.jl = new JsonLite<Object>(
				com.netcrest.pado.temporal.test.data.Portfolio.getKeyType());
	}

	public Portfolio(JsonLite<Object> jl) {
		this.jl = jl;
	}

	public void setAccountId(String AccountId) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Portfolio.KAccountId,
				AccountId);
	}

	public String getAccountId() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Portfolio.KAccountId);
	}

	public void setPortfolioId(String PortfolioId) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Portfolio.KPortfolioId,
				PortfolioId);
	}

	public String getPortfolioId() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Portfolio.KPortfolioId);
	}

	public void setPortfolioName(String PortfolioName) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Portfolio.KPortfolioName,
				PortfolioName);
	}

	public String getPortfolioName() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Portfolio.KPortfolioName);
	}

	public void setDescription(String Description) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Portfolio.KDescription,
				Description);
	}

	public String getDescription() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Portfolio.KDescription);
	}

	public void setPositions(List Positions) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Portfolio.KPositions,
				Positions);
	}

	public List getPositions() {
		return (List) this.jl
				.get(com.netcrest.pado.temporal.test.data.Portfolio.KPositions);
	}

	public JsonLite<Object> toJsonLite() {
		return this.jl;
	}

	public void fromJsonLite(JsonLite<Object> jl) {
		this.jl = jl;
	}
}