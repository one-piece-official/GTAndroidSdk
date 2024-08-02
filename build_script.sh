#!/bin/bash

set -x
Usage () {                  #定义函数Usage，输出脚本使用方法
    echo "Usage"
    echo "build_script :"
    echo "[-c ] 参数=Debug/Release"
    echo "[-t ] 参数=2: 代表Common 3: 代表Sigmob"
    echo "[-n ] 参数=改包名名称仅Sigmob"
    echo "[-u ] 参数=Sigmob ConfigUrl"
     echo "[-g ] 无参数 (打包Google Play环境)"
    echo "[-a ] 无参数 打包apk，默认仅打包framework"
    echo "[-p ] 无参数 代表是否发布"
    exit 0
}

SIG_IS_DEBUG="false"
SIG_IS_GOOGLE_PLAY="false"
SIG_BUILD_TYPE=1
SIG_NEW_PACKAGENAME=""
SIG_IS_APK="false"
SIG_IS_PUBLISH="false"
SIGMOB_CONFIG_URL="adservice.sigmob.cn/s/config"
SIG_CONFIG_URL=""
SNAPSHOT=""
SIG_VERSION=""
# SIGMOB_CONFIG_URL="adstage.sigmob.cn/sigmob/config"
# WINDMILL_CONFIG_URL="adstage.sigmob.cn/windmill/config"


WIND_BASE_PATH=`pwd`
# CHANGE_TOOL_PATH="/opt/android_change_package"
if [ -z ${CHANGE_TOOL_PATH} ]; then
  CHANGE_TOOL_PATH="/Users/happyelements/android_change_package"

  if [ ! -d $CHANGE_TOOL_PATH ]; then
    CHANGE_TOOL_PATH="/opt/android_change_package"
  fi
fi

if [ -z ${ANDROID_HOME} ]; then
  ANDROID_HOME="/Users/happyelements/Library/Android/sdk/"
fi

SIG_BUILD_DEMO="-SIG_BUILD_DEMO"


AAR_PATH="${WIND_BASE_PATH}/output/AAR/"
APK_RELEASE="${WIND_BASE_PATH}/output/apk/"
README_PATH="${WIND_BASE_PATH}/ReadMe/"
README_OUT_PATH="${WIND_BASE_PATH}/output/ReadMe/"

SIGMOB_AAR=${WIND_BASE_PATH}/sigmob-sdk/build/outputs/aar/sigmob-sdk-release.aar
WIND_AAR=${WIND_BASE_PATH}/sigmob-sdk/build/outputs/aar/wind-sdk-release.aar
WIND_CONSTANTS_PATH=${WIND_BASE_PATH}/sigmob-sdk/src/main/java/com/sigmob/sdk/base/WindConstants.java
COMMON_GRADLE=${WIND_BASE_PATH}/common-sdk/common/build.gradle
COMMON_CONSTANTS_PATH=${WIND_BASE_PATH}/common-sdk/common/src/main/java/com/czhj/sdk/common/Constants.java
ANDROIDMANIFEST_PATH=${WIND_BASE_PATH}/sigmob-sdk/src/main/AndroidManifest.xml
COMMON_VERSION_NAME=""
SIG_NEW_BUNDLE_PATH=""
wind_artifactId="wind-sdk"



 


############################################################################################################################
# 处理 WIND_CONSTANTS_PATH SIG_IS_DEBUG 字段
############################################################################################################################
function enableGooglePlay() {

      echo "set WincConstants GOOGLE_PLAY Status : " +${SIG_IS_DEBUG}
      IS_DEBUG_LINE_NUM=$(grep -n "GOOGLE_PLAY" ${WIND_CONSTANTS_PATH} | awk -F: '{print $1}' | head -n 1)

      sed -i '' "${IS_DEBUG_LINE_NUM}d" ${WIND_CONSTANTS_PATH}

      if [ "${SIG_IS_GOOGLE_PLAY}" == "false" ]; then
        SIG_INSERT_CODE="\  public static final Boolean GOOGLE_PLAY = false;"
      else
        SIG_INSERT_CODE="\  public static final Boolean GOOGLE_PLAY = true;"
      fi

      sed -i "" "${IS_DEBUG_LINE_NUM} a\ 
      ${SIG_INSERT_CODE}
      " ${WIND_CONSTANTS_PATH}

      grep "GOOGLE_PLAY" ${WIND_CONSTANTS_PATH}
}

