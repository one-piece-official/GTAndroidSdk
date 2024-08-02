#!/usr/bin/env bash

set -x

BASE_PATH=$(pwd)
chmod +x ${BASE_PATH}/build_script.sh


#export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

WIND_URL="adservice.sigmob.cn/s/config"

function build_ota_plist_android() {
  echo "Generating project.plist"
  cat <<EOF >/opt/php/ciservice/web/projects/com.sigmob.wind.android.demo/project.plist
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>items</key>
  <array>
    <dict>
      <key>assets</key>
      <array>
        <dict>
          <key>kind</key>
          <string>software-package</string>
          <key>url</key>
          <string>$APK_URL</string>
        </dict>
      </array>
    </dict>
  </array>
</dict>
</plist>
EOF
}


VERSION=$(grep "SDK_VERSION" "${BASE_PATH}/sigmob-sdk/src/main/java/com/sigmob/sdk/base/WindConstants.java" | awk -F\" '{print $2}')
VER=$(echo ${VERSION} | awk -F. '{print $1"."$2}')
COMMON_BUILD_VERSION=$(grep "SDK_VERSION" "${BASE_PATH}/common-sdk/common/src/main/java/com/czhj/sdk/common/Constants.java" | awk '{match($NF, /[0-9]+/, m);print m[0]}')
echo "COMMON_BUILD_VERSION " ${COMMON_BUILD_VERSION}
COMMON_VERSION_NAME=$(echo $COMMON_BUILD_VERSION | awk '{print substr($NF,1,1)"."substr($NF,2,1)"."substr($NF,3)}')

LOCATION="cn"
GOOGLE_PLAY=""
if ${IS_GOOGLE_PLAY} ;then
    GOOGLE_PLAY="-g"
    LOCATION="gp"
fi

RELEASE_DIR=/opt/php/ciservice/web/sigmob_release_android/${LOCATION}
TODAY=`date +%Y%m%d`

getNewFile() {
  local directory=$1
  local filelist=($(find ${directory} -type f -maxdepth 2 -name "wind_release_android_${LOCATION}_${VERSION}_*.zip"))
  local tmp=${filelist[0]}
  for ((i = 1; i < ${#filelist[@]}; i++)); do
    if [ ${filelist[i]} -nt $tmp ]; then
      tmp=${filelist[i]}
    fi
  done
  echo $tmp
}

buildSDK(){
  mkdir -p /opt/php/ciservice/web/wind_android/${LOCATION}/${VER}/
  SDK_PATH=/opt/php/ciservice/web/wind_android/${LOCATION}/${VER}/wind_release_android_${LOCATION}_${VERSION}_${TODAY}.zip
  zip_result=`cd "${BASE_PATH}/output/";rm -f "${SDK_PATH}";zip -ry "${SDK_PATH}" "./" -x "./apk/*" -x "*/.DS_Store" -x ".DS_Store"`
  MD5=$(md5 -q ${SDK_PATH})
  RELEASE_PATH_DIR=${RELEASE_DIR}/${VERSION}/${MD5}
  mkdir -p ${RELEASE_PATH_DIR}
  cp -R ${SDK_PATH} ${RELEASE_PATH_DIR}
}

buildAPK(){
     DEMO_DIR="/opt/php/ciservice/web/projects"
     DEPLOY_PARENT="${DEMO_DIR}/com.sigmob.wind.android.demo"
     mkdir -p ${DEPLOY_PARENT}/${VERSION}
     rm -rf ${DEPLOY_PARENT}/${VERSION}/*
     cp ${BASE_PATH}/output/apk/app-debug.apk ${DEPLOY_PARENT}/${VERSION}/app.apk
     cp ${DEPLOY_PARENT}/${VERSION}/app.apk  ${RELEASE_DIR}/${VERSION}/${MD5}/app.apk
     APK_URL="https://sigsdk.happyelements.net/ciservice/projects/com.sigmob.wind.android.demo/${VERSION}/app.apk"
     build_ota_plist_android
}


publishaliyun(){

     SDK_FILE=$(getNewFile /opt/php/ciservice/web/wind_android/${LOCATION}/${VER})
     echo "search sdk.file = ${SDK_FILE}"
     MD5=$(md5 -q ${SDK_FILE})
     echo "sdk.md5 = $MD5"
     DEMO_PATH=${RELEASE_DIR}/${VERSION}/${MD5}/app.apk

     if [ -z ${NEW_PACKAGE_NAME} ];then
          OSS_PATH=wind/android/${VERSION}_${MD5}/wind_release_android_${LOCATION}_${VERSION}_${TODAY}.zip
          OSS_DEMO_PATH=wind/android/${VERSION}_${MD5}/app.apk
     else
          OSS_PATH=wind/rename_android/${VERSION}_${MD5}/wind_release_android_${LOCATION}_${VERSION}_${TODAY}.zip
          OSS_DEMO_PATH=wind/rename_android/${VERSION}_${MD5}/app.apk
     fi
       # 上传 SDK aliyun OSS
     python /opt/aliyun/oss_upload.py -k ${OSS_PATH} -f ${SDK_FILE}
     if [ $? -eq 0 ]; then
         CDN_URL="https://sdkres.sigmob.cn/${OSS_PATH}"
         echo "Upload SDK Aliyun OSS SUCCESS! ${CDN_URL}"
     else
          echo 'Upload SDK Aliyun OSS ERROR!'
          exit -1
     fi

       # 上传 sdk demo aliyun OSS
     python /opt/aliyun/oss_upload.py -k ${OSS_DEMO_PATH} -f ${DEMO_PATH}
     if [ $? -eq 0 ]; then
         CDN_DEMO_URL="https://sdkres.sigmob.cn/${OSS_DEMO_PATH}"
         echo "Upload DEMO Aliyun OSS SUCCESS! ${CDN_DEMO_URL}"
     else
          echo 'Upload DEMO Aliyun OSS ERROR!'
          exit -1
     fi

 # 通知中控平台
          NOW=`date +%s`
          NONCE=`expr ${NOW} \* 1000`

          SIGN_SRC="/ssp/sdk/ci/syncreadme"$NONCE"MTVlMWFmNjRkOTRjYWI0ZmNmZmYzNTg4NDBlMjFhMmI="
          SIGN=`md5 -q -s ${SIGN_SRC}`

          HOST="http://${CON_ENV}.sigmob.com"

          if [[ ${CON_ENV} == "manager" ]]; then
              HOST="https://${CON_ENV}.sigmob.com"
          fi

          SYNC_URL="${HOST}/ssp/sdk/ci/syncreadme?nonce=${NONCE}&sign=${SIGN}"

          # android
          curl -s --data-urlencode "os=2" --data-urlencode "version=${VERSION}" --data-urlencode "cdn=${CDN_URL}" --data-urlencode "source=sigmob" ${SYNC_URL}
          if [[ ${CON_ENV} == "manager" ]]; then
              curl -H "Content-Type: application/json" -X POST -d "{\"events\":[{\"version\":\"${VERSION}\",\"os\":\"Sigmob Android\",\"url\":\"${CDN_URL}\"}]}" "https://www.feishu.cn/flow/api/trigger-webhook/f644af83e65eaea2018db46d83b2a917"
          fi

          if [[ ${CON_ENV} == "manager" ]]; then
              git tag -d tag_${VERSION}
              git push origin :refs/tags/tag_${VERSION}
              git tag -a tag_${VERSION} -m 'Create tag by CI service'
              git push origin tag_${VERSION}
          fi
}

function publishMaven() {
   if  ${BASE_PATH}/publishMaven.sh -v ${VERSION} -t ${COMMON_VERSION_NAME} -f ${SDK_FILE} -l ${LOCATION}; then
       echo "publishMaven.sh -v ${VERSION} -t ${COMMON_VERSION_NAME} -f ${SDK_FILE} -l ${LOCATION} success"
           exit 0
   else
       echo "publishMaven.sh -v ${VERSION} -t ${COMMON_VERSION_NAME} -f ${SDK_FILE} -l ${LOCATION} error"
           exit 1
   fi

}

if ${IS_PUBLISH}; then
    publishaliyun
    publishMaven
    exit 0
else

    if [[ ${CON_ENV} == "manager" ]]; then
      PUBLISHARG="-p"
    fi
    #######################################################################################
    #                                       SDK 逻辑
    #######################################################################################
    MODE="Release"
    GOOGLE_PLAY=""
    if ${IS_GOOGLE_PLAY}; then
        GOOGLE_PLAY="-g"
    fi
    if ${IS_PUBLISH}; then
        if [ -z ${NEW_PACKAGE_NAME} ];then
            if ! ${BASE_PATH}/build_script.sh -c ${MODE} -t 4 -u ${WIND_URL} ${PUBLISHARG} ${GOOGLE_PLAY}; then
                echo "build_script.sh -c ${MODE} -t 4 -u ${WIND_URL} ${PUBLISHARG} -${GOOGLE_PLAY} error"
                exit 1
            fi
        else
            if ! ${BASE_PATH}/build_script.sh -c ${MODE} -t 4 -n ${NEW_PACKAGE_NAME} -u ${WIND_URL} -p ${GOOGLE_PLAY}; then
                echo "build_script.sh -c ${MODE} -t 4 -n ${NEW_PACKAGE_NAME} -u ${WIND_URL} ${PUBLISHARG} ${GOOGLE_PLAY} error"
                exit 1
            fi
        fi
    else
        if [ -z ${NEW_PACKAGE_NAME} ];then
            if ! ${BASE_PATH}/build_script.sh -c ${MODE} -t 4 -u ${WIND_URL} ${GOOGLE_PLAY}; then
                echo "build_script.sh -c ${MODE} -t 4 -u ${WIND_URL} ${GOOGLE_PLAY} error"
                exit 1
            fi
        else
            if ! ${BASE_PATH}/build_script.sh -c ${MODE} -t 4 -n ${NEW_PACKAGE_NAME} -u ${WIND_URL} ${GOOGLE_PLAY}; then
                echo "build_script.sh -c ${MODE} -t 4 -n ${NEW_PACKAGE_NAME} -u ${WIND_URL} ${GOOGLE_PLAY} error"
                exit 1
            fi
        fi
    fi

    VER=`echo ${VERSION} | awk -F. '{print $1"."$2}'`

    if [ -z ${NEW_PACKAGE_NAME} ];then
        APP_NAME="wind_android"
    else
        APP_NAME="wind_rename_android"
    fi

    buildSDK
    buildAPK

fi


set +x
