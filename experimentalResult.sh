#!/bin/bash

LIBCLASSPATH=./lib
CLASSPATH=./bin

naive(){
    java -cp $CLASSPATH:$LIBCLASSPATH/* ems.naiveScheduler.Scheduler $1 
}

singleHome(){
    java -cp $CLASSPATH:$LIBCLASSPATH/* ems.singleHome.Scheduler $1
}

scheduleSummarizer(){
    java -cp $CLASSPATH:$LIBCLASSPATH/* ems.util.ScheduleSummarizer $1
}

multipleHome(){
    java -cp $CLASSPATH:$LIBCLASSPATH/* ems.multipleHome.Scheduler $@
}


if [ $1 == "naive" ]; then
    naive $2
elif [ $1 == "singleHome" ]; then
    singleHome $2
elif [ $1 == "scheduleSummarizer" ]; then
    scheduleSummarizer $2
elif [ $1 == "multipleHome" ]; then
    shift
    multipleHome $@ 
else
    echo "Usage : ./experimentalResult.sh [naive|singleHome|scheduleSummarizer] scheduleFilePath"
fi

