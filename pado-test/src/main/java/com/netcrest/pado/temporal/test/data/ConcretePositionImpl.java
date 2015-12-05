package com.netcrest.pado.temporal.test.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.gemfire.util.DataSerializerEx;
import com.netcrest.pado.temporal.ITemporalDataSerializable;
import com.netcrest.pado.temporal.ITemporalDelta;
import com.netcrest.pado.temporal.ITemporalValue;
import com.netcrest.pado.temporal.TemporalHelper;

public class ConcretePositionImpl implements ConcretePosition, ITemporalDataSerializable, DataSerializable, ITemporalDelta
{
	private static final long serialVersionUID = 1L;

	private static final int BIT_MASK_SIZE = 32; // int type

	protected ITemporalValue __temporalValue;
	private int[] dirtyFlags = new int[1];
	private boolean deltaEnabled;
	static final int bitCount = 16;

	String accountCd;
	Long accountId;
	BigDecimal accrualAm;
	Date asOfDt;
	BigDecimal bvAm;
	BigDecimal currFaceAm;
	Long fiImntId;
	String imntAltCd;

	BigDecimal mkPr;
	BigDecimal mvAm;
	BigDecimal navAm;
	BigDecimal orgFaceAm;
	BigDecimal parAm;
	String positionCd;
	BigDecimal tavAm;
	String uuid;

