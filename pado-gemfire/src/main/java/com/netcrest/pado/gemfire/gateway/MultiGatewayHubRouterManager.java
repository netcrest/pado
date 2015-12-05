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
package com.netcrest.pado.gemfire.gateway;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.InterestPolicy;
import com.gemstone.gemfire.cache.Operation;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.cache.SubscriptionAttributes;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.gemstone.gemfire.cache.util.Gateway;
import com.gemstone.gemfire.cache.util.GatewayHub;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.internal.DM;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.distributed.internal.MembershipListener;
import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.internal.cache.EntryEventImpl;
import com.gemstone.gemfire.internal.cache.EnumListenerEvent;
import com.gemstone.gemfire.internal.cache.EventID;
import com.gemstone.gemfire.internal.cache.GatewayHubImpl;
import com.netcrest.pado.gemfire.util.RegionUtil;
import com.netcrest.pado.internal.util.QueueDispatcher;
import com.netcrest.pado.internal.util.QueueDispatcherListener;
import com.netcrest.pado.log.Logger;

/**
 * <b> <font color="red">IMPORTANT: Note that this release does NOT support GII. Please ignore
 * the GII option. Furthermore, as with other add-ons, this component is
 * NOT officially supported. Use at your own risk.</font>
 * </b>
 * <p>
 * MultiGatewayHubRouterManager manages a collection of gateway hubs that can be
 * used to distribute events across multiple socket connections. The following
 * system property must be set.
 * 
 * <pre>
 * MultiGatewayHubRouterManager.propertiesFile=The hub.properties file path. The default path is etc/hub.properties.
 * </pre>
 * 
 * The hub.properties file honors the following properties:
 * 
 * <pre>
 * hub.includes=* or comma separated list of region full paths to include
 *         in enabling the hubs.
 * 
 * hub.excludes=comma separated list of full paths of regions which are to be excluded from
 *         enabling the hubs.
 * 
 * hub.channel=the full path of the channel region that MultiGatewayHubRouterManager
 *         uses for communicating between sites. Do NOT define this region in the
 *         cache.xml file. It is created dynamically by MultiGatewayHubRouterManager.
 *         Default: /gateway/channel
 * 
 * hub.gii=the full path of the &quot;get initiali image&quot; region used for transferring
 *         the initial image from a distributed system to a distributed system. The default path
 *         is &quot;/gateway/gii&quot;. Do &lt;b&gt;NOT&lt;/b&gt; define this region in the
 *         cache.xml file. It is created dyanmically by MultiGatewayHubRouterManager.
 *         Default:/gateway/gii
 * 
 * hub.giiStopDelay=the stop timeout delay in msec. The GII provider closes the GII gateways
 *         after sending a "gii.complete" notification to the GII caller and upon receiving
 *         an ack from the GII caller. However, as a safety measure, it will close
 *         the gateways if they are still open after this delay. The GII caller
 *         closes the GII gateways after receiving the "gii.complete" notification
 *         from the GII provider. It waits this delay before closing the GII gateways.
 *         Default: 10000 msec or 10 sec.
 * 
 * hub.system=the full path of the &quot;system&quot; region used to establish communications
 *         between the GII getter and the secondary caches.
 *         Default:/__system
 * 
 * hub.debug=true|false If true, prints debug information. Default: false.
 * </pre>
 * 
 * You must <b>NOT</b> enable gateway, i.e., do not set enable-gateway="true",
 * for the regions that are listed in the "hub.includes" property. The wild card
 * * indicates that all regions are to be included.
 * <p>
 * Example: The following example includes all regions except /root, /root/foo
 * and /root2. It also specifies the channel and gii region paths. They are
 * optional.
 * 
 * <pre>
 * hub.includes=*
 * hub.excludes=/root,/root/foo,/root2
 * hub.channel=/gateway/channel
 * hub.gii=/gateway/gii
 * </pre>
 * 
 * Note that you must explicitly list all region paths. Wild card on sub-regions
 * is not supported in this version. For example, /root2/* is not supported.
 * <p>
 * <b>Enabling MultiGatewayHubRouterManager:</b>
 * <ul>
 * <li>In the cache, invoke the following after the cache has been fully
 * initialized.</li>
 * 
 * <pre>
 * MultiGatewayHubRouterManager hubManager = MultiGatewayHubRouterManager.initialize();
 * hubManager.startHubs();
 * </pre>
 * 
 * <li>Disable gateways for all regions in cache.xml, i.e.,
 * enable-gateway=false. The default is false.</li>
 * 
 * <pre></pre>
 * <li>Enable manual-start for all gateway hubs. Set the gateway hub attribute
 * manual-start="true".</li>
 * </ul>
 * <p>
 * <b>Getting initial image (GII) from another distributed system:</b>
 * <ul>
 * <li>There are three entities involved in GII: GII Provider, GII Getter, and
 * GII Secondary Receiver.</li>
 * 
 * <pre>
 * <b>GII Provider</b> - A GII provider is a gateway site that provides the GII service to
 * another site. To enable as a GII provider, you must initialize the manager using
 * MultiGatewayHubRouterManager.initialize() as described above. For example,
 * </pre>
 * 
 * <pre>
 * MultiGatewayHubRouterManager hubManager = MultiGatewayHubRouterManager.initialize();
 * hubManager.startHubs();
 * </pre>
 * 
 * <pre>
 * <b>GII Getter</b> - A GII getter is a GII initiator that requests a GII from a GII
 * provider. There can only be one GII getter at any time. To enable the cache as
 * a GII getter, you must initialize the manager using
 * MultiGatewayHubRouterManager.initializeGII(). When you are ready to send a GII
 * request to the GII provider, you would invoke MultiGatewayHubRouterManager.getGII().
 * For example, the following code downloads the initial image from the "UK" site:
 * </pre>
 * 
 * <pre>
 * MultiGatewayHubRouterManager.initialize().getGII(&quot;UK&quot;); // blocks until the initial image is fully downloaded
 * </pre>
 * 
 * <pre>
 * <b>GII Secondary Receiver</b> - A GII secondary receiver is a secondary cache that runs in the
 * same distributed system as the GII getter. It receives GII data requested by the GII getter
 * and sent by the GII provider. As such, it does not initiate the getGII() call but it
 * simply consumes incoming GII data. To enable the cache as a GII secondary receiver, you
 * must initialize the manager using MultiGatewayHubRouterManager.initializeGIISecondary().
 * For example,
 * </pre>
 * 
 * <pre>
 * MultiGatewayHubRouterManager.initializeGIISecondary();
 * </pre>
 * 
 * <li>This version of MultiGatewayHubRouterManger executes one GII request at a
 * time. It will wait till the current GII completes before executing the next
 * one in the channel request queue. This means if US invokes getGII("UK") and
 * shortly after TK invokes getGII("UK"), the TK GII will not be executed until
 * the US GII is complete.</li>
 * 
 * <pre></pre>
 * <li>If the GII caller terminates prematurely before the GII completes, the
 * other site will immediately close the GII connection. It is, however,
 * advisable to wait till the source's hub.giiStopDelay expires before invoking
 * getGII() again. The source is where you are downloading the data from, i.e.,
 * "UK" in the above example. The default value of hub.guiiStopDelay is 10,000
 * msec or 10 sec.</li>
 * </ul>
 * <p>
 * <b>Channel and GII Gateway Hub Naming Conventions and Descriptions</b>
 * <ul>
 * <li>The channel and gii gateway hubs have the following naming conventions.
 * They must be strictly enforced.</li>
 * 
 * <pre>
 * CHANNEL.&lt;source&gt;.&lt;target&gt;
 * GII.&lt;source&gt;.&lt;target&gt;.&lt;#&gt;
 * </pre>
 * 
 * <li>
 * There must be one and only one channel gateway hub.</li>
 * 
 * <pre></pre>
 * <li>
 * Defining multiple GII gateway hubs is recommended for performance reasons.</li>
 * 
 * <pre></pre>
 * <li>
 * Example:</li>
 * 
 * <pre>
 * &lt;gateway-hub id=&quot;<b>CHANNEL.US.UK</b>&quot; port=&quot;20010&quot; manual-start=&quot;true&quot;&gt;
 *     &lt;gateway id=&quot;US&quot; early-ack=&quot;false&quot;&gt;
 *       &lt;gateway-endpoint id=&quot;UK-1&quot; host=&quot;localhost&quot; port=&quot;10010&quot;/&gt;
 *       &lt;gateway-endpoint id=&quot;UK-2&quot; host=&quot;localhost&quot; port=&quot;10020&quot;/&gt;
 *       &lt;gateway-queue overflow-directory=&quot;db/channel.us1-uk&quot; maximum-queue-memory=&quot;50&quot; batch-size=&quot;100&quot; batch-time-interval=&quot;130&quot; batch-conflation=&quot;false&quot;/&gt;
 *     &lt;/gateway&gt;
 *   &lt;/gateway-hub&gt;
 * </pre>
 * 
 * <pre>
 * &lt;gateway-hub id=&quot;<b>GII.UK.US.1</b>&quot; port=&quot;10021&quot; manual-start=&quot;true&quot;&gt;
 *     &lt;gateway id=&quot;US&quot; early-ack=&quot;false&quot;&gt;
 *       &lt;gateway-endpoint id=&quot;US-1&quot; host=&quot;localhost&quot; port=&quot;20011&quot;/&gt;
 *       &lt;gateway-endpoint id=&quot;US-2&quot; host=&quot;localhost&quot; port=&quot;20021&quot;/&gt;
 *       &lt;gateway-queue overflow-directory=&quot;db/gii.uk2-us-1&quot; maximum-queue-memory=&quot;50&quot; batch-size=&quot;100&quot; batch-time-interval=&quot;130&quot; batch-conflation=&quot;false&quot;/&gt;
 *     &lt;/gateway&gt;
 *   &lt;/gateway-hub&gt;
 * 
 *   &lt;gateway-hub id=&quot;<b>GII.UK.US.2</b>&quot; port=&quot;10022&quot; manual-start=&quot;true&quot;&gt;
 *     &lt;gateway id=&quot;US&quot; early-ack=&quot;false&quot;&gt;
 *       &lt;gateway-endpoint id=&quot;US-1&quot; host=&quot;localhost&quot; port=&quot;20012&quot;/&gt;
 *       &lt;gateway-endpoint id=&quot;US-2&quot; host=&quot;localhost&quot; port=&quot;20022&quot;/&gt;
 *       &lt;gateway-queue overflow-directory=&quot;db/gii.uk2-us-1&quot; maximum-queue-memory=&quot;50&quot; batch-size=&quot;100&quot; batch-time-interval=&quot;130&quot; batch-conflation=&quot;false&quot;/&gt;
 *     &lt;/gateway&gt;
 *   &lt;/gateway-hub&gt;
 * 
 *   &lt;gateway-hub id=&quot;<b>GII.UK.US.3</b>&quot; port=&quot;10023&quot; manual-start=&quot;true&quot;&gt;
 *     &lt;gateway id=&quot;US&quot; early-ack=&quot;false&quot;&gt;
 *       &lt;gateway-endpoint id=&quot;US-1&quot; host=&quot;localhost&quot; port=&quot;20013&quot;/&gt;
 *       &lt;gateway-endpoint id=&quot;US-2&quot; host=&quot;localhost&quot; port=&quot;20023&quot;/&gt;
 *       &lt;gateway-queue overflow-directory=&quot;db/uk2-us-1&quot; maximum-queue-memory=&quot;50&quot; batch-size=&quot;100&quot; batch-time-interval=&quot;130&quot; batch-conflation=&quot;false&quot;/&gt;
 *     &lt;/gateway&gt;
 *   &lt;/gateway-hub&gt;
 * 
 *   &lt;gateway-hub id=&quot;<b>GII.UK.US.4</b>&quot; port=&quot;10024&quot; manual-start=&quot;true&quot;&gt;
 *     &lt;gateway id=&quot;US&quot; early-ack=&quot;false&quot;&gt;
 *       &lt;gateway-endpoint id=&quot;US-1&quot; host=&quot;localhost&quot; port=&quot;20014&quot;/&gt;
 *       &lt;gateway-endpoint id=&quot;US-2&quot; host=&quot;localhost&quot; port=&quot;20024&quot;/&gt;
 *       &lt;gateway-queue overflow-directory=&quot;db/uk2-us-1&quot; maximum-queue-memory=&quot;50&quot; batch-size=&quot;100&quot; batch-time-interval=&quot;130&quot; batch-conflation=&quot;false&quot;/&gt;
 *     &lt;/gateway&gt;
 *   &lt;/gateway-hub&gt;
 * </pre>
 * 
 * </ul>
 */
