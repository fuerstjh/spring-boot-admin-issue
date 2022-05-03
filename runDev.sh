#!/usr/bin/env bash
export CURRENT_UID=$(id -u):$(id -g)

#Just checking for any variable, otherwise assuming up
if [[ -z $1 ]]; then
    docker-compose -f ./docker/docker-compose.build.yaml run jar-build-no-test
    docker-compose -f ./docker/docker-compose.run.yaml pull
    docker-compose -f ./docker/docker-compose.run.yaml -f ./docker/docker-compose.run.ports.yaml up -d --build
else
    docker-compose -f ./docker/docker-compose.run.yaml down
fi
