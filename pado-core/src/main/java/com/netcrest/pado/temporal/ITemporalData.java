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


/**
 * ITemporalData should be implemented by all temporal data classes.
 * Although the temporal data framework also supports non-ITemporalData classes,
 * it is recommended that ITemporalData should be implemented to gain the
 * following important benefit:
 * <p>
 * <blockquote>The ability to create GemFire compact range indexes on overflow regions. 
 * A compact range index is an index of an object attribute at the first call level of
 * an object graph. For example, GemFire supports creating an index on 
 * Foo.getX() but not on Foo.getY().getZ().</blockquote> 
 * 
 * Because temporal data is by nature historical data, storing all of the
 * data in memory may not be feasible. The overflow/persistence capability
 * eases the memory constraints.
 * <p>
 * To implement ITemporalData, the following code must be added. The supplied 
 * Eclipse plug-in auto-generates this code.
 * 
 * <pre>
 * protected ITemporalValue __temporalValue;
 * public ITemporalValue<K> getTemporalValue()
 * {
 *    return __temporalValue;
 * }
 * 
 * public void setTemporalValue(ITemporalValue temporalValue)
 * {
 *    this.__temporalValue = temporalValue;
 * }
 * 
 * public void fromData(DataInput input) throws IOException, ClassNotFoundException
 * {
 *    __temporalValue = DataSerializer.readObject(input);
 *    __temporalValue.setData(this);
 * }
 * 
 * public void toData(DataOutput output) throws IOException
 * {
 *   DataSerializer.writeObject(__temporalValue, output);
 * }
 * 
 * public void readTemporal(DataInput input) throws IOException, ClassNotFoundException
 * {
 *    // Read all fields from the input stream
 * }
 * public void writeTemporal(DataOutput output) throws IOException
 * {
 *    // Write all fields to the output stream
 * }
 * </pre>
 * 
 * @author dpark
 * 
 * @param <K>
 *            Identity key type
 */
public interface ITemporalData<K>
{
	/**
	 * Returns the temporal value used by the temporal data framework.
	 */
	ITemporalValue<K> __getTemporalValue();

	/**
	 * Internal use only. Sets the temporal value object by the temporal data 
	 * framework. The application must not invoke this method.
	 * 
	 * @param value The temporal value created by the framework.
	 */
	void __setTemporalValue(ITemporalValue<K> value);
	
	/**
	 * Returns the temporal value object. If the temporal data object is a
	 * temporal wrapper then it returns the actual value object, otherwise, it
	 * returns this object.
	 */
	Object getValue();
	
}
