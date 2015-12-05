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
package com.netcrest.pado.temporal.gemfire;

import java.util.Set;

import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.IAttachmentSetFactory;
import com.netcrest.pado.temporal.IFilter;

public class GemfireAttachmentSetFactory<K> implements IAttachmentSetFactory<K>
{
	@Override
	public AttachmentSet<K> createAttachmentSet()
	{
		return new GemfireAttachmentSet<K>();
	}
	
	@Override
	public AttachmentSet<K> createAttachmentSet(Set attachments)
	{
		return new GemfireAttachmentSet<K>(attachments);
	}
	
	@Override
	public AttachmentSet<K> createAttachmentSet(String name, Set<K> attachments)
	{
		return new GemfireAttachmentSet<K>(name, attachments);
	}

	@Override
	public AttachmentSet<K> createAttachmentSet(String name, Set<K> attachments, IFilter filter)
	{
		return new GemfireAttachmentSet<K>(name, attachments, filter);
	}

	@Override
	public AttachmentSet<K> createAttachmentSet(String name, Set<K> attachments, String gridPath)
	{
		return new GemfireAttachmentSet<K>(name, attachments, gridPath);
	}
}
