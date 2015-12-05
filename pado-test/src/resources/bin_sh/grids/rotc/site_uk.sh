#!/bin/bash

# site_<$SITE>.sh defines site-wid parameters to configure
# a single site. Parameters defined in this file become the
# default parameters for servers running in the site.

# SITE: Site ID. It must be unique and must not include spaces.
# If SITE_ID is not defined then the site ID in the
# $SITES list defined in grid_env.sh is assigned. It must 
# not include spaces.
SITE_ID=$SITE

# SITE_NAME: Site name is a legible non-unique name for
# display purposes. Any characters including spaces are allowed.
SITE_NAME=$SITE

# SYSTEM_ID is a number that uniquely identifies this cluster for
# site-to-site (WAN) replication. GemFire specific.
SYSTEM_ID=2

GRID_ID=${GRID}-${SITE_ID}
GRID_NAME=${GRID}-${SITE_ID}

# List of comma separated remote locators for WAN replication.
# Format: "<host1>[<port1>],<host2>[<port2>]"
# Note the square brackets and no white spaces
# Unset or undefine it to disable WAN replication.
REMOTE_LOCATORS="localhost[20000],localhost[20300]"

# Remote system IDs to be included in server.xml for WAN replcation
# 1->us, 2->uk, 3->jp
# us
REMOTE_SYSTEM_ID_1=1
# jp
REMOTE_SYSTEM_ID_2=3

# Cache server port prefix. Limit the number of digits to 2 or 3.
# Each cache is assigned a trailing number from 1 to 99.
# For example, if it is set to 200, './start_server -num 1' assigns 20001
# and this script sets locator=20000, agent rmi=20050, agent http=20051
CACHE_SERVER_PORT_PREFIX=201

# JVM debug mode. Set DEBUG_ENABLED to true to enable the debug mode.
DEBUG_ENABLED=true

# Number of cache servers. This number is equally divided
# amongst the list of cache sever hosts defined by $SERVER_HOSTS.
# $SERVER_HOSTS is determined by the servers listed in bind_$SITE.sh.
NUM_SERVERS=2

# Gateway servers
# Starts one of the cache servers on each of the servers listed
# as a gateway swerver. Comment it out to disable gateways.
# The gateway server hosts must match the first hosts listed
# in SERVER_HOSTS and must run on different hosts.
GATEWAY_SERVERS=""

# min/max locator heap size
LOCATOR_HEAP_MAX=256m

# min/max cache server heap size.
HEAP_MAX=256m

# JMX Manager - GemFire 7.x
JMX_MANAGER_ENABLED=true

# Pado app directory that contains client app config files
PADO_APP_CONFIG_DIR=$ETC_GRID_DIR/app
# Pado properties file path
PADO_PROPERTY_FILE=$ETC_GRID_DIR/pado.properties
# Gemfire properties file path.
GEMFIRE_PROPERTY_FILE=$ETC_GRID_DIR/server.properties
# Server configuration XML file path.
SERVER_XML_FILE=$ETC_GRID_DIR/server.xml
# Server gateway configuraiton XML file path.
SERVER_GATEWAY_XML_FILE=$ETC_GRID_DIR/server_gateway.xml
