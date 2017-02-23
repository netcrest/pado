package com.netcrest.pado.demo.nw.data.domain;


import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class EmployeeTerritory implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public EmployeeTerritory()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.demo.nw.data.EmployeeTerritoryKey.getKeyType());
	}

	public EmployeeTerritory(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setEmployeeId(String employeeId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeTerritoryKey.KEmployeeId, employeeId);
	}

	public String getEmployeeId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeTerritoryKey.KEmployeeId);
	}

	public void setTerritoryId(String territoryId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeTerritoryKey.KTerritoryId, territoryId);
	}

	public String getTerritoryId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeTerritoryKey.KTerritoryId);
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
