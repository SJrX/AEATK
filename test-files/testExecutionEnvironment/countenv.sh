#!/bin/bash

#Without this we may kill every process
if [ "$#" -ne 1 ]; then
    echo "Must supply 1 arguments"
    exit 2
fi


fgrep -l "$1=" /proc/*/environ -s | grep -v self | grep -v $$ | wc -l 




