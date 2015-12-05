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

import com.gemstone.gemfire.StatisticDescriptor;
import com.gemstone.gemfire.Statistics;
import com.gemstone.gemfire.StatisticsFactory;
import com.gemstone.gemfire.StatisticsType;
import com.gemstone.gemfire.StatisticsTypeFactory;
import com.gemstone.gemfire.distributed.internal.DistributionStats;
import com.gemstone.gemfire.internal.StatisticsTypeFactoryImpl;

public class MultiGatewayHubRouterStatistics {

  public static final String typeName = "MultiGatewayHubRouterStatistics";

  private static final StatisticsType type;

  private static final String EVENTS_RECEIVED = "eventsReceived";
  private static final String EVENTS_DISPATCHED = "eventsDispatched";
  private static final String EVENT_DISPATCH_TIME = "eventDispatchTime";
  private static final String EVENT_DISPATCHES_IN_PROGRESS = "eventDispatchesInProgress";
  private static final String EVENTS_REJECTED_NO_HUBS = "eventsRejectedNoHubs";
  private static final String EVENTS_REJECTED_FROM_GATEWAY = "eventsRejectedFromGateway";
  private static final String EVENTS_REJECTED_NOT_DATA = "eventsRejectedNotData";
  private static final String NUMBER_OF_MEMBERS = "members";
  private static final String NUMBER_OF_THREADS = "threads";
  
  private static final int eventsReceivedId;
  private static final int eventsDispatchedId;
  private static final int eventDispatchTimeId;
  private static final int eventDispatchesInProgressId;
  private static final int eventsRejectedNoHubsId;
  private static final int eventsRejectedFromGatewayId;
  private static final int eventsRejectedNotDataId;
  private static final int membersId;
  private static final int threadsId;

  static {
    // Initialize type
    StatisticsTypeFactory f = StatisticsTypeFactoryImpl.singleton();
    type = f.createType(typeName, typeName,
      new StatisticDescriptor[] {
        f.createIntCounter(EVENTS_RECEIVED, "The number of events received by the router", "operations"),
        f.createIntCounter(EVENTS_DISPATCHED, "The number of events dispatched by the router", "operations"),
        f.createLongCounter(EVENT_DISPATCH_TIME, "Total time spent dispatching events to the gateways", "nanoseconds"),
        f.createIntGauge(EVENT_DISPATCHES_IN_PROGRESS, "The number of event dispatches currently in progress", "operations"),
        f.createIntCounter(EVENTS_REJECTED_NO_HUBS, "The number of events rejected by the router because there are no hubs", "operations"),
        f.createIntCounter(EVENTS_REJECTED_FROM_GATEWAY, "The number of events rejected by the router because they were received from a gateway", "operations"),
        f.createIntCounter(EVENTS_REJECTED_NOT_DATA, "The number of events rejected by the router because they are GII or channel events", "operations"),
        f.createIntGauge(NUMBER_OF_MEMBERS, "The number of members connected to this router", "members"),
        f.createIntGauge(NUMBER_OF_THREADS, "The number of threads connected to this router", "threads"),
      }
    );

    // Initialize id fields
    eventsReceivedId = type.nameToId(EVENTS_RECEIVED);
    eventsDispatchedId = type.nameToId(EVENTS_DISPATCHED);
    eventDispatchTimeId = type.nameToId(EVENT_DISPATCH_TIME);
    eventDispatchesInProgressId = type.nameToId(EVENT_DISPATCHES_IN_PROGRESS);
    eventsRejectedNoHubsId = type.nameToId(EVENTS_REJECTED_NO_HUBS);
    eventsRejectedFromGatewayId = type.nameToId(EVENTS_REJECTED_FROM_GATEWAY);
    eventsRejectedNotDataId = type.nameToId(EVENTS_REJECTED_NOT_DATA);
    membersId = type.nameToId(NUMBER_OF_MEMBERS);
    threadsId = type.nameToId(NUMBER_OF_THREADS);
  }

  private final Statistics stats;

  public MultiGatewayHubRouterStatistics(StatisticsFactory f, String sourceId) {
    this.stats = f.createAtomicStatistics(type, "multiGatewayHubRouterStatistics-" + sourceId);
  }

  public void close() {
    this.stats.close();
  }

  protected long getTime() {
    return DistributionStats.getStatTime();
  }

  public int getEventsReceived() {
    return this.stats.getInt(eventsReceivedId);
  }

  public int getEventsDispatched() {
    return this.stats.getInt(eventsDispatchedId);
  }

  public int getEventsRejectedNoHubs() {
    return this.stats.getInt(eventsRejectedNoHubsId);
  }

  public int getEventsRejectedFromGateway() {
    return this.stats.getInt(eventsRejectedFromGatewayId);
  }

  public int getEventsRejectedNotData() {
    return this.stats.getInt(eventsRejectedNotDataId);
  }

  public int getMembers() {
    return this.stats.getInt(membersId);
  }

  public int getThreads() {
    return this.stats.getInt(threadsId);
  }

  public void incEventsReceived() {
    this.stats.incInt(eventsReceivedId, 1);
  }

  public void incEventsRejectedNoHubs() {
    this.stats.incInt(eventsRejectedNoHubsId, 1);
  }

  public void incEventsRejectedFromGateway() {
    this.stats.incInt(eventsRejectedFromGatewayId, 1);
  }

  public void incEventsRejectedNotData() {
    this.stats.incInt(eventsRejectedNotDataId, 1);
  }

  public void incMembers() {
    this.stats.incInt(membersId, 1);
  }

  public void incThreads() {
    this.stats.incInt(threadsId, 1);
  }

  public void decMembers() {
    this.stats.incInt(membersId, -1);
  }

  public void decThreads(int numThreads) {
    this.stats.incInt(threadsId, -numThreads);
  }

  public long startEventDispatch() {
    stats.incInt(eventDispatchesInProgressId, 1);
    return getTime();
  }

  public void endEventDispatch(long start) {
    long end = getTime();

    // Increment number of events dispatched
    this.stats.incInt(eventsDispatchedId, 1);

    // Increment event dispatch time
    this.stats.incLong(eventDispatchTimeId, end-start);

    // Decrement the number of event dispatches in progress
    this.stats.incInt(eventDispatchesInProgressId, -1);
  }
}
