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

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AttachmentResults contains the value of the host identity key and all of its
 * attachment values.
 * 
 * @author dpark
 * 
 * @param <V>
 *            The host identity key's value.
 */
public abstract class AttachmentResults<V>
{
	protected V value;

	/**
	 * attachmentValues contains a map of attachment lists, i.e., &lt;name,
	 * attachment values&gt;. The name is provided by a Pado plug-in which
	 * assigns the field name identified by the annotation @TemporalAttachment.
	 * If annotation is not used, then the default name "default" is assigned.
	 */
	protected Map<String, List<V>> attachmentValues;

	/**
	 * Returns the host identity key's value.
	 */
	public V getValue()
	{
		return value;
	}

	/**
	 * Sets the host identity key's value.
	 * 
	 * @param value
	 *            The host identity key's value.
	 */
	public void setValue(V value)
	{
		this.value = value;
	}

	/**
	 * Returns the attachment values. It returns null, if there are no
	 * attachments. The list array maps to the attachment set array specified by
	 * Temporal.put().
	 */
	public Map<String, List<V>> getAttachmentValues()
	{
		return attachmentValues;
	}

	/**
	 * Sets the attachment values. The list array must map the attachment set
	 * array specified by Temporal.put().
	 * 
	 * @param attachmentValues
	 */
	public void setAttachmentValues(Map<String, List<V>> attachmentValues)
	{
		this.attachmentValues = attachmentValues;
	}

	/**
	 * Dumps this object contents to {@link System#out}
	 */
	public void dump()
	{
		dump(System.out);
	}

	/**
	 * Dumps this object contents to the specified output.
	 * 
	 * @param output
	 *            Output stream
	 */
	public void dump(PrintStream output)
	{
		output.println("value=" + value);
		if (attachmentValues == null) {
			output.println("attchmentValues=null");
		} else {
			if (attachmentValues.size() == 0) {
				output.println("attchmentValues: empty");
			} else {
				output.println("attchmentValues:");
				Set<Map.Entry<String, List<V>>> set = attachmentValues.entrySet();
				for (Map.Entry<String, List<V>> entry : set) {
					output.println("   " + entry.getKey() + ":");
					for (Object obj : entry.getValue()) {
						output.println("      " + obj);
					}
				}
			}
		}
	}
}
