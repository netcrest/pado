#!/bin/bash

# ========================================================================
# Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ========================================================================

. ./utilenv.sh > /dev/null 2>&1

#
# argenv.sh parses input arguments of individual scripts
# and assign appropriate parameters.
#

#
# Determine arguments
#
SERVER_NUM=1
SERVER_NUMBER_SPECIFIED=
REBALANCE=
GATEWAY=
GATEWAY_XML_FILE=
SERVER_XML_FILE=
GRID_SPECIFIED=
SITE_SPECIFIED=
SERVER=
SERVER_SPECIFIED=
START=false
COMMIT=false
HELP=
TEST=false
PARALLEL=true
CONF=
LIB=
DATA=
NO_DATA=false
DB=
ALL=
LOCATORS=
CLIENTS=
LOCATORS_OPT=
PERSIST=
PERSIST_OPT=
PID=
PIDONLY=
BEGIN_NUM=1
END_NUM=
KILL=
DEBUG=
DIR=
TO_DIR=
JAR_OPT=
SCHED=
NOW=
IMPORT=
CLEAN=
QUIET=false
REFID=
PARENT_GRID_ID=
PARENT_LOCATORS=
PREFIX=
HOST_NAMES=
SERVER_NAMES=
RECURSIVE=
PREV=
#for (( i=1; i<=$#; i++ ))
for i in $*
do
   if [ "$PREV" == "-num" ]; then
      SERVER_NUM=$i
      SERVER_NUM_SPECIFIED=true
   elif [ "$PREV" == "-grid" ]; then
      GRID=$i
      GRID_SPECIFIED=true
   elif [ "$PREV" == "-site" ]; then
      SITE=$i
      SITE_SPECIFIED=true
   elif [ "$PREV" == "-server" ]; then
      SERVER=$i
      SERVER_SPECIFIED=true
   elif [ "$i" == "-start" ]; then
      START=true
   elif [ "$i" == "-commit" ]; then
      COMMIT=true
   elif [ "$PREV" == "-begin" ]; then
      BEGIN_NUM=$i
   elif [ "$PREV" == "-end" ]; then
      END_NUM=$i
   elif [ "$PREV" == "-dir" ]; then
      DIR=$i
   elif [ "$PREV" == "-todir" ]; then
      TO_DIR=$i
   elif [ "$PREV" == "-jar" ]; then
      JAR_OPT=$i
   elif [ "$PREV" == "-prefix" ]; then
      PREFIX=$i
   elif [ "$PREV" == "-hosts" ]; then
      HOST_NAMES=$i
   elif [ "$PREV" == "-servers" ]; then
      SERVER_NAMES=$i
   elif [ "$PREV" == "-refid" ]; then
      REFID=$i
   elif [ "$PREV" == "-parent" ]; then
      PARENT_GRID_ID=$i
   elif [ "$PREV" == "-parent-locators" ]; then
      PARENT_LOCATORS=$i
   elif [ "$i" == "-gateway" ]; then
      GATEWAY=gateway
   elif [ "$i" == "-rebalance" ]; then
      REBALANCE=-rebalance
   elif [ "$i" == "-serial" ]; then
      PARALLEL=false
   elif [ "$i" == "-test" ]; then
      TEST=true
   elif [ "$i" == "-?" ]; then
      HELP=true
   elif [ "$i" == "-conf" ]; then
      CONF=true
   elif [ "$i" == "-lib" ]; then
      LIB=true
   elif [ "$i" == "-data" ]; then
      DATA=true
   elif [ "$i" == "-nodata" ]; then
      NO_DATA=true
   elif [ "$i" == "-db" ]; then
      DB=true
   elif [ "$i" == "-all" ]; then
      ALL=true
   elif [ "$i" == "-locators" ]; then
      LOCATORS=true
      LOCATORS_OPT=$i
   elif [ "$i" == "-clients" ]; then
      CLIENTS=true
   elif [ "$i" == "-r" ]; then
      RECURSIVE=-r
   elif [ "$i" == "-kill" ]; then
      KILL=true
   elif [ "$i" == "-debug" ]; then
      DEBUG=true
   elif [ "$i" == "-persist" ]; then
      PERSIST=true
      PERSIST_OPT=$i
   elif [ "$i" == "-pid" ]; then
      PID=true
   elif [ "$i" == "-pidonly" ]; then
      PIDONLY=true
   elif [ "$i" == "-sched" ]; then
      SCHED=true
   elif [ "$i" == "-now" ]; then
      NOW=true
   elif [ "$i" == "-import" ]; then
      IMPORT=true
   elif [ "$i" == "-clean" ]; then
      CLEAN=true
   elif [ "$i" == "-quiet" ]; then
      QUIET=true
   # this must be the last check
   elif [ "$PREV" == "-gateway" ]; then
      GATEWAY_XML_FILE=$i
   fi
   PREV=$i
