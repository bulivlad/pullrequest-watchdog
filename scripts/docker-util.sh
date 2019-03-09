#!/usr/bin/env bash
#
# Author vladclaudiubulimac on 03/09/2019
#
# Util script used for docker image releases
# The script is adapted for MacOS users and it will try to retrieve the docker
# hub password from KeyChain at DOCKER_HUB_PASSWORD key
# In order to publish the image ./gradlew clean build and docker build are called
# THE SCRIPT SHOULD ONLY BE USED AFTER A NEW GIT TAG WAS PUSHED
# Usage: ./docker-util.sh

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT_DIR="${SCRIPT_DIR}/../"
CURRENT_VERSION="$( git describe --tags --exact-match )"

IMAGE_REPO="dotinc"
IMAGE_NAME="pullrequest-watchdog"

DOCKER_USERNAME=""
DOCKER_PASSWORD=""


function build_jar() {
    pushd "${ROOT_DIR}"
    echo -e "\nBuilding jar"
    ./gradlew clean build
}
function build_docker_image() {
    build_jar
    FULL_IMAGE_NAME=${1}

    echo -e "\nBuilding image ${1}"
    docker build -t "${FULL_IMAGE_NAME}" .
}

function push_docker_image() {
    FULL_IMAGE_NAME="${IMAGE_REPO}/${IMAGE_NAME}:${1}"
    build_docker_image ${FULL_IMAGE_NAME}

    echo -e "Pushing image ${1}"
    docker push "${FULL_IMAGE_NAME}"
}

function get_docker_credentials() {
    read -p "Enter docker hub username: " DOCKER_USERNAME
    UNAME="$(uname -s)"
    if [[ -z "${UNAME}" ]]; then
        echo -e "\nCannot get the running OS. Aborting."
        exit 1
    fi
    if [[ "Darwin" == "${UNAME}" ]]; then
        DOCKER_PASSWORD="$( security find-generic-password -a `whoami` -s DOCKER_HUB_PASSWORD -w )"
    fi
}

function login() {
    get_docker_credentials
    echo -e "\nLogging in to Docker registry..."
    echo "${DOCKER_PASSWORD}" | docker login --username "${DOCKER_USERNAME}" --password-stdin
}

login
push_docker_image "${CURRENT_VERSION}"
push_docker_image "latest"
