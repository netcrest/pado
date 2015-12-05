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
 * IAttachmentSetFactory provides access to the underlying data grid specific
 * AttachementSet objects.
 * 
 * @author dpark
 * 
 * @param <K>
 */
public interface IAttachmentSetFactory<K>
{
	/**
	 * Returns a new empty AttachmentSet object. The returned AttachmentSet has
	 * the name "default".
	 */
	public AttachmentSet<K> createAttachmentSet();

	/**
	 * Returns a new AttachmentSet object set to the specified attachments. The
	 * returned AttachmentSet has the name "default".
	 * 
	 * @param attachments
	 *            Attachment set
	 */
	public AttachmentSet<K> createAttachmentSet(Set<K> attachments);

	/**
	 * Returns a new AttachmentSet object set to the specified name and
	 * attachments.
	 * 
	 * @param name
	 *            Attachment set name
	 * @param attachments
	 *            Attachment set
	 */
	public AttachmentSet<K> createAttachmentSet(String name, Set<K> attachments);

	/**
	 * Returns a new AttachmentSet object set to the specified name and filter.
	 * 
	 * @param name
	 *            Attachment set name
	 * @param attachments
	 *            Attachment set
	 * @param filter
	 *            Filter
	 */
	public AttachmentSet<K> createAttachmentSet(String name, Set<K> attachments, IFilter filter);

	/**
	 * Returns a new AttachmentSet object set to the specified name,
	 * attachments, and grid path.
	 * 
	 * @param name
	 *            Attachment set name
	 * @param attachments
	 *            Attachment set
	 * @param gridPath
	 *            Grid path
	 */
	public AttachmentSet<K> createAttachmentSet(String name, Set<K> attachments, String gridPath);
}
