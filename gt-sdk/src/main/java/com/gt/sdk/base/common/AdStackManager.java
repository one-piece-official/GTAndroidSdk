package com.gt.sdk.base.common;


import static com.czhj.sdk.common.ThreadPool.ThreadPoolFactory.BackgroundThreadPool.getInstance;
import static com.sigmob.sdk.base.WindConstants.PLAY_MODE_STREAM;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.czhj.sdk.common.models.AdCache;
import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.common.utils.Md5Util;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.VolleyError;
import com.czhj.volley.toolbox.DownloadItem;
import com.czhj.volley.toolbox.FileDownloadRequest;
import com.czhj.volley.toolbox.FileDownloader;
import com.gt.sdk.base.BaseAdUnit;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.rtb.BidResponse;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.base.models.rtb.ResponseAsset;
import com.sigmob.sdk.base.models.rtb.Template;
import com.sigmob.sdk.base.utils.GZipUtil;
import com.sigmob.sdk.base.utils.SigmobFileUtil;
import com.sigmob.sdk.videocache.CacheListener;
import com.sigmob.sdk.videocache.HttpProxyCacheServer;
import com.sigmob.sdk.videocache.VideoPreloadManager;
import com.sigmob.windad.WindAdError;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


public class AdStackManager {

    private static final Integer DOWNLOADSTATUS_UNKNOW = 0;
    private static final Integer DOWNLOADSTATUS_START = 1;
    private static final Integer DOWNLOADSTATUS_READY = 2;
    private static final Integer DOWNLOADSTATUS_END = 3;
    private static AdStackManager gshareInstance = null;
    private static Map<String, BaseAdUnit> AdUnitMap = new HashMap<>();
    private static HttpProxyCacheServer proxyCacheServer;
    private static HashMap<String, Integer> adDownloadStatus = new HashMap<>();
    private static volatile ImageManager mImageManager;
    private static BaseAdUnit clickAdUnit;
    private static HashMap<String, List<BaseAdUnit>> mNativeCacheAdList = new HashMap<>();
    private static NativeLoadReadyRecordManager mNativeLoadDc;
    private Map<String, String> videoFileMD5Map = new HashMap<>();
    private static HashMap<String, BidResponse> bidMap = new HashMap();
    private HashSet<AdCacheVideoListener> adCacheVideoListenerSet = new HashSet();

    private String vid;
    private String request_id;
    private static HashMap<String, Integer> showCount = new HashMap<>();
    final FileDownloadRequest.FileDownloadListener listener = new FileDownloadRequest.FileDownloadListener() {

        @Override
        public void onSuccess(DownloadItem item) {
            String key = Md5Util.md5(item.url);
            adDownloadStatus.put(key, DOWNLOADSTATUS_END);

            for (AdCacheVideoListener adCacheVideoListener : adCacheVideoListenerSet) {

                BaseAdUnit adUnit = adCacheVideoListener.getAdUnit();

                if (adUnit == null) continue;
                if (item.type == DownloadItem.FileType.FILE) {
                    if (adUnit.getEndCardZipPath().equals(item.filePath)) {
                        adCacheVideoListener.cache_endCard_success(adUnit);
                    }
                }
            }

            new AdUnitCacheFileTask(item).executeOnExecutor(getInstance().getExecutorService());
        }


        @Override
        public void onCancel(DownloadItem item) {
            String key = Md5Util.md5(item.url);
            adDownloadStatus.put(key, DOWNLOADSTATUS_END);

            for (AdCacheVideoListener adCacheVideoListener : adCacheVideoListenerSet) {

                BaseAdUnit adUnit = adCacheVideoListener.getAdUnit();

                if (adUnit == null) continue;

                if (item.type == DownloadItem.FileType.FILE) {
                    if (adUnit.getEndCardZipPath().equals(item.filePath)) {
                        adCacheVideoListener.cache_endCard_failed(adUnit);
                    }
                }
            }
        }


        @Override
        public void onErrorResponse(DownloadItem item) {
            SigmobLog.e("onErrorResponse: ", item.error);
            String key = Md5Util.md5(item.url);
            adDownloadStatus.put(key, DOWNLOADSTATUS_END);

            for (AdCacheVideoListener adCacheVideoListener : adCacheVideoListenerSet) {

                BaseAdUnit adUnit = adCacheVideoListener.getAdUnit();

                if (adUnit == null) continue;

                if (adUnit.getEndCardZipPath().equals(item.filePath) && item.type == DownloadItem.FileType.FILE) {
                    adCacheVideoListener.cache_endCard_failed(adUnit);
                }
            }

            new AdUnitCacheFileTask(item).executeOnExecutor(getInstance().getExecutorService());

        }

        @Override
        public void downloadProgress(DownloadItem item, long totalSize, long readSize) {

        }
    };
    private ArrayList<BaseAdUnit> waitReadyAdList = new ArrayList<>();
    private HashMap<String, AdCache> mVideohistoryAdCache = new HashMap<>();

    private String last_crid;
    private String last_campid;
    private HashMap<String, AdStackStatusListener> adStackStatusListenerList = new HashMap<>();
    private HashMap<String, AdCache> mNativehistoryAdCache = new HashMap<>();
    private Map<String, AdCache> mInterstitialAdCache = new HashMap<>();

    private AdStackManager() {

    }


