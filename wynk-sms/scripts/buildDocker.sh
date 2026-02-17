#!/usr/bin/env bash

#cd ..

env=$1
env=${env:-local}

echo "################ Buidling SMS projects for environment : $env ###################"
mvn clean install -P "$env"

echo "################ Building docker SMS for environment : $env ###################"
docker build -f scripts/Dockerfile.local -t "wynk-sms" --build-arg profile="$env" .
