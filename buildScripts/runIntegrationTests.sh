#!/bin/bash
failures=0
trap 'failures=$((failures+1))' ERR
./gradlew marketing-integration-task1
./gradlew marketing-integration-task2
./gradlew marketing-integration-task3
./gradlew marketing-integration-task4
if ((failures == 0)); then
  echo "Success"
else
  echo "$failures failures"
  exit 1
fi