############################################################################################################################
# 处理 WIND_CONSTANTS_PATH SIG_IS_DEBUG 字段
############################################################################################################################
function setDebug() {

      echo "set WincConstants Debug Status : " +${SIG_IS_DEBUG} 
      IS_DEBUG_LINE_NUM=$(grep -n "IS_DEBUG" ${WIND_CONSTANTS_PATH} | awk -F: '{print $1}' | head -n 1)

      sed -i '' "${IS_DEBUG_LINE_NUM}d" ${WIND_CONSTANTS_PATH}

      if [ "${SIG_IS_DEBUG}" == "false" ]; then
        SIG_INSERT_CODE="\  public static final Boolean IS_DEBUG = false;"
      else
        SIG_INSERT_CODE="\  public static final Boolean IS_DEBUG = true;"
      fi

      sed -i "" "${IS_DEBUG_LINE_NUM} a\ 
      ${SIG_INSERT_CODE}
      " ${WIND_CONSTANTS_PATH}

      grep "IS_DEBUG" ${WIND_CONSTANTS_PATH}
}


############################################################################################################################
# 处理 WIND_CONSTANTS_PATH SDK_FOLDER 字段
############################################################################################################################
function changeSDKFolder() {
      if [ -n "${SIG_NEW_PACKAGENAME}" ]; then

        SDK_FOLDER_LINE_NUM=$(grep -n "SDK_FOLDER" ${WIND_CONSTANTS_PATH} | awk -F: '{print $1}' | head -n 1)

        sed -i '' "${SDK_FOLDER_LINE_NUM}d" ${WIND_CONSTANTS_PATH}

        SIG_INSERT_CODE="\  public static final String SDK_FOLDER = \"${SIG_NEW_PACKAGENAME}\";"

        sed -i "" "${SDK_FOLDER_LINE_NUM} a\ 
    ${SIG_INSERT_CODE}
        " ${WIND_CONSTANTS_PATH}
      fi

        grep "SDK_FOLDER" ${WIND_CONSTANTS_PATH}
}