public class MultiGatewayHubRouterManager
{
	public final static String PROPERTY_HUB_PROPERTIES_FILE = "MultiGatewayHubRouterManager.propertiesFile";

	public final static String DEFAULT_CHANNEL_PATH = "/gateway/channel";

	private static MultiGatewayHubRouterManager hubManager = null;

	private Cache cache;

	// data gateway hub list
	private final List<GatewayHub> allDataGatewayHubs = new ArrayList<GatewayHub>();
	private final List<AtomicLong> dataHubSequenceList = new ArrayList<AtomicLong>();

	// channel gateway hub list
	private final List<GatewayHub> allChannelGatewayHubs = new ArrayList<GatewayHub>();
	private final List<AtomicLong> channelHubSequenceList = new ArrayList<AtomicLong>();

	// write-behind gateway hub list
	private final List<GatewayHub> allWriteBehindGatewayHubs = new ArrayList<GatewayHub>();
	private final List<AtomicLong> writeBehindHubSequenceList = new ArrayList<AtomicLong>();

	// lock used to synchronize start/stop hubs.
	private final Object lock = new Object();

	// A list of a complete region full paths supported by this
	// MultiGatewayHubRouterManager.
	private String regionPaths[];

	private List<String> remoteEnabledRegions;

	private List<String> writeBehindEnabledRegions;