    public static void addBidResponse(String request_id, BidResponse bidResponse) {
        if (TextUtils.isEmpty(request_id) || bidResponse == null) {
            return;
        }
        bidMap.put(request_id, bidResponse);
    }

    public static BidResponse getBidResponse(String request_id) {
        return bidMap.get(request_id);
    }

    public static void removeBidResponse(String request_id) {
        if (!TextUtils.isEmpty(request_id)) {
            bidMap.remove(request_id);
        }
    }

    public static synchronized ImageManager getImageManger() {

        if (mImageManager == null) {
            File file = new File(SigmobFileUtil.getNativeCachePath());

            synchronized (ImageManager.class) {
                if (mImageManager == null) {
                    mImageManager = new ImageManager(SDKContext.getApplicationContext()).customCachePath(file);
                }

            }
        }
        return mImageManager;
    }


    public static void setPlayAdUnit(final BaseAdUnit adUnit) {
        addAdUnit(adUnit);
    }

    public static void addAdUnit(final BaseAdUnit adUnit) {
        if (adUnit != null && !TextUtils.isEmpty(adUnit.getUuid())) {
            AdUnitMap.put(adUnit.getUuid(), adUnit);
        }
    }

    public static void cleanPlayAdUnit(BaseAdUnit adUnit) {

        if (adUnit != null && !TextUtils.isEmpty(adUnit.getUuid())) {
            AdUnitMap.remove(adUnit.getUuid());
            AdUnitListMap.remove(adUnit.getUuid());
        }
    }

    public static BaseAdUnit getPlayAdUnit(String uuid) {
        return AdUnitMap.get(uuid);
    }

    private static Map<String, List<BaseAdUnit>> AdUnitListMap = new HashMap<>();


//    public static synchronized void startNativeLoadDc(){
//        if (mNativeLoadDc == null) {
//            synchronized (AdStackManager.class){
//                if(mNativeLoadDc == null){
//                    mNativeLoadDc = new NativeLoadDc();
//                }
//            }
//        }
//        mNativeLoadDc.start();
//    }

    public static List<BaseAdUnit> getNativeAdValidList(String placementId) {
        if (TextUtils.isEmpty(placementId)) return null;
        List<BaseAdUnit> baseAdUnits = mNativeCacheAdList.get(placementId);

        if (baseAdUnits == null || baseAdUnits.isEmpty()) return null;
        CopyOnWriteArrayList<BaseAdUnit> list = new CopyOnWriteArrayList<>(baseAdUnits);

        for (BaseAdUnit adUnit : list) {
            if (adUnit.isExpiredAd()) {
                baseAdUnits.remove(adUnit);
//                SigmobLog.i("native_ad cache getNativeAdValidList  adUnit isExpiredAd remove vid "+ adUnit.getVid());
            }
        }

//        SigmobLog.i("native_ad cache getNativeAdValidList  size: "+ baseAdUnits.size() + "  placementId: " + placementId);
        return baseAdUnits;
    }

    public static void addCacheAdList(String placementId, List<BaseAdUnit> list, int maxLimit) {
        if (!TextUtils.isEmpty(placementId) && list != null && list.size() > 0) {
            List<BaseAdUnit> baseAdUnits = getNativeAdValidList(placementId);
            if (baseAdUnits == null || baseAdUnits.isEmpty() || maxLimit == 0) {
                baseAdUnits = list;
            } else {
                baseAdUnits.addAll(list);
            }
            Collections.sort(baseAdUnits, new Comparator<BaseAdUnit>() {
                @Override
                public int compare(BaseAdUnit lhs, BaseAdUnit rhs) {

                    int lprice = lhs.getBidEcpm() > 0 ? lhs.getBidEcpm() : lhs.getBP();
                    int rprice = rhs.getBidEcpm() > 0 ? rhs.getBidEcpm() : rhs.getBP();
                    return -Integer.compare(lprice, rprice);
                }
            });

//            for (BaseAdUnit baseAdUnit : baseAdUnits) {
//                SigmobLog.i("native_ad cache addCacheAdList bid_price "+baseAdUnit.getBP()+"  bid_ecpm "+baseAdUnit.getBidEcpm());
//            }
            if (maxLimit <= 0) {
                mNativeCacheAdList.remove(placementId);
            } else {
                if (baseAdUnits.size() > maxLimit) {
                    baseAdUnits = new LinkedList<>(baseAdUnits.subList(0, maxLimit));
                }
                mNativeCacheAdList.put(placementId, baseAdUnits);
//                SigmobLog.i("native_ad cache addCacheAdList  cache size "+ baseAdUnits.size() + "  placementId: " + placementId);
            }


        }
    }

    public static void addAdUnitList(List<BaseAdUnit> list) {
        if (list != null && list.size() > 0) {
            AdUnitListMap.put(list.get(0).getUuid(), list);
        }
    }

    public static List<BaseAdUnit> getAdUnitList(String uuid) {
        return AdUnitListMap.get(uuid);
    }


    public static void addShowCount(String placement_id) {
        if (!TextUtils.isEmpty(placement_id)) {

            Integer integer = showCount.get(placement_id);
            if (integer != null) {
                showCount.put(placement_id, integer + 1);
            } else {
                showCount.put(placement_id, 1);
            }
        }
    }

    public static void cleanShowCount(String placement_id) {
        if (!TextUtils.isEmpty(placement_id)) {
            showCount.remove(placement_id);
        }
    }