############################################################################################################################
# 处理 WIND_CONSTANTS_PATH SIG_CONFIG_URL 字段
############################################################################################################################
function setConfigUrl() {
    if [ -n "${SIG_CONFIG_URL}" ]; then
 
        CONFIG_URL_LINE_NUM=$(grep -n "CONFIG_URL" ${WIND_CONSTANTS_PATH} | awk -F: '{print $1}'| head -n 1)
        sed -i '' "${CONFIG_URL_LINE_NUM}d" ${WIND_CONSTANTS_PATH}
        SIG_INSERT_CODE="\  public static final String CONFIG_URL = \"${SIG_CONFIG_URL}\";"
        sed -i "" "${CONFIG_URL_LINE_NUM} a\ 
        ${SIG_INSERT_CODE}
        " ${WIND_CONSTANTS_PATH}

    fi

    grep "CONFIG_URL" ${WIND_CONSTANTS_PATH}

      
}
############################################################################################################################
# 处理 WIND_CONSTANTS_PATH SIG_CONFIG_URL 字段
############################################################################################################################
function checkConfigUrl() {

        ORIGIN_URL=$(grep "CONFIG_URL" ${WIND_CONSTANTS_PATH} | awk -F= '{split($NF,m,";");print m[1]}')
        echo ${ORIGIN_URL} \"${SIGMOB_CONFIG_URL}\"
        if [ ${ORIGIN_URL} != \"${SIGMOB_CONFIG_URL}\" ]; then
            echo "checkConfigUrl error!"
            exit -1
        fi    
}


############################################################################################################################
# 处理 WIND_CONSTANTS_PATH COMMON_VERSION 字段
############################################################################################################################
function setCommonVersion() {

    echo "set WincConstants Debug Status : " +${SIG_IS_DEBUG} 

    COMMON_BUILD_VERSION=$(grep "SDK_VERSION" ${COMMON_CONSTANTS_PATH} | awk '{match($NF,/[0-9]+/,m);print m[0]}')

    COMMON_LINE_NUM=$(grep -n "COMMON_VERSION" ${WIND_CONSTANTS_PATH} | awk -F: '{print $1}')

    sed -i '' "${COMMON_LINE_NUM}d" ${WIND_CONSTANTS_PATH}

    SIG_INSERT_CODE="\  public static final int COMMON_VERSION = ${COMMON_BUILD_VERSION};"

    sed -i "" "${COMMON_LINE_NUM} a\ 
    ${SIG_INSERT_CODE}
      " ${WIND_CONSTANTS_PATH}

    COMMON_VERSION_NAME=$(echo $COMMON_BUILD_VERSION | awk '{print substr($NF,1,1)"."substr($NF,2,1)"."substr($NF,3)}')

    echo "COMMON_VERSION_NAME " ${COMMON_VERSION_NAME}

    grep "COMMON_VERSION" ${WIND_CONSTANTS_PATH}
}

############################################################################################################################
# 处理 WIND_CONSTANTS_PATH COMMON_VERSION 字段
############################################################################################################################
function setSIGCommonVersion() {

    echo "set WincConstants Debug Status : " +${SIG_IS_DEBUG}

    SIG_COMMON_BUILD_VERSION=$(grep "SIG_VERSION" ${COMMON_CONSTANTS_PATH} | awk '{match($NF,/[0-9]+/,m);print m[0]}')

    SIG_COMMON_LINE_NUM=$(grep -n "SIG_VERSION" ${WIND_CONSTANTS_PATH} | awk -F: '{print $1}')

    sed -i '' "${SIG_COMMON_LINE_NUM}d" ${WIND_CONSTANTS_PATH}

    SIG_COMMON_INSERT_CODE="\  public static final int SIG_VERSION = ${SIG_COMMON_BUILD_VERSION};"

    sed -i "" "${SIG_COMMON_LINE_NUM} a\ 
    ${SIG_COMMON_INSERT_CODE}
      " ${WIND_CONSTANTS_PATH}

    SIG_COMMON_VERSION_NAME=$(echo SIG_COMMON_BUILD_VERSION | awk '{print substr($NF,1,1)"."substr($NF,2,1)"."substr($NF,3)}')

    echo "SIG_COMMON_VERSION_NAME " ${SIG_COMMON_VERSION_NAME}

    grep "SIG_VERSION" ${WIND_CONSTANTS_PATH}
}


function setAndroidManifest() {
    if [ "${SIG_IS_GOOGLE_PLAY}" == "true" ]; then
      cp ${WIND_BASE_PATH}/AndroidManifest.xml.gp ${ANDROIDMANIFEST_PATH}
    else
      cp ${WIND_BASE_PATH}/AndroidManifest.xml.cn ${ANDROIDMANIFEST_PATH}
    fi
}






function build_ota_plist_android() {
  APK_URL="http://sigci.happyelements.net/ciservice/projects/${CI_DEMO_NAME}/${SIG_VERSION}/${CI_DEMO_NAME}.apk"
  if [ "${SIG_IS_DEBUG}" == "true" ]; then
    APK_URL="http://sigci.happyelements.net/ciservice/projects/${CI_DEMO_NAME}/${SIG_VERSION}/${CI_DEMO_NAME}-test.apk"
  fi
  echo "Generating project.plist"
  cat <<EOF >/opt/php/ciservice/web/projects/${CI_DEMO_NAME}/project.plist
<?xml SIG_VERSION="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist SIG_VERSION="1.0">
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


function repackSigmobAAR() {

    SIG_OLD_BUILD_VERSION=$(echo ${SIG_BUILD_VERSION} | awk -F. '{print $1"\\\."$2"\\\."$3}')
    if [ "${SIG_NEW_PACKAGENAME}" != "wind" ]; then
        NEW_SiGMOB_BUILD_VERSION=$(echo ${SIG_BUILD_VERSION} | awk -F. '{print $1"."$2".2"$3}')
    else
        NEW_SiGMOB_BUILD_VERSION=$(echo ${SIG_BUILD_VERSION} | awk -F. '{print $1"."$2".1"$3}')
    fi

    echo "BUILD SIG_VERSION : "+SIG_BUILD_VERSION 
    echo "OLD BUILD SIG_VERSION : "+SIG_OLD_BUILD_VERSION 
    echo "NEW BUILD SIG_VERSION : "+NEW_SiGMOB_BUILD_VERSION 
    SIG_NEW_BUNDLE_PATH=${WIND_BASE_PATH}/${NEW_SiGMOB_BUILD_VERSION}

    rm -rf ${SIG_NEW_BUNDLE_PATH}
    mkdir -p ${SIG_NEW_BUNDLE_PATH}

    unzip $SIGMOB_AAR -d ${SIG_NEW_BUNDLE_PATH}/repackAAR
    cd ${SIG_NEW_BUNDLE_PATH}/repackAAR
    ${ANDROID_HOME}/build-tools/28.0.3/dx --dex --output=${SIG_NEW_BUNDLE_PATH}/repackAAR/classes.dex ${SIG_NEW_BUNDLE_PATH}/repackAAR/classes.jar
    java -jar ${CHANGE_TOOL_PATH}/baksmali-2.3.4.jar d ${SIG_NEW_BUNDLE_PATH}/repackAAR/classes.dex -o ${SIG_NEW_BUNDLE_PATH}/repackAAR/out
    rm -rf ${SIG_NEW_BUNDLE_PATH}/repackAAR/classes.dex
    ls -l ${SIG_NEW_BUNDLE_PATH}/repackAAR
    mv ${SIG_NEW_BUNDLE_PATH}/repackAAR/out/com/sigmob ${SIG_NEW_BUNDLE_PATH}/repackAAR/out/com/${SIG_NEW_PACKAGENAME}
    find ${SIG_NEW_BUNDLE_PATH}/repackAAR/out -name "*.smali" | xargs sed -i "" "s/Lcom\/sigmob/Lcom\/${SIG_NEW_PACKAGENAME}/g"
    find ${SIG_NEW_BUNDLE_PATH}/repackAAR/out -name "*.smali" | xargs sed -i "" "s/${SIG_OLD_BUILD_VERSION}/${NEW_SiGMOB_BUILD_VERSION}/g"

    java -jar ${CHANGE_TOOL_PATH}/smali-2.3.4.jar a ${SIG_NEW_BUNDLE_PATH}/repackAAR/out -o ${SIG_NEW_BUNDLE_PATH}/repackAAR/classes.dex
    rm -rf ${SIG_NEW_BUNDLE_PATH}/repackAAR/classes.jar
    sh ${CHANGE_TOOL_PATH}/dex-tools-2.1-SNAPSHOT/d2j-dex2jar.sh ${SIG_NEW_BUNDLE_PATH}/repackAAR/classes.dex -o ${SIG_NEW_BUNDLE_PATH}/repackAAR/classes.jar
    find ${SIG_NEW_BUNDLE_PATH}/repackAAR/ -name "AndroidManifest.xml" | xargs sed -i "" "s/com.sigmob/com.${SIG_NEW_PACKAGENAME}/g"
    find ${SIG_NEW_BUNDLE_PATH}/repackAAR/ -name "*_layout.xml" | xargs sed -i "" "s/com.sigmob/com.${SIG_NEW_PACKAGENAME}/g"
    find ${SIG_NEW_BUNDLE_PATH}/repackAAR/ -name "proguard.txt" | xargs sed -i "" "s/com.sigmob/com.${SIG_NEW_PACKAGENAME}/g"

    rm -rf ${SIG_NEW_BUNDLE_PATH}/repackAAR/classes.dex
    rm -rf ${SIG_NEW_BUNDLE_PATH}/repackAAR/out


    cd ${SIG_NEW_BUNDLE_PATH}/repackAAR/
    
    zip -q -r ${WIND_AAR} * -x "*/\.*" -x "\.*"
    cd ..

}

function buildCommonSDK() {


  cd ${WIND_BASE_PATH}/common-sdk

  setCommonVersion
  setSIGCommonVersion

  GOOGLE_PLAY=""
  if [ "${SIG_IS_GOOGLE_PLAY}" == "true" ]; then
      GOOGLE_PLAY="-g"
  fi
  if [ "${SIG_IS_PUBLISH}" == "true" ]; then
    ./common_script.sh -c ${SIG_IS_DEBUG} -p ${GOOGLE_PLAY}
  else
    ./common_script.sh -c ${SIG_IS_DEBUG} ${GOOGLE_PLAY}
  fi
  mkdir -p ${WIND_BASE_PATH}/output/AAR/
  cp ${WIND_BASE_PATH}/common-sdk/output/AAR/*.aar ${WIND_BASE_PATH}/output/AAR/
  cd ..
#  if [ "${SIG_IS_GOOGLE_PLAY}" == "true" ]; then
#    cp -f ${WIND_BASE_PATH}/common-sdk/build-SigCommon-gp.gradle ${COMMON_GRADLE}
#  else
#    cp -f ${WIND_BASE_PATH}/common-sdk/build-SigCommon-cn.gradle ${COMMON_GRADLE}
#  fi
#
#  setCommonVersion
#  ${WIND_BASE_PATH}/gradlew :common:clean
#  ${WIND_BASE_PATH}/gradlew :common:assembleRelease -PbuildVersion=${COMMON_VERSION_NAME}
#  if [ $? != 0 ]; then
#    echo "gradlew common assembleRelease error!"
#    exit -1
#  fi
#  ${WIND_BASE_PATH}/gradlew :common:copyAAR -PbuildVersion=${COMMON_VERSION_NAME}
#  if [ $? != 0 ]; then
#    echo "gradlew common copyAAR error!"
#    exit -1
#  fi
#
#  if [ "${SIG_IS_PUBLISH}" == "true" ]; then
#     pushMavenCommonSDK
#  fi

}

function buildSigmobSDK() {


  SIG_CONFIG_URL=${SIGMOB_CONFIG_URL}
  setConfigUrl
  checkConfigUrl
  enableGooglePlay
  setDebug
  setAndroidManifest

  if [ "${SIG_IS_GOOGLE_PLAY}" == "true" ]; then
    cp ${WIND_BASE_PATH}/build-sigmob-gp.gradle ${WIND_BASE_PATH}/sigmob-sdk/build.gradle
  else
    cp ${WIND_BASE_PATH}/build-sigmob-cn.gradle ${WIND_BASE_PATH}/sigmob-sdk//build.gradle
  fi

  SIG_BUILD_VERSION=$(grep "SDK_VERSION" ${WIND_CONSTANTS_PATH} | awk '{match($NF,/"(.+)";/,m);print m[1];}')
  ${WIND_BASE_PATH}/gradlew :sigmob-sdk:clean

  cp ${WIND_BASE_PATH}/sigmob-sdk/src/main/java/com/sigmob/sdk/SigmobFileV4Provider.java.bak ${WIND_BASE_PATH}/sigmob-sdk/src/main/java/com/sigmob/sdk/SigmobFileV4Provider.java

  ${WIND_BASE_PATH}/gradlew :sigmob-sdk:assembleRelease -PbuildVersion=${SIG_BUILD_VERSION}
  if [ $? != 0 ]; then
    echo "gradlew sigmob-sdk assembleRelease error!"
    exit -1
  fi

  ${WIND_BASE_PATH}/gradlew :sigmob-sdk:copyAAR -PbuildVersion=${SIG_BUILD_VERSION}
  if [ $? != 0 ]; then
    echo "gradlew sigmob-sdk copyAAR error!"
    exit -1
  fi

  if [ "${SIG_IS_PUBLISH}" == "true" ]; then
      pushMavenSigmobSDK
  fi

}

function buildSigmobXSDK() {
    changeSDKFolder
    buildSigmobSDK
    repackSigmobAAR
    if [ $? != 0 ]; then
      echo "gradlew repackSigmobAAR error!"
      exit -1
    fi
    cd ${WIND_BASE_PATH}
    ${WIND_BASE_PATH}/gradlew :sigmob-sdk:copyAARX -PbuildVersion=${SIG_BUILD_VERSION}
    if [ $? != 0 ]; then
      echo "gradlew sigmob-sdk copyAARX error!"
      exit -1
    fi
}

function pushMavenSigmobSDK() {


    if [ "${SIG_IS_GOOGLE_PLAY}" == "true" ]; then
        wind_artifactId="wind-sdk-gp"
    fi

    ${WIND_BASE_PATH}/gradlew :sigmob-sdk:uploadArchives -PbuildVersion=${SIG_BUILD_VERSION}${SNAPSHOT} -PartifactId=${wind_artifactId}
    if [ $? != 0 ]; then
      echo "gradlew  pushMavenSigmobSDK error!"
      exit -1
    fi
}


function pushMavenCommonSDK() {

    ${WIND_BASE_PATH}/gradlew :common:uploadArchives -PbuildVersion=${COMMON_VERSION_NAME}${SNAPSHOT}
    if [ $? != 0 ]; then
      echo "gradlew pushMavenCommonSDK error!"
#      exit -1
    fi
}




#########################################################replaceTanx###################################################################

function replaceTanx() {

  echo "-----------------replaceTanx------------"

  rm -rf windAAR windSmali classes.dex

  unzip -o app/libs/rename-windAd-${SIG_BUILD_VERSION}.aar -d windAAR

  rm -rf windAAR/jni

  ${ANDROID_HOME}/build-tools/28.0.3/dx --dex --output=classes.dex windAAR/classes.jar

  java -jar ${CHANGE_TOOL_PATH}/baksmali-2.3.4.jar d classes.dex -o windSmali

  rm -rf windSmali/com/tan

  rm -rf classes.dex

  rm -rf windAAR/classes.jar

  java -jar ${CHANGE_TOOL_PATH}/smali-2.3.4.jar a windSmali -o classes.dex

  sh ${CHANGE_TOOL_PATH}/dex-tools-2.1-SNAPSHOT/d2j-dex2jar.sh classes.dex -o windAAR/classes.jar

  cd windAAR

  zip -q -r rename-windAd-${SIG_BUILD_VERSION}.aar *

  mv -f rename-windAd-${SIG_BUILD_VERSION}.aar ${WIND_BASE_PATH}/WindTwinDemo/app/libs/rename-windAd-${SIG_BUILD_VERSION}.aar

  cd ${WIND_BASE_PATH}/WindTwinDemo

  rm -rf windAAR windSmali classes.dex
}

function buildFramework() {

    rm -rf ${WIND_BASE_PATH}/WindDemo/app/libs/wind?-sdk-*.aar
    if [ "${SIG_NEW_PACKAGENAME}" != "" ]; then
      find ${WIND_BASE_PATH}/WindAd/app/src/main/java/com/sigmob/demo/ -name "*.java" | xargs sed -i "" "s/com.sigmob.windad/com.${SIG_NEW_PACKAGENAME}.windad/g"
      cp ${WIND_BASE_PATH}/output/AAR/Windx-sdk-${SIG_BUILD_VERSION}.aar ${WIND_BASE_PATH}/WindAd/app/libs
      find ${WIND_BASE_PATH}/WindAd/app/src/main/ -name "AndroidManifest.xml" | xargs sed -i "" "s/com.sigmob.sdk.SigmobFileProvider/com.${SIG_NEW_PACKAGENAME}.sdk.SigmobFileProvider/g"
    else
      cp ${WIND_BASE_PATH}/output/AAR/Wind-sdk-${SIG_BUILD_VERSION}.aar ${WIND_BASE_PATH}/WindAd/app/libs
    fi


}

function buildDemoApk() {

    rm -rf ${WIND_BASE_PATH}/WindDemo/app/libs/wind-sdk-*.aar
    rm -rf ${WIND_BASE_PATH}/WindDemo/app/libs/wind-common-*.aar



    if [ "${SIG_IS_GOOGLE_PLAY}" == "true" ]; then
        cp ${WIND_BASE_PATH}/MainActivity.java.gp ${WIND_BASE_PATH}/WindDemo/app/src/main/java/com/wind/demo/MainActivity.jva
    else
        rm -f ${WIND_BASE_PATH}/WindDemo/app/libs/Wind-Consent.aar
    fi

    if [ -n "${SIG_NEW_PACKAGENAME}" ]; then
      find ${WIND_BASE_PATH}/WindDemo/app/src/main/java/com/wind/demo/ -name "*.java" | xargs sed -i "" "s/com.sigmob.windad/com.${SIG_NEW_PACKAGENAME}.windad/g"
      find ${WIND_BASE_PATH}/WindDemo/app/src/main/ -name "AndroidManifest.xml" | xargs sed -i "" "s/com.sigmob.sdk.SigmobFileProvider/com.${SIG_NEW_PACKAGENAME}.sdk.SigmobFileProvider/g"
    fi

    cp ${WIND_BASE_PATH}/output/AAR/wind-sdk-${SIG_BUILD_VERSION}.aar ${WIND_BASE_PATH}/WindDemo/app/libs

    cp ${WIND_BASE_PATH}/output/AAR/wind-common-*.aar ${WIND_BASE_PATH}/WindDemo/app/libs

    cd ${WIND_BASE_PATH}/WindDemo

    chmod 0755 ${WIND_BASE_PATH}/WindDemo/gradlew

    ${WIND_BASE_PATH}/WindDemo/gradlew assembleDebug -PbuildVersion=${SIG_BUILD_VERSION}

    mkdir -p ${APK_RELEASE}

    cp ${WIND_BASE_PATH}/WindDemo/app/build/outputs/apk/debug/app-debug.apk ${APK_RELEASE}

}

function build() {

  git submodule update --init --recursive

  echo " build Type" ${SIG_BUILD_TYPE}

  if [ "${SIG_IS_PUBLISH}" == "true" ]; then
        if [ "${SIG_IS_DEBUG}" == "true" ] || [ "${SIG_CONFIG_URL}" != "${SIGMOB_CONFIG_URL}" ]; then
                echo " IS DEBUG Status "${SIG_IS_DEBUG}
                echo " SIG_CONFIG_URL "${SIG_CONFIG_URL}
                echo " publish  Sigmob SDK error! "
                exit -1
        fi
  fi

  case ${SIG_BUILD_TYPE} in

      2)
        buildCommonSDK
        exit
        ;;
      3)
            setConfigUrl
            buildCommonSDK
            buildSigmobSDK

            rm -rf ${README_OUT_PATH}
            mkdir -p ${WIND_BASE_PATH}/output
            cp -r ${README_PATH} ${README_OUT_PATH}

            if [ "${SIG_NEW_PACKAGENAME}" != "" ]; then
                buildSigmobXSDK
            fi

            if [ "${SIG_IS_APK}" == "true" ]; then
                buildDemoApk
            else
                buildFramework
            fi
            exit
            ;;
      4)
            setConfigUrl
            buildCommonSDK
            buildSigmobSDK

            rm -rf ${README_OUT_PATH}
            mkdir -p ${WIND_BASE_PATH}/output
            cp -r ${README_PATH} ${README_OUT_PATH}

            if [ "${SIG_NEW_PACKAGENAME}" != "" ]; then
                buildSigmobXSDK
            fi
            buildDemoApk
            exit
            ;;
      *)
        exit;;
  esac
}

while getopts ":pgahc:t:n:u:" opt; do
    case $opt in
      h)
          Usage
          exit
          ;;
      c)

        if [ "${OPTARG}" == "Debug" ]; then
         SIG_VERSION=$(git rev-list HEAD --first-parent --count)
         SIG_VERSION=$(expr ${SIG_VERSION} + 100)
         SNAPSHOT=".${SIG_VERSION}-SNAPSHOT"
#         SIG_IS_DEBUG="true"
        fi
        ;;
      u)
        SIG_CONFIG_URL="${OPTARG}"
        ;;
      a)
        SIG_IS_APK="true"
        ;;
      g)
        SIG_IS_GOOGLE_PLAY="true"
        ;;
      p)
        SIG_IS_PUBLISH="true"
        rm -rf ${README_OUT_PATH}
        mkdir -p ${WIND_BASE_PATH}/output
        cp -r ${README_PATH} ${README_OUT_PATH}
      ;;
      t)
         SIG_BUILD_TYPE=$OPTARG
        ;;
      n)
        SIG_NEW_PACKAGENAME="$OPTARG"
        if [ -n ${SIG_NEW_PACKAGENAME} ]; then
            echo "exist "${SIG_NEW_PACKAGENAME}
        fi
        ;;
      *)
        Usage
        exit 0
        ;;
    esac
done
build

set +x
