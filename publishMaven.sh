#!/bin/bash
set -x

BASE_PATH=$(pwd)
Usage () {                  #定义函数Usage，输出脚本使用方法
    echo "Usage"
    echo "build_script :"
    echo "[-v ] Wind-SDK版本号"
    echo "[-t ] Common SDK 版本号"
    echo "[-l ] 地区后缀"
    echo "[-f ] Wind SDK zip包路径"
    exit 0
}
#maven repo env
repository_id="2154005-release-k0onJU"
REPO_URL="https://packages.aliyun.com/maven/repository/${repository_id}/"
GROUP_ID="com.sigmob.sdk"

function getWindSDKFile() {
    directory=$1
    WIND_SDK_NAME="wind-sdk${LOCATION_SUFFIX}"
    local filelist=$(find ${directory} -maxdepth 2 -type f -name "${WIND_SDK_NAME}*.aar")
    local tmp=${filelist[0]}
    echo $tmp
}

function unzipSDKZip() {
    local zipFile=$1
    local unzipDir=$2
    rm -rf ${unzipDir}
    mkdir -p ${unzipDir}
    unzip -o ${zipFile} -d ${unzipDir}
}

function getWindCommonSDKFile() {
    directory=$1
    WIND_COMMON_SDK_NAME="wind-common${LOCATION_SUFFIX}"
    local filelist=$(find ${directory} -maxdepth 2 -type f -name "${WIND_COMMON_SDK_NAME}*.aar")
    local tmp=${filelist[0]}
    echo $tmp
}


function publishSigmobSDK() {
    AAR_PATH=$1
    ARTIFACT_ID="wind-sdk${LOCATION_SUFFIX}"
    mvn deploy:deploy-file -Durl=$REPO_URL -DrepositoryId=${repository_id} -Dfile=$AAR_PATH -DgroupId=$GROUP_ID -DartifactId=$ARTIFACT_ID -Dpackaging=aar -Dversion="${SDK_VERSION}${SNAPSHOT}" -DgeneratePom=true
}

function publishCommonSDK() {
    AAR_PATH=$1
    ARTIFACT_ID="wind-common${LOCATION_SUFFIX}"
    mvn deploy:deploy-file -Durl=$REPO_URL -DrepositoryId=${repository_id} -Dfile=$AAR_PATH -DgroupId=$GROUP_ID -DartifactId=$ARTIFACT_ID -Dpackaging=aar -Dversion="${COMMON_VERSION}${SNAPSHOT}"  -DgeneratePom=true
}

while getopts ":hc:t:v:f:l:" opt; do
    case $opt in
      h)
          Usage
          exit
          ;;
      c)
          if [ "${OPTARG}" == "true" ]; then
              SNAPSHOT="-SNAPSHOT"
              repository_id="2154005-release-JYlibY"
              REPO_URL="https://packages.aliyun.com/maven/repository/${repository_id}/"
          fi
          ;;
      v)
          SDK_VERSION=${OPTARG}
          ;;
      t)
          COMMON_VERSION=${OPTARG}
          ;;
      f)
          SDKZIP_FILE=${OPTARG}
          ;;
      l)
          if [ "${OPTARG}" = "gp"  ]; then
              LOCATION_SUFFIX="-gp"
          fi
          ;;
      *)
        Usage
        exit 0
        ;;
      esac
done


unzipSDKZip ${SDKZIP_FILE} ${BASE_PATH}/tmp/maven
WIND_SDK_FILE=$(getWindSDKFile "${BASE_PATH}/tmp/maven")
WIND_COMMON_SDK_FILE=$(getWindCommonSDKFile "${BASE_PATH}/tmp/maven")
echo "WIND_SDK_FILE = ${WIND_SDK_FILE}"
echo "WIND_COMMON_SDK_FILE = ${WIND_COMMON_SDK_FILE}"
publishSigmobSDK ${WIND_SDK_FILE}
publishCommonSDK ${WIND_COMMON_SDK_FILE}
