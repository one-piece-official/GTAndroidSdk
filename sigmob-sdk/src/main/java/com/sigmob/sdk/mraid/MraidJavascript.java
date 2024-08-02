package com.sigmob.sdk.mraid;

class MraidJavascript {
    public static final String JAVASCRIPT_SOURCE = "(function() {\n" +
            "     var isIOS = (/iphone|ipad|ipod/i).test(window.navigator.userAgent.toLowerCase());\n" +
            "     try {\n" +
            "         console.dclog = function(log) {\n" +
            "             if (isIOS) {\n" +
            "                 var iframe = document.createElement('iframe');\n" +
            "                 iframe.setAttribute('src', 'ios-log: ' + log);\n" +
            "                 document.documentElement.appendChild(iframe);\n" +
            "                 iframe.parentNode.removeChild(iframe);\n" +
            "                 iframe = null\n" +
            "             }\n" +
            "             console.log(log)\n" +
            "         }\n" +
            "     } catch (e) {\n" +
            "         console.log(e)\n" +
            "     }\n" +
            " }());\n" +
            "\n" +
            " (function() {\n" +
            "     var xxx = window.xxx = {};\n" +
            "     var bridge = window.bridge = xxx; \n" +
            "     var adRvSettring = {};\n" +
            "     var materialMeta = {};\n" +
            "     var ad = {};\n" +
            "     var os = 0;\n" +
            "     var xxxHandlers = {\n" +
            "         rvSetting: function(val) {\n" +
            "             for (var key in val) {\n" +
            "                 if (val.hasOwnProperty(key)) adRvSettring[key] = val[key]\n" +
            "             }\n" +
            "         },\n" +
            "         osType: function(val) {\n" +
            "             os = val\n" +
            "         },\n" +
            "         video: function(val) {\n" +
            "             var videoObj = val;\n" +
            "             materialMeta.video = videoObj\n" +
            "         },\n" +
            "         material: function(val) {\n" +
            "             var materialObj = val;\n" +
            "             for (var key in materialObj) {\n" +
            "                 if (materialObj.hasOwnProperty(key)) {\n" +
            "                     materialMeta[key] = materialObj[key]\n" +
            "                 }\n" +
            "             }\n" +
            "         },\n" +
            "         ad: function(val) {\n" +
            "             var adObj = val;\n" +
            "             for (var key in adObj) {\n" +
            "                 if (adObj.hasOwnProperty(key)) {\n" +
            "                     ad[key] = adObj[key]\n" +
            "                 }\n" +
            "             }\n" +
            "         },\n" +
            "     };\n" +
            "     xxx.getOs = function() {\n" +
            "         return os\n" +
            "     };\n" +
            "     xxx.loaded = function() {\n" +
            "         return sigandroid.mraidJsLoaded();\n" +
            "     };\n" +
            "     xxx.getRvSetting = function() {\n" +
            "         return adRvSettring\n" +
            "     };\n" +
            "     xxx.getMaterialMeta = function() {\n" +
            "         return materialMeta\n" +
            "     };\n" +
            "     xxx.getAd = function() {\n" +
            "         return ad\n" +
            "     };\n" +
            "     var sendCustomEvent = function() {\n" +
            "         var args = new Array();\n" +
            "         var l = arguments.length;\n" +
            "         for (var i = 0; i < l; i++) {\n" +
            "             var obj = arguments[i];\n" +
            "             if (obj === null) continue;\n" +
            "             if (Array.isArray(obj)) {\n" +
            "                 args = args.concat(obj)\n" +
            "             } else {\n" +
            "                 args.push(obj)\n" +
            "             }\n" +
            "         }\n" +
            "         args.unshift('event');\n" +
            "         args.unshift('smextension');\n" +
            "         executeNativeCall(args)\n" +
            "     };\n" +
            "     var executeNativeCall = function(args) {\n" +
            "         var command = args.shift();\n" +
            "         var call = 'xxx://' + command;\n" +
            "         var key, value;\n" +
            "         var isFirstArgument = true;\n" +
            "         for (var i = 0; i < args.length; i += 2) {\n" +
            "             key = args[i];\n" +
            "             value = args[i + 1];\n" +
            "             if (value === null) continue;\n" +
            "             if (isFirstArgument) {\n" +
            "                 call += '?';\n" +
            "                 isFirstArgument = false\n" +
            "             } else {\n" +
            "                 call += '&'\n" +
            "             }\n" +
            "             call += encodeURIComponent(key) + '=' + encodeURIComponent(value)\n" +
            "         }\n" +
            "         iframeSendSrc(call)\n" +
            "     };\n" +
            "     var iframeSendSrc = function(src) {\n" +
            "         var iframe = document.createElement('iframe');\n" +
            "         iframe.setAttribute('src', src);\n" +
            "         document.documentElement.appendChild(iframe);\n" +
            "         iframe.parentNode.removeChild(iframe);\n" +
            "         iframe = null\n" +
            "     }\n" +
            "\n" +
            "     function callNativeFunc(kwargs, func) {\n" +
            "         if (kwargs === undefined) return undefined;\n" +
            "         if (func === undefined) return undefined;\n" +
            "         if (os === 1) {\n" +
            "             kwargs['func'] = func;\n" +
            "             var returnStr = prompt(JSON.stringify(kwargs));\n" +
            "             return JSON.parse(returnStr)\n" +
            "         } else {\n" +
            "             kwargs['func'] = func;\n" +
            "             var returnStr = sigandroid.func(JSON.stringify(kwargs));\n" +
            "             return JSON.parse(returnStr)\n" +
            "         }\n" +
            "     };\n" +
            "     xxx.getAppInfo = function(kwargs) {\n" +
            "         return callNativeFunc(kwargs, 'getAppInfo:')\n" +
            "     };\n" +
            "     xxx.addDcLog = function(kwargs) {\n" +
            "         return callNativeFunc(kwargs, 'javascriptAddDcLog:')\n" +
            "     };\n" +
            "     xxx.addMacro = function(key, value) {\n" +
            "         var kwargs = {};\n" +
            "         kwargs['key'] = key;\n" +
            "         kwargs['value'] = value;\n" +
            "         return callNativeFunc(kwargs, 'addMacro:')\n" +
            "     };\n" +
            "     xxx.executeVideoAdTracking = function(event) {\n" +
            "         var kwargs = {};\n" +
            "         kwargs['event'] = event;\n" +
            "         return callNativeFunc(kwargs, 'excuteRewardAdTrack:')\n" +
            "     };\n" +
            "     xxx.tracking = function(event, urls) {\n" +
            "         var kwargs = {};\n" +
            "         kwargs.event = event;\n" +
            "         kwargs.urls = urls;\n" +
            "         return callNativeFunc(kwargs, 'tracking:');\n" +
            "     };\n" +
            "     xxx.loadProduct = function(kwargs) {\n" +
            "         if (!kwargs) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'args are required.', 'loadProduct')\n" +
            "         } else if (!kwargs.itunesId || !kwargs.mode) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'itunesId and mode are required.', 'loadProduct')\n" +
            "         } else {\n" +
            "             var mode = kwargs.mode\n" +
            "             if (mode === 'overlay') {\n" +
            "                 broadcastEvent(EVENTS.ERROR, 'overlay does not support preloading', 'loadProduct')\n" +
            "             }\n" +
            "             sendCustomEvent('LoadProduct', 'args', JSON.stringify(kwargs))\n" +
            "         }\n" +
            "     };\n" +
            "     xxx.dissStoreKit = function(mode) {\n" +
            "         if (!mode) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'mode are required.', 'dissStoreKit')\n" +
            "         } else {\n" +
            "             sendCustomEvent('DissStoreKit', 'mode', mode)\n" +
            "         }\n" +
            "     };\n" +
            "     xxx.fireChangeEvent = function(properties) {\n" +
            "         for (var p in properties) {\n" +
            "             if (properties.hasOwnProperty(p)) {\n" +
            "                 var handler = xxxHandlers[p];\n" +
            "                 handler(properties[p])\n" +
            "             }\n" +
            "         }\n" +
            "     };\n" +
            "     var contains = function(value, array) {\n" +
            "         for (var i in array) {\n" +
            "             if (array[i] === value) return true\n" +
            "         }\n" +
            "         return false\n" +
            "     };\n" +
            "     var EVENTS = xxx.EVENTS = {\n" +
            "         ERROR: 'error',\n" +
            "         STOREKIT_READY: 'storekit_ready',\n" +
            "         STOREKIT_DIDFAILTOLOAD: 'storekit_didFailToLoad',\n" +
            "         STOREKIT_PRESENT: 'storekit_present',\n" +
            "         STOREKIT_CLICK: 'storekit_click',\n" +
            "         STOREKIT_FINISH: 'storekit_finish',\n" +
            "         OVERLAY_DIDFAILTOLOAD: 'overlay_didFailToLoad',\n" +
            "         OVERLAY_DIDPRESENT: 'overlay_didPresent',\n" +
            "         OVERLAY_CLICK: 'overlay_click',\n" +
            "         OVERLAY_DIDFINISH: 'overlay_didFinish',\n" +
            "         APK_DOWNLOAD_START: 'apk_download_strat',\n" +
            "         APK_DOWNLOAD_PAUSE: 'apk_download_pause',\n" +
            "         APK_DOWNLOAD_FAIL: 'apk_download_fail',\n" +
            "         APK_DOWNLOAD_END: 'apk_download_end',\n" +
            "         APK_DOWNLOAD_INSTALLED: 'apk_download_installed'\n" +
            "     };\n" +
            "     var listeners = {};\n" +
            "     var EventListeners = function(event) {\n" +
            "         this.event = event;\n" +
            "         this.count = 0;\n" +
            "         var listeners = {};\n" +
            "         this.add = function(func) {\n" +
            "             var id = String(func);\n" +
            "             if (!listeners[id]) {\n" +
            "                 listeners[id] = func;\n" +
            "                 this.count++\n" +
            "             }\n" +
            "         };\n" +
            "         this.remove = function(func) {\n" +
            "             var id = String(func);\n" +
            "             if (listeners[id]) {\n" +
            "                 listeners[id] = null;\n" +
            "                 delete listeners[id];\n" +
            "                 this.count--;\n" +
            "                 return true\n" +
            "             } else {\n" +
            "                 return false\n" +
            "             }\n" +
            "         };\n" +
            "         this.removeAll = function() {\n" +
            "             for (var id in listeners) {\n" +
            "                 if (listeners.hasOwnProperty(id)) this.remove(listeners[id])\n" +
            "             }\n" +
            "         };\n" +
            "         this.broadcast = function(args) {\n" +
            "             for (var id in listeners) {\n" +
            "                 if (listeners.hasOwnProperty(id)) listeners[id].apply(mraid, args)\n" +
            "             }\n" +
            "         };\n" +
            "         this.toString = function() {\n" +
            "             var out = [event, ':'];\n" +
            "             for (var id in listeners) {\n" +
            "                 if (listeners.hasOwnProperty(id)) out.push('|', id, '|')\n" +
            "             }\n" +
            "             return out.join('')\n" +
            "         }\n" +
            "     };\n" +
            "     var broadcastEvent = function() {\n" +
            "         var args = new Array(arguments.length);\n" +
            "         var l = arguments.length;\n" +
            "         for (var i = 0; i < l; i++) args[i] = arguments[i];\n" +
            "         var event = args.shift();\n" +
            "         if (listeners[event]) listeners[event].broadcast(args)\n" +
            "     };\n" +
            "     xxx.addEventListener = function(event, listener) {\n" +
            "         if (!event || !listener) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'Both event and listener are required.', 'addEventListener')\n" +
            "         } else {\n" +
            "             if (!listeners[event]) {\n" +
            "                 listeners[event] = new EventListeners(event)\n" +
            "             }\n" +
            "             listeners[event].add(listener)\n" +
            "         }\n" +
            "     };\n" +
            "     xxx.removeEventListener = function(event, listener) {\n" +
            "         if (!event) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'Event is required.', 'removeEventListener');\n" +
            "             return\n" +
            "         }\n" +
            "         if (listener) {\n" +
            "             var success = false;\n" +
            "             if (listeners[event]) {\n" +
            "                 success = listeners[event].remove(listener)\n" +
            "             }\n" +
            "             if (!success) {\n" +
            "                 broadcastEvent(EVENTS.ERROR, 'Listener not currently registered for event.', 'removeEventListener');\n" +
            "                 return\n" +
            "             }\n" +
            "         } else if (!listener && listeners[event]) {\n" +
            "             listeners[event].removeAll()\n" +
            "         }\n" +
            "         if (listeners[event] && listeners[event].count === 0) {\n" +
            "             listeners[event] = null;\n" +
            "             delete listeners[event]\n" +
            "         }\n" +
            "     };\n" +
            "     xxx.notifyStorekitReadyEvent = function(itunesId) {\n" +
            "         broadcastEvent(EVENTS.STOREKIT_READY, itunesId)\n" +
            "     };\n" +
            "     xxx.notifyStorekitDidFailToLoadEvent = function(itunesId, error) {\n" +
            "         broadcastEvent(EVENTS.STOREKIT_DIDFAILTOLOAD, itunesId, error)\n" +
            "     };\n" +
            "     xxx.notifyStorekitClickEvent = function(itunesId) {\n" +
            "         broadcastEvent(EVENTS.STOREKIT_CLICK, itunesId)\n" +
            "     };\n" +
            "     xxx.notifyStorekitPresentEvent = function(itunesId) {\n" +
            "         broadcastEvent(EVENTS.STOREKIT_PRESENT, itunesId)\n" +
            "     };\n" +
            "     xxx.notifyStorekitFinishEvent = function(itunesId) {\n" +
            "         broadcastEvent(EVENTS.STOREKIT_FINISH, itunesId)\n" +
            "     };\n" +
            "     xxx.notifyOverlayDidFailToLoadEvent = function(itunesId, error) {\n" +
            "         broadcastEvent(EVENTS.OVERLAY_DIDFAILTOLOAD, itunesId, error)\n" +
            "     };\n" +
            "     xxx.notifyOverlayPresentEvent = function(itunesId) {\n" +
            "         broadcastEvent(EVENTS.OVERLAY_DIDPRESENT, itunesId)\n" +
            "     };\n" +
            "     xxx.notifyOverlayClickEvent = function(itunesId) {\n" +
            "         broadcastEvent(EVENTS.OVERLAY_CLICK, itunesId)\n" +
            "     };\n" +
            "     xxx.notifyOverlayFinishEvent = function(itunesId) {\n" +
            "         broadcastEvent(EVENTS.OVERLAY_DIDFINISH, itunesId)\n" +
            "     };\n" +
            "     xxx.notifyApkDownloadStartEvent = function() {\n" +
            "        broadcastEvent(EVENTS.APK_DOWNLOAD_START)\n" +
            "    };\n" +
            "     xxx.notifyApkDownloadPauseEvent = function() {\n" +
            "        broadcastEvent(EVENTS.APK_DOWNLOAD_PAUSE)\n" +
            "    };\n" +
            "    xxx.notifyApkDownloadFailEvent = function() {\n" +
            "        broadcastEvent(EVENTS.APK_DOWNLOAD_FAIL)\n" +
            "    };\n" +
            "    xxx.notifyApkDownloadEndEvent = function() {\n" +
            "        broadcastEvent(EVENTS.APK_DOWNLOAD_END)\n" +
            "    };\n" +
            "    xxx.notifyApkDownloadInstalledEvent = function() {\n" +
            "        broadcastEvent(EVENTS.APK_DOWNLOAD_INSTALLED)\n" +
            "    }\n" +
            " }());\n" +
            "\n" +
            " (function() {\n" +
            "     var mraid = window.mraid = {};\n" +
            "     window.MRAID_ENV = {\n" +
            "         version: '',\n" +
            "         sdk: '',\n" +
            "         sdkVersion: '',\n" +
            "         appId: '',\n" +
            "         ifa: '',\n" +
            "         limitAdTracking: '',\n" +
            "         coppa: ''\n" +
            "     };\n" +
            "     var bridge = window.mraidbridge = {\n" +
            "         nativeSDKFiredReady: false,\n" +
            "         nativeCallQueue: [],\n" +
            "         nativeCallInFlight: false,\n" +
            "         lastSizeChangeProperties: null,\n" +
            "         nativeCallQueueV2: [],\n" +
            "         nativeCallInFlightV2: false,\n" +
            "         vpaidQueue: {},\n" +
            "         queue: {}\n" +
            "     };\n" +
            "     bridge.fireChangeEvent = function(properties) {\n" +
            "         for (var p in properties) {\n" +
            "             if (properties.hasOwnProperty(p)) {\n" +
            "                 var handler = changeHandlers[p];\n" +
            "                 handler(properties[p])\n" +
            "             }\n" +
            "         }\n" +
            "     };\n" +
            "     bridge.nativeCallComplete = function(command) {\n" +
            "         console.dclog('nativeCallCompletecommand = ' + command)\n" +
            "         if (this.nativeCallQueue.length === 0) {\n" +
            "             this.nativeCallInFlight = false;\n" +
            "             return\n" +
            "         }\n" +
            "         var nextCall = this.nativeCallQueue.pop();\n" +
            "         window.location.href = nextCall\n" +
            "     };\n" +
            "     bridge.executeNativeCall = function(args) {\n" +
            "         var command = args.shift();\n" +
            "         if (!this.nativeSDKFiredReady) {\n" +
            "             console.dclog('rejecting ' + command + ' because mraid is not ready');\n" +
            "             bridge.notifyErrorEvent('mraid is not ready', command);\n" +
            "             return\n" +
            "         }\n" +
            "         var call = 'mraid://' + command;\n" +
            "         var key, value;\n" +
            "         var isFirstArgument = true;\n" +
            "         for (var i = 0; i < args.length; i += 2) {\n" +
            "             key = args[i];\n" +
            "             value = args[i + 1];\n" +
            "             if (value === null) continue;\n" +
            "             if (isFirstArgument) {\n" +
            "                 call += '?';\n" +
            "                 isFirstArgument = false\n" +
            "             } else {\n" +
            "                 call += '&'\n" +
            "             }\n" +
            "             call += encodeURIComponent(key) + '=' + encodeURIComponent(value)\n" +
            "         }\n" +
            "         if (this.nativeCallInFlight) {\n" +
            "             this.nativeCallQueue.push(call)\n" +
            "         } else {\n" +
            "             this.nativeCallInFlight = true;\n" +
            "             window.location = call\n" +
            "         }\n" +
            "     };\n" +
            "     bridge.setCurrentPosition = function(x, y, width, height) {\n" +
            "         currentPosition = {\n" +
            "             x: x,\n" +
            "             y: y,\n" +
            "             width: width,\n" +
            "             height: height\n" +
            "         };\n" +
            "         broadcastEvent(EVENTS.INFO, 'Set current position to ' + stringify(currentPosition))\n" +
            "     };\n" +
            "     bridge.setDefaultPosition = function(x, y, width, height) {\n" +
            "         defaultPosition = {\n" +
            "             x: x,\n" +
            "             y: y,\n" +
            "             width: width,\n" +
            "             height: height\n" +
            "         };\n" +
            "         broadcastEvent(EVENTS.INFO, 'Set default position to ' + stringify(defaultPosition))\n" +
            "     };\n" +
            "     bridge.setLocation = function(lat, lon, type) {\n" +
            "         location = {\n" +
            "             lat: lat,\n" +
            "             lon: lon,\n" +
            "             type: type\n" +
            "         };\n" +
            "         broadcastEvent(EVENTS.INFO, 'Set location to ' + stringify(location))\n" +
            "     };\n" +
            "     bridge.setMaxSize = function(width, height) {\n" +
            "         maxSize = {\n" +
            "             width: width,\n" +
            "             height: height\n" +
            "         };\n" +
            "         expandProperties.width = width;\n" +
            "         expandProperties.height = height;\n" +
            "         broadcastEvent(EVENTS.INFO, 'Set max size to ' + stringify(maxSize))\n" +
            "     };\n" +
            "     bridge.setPlacementType = function(_placementType) {\n" +
            "         placementType = _placementType;\n" +
            "         broadcastEvent(EVENTS.INFO, 'Set placement type to ' + stringify(placementType))\n" +
            "     };\n" +
            "     bridge.setScreenSize = function(width, height) {\n" +
            "         screenSize = {\n" +
            "             width: width,\n" +
            "             height: height\n" +
            "         };\n" +
            "         broadcastEvent(EVENTS.INFO, 'Set screen size to ' + stringify(screenSize))\n" +
            "     };\n" +
            "     bridge.setState = function(_state) {\n" +
            "         state = _state;\n" +
            "         broadcastEvent(EVENTS.INFO, 'Set state to ' + stringify(state));\n" +
            "         broadcastEvent(EVENTS.STATECHANGE, state)\n" +
            "     };\n" +
            "     bridge.setIsViewable = function(_isViewable) {\n" +
            "         isViewable = _isViewable;\n" +
            "         broadcastEvent(EVENTS.INFO, 'Set isViewable to ' + stringify(isViewable));\n" +
            "         broadcastEvent(EVENTS.VIEWABLECHANGE, isViewable)\n" +
            "     };\n" +
            "     bridge.setSupports = function(sms, tel, calendar, storePicture, inlineVideo, vpaid, location) {\n" +
            "         supportProperties = {\n" +
            "             sms: sms,\n" +
            "             tel: tel,\n" +
            "             calendar: calendar,\n" +
            "             storePicture: storePicture,\n" +
            "             inlineVideo: inlineVideo,\n" +
            "             vpaid: vpaid,\n" +
            "             location: location\n" +
            "         }\n" +
            "     };\n" +
            "     bridge.notifyReadyEvent = function() {\n" +
            "         this.nativeSDKFiredReady = true;\n" +
            "         broadcastEvent(EVENTS.READY)\n" +
            "     };\n" +
            "     bridge.notifyErrorEvent = function(message, action) {\n" +
            "         broadcastEvent(EVENTS.ERROR, message, action)\n" +
            "     };\n" +
            "     bridge.fireReadyEvent = bridge.notifyReadyEvent;\n" +
            "     bridge.fireErrorEvent = bridge.notifyErrorEvent;\n" +
            "     bridge.notifySizeChangeEvent = function(width, height) {\n" +
            "         if (this.lastSizeChangeProperties && width == this.lastSizeChangeProperties.width && height == this.lastSizeChangeProperties.height) {\n" +
            "         }\n" +
            "         this.lastSizeChangeProperties = {\n" +
            "             width: width,\n" +
            "             height: height\n" +
            "         };\n" +
            "         broadcastEvent(EVENTS.SIZECHANGE, width, height)\n" +
            "     };\n" +
            "     bridge.notifyStateChangeEvent = function() {\n" +
            "         if (state === STATES.LOADING) {\n" +
            "             broadcastEvent(EVENTS.INFO, 'Native SDK initialized.')\n" +
            "         }\n" +
            "         broadcastEvent(EVENTS.INFO, 'Set state to ' + stringify(state));\n" +
            "         broadcastEvent(EVENTS.STATECHANGE, state)\n" +
            "     };\n" +
            "     bridge.notifyViewableChangeEvent = function() {\n" +
            "         broadcastEvent(EVENTS.INFO, 'Set isViewable to ' + stringify(isViewable));\n" +
            "         broadcastEvent(EVENTS.VIEWABLECHANGE, isViewable)\n" +
            "     };\n" +
            "     var VERSION = mraid.VERSION = '3.0';\n" +
            "     var isIOS = (/iphone|ipad|ipod/i).test(window.navigator.userAgent.toLowerCase());\n" +
            "     var STATES = mraid.STATES = {\n" +
            "         LOADING: 'loading',\n" +
            "         DEFAULT: 'default',\n" +
            "         EXPANDED: 'expanded',\n" +
            "         HIDDEN: 'hidden',\n" +
            "         RESIZED: 'resized'\n" +
            "     };\n" +
            "     var EVENTS = mraid.EVENTS = {\n" +
            "         ERROR: 'error',\n" +
            "         INFO: 'info',\n" +
            "         READY: 'ready',\n" +
            "         STATECHANGE: 'stateChange',\n" +
            "         VIEWABLECHANGE: 'viewableChange',\n" +
            "         SIZECHANGE: 'sizeChange',\n" +
            "         VOLUMECHANGE: 'audioVolumeChange',\n" +
            "         EXPOSURECHANGE: 'exposureChange',\n" +
            "         error: 'error',\n" +
            "         info: 'info',\n" +
            "         ready: 'ready',\n" +
            "         playstatechanged: 'playStateChanged',\n" +
            "         loadStateChanged: 'loadStateChanged',\n" +
            "         currentTime: 'currentTime',\n" +
            "         playEnd: 'playEnd'\n" +
            "     };\n" +
            "     var PLACEMENT_TYPES = mraid.PLACEMENT_TYPES = {\n" +
            "         UNKNOWN: 'unknown',\n" +
            "         INLINE: 'inline',\n" +
            "         INTERSTITIAL: 'interstitial'\n" +
            "     };\n" +
            "     var VPAID_EVENTS = mraid.VPAID_EVENTS = {\n" +
            "         AD_CLICKED: 'AdClickThru',\n" +
            "         AD_ERROR: 'AdError',\n" +
            "         AD_IMPRESSION: 'AdImpression',\n" +
            "         AD_PAUSED: 'AdPaused',\n" +
            "         AD_PLAYING: 'AdPlaying',\n" +
            "         AD_VIDEO_COMPLETE: 'AdVideoComplete',\n" +
            "         AD_VIDEO_FIRST_QUARTILE: 'AdVideoFirstQuartile',\n" +
            "         AD_VIDEO_MIDPOINT: 'AdVideoMidpoint',\n" +
            "         AD_VIDEO_THIRD_QUARTILE: 'AdVideoThirdQuartile',\n" +
            "         AD_VIDEO_START: 'AdVideoStart'\n" +
            "     };\n" +
            "     var MRAID_CUSTOM_EVENTS = mraid.MRAID_CUSTOM_EVENTS = {\n" +
            "         AD_VIDEO_DOM_RECT: 'AdVideoDomRect',\n" +
            "         AD_SKIP_AD: 'skipAd',\n" +
            "         AD_REWARD_AD: 'reward',\n" +
            "         AD_VIDEO_VOICE: 'voice',\n" +
            "         AD_SKIP_SHOW_TIME: 'showSkipTime',\n" +
            "         AD_COMPANION_CLICK: 'companionClick',\n" +
            "         AD_ENDCARD_SHOW: 'endcardShow',\n" +
            "         AD_APKMONITOR: 'apkMonitor',\n" +
            "     };\n" +
            "     var vpaid_handlers = {\n" +
            "         AdClickThru: function(url, id, playerHandles) {\n" +
            "             var args = ['url', url, 'id', id, 'playerHandles', playerHandles];\n" +
            "             sendVpaidEvent(VPAID_EVENTS.AD_CLICKED, args)\n" +
            "         },\n" +
            "         AdError: function() {\n" +
            "             sendVpaidEvent(VPAID_EVENTS.AD_ERROR)\n" +
            "         },\n" +
            "         AdImpression: function() {\n" +
            "             sendVpaidEvent(VPAID_EVENTS.AD_IMPRESSION)\n" +
            "         },\n" +
            "         AdPaused: function() {\n" +
            "             sendVpaidEvent(VPAID_EVENTS.AD_PAUSED)\n" +
            "         },\n" +
            "         AdPlaying: function() {\n" +
            "             sendVpaidEvent(VPAID_EVENTS.AD_PLAYING)\n" +
            "         },\n" +
            "         AdVideoComplete: function() {\n" +
            "             sendVpaidEvent(VPAID_EVENTS.AD_VIDEO_COMPLETE)\n" +
            "         },\n" +
            "         AdVideoFirstQuartile: function() {\n" +
            "             sendVpaidEvent(VPAID_EVENTS.AD_VIDEO_FIRST_QUARTILE)\n" +
            "         },\n" +
            "         AdVideoMidpoint: function() {\n" +
            "             sendVpaidEvent(VPAID_EVENTS.AD_VIDEO_MIDPOINT)\n" +
            "         },\n" +
            "         AdVideoThirdQuartile: function() {\n" +
            "             sendVpaidEvent(VPAID_EVENTS.AD_VIDEO_THIRD_QUARTILE)\n" +
            "         },\n" +
            "         AdVideoStart: function() {\n" +
            "             sendVpaidEvent(VPAID_EVENTS.AD_VIDEO_START)\n" +
            "         }\n" +
            "     }\n" +
            "     var expandProperties = {\n" +
            "         width: false,\n" +
            "         height: false,\n" +
            "         useCustomClose: false,\n" +
            "         isModal: true\n" +
            "     };\n" +
            "     var resizeProperties = {\n" +
            "         width: 0,\n" +
            "         height: 0,\n" +
            "         offsetX: 0,\n" +
            "         offsetY: 0,\n" +
            "         customClosePosition: 'top-right',\n" +
            "         allowOffscreen: true\n" +
            "     };\n" +
            "     var orientationProperties = {\n" +
            "         allowOrientationChange: true,\n" +
            "         forceOrientation: \"none\"\n" +
            "     };\n" +
            "     var currentAppOrientation = {\n" +
            "         orientation: 'none',\n" +
            "         locked: true\n" +
            "     };\n" +
            "     if (isIOS) {\n" +
            "         orientationProperties.allowOrientationChange = false\n" +
            "     }\n" +
            "     var supportProperties = {\n" +
            "         sms: false,\n" +
            "         tel: false,\n" +
            "         calendar: false,\n" +
            "         storePicture: false,\n" +
            "         inlineVideo: false,\n" +
            "         vpaid: true,\n" +
            "         location: false\n" +
            "     };\n" +
            "     var lastSizeChangeProperties;\n" +
            "     var maxSize = {};\n" +
            "     var currentPosition = {};\n" +
            "     var defaultPosition = {};\n" +
            "     var location = {};\n" +
            "     var screenSize = {};\n" +
            "     var hasSetCustomClose = false;\n" +
            "     var listeners = {};\n" +
            "     var state = STATES.LOADING;\n" +
            "     var isViewable = false;\n" +
            "     var placementType = PLACEMENT_TYPES.UNKNOWN;\n" +
            "     var hostSDKVersion = {\n" +
            "         'major': 0,\n" +
            "         'minor': 0,\n" +
            "         'patch': 0\n" +
            "     };\n" +
            "     var uniqueId = 1;\n" +
            "     var EventListeners = function(event) {\n" +
            "         this.event = event;\n" +
            "         this.count = 0;\n" +
            "         var listeners = {};\n" +
            "         this.add = function(func) {\n" +
            "             var id = String(func);\n" +
            "             if (!listeners[id]) {\n" +
            "                 listeners[id] = func;\n" +
            "                 this.count++\n" +
            "             }\n" +
            "         };\n" +
            "         this.remove = function(func) {\n" +
            "             var id = String(func);\n" +
            "             if (listeners[id]) {\n" +
            "                 listeners[id] = null;\n" +
            "                 delete listeners[id];\n" +
            "                 this.count--;\n" +
            "                 return true\n" +
            "             } else {\n" +
            "                 return false\n" +
            "             }\n" +
            "         };\n" +
            "         this.removeAll = function() {\n" +
            "             for (var id in listeners) {\n" +
            "                 if (listeners.hasOwnProperty(id)) this.remove(listeners[id])\n" +
            "             }\n" +
            "         };\n" +
            "         this.broadcast = function(args) {\n" +
            "             for (var id in listeners) {\n" +
            "                 if (listeners.hasOwnProperty(id)) listeners[id].apply(mraid, args)\n" +
            "             }\n" +
            "         };\n" +
            "         this.toString = function() {\n" +
            "             var out = [event, ':'];\n" +
            "             for (var id in listeners) {\n" +
            "                 if (listeners.hasOwnProperty(id)) out.push('|', id, '|')\n" +
            "             }\n" +
            "             return out.join('')\n" +
            "         }\n" +
            "     };\n" +
            "     var broadcastEvent = function() {\n" +
            "         var args = new Array(arguments.length);\n" +
            "         var l = arguments.length;\n" +
            "         for (var i = 0; i < l; i++) args[i] = arguments[i];\n" +
            "         var event = args.shift();\n" +
            "         if (listeners[event]) listeners[event].broadcast(args)\n" +
            "     };\n" +
            "     var sendVpaidEvent = function() {\n" +
            "         var args = new Array();\n" +
            "         var l = arguments.length;\n" +
            "         for (var i = 0; i < l; i++) {\n" +
            "             var obj = arguments[i];\n" +
            "             if (obj === null) continue;\n" +
            "             if (Array.isArray(obj)) {\n" +
            "                 args = args.concat(obj)\n" +
            "             } else {\n" +
            "                 args.push(obj)\n" +
            "             }\n" +
            "         }\n" +
            "         args.unshift('event');\n" +
            "         args.unshift('vpaid');\n" +
            "         bridge.executeNativeCall(args)\n" +
            "     };\n" +
            "     var sendCustomEvent = function() {\n" +
            "         var args = new Array();\n" +
            "         var l = arguments.length;\n" +
            "         for (var i = 0; i < l; i++) {\n" +
            "             var obj = arguments[i];\n" +
            "             if (obj === null) continue;\n" +
            "             if (Array.isArray(obj)) {\n" +
            "                 args = args.concat(obj)\n" +
            "             } else {\n" +
            "                 args.push(obj)\n" +
            "             }\n" +
            "         }\n" +
            "         args.unshift('event');\n" +
            "         args.unshift('extension');\n" +
            "         bridge.executeNativeCall(args)\n" +
            "     };\n" +
            "     var contains = function(value, array) {\n" +
            "         for (var i in array) {\n" +
            "             if (array[i] === value) return true\n" +
            "         }\n" +
            "         return false\n" +
            "     };\n" +
            "     var clone = function(obj) {\n" +
            "         if (obj === null) return null;\n" +
            "         var f = function() {};\n" +
            "         f.prototype = obj;\n" +
            "         return new f()\n" +
            "     };\n" +
            "     var stringify = function(obj) {\n" +
            "         if (typeof obj === 'object') {\n" +
            "             var out = [];\n" +
            "             if (obj.push) {\n" +
            "                 for (var p in obj) out.push(obj[p]);\n" +
            "                 return '[' + out.join(',') + ']'\n" +
            "             } else {\n" +
            "                 for (var p in obj) out.push(\"'\" + p + \"': \" + obj[p]);\n" +
            "                 return '{' + out.join(',') + '}'\n" +
            "             }\n" +
            "         } else return String(obj)\n" +
            "     };\n" +
            "     var trim = function(str) {\n" +
            "         return str.replace(/^\\s+|\\s+$/g, '')\n" +
            "     };\n" +
            "     var validate = function(obj, validators, action, merge) {\n" +
            "         if (!merge) {\n" +
            "             if (obj === null) {\n" +
            "                 broadcastEvent(EVENTS.ERROR, 'Required object not provided.', action);\n" +
            "                 return false\n" +
            "             } else {\n" +
            "                 for (var i in validators) {\n" +
            "                     if (validators.hasOwnProperty(i) && obj[i] === undefined) {\n" +
            "                         broadcastEvent(EVENTS.ERROR, 'Object is missing required property: ' + i, action);\n" +
            "                         return false\n" +
            "                     }\n" +
            "                 }\n" +
            "             }\n" +
            "         }\n" +
            "         for (var prop in obj) {\n" +
            "             var validator = validators[prop];\n" +
            "             var value = obj[prop];\n" +
            "             if (validator && !validator(value)) {\n" +
            "                 broadcastEvent(EVENTS.ERROR, 'Value of property ' + prop + ' is invalid: ' + value, action);\n" +
            "                 return false\n" +
            "             }\n" +
            "         }\n" +
            "         return true\n" +
            "     };\n" +
            "     var expandPropertyValidators = {\n" +
            "         useCustomClose: function(v) {\n" +
            "             return (typeof v === 'boolean')\n" +
            "         },\n" +
            "     };\n" +
            "    bridge.postMessage = function(msg) {\n" +
            "        var msgStr = JSON.stringify(msg);\n" +
            "        window.sigandroid.postMessage(msgStr);\n" +
            "    }\n" +
            "    bridge.syncMessage = function(msg) {\n" +
            "        if (this.nativeCallInFlightV2) {\n" +
            "            this.nativeCallQueueV2.push(msg)\n" +
            "        } else {\n" +
            "            this.nativeCallInFlightV2 = true;\n" +
            "            var msgStr = JSON.stringify(msg);\n" +
            "            window.sigandroid.postMessage(msgStr);\n" +
            "        }\n" +
            "    }\n" +
            "    var publishEvent = function() {\n" +
            "        var args = new Array(arguments.length);\n" +
            "        var l = arguments.length;\n" +
            "        for (var i = 0; i < l; i++) args[i] = arguments[i];\n" +
            "        var handlers = args.shift();\n" +
            "        var event = args.shift();\n" +
            "        if (handlers[event]) {\n" +
            "            handlers[event].broadcast(args)\n" +
            "        }\n" +
            "    };\n" +
            "    bridge.setvdReadyToPlay = function(val){\n" +
            "        let vpaid = bridge.vpaidQueue[val.uniqueId]\n" +
            "        publishEvent(vpaid.handlers, EVENTS.ready, val.duration, val.width, val.height);\n" +
            "    };\n" +
            "    bridge.setvdPlayStateChanged = function(val){\n" +
            "        let vpaid = bridge.vpaidQueue[val.uniqueId]\n" +
            "        publishEvent(vpaid.handlers, EVENTS.playStateChanged, val.state);\n" +
            "    };\n" +
            "    bridge.setvdLoadStateChanged = function(val) {\n" +
            "        let vpaid = bridge.vpaidQueue[val.uniqueId]\n" +
            "        publishEvent(vpaid.handlers, EVENTS.loadStateChanged, val.state);\n" +
            "    };\n" +
            "    bridge.setvdPlayCurrentTime = function(val) {\n" +
            "        let vpaid = bridge.vpaidQueue[val.uniqueId]\n" +
            "        publishEvent(vpaid.handlers, EVENTS.currentTime, val.currentTime, val.duration);\n" +
            "    };\n" +
            "    bridge.setvdPlayToEnd = function(val) {\n" +
            "        let vpaid = bridge.vpaidQueue[val.uniqueId]\n" +
            "        publishEvent(vpaid.handlers, EVENTS.playEnd, val.currentTime);\n" +
            "    };\n" +
            "    bridge.setvdPlayError = function(val) {\n" +
            "        let vpaid = bridge.vpaidQueue[val.uniqueId]\n" +
            "        publishEvent(vpaid.handlers, EVENTS.error, val.error);\n" +
            "    };\n" +
            "   bridge.onChangeFired = function (val) {\n" +
            "        let obj = bridge.queue[val.uniqueId];\n" +
            "        publishEvent(obj.handlers, val.event, val.args);\n" +
            "    };\n" +
            "    bridge.onChangeEvent = function (val) {\n" +
            "        broadcastEvent(val.event, val.args);\n" +
            "    };\n" +
            "    bridge.onMotionChanged = function (val) {\n" +
            "        const key = 'motion_' + val.type+val.event;\n" +
            "        delete val.type;\n" +
            "        delete val.event;\n" +
            "        publishEvent(listeners, key, val);\n" +
            "    };\n" +
            "    bridge.nativeCallCompleteV2 = function(command) {\n" +
            "        console.log('nativeCallCompletecommandV2 = ' + command)\n" +
            "        if (this.nativeCallQueueV2.length === 0) {\n" +
            "            this.nativeCallInFlightV2 = false;\n" +
            "            return\n" +
            "        }\n" +
            "        var nextCall = this.nativeCallQueueV2.shift();\n" +
            "        bridge.postMessage(nextCall);\n" +
            "     };\n" +
            "    var addEventListener = function(handlers, event, listener) {\n" +
            "        if (!event || !listener) {\n" +
            "            broadcastEvent(EVENTS.error, 'Both event and listener are required.', 'addEventListener')\n" +
            "        } else {\n" +
            "            if (!handlers[event]) {\n" +
            "                handlers[event] = new EventListeners(event)\n" +
            "            }\n" +
            "            handlers[event].add(listener)\n" +
            "        }\n" +
            "    };\n" +
            "    var removeEventListener = function(funs, event, listener) {\n" +
            "        if (!event) {\n" +
            "            broadcastEvent(EVENTS.error, 'Event is required.', 'removeEventListener');\n" +
            "            return\n" +
            "        }\n" +
            "        if (listener) {\n" +
            "            var success = false;\n" +
            "            if (handlers[event]) {\n" +
            "                success = funs[event].remove(listener)\n" +
            "            }\n" +
            "            if (!success) {\n" +
            "                broadcastEvent(EVENTS.error, 'Listener not currently registered for event.', 'removeEventListener');\n" +
            "                return\n" +
            "            }\n" +
            "        } else if (funs && funs[event]) {\n" +
            "            console.log('removeAll -- ' + event);\n" +
            "            funs[event].removeAll()\n" +
            "        }\n" +
            "        if (funs[event] && funs[event].count === 0) {\n" +
            "            funs[event] = null;\n" +
            "            delete funs[event]\n" +
            "        }\n" +
            "    };\n" +
            "    var strFromRect = function(x,y,w,h) {return '{' + x + ','+ y + ','+ w + ','+ h + '}';}\n" +
            "    var strFromPoint = function(x, y) {return '{' + x + ','+ y + '}';}\n" +
            "    bridge.fireReadyEvent = function() {broadcastEvent(EVENTS.ready)};\n" +
            "    bridge.frame = function(event, uniqId, x, y, w, h) {\n" +
            "        if(!w || !h) {\n" +
            "            broadcastEvent(EVENTS.error, 'x,y,w,h is required!', 'frame');\n" +
            "        }else {\n" +
            "            bridge.syncMessage({event: event, subEvent: 'frame', args: {uniqueId: uniqId, frame: {x: x, y: y, w: w, h: h}}});" +
            "        }\n" +
            "    };\n" +
            "     var changeHandlers = {\n" +
            "         onChangeEvent: bridge.onChangeEvent,\n" +
            "         state: function(val) {\n" +
            "             if (state === STATES.LOADING) {\n" +
            "                 broadcastEvent(EVENTS.INFO, 'Native SDK initialized.')\n" +
            "             }\n" +
            "             state = val;\n" +
            "             broadcastEvent(EVENTS.INFO, 'Set state to ' + stringify(val));\n" +
            "             broadcastEvent(EVENTS.STATECHANGE, state)\n" +
            "         },\n" +
            "         exposureChange: function(val) {\n" +
            "             console.dclog('mraid.js exposureChange');\n" +
            "             if (val.hasOwnProperty('exposedPercentage')) {\n" +
            "                 var exposedPercentage = val['exposedPercentage']\n" +
            "             }\n" +
            "             if (val.hasOwnProperty('visibleRectangle')) {\n" +
            "                 var visibleRectangle = val['visibleRectangle']\n" +
            "             }\n" +
            "             if (val.hasOwnProperty('occlusionRectangles')) {\n" +
            "                 var occlusionRectangles = val['occlusionRectangles']\n" +
            "             }\n" +
            "             broadcastEvent(EVENTS.EXPOSURECHANGE, exposedPercentage, visibleRectangle, occlusionRectangles)\n" +
            "         },\n" +
            "         viewable: function(val) {\n" +
            "             isViewable = val;\n" +
            "             broadcastEvent(EVENTS.INFO, 'Set isViewable to ' + stringify(val));\n" +
            "             broadcastEvent(EVENTS.VIEWABLECHANGE, isViewable)\n" +
            "         },\n" +
            "         placementType: function(val) {\n" +
            "             placementType = val\n" +
            "             broadcastEvent(EVENTS.INFO, 'Set placementType to ' + stringify(val));\n" +
            "         },\n" +
            "         sizeChange: function(val) {\n" +
            "             for (var key in val) {\n" +
            "                 if (val.hasOwnProperty(key)) screenSize[key] = val[key]\n" +
            "             }\n" +
            "             broadcastEvent(EVENTS.INFO, 'Set screenSize to ' + stringify(val));\n" +
            "         },\n" +
            "         supports: function(val) {\n" +
            "             supportProperties = val\n" +
            "             broadcastEvent(EVENTS.INFO, 'Set supports to ' + stringify(val));\n" +
            "         },\n" +
            "         env: function(val) {\n" +
            "             MRAID_ENV = val\n" +
            "             broadcastEvent(EVENTS.INFO, 'Set MRAID_ENV to ' + stringify(val));\n" +
            "         },\n" +
            "         location: function(val) {\n" +
            "             location = val\n" +
            "             broadcastEvent(EVENTS.INFO, 'Set location to ' + stringify(val));\n" +
            "         },\n" +
            "         appOrientation: function(val) {\n" +
            "             currentAppOrientation = val\n" +
            "             broadcastEvent(EVENTS.INFO, 'Set appOrientation to ' + stringify(val));\n" +
            "         },\n" +
            "         hostSDKVersion: function(val) {\n" +
            "             var versions = val.split('.').map(function(version) {\n" +
            "                 return parseInt(version, 10)\n" +
            "             }).filter(function(version) {\n" +
            "                 return version >= 0\n" +
            "             });\n" +
            "             if (versions.length >= 3) {\n" +
            "                 hostSDKVersion['major'] = parseInt(versions[0], 10);\n" +
            "                 hostSDKVersion['minor'] = parseInt(versions[1], 10);\n" +
            "                 hostSDKVersion['patch'] = parseInt(versions[2], 10);\n" +
            "                 broadcastEvent(EVENTS.INFO, 'Set hostSDKVersion to ' + stringify(hostSDKVersion))\n" +
            "             }\n" +
            "         },\n" +
            "        motionChanged: bridge.onMotionChanged,\n" +
            "        onChangeFired: bridge.onChangeFired,\n" +
            "     };\n" +
            "    mraid.belowSubview = function(val) {bridge.syncMessage({event: 'belowSubview', args: {uniqueId: val.uniqId}});};\n" +
            "    mraid.addSubview = function (val) { bridge.syncMessage({ event: 'addSubview', args: { uniqueId: val.uniqId } }); };\n" +
            "    mraid.feedback = function() {\n" +
            "        bridge.executeNativeCall(['feedback']);\n" +
            "    };\n" +
            "    mraid.MotionView = function (type) {\n" +
            "        this.uniqId = 'motion_view_' + (uniqueId++) + '_' + new Date().getTime();\n" +
            "        this.event = 'motionView';\n" +
            "        this.rect = { x: 0, y: 0, w: 0, h: 0 };\n" +
            "        this.handlers = {};\n" +
            "        bridge.queue[this.uniqId] = this;\n" +
            "        bridge.syncMessage({ event: this.event, subEvent: 'init', args: { uniqueId: this.uniqId, type } });\n" +
            "        this.frame = function (x, y, w, h) {\n" +
            "            this.rect = { x, y, w, h };\n" +
            "            bridge.frame(this.event, this.uniqId, x, y, w, h);\n" +
            "        };\n" +
            "        this.hidden = function (hidden) {\n" +
            "            bridge.syncMessage({ event: this.event, subEvent: 'hidden', args: { uniqueId: this.uniqId, hidden } });\n" +
            "        };\n" +
            "        this.sensitivity = function (sensitivity) {\n" +
            "            bridge.syncMessage({ event: this.event, subEvent: 'sensitivity', args: { uniqueId: this.uniqId, sensitivity } });\n" +
            "        };\n" +
            "        this.sensitivityRaw = function (sensitivity_raw) {\n" +
            "            bridge.syncMessage({ event: this.event, subEvent: 'sensitivity_raw', args: { uniqueId: this.uniqId, sensitivity_raw } });\n" +
            "        };\n" +
            "        this.start = function () {\n" +
            "            bridge.syncMessage({ event: this.event, subEvent: 'start', args: { uniqueId: this.uniqId } });\n" +
            "        };\n" +
            "        this.destroy = function () {\n" +
            "            bridge.syncMessage({ event: this.event, subEvent: 'destroy', args: { uniqueId: this.uniqId } });\n" +
            "        };\n" +
            "        this.addEventListener = function (event, listener) {\n" +
            "            addEventListener(this.handlers, event, listener);\n" +
            "        };\n" +
            "        this.removeEventListener = function (event, listener) {\n" +
            "            removeEventListener(this.handlers, event, listener);\n" +
            "        };\n" +
            "    };\n" +
            "    mraid.Vpaid = function() {\n" +
            "        this.uniqId  = 'vd_'+(uniqueId++)+'_'+new Date().getTime();\n" +
            "        bridge.syncMessage({event: 'vpaid', subEvent: 'init', args: {uniqueId: this.uniqId}});\n" +
            "        bridge.vpaidQueue[this.uniqId] = this;\n" +
            "        this.handlers = {};\n" +
            "        this.assetURL = function(URL, useProxy = true) {\n" +
            "            bridge.syncMessage({event: 'vpaid', subEvent: 'assetURL', args: {uniqueId: this.uniqId,URL: URL,proxy:useProxy}});\n" +
            "        };\n" +
            "        this.play = function() {\n" +
            "            bridge.syncMessage({event: 'vpaid', subEvent: 'play', args: {uniqueId: this.uniqId}});\n" +
            "        };\n" +
            "        this.replay = function() {\n" +
            "            bridge.syncMessage({event: 'vpaid', subEvent: 'replay', args: {uniqueId: this.uniqId}});\n" +
            "        };\n" +
            "        this.pause = function () {\n" +
            "            bridge.syncMessage({event: 'vpaid', subEvent: 'pause', args: {uniqueId: this.uniqId}});\n" +
            "        };\n" +
            "        this.stop = function () {\n" +
            "            bridge.syncMessage({event: 'vpaid', subEvent: 'stop', args: {uniqueId: this.uniqId}});\n" +
            "        };\n" +
            "        this.muted = function (flag) {\n" +
            "            bridge.syncMessage({event: 'vpaid', subEvent: 'muted', args: {uniqueId: this.uniqId, muted: flag}});\n" +
            "        };\n" +
            "        this.seek = function(val) {\n" +
            "            bridge.syncMessage({event: 'vpaid',subEvent: 'seek',args: {uniqueId: this.uniqId,seekTime: val} });\n" +
            "        },\n" +
            "        this.frame = function(x, y, w, h) {\n" +
            "            bridge.frame('vpaid', this.uniqId, x, y, w, h)\n" +
            "        };\n" +
            "        this.addEventListener = function(event, listener) {\n" +
            "            addEventListener(this.handlers, event, listener);  \n" +
            "        };\n" +
            "        this.removeEventListener = function(event, listener) {\n" +
            "            removeEventListener(this.handlers, event, listener);\n" +
            "        };\n" +
            "     };\n" +
            "     mraid.skipAd = function(ctime) {\n" +
            "         sendCustomEvent(MRAID_CUSTOM_EVENTS.AD_SKIP_AD, 'ctime', ctime)\n" +
            "     };\n" +
            "     mraid.reward = function(ctime) {\n" +
            "         sendCustomEvent(MRAID_CUSTOM_EVENTS.AD_REWARD_AD, 'ctime', ctime)\n" +
            "     };\n" +
            "     mraid.apkMonitor = function(ext) {\n" +
            "         sendCustomEvent(MRAID_CUSTOM_EVENTS.AD_APKMONITOR, 'ext', ext)\n" +
            "     };\n" +
            "     mraid.volumChange = function(mute) {\n" +
            "         sendCustomEvent(MRAID_CUSTOM_EVENTS.AD_VIDEO_VOICE, 'state', mute)\n" +
            "     };\n" +
            "     mraid.showSkip = function(ctime) {\n" +
            "         sendCustomEvent(MRAID_CUSTOM_EVENTS.AD_SKIP_SHOW_TIME, 'ctime', ctime)\n" +
            "     };\n" +
            "     mraid.endcardShow = function() {\n" +
            "         sendCustomEvent(MRAID_CUSTOM_EVENTS.AD_ENDCARD_SHOW)\n" +
            "     };\n" +
            "     mraid.companionClick = function(ctime) {\n" +
            "         var args = new Array();\n" +
            "         var l = arguments.length;\n" +
            "         for (var i = 0; i < l; i++) {\n" +
            "             var obj = arguments[i];\n" +
            "             args.push(obj)\n" +
            "         }\n" +
            "         args.shift();\n" +
            "         var ln = args.length;\n" +
            "         if (ln == 0) sendCustomEvent(MRAID_CUSTOM_EVENTS.AD_COMPANION_CLICK, 'ctime', ctime);\n" +
            "         else {\n" +
            "             var ext = args[ln - 1];\n" +
            "             sendCustomEvent(MRAID_CUSTOM_EVENTS.AD_COMPANION_CLICK, 'ctime', ctime, 'ext', JSON.stringify(ext))\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.addEventListener = function(event, listener) {\n" +
            "         if (!event || !listener) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'Both event and listener are required.', 'addEventListener')\n" +
            "         } else {\n" +
            "             if (!listeners[event]) {\n" +
            "                 listeners[event] = new EventListeners(event)\n" +
            "             }\n" +
            "             listeners[event].add(listener)\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.close = function() {\n" +
            "         if (state === STATES.HIDDEN) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'Ad cannot be closed when it is already hidden.', 'close')\n" +
            "         } else bridge.executeNativeCall(['close'])\n" +
            "     };\n" +
            "     mraid.unload = function() {\n" +
            "         if (state === STATES.HIDDEN) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'Ad cannot be unload when it is already hidden.', 'unload')\n" +
            "         } else bridge.executeNativeCall(['unload'])\n" +
            "     };\n" +
            "     mraid.openFourElements = function() {\n" +
            "         if (state === STATES.HIDDEN) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'Ad cannot be openFourElements when it is already hidden.', 'openFourElements')\n" +
            "         } else bridge.executeNativeCall(['openFourElements'])\n" +
            "     };\n" +
            "     mraid.expand = function(URL) {\n" +
            "         if (!(this.getState() === STATES.DEFAULT || this.getState() === STATES.RESIZED)) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'Ad can only be expanded from the default or resized state.', 'expand')\n" +
            "         } else {\n" +
            "             var args = ['expand', 'shouldUseCustomClose', expandProperties.useCustomClose];\n" +
            "             if (URL) {\n" +
            "                 args = args.concat(['url', URL])\n" +
            "             }\n" +
            "             bridge.executeNativeCall(args)\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.getExpandProperties = function() {\n" +
            "         var properties = {\n" +
            "             width: expandProperties.width,\n" +
            "             height: expandProperties.height,\n" +
            "             useCustomClose: expandProperties.useCustomClose,\n" +
            "             isModal: expandProperties.isModal\n" +
            "         };\n" +
            "         return properties\n" +
            "     };\n" +
            "     mraid.version = () => '1.2'; \n " +
            "     mraid.getCurrentPosition = function() {\n" +
            "         return {\n" +
            "             x: currentPosition.x,\n" +
            "             y: currentPosition.y,\n" +
            "             width: currentPosition.width,\n" +
            "             height: currentPosition.height\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.getDefaultPosition = function() {\n" +
            "         return {\n" +
            "             x: defaultPosition.x,\n" +
            "             y: defaultPosition.y,\n" +
            "             width: defaultPosition.width,\n" +
            "             height: defaultPosition.height\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.getlocation = function() {\n" +
            "         return location\n" +
            "     };\n" +
            "     mraid.getMaxSize = function() {\n" +
            "         return {\n" +
            "             width: maxSize.width,\n" +
            "             height: maxSize.height\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.getPlacementType = function() {\n" +
            "         return placementType\n" +
            "     };\n" +
            "     mraid.getScreenSize = function() {\n" +
            "         return {\n" +
            "             width: screenSize.width,\n" +
            "             height: screenSize.height\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.getState = function() {\n" +
            "         return state\n" +
            "     };\n" +
            "     mraid.isViewable = function() {\n" +
            "         return isViewable\n" +
            "     };\n" +
            "     mraid.getVersion = function() {\n" +
            "         return mraid.VERSION\n" +
            "     };\n" +
            "     mraid.open = function(URL) {\n" +
            "         var args = new Array();\n" +
            "         var l = arguments.length;\n" +
            "         for (var i = 0; i < l; i++) {\n" +
            "             var obj = arguments[i];\n" +
            "             args.push(obj)\n" +
            "         }\n" +
            "         args.shift();\n" +
            "         if (!URL) broadcastEvent(EVENTS.ERROR, 'URL is required.', 'open');\n" +
            "         else {\n" +
            "             var ln = args.length;\n" +
            "             if (ln == 0) bridge.executeNativeCall(['open', 'url', URL]);\n" +
            "             var ext = args[ln - 1];\n" +
            "             bridge.executeNativeCall(['open', 'url', URL, 'ext', JSON.stringify(ext)])\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.removeEventListener = function(event, listener) {\n" +
            "         if (!event) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'Event is required.', 'removeEventListener');\n" +
            "             return\n" +
            "         }\n" +
            "         if (listener) {\n" +
            "             var success = false;\n" +
            "             if (listeners[event]) {\n" +
            "                 success = listeners[event].remove(listener)\n" +
            "             }\n" +
            "             if (!success) {\n" +
            "                 broadcastEvent(EVENTS.ERROR, 'Listener not currently registered for event.', 'removeEventListener');\n" +
            "                 return\n" +
            "             }\n" +
            "         } else if (!listener && listeners[event]) {\n" +
            "             listeners[event].removeAll()\n" +
            "         }\n" +
            "         if (listeners[event] && listeners[event].count === 0) {\n" +
            "             listeners[event] = null;\n" +
            "             delete listeners[event]\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.setExpandProperties = function(properties) {\n" +
            "         if (validate(properties, expandPropertyValidators, 'setExpandProperties', true)) {\n" +
            "             if (properties.hasOwnProperty('useCustomClose')) {\n" +
            "                 expandProperties.useCustomClose = properties.useCustomClose\n" +
            "             }\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.useCustomClose = function(shouldUseCustomClose) {\n" +
            "         expandProperties.useCustomClose = shouldUseCustomClose;\n" +
            "         hasSetCustomClose = true;\n" +
            "         bridge.executeNativeCall(['usecustomclose', 'shouldUseCustomClose', shouldUseCustomClose])\n" +
            "     };\n" +
            "     mraid.createCalendarEvent = function(parameters) {\n" +
            "         CalendarEventParser.initialize(parameters);\n" +
            "         if (CalendarEventParser.parse()) {\n" +
            "             bridge.executeNativeCall(CalendarEventParser.arguments)\n" +
            "         } else {\n" +
            "             broadcastEvent(EVENTS.ERROR, CalendarEventParser.errors[0], 'createCalendarEvent')\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.getSupports = function() {\n" +
            "         return supportProperties\n" +
            "     };\n" +
            "     mraid.supports = function(feature) {\n" +
            "         return supportProperties[feature]\n" +
            "     };\n" +
            "     mraid.playVideo = function(uri) {\n" +
            "         if (!mraid.isViewable()) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'playVideo cannot be called until the ad is viewable', 'playVideo');\n" +
            "             return\n" +
            "         }\n" +
            "         if (!uri) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'playVideo must be called with a valid URI', 'playVideo')\n" +
            "         } else {\n" +
            "             bridge.executeNativeCall(['playVideo', 'uri', uri])\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.storePicture = function(uri) {\n" +
            "         if (!mraid.isViewable()) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'storePicture cannot be called until the ad is viewable', 'storePicture');\n" +
            "             return\n" +
            "         }\n" +
            "         if (!uri) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'storePicture must be called with a valid URI', 'storePicture')\n" +
            "         } else {\n" +
            "             bridge.executeNativeCall(['storePicture', 'uri', uri])\n" +
            "         }\n" +
            "     };\n" +
            "     var resizePropertyValidators = {\n" +
            "         width: function(v) {\n" +
            "             return !isNaN(v) && v > 0\n" +
            "         },\n" +
            "         height: function(v) {\n" +
            "             return !isNaN(v) && v > 0\n" +
            "         },\n" +
            "         offsetX: function(v) {\n" +
            "             return !isNaN(v)\n" +
            "         },\n" +
            "         offsetY: function(v) {\n" +
            "             return !isNaN(v)\n" +
            "         },\n" +
            "         customClosePosition: function(v) {\n" +
            "             return (typeof v === 'string' && ['top-right', 'bottom-right', 'top-left', 'bottom-left', 'center', 'top-center', 'bottom-center'].indexOf(v) > -1)\n" +
            "         },\n" +
            "         allowOffscreen: function(v) {\n" +
            "             return (typeof v === 'boolean')\n" +
            "         }\n" +
            "     };\n" +
            "    var Motion = function(type) {\n" +
            "        this.uniqId = 'motion_' + (uniqueId++) + '_' + new Date().getTime();\n" +
            "        this.event = 'motion';\n" +
            "        var events = [];\n" +
            "        this.handlers = {};\n" +
            "        bridge.queue[this.uniqId] = this;\n" +
            "        this.init = function (sensitivity) {\n" +
            "            bridge.syncMessage({ event: this.event, subEvent: 'init',args: { uniqueId: this.uniqId, type, sensitivity} });\n" +
            "        };\n" +
            "        this.initSensitivityRaw = function (sensitivity_raw) {\n" +
            "            bridge.syncMessage({ event: this.event, subEvent: 'init_sensitivity_raw', args: { uniqueId: this.uniqId, type ,sensitivity_raw } });\n" +
            "        };\n" +
            "        this.destroy = function () {\n" +
            "            bridge.syncMessage({ event: this.event, subEvent: 'destroy',args: { uniqueId: this.uniqId, type } });\n" +
            "        };\n" +
            "        this.addEventListener = function (event,listener) {\n" +
            "            addEventListener(this.handlers, event, listener);\n" +
            "        };\n" +
            "        this.removeEventListener = function (event, listener) {\n" +
            "            removeEventListener(this.handlers, event, listener);\n" +
            "        };\n" +
            "    };\n" +
            "    mraid.motion = {\n" +
            "        shake: new Motion('shake'),\n" +
            "        twist: new Motion('twist'),\n" +
            "        slope: new Motion('slope'),\n" +
            "        swing: new Motion('swing'),\n" +
            "    };\n" +
            "     mraid.setOrientationProperties = function(properties) {\n" +
            "         if (properties.hasOwnProperty('allowOrientationChange')) {\n" +
            "             orientationProperties.allowOrientationChange = properties.allowOrientationChange\n" +
            "         }\n" +
            "         if (properties.hasOwnProperty('forceOrientation')) {\n" +
            "             orientationProperties.forceOrientation = properties.forceOrientation\n" +
            "         }\n" +
            "         var args = ['setOrientationProperties', 'allowOrientationChange', orientationProperties.allowOrientationChange, 'forceOrientation', orientationProperties.forceOrientation];\n" +
            "         bridge.executeNativeCall(args)\n" +
            "     };\n" +
            "     mraid.getOrientationProperties = function() {\n" +
            "         return {\n" +
            "             allowOrientationChange: orientationProperties.allowOrientationChange,\n" +
            "             forceOrientation: orientationProperties.forceOrientation\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.getCurrentAppOrientation = function() {\n" +
            "         return {\n" +
            "             orientation: currentAppOrientation.orientation,\n" +
            "             locked: currentAppOrientation.locked\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.resize = function() {\n" +
            "         if (!(this.getState() === STATES.DEFAULT || this.getState() === STATES.RESIZED)) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'Ad can only be resized from the default or resized state.', 'resize')\n" +
            "         } else if (!resizeProperties.width || !resizeProperties.height) {\n" +
            "             broadcastEvent(EVENTS.ERROR, 'Must set resize properties before calling resize()', 'resize')\n" +
            "         } else {\n" +
            "             var args = ['resize', 'width', resizeProperties.width, 'height', resizeProperties.height, 'offsetX', resizeProperties.offsetX || 0, 'offsetY', resizeProperties.offsetY || 0, 'customClosePosition', resizeProperties.customClosePosition, 'allowOffscreen', !!resizeProperties.allowOffscreen];\n" +
            "             bridge.executeNativeCall(args)\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.getResizeProperties = function() {\n" +
            "         var properties = {\n" +
            "             width: resizeProperties.width,\n" +
            "             height: resizeProperties.height,\n" +
            "             offsetX: resizeProperties.offsetX,\n" +
            "             offsetY: resizeProperties.offsetY,\n" +
            "             customClosePosition: resizeProperties.customClosePosition,\n" +
            "             allowOffscreen: resizeProperties.allowOffscreen\n" +
            "         };\n" +
            "         return properties\n" +
            "     };\n" +
            "     mraid.setResizeProperties = function(properties) {\n" +
            "         if (validate(properties, resizePropertyValidators, 'setResizeProperties', true)) {\n" +
            "             var desiredProperties = ['width', 'height', 'offsetX', 'offsetY', 'customClosePosition', 'allowOffscreen'];\n" +
            "             var length = desiredProperties.length;\n" +
            "             for (var i = 0; i < length; i++) {\n" +
            "                 var propname = desiredProperties[i];\n" +
            "                 if (properties.hasOwnProperty(propname)) {\n" +
            "                     resizeProperties[propname] = properties[propname]\n" +
            "                 }\n" +
            "             }\n" +
            "         }\n" +
            "     };\n" +
            "     mraid.setVideoObject = function(videoObject) {\n" +
            "         this._videoObject = videoObject\n" +
            "     };\n" +
            "     mraid.initVpaid = function(vpaidObject) {\n" +
            "         for (var event in VPAID_EVENTS) {\n" +
            "             var handle = vpaid_handlers[VPAID_EVENTS[event]];\n" +
            "             vpaidObject.subscribe(handle, VPAID_EVENTS[event])\n" +
            "         }\n" +
            "         this._vpaid = vpaidObject\n" +
            "     };\n" +
            "     bridge.startAd = function() {\n" +
            "         if (typeof(mraid._vpaid) === 'undefined') {\n" +
            "             console.dclog('vpaid = undefine');\n" +
            "             vpaid_handlers[VPAID_EVENTS.AD_ERROR].call();\n" +
            "             return\n" +
            "         }\n" +
            "         if (mraid._vpaid.startAd) {\n" +
            "             mraid._vpaid.startAd()\n" +
            "         } else vpaid_handlers[VPAID_EVENTS.AD_ERROR].call()\n" +
            "     };\n" +
            "     bridge.getAdDuration = function() {\n" +
            "         if (typeof(mraid._vpaid) == \"undefined\") {\n" +
            "             return undefined\n" +
            "         }\n" +
            "         if (mraid._vpaid.getAdDuration) {\n" +
            "             return mraid._vpaid.getAdDuration()\n" +
            "         } else return undefined\n" +
            "     };\n" +
            "     bridge.getPlayProgress = function() {\n" +
            "         if (typeof(mraid._vpaid) == \"undefined\") {\n" +
            "             return undefined\n" +
            "         }\n" +
            "         if (mraid._vpaid.getAdDuration && mraid._vpaid.getAdRemainingTime) {\n" +
            "             return (1 - mraid._vpaid.getAdRemainingTime() / mraid._vpaid.getAdDuration())\n" +
            "         } else return undefined\n" +
            "     };\n" +
            "     bridge.getVideoCurrentTime = function() {\n" +
            "         if (typeof(mraid._vpaid) == \"undefined\") {\n" +
            "             return undefined\n" +
            "         }\n" +
            "         if (mraid._vpaid.getAdDuration && mraid._vpaid.getAdRemainingTime) {\n" +
            "             return mraid._vpaid.getAdDuration() - mraid._vpaid.getAdRemainingTime()\n" +
            "         } else return undefined\n" +
            "     };\n" +
            "     mraid.getHostSDKVersion = function() {\n" +
            "         return hostSDKVersion\n" +
            "     };\n" +
            "     var CalendarEventParser = {\n" +
            "         initialize: function(parameters) {\n" +
            "             this.parameters = parameters;\n" +
            "             this.errors = [];\n" +
            "             this.arguments = ['createCalendarEvent']\n" +
            "         },\n" +
            "         parse: function() {\n" +
            "             if (!this.parameters) {\n" +
            "                 this.errors.push('The object passed to createCalendarEvent cannot be null.')\n" +
            "             } else {\n" +
            "                 this.parseDescription();\n" +
            "                 this.parseLocation();\n" +
            "                 this.parseSummary();\n" +
            "                 this.parseStartAndEndDates();\n" +
            "                 this.parseReminder();\n" +
            "                 this.parseRecurrence();\n" +
            "                 this.parseTransparency()\n" +
            "             }\n" +
            "             var errorCount = this.errors.length;\n" +
            "             if (errorCount) {\n" +
            "                 this.arguments.length = 0\n" +
            "             }\n" +
            "             return (errorCount === 0)\n" +
            "         },\n" +
            "         parseDescription: function() {\n" +
            "             this._processStringValue('description')\n" +
            "         },\n" +
            "         parseLocation: function() {\n" +
            "             this._processStringValue('location')\n" +
            "         },\n" +
            "         parseSummary: function() {\n" +
            "             this._processStringValue('summary')\n" +
            "         },\n" +
            "         parseStartAndEndDates: function() {\n" +
            "             this._processDateValue('start');\n" +
            "             this._processDateValue('end')\n" +
            "         },\n" +
            "         parseReminder: function() {\n" +
            "             var reminder = this._getParameter('reminder');\n" +
            "             if (!reminder) {\n" +
            "                 return\n" +
            "             }\n" +
            "             if (reminder < 0) {\n" +
            "                 this.arguments.push('relativeReminder');\n" +
            "                 this.arguments.push(parseInt(reminder) / 1000)\n" +
            "             } else {\n" +
            "                 this.arguments.push('absoluteReminder');\n" +
            "                 this.arguments.push(reminder)\n" +
            "             }\n" +
            "         },\n" +
            "         parseRecurrence: function() {\n" +
            "             var recurrenceDict = this._getParameter('recurrence');\n" +
            "             if (!recurrenceDict) {\n" +
            "                 return\n" +
            "             }\n" +
            "             this.parseRecurrenceInterval(recurrenceDict);\n" +
            "             this.parseRecurrenceFrequency(recurrenceDict);\n" +
            "             this.parseRecurrenceEndDate(recurrenceDict);\n" +
            "             this.parseRecurrenceArrayValue(recurrenceDict, 'daysInWeek');\n" +
            "             this.parseRecurrenceArrayValue(recurrenceDict, 'daysInMonth');\n" +
            "             this.parseRecurrenceArrayValue(recurrenceDict, 'daysInYear');\n" +
            "             this.parseRecurrenceArrayValue(recurrenceDict, 'monthsInYear')\n" +
            "         },\n" +
            "         parseTransparency: function() {\n" +
            "             var validValues = ['opaque', 'transparent'];\n" +
            "             if (this.parameters.hasOwnProperty('transparency')) {\n" +
            "                 var transparency = this.parameters.transparency;\n" +
            "                 if (contains(transparency, validValues)) {\n" +
            "                     this.arguments.push('transparency');\n" +
            "                     this.arguments.push(transparency)\n" +
            "                 } else {\n" +
            "                     this.errors.push('transparency must be opaque or transparent')\n" +
            "                 }\n" +
            "             }\n" +
            "         },\n" +
            "         parseRecurrenceArrayValue: function(recurrenceDict, kind) {\n" +
            "             if (recurrenceDict.hasOwnProperty(kind)) {\n" +
            "                 var array = recurrenceDict[kind];\n" +
            "                 if (!array || !(array instanceof Array)) {\n" +
            "                     this.errors.push(kind + ' must be an array.')\n" +
            "                 } else {\n" +
            "                     var arrayStr = array.join(',');\n" +
            "                     this.arguments.push(kind);\n" +
            "                     this.arguments.push(arrayStr)\n" +
            "                 }\n" +
            "             }\n" +
            "         },\n" +
            "         parseRecurrenceInterval: function(recurrenceDict) {\n" +
            "             if (recurrenceDict.hasOwnProperty('interval')) {\n" +
            "                 var interval = recurrenceDict.interval;\n" +
            "                 if (!interval) {\n" +
            "                     this.errors.push('Recurrence interval cannot be null.')\n" +
            "                 } else {\n" +
            "                     this.arguments.push('interval');\n" +
            "                     this.arguments.push(interval)\n" +
            "                 }\n" +
            "             } else {\n" +
            "                 this.arguments.push('interval');\n" +
            "                 this.arguments.push(1)\n" +
            "             }\n" +
            "         },\n" +
            "         parseRecurrenceFrequency: function(recurrenceDict) {\n" +
            "             if (recurrenceDict.hasOwnProperty('frequency')) {\n" +
            "                 var frequency = recurrenceDict.frequency;\n" +
            "                 var validFrequencies = ['daily', 'weekly', 'monthly', 'yearly'];\n" +
            "                 if (contains(frequency, validFrequencies)) {\n" +
            "                     this.arguments.push('frequency');\n" +
            "                     this.arguments.push(frequency)\n" +
            "                 } else {\n" +
            "                     this.errors.push('Recurrence frequency must be one of: \"daily\", \"weekly\", \"monthly\", \"yearly\".')\n" +
            "                 }\n" +
            "             }\n" +
            "         },\n" +
            "         parseRecurrenceEndDate: function(recurrenceDict) {\n" +
            "             var expires = recurrenceDict.expires;\n" +
            "             if (!expires) {\n" +
            "                 return\n" +
            "             }\n" +
            "             this.arguments.push('expires');\n" +
            "             this.arguments.push(expires)\n" +
            "         },\n" +
            "         _getParameter: function(key) {\n" +
            "             if (this.parameters.hasOwnProperty(key)) {\n" +
            "                 return this.parameters[key]\n" +
            "             }\n" +
            "             return null\n" +
            "         },\n" +
            "         _processStringValue: function(kind) {\n" +
            "             if (this.parameters.hasOwnProperty(kind)) {\n" +
            "                 var value = this.parameters[kind];\n" +
            "                 this.arguments.push(kind);\n" +
            "                 this.arguments.push(value)\n" +
            "             }\n" +
            "         },\n" +
            "         _processDateValue: function(kind) {\n" +
            "             if (this.parameters.hasOwnProperty(kind)) {\n" +
            "                 var dateString = this._getParameter(kind);\n" +
            "                 this.arguments.push(kind);\n" +
            "                 this.arguments.push(dateString)\n" +
            "             }\n" +
            "         }\n" +
            "     }\n" +
            " }());xxx.loaded();";
}