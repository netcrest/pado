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
 * ITemporalDelta should be implemented for temporal classes that enable Pado
 * delta services. The implementing class must provide logic to serialize or
 * deserialize deltas.
 * 
 * @author dpark
 * 
 */
public interface ITemporalDelta
{
	/**
	 * Returns true if the object contains deltas.
	 */
	boolean hasDelta();

	/**
	 * Reads deltas from the specified input stream.
	 * 
	 * @param in
	 *            Input stream
	 * @throws IOException
	 *             Thrown if reading from the input stream fails
	 * @throws ClassNotFoundException
	 *             Thrown if the data read from the input stream requires an
	 *             undefined class
	 */
	void readDelta(DataInput in) throws IOException, ClassNotFoundException;

	/**
	 * Writes deltas to the specified output stream.
	 * 
	 * @param out
	 *            Output stream
	 * @throws IOException
	 *             Thrown if writing to the output stream fails
	 */
	void writeDelta(DataOutput out) throws IOException;
}
