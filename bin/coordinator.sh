#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
BENCHMARK_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
PID_FILE=coordinator_instance.pid
if [ $JAVA_HOME ]
then
    echo "JAVA_HOME found at $JAVA_HOME"
    RUN_JAVA=$JAVA_HOME/bin/java
else
    echo "JAVA_HOME environment variable not available."
    RUN_JAVA=`which java 2>/dev/null`
fi

if [ -z $RUN_JAVA ]
then
    echo "JAVA could not be found in your system."
    echo "please install Java 1.7 or higher!!!"
    exit 1
fi

echo "Path to Java : $RUN_JAVA"

#### minimum heap size
#MIN_HEAP_SIZE=4G

#### maximum heap size
#MAX_HEAP_SIZE=4G

if [ "x$MIN_HEAP_SIZE" != "x" ]; then
        JAVA_OPTS="$JAVA_OPTS -Xms${MIN_HEAP_SIZE}"
fi

if [ "x$MAX_HEAP_SIZE" != "x" ]; then
        JAVA_OPTS="$JAVA_OPTS -Xmx${MAX_HEAP_SIZE}"
fi

export CLASSPATH=$BENCHMARK_HOME/lib/benchmark.jar:$BENCHMARK_HOME/lib/hazelcast-all-3.7.jar

echo "########################################"
echo "# RUN_JAVA=$RUN_JAVA"
echo "# JAVA_OPTS=$JAVA_OPTS"
echo "# starting now...."
echo "########################################"

PID=$(cat "${PID_FILE}");
if [ -z "${PID}" ]; then
    echo "Process id for coordinator instance is written to location: {$PID_FILE}"
    $RUN_JAVA $JAVA_OPTS benchmark.Coordinator &
    echo $! > ${PID_FILE}
else
    echo "Another coordinator instance is already started in this folder. To start a new instance, please unzip benchmark.zip/tar.gz in a new folder."
    exit 0
fi
