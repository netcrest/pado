package com.netcrest.pado.demo.nw.data.domain;

import java.util.Date;

import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Employee implements IJsonLiteWrapper<Object>
{
	private transient JsonLite<Object> jl;

	public Employee()
	{
		this.jl = new JsonLite<Object>(com.netcrest.pado.demo.nw.data.EmployeeKey.getKeyType());
	}

	public Employee(JsonLite<Object> jl)
	{
		this.jl = jl;
	}

	public void setEmployeeId(String employeeId) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KEmployeeId, employeeId);
	}

	public String getEmployeeId() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KEmployeeId);
	}

	public void setLastName(String lastName) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KLastName, lastName);
	}

	public String getLastName() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KLastName);
	}

	public void setFirstName(String firstName) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KFirstName, firstName);
	}

	public String getFirstName() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KFirstName);
	}

	public void setTitle(String title) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KTitle, title);
	}

	public String getTitle() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KTitle);
	}

	public void setTitleOfCourtesy(String titleOfCourtesy) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KTitleOfCourtesy, titleOfCourtesy);
	}

	public String getTitleOfCourtesy() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KTitleOfCourtesy);
	}

	public void setBirthDate(Date birthDate) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KBirthDate, birthDate);
	}

	public Date getBirthDate() {
		return (Date) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KBirthDate);
	}

	public void setHireDate(Date hireDate) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KHireDate, hireDate);
	}

	public Date getHireDate() {
		return (Date) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KHireDate);
	}

	public void setAddress(String address) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KAddress, address);
	}

	public String getAddress() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KAddress);
	}

	public void setCity(String city) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KCity, city);
	}

	public String getCity() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KCity);
	}

	public void setRegion(String region) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KRegion, region);
	}

	public String getRegion() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KRegion);
	}

	public void setPostalCode(String postalCode) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KPostalCode, postalCode);
	}

	public String getPostalCode() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KPostalCode);
	}

	public void setCountry(String country) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KCountry, country);
	}

	public String getCountry() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KCountry);
	}

	public void setHomePhone(String homePhone) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KHomePhone, homePhone);
	}

	public String getHomePhone() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KHomePhone);
	}

	public void setExtension(String extension) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KExtension, extension);
	}

	public String getExtension() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KExtension);
	}

	public void setPhoto(String photo) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KPhoto, photo);
	}

	public String getPhoto() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KPhoto);
	}

	public void setNotes(String notes) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KNotes, notes);
	}

	public String getNotes() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KNotes);
	}

	public void setReportsTo(String reportsTo) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KReportsTo, reportsTo);
	}

	public String getReportsTo() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KReportsTo);
	}

	public void setPhotoPath(String photoPath) {
		this.jl.put(com.netcrest.pado.demo.nw.data.EmployeeKey.KPhotoPath, photoPath);
	}

	public String getPhotoPath() {
		return (String) this.jl.get(com.netcrest.pado.demo.nw.data.EmployeeKey.KPhotoPath);
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
