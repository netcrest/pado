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
. ./setenv.sh > /dev/null 2>&1
. ./all_env.sh > /dev/null 2>&1
. ./argenv.sh > /dev/null 2>&1
popd > /dev/null 2>&1

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
  export CLASSPATH=${PLUGINS_JARS}:${CLASSPATH}
fi

if [ "$GEMFIRE_SECURITY_PROPERTY_FILE" == "" ]; then
   GEMFIRE_SECURITY_PROPERTY_FILE=$ETC_DIR/client/gfsecurity.properties
fi

#
# Check if security is enabled
#
GEMFIRE_SECURITY_PROPERTY_SYSTEM=
if [ -f $GEMFIRE_SECURITY_PROPERTY_FILE ]; then
   if [ "$SECURITY_ENABLED" == "true" ]; then
      GEMFIRE_SECURITY_PROPERTY_SYSTEM=-DgemfireSecurityPropertyFile=$GEMFIRE_SECURITY_PROPERTY_FILE
   fi
else
   if [ "$SECURITY_ENABLED" == "true" ]; then
      echo ""
      echo "Security is enabled but the following security file does not exist:"
      echo "   $GEMFIRE_SECURITY_PROPERTY_FILE"
      echo "start_server Aborted."
      echo ""
      exit
   fi
fi

if [ "$SECURITY_ENABLED" == "true" ]; then
   SECURITY_PROPERTIES=-Dpado.security.enabled=true
else
   SECURITY_PROPERTIES=-Dpado.security.enabled=false
fi

if [ "$SECURITY_ENABLED" == "true" ]; then
   GEMFIRE_SECURITY_PROPERTY_SYSTEM="$GEMFIRE_SECURITY_PROPERTY_SYSTEM -Dgemfire.security-client-auth-init=com.netcrest.pado.gemfire.security.PadoAuthInit.create"
fi
SECURITY_PROPERTIES="$SECURITY_PROPERTIES $GEMFIRE_SECURITY_PROPERTY_SYSTEM"

PADO_PROPERTIES="-Dpado.home.dir=$PADO_HOME -Dpado.server=false -Dpado.properties=$ETC_DIR/client/pado.properties -Dpado.command.jar.path=$BASE_DIR/lib/pado-tools.jar -Dpado.security.aes.userCertificate=$SECURITY_DIR/user.cer -Dpado.security.keystore.path=$SECURITY_DIR/client/client-user.keystore"
