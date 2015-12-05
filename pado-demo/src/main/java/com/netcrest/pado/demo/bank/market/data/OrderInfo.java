/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.demo.bank.market.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.netcrest.pado.gemfire.util.DataSerializerEx;

//create table OrderInfo
//(
//
//	            origGroupAcronym varchar(50) NOT NULL,
//	            connectacronym   varchar(50) NOT NULL,
//	            targetGroupAcronym varchar(50) NOT NULL,
//	            symbol             varchar(20) NOT NULL,
//	            clientOrdId        varchar(50) NOT NULL,
//	            handleInst         int NOT NULL,
//	            side                  int NOT NULL,
//	            ordType              int NOT NULL,
//	            timeStamp         datetime not null,
//	            transactionTime datetime not null,
//	            hostCounter       int not null,
//	            clientSessionId int not null
//	)
public class OrderInfo implements DataSerializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String origGroupAcronym;
	private String connectAcronym;
	private String targetGroupAcronym;
	private String symbol;
	private String clientOrdId;

	private int handleInst;
	private int side;
	private int ordType;
	private int hostCounter;
	private int clientSessionId;

	private Date timeStamp;
	private Date transactionTime;

	public OrderInfo()
	{
	}

	public String getOrigGroupAcronym()
	{
		return origGroupAcronym;
	}

	public void setOrigGroupAcronym(String origGroupAcronym)
	{
		this.origGroupAcronym = origGroupAcronym;
	}

	public String getConnectAcronym()
	{
		return connectAcronym;
	}

	public void setConnectAcronym(String connectAcronym)
	{
		this.connectAcronym = connectAcronym;
	}

	public String getTargetGroupAcronym()
	{
		return targetGroupAcronym;
	}

	public void setTargetGroupAcronym(String targetGroupAcronym)
	{
		this.targetGroupAcronym = targetGroupAcronym;
	}

	public String getSymbol()
	{
		return symbol;
	}

	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}

	public String getClientOrdId()
	{
		return clientOrdId;
	}

	public void setClientOrdId(String clientOrderId)
	{
		this.clientOrdId = clientOrderId;
	}

	public int getHandleInst()
	{
		return handleInst;
	}

	public void setHandleInst(int handleInst)
	{
		this.handleInst = handleInst;
	}

	public int getSide()
	{
		return side;
	}

	public void setSide(int side)
	{
		this.side = side;
	}

	public int getOrdType()
	{
		return ordType;
	}

	public void setOrdType(int ordType)
	{
		this.ordType = ordType;
	}

	public int getHostCounter()
	{
		return hostCounter;
	}

	public void setHostCounter(int hostCounter)
	{
		this.hostCounter = hostCounter;
	}

	public int getClientSessionId()
	{
		return clientSessionId;
	}

	public void setClientSessionId(int clientSessionId)
	{
		this.clientSessionId = clientSessionId;
	}

	public Date getTimeStamp()
	{
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp)
	{
		this.timeStamp = timeStamp;
	}

	public Date getTransactionTime()
	{
		return transactionTime;
	}

	public void setTransactionTime(Date transactionTime)
	{
		this.transactionTime = transactionTime;
	}

	
	@Override
	public String toString()
	{
		return "OrderInfo [origGroupAcronym=" + origGroupAcronym + ", connectAcronym=" + connectAcronym
				+ ", targetGroupAcronym=" + targetGroupAcronym + ", symbol=" + symbol + ", clientOrdId=" + clientOrdId
				+ ", handleInst=" + handleInst + ", side=" + side + ", ordType=" + ordType + ", hostCounter="
				+ hostCounter + ", clientSessionId=" + clientSessionId + ", timeStamp=" + timeStamp
				+ ", transactionTime=" + transactionTime + "]";
	}

	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		origGroupAcronym = DataSerializerEx.readUTF(input);
		connectAcronym = DataSerializerEx.readUTF(input);
		targetGroupAcronym = DataSerializerEx.readUTF(input);
		symbol = DataSerializerEx.readUTF(input);
		clientOrdId = DataSerializerEx.readUTF(input);

		handleInst = input.readInt();
		side = input.readInt();
		ordType = input.readInt();
		hostCounter = input.readInt();
		clientSessionId = input.readInt();

		timeStamp = DataSerializer.readDate(input);
		transactionTime = DataSerializer.readDate(input);
	}

	public void toData(DataOutput output) throws IOException
	{
		DataSerializerEx.writeUTF(origGroupAcronym, output);
		DataSerializerEx.writeUTF(connectAcronym, output);
		DataSerializerEx.writeUTF(targetGroupAcronym, output);
		DataSerializerEx.writeUTF(symbol, output);
		DataSerializerEx.writeUTF(clientOrdId, output);

		output.writeInt(handleInst);
		output.writeInt(side);
		output.writeInt(ordType);
		output.writeInt(hostCounter);
		output.writeInt(clientSessionId);

		DataSerializer.writeDate(timeStamp, output);
		DataSerializer.writeDate(transactionTime, output);
	}

}
