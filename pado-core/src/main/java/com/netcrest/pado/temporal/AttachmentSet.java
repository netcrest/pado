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

import java.util.Set;

/**
 * AttachmentSet contains a set of attachment identity keys that are part of the
 * host identity key. The host identity key is the identity key specified in
 * Temporal.put(). A typical use of AttachmentSet is for establishing
 * parent/child relationships. An identity key can have multiple attachment sets
 * representing multiple entity relationships.
 * <p>
 * Identity keys in AttachementSet can belong to a different grid path than the
 * host identity key's grid path. This is done by specifying the grid path.
 * 
 * @author dpark
 * 
 * @param <K>
 *            identity key
 */
public abstract class AttachmentSet<K>
{
	/**
	 * Attachment name. If undefined, then "default" is assigned.
	 */
	protected String name = "default";

	/**
	 * Grid ID. Grid ID is not shipped to the servers. For client use only.
	 */
	private transient String gridId;

	/**
	 * Grid path
	 */
	protected String gridPath;

	/**
	 * Query statement
	 */
	protected String queryStatement;

	/**
	 * Attachments
	 */
	protected Set<K> attachments;

	/**
	 * Filter
	 */
	protected IFilter filter;

	/**
	 * Comment for UI display purposes
	 */
	protected String comment;

	/**
	 * Constructs an emtpy AttachmentSet object.
	 */
	public AttachmentSet()
	{
	}

	/**
	 * Constructs a AttachementSet object with the specified attachments. It
	 * assigns the name "default".
	 * 
	 * @param attachments
	 *            Identity key attachments.
	 */
	public AttachmentSet(Set<K> attachments)
	{
		this("default", attachments, null, null, null, null);
	}

	/**
	 * Constructs an AttachmentSet object with the specified parameters.
	 * 
	 * @param name
	 *            Attachment set name
	 * @param attachments
	 *            Attachment set
	 */
	public AttachmentSet(String name, Set<K> attachments)
	{
		this(name, attachments, null, null, null, null);
	}

	/**
	 * Constructs an AttachmentSet object with the specified parameters.
	 * 
	 * @param name
	 *            Attachment set name
	 * @param attachments
	 *            Attachment set
	 * @param filter
	 *            Filter
	 */
	public AttachmentSet(String name, Set<K> attachments, IFilter filter)
	{
		this(name, attachments, null, filter, null, null);
	}

	/**
	 * Constructs an AttachmentSet object with the specified parameters.
	 * 
	 * @param name
	 *            Attachment set name
	 * @param attachments
	 *            Attachment set
	 * @param gridPath
	 *            Grid path
	 */
	public AttachmentSet(String name, Set<K> attachments, String gridPath)
	{
		this(name, attachments, null, null, gridPath, null);
	}

	/**
	 * Constructs an AttachmentSet object with the specified parameters.
	 * 
	 * @param name
	 *            set name
	 * @param attachments
	 *            Attachment set
	 * @param queryStatement
	 *            Query statement
	 * @param filter
	 *            filter
	 * @param gridPath
	 *            Grid path
	 * @param comment
	 *            Comment
	 */
	public AttachmentSet(String name, Set<K> attachments, String queryStatement, IFilter filter, String gridPath,
			String comment)
	{
		setName(name);
		this.attachments = attachments;
		this.queryStatement = queryStatement;
		this.filter = filter;
		this.gridPath = gridPath;
		this.comment = comment;
	}

	/**
	 * Returns the grid ID. If null, then the default grid ID should be used.
	 */
	public String getGridId()
	{
		return gridId;
	}

	/**
	 * Sets the grid ID.
	 * 
	 * @param gridId
	 *            The ID of the grid this attachment set to be sent. If null,
	 *            then the default grid ID should be used.
	 */
	public void setGridId(String gridId)
	{
		this.gridId = gridId;
	}

	/**
	 * Returns the grid path. The default is null.
	 */
	public String getGridPath()
	{
		return gridPath;
	}

	/**
	 * Sets the grid path. If null, then it assumes the attachments are in the
	 * host identity key's grid path.
	 * 
	 * @param gridPath
	 *            The grid path.
	 */
	public void setGridPath(String gridPath)
	{
		this.gridPath = gridPath;
	}

	/**
	 * Returns attachments. It returns null if undefined
	 */
	public Set<K> getAttachments()
	{
		return attachments;
	}

	/**
	 * Sets attachments.
	 * 
	 * @param attachments
	 *            Attachment set
	 */
	public void setAttachments(Set<K> attachments)
	{
		this.attachments = attachments;
	}

	/**
	 * Returns filter. It returns null if undefined.
	 */
	public IFilter getFilter()
	{
		return filter;
	}

	/**
	 * Sets the specified filter.
	 * 
	 * @param filter
	 *            Filter
	 */
	public void setFilter(IFilter filter)
	{
		this.filter = filter;
	}

	/**
	 * Returns the attachment set name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name for this AttachementSet instance. This name <i>must</i> be
	 * unique within a temporal operation, which may include more than one
	 * AttachmentSet objects.
	 * 
	 * @param name
	 *            AttachmentSet name.
	 */
	public void setName(String name)
	{
		if (name == null) {
			this.name = "default";
		} else {
			this.name = name;
		}
	}

	/**
	 * Returns the query statement. It returns null if undefined.
	 */
	public String getQueryStatement()
	{
		return queryStatement;
	}

	/**
	 * Sets the query statement for including temporal entities in addition to
	 * the attachments.
	 * 
	 * @param queryStatement
	 *            Query statement to be executed in the grid. Supported query
	 *            languages are GemFire OQL and Apache Lucene.
	 */
	public void setQueryStatement(String queryStatement)
	{
		this.queryStatement = queryStatement;
	}

	/**
	 * Returns comment. It returns null if undefined.
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * Sets comment. Comment describes the attachments and is used for display
	 * purposes.
	 * 
	 * @param comment
	 *            Comment
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	@Override
	public String toString()
	{
		return "AttachmentSet [name=" + name + ", gridPath=" + gridPath + ", queryStatement=" + queryStatement
				+ ", attachments=" + attachments + ", filter=" + filter + ", comment=" + comment + "]";
	}
}