done

# Determine the server number
if [ $SERVER_NUM -lt 10 ]; then
   SERVER_NUM=0$SERVER_NUM
fi
# Set SERVER_NUM_NO_LEADING_ZERO
if [[ $SERVER_NUM == 0* ]]; then
   SERVER_NUM_NO_LEADING_ZERO=${SERVER_NUM:1};
else
   SERVER_NUM_NO_LEADING_ZERO=$SERVER_NUM
fi

# If the end server number is not defined then
# assign it to the beginning server number.
if [ "$END_NUM" == "" ]; then
   END_NUM=$BEGIN_NUM
fi

# Set the grid options to display in the command usage.
GRID_DEFAULT=
GRIDS_OPT=
ALL_SITES=
for i in $GRIDS
do
   if [ "$GRIDS_OPT" == "" ]; then
      GRIDS_OPT=$i
      GRID_DEFAULT=$i
   else
      GRIDS_OPT=${GRIDS_OPT}"|"$i
   fi
   . grids/$i/grid_env.sh
   ALL_SITES="$ALL_SITES $SITES"
done

# Set all sites found in all grids
unique_words "$ALL_SITES" SITES

# Set the grid to the default grid ID if undefined.
if [ "$GRID" == "" ]; then
   GRID=$GRID_DEFAULT
fi


# Set the etc directory for the grid if undefined.
if [ "$GRIDS_DIR" == "" ]; then
   GRIDS_DIR=$BASE_DIR/bin_sh/grids
fi

# Set the etc directory
if [ "$ETC_DIR" == "" ]; then
   ETC_DIR=$BASE_DIR/etc
fi

# Set the etc directory for the grid if undefined.
if [ "$ETC_GRID_DIR" == "" ]; then
   ETC_GRID_DIR=$ETC_DIR/$GRID
fi

# Set RUN_DIR if not defined
if [ "$RUN_DIR" == "" ]; then
   RUN_DIR=$BASE_DIR/run
fi

# Set SECURITY_DIR if not defined
if [ "$SECURITY_DIR" == "" ]; then
   SECURITY_DIR=$BASE_DIR/security
fi

# Set LOG_DIR if not defined
if [ "$LOG_DIR" == "" ]; then
   LOG_DIR=$BASE_DIR/log
fi

# Set STATS_DIR if not defined
if [ "$STATS_DIR" == "" ]; then
   STATS_DIR=$BASE_DIR/stats
fi

# Set PADO_PLUGINS_DIR if not defined
if [ "$PADO_PLUGINS_DIR" == "" ]; then
   PADO_PLUGINS_DIR=$BASE_DIR/plugins
fi

# Set PADO_APP_CONFIG_DIR if not defined
if [ "$PADO_APP_CONFIG_DIR" == "" ]; then
  PADO_APP_CONFIG_DIR=$ETC_GRID_DIR/app
fi

