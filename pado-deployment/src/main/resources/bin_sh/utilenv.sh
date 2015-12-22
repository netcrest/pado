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
# Returns trimmed string
# @param string
#
function trimString
{
   echo "$1" | xargs
}

#
# Returns all of this host's addresses.
#
function getAllMyAddresses 
{ 
   OS_NAME=`uname -s`
   if [ "$OS_NAME" == "Darwin" ]; then
      /sbin/ifconfig |grep -B1 "inet " |awk '{ if ( $1 == "inet" ) { print $2 } else if ( $2 == "Link" ) { printf "%s" ,$1 } }' |awk -F: '{ print $1 " " $3 }'
   else
      /sbin/ifconfig |grep -B1 "inet addr" |awk '{ if ( $1 == "inet" ) { print $2 } else if ( $2 == "Link" ) { printf "%s:" ,$1 } }' |awk -F: '{ print $1 ": " $3 }'
   fi
}

#
# Returns true if the specified address belongs to this host.
# @param address
#
function isMyAddress
{
   addr=$1
   if [ "$addr" == "localhost" ]; then
      FOUND=true
   elif [ "$addr" == "`hostname`" ]; then
      FOUND=true
   else 
      IP_ADDRESSES=`getAllMyAddresses`
      FOUND=false
      for i in ${IP_ADDRESSES}; do
         if [ "$i" == "$addr" ]; then
            FOUND=true
            break
         fi
      done
   fi
   echo $FOUND 
}

