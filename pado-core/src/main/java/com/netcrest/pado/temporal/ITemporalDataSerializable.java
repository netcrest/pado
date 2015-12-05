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
package com.netcrest.pado.temporal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * ITemporalData should be implemented by all temporal data classes. Although
 * the temporal data framework also supports non-ITemporalData classes, it is
 * recommended that ITemporalData should be implemented to gain the following
 * important benefits:
 * <p>
 * <ul><li>The ability to create GemFire compact range indexes on overflow
 * regions. A compact range index is an index of an object attribute at the
 * first call level of an object graph. For example, GemFire supports creating
 * an index on <code>Foo.getX()</code> but not on <code>Foo.getY().getZ()</code>
 * .</li>
 * <li>Because temporal data is by nature historic, storing all of the data in
 * memory may not be feasible. The overflow/persistence capability may ease the
 * memory constraints.</li>
 * </ul>
 * <p>
 * To implement ITemporalData, the following code must be added. The supplied
 * Eclipse plug-in auto generates this code.
 * 
 * <pre>
 * private TemporalValue&lt;K&gt; temporalValue;
 * 
 * public ITemporalValue&lt;K&gt; getTemporalValue()
 * {
 * 	return temporalValue;
 * }
 * 
 * private void deserializeData()
 * {
 * 	if (temporalValue != null) {
 * 		temporalValue.deserializeData();
 * 	}
 * }
 * 
 * public void fromData(DataInput input) throws IOException, ClassNotFoundException
 * {
 * 	temporalValue = DataSerializer.readObject(input);
 * 	temporalValue.setData(this);
 * }
 * 
 * public void toData(DataOutput output) throws IOException
 * {
 * 	DataSerializer.writeObject(temporalValue, output);
 * }
 * 
 * public void readTemporal(DataInput input) throws IOException, ClassNotFoundException
 * {
 * 	// Read all fields from the input stream
 * }
 * 
 * public void writeTemporal(DataOutput output) throws IOException
 * {
 * 	// Write all fields to the output stream
 * }
 * </pre>
 * 
 * @author dpark
 * 
 * @param <K>
 *            Identity key type
 */
public interface ITemporalDataSerializable<K> extends ITemporalData<K>
{
	/**
	 * Reads temporal data from the specified input stream.
	 * 
	 * @param input
	 *            Input stream
	 * @throws IOException
	 *             Thrown if reading from the input stream fails
	 * @throws ClassNotFoundException
	 *             Thrown if the data read from the input stream requires
	 *             undefined class
	 */
	void readTemporal(DataInput input) throws IOException, ClassNotFoundException;

	/**
	 * Writes temporal data to the specified output stream.
	 * 
	 * @param output
	 *            Output stream
	 * @throws IOException
	 *             Thrown if writing to the output stream fails
	 */
	void writeTemporal(DataOutput output) throws IOException;

	/**
	 * Reads the temporal attributes from the specified input stream.
	 * 
	 * @param input
	 *            Input stream
	 * @throws IOException
	 *             Thrown if reading from the input stream fails
	 * @throws ClassNotFoundException
	 *             Thrown if the data read from the input stream requires an
	 *             undefined class
	 */
	void readTemporalAttributes(DataInput input) throws IOException, ClassNotFoundException;

	/**
	 * Writes the temporal attributes to the specified output stream.
	 * 
	 * @param output
	 *            Output stream
	 * @throws IOException
	 *             Thrown if writing to the output stream fails
	 */
	void writeTemporalAttributes(DataOutput output) throws IOException;
}
