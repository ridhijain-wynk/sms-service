#!/usr/bin/env bash
#cd ..

HOSTIP=`hostname -f`
env=$1
env=${env:-local}

echo "################ Buidling SMS projects for environment : ${env} ###################"
mvn clean install -P ${env}

echo "################ Deploying SMS for environment : ${env} ###################"

JAVA_DEBUG_OPTS="-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5433"
JAVA_OPTS="-Duser.timezone=Asia/Calcutta -Dfile.encoding=UTF-8 -Danalytic.properties=server.properties"
JMX_OPTS="-Xmx3g -Dhostname=$HOSTIP -Dspring.profiles.active=${env} -XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:MaxTenuringThreshold=1 -XX:+AggressiveOpts -XX:+UseFastAccessorMethods"
BUILD_PATH="build/ignite-sms-platform-0.0.1.jar"

java ${JMX_OPTS} ${JAVA_DEBUG_OPTS} -jar ${BUILD_PATH}