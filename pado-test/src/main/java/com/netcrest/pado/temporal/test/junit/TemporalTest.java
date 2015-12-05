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
package com.netcrest.pado.temporal.test.junit;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.AttachmentSetFactory;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalData;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.test.TemporalLoader;
import com.netcrest.pado.temporal.test.data.Position;

public class TemporalTest
{
	private static IPado pado;
	private static ITemporalBiz<Object, KeyMap> temporalBiz;
	private static Set<ITemporalKey<Object>> temporalKeySet;
	private static TemporalLoader loader;
	private static Set<TemporalEntry<Object, KeyMap>> temporalEntrySet;
	
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		temporalBiz = catalog.newInstance(ITemporalBiz.class, "position");
		loader = new TemporalLoader();
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
	@Before
	public void testPut() throws Exception
	{
		System.out.println("TemporalTst.testPut()");
		System.out.println("---------------------");
		temporalEntrySet = loader.loadPositionsByPut(temporalBiz, 100);
//		Assert.assertEquals(100, temporalEntrySet.size());
	}
	
	@Before
	public void testBulkLoad() throws Exception
	{
		System.out.println("TemporalTst.testBulkLoad()");
		System.out.println("--------------------------");
		temporalEntrySet = loader.bulkLoadPositions(temporalBiz, 100, 10, true);
//		Assert.assertEquals(100, temporalEntrySet.size());
	}
	
	@Test
	public void testGet() throws Exception
	{
		System.out.println("TemporalTst.testGet()");
		System.out.println("---------------------");
		Assert.assertNotNull(temporalEntrySet);
		for (TemporalEntry entry : temporalEntrySet) {
			ITemporalKey<Object> key = entry.getTemporalKey();
			ITemporalData<Object> data = entry.getTemporalData();
			KeyMap positionOrg = (KeyMap)((TemporalData)data).getValue();
			KeyMap position = temporalBiz.get(key.getIdentityKey(), key.getStartValidTime(), key.getWrittenTime());
			System.out.println(key);
			System.out.println("Get: " + key.getIdentityKey() + "=" + position);
			Assert.assertEquals(positionOrg.get(Position.KSecId), position.get(Position.KSecId));
		}
	}
	
	@Test
	public void testGetEntries() throws Exception
	{
		System.out.println("TemporalTst.testGetEntries()");
		System.out.println("----------------------------");
		Map<ITemporalKey<Object>, ITemporalData<Object>> map = temporalBiz.getEntries(System.currentTimeMillis());
		Assert.assertNotNull(map);
		Assert.assertTrue(map.size() > 0);
		System.out.println("testTemporalEntries(): validAt map size=" + map.size());
		map = temporalBiz.getEntries(System.currentTimeMillis(), System.currentTimeMillis());
		Assert.assertNotNull(map);
		Assert.assertTrue(map.size() > 0);
		System.out.println("testTemporalEntries(): validAt, asOf map size=" + map.size());
	}
	
	@Test
	public void testGetKeySet() throws Exception
	{
		System.out.println("TemporalTst.testGetKeySet()");
		System.out.println("---------------------------");
		Set<ITemporalKey<Object>> set = temporalBiz.getKeySet(System.currentTimeMillis());
		Assert.assertNotNull(set);
		Assert.assertTrue(set.size() > 0);
		System.out.println("testGetKeySet(): validAt set size=" + set.size());
		set = temporalBiz.getKeySet(System.currentTimeMillis(), System.currentTimeMillis());
		Assert.assertNotNull(set);
		Assert.assertTrue(set.size() > 0);
		System.out.println("testGetKeySet(): validAt, asOf set size=" + set.size());
	}


	@Test
	public void testGet2() throws Exception
	{
		System.out.println("TemporalTst.testGet2()");
		System.out.println("----------------------");
		Set<KeyMap> set = temporalBiz.get(System.currentTimeMillis());
		Assert.assertNotNull(set);
		Assert.assertTrue(set.size() > 0);
		System.out.println("testGet2(): validAt set size=" + set.size());
		set = temporalBiz.get(System.currentTimeMillis(), System.currentTimeMillis());
		Assert.assertNotNull(set);
		Assert.assertTrue(set.size() > 0);
		System.out.println("testGet2(): validAt, asOf set size=" + set.size());
	}
	
	@Test
	public void testGetAttachments() throws Exception
	{
		System.out.println("TemporalTst.testGetEntries()");
		System.out.println("----------------------------");
		Assert.assertNotNull(temporalEntrySet);
		
		AttachmentSet<Object> as = new AttachmentSetFactory<Object>().createAttachmentSet();
		as.setGridPath("position");
		Set<Object> attachmentIdentitySet = new HashSet();
		as.setAttachments(attachmentIdentitySet);
		for (TemporalEntry<Object, KeyMap> entry : temporalEntrySet) {
			Object identityKey = entry.getTemporalKey().getIdentityKey();
			attachmentIdentitySet.add(identityKey);
		}
		List<KeyMap> list = temporalBiz.getAttachments(as);
		Assert.assertNotNull(list);
		System.out.println("testGetAttachments(): list size=" + list.size());
//		Assert.assertEquals(attachmentIdentitySet.size(), list.size());
		list = temporalBiz.getAttachments(as, System.currentTimeMillis());
		Assert.assertNotNull(list);
		System.out.println("testGetAttachments(): list validAt size=" + list.size());
//		Assert.assertEquals(attachmentIdentitySet.size(), list.size());
		list = temporalBiz.getAttachments(as, System.currentTimeMillis(), System.currentTimeMillis());
		Assert.assertNotNull(list);
		System.out.println("testGetAttachments(): list validAt, asOf size=" + list.size());
//		Assert.assertEquals(attachmentIdentitySet.size(), list.size());
	}
	
