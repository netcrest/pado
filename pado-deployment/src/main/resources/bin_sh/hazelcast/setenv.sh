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

pushd .. > /dev/null 2>&1
. ./setenv.sh 
. ./all_env.sh
. ./argenv.sh
popd > /dev/null 2>&1

#
# Set Hazelcast major version number (3 or 4)
#
if [ "$HAZELCAST_MAJOR_VERSION_NUMBER" == "" ]; then
   HAZELCAST_MAJOR_VERSION_NUMBER=4
fi

CLASSPATH=$CLASSPATH:$BASE_DIR/lib/hazelcast/v$HAZELCAST_MAJOR_VERSION_NUMBER/*

#
# Append all jar files found in the $BASE_DIR/plugins directory and
# its subdirectories in the class path. 
#
PLUGINS_JARS=
for file in `find $PADO_PLUGINS_DIR -maxdepth 1 -name *.jar |sort -r`
do
  if [ "${PLUGINS_JARS}" ]; then
    PLUGINS_JARS=${PLUGINS_JARS}:${file}
  else
    PLUGINS_JARS=${file}
  fi
done
if [ "${PLUGINS_JARS}" ]; then
  export CLASSPATH=${CLASSPATH}:${PLUGINS_JARS}
fi

if [ "$SECURITY_ENABLED" == "true" ]; then
   SECURITY_PROPERTIES=-Dpado.security.enabled=true
else
   SECURITY_PROPERTIES=-Dpado.security.enabled=false
fi
if [ "$PADOGRID_WORKSPACE" != "" ]; then
   CLASSPATH="${CLASSPATH}:${PADOGRID_WORKSPACE}/plugins:${PADOGRID_WORKSPACE}/lib/*"
fi

HAZELCAST_CLIENT_CONFIG_FILE=$BASE_DIR/etc/hazelcast/hazelcast-client.xml

JAVA_OPTS="-Dhazelcast.logging.type=none"
JAVA_OPTS="$JAVA_OPTS -Dhazelcast.client.config=$HAZELCAST_CLIENT_CONFIG_FILE"