# Set PADO_PROPERTY_FILE if not defined
if [ "$PADO_PROPERTY_FILE" == "" ]; then
  PADO_PROPERTY_FILE=$ETC_GRID_DIR/pado.properties
fi

# Set PADO_CONFIG_FILE if not defined
if [ "$PADO_CONFIG_FILE" == "" ]; then
  PADO_CONFIG_FILE=$ETC_GRID_DIR/pado.xml
fi

# Set PADO_DB_DIR if not defined
if [ "$PADO_DB_DIR" == "" ]; then
   PADO_DB_DIR=$BASE_DIR/db
fi

# Set PADO_DATA_DIR if not defined
if [ "$PADO_DATA_DIR" == "" ]; then
   PADO_DATA_DIR=$BASE_DIR/data
fi

# Set PADO_BUNDLE_DIR if not defined
if [ "$PADO_BUNDLE_DIR" == "" ]; then
   PADO_BUNDLE_DIR=$BASE_DIR/bundle
fi

# Set GEMFIRE_PROPERTY_FILE if not defined
SERVER_TAILORED_FILE=$ETC_GRID_DIR/server${SERVER_NUM}.properties
if [ -f $SERVER_TAILORED_FILE ]; then
   GEMFIRE_PROPERTY_FILE=$SERVER_TAILORED_FILE
elif [ "$GEMFIRE_PROPERTY_FILE" == "" ]; then
   GEMFIRE_PROPERTY_FILE=$ETC_GRID_DIR/server.properties
fi

# Set SERVER_XML_FILE if not defined
if [ "$SERVER_XML_FILE" == "" ]; then
  SERVER_XML_FILE=$ETC_GRID_DIR/server.xml
fi

# Set the site options to display in the command usage.
SITE_DEFAULT=
SITES_OPT=
for i in $SITES
do
   if [ "$SITES_OPT" == "" ]; then
      SITES_OPT=$i
      SITE_DEFAULT=$i
   else
      SITES_OPT=${SITES_OPT}"|"$i
   fi
done

# Set the site to the default site if undefined.
if [ "$SITE" == "" ]; then
   SITE=$SITE_DEFAULT
fi

LOCATOR_ID=locator-${GRID}-${SITE}${SERVER_NUM}
SERVER_ID=server-${GRID}-${SITE}${SERVER_NUM}

MY_ADDRESS=`getMyAddress`
BIND_ADDRESSES=`getBindAddresses $MY_ADDRESS`
if [ "$BIND_ADDRESSES" == "" ]; then
   SERVER_HOST=$MY_ADDRESS
   SERVER_BIND_ADDRESS=$MY_ADDRESS
   CLIENT_BIND_ADDRESS=$MY_ADDRESS
   GATEWAY_BIND_ADDRESS=$MY_ADDRESS
else
   INDEX=0
   for i in ${BIND_ADDRESSES}; do
      let INDEX=INDEX+1
      if [ "$INDEX" == "1" ]; then
         SERVER_HOST=$i
      elif [ "$INDEX" == "2" ]; then
         SERVER_BIND_ADDRESS=$i
      elif [ "$INDEX" == "3" ]; then
         CLIENT_BIND_ADDRESS=$i
      elif [ "$INDEX" == "4" ]; then
         GATEWAY_BIND_ADDRESS=$i
      else
         break
      fi
   done
fi

OS_NAME=`uname`
if [ "$SSH_USER" == "" ]; then
   SSH_USER=`id -un`
fi
if [ "$REMOTE_BASE_DIR" == "" ]; then
    REMOTE_BASE_DIR=$BASE_DIR
fi
REMOTE_TOP_DIR=${REMOTE_BASE_DIR%\/*}
if [ "$REMOTE_TOP_DIR" == "" ]; then
   REMOTE_TOP_DIR=/
fi

# Load the grid specifics from the grid_env.sh file which
# must be supplied in the grid directory that has the
# same name as the grid ID.
. grids/${GRID}/grid_env.sh > /dev/null 2>&1
