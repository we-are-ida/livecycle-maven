#!/bin/bash

SDK_DIR="/Users/user/SDKs/LiveCycleSDK"
REPO_URL="http://example.com/nexus/content/repositories/releases"
REPO_ID="example-nexus"

mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file \
  -DgroupId=com.adobe.livecycle \
  -DartifactId=livecycle-client \
  -Dversion=9.0 \
  -Dpackaging=jar \
  -Durl=$REPO_URL \
  -DrepositoryId=$REPO_ID \
  -Dfile=$SDK_DIR/Adobe/Adobe LiveCycle ES2.5/LiveCycle_ES_SDK/client-libs/common/adobe-livecycle-client.jar

mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file \
  -DgroupId=com.adobe.livecycle \
  -DartifactId=usermanager-client \
  -Dversion=9.0 \
  -Dpackaging=jar \
  -Durl=$REPO_URL \
  -DrepositoryId=$REPO_ID \
  -Dfile=$SDK_DIR/Adobe/Adobe LiveCycle ES2.5/LiveCycle_ES_SDK/client-libs/common/adobe-usermanager-client.jar

mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file \
  -DgroupId=com.adobe.livecycle \
  -DartifactId=applicationmanager-client \
  -Dversion=9.0 \
  -Dpackaging=jar \
  -Durl=$REPO_URL \
  -DrepositoryId=$REPO_ID \
  -Dfile=$SDK_DIR/Adobe/Adobe LiveCycle ES2.5/LiveCycle_ES_SDK/client-libs/common/adobe-applicationmanager-client-sdk.jar

mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file \
  -DgroupId=com.adobe.livecycle \
  -DartifactId=contentservices-client \
  -Dversion=9.0 \
  -Dpackaging=jar \
  -Durl=$REPO_URL \
  -DrepositoryId=$REPO_ID \
  -Dfile=$SDK_DIR/Adobe/Adobe LiveCycle ES2.5/LiveCycle_ES_SDK/client-libs/common/adobe-contentservices-client.jar