	// The cache listener that routes the data to the multi-gateway hubs.
	private MultiGatewayHubRouterCacheListener cacheListener = new MultiGatewayHubRouterCacheListener();

	// If false, the MultiGatewayHubRouterManager is disabled.
	private boolean routerEnabled = false;

	// If true, the hubs running. The hubs are stopped if routerEnabled is false
	private volatile boolean isRouterRunning = false;

	// (from.to, gateway hub list)
	private Map<String, GatewayHub> channelMap = new HashMap<String, GatewayHub>(10);

	private Region channelRegion;

	private String sourceId; // this location id, i.e., UK

	// Set hub.debug=true in etc/hub.properties to print debug information.
	private static boolean debug = false;

	private Timer timer = new Timer("MultiGatewayHubRouterManager Timer", true);

	private final MultiGatewayHubRouterStatistics statistics;

	// memberId->threadId->sequenceIdGenerator
	private final ConcurrentMap<DistributedMember, MemberSequenceIdHandler> sequenceIdHandlers = new ConcurrentHashMap<DistributedMember, MemberSequenceIdHandler>();

	/** Controls whether the router is running */
	private CountDownLatch routerRunningLatch = new CountDownLatch(1);

	/**
	 * The time limit in milliseconds the router hasn't processed an event from
	 * a departed member after which the MemberSequenceIdHandler Cleanup Task
	 * will clean up the resources for that member. This accounts for a no-ack
	 * member whose events can continue to be received and processed after it
	 * has departed. The default = 10800000 (3 hours).
	 */
	private static final long LAST_UPDATE_TIME_LIMIT_MS = Long.parseLong(System.getProperty(
			"gemfire.multiHubRouter.updateTimeLimit", "10800000"));

	/**
	 * The delay in milliseconds to wait before starting the
	 * MemberSequenceIdHandler Cleanup Task. The default = 3600000 (1 hour).
	 */
	private static final long MEMBER_SEQUENCE_ID_HANDLER_CLEANUP_DELAY_MS = Long.parseLong(System.getProperty(
			"gemfire.multiHubRouter.cleanupTaskDelay", "3600000"));

	/**
	 * The period in milliseconds between invocations of the
	 * MemberSequenceIdHandler Cleanup Task. The default = 3600000 (1 hour).
	 */
	private static final long MEMBER_SEQUENCE_ID_HANDLER_CLEANUP_PERIOD_MS = Long.parseLong(System.getProperty(
			"gemfire.multiHubRouter.cleanupTaskPeriod", "3600000"));

	private Region memberStatusRegion;

	/**
	 * Constructs a new MultiGatewayHubRouterManager object.
	 */
	private MultiGatewayHubRouterManager()
	{
		cache = CacheFactory.getAnyInstance();
		Properties prop = getHubProperties(cache);
		if (prop == null) {
			routerEnabled = false;
		} else {
			init(prop);
		}
		this.statistics = new MultiGatewayHubRouterStatistics(cache.getDistributedSystem(), getSourceId());
		addMembershipListener();
		addMemberSequenceIdHandlerCleanupTask();
	}

	private static Properties getHubProperties(Cache cache)
	{
		String propertiesFilePath = System.getProperty(PROPERTY_HUB_PROPERTIES_FILE, "etc/hub.properties");
		cache.getLogger().info("Initializing MultiGatewayHubRouterManager with " + propertiesFilePath);
		Properties prop = new Properties();
		try {
			File file = new File(propertiesFilePath);
			if (file.exists()) {
				prop.load(new FileInputStream(file));
			} else {
				cache.getLogger().warning(
						"MultiGatewayHubRouterManager added but the hub properties file is undefined. "
								+ "MultiGatewayHubRouterManager is NOT activated. " + "Use the system property "
								+ PROPERTY_HUB_PROPERTIES_FILE + " to specify the file path. "
								+ "The defulat file path is etc/hub.properties. ");
			}
		} catch (Exception ex) {
			cache.getLogger().error(
					"Unable to parse the gateway hub list in MultiGatewayHubRouterManager: " + ex.getMessage(), ex);
		}

		return prop;
	}

	private void init(Properties prop)
	{
		if (prop == null) {
			return;
		}

		try {
			// hub.debug
			String debugStr = prop.getProperty("hub.debug");
			debug = debugStr != null && debugStr.equalsIgnoreCase("true");

			// hub.channel
			String channelPath = prop.getProperty("hub.channel", DEFAULT_CHANNEL_PATH);
			channelRegion = RegionUtil.getRegion(channelPath, DataPolicy.REPLICATE);
			DEBUGLN("Created channelRegion: " + channelRegion);
			channelRegion.getAttributesMutator().addCacheListener(new ChannelCacheListener());

			// Create a region that is local and empty to communicate remote
			// member status changes
			// for the MemberSequenceIdHandlerCleanupTask. The RegionUtil class
			// is not being used
			// because its doesn't have an API to set SubscriptionAttributes.
			memberStatusRegion = new RegionFactory().setScope(Scope.DISTRIBUTED_NO_ACK).setDataPolicy(DataPolicy.EMPTY)
					.setSubscriptionAttributes(new SubscriptionAttributes(InterestPolicy.ALL)).addCacheListener(
							new MemberStatusCacheListener()).addCacheListener(cacheListener).create("__member_status");

			// hub.includes
			String includesPaths[];
			List<String> includesList = new ArrayList<String>();
			String includes = prop.getProperty("hub.includes");
			if (includes != null) {
				includesPaths = includes.split(",");
				for (int i = 0; i < includesPaths.length; i++) {
					includesList.add(includesPaths[i].trim());
				}
			}

			boolean all = includesList.contains("*");

			if (all) {
				includesList = RegionUtil.getAllRegionPathList(cache);
			}

			// hub.excludes - overrides hub.includes
			String excludes = prop.getProperty("hub.excludes");
			String excludesPaths[];
			if (excludes == null) {
				excludesPaths = new String[0];
			} else {
				excludesPaths = excludes.split(",");
				for (int i = 0; i < excludesPaths.length; i++) {
					excludesPaths[i] = excludesPaths[i].trim();
				}
			}

			for (int i = 0; i < excludesPaths.length; i++) {
				includesList.remove(excludesPaths[i]);
			}

			regionPaths = (String[]) includesList.toArray(new String[0]);

			StringBuffer buffer = new StringBuffer(1024);
			for (int i = 0; i < regionPaths.length; i++) {
				if (i == regionPaths.length - 1) {
					buffer.append(regionPaths[i]);
				} else {
					buffer.append(regionPaths[i] + ",");
				}
			}

			// remote.enabled
			String remoteEnabledPaths[];
			remoteEnabledRegions = new ArrayList<String>();
			String remoteEnabled = prop.getProperty("remote.enabled");
			if (remoteEnabled != null) {
				remoteEnabledPaths = remoteEnabled.split(",");
				for (int i = 0; i < remoteEnabledPaths.length; i++) {
					remoteEnabledRegions.add(remoteEnabledPaths[i].trim());
				}
			}

			boolean allRemoteEnabled = remoteEnabledRegions.contains("*");

			if (allRemoteEnabled) {
				remoteEnabledRegions = RegionUtil.getAllRegionPathList(cache);
			}

			// write-behind.enabled
			String writeBehindEnabledPaths[];
			writeBehindEnabledRegions = new ArrayList<String>();
			String writeBehindEnabled = prop.getProperty("writebehind.enabled");
			if (writeBehindEnabled != null) {
				writeBehindEnabledPaths = writeBehindEnabled.split(",");
				for (int i = 0; i < writeBehindEnabledPaths.length; i++) {
					writeBehindEnabledRegions.add(writeBehindEnabledPaths[i].trim());
				}
			}

			boolean allWriteBehindEnabled = writeBehindEnabledRegions.contains("*");

			if (allWriteBehindEnabled) {
				writeBehindEnabledRegions = RegionUtil.getAllRegionPathList(cache);
			}

			cache.getLogger().info("MultiGatewayHubRouterManager initialized with regions: " + buffer.toString());

			cache.getLogger().info(
					"MultiGatewayHubRouterManager initialized with remote-enabled regions: " + remoteEnabledRegions);

			cache.getLogger().info(
					"MultiGatewayHubRouterManager initialized with write-behind-enabled regions: "
							+ writeBehindEnabledRegions);

			/*
			 * Let channel hubs start outside of the manager initChannelHubs();
			 * 
			 * boolean channelDisabled = Boolean.getBoolean("channelDisabled");
			 * 
			 * 
			 * 
			 * DEBUGLN("MultiGatewayHubRouterManager.<init> starting channel hubs: "
			 * + !channelDisabled); if (!channelDisabled) {
			 * startAllChannelHubs(); } else { removeChannelHubs(); }
			 */
			routerEnabled = true;

		} catch (Exception ex) {
			cache.getLogger().error(
					"Unable to parse the gateway hub list in MultiGatewayHubRouterManager: " + ex.getMessage(), ex);
		}

		if (debug) {
			startTimer();
		}
	}

