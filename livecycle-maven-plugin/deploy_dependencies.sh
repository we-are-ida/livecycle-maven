#!/bin/bash

SDK_DIR="/Users/user/SDKs/LiveCycleSDK"

mvn org.apache.maven.plugins:maven-install-plugin:2.4:install-file \
  -DgroupId=com.adobe.livecycle \
  -DartifactId=livecycle-client \
  -Dversion=9.0 \
  -Dpackaging=jar \
  -Dfile=$SDK_DIR/Adobe/Adobe LiveCycle ES2.5/LiveCycle_ES_SDK/client-libs/common/adobe-livecycle-client.jar

mvn org.apache.maven.plugins:maven-install-plugin:2.4:install-file \
  -DgroupId=com.adobe.livecycle \
  -DartifactId=usermanager-client \
  -Dversion=9.0 \
  -Dpackaging=jar \
  -Dfile=$SDK_DIR/Adobe/Adobe LiveCycle ES2.5/LiveCycle_ES_SDK/client-libs/common/adobe-usermanager-client.jar

mvn org.apache.maven.plugins:maven-install-plugin:2.4:install-file \
  -DgroupId=com.adobe.livecycle \
  -DartifactId=applicationmanager-client \
  -Dversion=9.0 \
  -Dpackaging=jar \
  -Dfile=$SDK_DIR/Adobe/Adobe LiveCycle ES2.5/LiveCycle_ES_SDK/client-libs/common/adobe-applicationmanager-client-sdk.jar

mvn org.apache.maven.plugins:maven-install-plugin:2.4:install-file \
  -DgroupId=com.adobe.livecycle \
  -DartifactId=contentservices-client \
  -Dversion=9.0 \
  -Dpackaging=jar \
  -Dfile=$SDK_DIR/Adobe/Adobe LiveCycle ES2.5/LiveCycle_ES_SDK/client-libs/common/adobe-contentservices-client.jar
