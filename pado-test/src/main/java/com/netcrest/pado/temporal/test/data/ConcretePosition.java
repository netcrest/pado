package com.netcrest.pado.temporal.test.data;

import java.math.BigDecimal;
import java.util.Date;

public interface ConcretePosition
{
	public String getAccountCd();

	public void setAccountCd(String accountCd);

	public Long getAccountId();

	public void setAccountId(Long accountId);

	public BigDecimal getAccrualAm();

	public void setAccrualAm(BigDecimal accrualAm);

	public Date getAsOfDt();

	public void setAsOfDt(Date asOfDt);

	public BigDecimal getBvAm();

	public void setBvAm(BigDecimal bvAm);

	public BigDecimal getCurrFaceAm();

	public void setCurrFaceAm(BigDecimal currFaceAm);

	public Long getFiImntId();

	public void setFiImntId(Long fiImntId);

	public String getImntAltCd();

	public void setImntAltCd(String imntAltCd);

	public BigDecimal getMkPr();

	public void setMkPr(BigDecimal mkPr);

	public BigDecimal getMvAm();

	public void setMvAm(BigDecimal mvAm);

	public BigDecimal getNavAm();

	public void setNavAm(BigDecimal navAm);

	public BigDecimal getOrgFaceAm();

	public void setOrgFaceAm(BigDecimal orgFaceAm);

	public BigDecimal getParAm();

	public void setParAm(BigDecimal parAm);

	public String getPositionCd();

	public void setPositionCd(String positionCd);

	public BigDecimal getTavAm();

	public void setTavAm(BigDecimal tavAm);

	public String getUuid();

	public void setUuid(String uuid);

}
