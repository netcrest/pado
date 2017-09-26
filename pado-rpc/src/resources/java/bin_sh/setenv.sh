#!/bin/bash

# ========================================================================
# Copyright (c) 2013-2017 Netcrest Technologies, LLC. All rights reserved.
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

#
# You may change this file to assign environment variables. At a minimum,
# you must set the "REQUIRED" environment variables. The "OPTIONAL" 
# environment variables are mostly for reconfiguring rpc-client.
#

#
# IMPORTANT: The following environment variables are set by this file.
# They are used by Pado internals and scripts. Do not set them manually.
#
#  $PADO_HOME - The absolute path of the Pado directory.
#  $BASE_DIR - The absolute path of the RPC directory. 
#

#
# REQUIRED: The following environment varirables are required to run Pado rpc-client.
#
#  $JAVA_HOME - Java home directory path.
#

#
# OPTIONAL: The following environment variables are optional. Please see
#           details further down in this file.
# $HEAP_MIN - Mimimum heap size with the "m" or "g" suffix for MB and GB respectively.
# $HEAP_MAX - Maximum heap size with the "m" or "g" suffix for MB and GB respectively.

#
# Application specifics:
#    $APP_PROPERTIES - Application specific system properties and JVM settings
#             should be set using APP_PROPERTIES. See the APP_PROPERTIES section
#             below for setting system properties. Note that APP_PROPERTIES
#             overrides Pado settings set by start_server.
#    $APP_JARS - This environment variable is automatically set to include all
#             jar files in the $BASE_DIR/lib directory and its 
#             subdirectories. If you want to include other jar files or 
#             class directories then include them in APP_JARS. You might need 
#             to reorder the files listed in APP_JARS if you have multiple versions
#             of classes. If so, prepend the proper jar files. APP_JARS is 
#             found below.
#    $USER_CLASSPATH - Instead of using APP_JARS which also include Pado jars, 
#             you can set your library class paths using this environment
#             variable. Note that USER_CLASSPATH is placed in front of CLASSPATH.
#             You can change the order where USER_CLASSPATH is included in this
#             file.
#

#
# -----------------------------------------------------------------
# Set environment variables below this line.
# -----------------------------------------------------------------
#

# 
# Set JAVA_HOME to the Java home (root) directory
#
export JAVA_HOME=/apps/adf/products/jdk
#export JAVA_HOME="/cygdrive/c/Program Files/Java/jdk1.8.0_144"

#
# Set HEAP_MIN and HEAP_MAX
#
HEAP_MIN=512m
HEAP_MAX=512m


# 
# Application specifics 
#
# List all application specific properties here. Make sure to
# use the prefix "-J-D", e.g., APP_PROPERTIES=-J-Dfoo.test=true
# 
APP_PROPERTIES=

#
# PADO_JAVA executable
#
export PADO_JAVA=$JAVA_HOME/bin/java
export PATH=$JAVA_HOME/bin:$PATH

#
# -----------------------------------------------------------------
# Do not modify below this line except for the order of CLASSPATH.
# -----------------------------------------------------------------
#


#
# PADO_HOME
# This assumes rpc-client is deployed in $PADO_HOME/lang/java/.
#
pushd ../../.. > /dev/null 2>&1
export PADO_HOME=`pwd`
popd  > /dev/null 2>&1

pushd $PADO_HOME/bin_sh > /dev/null 2>&1
. ./setenv.sh
popd  > /dev/null 2>&1

#
# Working directory. Do not edit this line.
#
pushd .. > /dev/null 2>&1
export BASE_DIR=`pwd`
popd  > /dev/null 2>&1

export PATH=$BASE_DIR/bin_sh:$PATH
export LD_LIBRARY_PATH=$BASE_DIR/lib:$LD_LIBRARY_PATH

#
# Application library path 
#
# Append all jar files found in the $BASE_DIR/lib directory and
# its subdirectories in the class path. 
#
APP_JARS=
for file in `find $BASE_DIR/lib -name *.jar`
do
  if [ "${APP_JARS}" ]; then
    APP_JARS=${APP_JARS}:${file}
  else
    APP_JARS=${file}
  fi
done

#
# If jar files in the Pado lib directory need to be ordered, 
# then list them in front of APP_JARS here.
#
APP_JARS=$APP_JARS

# 
# class path
#
if [ "${APP_JARS}" ]; then
   export CLASSPATH=$BASE_DIR/classes:$APP_JARS:$CLASSPATH
else
   export CLASSPATH=$BASE_DIR/classes:$CLASSPATH
fi

#
# Change the order of class path as necessary here
#
if [ "${USER_CLASSPATH}" ]; then
   export CLASSPATH=${USER_CLASSPATH}:$CLASSPATH
fi
