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
SYSTEM_ID=1

GRID_ID=${GRID}-${SITE_ID}
GRID_NAME=${GRID}-${SITE_ID}

# JVM debug mode. Set DEBUG_ENABLED to true to enable the debug mode.
DEBUG_ENABLED=true

# min/max locator heap size
LOCATOR_HEAP_MAX=256m

# min/max cache server heap size.
HEAP_MAX=512m

# JMX Manager - GemFire 7.x+
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