	@Test
	public void testGetAttachmentsArray() throws Exception
	{
		System.out.println("TemporalTst.testGetAttachmentsArray()");
		System.out.println("-------------------------------------");
		Assert.assertNotNull(temporalEntrySet);
	
		Set<Object> attachmentIdentitySet = new HashSet();
		for (TemporalEntry<Object, KeyMap> entry : temporalEntrySet) {
			Object identityKey = entry.getTemporalKey().getIdentityKey();
			attachmentIdentitySet.add(identityKey);
		}
		
		AttachmentSet<Object> asets[] = new AttachmentSet[3];
		AttachmentSetFactory<Object> attachmentSetFactory = new AttachmentSetFactory<Object>();
		for (int i = 0; i < asets.length; i++) {
			asets[i] = attachmentSetFactory.createAttachmentSet();
			asets[i].setGridPath("position");
			asets[i].setAttachments(attachmentIdentitySet);
		}

		List<KeyMap>[] list = temporalBiz.getAttachments(asets);
		Assert.assertNotNull(list);
		System.out.println("testGetAttachmentsArray(): list.length=" + list.length);
		for (int i = 0; i < list.length; i++) {
			System.out.println("   list[" + i + "]=" + list[i].size());
		}
		list = temporalBiz.getAttachments(asets, System.currentTimeMillis());
		Assert.assertNotNull(list);
		System.out.println("testGetAttachmentsArray(): list validAt length=" +  list.length);
		for (int i = 0; i < list.length; i++) {
			System.out.println("   list[" + i + "]=" + list[i].size());
		}
		list = temporalBiz.getAttachments(asets, System.currentTimeMillis(), System.currentTimeMillis());
		Assert.assertNotNull(list);
		System.out.println("testGetAttachmentsArray(): list validAt, asOf length="  + list.length);
		for (int i = 0; i < list.length; i++) {
			System.out.println("   list[" + i + "]=" + list[i].size());
		}

	}
	
	@Test
	public void testGetAttachmentsEntries() throws Exception
	{
		System.out.println("TemporalTst.testGetAttachmentsEntries()");
		System.out.println("---------------------------------------");
		Assert.assertNotNull(temporalEntrySet);
		
		AttachmentSet<Object> as = new AttachmentSetFactory<Object>().createAttachmentSet();
		as.setGridPath("position");
		Set<Object> attachmentIdentitySet = new HashSet();
		as.setAttachments(attachmentIdentitySet);
		for (TemporalEntry<Object, KeyMap> entry : temporalEntrySet) {
			Object identityKey = entry.getTemporalKey().getIdentityKey();
			attachmentIdentitySet.add(identityKey);
		}
		Map<ITemporalKey<Object>, ITemporalData<Object>> map = temporalBiz.getAttachmentsEntries(as);
		Assert.assertNotNull(map);
		System.out.println("testGetAttachmentsEntries(): map size=" + map.size());
		map = temporalBiz.getAttachmentsEntries(as, System.currentTimeMillis());
		Assert.assertNotNull(map);
		System.out.println("testGetAttachmentsEntries(): map validAt size=" + map.size());
		map = temporalBiz.getAttachmentsEntries(as, System.currentTimeMillis(), System.currentTimeMillis());
		Assert.assertNotNull(map);
		System.out.println("testGetAttachmentsEntries(): map validAt, asOf size=" + map.size());
	}
	
//	@Test
//	public void testAttachments() throws Exception
//	{
//		temporalBiz.putAttachments(identityKey, value, attachmentIdentityKeySets)
//	}
	
