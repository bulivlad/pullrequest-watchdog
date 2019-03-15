#!/usr/bin/env bash
#
# Author vladclaudiubulimac on 03/09/2019
#
# Util script used for docker image releases
# The script is adapted for MacOS users and it will try to retrieve the docker
# hub password from KeyChain at DOCKER_HUB_PASSWORD key
# In order to publish the image ./gradlew clean build and docker build are called
# THE SCRIPT SHOULD ONLY BE USED AFTER A NEW GIT TAG WAS PUSHED
# Usage: ./docker-util.sh <parameters>
# parameters:
#           -b          build the images
#           -p          publish the images
#           [-t<=tag>]    build the image with a specific tag

CURRENT_VERSION="$( git describe --tags --exact-match )"
set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT_DIR="${SCRIPT_DIR}/../"

IMAGE_REPO="dotinc"
IMAGE_NAME="pullrequest-watchdog"
IMAGE_PATH="${IMAGE_REPO}/${IMAGE_NAME}"

DOCKER_USERNAME=""
DOCKER_PASSWORD=""

while getopts ":hbpt:" arg; do
    case $arg in
        h)
            echo -e "usage ./docker-util.sh <parameters>"
            echo -e "\tparamters:"
            echo -e "\t\t-b\tbuild the images"
            echo -e "\t\t-p\tpush the images to docker hub"
            echo -e "\t\t[-t<=tag>]    build the image with a specific tag"
            ;;
        b)
            BUILD_IMAGES="true"
            ;;
        p)
            PUSH_IMAGES="true"
            ;;
        t)
            if [[ -z $OPTARG ]] || ( [[ -z "${PUSH_IMAGES}" && -z "${BUILD_IMAGES}" ]] ); then
                echo "Option -$arg requires an argument or at least on of -b or -p are missing." >&2
                exit 1
            fi
            INPUT_TAG=$OPTARG
            ;;
        \?)
          echo "Invalid option: -$OPTARG" >&2
          exit 1
          ;;
        :)
          echo "Option -$OPTARG requires an argument." >&2
          exit 1
          ;;
    esac
done


function build_jar() {
    pushd "${ROOT_DIR}"
    echo -e "\nBuilding jar"
    ./gradlew clean build
}
function build_docker_image() {
    build_jar
    FULL_IMAGE_NAME="${IMAGE_PATH}:${1}"

    echo -e "\nBuilding image ${1}"
    docker build -t "${FULL_IMAGE_NAME}" .
}

function push_docker_image() {
    FULL_IMAGE_NAME="${IMAGE_PATH}:${1}"
    build_docker_image ${1}

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

if [[ ! -z "${BUILD_IMAGES}" ]]; then
    build_docker_image "${CURRENT_VERSION:-dev}"
    build_docker_image "${INPUT_TAG:-latest}"
fi

if [[ ! -z "${PUSH_IMAGES}" ]]; then
    if [[ "${CURRENT_VERSION}" -eq "dev" ]]; then
        echo -e "dev images will not be pushed"
        exit 0
    fi
    login
    push_docker_image "${CURRENT_VERSION:-dev}"
    push_docker_image "${INPUT_TAG:-latest}"
fi
