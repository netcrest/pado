#!/bin/bash

pushd .. > /dev/null 2>&1
BASE_DIR=`pwd`
popd > /dev/null 2>&1

LOG_DIR=$BASE_DIR/log
if [ ! -d $LOG_DIR ]; then
  mkdir -p $LOG_DIR
fi
DATA_DIR=$BASE_DIR/data
if [ ! -d $DATA_DIR ]; then
  mkdir -p $DATA_DIR
fi
export PYTHON=python

#
# Application library path
#
# Append all jar files found in the $BASE_DIR/lib directory and
# its subdirectories in the class path.
#
APP_ZIP=
for file in `find $BASE_DIR/lib -name *.zip`
do
  if [ "${APP_ZIP}" ]; then
    APP_ZIP=${APP_ZIP}:${file}
  else
    APP_ZIP=${file}
  fi
done

# For some reason, PTYHONPATH does not work with multiple paths.
# For now, unzip the entire library modules the local-packages directory.
export PYTHONPATH=$BASE_DIR/local-packages:$APP_ZIP
