#!/usr/bin/env bash

PID_FILE=coordinator_instance.pid

if [ ! -f "${PID_FILE}" ]; then
    echo "No coordinator instance is running."
else
    PID=$(cat "${PID_FILE}");
    if [ -z "${PID}" ]; then
        echo "No coordinator instance is running."
    else
        kill -15 "${PID}"
        rm "${PID_FILE}"
        echo "Coordinator Instance with PID ${PID} shutdown."
    fi
fi


PID_FILE=worker_instance.pid

if [ ! -f "${PID_FILE}" ]; then
    echo "No worker instance is running."
    exit 0
fi

PID=$(cat "${PID_FILE}");
if [ -z "${PID}" ]; then
    echo "No worker instance is running."
    exit 0
else
   kill -15 "${PID}"
   rm "${PID_FILE}"
   echo "Worker Instance with PID ${PID} shutdown."
   exit 0
fi