	void startTimer()
	{
		timer.schedule(new TimerTask()
		{
			public void run()
			{
				DEBUGLN("Channel Hubs: running = " + isChannelHubsRunning() + ", connected = "
						+ isChannelHubsConnected());
				DEBUGLN("Channel Hubs queue size: " + getTotalChannelQueueSize());
			}
		}, 10000, 5000);
	}

	private void addMembershipListener()
	{
		DM dm = ((InternalDistributedSystem) this.cache.getDistributedSystem()).getDistributionManager();
		dm.addMembershipListener(new SequenceIdGeneratorMembershipListener());
	}

	private void addMemberSequenceIdHandlerCleanupTask()
	{
		if (debug) {
			StringBuilder builder = new StringBuilder();
			builder.append("Scheduling MemberSequenceIdHandler Cleanup Task with delay=").append(
					MEMBER_SEQUENCE_ID_HANDLER_CLEANUP_DELAY_MS).append(" ms, period=").append(
					MEMBER_SEQUENCE_ID_HANDLER_CLEANUP_PERIOD_MS).append(" ms, limit=").append(
					LAST_UPDATE_TIME_LIMIT_MS).append(" ms");
			DEBUGLN(builder.toString());
		}
		this.timer.schedule(new MemberSequenceIdHandlerCleanupTask(), MEMBER_SEQUENCE_ID_HANDLER_CLEANUP_DELAY_MS,
				MEMBER_SEQUENCE_ID_HANDLER_CLEANUP_PERIOD_MS);
	}

	/**
	 * Initializes the MultiGatewayHubRouterManager. initialize() or
	 * getMultiGatewayHubRouterManager() must be invoked after the cache has
	 * been fully initialized.
	 */
	public synchronized static MultiGatewayHubRouterManager initialize()
	{
		if (hubManager == null) {
			hubManager = new MultiGatewayHubRouterManager();
		}
		return hubManager;
	}

	/**
	 * Returns the MultiGatewayHubRouterManager. It initializes the manager if
	 * it hasn't been so. initialize() or getMultiGatewayHubRouterManager() must
	 * be invoked after the cache has been fully initialized.
	 */
	public static MultiGatewayHubRouterManager getMultiGatewayHubRouterManager()
	{
		if (hubManager == null) {
			initialize();
		}
		return hubManager;
	}

	/**
	 * Sets the region paths. This call overwrites the hub list defined in the
	 * etc/hub.properties file.
	 * 
	 * @param regionPaths
	 *            The region paths.
	 */
	public void setRegionPaths(String regionPaths[])
	{
		this.regionPaths = regionPaths;
	}

	/**
	 * Stops all hubs including data and gii hubs.
	 */
	private void stopAllHubs()
	{
		if (routerEnabled == false) {
			cache.getLogger().warning(
					"Trying to stop hubs in MultiGatewayHubRouterManager that is deactivated due to undefined hub list. "
							+ "MultiGatewayHubRouterManager.stopHubs() call is ignored.");
			return;
		}

		// Synchronize it so that start/stop is for all.
		synchronized (lock) {
			cache.getLogger().config("Stopping hubs");

			// remove cache listener from all regions first
			String paths[] = regionPaths;
			if (paths != null) {
				for (int i = 0; i < paths.length; i++) {
					Region region = cache.getRegion(paths[i]);
					if (region != null) {
						region.getAttributesMutator().removeCacheListener(cacheListener);
					}
				}
			}

			// stop all hubs
			for (GatewayHub h : (List<GatewayHub>) cache.getGatewayHubs()) {
				h.stop();
			}
			isRouterRunning = false;
			this.routerRunningLatch = new CountDownLatch(1);
			cache.getLogger().config("Hubs stopped");
		}
	}

	/**
	 * Starts gateway hubs.
	 */
	public void startHubs()
	{
		startDataHubs();
	}

	/**
	 * Stops gateway hubs.
	 */
	public void stopHubs()
	{
		stopDataHubs();
	}

	/**
	 * Returns true if all of the gateway hubs is running.
	 */
	public boolean isRunning()
	{
		return isDataHubsRunning();
	}

	/**
	 * Returns the total size of all of the gateway queues.
	 */
	public int getTotalQueueSize()
	{
		int queueSize = 0;
		for (Iterator<GatewayHub> iterator = allDataGatewayHubs.iterator(); iterator.hasNext();) {
			GatewayHub hub = iterator.next();
			List<Gateway> gatewayList = hub.getGateways();
			for (Iterator<Gateway> iterator2 = gatewayList.iterator(); iterator2.hasNext();) {
				Gateway gateway = iterator2.next();
				queueSize += gateway.getQueueSize();
			}
		}
		return queueSize;
	}

