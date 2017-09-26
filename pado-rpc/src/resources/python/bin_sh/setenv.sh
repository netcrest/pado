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
export PYTHON=/home/dpark/anaconda2/bin/python
export PYTHONPATH=$BASE_DIR/src
