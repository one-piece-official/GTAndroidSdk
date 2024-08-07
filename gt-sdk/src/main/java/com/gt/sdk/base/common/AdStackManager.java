package com.gt.sdk.base.common;


import static com.czhj.sdk.common.ThreadPool.ThreadPoolFactory.BackgroundThreadPool.getInstance;
import static com.sigmob.sdk.base.WindConstants.PLAY_MODE_STREAM;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.czhj.sdk.common.models.AdCache;
import com.czhj.sdk.common.models.AdFormat;
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


    private void AdCache(final BaseAdUnit adUnit, final AdStackStatusListener adStackStatusListener) {
        if (adUnit != null) {

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

        AdCache(adUnit, adStackStatusListener);
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

}