#
# Returns a list of bind addresses found in $GRIDS_DIR/$GRID/bind_${SITE}.sh
# that matches the specified ServerHost address.
# @param ServerHost address. See bind_${SITE}.sh for details.
#
function getBindAddresses
{
   addr=$1
   file=$GRIDS_DIR/$GRID/bind_${SITE}.sh
   while read line
   do
      # Skip comment and blank lines
      if [ ${#line} == 0 ] || [ "${line:0:1}" == "#" ]; then
         continue
      fi
      array=($line)
      SERVER_HOST=${array[0]}
      if [ "$SERVER_HOST" == "$addr" ]; then
         BIND_ADDRESSES=$line
         break
     fi
   done < $file
   echo $BIND_ADDRESSES 
}

#
# Sets the global variable LineArray to trimmed lines except comment and blank lines
# read from the specified file. The returned array is sorted by server number.
# It also sets NEXT_AVAILABLE_SERVER_NUM to the first server number that is available
# for addition, LAST_SERVER_NUM to the last index of the array, and SERVER_COUNT the
# number of servers.
# @param file_path
#
function setLineArray
{
   file=$1
   # Unset all array elements
   unset LineArray
   LAST_SERVER_NUM=0
   SERVER_COUNT=0
   while read line
   do
      # Skip comment and blank lines
      if [ ${#line} == 0 ] || [ "${line:0:1}" == "#" ]; then
         continue
      fi
      if [ "$line" != "" ]; then
         array=($line)
         index=${array[0]}
         LineArray[$index]=$line
         if [ $index -gt $LAST_SERVER_NUM ]; then
            LAST_SERVER_NUM=$index
         fi
         let SERVER_COUNT=SERVER_COUNT+1
      fi
   done < $file
   sorted=($(printf '%s\n' "${LineArray[@]}"|sort))

   NEXT_AVAILABLE_SERVER_NUM=0
   for (( i = 1; i <= $LAST_SERVER_NUM; i++ ))
   do
      line="${LineArray[$i]}"
      array=($line)
      __SERVER_NUM=${array[0]}
      if [ "$__SERVER_NUM" == "" ]; then
         NEXT_AVAILABLE_SERVER_NUM=$i
         break;
      fi
   done
   if [ $NEXT_AVAILABLE_SERVER_NUM == "0" ]; then
      let NEXT_AVAILABLE_SERVER_NUM=LAST_SERVER_NUM+1
   fi
}

#
# Returns this host's address that matches the ServerHost
# address listed in $GRIDS_DIR/$GRID/bind_${SITE}.sh.
#
function getMyAddress
{
   MY_ADDRESSES="`getAllMyAddresses` localhost `hostname` `hostname`.local"
   for i in ${MY_ADDRESSES}; do
      BIND_ADDRESSES=`getBindAddresses $i`
      if [ "$BIND_ADDRESSES" != "" ]; then
         echo $i
         break
      fi
   done
}

#
# Returns a list of host names extracted from the second column of each row of 
# the specified file path.
#
# @param filePath  File path. Example files are locator_<SITE>.sh, server_<SITE>.sh.
#
function getHostsFromFile
{
   local __FILE_PATH=$1
   local __SERVER_HOSTS=
   while read line
   do
      if [[ "$line" =~ ^.*#.* ]]; then
         continue
      else
         SERVER_ADDRESSES=$line
         INDEX=0
         for i in ${SERVER_ADDRESSES}; do
            let INDEX=INDEX+1
            if [ "$INDEX" == "2" ]; then
               __EXISTS=false
               if [ "$__SERVER_HOSTS" != "" ]; then
                  for j in ${__SERVER_HOSTS}; do
                     if [ "$i" == "$j" ]; then
                        __EXISTS=true
                        break;
                     fi 
                  done
               fi
               if [ "$__EXISTS" == "false" ]; then
                  __SERVER_HOSTS="$__SERVER_HOSTS $i"
               fi
               break
            fi
         done
      fi
   done < $__FILE_PATH
   echo $__SERVER_HOSTS
}

#
# Returns a list of locator hosts found in the $GRIDS_DIR/$GRID/locator_${SITE}.sh file.
#
function getLocatorHosts
{
   echo `getHostsFromFile "$GRIDS_DIR/$GRID/locator_${SITE}.sh"`
}

#
# Returns a list of server hosts found in the $GRIDS_DIR/$GRID/server_${SITE}.sh file.
#
function getServerHosts
{
#   getHostsFromFile "$GRIDS_DIR/$GRID/server_${SITE}.sh" __SERVER_HOSTS
#   echo $__SERVER_HOSTS
   getHostsFromFile "$GRIDS_DIR/$GRID/server_${SITE}.sh"
}

#
# arg1 string_list - string list of words separated by spaces
# arg2 output - string list with unique words
#
function unique_words
{
   local __words=$1
   local  __resultvar=$2
   local __visited
   local __unique_words
   local __i
   local __j

   # remove all repeating hosts
   for __i in $__words; do
      __visited=false
      for __j in $__unique_words; do
         if [ "$__i" == "$__j" ]; then
            __visited=true
         fi
      done
      if [ "$__visited" == "false" ]; then
         __unique_words="$__unique_words $__i"
      fi
   done

   if [[ "$__resultvar" ]]; then
      eval $__resultvar="'$__unique_words'"
      #echo `trimString "$__resultvar"`
   else
     echo `trimString "$__unique_words"`
   fi
}

#
# arg1 string_list - string list separated by spaces
# args output - the largest string length
#
function max_len_word
{
   local __servers=$1
   local  __resultvar=$2
   local __maxlen=0
   local __len
   local __i
  
   for __i in $__servers; do
      __len=${#__i}
      if [ "$__len" -gt "$__maxlen" ]; then
          __maxlen=$__len
      fi
   done

   if [[ "$__resultvar" ]]; then
      eval $__resultvar="'$__maxlen'"
   else
     echo "$__maxlen"
   fi
}

#
# arg1 maxlen - the max length of string
# arg2 value - string to evaluate
# arg3 output - output value
function append_spaces
{
   local __maxlen=$1
   local  myresult=$2
   local  __resultvar=$3
   local __i

   let X=${#2}
   OS_NAME=`uname -s`

   if [ "$OS_NAME" == "Darwin" ]; then
      if [ "$X" -gt "0" ]; then
         # seq not available on Mac. Use jot instead.
         for __i in `jot - $X $__maxlen`; do
            myresult="$myresult "
         done
      fi
   else
      if [ "$X" -gt "0" ]; then
         for __i in `seq $X $__maxlen`; do
            myresult="$myresult "
         done
      fi
   fi
   if [[ "$__resultvar" ]]; then
      eval $__resultvar="'$myresult'"
   else
     echo "$myresult"
   fi
}

#
# Returns the number of locators defined in the $GRIDS_DIR/$GRID/locator_${SITE}.sh file.
#
function getLocatorCount
{
   setLineArray $GRIDS_DIR/$GRID/locator_${SITE}.sh
   echo $SERVER_COUNT
}

#
# Returns the number of servers defined in the $GRIDS_DIR/$GRID/server_${SITE}.sh file.
#
function getServerCount
{
   setLineArray $GRIDS_DIR/$GRID/server_${SITE}.sh
   echo $SERVER_COUNT
}

#
# Returns the locator number range in the format of #-# by reading the $GRIDS_DIR/$GRID/locator_${SITE}.sh file.
#
function getLocatorNumberRange
{
   LOCATOR_COUNT=`getLocatorCount`
   if [ "$LOCATOR_COUNT" == "1" ]; then
      echo $LOCATOR_COUNT
   else 
      echo 1-$LOCATOR_COUNT
   fi
}

#
# Returns GemFire locators in the format of "<host>[<port>],..."
#
function getGemfireLocators
{
   # LocatorArray elements:
   #    array[0] LocatorNumber
   #    array[1] LocatorHost
   #    array[2] LocatorPort
   #    array[3] JmxRmiPort
   #    array[4] JmxHttpPort
   setLineArray $GRIDS_DIR/$GRID/locator_${SITE}.sh

   LOCATORS=""
   for (( i = 1; i <= $LAST_SERVER_NUM; i++ ))
   do
      line="${LineArray[$i]}"
      array=($line)
      LOCATOR_NUM=${array[0]}
      if [ "$LOCATOR_NUM" == "" ]; then
         continue;
      fi
      LOCATOR_HOST=${array[1]}
      LOCATOR_PORT=${array[2]}
      if [ "$LOCATORS" == "" ]; then
         LOCATORS="$LOCATOR_HOST[${LOCATOR_PORT}]"
      else
         LOCATORS=${LOCATORS}",$LOCATOR_HOST[${LOCATOR_PORT}]"
      fi
   done
   echo $LOCATORS
}

#
# Returns Pado locators in the format of "<host>:<port>,..."
#
function getPadoLocators
{
   # LocatorArray elements:
   #    array[0] LocatorNumber
   #    array[1] LocatorHost
   #    array[2] LocatorPort
   #    array[3] JmxRmiPort
   #    array[4] JmxHttpPort
   setLineArray $GRIDS_DIR/$GRID/locator_${SITE}.sh

   LOCATORS=""
   for (( i = 1; i <= $LAST_SERVER_NUM; i++ ))
   do
      line="${LineArray[$i]}"
      array=($line)
      LOCATOR_NUM=${array[0]}
      if [ "$LOCATOR_NUM" == "" ]; then
         continue;
      fi
      LOCATOR_HOST=${array[1]}
      LOCATOR_PORT=${array[2]}
      if [ "$LOCATORS" == "" ]; then
         LOCATORS="$LOCATOR_HOST:${LOCATOR_PORT}"
      else
         LOCATORS=${LOCATORS}",$LOCATOR_HOST:${LOCATOR_PORT}"
      fi
   done
   echo $LOCATORS
}

# 
# Returns "true" if number, else "false"
# @param number
#
function isNumber
{
   num=$1 
   [ ! -z "${num##*[!0-9]*}" ] && echo "true" || echo "false";
}