    /**
     * 删除缓存的临时html文件
     */
    public static void clearCacheFiles() {
        try {
            FileUtil.deleteDirectory(SigmobFileUtil.getSigHtmlDir(SigmobFileUtil.SigHtmlResourceDir).getAbsolutePath());
            clearMraid2Zip();
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
    }


    public static void cleanWebSourceCache() {
        try {
            File[] files = FileUtil.orderByDate(SigmobFileUtil.getWebCachePath());

            boolean flag;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                long before = System.currentTimeMillis() - WindSDKConfig.getInstance().getWebResourceCacheExpireTime() * 3600 * 1000;
                if (file.exists() && before > file.lastModified()) {

                    if (file.isFile()) {// 删除子文件
                        flag = FileUtil.deleteFile(file.getAbsolutePath());
                        if (!flag) {
                            break;
                        }
                    } else if (file.isDirectory()) {// 删除子目录
                        flag = FileUtil.deleteDirectory(file.getAbsolutePath());
                        if (!flag) {
                            break;
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    private static void clearMraid2Zip() {
        try {
            File[] files = FileUtil.orderByDate(SigmobFileUtil.getSigZipDir(SigmobFileUtil.SigZipResourceDir).getAbsolutePath());

            boolean flag;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                long before = System.currentTimeMillis() - WindSDKConfig.getInstance().getSplashExpiredTime();
                if (file.exists() && before > file.lastModified()) {

                    if (file.isFile()) {// 删除子文件
                        flag = FileUtil.deleteFile(file.getAbsolutePath());
                        if (!flag) {
                            break;
                        }
                    } else if (file.isDirectory()) {// 删除子目录
                        flag = FileUtil.deleteDirectory(file.getAbsolutePath());
                        if (!flag) {
                            break;
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static synchronized HttpProxyCacheServer getHttProxyCacheServer() {
        if (proxyCacheServer == null) {
            initHttpProxyCacheServer();
        }
        return proxyCacheServer;
    }

    public static synchronized void initHttpProxyCacheServer() {

        HttpProxyCacheServer.Builder builder = new HttpProxyCacheServer.Builder(SDKContext.getApplicationContext());

        try {
            File file = new File(SigmobFileUtil.getVideoCachePath());
            builder.cacheDirectory(file);
        } catch (Throwable e) {
            SigmobLog.e("initHttpProxyCacheServer fail ", e);
        }

        proxyCacheServer = builder.build();
    }

    public static BaseAdUnit getClickAdUnit() {
        return clickAdUnit;
    }

    public static void setClickAdUnit(BaseAdUnit adUnit) {
        clickAdUnit = adUnit;
    }

    public static Integer getShowCount(String placementId) {
        if (!TextUtils.isEmpty(placementId) && showCount.containsKey(placementId)) {
            return showCount.get(placementId);
        }
        return 1;
    }


    public static List<BaseAdUnit> getAdCacheList(String placementId, int bid_floor, int ad_count) {
        ArrayList<BaseAdUnit> baseAdUnits = new ArrayList<>();

        List<BaseAdUnit> nativeAdValidList = getNativeAdValidList(placementId);

        if (nativeAdValidList != null) {
            CopyOnWriteArrayList<BaseAdUnit> list = new CopyOnWriteArrayList<>(nativeAdValidList);
            for (BaseAdUnit adUnit : list) {
                int price = adUnit.getBidEcpm();
                if (price == 0) {
                    price = adUnit.getBP();
                }
                if (price >= bid_floor || bid_floor == 0) {
//                    SigmobLog.i("native_ad cache getAdCacheList load success adUnit vid "+ adUnit.getVid() + " price "+ price + " floor "+ bid_floor);
                    baseAdUnits.add(adUnit);
                    nativeAdValidList.remove(adUnit);
                }

                if (adUnit.bidding_response != null) {
                    break;
                }

                if (baseAdUnits.size() >= ad_count) {
                    break;
                }
            }
//            SigmobLog.i("native_ad cache getAdCacheList load success adUnit size  "+ baseAdUnits.size() + " cache size "+ nativeAdValidList.size());

        }

        return baseAdUnits;
    }

    private void preloadUrl(final DownloadItem item, final BaseAdUnit adUnit) {

        if (proxyCacheServer != null) {
            String url = item.url;
            if (!TextUtils.isEmpty(url) && !proxyCacheServer.isCached(url)) {

                String proxyUrl = proxyCacheServer.getProxyUrl(url);
                long start = System.currentTimeMillis();
                proxyCacheServer.registerCacheListener(new CacheListener() {
                    @Override
                    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
                        SigmobLog.d(cacheFile + " onCacheAvailable " + percentsAvailable);
                        if (percentsAvailable == 100 && cacheFile != null && !cacheFile.getAbsolutePath().endsWith("download")) {
                            proxyCacheServer.unregisterCacheListener(this);
                            item.networkMs = System.currentTimeMillis() - start;
                            new AdUnitCacheFileTask(item).executeOnExecutor(getInstance().getExecutorService());
                        }
                    }

                    @Override
                    public void onCacheUnavailable(String url, Throwable throwable) {
                        proxyCacheServer.unregisterCacheListener(this);
                        item.error = new VolleyError(WindAdError.ERROR_SIGMOB_VIDEO_FILE.getMessage());
//                        PointEntitySigmobUtils.SigmobError(PointCategory.LOAD, 0, throwable.getMessage(), adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
//                            @Override
//                            public void onAddExtra(Object pointEntityBase) {
//                                if (pointEntityBase instanceof PointEntitySigmob) {
//                                    PointEntitySigmob pointEntitySigmob = (PointEntitySigmob) pointEntityBase;
//                                    pointEntitySigmob.getOptions().put("video_url", adUnit.getVideo_url());
//                                }
//                            }
//                        });
                        item.networkMs = System.currentTimeMillis() - start;
                        new AdUnitCacheFileTask(item).executeOnExecutor(getInstance().getExecutorService());
                        SigmobLog.e(url + " onCacheUnavailable ", throwable);

                    }
                }, url);

                VideoPreloadManager.getInstance(SDKContext.getApplicationContext()).preloadVideo(proxyUrl);

            }
        }
    }

    public static synchronized AdStackManager shareInstance() {

        if (gshareInstance == null) {
            gshareInstance = new AdStackManager();
        }
        return gshareInstance;
    }

    public static void clearSplashAd() {

        try {
            File[] files = FileUtil.orderByDate(SigmobFileUtil.getSplashCachePath());

            files = clearCacheFileByDate(files, System.currentTimeMillis(), WindSDKConfig.getInstance().getSplashExpiredTime());

            files = FileUtil.clearCacheFileByCount(files, WindSDKConfig.getInstance().getSplashCacheTop());

            if (files == null) {
                SigmobLog.i("splash ad file list is null");
            } else {
                SigmobLog.i("splash ad file remain num: " + files.length);
            }
        } catch (Throwable throwable) {

            SigmobLog.e("clean splash ad file error", throwable);
        }

    }

    public static void clearVideoAdCache() {

        try {
            File[] files = FileUtil.orderByDate(SigmobFileUtil.getVideoCachePath());

            files = FileUtil.clearCacheFileByCount(files, WindSDKConfig.getInstance().getCacheTop() + WindSDKConfig.getInstance().getNativeAdCacheTop());

            if (files == null) {
                SigmobLog.i("splash ad file list is null");
            } else {
                SigmobLog.i("splash ad file remain num: " + files.length);
            }
        } catch (Throwable throwable) {

            SigmobLog.e("clean splash ad file error", throwable);
        }

    }

    public static void clearNativeAdCache() {

        try {
            File[] files = FileUtil.orderByDate(SigmobFileUtil.getNativeCachePath());

            files = FileUtil.clearCacheFileByCount(files, WindSDKConfig.getInstance().getNativeAdCacheTop());

            if (files == null) {
                SigmobLog.i("native ad file list is null");
            } else {
                SigmobLog.i("native ad file remain num: " + files.length);
            }
        } catch (Throwable throwable) {

            SigmobLog.e("clean native ad file error", throwable);
        }

    }

    public static void clearDownloadAPK() {

        try {

            File[] files = FileUtil.orderByDate(SigmobFileUtil.getDownloadAPKPathFile(SDKContext.getApplicationContext()).getAbsolutePath());

//            files = FileUtil.clearCacheFileByCount(files, 5);
//
//            if (files == null) {
//                SigmobLog.i("Download APK files  is null");
//            } else {
//                SigmobLog.i("Download APK files remain num: " + files.length);
//            }

            //然后删除7天过期文件
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if ((System.currentTimeMillis() - file.lastModified()) > 1000 * 60 * 60 * 24 * 7) {
                        boolean delete = file.delete();
                        SigmobLog.i("Download APK files timeOut seven day and delete:" + file.getName() + ":" + delete);
                    }
                }
            }

        } catch (Throwable throwable) {

            SigmobLog.e("clean Download APK file error", throwable);
        }

    }

    public static void deleteCacheTmpfiles() {

        try {

            File[] files = FileUtil.orderByDate(SigmobFileUtil.getCachePath());
            ArrayList<File> fileList = new ArrayList<>(Arrays.asList(files));


            for (int i = 0; i < files.length; i++) {


                File tempFile = files[i];

                if (!(tempFile.getPath().endsWith(".mp4") || tempFile.getPath().endsWith(".mp4.tmp") || tempFile.getPath().endsWith(".tgz") || tempFile.getPath().endsWith(".html"))) {
                    fileList.remove(tempFile);
                }
            }

            files = fileList.toArray(new File[0]);

            files = FileUtil.clearCacheFileByCount(files, 5);

            if (files == null) {
                SigmobLog.i("splash ad file list is null");
            } else {
                SigmobLog.i("splash ad file remain num: " + files.length);
            }
        } catch (Throwable throwable) {

            SigmobLog.e("clean splash ad file error", throwable);
        }

    }

    public static File[] clearCacheFileByDate(File[] files, long currentTime, long time) {

        if (files == null || files.length == 0) return null;

        ArrayList<File> fileList = new ArrayList<>(Arrays.asList(files));
        for (int i = 0; i < files.length; i++) {

            File file = files[i];
            long before = currentTime - time;
            if (file.exists() && before > file.lastModified()) {
                file.delete();
                fileList.remove(file);
                SigmobLog.d("file delete " + file.getName());
            }
        }

        return fileList.toArray(new File[0]);

    }

    public String getLast_crid() {
        return last_crid;
    }

    public void setLast_crid(String last_crid) {
        this.last_crid = last_crid;
    }

    public String getLast_campid() {
        return last_campid;
    }

    public void setLast_campid(String last_campid) {
        this.last_campid = last_campid;
    }

    public void removeHistoryAdAche(String placementId, int adtype) {
        if (TextUtils.isEmpty(placementId)) return;
        Map<String, AdCache> historyAdCache = getHistoryAdCache(adtype);


        if (historyAdCache != null) {
            historyAdCache.remove(placementId);
        }

    }

    public void removeHistoryAdCache(final BaseAdUnit adUnit) {

        if (adUnit == null || TextUtils.isEmpty(adUnit.getAdslot_id())) return;
        Map<String, AdCache> historyAdCache = getHistoryAdCache(adUnit.getAd_type());

        if (historyAdCache != null) {
            AdCache adCache = historyAdCache.get(adUnit.getAdslot_id());

            if (adCache != null) {
                List<String> crids = new ArrayList<>(adCache.crids);

                if (adCache != null && crids.contains(adUnit.getCrid())) {
                    crids.remove(adUnit.getCrid());
                }
                adCache = new AdCache(adUnit.getAd_type(), crids);
                historyAdCache.put(adUnit.getAdslot_id(), adCache);
            }

        }
    }

    public void addHistoryAdCache(BaseAdUnit adUnit) {


        if (adUnit == null || TextUtils.isEmpty(adUnit.getCrid()) || TextUtils.isEmpty(adUnit.getAdslot_id()))
            return;
        Map<String, AdCache> historyAdCache = getHistoryAdCache(adUnit.getAd_type());

        if (historyAdCache != null && historyAdCache.size() > 29) {
            return;
        }
        if (historyAdCache != null) {
            AdCache adCache = historyAdCache.get(adUnit.getAdslot_id());
            ArrayList<String> crids = new ArrayList<String>();
            crids.add(adUnit.getCrid());
            if (adCache != null) {
                crids.addAll(adCache.crids);
            }
            adCache = new AdCache(adUnit.getAd_type(), crids);

            historyAdCache.put(adUnit.getAdslot_id(), adCache);
        }
    }

    public List<BaseAdUnit> getCacheAdList(String placementId, int limit) {

        return null;
    }

    public Map<String, AdCache> getHistoryAdCache(int adType) {

        switch (adType) {
            case AdFormat.FULLSCREEN_VIDEO:
            case AdFormat.REWARD_VIDEO: {
                return mVideohistoryAdCache;
            }
            case AdFormat.UNIFIED_NATIVE: {
                return mNativehistoryAdCache;
            }
            case AdFormat.NEW_INTERSTITIAL: {
                return mInterstitialAdCache;
            }
            default: {
                return null;
            }
        }
    }

    private void cacheAdHanlder(BaseAdUnit adUnit, String error, AdStackStatusListener adStackStatusListener) {

        if (error == null) {
            if (!adUnit.isVideoExist() || !adUnit.isEndCardIndexExist()) {
                return;
            }

            if (adStackStatusListener != null) {
                adStackStatusListener.loadEnd(adUnit, null);
            }

        } else {
            if (adStackStatusListener != null) {
                adStackStatusListener.loadEnd(adUnit, error);
            }
        }
        cacheRemove(adUnit);
    }

    public void cacheRemove(BaseAdUnit adUnit) {
        waitReadyAdList.remove(adUnit);
        adStackStatusListenerList.remove(adUnit.getUuid());
    }

    public String getVideoFileMD5(String videoPath) {
        return videoFileMD5Map.get(videoPath);
    }


    public void addAdCacheVideoListener(AdCacheVideoListener adCacheVideoListener) {

        if (adCacheVideoListener != null && !adCacheVideoListenerSet.contains(adCacheVideoListener)) {
            adCacheVideoListenerSet.add(adCacheVideoListener);
        }
    }

    public void removeAdCacheVideoListener(AdCacheVideoListener adCacheVideoListener) {

        if (adCacheVideoListener != null && adCacheVideoListenerSet.contains(adCacheVideoListener)) {
            adCacheVideoListenerSet.remove(adCacheVideoListener);
        }
    }


    private void splashCache(final BaseAdUnit adUnit, final AdStackStatusListener adStackStatusListener) {
        if (adUnit != null) {
            File file = adUnit.getAdPrivacyTemplateFile();
            if (file != null) {
                if (!file.exists()) {
                    DownloadItem downloadItem = new DownloadItem();
                    downloadItem.url = adUnit.getadPrivacy().privacy_template_url;
                    downloadItem.filePath = file.getAbsolutePath();
                    downloadItem.type = DownloadItem.FileType.OTHER;
                    FileDownloader downloader = DownloaderFactory.getDownloader();
                    if (downloader != null) {
                        downloader.add(downloadItem, new FileDownloadRequest.FileDownloadListener() {
                            @Override
                            public void onSuccess(DownloadItem item) {

                            }

                            @Override
                            public void onCancel(DownloadItem item) {

                            }

                            @Override
                            public void onErrorResponse(DownloadItem item) {

                            }

                            @Override
                            public void downloadProgress(DownloadItem item, long totalSize, long readSize) {

                            }
                        });
                    }
                }
            }

            File splashAdFile = new File(adUnit.getSplashFilePath());
            if (splashAdFile.exists()) {

                splashAdFile.setLastModified(System.currentTimeMillis());
                if (adStackStatusListener != null) {
                    adStackStatusListener.loadEnd(adUnit, null);
                }
            } else {
                FileDownloader downloader = DownloaderFactory.getDownloader();
                DownloadItem item = new DownloadItem();
                item.filePath = adUnit.getSplashFilePath();
                item.url = adUnit.getSplashURL();
                item.type = DownloadItem.FileType.OTHER;
                item.userRange = false;
                FileDownloader.DownloadController mDownloadController;
                mDownloadController = downloader.add(item, new FileDownloadRequest.FileDownloadListener() {
                    @Override
                    public void onSuccess(DownloadItem item) {

                        if (adStackStatusListener != null) {
                            adStackStatusListener.loadEnd(adUnit, null);
                        }
                    }

                    @Override
                    public void onCancel(DownloadItem item) {

                    }


                    @Override
                    public void onErrorResponse(DownloadItem item) {
                        String error = item.error.getMessage();
                        if (item.error.networkResponse != null) {
                            error = error + " status code " + item.error.networkResponse.statusCode;
                        }
                        if (adStackStatusListener != null) {
                            adStackStatusListener.loadEnd(adUnit, error);
                        }
                        SigmobLog.e("onErrorResponse: " + error);
                    }

                    @Override
                    public void downloadProgress(DownloadItem item, long totalSize, long readSize) {

                    }
                });

            }

        }
    }

    public void cache(final BaseAdUnit adUnit, AdStackStatusListener adStackStatusListener) {

        if (adStackStatusListener != null) {
            adStackStatusListener.loadStart(adUnit);
        }

        if (adUnit == null) {
            SigmobLog.e("adUnit is null");
            if (adStackStatusListener != null) {
                adStackStatusListener.loadEnd(null, "adUnit is null");
            }
            return;
        }

        if (adUnit.getAd_type() == AdFormat.SPLASH) {
            splashCache(adUnit, adStackStatusListener);
        } else {
            if (adStackStatusListener != null) {
                adStackStatusListenerList.put(adUnit.getUuid(), adStackStatusListener);
            }

            try {
                new AdUnitCheckCacheTask(adUnit).executeOnExecutor(getInstance().getExecutorService());

            } catch (Throwable throwable) {
                SigmobLog.e("AdUnitCheckCacheTask execute error", throwable);
            }
        }


    }

    public interface AdCacheVideoListener {
        BaseAdUnit getAdUnit();

        void cache_endCard_success(BaseAdUnit adUnit);

        void cache_endCard_failed(BaseAdUnit adUnit);

    }

    private String getExistVideoFile(String url) {

        if (TextUtils.isEmpty(url)) {
            return null;
        }
        File file = AdStackManager.getHttProxyCacheServer().getCacheFile(url);
        if (file != null && file.exists()) {
            return file.getAbsolutePath();
        }
        return null;
    }


    public interface AdStackStatusListener {

        void loadStart(BaseAdUnit adUnit);

        void loadEnd(BaseAdUnit adUnit, String message);
    }

    class AdUnitCacheFileTask extends AsyncTask<Object, Void, String> {


        private DownloadItem mDownloadItem = null;

        AdUnitCacheFileTask(DownloadItem downloadItem) {
            mDownloadItem = downloadItem;
        }

        @Override
        protected String doInBackground(Object... objects) {

            if (mDownloadItem == null) {
                SigmobLog.e("Download Item is null");
                return WindAdError.ERROR_SIGMOB_FILE_DOWNLOAD.toString();

            }
            if (mDownloadItem.error != null) {
                return mDownloadItem.error.toString();
            }

            String downloadPath = mDownloadItem.filePath;

            String extensionName = FileUtil.getExtensionName(downloadPath);
            String md5 = Md5Util.fileMd5(downloadPath);
            if (!TextUtils.isEmpty(mDownloadItem.md5) && !mDownloadItem.md5.equalsIgnoreCase(md5)) {

                mDownloadItem.status = 0;
                return WindAdError.ERROR_SIGMOB_FILE_MD5.toString();
            }
            if (mDownloadItem.type == DownloadItem.FileType.VIDEO) {
                videoFileMD5Map.put(downloadPath, md5);
                mDownloadItem.status = 1;
            } else if (!TextUtils.isEmpty(extensionName) && extensionName.equalsIgnoreCase("tgz")) {
                try {

                    GZipUtil.uncompressTarGzipSync(new File(downloadPath), new File(downloadPath.replace(".tgz", "/")));
                    mDownloadItem.status = 1;

                } catch (Throwable e) {
                    mDownloadItem.status = 0;

                    SigmobLog.e(e.getMessage());
                    return WindAdError.ERROR_SIGMOB_FILE_DOWNLOAD.toString();
                }

            } else if (mDownloadItem.type == DownloadItem.FileType.MRAID_VIDEO) {
                return null;
            } else {
                mDownloadItem.status = 0;
                return WindAdError.ERROR_SIGMOB_INFORMATION_LOSE.toString();

            }
            return null;
        }

        @Override
        protected void onPostExecute(final String error) {

            super.onPostExecute(error);


            if (!TextUtils.isEmpty(error)) {
                FileUtil.deleteFile(mDownloadItem.filePath);
            }

            for (BaseAdUnit adUnit : new CopyOnWriteArrayList<>(waitReadyAdList)) {
                AdStackStatusListener adStackStatusListener = adStackStatusListenerList.get(adUnit.getUuid());
                if (mDownloadItem.type == DownloadItem.FileType.VIDEO && adUnit.getVideoPath().equals(mDownloadItem.filePath)) {
                    PointEntitySigmobUtils.eventDownloadTracking(mDownloadItem, adUnit, error, false);
                    cacheAdHanlder(adUnit, error, adStackStatusListener);
                } else if (mDownloadItem.type == DownloadItem.FileType.FILE && adUnit.getEndCardZipPath().equals(mDownloadItem.filePath)) {
                    cacheAdHanlder(adUnit, error, adStackStatusListener);
                } else if (mDownloadItem.type == DownloadItem.FileType.ZIP_FILE) {
                    cacheAdHanlder(adUnit, error, adStackStatusListener);
                } else if (mDownloadItem.type == DownloadItem.FileType.MRAID_VIDEO) {
                    PointEntitySigmobUtils.eventDownloadTracking(mDownloadItem, adUnit, error, false);
                    cacheAdHanlder(adUnit, error, adStackStatusListener);
                }
            }

        }
    }

    private class AdUnitCheckCacheTask extends AsyncTask<Object, Void, ArrayList<DownloadItem>> {

        BaseAdUnit mAdUnit;

        AdUnitCheckCacheTask(BaseAdUnit adUnit) {
            mAdUnit = adUnit;
        }

        private DownloadItem getZipDownloadItem(Template template) {
            DownloadItem sceneItem = null;
            if (!TextUtils.isEmpty(template.context.utf8())) {
                String fileName = Md5Util.md5(template.context.utf8());
                File sigZipDir = SigmobFileUtil.getSigZipDir(SigmobFileUtil.SigZipResourceDir);
                File destFile = new File(sigZipDir, fileName + ".tgz");

                if (!destFile.exists()) {//不存在就要下载
                    sceneItem = new DownloadItem();
                    sceneItem.url = template.context.utf8();
                    sceneItem.filePath = destFile.getAbsolutePath();
                    sceneItem.type = DownloadItem.FileType.ZIP_FILE;
                }
            }

            return sceneItem;
        }

        @Override
        protected ArrayList<DownloadItem> doInBackground(Object... objects) {

            try {
                ArrayList<DownloadItem> downloadItems = new ArrayList<>();

                if (mAdUnit == null || mAdUnit.getMaterial() == null) return null;
                final MaterialMeta material = mAdUnit.getMaterial();
                String md5 = null;
                long videoFileSize = 0;
                if (!TextUtils.isEmpty(mAdUnit.getVideo_url())) {

                    if (mAdUnit.isVideoExist()) {
                        md5 = Md5Util.fileMd5(mAdUnit.getVideoPath());
                        if (!TextUtils.isEmpty(mAdUnit.getVideo_OriginMD5()) && !mAdUnit.getVideo_OriginMD5().equalsIgnoreCase(md5)) {
                            FileUtil.deleteFile(mAdUnit.getVideoPath());
                        } else {
                            videoFileMD5Map.put(mAdUnit.getVideoPath(), md5);
                            File file = new File(mAdUnit.getVideoPath());
                            videoFileSize = file.length();
                        }
                    }

                    boolean isVideoValid = mAdUnit.checkVideoValid();
                    DownloadItem videoItem = new DownloadItem();

                    if (isVideoValid && mAdUnit.isVideoExist()) {
                        videoItem.url = mAdUnit.getVideo_url();
                        videoItem.filePath = mAdUnit.getVideoPath();
                        videoItem.type = DownloadItem.FileType.VIDEO;
                        videoItem.md5 = md5;
                        videoItem.size = videoFileSize;
                        videoItem.status = 1;

                        PointEntitySigmobUtils.eventDownloadTracking(videoItem, mAdUnit, null, true);

                    } else if (mAdUnit.getPlayMode() != PLAY_MODE_STREAM) {

                        videoItem.url = mAdUnit.getVideo_url();
                        videoItem.filePath = mAdUnit.getVideoPath();
                        videoItem.type = DownloadItem.FileType.VIDEO;
                        videoItem.md5 = mAdUnit.getVideo_OriginMD5();
                        preloadUrl(videoItem, mAdUnit);
                        downloadItems.add(videoItem);
                    }

                }

                if (material.creative_type == CreativeType.CreativeTypeVideo_Tar.getCreativeType()) {

                    DownloadItem endcardItem = new DownloadItem();
                    endcardItem.url = mAdUnit.getEndcard_url();
                    endcardItem.filePath = mAdUnit.getEndCardZipPath();
                    endcardItem.type = DownloadItem.FileType.FILE;
                    endcardItem.md5 = mAdUnit.getEndCard_OriginMD5();
                    downloadItems.add(endcardItem);
                }

                /**
                 * 下载2.0——main模版
                 */
                if (mAdUnit.scene != null && mAdUnit.scene.type == 3) {
                    DownloadItem zipDownloadItem = getZipDownloadItem(mAdUnit.scene);
                    if (zipDownloadItem != null) {
                        downloadItems.add(zipDownloadItem);
                    }
                }

                if (material.main_template != null && material.main_template.type == 3) {
                    DownloadItem zipDownloadItem = getZipDownloadItem(material.main_template);
                    if (zipDownloadItem != null) {
                        downloadItems.add(zipDownloadItem);
                    }
                }

                if (material.sub_template != null && material.sub_template.type == 3) {
                    DownloadItem zipDownloadItem = getZipDownloadItem(material.sub_template);
                    if (zipDownloadItem != null) {
                        downloadItems.add(zipDownloadItem);
                    }
                }

                /**
                 * 下载2.0video-只有video受予加载控制
                 */
                if (mAdUnit.isCatchVideo()) {
                    if (material.asset != null && material.asset.size() > 0) {
                        for (int i = 0; i < material.asset.size(); i++) {
                            ResponseAsset responseAsset = material.asset.get(i);
                            if (responseAsset != null && responseAsset.video != null && !TextUtils.isEmpty(responseAsset.video.url)) {

                                File cacheFile = AdStackManager.getHttProxyCacheServer().getCacheFile(responseAsset.video.url);

                                if (!cacheFile.exists()) {
                                    DownloadItem videoItem = new DownloadItem();
                                    videoItem.url = responseAsset.video.url;
                                    videoItem.filePath = cacheFile.getAbsolutePath();
                                    videoItem.type = DownloadItem.FileType.MRAID_VIDEO;
                                    preloadUrl(videoItem, mAdUnit);
                                    downloadItems.add(videoItem);
                                }
                            }
                        }
                    }
                }
                SigmobLog.d("cache() adUnit = [" + mAdUnit.getCrid() + "] videoUrl = [" + mAdUnit.getVideo_url() + "] endcardUrl = [" + mAdUnit.getEndcard_url() + "]");
                return downloadItems;

            } catch (Throwable t) {
                SigmobLog.e("AdUnitCheckCacheTask error: " + t.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<DownloadItem> downloadItems) {
            super.onPostExecute(downloadItems);

            if (mAdUnit == null) return;

            final MaterialMeta material = mAdUnit.getMaterial();


            AdStackStatusListener adStackStatusListener = adStackStatusListenerList.get(mAdUnit.getUuid());

            if (downloadItems == null) {
                downloadItems = new ArrayList<>();
            }
            if (downloadItems.size() == 0) {
                if (adStackStatusListener != null) {
                    adStackStatusListener.loadEnd(mAdUnit, null);
                }
                cacheRemove(mAdUnit);
            } else {

                FileDownloader downloader = DownloaderFactory.getDownloader();


                boolean isFind = false;

                for (BaseAdUnit adUnit : waitReadyAdList) {

                    if (adUnit.getUuid().equals(mAdUnit.getUuid())) {
                        isFind = true;
                        break;
                    }
                }

                if (!isFind && adStackStatusListener != null) {
                    waitReadyAdList.add(mAdUnit);
                }
                for (DownloadItem item : downloadItems) {
                    String key = Md5Util.md5(item.url);
                    SigmobLog.d("downloader add  item " + item.url);

                    if (item.type != DownloadItem.FileType.VIDEO) {
                        if (adDownloadStatus.get(key) == null || adDownloadStatus.get(key).equals(DOWNLOADSTATUS_END)) {
                            adDownloadStatus.put(key, DOWNLOADSTATUS_START);
                            downloader.add(item, listener);
                        }
                    }

                }

            }
            if (material != null && material.ad_privacy != null) {
                String privacy_template_url = material.ad_privacy.privacy_template_url;
                if (!TextUtils.isEmpty(privacy_template_url)) {
                    String fileName = Md5Util.md5(privacy_template_url);
                    File sigHtmlDir = SigmobFileUtil.getSigHtmlDir(SigmobFileUtil.SigHtmlPrivacyDir);
                    File destFile = new File(sigHtmlDir, fileName + ".html");
                    if (!destFile.exists()) {//不存在就要下载
                        try {
                            //如果原来文件夹有文件就先删除以前的文件
                            if ((sigHtmlDir.exists()) && sigHtmlDir.isDirectory()) {
                                // 删除文件夹中的所有文件包括子目录
                                File[] files = sigHtmlDir.listFiles();
                                for (File file : files) {
                                    // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
                                    if (file.exists() && file.isFile()) {
                                        if (file.delete()) {
                                            SigmobLog.d("删除单个文件" + file.getAbsolutePath() + "成功！");
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //然后再去下载新的文件
                        DownloadItem downloadItem = new DownloadItem();
                        downloadItem.url = privacy_template_url;
                        downloadItem.filePath = destFile.getAbsolutePath();
                        downloadItem.type = DownloadItem.FileType.OTHER;
                        downloadItems.add(downloadItem);
                        FileDownloader downloader = DownloaderFactory.getDownloader();
                        downloader.add(downloadItem, new FileDownloadRequest.FileDownloadListener() {
                            @Override
                            public void onSuccess(DownloadItem item) {
                                SigmobLog.i("onPostExecute onSuccess:" + item.url);
                            }

                            @Override
                            public void onCancel(DownloadItem item) {
                                SigmobLog.i("onPostExecute onCancel:" + item.url);
                            }


                            @Override
                            public void onErrorResponse(DownloadItem item) {
                                SigmobLog.i("onPostExecute onErrorResponse:" + item.url);
                            }

                            @Override
                            public void downloadProgress(DownloadItem item, long totalSize, long readSize) {

                            }
                        });
                    } else {
                        SigmobLog.i("privacy_template_url:" + fileName + " is exists");
                    }
                }
            }


        }
    }

}
