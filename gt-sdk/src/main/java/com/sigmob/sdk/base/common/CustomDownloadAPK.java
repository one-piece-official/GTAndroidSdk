package com.sigmob.sdk.base.common;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.utils.Md5Util;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.IntentActions;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;
import com.sigmob.sdk.base.utils.SigmobFileUtil;
import com.sigmob.sdk.downloader.DownloadTask;
import com.sigmob.sdk.downloader.FileDownload;
import com.sigmob.sdk.downloader.FileDownloaderNotificationListener;
import com.sigmob.sdk.downloader.StatusUtil;
import com.sigmob.sdk.downloader.core.Util;
import com.sigmob.sdk.downloader.core.breakpoint.BreakpointInfo;
import com.sigmob.sdk.downloader.core.breakpoint.BreakpointStore;
import com.sigmob.sdk.videoAd.FractionalProgressAdTracker;
import com.sigmob.windad.WindAds;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomDownloadAPK {


    public static int PauseDownload(Context context, BaseAdUnit adUnit) {

        DownloadTask task = adUnit.getDownloadTask();
        if (task != null && StatusUtil.isSameTaskPendingOrRunning(task)){
            HashMap ext = new HashMap<>();
            ext.put("result", Constants.SUCCESS);
            ext.put("downloadId", adUnit.getDownloadId());
            BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_DOWNLOAD_PAUSE);
            WindAds.sharedAds().getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    task.cancel();
                }
            }, 200);
            return 0;
        }

        return -1;
    }

    public static int resumeDownload(Context context, BaseAdUnit adUnit) {

        DownloadTask task = adUnit.getDownloadTask();

        if (task != null && !StatusUtil.isSameTaskPendingOrRunning(task)){
            task.enqueue();
            return 0;
        }


        return -1;
    }

    public static int CancelDownloadFile(Context context, BaseAdUnit adUnit) {

        DownloadTask task = adUnit.getDownloadTask();

        if (task != null){

                task.replaceListener(null);

                task.cancel();
                BreakpointStore breakpointStore = FileDownload.with().breakpointStore();
                breakpointStore.remove(task.getId());
                File tempFile = task.getTempFile();

                if (tempFile != null && tempFile.exists()){
                    tempFile.delete();
                }

                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null){
                    manager.cancel((int) task.getId());
                }



            // Remove target file.
                final File file = task.getFile();
                // Do nothing, because the filename hasn't found yet.
                if (file != null && file.exists()){
                    file.delete();
                }

                HashMap ext = new HashMap<>();
                ext.put("result", Constants.FAIL);
                ext.put("downloadId", adUnit.getDownloadId());
                BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_DOWNLOAD_END);
                adUnit.setDownloadId(null);
                adUnit.setDownloadTask(null);
                if (adUnit.isRecord()) {
                    PointEntitySigmobUtils.eventRecord(PointCategory.DOWNLOAD_END, Constants.FAIL, adUnit);
                }
                PointEntitySigmobUtils.SigmobTracking(PointCategory.APKDOWNLOAD, PointCategory.END, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {
                        if (pointEntityBase instanceof PointEntitySigmob) {
                            PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                            entitySigmob.getOptions().put("status", String.valueOf(0));
                            entitySigmob.getOptions().put("error", "cancel download");
                            //                                entitySigmob.getOptions().put("url", url);

                        }
                    }
                });
                return 0;
        }

        return -1;
    }

    static class DownloadActionReceiver extends BroadcastReceiver {
        static final String BUTTON_ACTION = "downloadButtonAction";
        static final String ACTION = "downloadAction";
        private final DownloadTask task;
        private final BaseAdUnit adUnit;


        public DownloadActionReceiver(DownloadTask task, BaseAdUnit adUnit) {
            this.task = task;
            this.adUnit = adUnit;
        }

        @Override public void onReceive(Context context, Intent intent) {
            if (task == null) return;

            String action = intent.getAction();

            if (Objects.equals(action, BUTTON_ACTION+"_"+adUnit.getUuid()+"_"+task.getId())) {
                    boolean sameTaskPendingOrRunning = StatusUtil.isSameTaskPendingOrRunning(task);

                    if (sameTaskPendingOrRunning) {
                        HashMap ext = new HashMap<>();
                        ext.put("result", Constants.SUCCESS);
                        ext.put("downloadId", adUnit.getDownloadId());
                        BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_DOWNLOAD_PAUSE);
                        WindAds.sharedAds().getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                task.cancel();
                            }
                        }, 200);
                    } else {
                        task.enqueue();
                    }
            }else{
                    boolean completed = StatusUtil.isCompleted(task);
                    if (completed) {
                        WindAds.sharedAds().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                DownloadAPK.installAPK(SDKContext.getApplicationContext(), task.getFile().getAbsolutePath(), adUnit);
                            }
                        });
                    }
                }
            }
    }


    private static String getApkName(BaseAdUnit adUnit,String url) {

        String apkMd5 = adUnit.getApkMd5();
        if (TextUtils.isEmpty(apkMd5)) {
            apkMd5 = Md5Util.md5(url);
        }

       return apkMd5 + ".apk";
    }

    protected static void useCustomDownloadManager(final String url, BaseAdUnit adUnit,boolean isMThread) {

        Context context = SDKContext.getApplicationContext();
        String apkFilePath = null;
        try {


            DownloadTask task = adUnit.getDownloadTask();
            if (task != null){

                apkFilePath = task.getFile().getAbsolutePath();
                boolean completed = StatusUtil.isCompleted(task);

                if (completed){
                    if (adUnit.isRecord()){
                        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_DOWNLOAD_FINISH);
                    }

                    DownloadTask finalTask = task;
                    WindAds.sharedAds().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            DownloadAPK.installAPK(SDKContext.getApplicationContext(), finalTask.getFile().getAbsolutePath(), adUnit);
                        }
                    });
                    return;
                } else {

                    boolean sameTaskPendingOrRunning = StatusUtil.isSameTaskPendingOrRunning(task);
                    if (sameTaskPendingOrRunning){

                        SigToast.makeText(context,   "正在下载", Toast.LENGTH_LONG).show();

//                        FileDownload.with().downloadDispatcher().enqueue(task);
                        HashMap ext = new HashMap<>();
                        ext.put("result", Constants.SUCCESS);
                        ext.put("downloadId", adUnit.getDownloadId());
                        BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_DOWNLOAD_START);
                        return;
                    }

                }

            }else{

                String apkName = adUnit.getApkName();
                DownloadTask.Builder builder = new DownloadTask.Builder(url, SigmobFileUtil.getDownloadAPKPathFile(context))
                        .setFilename(apkName)
                        .setPassIfAlreadyCompleted(false);

                builder.addHeader(Util.USER_AGENT, Networking.getUserAgent());
                if (isMThread){
                    task = builder.build();
                }else{
                    task = builder.setConnectionCount(1).build();
                }

                boolean sameTaskPendingOrRunning = StatusUtil.isSameTaskPendingOrRunning(task);
                if (sameTaskPendingOrRunning){
                    FileDownload.with().downloadDispatcher().cancel(task);
                }
                File file = task.getFile();
                if (file != null && file.exists()){
                    file.delete();
                }
                if (file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }

                FileDownloaderNotificationListener downloaderNotificationListener = new FileDownloaderNotificationListener(SDKContext.getApplicationContext()){
                    private StatusUtil.Status status;

                    @Override
                    protected void completed(DownloadTask task) {
                        status = StatusUtil.Status.COMPLETED;

                        super.completed(task);
                        adUnit.enableUseDownloadApk(true);
                        if (adUnit.isRecord()){
                            PointEntitySigmobUtils.eventRecord(PointCategory.DOWNLOAD_END, Constants.SUCCESS, adUnit);
                            SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_DOWNLOAD_FINISH);
                        }

                        WindAds.sharedAds().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                DownloadAPK.installAPK(SDKContext.getApplicationContext(), task.getFile().getAbsolutePath(), adUnit);
                            }
                        });
                        PointEntitySigmobUtils.SigmobTracking(PointCategory.APKDOWNLOAD, PointCategory.END, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                            @Override
                            public void onAddExtra(Object pointEntityBase) {
                                if (pointEntityBase instanceof PointEntitySigmob) {
                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                    entitySigmob.getOptions().put("status", String.valueOf(1));
//                                entitySigmob.getOptions().put("url", url);

                                }
                            }
                        });

                        HashMap ext = new HashMap<>();
                        ext.put("result", Constants.SUCCESS);
                        ext.put("downloadId", adUnit.getDownloadId());

                        BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_DOWNLOAD_END);


                    }

                    @Override
                    protected void error(DownloadTask task, Exception e) {
                        status = StatusUtil.Status.UNKNOWN;

                        super.error(task, e);
                        boolean retry = task.retry();
                        if (retry) return;
                        SigToast.makeText(context, "下载失败，"+e.getMessage(), Toast.LENGTH_LONG).show();
                        if (adUnit.isRecord()) {
                            PointEntitySigmobUtils.eventRecord(PointCategory.DOWNLOAD_END, Constants.FAIL, adUnit);
                        }
                        PointEntitySigmobUtils.SigmobTracking(PointCategory.APKDOWNLOAD, PointCategory.END, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                            @Override
                            public void onAddExtra(Object pointEntityBase) {
                                if (pointEntityBase instanceof PointEntitySigmob) {
                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                    entitySigmob.getOptions().put("status", String.valueOf(0));
                                    entitySigmob.getOptions().put("error", e.getMessage());
//                                entitySigmob.getOptions().put("url", url);

                                }
                            }
                        });
                        HashMap ext = new HashMap<>();
                        ext.put("result", Constants.FAIL);
                        ext.put("downloadId", adUnit.getDownloadId());

                        BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_DOWNLOAD_END);

                    }

                    @Override
                    public void connected(DownloadTask task, int blockCount, long currentOffset, long totalLength) {
                        super.connected(task, blockCount, currentOffset, totalLength);
                        HashMap ext = new HashMap<>();
                        ext.put("result", Constants.SUCCESS);
                        ext.put("downloadId", adUnit.getDownloadId());
                        BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_DOWNLOAD_START);
                    }

                    @Override
                    protected void started(DownloadTask task) {
                        status = StatusUtil.Status.RUNNING;

                        super.started(task);


                        PointEntitySigmobUtils.SigmobTracking(PointCategory.APKDOWNLOAD, PointCategory.START, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                            @Override
                            public void onAddExtra(Object pointEntityBase) {
                                if (pointEntityBase instanceof PointEntitySigmob) {
                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                    entitySigmob.getOptions().put("status", String.valueOf(1));
//                                entitySigmob.getOptions().put("url", url);

                                }
                            }
                        });
                    }

                    @Override
                    protected void canceled(DownloadTask task) {
                        if (status == StatusUtil.Status.CANCELED) {
                            return;
                        }
                        status = StatusUtil.Status.CANCELED;
                        super.canceled(task);

                        PointEntitySigmobUtils.SigmobTracking(PointCategory.APKDOWNLOAD, PointCategory.PAUSE, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                            @Override
                            public void onAddExtra(Object pointEntityBase) {
                                if (pointEntityBase instanceof PointEntitySigmob) {
                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                    entitySigmob.getOptions().put("status", String.valueOf(1));
//                                entitySigmob.getOptions().put("url", url);

                                }
                            }
                        });
                    }

                    @Override
                    public void progress(DownloadTask task, long currentOffset, long totalLength) {
                        status = StatusUtil.Status.RUNNING;

                        super.progress(task, currentOffset, totalLength);

                        List<FractionalProgressAdTracker> untriggeredTrackersBefore = FractionalProgressAdTracker.getUntriggeredTrackersBefore(adUnit.getDownloadQuarterTrack(), ADEvent.AD_DOWNLOAD_QUARTER, currentOffset, totalLength);
                        if (untriggeredTrackersBefore.size() > 0) {
                            for (FractionalProgressAdTracker tracker : untriggeredTrackersBefore) {
                                tracker.setTracked();

                                PointEntitySigmobUtils.SigmobTracking(PointCategory.APKDOWNLOAD, PointCategory.PROGRESS, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                    @Override
                                    public void onAddExtra(Object pointEntityBase) {

                                        if (pointEntityBase instanceof PointEntitySigmob) {
                                            PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
//                                        entitySigmob.getOptions().put("url", url);

                                            switch (tracker.getEvent()) {
                                                case ADEvent.AD_DOWNLOAD_QUARTER: {
                                                    entitySigmob.getOptions().put("status", String.valueOf(25));


                                                }
                                                break;
                                                case ADEvent.AD_DOWNLOAD_TWO_QUARTER: {
                                                    entitySigmob.getOptions().put("status", String.valueOf(50));

                                                }
                                                break;
                                                case ADEvent.AD_DOWNLOAD_THREE_QUARTER: {
                                                    entitySigmob.getOptions().put("status", String.valueOf(75));
                                                }
                                                break;
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                };

                final Intent button_intent = new Intent(DownloadActionReceiver.BUTTON_ACTION+"_"+adUnit.getUuid()+"_"+task.getId());
                button_intent.setPackage(SDKContext.getApplicationContext().getPackageName());
                DownloadActionReceiver receiver = new DownloadActionReceiver(task,adUnit);
                IntentFilter intentFilter = new IntentFilter(DownloadActionReceiver.ACTION+"_"+adUnit.getUuid()+"_"+task.getId());

                intentFilter.addAction(DownloadActionReceiver.BUTTON_ACTION+"_"+adUnit.getUuid()+"_"+task.getId());
                IntentUtil.registerReceiver(context,receiver, intentFilter);

                final Intent intent = new Intent(DownloadActionReceiver.ACTION+"_"+adUnit.getUuid()+"_"+task.getId());
                intent.setPackage(SDKContext.getApplicationContext().getPackageName());


                final List<FractionalProgressAdTracker> trackers =
                        new ArrayList<>();
                trackers.add(new FractionalProgressAdTracker(
                        ADEvent.AD_DOWNLOAD_QUARTER, 0.25f));
                trackers.add(new FractionalProgressAdTracker(
                        ADEvent.AD_DOWNLOAD_TWO_QUARTER, 0.5f));
                trackers.add(new FractionalProgressAdTracker(
                        ADEvent.AD_DOWNLOAD_THREE_QUARTER, 0.75f));

                adUnit.setDownloadQuarterTrack(trackers);
                downloaderNotificationListener.initNotification(adUnit.getIconUrl(),adUnit.getAppName(),adUnit.getDesc());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    downloaderNotificationListener.setButtonAction(PendingIntent.getBroadcast(SDKContext.getApplicationContext(), 0, button_intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
                    downloaderNotificationListener.setContextIntent(PendingIntent.getBroadcast(SDKContext.getApplicationContext(), 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
                } else {
                    downloaderNotificationListener.setButtonAction(PendingIntent.getBroadcast(SDKContext.getApplicationContext(), 0, button_intent,
                            PendingIntent.FLAG_UPDATE_CURRENT));
                    downloaderNotificationListener.setContextIntent(PendingIntent.getBroadcast(SDKContext.getApplicationContext(), 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT));
                }

                task.replaceListener(downloaderNotificationListener);

                adUnit.setDownloadTask(task);
                if (!adUnit.isResumableDownload()){
                    BreakpointStore breakpointStore = FileDownload.with().breakpointStore();
                    if (breakpointStore != null){
                        breakpointStore.remove(task.getId());
                    }
                }
            }

            adUnit.setDownloadId((long) task.getId());

            task.enqueue();
            SigToast.makeText(context, "已开始下载", Toast.LENGTH_LONG).show();

            if (adUnit.isRecord()){
                SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_DOWNLOAD_START);
                String finalApkFilePath = apkFilePath;
                PointEntitySigmobUtils.SigmobTracking(PointCategory.DOWNLOAD_START, Constants.SUCCESS, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {

                        if (pointEntityBase instanceof PointEntitySigmob) {
                            PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                            entitySigmob.setFinal_url(url);
                            Map<String, String> options = new HashMap<>();

                            options.put("apkfile", finalApkFilePath);
                            options.put("apkurl", url);
                            entitySigmob.setOptions(options);
                        }

                    }
                });
            }

        } catch (Throwable th) {

            HashMap ext = new HashMap<>();
            ext.put("result", Constants.FAIL);
            ext.put("downloadId", adUnit.getDownloadId());
            BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_DOWNLOAD_START);
            String finalApkFilePath = apkFilePath;
            PointEntitySigmobUtils.SigmobTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                @Override
                public void onAddExtra(Object pointEntityBase) {

                    if (pointEntityBase instanceof PointEntitySigmob) {
                        PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                        entitySigmob.setFinal_url(url);
                        Map<String, String> options = new HashMap<>();


                        options.put("apkfile", finalApkFilePath);
                        options.put("apkurl", url);
                        entitySigmob.setOptions(options);
                    }

                }
            });

            PointEntitySigmobUtils.SigmobTracking(PointCategory.APKDOWNLOAD, PointCategory.START, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                @Override
                public void onAddExtra(Object pointEntityBase) {
                    if (pointEntityBase instanceof PointEntitySigmob) {
                        PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                        entitySigmob.getOptions().put("status", String.valueOf(0));
                        entitySigmob.getOptions().put("error", th.getMessage());
                        SigToast.makeText(context, "下载失败，"+th.getMessage(), Toast.LENGTH_LONG).show();

//                        entitySigmob.getOptions().put("url", url);

                    }
                }
            });
        }

    }

    public static long[] getDownloadIdBytesAndStatus(Context context, BaseAdUnit adUnit) {
        long[] bytesAndStatus = new long[]{
                -1, -1, 0
        };

        DownloadTask downloadTask = adUnit.getDownloadTask();

        if (downloadTask == null) return bytesAndStatus;

        StatusUtil.Status status;
        BreakpointInfo breakpointInfo = downloadTask.getInfo();
        if (breakpointInfo != null){

            bytesAndStatus[0] = breakpointInfo.getTotalOffset();
            //下载文件的总大小
            bytesAndStatus[1] = breakpointInfo.getTotalLength();
            status = StatusUtil.getStatus(downloadTask);
        }else {
            status = StatusUtil.getStatus(adUnit.getDownloadUrl(), SigmobFileUtil.getDownloadAPKPathFile(context).getAbsolutePath(), adUnit.getApkName());
        }

        switch (status){
            case RUNNING:
                bytesAndStatus[2] = DownloadManager.STATUS_RUNNING;
                break;
            case COMPLETED:
                bytesAndStatus[2] = DownloadManager.STATUS_SUCCESSFUL;
                break;
            case PENDING:
                bytesAndStatus[2] = DownloadManager.STATUS_PENDING;
                break;
            case CANCELED:
                bytesAndStatus[2] = DownloadManager.STATUS_PAUSED;
                break;
            default:
                bytesAndStatus[2] = DownloadManager.STATUS_FAILED;
                break;
        }

        return bytesAndStatus;

    }


}