	/**
	 * Returns true if all of the gateway queues are empty.
	 */
	public boolean isQueueEmpty()
	{
		return getTotalQueueSize() == 0;
	}

	static void DEBUGLN()
	{
		if (debug) {
			Logger.info("");
		}
	}

	static void DEBUGLN(String message)
	{
		if (debug) {
			Logger.info(message);
		}
	}

	static void DEBUG(String message)
	{
		if (debug) {
			System.out.print(message);
		}
	}

	/**
	 * Starts the gateway hubs
	 */
	private void startDataHubs()
	{
		if (routerEnabled == false) {
			cache.getLogger().warning(
					"Trying to start data hubs in MultiGatewayHubRouterManager that is deactivated due to undefined hub list. "
							+ "MultiGatewayHubRouterManager.startHubs() call is ignored.");
			return;
		}

		// Synchronize it so that start/stop is for all hubs.
		synchronized (lock) {
			cache.getLogger().config("Starting data hubs");

			// Add cache listener to all regions first
			String paths[] = regionPaths;
			if (paths != null) {
				for (int i = 0; i < paths.length; i++) {
					Region region = cache.getRegion(paths[i]);
					if (region != null) {
						region.getAttributesMutator().removeCacheListener(cacheListener);
						region.getAttributesMutator().addCacheListener(cacheListener);
						// cache.getLogger().config("MultiGW Listener is Added...");
					}
				}
			}

			// start all data hubs (remote and write-behind)
			for (GatewayHub h : (List<GatewayHub>) cache.getGatewayHubs()) {
				try {
					if (h.getId().startsWith("GII") == false) {
						h.start(true);
						DEBUGLN("Started data hub: " + h);
					}
				} catch (IOException e) {
					throw new RuntimeException("Failed to start gateway hubs. Gateway id is " + h.getId()
							+ " Port is :" + h.getPort(), e);
				}
			}
			initDataHubs();
			isRouterRunning = true;
			this.routerRunningLatch.countDown();
			cache.getLogger().config("Started data hubs");
		}
	}

	private void stopDataHubs()
	{
		if (routerEnabled == false) {
			cache.getLogger().warning(
					"Trying to stop data hubs in MultiGatewayHubRouterManager that is deactivated due to undefined hub list. "
							+ "MultiGatewayHubRouterManager.stopHubs() call is ignored.");
			return;
		}

		// Synchronize it so that start/stop is for all.
		synchronized (lock) {
			cache.getLogger().config("Stopping data hubs");

			// remove cache listener from all regions first
			String paths[] = regionPaths;
			if (paths != null) {
				for (int i = 0; i < paths.length; i++) {
					Region region = cache.getRegion(paths[i]);
					if (region != null) {
						region.getAttributesMutator().removeCacheListener(cacheListener);
					}
				}
			}

			// stop all data hubs
			for (GatewayHub h : (List<GatewayHub>) cache.getGatewayHubs()) {
				if (h.getId().startsWith("GII") == false && h.getId().startsWith("CHANNEL") == false) {
					h.stop();
					DEBUGLN("Stopped data hub: " + h);
				}
			}
			isRouterRunning = false;
			this.routerRunningLatch = new CountDownLatch(1);
			cache.getLogger().config("Data hubs stopped");
		}
	}

	private void pauseDataHubs(String targetId)
	{
		if (routerEnabled == false) {
			cache.getLogger().warning(
					"Trying to pause data hubs in MultiGatewayHubRouterManager that is deactivated due to undefined hub list. "
							+ "MultiGatewayHubRouterManager.pauseDataHubs() call is ignored for the gateway target "
							+ targetId + ".");
			return;
		}

		// Synchronize it so that start/stop is for all.
		synchronized (lock) {
			cache.getLogger().config("Pausing data hubs for the gateway target " + targetId);

			// pause all data hubs
			for (GatewayHub h : (List<GatewayHub>) cache.getGatewayHubs()) {
				if (h.getId().startsWith("GII") == false && h.getId().startsWith("CHANNEL") == false) {
					List<Gateway> gatewayList = h.getGateways();
					for (Gateway gateway : gatewayList) {
						if (targetId.equals(gateway.getId())) {
							gateway.pause();
						}
					}
				}
			}
			cache.getLogger().config("Data hubs paused for the gateway target " + targetId);
		}
	}

	private void resumeDataHubs(String targetId)
	{
		if (routerEnabled == false) {
			cache.getLogger().warning(
					"Trying to resume data hubs in MultiGatewayHubRouterManager that is deactivated due to undefined hub list. "
							+ "MultiGatewayHubRouterManager.resumeHubs() call is ignored for the gateway target "
							+ targetId + ".");
			return;
		}

		// Synchronize it so that start/stop is for all hubs.
		synchronized (lock) {
			cache.getLogger().config("Resuming data hubs for the gateway target " + targetId);

			// resume all data hubs
			for (GatewayHub h : (List<GatewayHub>) cache.getGatewayHubs()) {
				if (h.getId().startsWith("GII") == false && h.getId().startsWith("CHANNEL") == false) {
					List<Gateway> gatewayList = h.getGateways();
					for (Gateway gateway : gatewayList) {
						if (targetId.equals(gateway.getId())) {
							gateway.resume();
						}
					}
				}
			}
			cache.getLogger().config("Data hubs resumed for the gateway target " + targetId);
		}
	}

	private boolean isDataHubsRunning()
	{
		if (routerEnabled == false) {
			return false;
		}

		if (allDataGatewayHubs.size() == 0) {
			return false;
		}

		for (GatewayHub h : allDataGatewayHubs) {
			if (h.isRunning() == false) {
				return false;
			}
		}
		return true;
	}

	private int getTotalChannelQueueSize()
	{
		int queueSize = 0;
		for (Iterator<GatewayHub> iterator = allChannelGatewayHubs.iterator(); iterator.hasNext();) {
			GatewayHub hub = iterator.next();
			List<Gateway> gatewayList = hub.getGateways();
			for (Iterator<Gateway> iterator2 = gatewayList.iterator(); iterator2.hasNext();) {
				Gateway gateway = iterator2.next();
				queueSize += gateway.getQueueSize();
			}
		}
		return queueSize;
	}

	private boolean isChannelHubsRunning()
	{
		if (routerEnabled == false) {
			return false;
		}

		if (allChannelGatewayHubs.size() == 0) {
			return false;
		}

		for (GatewayHub h : allChannelGatewayHubs) {
			if (h.isRunning() == false) {
				return false;
			}
		}
		return true;
	}

