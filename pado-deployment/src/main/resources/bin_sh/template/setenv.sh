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

#
# You may change this file to assign environment variables. At a minimum,
# you must set the "REQUIRED" environment variables. The "OPTIONAL" 
# environment variables are mostly for building Pado and running GemFire
# tools. All third-party dependencies are specifed in this file. 
#

#
# IMPORTANT: The following environment variables are set by this file.
# They are used by Pado internals and scripts. Do not set them manually.
#
#  $PADO_HOME - The absolute path of the Pado directory. Same as $BASE_DIR.
#  $BASE_DIR - The absolute path of the Pado directory. Same as $PADO_HOME.
#

#
# REQUIRED: The following environment varirables are required to run Pado.
#
#  $JAVA_HOME - Java home directory path.
#  $GEMFIRE - GemFire home path.
#

#
# OPTIONAL: The following environment variables are optional. Please see
#           details further down in this file.
#    $SSH_USER - The remote SSH user name used to establish ssh connections.
#             Set this variable only if the login user names are different.
#    $REMOTE_BASE_DIR - The remote base directory path. Set this variable
#             only if the base directory in the remote hosts is different
#             from this host's base directory.
#    $COMMAND_PREFIX - If you need to run Pado as root, set this variable to sudo. 
#             All commands will be run with this prefix. For example, if it
#             is set with sudo, then start_server_local is exectued as 
#             "sudo start_server_lcoal".
#

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
# OPTIONAL: The following environment variables are optional.
#
#  $ANT_HOME - The absolute path of the Apache ant root directory. Ant
#              is required to build Pado.
#
# Note that the class path constructed by this script is used by all executables,
# i.e., cacheserver, gfmon, databrower, vsd, and gfsh.

#
# -----------------------------------------------------------------
# Set environment variables below this line.
# -----------------------------------------------------------------
#

# 
# Set JAVA_VERSION to 7 or 8. This is required in order to set the
# proper JVM parameters. Use JDK 7 if GemFire version 8.1 or less.
# Use JDK 8 if GemFire version 8.2 or above. Make sure to set
# JAVA_HOME to the same version.
#
JAVA_VERSION=%%JAVA_VERSION%%

# 
# Set JAVA_HOME to the Java home (root) directory
#
export JAVA_HOME=%%JAVA_HOME%%

#
# GEMFIRE   -- Gemfire root directory path
#
export GEMFIRE=%%GEMFIRE%%

#
# ANT_HOME  -- Ant root directory path
#
export ANT_HOME=%%ANT_HOME%%

# 
# Application specifics 
#
# List all application specific properties here. Make sure to
# use the prefix "-J-D", e.g., APP_PROPERTIES=-J-Dfoo.test=true
# 
#IOT_AGENT_DIR=/apps/adf/iot/iot-agent
#APP_PROPERTIES="-J-Djavax.net.ssl.trustStore=../../security/pado.keystore \
#		-J-Djavax.net.ssl.keyStore=$IOT_AGENT_DIR/security/server.keystore \
#		-J-Djavax.net.ssl.keyStorePassword=jnjiot"
APP_PROPERTIES="%%APP_PROPERTIES%%"

#
# GF_JAVA executable
#
export GF_JAVA=$JAVA_HOME/bin/java
if [ "$ANT_HOME" == "" ]; then
   export PATH=$JAVA_HOME/bin:$GEMFIRE/bin:$GEMFIRE/lib:$PATH
else
   export PATH=$JAVA_HOME/bin:$GEMFIRE/bin:$ANT_HOME/bin:$GEMFIRE/lib:$PATH
fi

# 
# SSH user name 
# Set SSH_USER if the login user name on this host is different from
# other hosts. If not set, then it defaults to the logged on user name.
# SSH_USER enables Pado on this host to manage grids that are running
# with a different user name.
#
SSH_USER=%%SSH_USER%%

#
# Set the remote base directory path if this Pado installation directory is
# different from other hosts. If not set, then it defaults to this Pado
# installation directory path. REMOTE_BASE_DIR enables Pado on this host
# to manage grids that are running in a different base directory (PADO_HOME).
#
REMOTE_BASE_DIR=%%REMOTE_BASE_DIR%%

#
# Command prefix
# Set COMMAND_PREFIX to add a prefix to each command. For example,
# if it is set to "sudo", all commands will be executed with sudo allowing
# Pado to execute as root.
COMMAND_PREFIX=%%COMMAND_PREFIX%%


#
# -----------------------------------------------------------------
# Do not modify below this line except for the order of CLASSPATH.
# -----------------------------------------------------------------
#

#
# Working directory. Do not edit this line.
#
pushd .. > /dev/null 2>&1
export BASE_DIR=`pwd`
popd  > /dev/null 2>&1

export PATH=$BASE_DIR/bin_sh:$PATH
export LD_LIBRARY_PATH=$BASE_DIR/lib:$GEMFIRE/lib:$LD_LIBRARY_PATH

#
# PADO_HOME
#
export PADO_HOME=$BASE_DIR

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
   export CLASSPATH=$BASE_DIR/classes:$APP_JARS:$GEMFIRE/lib/gemfire.jar:$GEMFIRE/lib/antlr.jar:$GEMFIRE/lib/gfsh-dependencies.jar
else
   export CLASSPATH=$BASE_DIR/classes:$GEMFIRE/lib/gemfire.jar:$GEMFIRE/lib/antlr.jar:$GEMFIRE/lib/gfsh-dependencies.jar
fi

#
# Change the order of class path as necessary here
#
if [ "${USER_CLASSPATH}" ]; then
   export CLASSPATH=${USER_CLASSPATH}:$CLASSPATH
fi