	public String getAccountCd()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return accountCd;
	}

	public void setAccountCd(String accountCd)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.accountCd = accountCd;
		setDirty(0, dirtyFlags);
	}

	public Long getAccountId()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return accountId;
	}

	public void setAccountId(Long accountId)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.accountId = accountId;
		setDirty(1, dirtyFlags);
	}

	public BigDecimal getAccrualAm()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return accrualAm;
	}

	public void setAccrualAm(BigDecimal accrualAm)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.accrualAm = accrualAm;
		setDirty(2, dirtyFlags);
	}

	public Date getAsOfDt()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return asOfDt;
	}

	public void setAsOfDt(Date asOfDt)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.asOfDt = asOfDt;
		setDirty(3, dirtyFlags);
	}

	public BigDecimal getBvAm()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return bvAm;
	}

	public void setBvAm(BigDecimal bvAm)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.bvAm = bvAm;
		setDirty(4, dirtyFlags);
	}

	public BigDecimal getCurrFaceAm()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return currFaceAm;
	}

	public void setCurrFaceAm(BigDecimal currFaceAm)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.currFaceAm = currFaceAm;
		setDirty(5, dirtyFlags);
	}

	public Long getFiImntId()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return fiImntId;
	}

	public void setFiImntId(Long fiImntId)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.fiImntId = fiImntId;
		setDirty(6, dirtyFlags);
	}

	public String getImntAltCd()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return imntAltCd;
	}

	public void setImntAltCd(String imntAltCd)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.imntAltCd = imntAltCd;
		setDirty(7, dirtyFlags);
	}

	public BigDecimal getMkPr()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return mkPr;
	}

	public void setMkPr(BigDecimal mkPr)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.mkPr = mkPr;
		setDirty(8, dirtyFlags);
	}

	public BigDecimal getMvAm()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return mvAm;
	}

	public void setMvAm(BigDecimal mvAm)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.mvAm = mvAm;
		setDirty(9, dirtyFlags);
	}

	public BigDecimal getNavAm()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return navAm;
	}

	public void setNavAm(BigDecimal navAm)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.navAm = navAm;
		setDirty(10, dirtyFlags);
	}

	public BigDecimal getOrgFaceAm()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return orgFaceAm;
	}

	public void setOrgFaceAm(BigDecimal orgFaceAm)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.orgFaceAm = orgFaceAm;
		setDirty(11, dirtyFlags);
	}

	public BigDecimal getParAm()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return parAm;
	}

	public void setParAm(BigDecimal parAm)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.parAm = parAm;
		setDirty(12, dirtyFlags);
	}

	public String getPositionCd()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return positionCd;
	}

	public void setPositionCd(String positionCd)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.positionCd = positionCd;
		setDirty(13, dirtyFlags);
	}

	public BigDecimal getTavAm()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return tavAm;
	}

	public void setTavAm(BigDecimal tavAm)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.tavAm = tavAm;
		setDirty(14, dirtyFlags);
	}

	public String getUuid()
	{
		TemporalHelper.deserializeData(__temporalValue);
		return uuid;
	}

	public void setUuid(String uuid)
	{
		TemporalHelper.deserializeData(__temporalValue);
		this.uuid = uuid;
		setDirty(15, dirtyFlags);
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		__temporalValue = DataSerializer.readObject(input);
		__temporalValue.setData(this);
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void toData(DataOutput output) throws IOException
	{
		DataSerializer.writeObject(__temporalValue, output);
		clearDirty();
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void readTemporalAttributes(DataInput input) throws IOException, ClassNotFoundException
	{
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void writeTemporalAttributes(DataOutput output) throws IOException
	{
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void readTemporal(DataInput input) throws IOException, ClassNotFoundException
	{
		accountCd = DataSerializer.readString(input);
		accountId = DataSerializer.readLong(input);
		accrualAm = DataSerializer.readObject(input);
		asOfDt = DataSerializer.readDate(input);
		bvAm = DataSerializer.readObject(input);
		currFaceAm = DataSerializer.readObject(input);
		fiImntId = DataSerializer.readLong(input);
		imntAltCd = DataSerializer.readString(input);
		mkPr = DataSerializer.readObject(input);
		mvAm = DataSerializer.readObject(input);
		navAm = DataSerializer.readObject(input);
		orgFaceAm = DataSerializer.readObject(input);
		parAm = DataSerializer.readObject(input);
		positionCd = DataSerializer.readString(input);
		tavAm = DataSerializer.readObject(input);
		uuid = DataSerializer.readString(input);
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void writeTemporal(DataOutput output) throws IOException
	{
		DataSerializer.writeString(accountCd, output);
		DataSerializer.writeLong(accountId, output);
		DataSerializer.writeObject(accrualAm, output);
		DataSerializer.writeDate(asOfDt, output);
		DataSerializer.writeObject(bvAm, output);
		DataSerializer.writeObject(currFaceAm, output);
		DataSerializer.writeLong(fiImntId, output);
		DataSerializer.writeString(imntAltCd, output);
		DataSerializer.writeObject(mkPr, output);
		DataSerializer.writeObject(mvAm, output);
		DataSerializer.writeObject(navAm, output);
		DataSerializer.writeObject(orgFaceAm, output);
		DataSerializer.writeObject(parAm, output);
		DataSerializer.writeString(positionCd, output);
		DataSerializer.writeObject(tavAm, output);
		DataSerializer.writeString(uuid, output);
	}

	/**
	 * Returns temporal value internal to the temporal framework. <b>Internal
	 * use only.</b>
	 * 
	 * @gfcodegen This code is generated by the gfcodegen code generator.
	 */
	public ITemporalValue __getTemporalValue()
	{
		return __temporalValue;
	}

	/**
	 * Sets temporal value internal to the temporal framework. <b>Internal use
	 * only.</b>
	 * 
	 * @gfcodegen This code is generated by the gfcodegen code generator.
	 */
	public void __setTemporalValue(ITemporalValue temporalValue)
	{
		this.__temporalValue = temporalValue;
	}

	/**
	 * Returns true if there are changes made in values.
	 */
	public boolean isDirty()
	{
		for (int i = 0; i < dirtyFlags.length; i++) {
			if ((dirtyFlags[i] & 0xFFFFFFFF) != 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the specified key type dirty.
	 * 
	 * @param keyType
	 *            The key type to set dirty.
	 * @param flags
	 *            The flags that contain the key type.
	 */
	private void setDirty(KeyType keyType, int flags[])
	{
		int index = keyType.getIndex();
		setDirty(index, flags);
	}

	/**
	 * Sets the specified contiguous bit of the flags. A contiguous bit is the
	 * bit number of the contiguous array integers. For example, if the flags
	 * array size is 2 then the contiguous bit of 32 represents the first bit of
	 * the flags[1] integer, 33 represents the second bit, and etc.
	 * 
	 * @param contiguousBit
	 *            The contiguous bit position.
	 * @param flags
	 *            The bit flags.
	 */
	private void setDirty(int contiguousBit, int flags[])
	{
		int dirtyFlagsIndex = contiguousBit / BIT_MASK_SIZE;
		int bit = contiguousBit % BIT_MASK_SIZE;
		flags[dirtyFlagsIndex] |= 1 << bit;
	}

	/**
	 * Returns true if the specified key type is dirty.
	 * 
	 * @param keyType
	 *            The key type to check.
	 * @param flags
	 *            The flags that contain the key type.
	 */
	private boolean isBitDirty(KeyType keyType, int flags[])
	{
		int index = keyType.getIndex();
		int dirtyFlagsIndex = index / BIT_MASK_SIZE;
		int bit = index % BIT_MASK_SIZE;
		return isBitDirty(flags[dirtyFlagsIndex], bit);
	}

	/**
	 * Returns true if the specified flag bit id dirty.
	 * 
	 * @param flag
	 *            The flag to check.
	 * @param bit
	 *            The bit to compare.
	 * @return
	 */
	private boolean isBitDirty(int flag, int bit)
	{
		return ((flag >> bit) & 1) == 1;
	}

	/**
	 * Returns true if the any of the flag bits is dirty.
	 * 
	 * @param flag
	 *            The flag to check.
	 */
	private boolean isDirty(int flag)
	{
		return (flag & 0xFFFFFFFF) != 0;
	}

	/**
	 * Clears the entire dirty flags.
	 */
	private void clearDirty()
	{
		if (dirtyFlags != null) {
			for (int i = 0; i < dirtyFlags.length; i++) {
				dirtyFlags[i] = 0x0;
			}
		}
	}

	public boolean isDeltaEnabled()
	{
		return deltaEnabled;
	}

	public void setDeltaEnabled(boolean deltaEnabled)
	{
		this.deltaEnabled = deltaEnabled;
	}

	/**
	 * Returns true if GemFire delta propagation is enabled and there are
	 * changes in values.
	 */
	public boolean hasDelta()
	{
		if (isDeltaEnabled()) {
			return isDirty();
		}
		return false;
	}

	@Override
	public void readDelta(DataInput input) throws IOException
	{
		int dirtyFlagCount = dirtyFlags.length;
		int dirtyFlagsToApply[] = new int[dirtyFlagCount];
		for (int i = 0; i < dirtyFlagCount; i++) {
			dirtyFlagsToApply[i] = input.readInt();
		}

		int count = BIT_MASK_SIZE; // int
		for (int i = 0; i < dirtyFlagsToApply.length; i++) {
			int dirty = dirtyFlagsToApply[i]; // received dirty
			int userDirty = dirtyFlags[i]; // app dirty
			if (i == dirtyFlagsToApply.length - 1) {
				count = bitCount % BIT_MASK_SIZE;
				if (count == 0 && bitCount != 0) {
					count = BIT_MASK_SIZE;
				}
			}

			// Compare both the current bit and the received bit.
			// The app might be modifying the object. If so, keep the
			// user modified data and discard the change received.
			int startIndex = i * BIT_MASK_SIZE;
			for (int j = 0; j < count; j++) {
				if (isBitDirty(dirty, j)) {
					int index = startIndex + j;
					readField(index, userDirty, j, input);
				}
			}
		}
	}

	private void readField(final int index, int userDirty, int userDirtyIndex, DataInput input) throws IOException
	{
		// Set the new value only if the app has not set the
		// value
		// TODO: Support this?
		// if (isBitDirty(userDirty, userDirtyIndex) == false) {
		//
		// }

		switch (index) {
		case 0: // accountCd
			accountCd = DataSerializerEx.readUTF(input);
			break;
		case 1: // accountId
			accountId = DataSerializerEx.readLong(input);
			break;
		case 2: // accrualAm
			accrualAm = DataSerializerEx.readBigDecimal(input);
			break;
		case 3: // asOfDt
			asOfDt = DataSerializerEx.readDate(input);
			break;
		case 4: // bvAm
			bvAm = DataSerializerEx.readBigDecimal(input);
			break;
		case 5: // currFaceAm
			currFaceAm = DataSerializerEx.readBigDecimal(input);
			break;
		case 6: // fiImntId
			fiImntId = DataSerializerEx.readLong(input);
			break;
		case 7: // imntAltCd
			imntAltCd = DataSerializerEx.readUTF(input);
			break;
		case 8: // mkPr
			mkPr = DataSerializerEx.readBigDecimal(input);
			break;
		case 9: // mvAm
			mvAm = DataSerializerEx.readBigDecimal(input);
			break;
		case 10: // navAm
			navAm = DataSerializerEx.readBigDecimal(input);
			break;
		case 11: // orgFaceAm
			orgFaceAm = DataSerializerEx.readBigDecimal(input);
			break;
		case 12: // parAm
			parAm = DataSerializerEx.readBigDecimal(input);
			break;
		case 13: // positionCd
			positionCd = DataSerializerEx.readUTF(input);
			break;
		case 14: // tavAm
			tavAm = DataSerializerEx.readBigDecimal(input);
			break;
		case 15: // uuid
			uuid = DataSerializerEx.readUTF(input);
			break;
		}
	}

	private void writeField(final int index, DataOutput output) throws IOException
	{
		switch (index) {
		case 0: // accountCd
			DataSerializerEx.writeUTF(accountCd, output);
			break;
		case 1: // accountId
			DataSerializerEx.writeLong(accountId, output);
			break;
		case 2: // accrualAm
			DataSerializerEx.writeBigDecimal(accrualAm, output);
			break;
		case 3: // asOfDt
			DataSerializerEx.writeDate(asOfDt, output);
			break;
		case 4: // bvAm
			DataSerializerEx.writeBigDecimal(bvAm, output);
			break;
		case 5: // currFaceAm
			DataSerializerEx.writeBigDecimal(currFaceAm, output);
			break;
		case 6: // fiImntId
			DataSerializerEx.writeLong(fiImntId, output);
			break;
		case 7: // imntAltCd
			DataSerializerEx.writeUTF(imntAltCd, output);
			break;
		case 8: // mkPr
			DataSerializerEx.writeBigDecimal(mkPr, output);
			break;
		case 9: // mvAm
			DataSerializerEx.writeBigDecimal(mvAm, output);
			break;
		case 10: // navAm
			DataSerializerEx.writeBigDecimal(navAm, output);
			break;
		case 11: // orgFaceAm
			DataSerializerEx.writeBigDecimal(orgFaceAm, output);
			break;
		case 12: // parAm
			DataSerializerEx.writeBigDecimal(parAm, output);
			break;
		case 13: // positionCd
			DataSerializerEx.writeUTF(positionCd, output);
			break;
		case 14: // tavAm
			DataSerializerEx.writeBigDecimal(tavAm, output);
			break;
		case 15: // uuid
			DataSerializerEx.writeUTF(uuid, output);
			break;
		}

	}

	@Override
	public void writeDelta(DataOutput output) throws IOException
	{
		for (int i = 0; i < dirtyFlags.length; i++) {
			output.writeInt(dirtyFlags[i]);
		}
		int count = BIT_MASK_SIZE;
		for (int i = 0; i < dirtyFlags.length; i++) {
			int dirty = dirtyFlags[i];
			if (isDirty(dirty) == false) {
				continue;
			}
			if (i == dirtyFlags.length - 1) {
				count = bitCount % BIT_MASK_SIZE;
				if (count == 0 && bitCount != 0) {
					count = BIT_MASK_SIZE;
				}
			}
			int startIndex = i * BIT_MASK_SIZE;
			for (int j = 0; j < count; j++) {
				if (isBitDirty(dirty, j)) {
					int index = startIndex + j;
					writeField(index, output);
				}
			}
		}
		clearDirty();
	}

	@Override
	public String toString()
	{
		return "MBMPositionImpl [getAccountCd()=" + getAccountCd() + ", getAccountId()=" + getAccountId()
				+ ", getAccrualAm()=" + getAccrualAm() + ", getAsOfDt()=" + getAsOfDt() + ", getBvAm()=" + getBvAm()
				+ ", getCurrFaceAm()=" + getCurrFaceAm() + ", getFiImntId()=" + getFiImntId() + ", getImntAltCd()="
				+ getImntAltCd() + ", getMkPr()=" + getMkPr() + ", getMvAm()=" + getMvAm() + ", getNavAm()="
				+ getNavAm() + ", getOrgFaceAm()=" + getOrgFaceAm() + ", getParAm()=" + getParAm()
				+ ", getPositionCd()=" + getPositionCd() + ", getTavAm()=" + getTavAm() + ", getUuid()=" + getUuid()
				+ "]";
	}
	
	@Override
	public Object getValue()
	{
		return this;
	}
}