	private boolean isChannelHubsConnected()
	{
		int totalConnected = 0;
		int numGateways = 0;
		for (GatewayHub h : allChannelGatewayHubs) {
			List<Gateway> gatewayList = h.getGateways();
			numGateways += gatewayList.size();
			for (Iterator iterator = gatewayList.iterator(); iterator.hasNext();) {
				Gateway gateway = (Gateway) iterator.next();
				if (gateway.isConnected()) {
					totalConnected++;
				}
			}
		}

		DEBUGLN("totalConnected = " + totalConnected + " / " + numGateways);
		return totalConnected > 0;
	}

	private void startAllChannelHubs()
	{
		Collection<GatewayHub> col = channelMap.values();
		allChannelGatewayHubs.clear();
		channelHubSequenceList.clear();
		for (Iterator<GatewayHub> iterator = col.iterator(); iterator.hasNext();) {
			GatewayHub hub = iterator.next();
			try {
				hub.start();
				allChannelGatewayHubs.add(hub);
				channelHubSequenceList.add(new AtomicLong(0));
				DEBUGLN("Channel Hub Started: " + hub.getId());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Starts the channel gateway hub for the specified source (from).
	 * 
	 * @param from
	 *            The source of channel.
	 */
	private void startChannelHubs(String from)
	{
		if (routerEnabled == false) {
			cache.getLogger().warning(
					"Trying to start Channel hubs in MultiGatewayHubRouterManager that is deactivated due to undefined hub list. "
							+ "MultiGatewayHubRouterManager.startHubs() call is ignored.");
			return;
		}

		// Synchronize it so that start/stop is for all hubs.
		synchronized (lock) {
			cache.getLogger().config("Starting Channel hub for source " + from);

			// start all data hubs
			GatewayHub h = channelMap.get(from);
			allChannelGatewayHubs.clear();
			channelHubSequenceList.clear();
			try {
				h.start(true);
				if (h.isPrimary()) {
					allChannelGatewayHubs.add(h);
					channelHubSequenceList.add(new AtomicLong(0));
				}
			} catch (IOException e) {
				throw new RuntimeException("Failed to start Channel gateway hub for source " + h.getId(), e);
			}
			cache.getLogger().config("Started Channel hub for source " + from);
		}
	}

	/**
	 * Stops the channel gateway hub for the specified source (from).
	 * 
	 * @param from
	 *            The source of channel.
	 */
	private void stopChannelHubs(String from)
	{
		if (routerEnabled == false) {
			cache.getLogger().warning(
					"Trying to stop Channel hubs in MultiGatewayHubRouterManager that is deactivated due to undefined hub list. "
							+ "MultiGatewayHubRouterManager.stopHubs() call is ignored.");
			return;
		}

		// Synchronize it so that start/stop is for all.
		synchronized (lock) {
			cache.getLogger().config("Stopping Channel hub for source " + from);

			// stop channel hubs
			GatewayHub h = channelMap.get(from);
			h.stop();
			allChannelGatewayHubs.remove(h);
			cache.getLogger().config("Channel hub stopped for source " + from);
		}
	}

	/**
	 * Returns the source ID. The source Id is equivalent to the &lt;source&gt;
	 * part of the gateway hub ID, CHANNEL.&lt;source&gt.&lt;target&gt;, defined
	 * in the cache.xml file.
	 */
	public String getSourceId()
	{
		return sourceId;
	}

	/**
	 * Returns all of the included region paths.
	 */
	public String[] getRegionPaths()
	{
		return regionPaths;
	}

	/**
	 * Returns true if this MultiGatewayHubRouterManager is running.
	 */
	public boolean isRouterRunning()
	{
		return routerEnabled && isRouterRunning;
	}

	/**
	 * Gather all gateway hubs.
	 */
	private void initDataHubs()
	{
		allDataGatewayHubs.clear();
		dataHubSequenceList.clear();
		allWriteBehindGatewayHubs.clear();
		writeBehindHubSequenceList.clear();
		for (Iterator i = this.cache.getGatewayHubs().iterator(); i.hasNext();) {
			GatewayHub gatewayHub = (GatewayHub) i.next();
			String id = gatewayHub.getId();
			Gateway gateway = (Gateway) gatewayHub.getGateways().get(0);
			boolean isRemote = gateway.hasEndpoints();
			if (id.startsWith("GII") == false && id.startsWith("CHANNEL") == false) {
				if (isRemote) {
					this.allDataGatewayHubs.add(gatewayHub);
					dataHubSequenceList.add(new AtomicLong(0));
				} else {
					this.allWriteBehindGatewayHubs.add(gatewayHub);
					writeBehindHubSequenceList.add(new AtomicLong(0));
				}
				cache.getLogger().info(
						"Adding to " + (isRemote ? "remote" : "write-behind") + " data hubs: " + gatewayHub);
			}
		}
	}

	private void initChannelHubs()
	{
		channelMap.clear();
		allChannelGatewayHubs.clear();
		channelHubSequenceList.clear();
		for (Iterator i = this.cache.getGatewayHubs().iterator(); i.hasNext();) {
			GatewayHub gatewayHub = (GatewayHub) i.next();
			String id = gatewayHub.getId();
			if (id.startsWith("CHANNEL")) {
				String split[] = id.split("\\.");
				if (split.length >= 3) {
					sourceId = split[1];
					String to = split[2];

					// From.To Map
					String fromTo = sourceId + "." + to;
					channelMap.put(fromTo, gatewayHub);
				}
			}
		}
	}

	private void removeChannelHubs()
	{
		channelMap.clear();
		allChannelGatewayHubs.clear();
		channelHubSequenceList.clear();
		for (Iterator i = this.cache.getGatewayHubs().iterator(); i.hasNext();) {
			GatewayHub gatewayHub = (GatewayHub) i.next();
			try {
				gatewayHub.stop();
			} catch (Exception ex) {
				// ignore
			}
			List list = gatewayHub.getGatewayIds();
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				String id = (String) iterator.next();
				gatewayHub.removeGateway(id);
			}
		}
	}

	/**
	 * Dispatch the event to data gateway hubs. It selects the hub evenly based
	 * on the key hash code.
	 */
	private void dispatchData(EntryEvent event)
	{
		waitForRouterToStart();
		String regionName = event.getRegion().getFullPath();
		if (remoteEnabledRegions.contains(regionName)) {
			dispatch(event, allDataGatewayHubs, dataHubSequenceList);
		}
		if (writeBehindEnabledRegions.contains(regionName)) {
			dispatch(event, allWriteBehindGatewayHubs, writeBehindHubSequenceList);
		}
	}

	/**
	 * Dispatch the event to channel gateway hub. It selects the hub evenly
	 * based on the key has code.
	 */
	private void dispatchChannel(EntryEvent event)
	{
		waitForRouterToStart();
		dispatch(event, allChannelGatewayHubs, channelHubSequenceList);
	}

	/**
	 * Dispatch the event to non-gii gateway hubs. It selects the hub evenly
	 * based on the key has code.
	 */
	private void dispatch(EntryEvent event, List<GatewayHub> hubs, List<AtomicLong> sequenceList)
	{
		if (hubs.size() == 0) {
			getStatistics().incEventsRejectedNoHubs();
			cache.getLogger().info("*** Warning *** No Hubs found to dispatch the event.." + event.getKey());
			return;
		}

		EntryEventImpl e = (EntryEventImpl) event;
		GatewayHub targetHub = null;
		int targetHubIndex = 0;
		if (hubs.size() > 1) {
			targetHubIndex = Math.abs(event.getKey().hashCode()) % hubs.size();
		}
		targetHub = hubs.get(targetHubIndex);
		synchronized (targetHub) {
			EventID newId = generateEventId(e, hubs.size(), targetHubIndex);
			if (debug) {
				DEBUGLN("Processing key=" + event.getKey() + " hub=" + targetHub.getId() + " old id=" + e.getEventId()
						+ " new id=" + newId);
			}
			e.setEventId(newId);
			if (debug) {
				DEBUGLN("Propagating event key=" + event.getKey() + " event=" + event + " hashCode="
						+ event.getKey().hashCode() + " hub=" + targetHub.getId());
			}
			if (hubs == allChannelGatewayHubs && debug) {
				DEBUGLN("Propagating event for " + event + " to hub " + targetHub.getId());
			}
			GatewayHubImpl impl = (GatewayHubImpl) targetHub;

			// To force conflation, use AFTER_UPDATE for all
			// messages. Note that DESTROY/INVALIDATE may not work
			// if AFTER_UPDATE is forced.
			if (debug) {
				DEBUGLN("dispatch distributing to: " + impl);
			}
			long start = getStatistics().startEventDispatch();
			if (e.getOperation() == Operation.DESTROY) {
				impl.distribute(EnumListenerEvent.AFTER_DESTROY, e);
			} else if (e.getOperation() == Operation.INVALIDATE) {
				impl.distribute(EnumListenerEvent.AFTER_INVALIDATE, e);
			} else {
				impl.distribute(EnumListenerEvent.AFTER_UPDATE, e);
			}
			getStatistics().endEventDispatch(start);
		}
	}

	private void waitForRouterToStart()
	{
		if (this.isRouterRunning) {
			return;
		}

		try {
			cache.getLogger().info("Waiting for router to start");
			this.routerRunningLatch.await();
			cache.getLogger().info("Router started");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private EventID generateEventId(EntryEventImpl event, int numHubs, int targetHubIndex)
	{
		// Get the original event's DistributedMember
		EventID originalEventId = event.getEventId();
		byte[] memberId = originalEventId.getMembershipID();
		DistributedMember member = originalEventId.getDistributedMember();

		// Get the MemberSequenceIdHandler for the member
		MemberSequenceIdHandler handler = this.sequenceIdHandlers.get(member);
		if (handler == null) {
			// Create the MemberSequenceIdHandler for this member
			handler = new MemberSequenceIdHandler(member, numHubs);
			MemberSequenceIdHandler previousHandler = this.sequenceIdHandlers.putIfAbsent(member, handler);
			if (previousHandler == null) {
				getStatistics().incMembers();
			} else {// somebody beat me to it
				handler = previousHandler;
			}
		}

		// Create the new EventID
		EventID generatedEventId = handler.generateEventID(memberId, originalEventId.getThreadID(), targetHubIndex);
		if (debug) {
			DEBUGLN("Original EventID key: " + event.getKey() + "; member: " + member + "; threadId: "
					+ originalEventId.getThreadID() + "; sequenceId: " + originalEventId.getSequenceID());
			DEBUGLN("New EventID key: " + event.getKey() + "; member: " + member + "; threadId: "
					+ generatedEventId.getThreadID() + "; sequenceId: " + generatedEventId.getSequenceID());
		}

		return generatedEventId;
	}

	private boolean isChannelPrimary()
	{
		// DEBUG("allChannelGatewayHubs.size() = " +
		// allChannelGatewayHubs.size());
		boolean retval = false;
		for (Iterator<GatewayHub> iterator = allChannelGatewayHubs.iterator(); iterator.hasNext();) {
			GatewayHub hub = iterator.next();
			DEBUGLN("ChannelCacheListener.isChannelPrimary() " + hub + "isPrimary: " + hub.isPrimary());
			if (hub.isPrimary()) {
				retval = true;
				break;
			}
		}
		// DEBUG("isChannelPrimary() = " + retval);
		return retval;
	}

	protected MultiGatewayHubRouterStatistics getStatistics()
	{
		return this.statistics;
	}

	protected Map<DistributedMember, MemberSequenceIdHandler> getMemberSequenceIdHandlers()
	{
		return this.sequenceIdHandlers;
	}

	protected Cache getCache()
	{
		return this.cache;
	}

	protected Region getMemberStatusRegion()
	{
		return this.memberStatusRegion;
	}

	private class MultiGatewayHubRouterCacheListener extends CacheListenerAdapter
	{
		private void dispatch(EntryEvent event)
		{
			getStatistics().incEventsReceived();
			if (event.getRegion() == channelRegion) {
				getStatistics().incEventsRejectedNotData();
				return;
			}
			MultiGatewayHubRouterManager.getMultiGatewayHubRouterManager().dispatchData(event);
		}

		public void afterCreate(EntryEvent event)
		{
			dispatch(event);
		}

		public void afterDestroy(EntryEvent event)
		{
			dispatch(event);
		}

		public void afterInvalidate(EntryEvent event)
		{
			dispatch(event);
		}

		public void afterUpdate(EntryEvent event)
		{
			dispatch(event);
		}
	}

	private class ChannelCacheListener extends CacheListenerAdapter implements QueueDispatcherListener
	{
		QueueDispatcher dispatcher = new QueueDispatcher();

		ChannelCacheListener()
		{
			dispatcher.setQueueDispatcherListener(this);
			dispatcher.start();
		}

		private void dispatch(EntryEvent event)
		{
			DEBUGLN("ChannelCacheListener.dispatch()");
			if (event.getRegion() != channelRegion || isChannelPrimary() == false) {
				return;
			}
			dispatcher.enqueue(event);
			// objectDispatched(event);
		}

		public void afterCreate(EntryEvent event)
		{
			// DEBUG("afterCreate: " + event);
			dispatch(event);
		}

		public void afterUpdate(EntryEvent event)
		{
			// DEBUG("afterUpdate: " + event);
			dispatch(event);
		}

		public void objectDispatched(Object obj)
		{
		}
	}

	private class MemberStatusCacheListener extends CacheListenerAdapter
	{

		public void afterCreate(EntryEvent event)
		{
			// Get the associated entry from the sequenceIdHanders
			MemberSequenceIdHandler handler = getMemberSequenceIdHandlers().get(
					(InternalDistributedMember) event.getKey());

			// If it exists, set it to be departed.
			// It can't be removed here because in the no-ack case, events for
			// it might still
			// be being processed. If it is removed, it'll get re-added with its
			// sequenceId
			// reset back to 0.
			if (handler != null) {
				handler.setMemberDeparted();
			}
		}
	}

	/**
	 * Class MemberSequenceIdHandler keeps track of the sequence id generators
	 * for the threads of a particular member.
	 */
	private class MemberSequenceIdHandler
	{

		/**
		 * The member
		 */
		private final DistributedMember member;

		/**
		 * The number of gateway hubs
		 */
		private final int numHubs;

		/**
		 * The last time an update from this member was handled
		 */
		private long lastUpdateTime;

		/**
		 * Whether this member has departed
		 */
		private volatile boolean memberDeparted;

		/**
		 * The mapping between threadId->sequence id generator
		 */
		private final Map<Long, SequenceIdGenerator> sequenceIdGenerators;

		protected MemberSequenceIdHandler(DistributedMember member, int numHubs)
		{
			this.member = member;
			this.numHubs = numHubs;
			setLastUpdateTime();
			this.memberDeparted = false;
			this.sequenceIdGenerators = new ConcurrentHashMap<Long, SequenceIdGenerator>();
		}

		private EventID generateEventID(byte[] memberId, long originalThreadId, int targetHubIndex)
		{
			// Get the SequenceIdGenerator for the thread
			SequenceIdGenerator generator = this.sequenceIdGenerators.get(originalThreadId);
			if (generator == null) {
				generator = new SequenceIdGenerator(this.member, originalThreadId, this.numHubs);
				this.sequenceIdGenerators.put(originalThreadId, generator);
				getStatistics().incThreads();
			}

			// Get the new thread id for the target hub index
			long generatedThreadId = generator.getThreadId(targetHubIndex);

			// Generate the sequence number for the target hub index
			long generatedSequenceId = generator.generateSequenceId(targetHubIndex);

			// Update last update time
			setLastUpdateTime();

			// Create and return the new EventID
			return new EventID(memberId, generatedThreadId, generatedSequenceId);
		}

		private DistributedMember getMember()
		{
			return this.member;
		}

		private void setLastUpdateTime()
		{
			this.lastUpdateTime = System.currentTimeMillis();
		}

		private long getLastUpdateTime()
		{
			return this.lastUpdateTime;
		}

		private void setMemberDeparted()
		{
			// Set departed equal true
			this.memberDeparted = true;

			// Decrement stats
			getStatistics().decMembers();
			getStatistics().decThreads(this.sequenceIdGenerators.size());
		}

		private boolean getMemberDeparted()
		{
			return this.memberDeparted;
		}

		public String toString()
		{
			return new StringBuilder().append("MemberSequenceIdHandler[").append("member=").append(this.member).append(
					"; numHubs=").append(this.numHubs).append("; lastUpdateTime=").append(this.lastUpdateTime).append(
					"; memberDeparted=").append(this.memberDeparted).append("; sequenceIdGenerators=").append(
					this.sequenceIdGenerators).append("]").toString();
		}
	}

	/**
	 * Class SequenceIdGenerator is the sequence id generator for 1
	 * memberId->threadId combination.
	 */
	private class SequenceIdGenerator
	{

		/**
		 * The member
		 */
		private final DistributedMember member;

		/**
		 * The original thread id for the memberId->threadId
		 */
		private final long originalThreadId;

		/**
		 * The list of threadIds based on the original threadId. This list
		 * contains one threadId per gateway hub.
		 */
		private final List<Long> threadIds;

		/**
		 * The map of sequenceIds. This map contains one entry per threadId.
		 */
		private final Map<Long, Long> sequenceIds;

		protected SequenceIdGenerator(DistributedMember member, long originalThreadId, int numHubs)
		{
			this.member = member;
			this.originalThreadId = originalThreadId;

			// Determine the base threadId
			String memberPlusOriginalThreadId = originalThreadId + "-" + member.toString();
			int baseThreadId = Math.abs(memberPlusOriginalThreadId.hashCode());

			// Initialize the list of threadIds
			this.threadIds = new ArrayList<Long>();
			for (int i = 0; i < numHubs; i++) {
				this.threadIds.add((long) baseThreadId + i);
			}

			// Initialize the map of threadIds->sequenceIds
			this.sequenceIds = new HashMap<Long, Long>();
			for (long threadId : this.threadIds) {
				this.sequenceIds.put(threadId, 0l);
			}
		}

		protected long getThreadId(int index)
		{
			return this.threadIds.get(index);
		}

		protected long generateSequenceId(int index)
		{
			// Get the thread id for the index
			long threadId = getThreadId(index);

			// Get the sequence id for the threadId
			Long sequenceId = this.sequenceIds.get(threadId);

			this.sequenceIds.put(threadId, ++sequenceId);

			return sequenceId;
		}

		public String toString()
		{
			return new StringBuilder().append("SequenceIdGenerator[").append("member=").append(this.member).append(
					"; originalThreadId=").append(this.originalThreadId).append("; threadIds=").append(this.threadIds)
					.append("]").toString();
		}
	}

	private class SequenceIdGeneratorMembershipListener implements MembershipListener
	{

		public void memberJoined(InternalDistributedMember id)
		{
		}

		public void memberDeparted(InternalDistributedMember id, boolean crashed)
		{
			// Add an entry to the member status region. This entry will get
			// processed both locally
			// and across the WAN.
			getMemberStatusRegion().put(id, true);
		}

		public void memberSuspect(InternalDistributedMember id, InternalDistributedMember whoSuspected)
		{
		}

		/**
		 * GemFire 7.0 addtion
		 */
		public void quorumLost(Set<InternalDistributedMember> arg0, List<InternalDistributedMember> arg1)
		{
			
		}
	}

	private class MemberSequenceIdHandlerCleanupTask extends TimerTask
	{

		public void run()
		{
			if (debug) {
				DEBUGLN("Checking the handlers for " + getMemberSequenceIdHandlers().size() + " members");
			}
			for (Iterator<MemberSequenceIdHandler> i = getMemberSequenceIdHandlers().values().iterator(); i.hasNext();) {
				MemberSequenceIdHandler handler = i.next();
				if (debug) {
					DEBUGLN("Member=" + handler.getMember() + " departed=" + handler.getMemberDeparted()
							+ " lastUpdateTime=" + handler.getLastUpdateTime());
				}
				if (handler.getMemberDeparted()) {
					if (System.currentTimeMillis() > handler.getLastUpdateTime() + LAST_UPDATE_TIME_LIMIT_MS) {
						i.remove();
						if (debug) {
							DEBUGLN("Cleaned up handler for member " + handler.getMember());
						}
					} else {
						if (debug) {
							DEBUGLN("Keeping handler for member " + handler.getMember()
									+ ": Departed but not over the time limit");
						}
					}
				} else {
					if (debug) {
						DEBUGLN("Keeping handler for member " + handler.getMember() + ": Not departed");
					}
				}
			}
		}
	}
}