	@Test
	public void testGetAttachmentsEntriesArray() throws Exception
	{
		System.out.println("TemporalTst.testGetAttachmentsEntriesArray()");
		System.out.println("--------------------------------------------");
		Assert.assertNotNull(temporalEntrySet);
		
		Set<Object> attachmentIdentitySet = new HashSet();
		for (TemporalEntry<Object, KeyMap> entry : temporalEntrySet) {
			Object identityKey = entry.getTemporalKey().getIdentityKey();
			attachmentIdentitySet.add(identityKey);
		}
		
		AttachmentSet<Object> asets[] = new AttachmentSet[3];
		AttachmentSetFactory<Object> attachmentSetFactory = new AttachmentSetFactory<Object>();
		for (int i = 0; i < asets.length; i++) {
			asets[i] = attachmentSetFactory.createAttachmentSet();
			asets[i].setGridPath("position");
			asets[i].setAttachments(attachmentIdentitySet);
		}

		Map<ITemporalKey<Object>, ITemporalData<Object>>[] map = temporalBiz.getAttachmentsEntries(asets);
		Assert.assertNotNull(map);
		System.out.println("testGetAttachmentsEntriesArray(): map.length=" + map.length);
		for (int i = 0; i < map.length; i++) {
			System.out.println("   map[" + i + "]=" + map[i].size());
		}
		map = temporalBiz.getAttachmentsEntries(asets, System.currentTimeMillis());
		Assert.assertNotNull(map);
		System.out.println("testGetAttachmentsEntriesArray(): map validAt length=" +  map.length);
		for (int i = 0; i < map.length; i++) {
			System.out.println("   list[" + i + "]=" + map[i].size());
		}
		map = temporalBiz.getAttachmentsEntries(asets, System.currentTimeMillis(), System.currentTimeMillis());
		Assert.assertNotNull(map);
		System.out.println("testGetAttachmentsEntriesArray(): map validAt, asOf length="  + map.length);
		for (int i = 0; i < map.length; i++) {
			System.out.println("   list[" + i + "]=" + map[i].size());
		}

	}
	
	@Test
	public void testGetAttachmentsKeys() throws Exception
	{
		System.out.println("TemporalTst.testGetAttachmentsKeys()");
		System.out.println("------------------------------------");
		Assert.assertNotNull(temporalEntrySet);
	
		AttachmentSet<Object> as = new AttachmentSetFactory<Object>().createAttachmentSet();
		as.setGridPath("position");
		Set<Object> attachmentIdentitySet = new HashSet();
		as.setAttachments(attachmentIdentitySet);
		for (TemporalEntry<Object, KeyMap> entry : temporalEntrySet) {
			Object identityKey = entry.getTemporalKey().getIdentityKey();
			attachmentIdentitySet.add(identityKey);
		}
		Set<ITemporalKey<Object>> set = temporalBiz.getAttachmentsKeys(as);
		Assert.assertNotNull(set);
		System.out.println("testGetAttachmentsKeys(): set size=" + set.size());
//		Assert.assertEquals(attachmentIdentitySet.size(), set.size());
		set = temporalBiz.getAttachmentsKeys(as, System.currentTimeMillis());
		Assert.assertNotNull(set);
		System.out.println("testGetAttachmentsKeys(): set validAt size=" + set.size());
//		Assert.assertEquals(attachmentIdentitySet.size(), set.size());
		set = temporalBiz.getAttachmentsKeys(as, System.currentTimeMillis(), System.currentTimeMillis());
		Assert.assertNotNull(set);
		System.out.println("testGetAttachmentsKeys(): set validAt, asOf size=" + set.size());
//		Assert.assertEquals(attachmentIdentitySet.size(), set.size());
	}
	
	@Test
	public void testGetAttachmentsKeysArray() throws Exception
	{
		System.out.println("TemporalTst.testGetAttachmentsKeysArray()");
		System.out.println("-----------------------------------------");
		Assert.assertNotNull(temporalEntrySet);
		
		Set<Object> attachmentIdentitySet = new HashSet();
		for (TemporalEntry<Object, KeyMap> entry : temporalEntrySet) {
			Object identityKey = entry.getTemporalKey().getIdentityKey();
			attachmentIdentitySet.add(identityKey);
		}
		
		AttachmentSet<Object> asets[] = new AttachmentSet[3];
		AttachmentSetFactory<Object> attachmentSetFactory = new AttachmentSetFactory<Object>();
		for (int i = 0; i < asets.length; i++) {
			asets[i] = attachmentSetFactory.createAttachmentSet();
			asets[i].setGridPath("position");
			asets[i].setAttachments(attachmentIdentitySet);
		}

		Set<ITemporalKey<Object>>[] sets = temporalBiz.getAttachmentsKeys(asets);
		Assert.assertNotNull(sets);
		System.out.println("testGetAttachmentsKeysArray(): list.length=" + sets.length);
		for (int i = 0; i < sets.length; i++) {
			System.out.println("   sets[" + i + "]=" + sets[i].size());
//			Assert.assertEquals(attachmentIdentitySet.size(), sets[i].size());
		}
		sets = temporalBiz.getAttachmentsKeys(asets, System.currentTimeMillis());
		Assert.assertNotNull(sets);
		System.out.println("testGetAttachmentsKeysArray(): list validAt length=" +  sets.length);
		for (int i = 0; i < sets.length; i++) {
			System.out.println("   sets[" + i + "]=" + sets[i].size());
//			Assert.assertEquals(attachmentIdentitySet.size(), sets[i].size());
		}
		sets = temporalBiz.getAttachmentsKeys(asets, System.currentTimeMillis(), System.currentTimeMillis());
		Assert.assertNotNull(sets);
		System.out.println("testGetAttachmentsKeysArray(): sets validAt, asOf length="  + sets.length);
		for (int i = 0; i < sets.length; i++) {
			System.out.println("   sets[" + i + "]=" + sets[i].size());
//			Assert.assertEquals(attachmentIdentitySet.size(), sets[i].size());
		}
	}
}
