package com.gemstone.gemfire.addon.dq.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.Instantiator;
import com.gemstone.gemfire.addon.util.DataSerializerEx;
import com.gemstone.gemfire.internal.util.BlobHelper;

/**
 * Class <code>DQueueOfferFunctionArguments</code> contains the arguments
 * used by the <code>DQueueOfferFunction</code>.
 */
public class DQueueOfferFunctionArguments implements DataSerializable {

  private String memberId;

  private byte[] value;
  
  private byte[] userData;

  private boolean possibleDuplicate;

  public DQueueOfferFunctionArguments() {}

  public DQueueOfferFunctionArguments(String memberId, Object value, Object userData) {
    this(memberId, value, userData, false);
  }

  public DQueueOfferFunctionArguments(String memberId, Object value, Object userData, boolean possibleDuplicate) {
	    this.memberId = memberId;
	    try {
	      this.value = BlobHelper.serializeToBlob(value);
	      this.userData = BlobHelper.serializeToBlob(userData);
	    } catch (Exception e) {
	      throw new IllegalArgumentException(e);
	    }
	    this.possibleDuplicate = possibleDuplicate;
	  }

  public String getMemberId() {
    return this.memberId;
  }

  public byte[] getSerializedValue() {
    return this.value;
  }

  public Object getValue() {
    try {
      return BlobHelper.deserializeBlob(this.value);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
  
  public byte[] getSerializedUserData()
  {
	return this.userData;
  }
  
  public Object getUserData() {
	try {
	  return BlobHelper.deserializeBlob(this.userData);
    } catch (Exception e) {
	  throw new IllegalArgumentException(e);
	}
  }

  public boolean getPossibleDuplicate() {
    return this.possibleDuplicate;
  }

  public void setPossibleDuplicate(boolean possibleDuplicate) {
    this.possibleDuplicate = possibleDuplicate;
  }

  public void toData(DataOutput out) throws IOException {
    DataSerializerEx.writeUTF(this.memberId, out);
    DataSerializer.writeByteArray(this.value, out);
    DataSerializer.writeByteArray(userData, out);
    out.writeBoolean(this.possibleDuplicate);
  }

  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    this.memberId = DataSerializerEx.readUTF(in);
//    byte[] v = DataSerializer.readByteArray(in);
//    this.value = new byte[v.length-1];
//    System.arraycopy(v, 1, value, 0, v.length-1);
//    byte[] u = DataSerializer.readByteArray(in);
//    this.userData = new byte[u.length-1];
//    System.arraycopy(u, 1, userData, 0, u.length-1);
    this.value = DataSerializer.readByteArray(in);
    this.userData =  DataSerializer.readByteArray(in);;
    this.possibleDuplicate = in.readBoolean();
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer()
      .append("DQueueOfferFunctionArguments[")
      .append("memberId=").append(this.memberId)
      .append("; value=").append(this.value)
      .append("; possibleDuplicate=").append(this.possibleDuplicate)
      .append("]");
    return buffer.toString();
  }

  public static void registerInstantiator(int id) {
    Instantiator.register(new Instantiator(DQueueOfferFunctionArguments.class, id) {
      public DataSerializable newInstance() {
        return new DQueueOfferFunctionArguments();
      }
    });
  }
}