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

import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

/**
 * AttachmentSetFactory creates data-grid product specific AttachmentSet
 * objects.
 * 
 * @author dpark
 * 
 * @param <K>
 */
public class AttachmentSetFactory<K> implements IAttachmentSetFactory<K>
{
	private static Class clazz;

	static {
		try {
			clazz = PadoUtil.getClass(Constants.PROP_CLASS_TEMPORAL_ATTACHMENT_SET_FACTORY,
					Constants.DEFAULT_CLASS_TEMPORAL_ATTACHMENT_SET_FACTORY);
			clazz.newInstance(); // Create a new instance to make sure it works
		} catch (Exception e) {
			Logger.severe("AttachmentSetFactory creation error", e);
		}
	}

	private IAttachmentSetFactory<K> attachmentSetFactory;

	/**
	 * Constructs an AttachmentSetFactory object.
	 */
	@SuppressWarnings("unchecked")
	public AttachmentSetFactory()
	{
		try {
			attachmentSetFactory = (IAttachmentSetFactory<K>) clazz.newInstance();
		} catch (Exception e) {
			Logger.severe("AttachmentSetFactory creation error", e);
			throw new TemporalException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttachmentSet<K> createAttachmentSet()
	{
		return attachmentSetFactory.createAttachmentSet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentSet<K> createAttachmentSet(Set<K> attachments)
	{
		return attachmentSetFactory.createAttachmentSet(attachments);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentSet<K> createAttachmentSet(String name, Set<K> attachments)
	{
		return attachmentSetFactory.createAttachmentSet(name, attachments);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentSet<K> createAttachmentSet(String name, Set<K> attachments, IFilter filter)
	{
		return attachmentSetFactory.createAttachmentSet(name, attachments, filter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentSet<K> createAttachmentSet(String name, Set<K> attachments, String gridPath)
	{
		return attachmentSetFactory.createAttachmentSet(name, attachments, gridPath);
	}
}
