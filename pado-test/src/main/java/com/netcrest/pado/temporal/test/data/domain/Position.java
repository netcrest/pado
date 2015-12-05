package com.netcrest.pado.temporal.test.data.domain;

import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;
import java.util.Date;

public class Position implements IJsonLiteWrapper<Object> {

	private transient JsonLite<Object> jl;

	public Position() {
		this.jl = new JsonLite<Object>(
				com.netcrest.pado.temporal.test.data.Position.getKeyType());
	}

	public Position(JsonLite<Object> jl) {
		this.jl = jl;
	}

	public void setSecId(String SecId) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Position.KSecId, SecId);
	}

	public String getSecId() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Position.KSecId);
	}

	public void setAccountId(String AccountId) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Position.KAccountId,
				AccountId);
	}

	public String getAccountId() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Position.KAccountId);
	}

	public void setExposure1(double Exposure1) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Position.KExposure1,
				Exposure1);
	}

	public double getExposure1() {
		return (Double) this.jl
				.get(com.netcrest.pado.temporal.test.data.Position.KExposure1);
	}

	public void setSettlementDate(Date SettlementDate) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Position.KSettlementDate,
				SettlementDate);
	}

	public Date getSettlementDate() {
		return (Date) this.jl
				.get(com.netcrest.pado.temporal.test.data.Position.KSettlementDate);
	}

	public void setExposure2(double Exposure2) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Position.KExposure2,
				Exposure2);
	}

	public double getExposure2() {
		return (Double) this.jl
				.get(com.netcrest.pado.temporal.test.data.Position.KExposure2);
	}

	public void setRiskFactor(double RiskFactor) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Position.KRiskFactor,
				RiskFactor);
	}

	public double getRiskFactor() {
		return (Double) this.jl
				.get(com.netcrest.pado.temporal.test.data.Position.KRiskFactor);
	}

	public JsonLite<Object> toJsonLite() {
		return this.jl;
	}

	public void fromJsonLite(JsonLite<Object> jl) {
		this.jl = jl;
	}
}