package com.gemstone.gemfire.addon.dq.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.Instantiator;
import com.gemstone.gemfire.addon.util.DataSerializerEx;
import com.gemstone.gemfire.internal.util.BlobHelper;

public class DQueueKey implements DataSerializable {

	private static final long serialVersionUID = 1L;

  private String memberId;

  private Object type;

  private int sequenceNumber;

  private boolean possibleDuplicate;
  
  private byte[] userData;

  public DQueueKey() {}

  public DQueueKey(String memberId, Object type, int sequenceNumber, byte[] serializedUserData, boolean possibleDuplicate) {
    this.memberId = memberId;
    this.type = type;
    this.sequenceNumber = sequenceNumber;
    this.possibleDuplicate = possibleDuplicate;
    this.userData = serializedUserData;
	}

  public String getMemberId() {
    return memberId;
  }

  public Object getType() {
    return type;
  }

  public int getSequenceNumber() {
    return this.sequenceNumber;
  }

  public boolean getPossibleDuplicate() {
    return this.possibleDuplicate;
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
  
  protected void setPossibleDuplicate(boolean possibleDuplicate) {
    this.possibleDuplicate = possibleDuplicate;
  }

	public void fromData(DataInput in) throws IOException,
			ClassNotFoundException {
    this.memberId = DataSerializerEx.readUTF(in);
    this.type = DataSerializer.readObject(in);
    this.sequenceNumber = DataSerializer.readPrimitiveInt(in);
    this.userData = DataSerializer.readByteArray(in);;
	}

	public void toData(DataOutput out) throws IOException {
    DataSerializerEx.writeUTF(this.memberId, out);
    DataSerializer.writeObject(this.type, out);
    DataSerializer.writePrimitiveInt(this.sequenceNumber, out);
    DataSerializer.writeByteArray(this.userData, out);
	}

  public String toString() {
    StringBuffer buffer = new StringBuffer()
      .append("DQueueKey[")
      .append("memberId=").append(this.memberId)
      .append("; type=").append(this.type)
      .append("; sequenceNumber=").append(this.sequenceNumber)
      .append("; possibleDuplicate=").append(this.possibleDuplicate)
      .append("]");
    return buffer.toString();
  }

  public int hashCode() {
    int result = 17;
    final int mult = 37;

    result = mult * result + this.sequenceNumber;
    result = mult * result + this.type.hashCode();
    result = mult * result + this.memberId.hashCode();

    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || !(obj instanceof DQueueKey)) {
      return false;
    }

    DQueueKey that = (DQueueKey) obj;
    return (
      this.sequenceNumber == that.sequenceNumber
      && this.type.equals(that.type)
      && this.memberId.equals(that.memberId)
    );
  }

  public static void registerInstantiator(int id) {
    Instantiator.register(new Instantiator(DQueueKey.class, id) {
      public DataSerializable newInstance() {
        return new DQueueKey();
      }
    });
  }
}
