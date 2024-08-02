package com.sigmob.sdk.downloader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.common.utils.ResourceUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.downloader.core.cause.ResumeFailedCause;
import com.sigmob.sdk.downloader.core.listener.DownloadListener3;

public class FileDownloaderNotificationListener extends DownloadListener3 {

    private Notification.Builder builder;
    private NotificationManager manager;
    private RemoteViews contentView;
    private Context context;

    public FileDownloaderNotificationListener(Context context) {
        this.context = context.getApplicationContext();
        contentView = new RemoteViews(context.getPackageName(), ResourceUtil.getLayoutId(context, "sig_download_notification_layout"));
    }

    public void setButtonAction(PendingIntent intent) {
        contentView.setOnClickPendingIntent(ResourceUtil.getId(context, "sig_download_notification_button"), intent);
    }

    public void setContextIntent(PendingIntent intent) {
        builder.setContentIntent(intent);
    }

    public void initNotification(String icon_url, String title, String description) {
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final String channelId = "sig_filedownloader_notification";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "sig_filedownloader",
                    NotificationManager.IMPORTANCE_MIN);
            manager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, channelId);
        } else {
            builder = new Notification.Builder(context);
        }

        contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_title"), title);
        if (!TextUtils.isEmpty(icon_url)) {

            try {
                AdStackManager.getImageManger().getBitmap(icon_url, new ImageManager.BitmapLoadedListener() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap) {
                        contentView.setImageViewBitmap(ResourceUtil.getId(context, "sig_download_notification_icon"), bitmap);

                    }

                    @Override
                    public void onBitmapLoadFailed() {

                    }
                });
            } catch (Throwable ignored) {

            }
        }
        contentView.setProgressBar(ResourceUtil.getId(context, "sig_download_notification_progress"), 100, 0, false);
        contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_speed"), "0M/0M");
        contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_status"), "等待开始");
        contentView.setViewVisibility(ResourceUtil.getId(context, "sig_download_notification_button"), View.VISIBLE);

        builder.setDefaults(Notification.DEFAULT_LIGHTS)
                .setOngoing(true)
                .setSmallIcon(context.getApplicationInfo().icon)
                .setPriority(Notification.PRIORITY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setCustomContentView(contentView);
        } else {
            builder.setContent(contentView);
        }
    }
    @Override
    protected void started(DownloadTask task) {
        contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_status"), "准备下载");
        contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_button"), "暂停");
        contentView.setViewVisibility(ResourceUtil.getId(context, "sig_download_notification_button"), View.VISIBLE);

        manager.notify(task.getId(), builder.build());
    }

    @Override
    protected void completed(DownloadTask task) {

        boolean b = task.getTempFile().renameTo(task.getFile());
        if (!b) {
            SigmobLog.e( "download temp file renameTo failed");
        }
        SigmobLog.d("FileDownloaderNotificationListener completed " );

        contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_speed"), "下载完成,立即安装");
        contentView.setViewVisibility(ResourceUtil.getId(context, "sig_download_notification_button"), View.INVISIBLE);
        contentView.setViewVisibility(ResourceUtil.getId(context, "sig_download_notification_status"), View.GONE);

        builder.setOngoing(false);
        builder.setAutoCancel(true);
        manager.notify(task.getId(), builder.build());
    }
        @Override
    protected void canceled(DownloadTask task) {

        contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_status"), "下载暂停");
        contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_button"), "继续");
        contentView.setViewVisibility(ResourceUtil.getId(context, "sig_download_notification_button"), View.VISIBLE);

        manager.notify(task.getId(), builder.build());

    }

    @Override
    protected void error(DownloadTask task, Exception e) {

        contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_status"), "下载失败");
        contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_button"), "重试");
        contentView.setViewVisibility(ResourceUtil.getId(context, "sig_download_notification_button"), View.VISIBLE);

        manager.notify(task.getId(), builder.build());

    }

    @Override
    protected void warn(DownloadTask task) {

    }

    @Override
    public void retry(DownloadTask task, ResumeFailedCause cause) {

    }

    @Override
    public void connected(DownloadTask task, int blockCount, long currentOffset, long totalLength) {
        if (currentOffset > 1024 * 1024) {
            contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_speed"), currentOffset / 1024 / 1024 + "M/" + totalLength / 1024 / 1024 + "M");
        } else {
            contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_speed"), currentOffset / 1024 + "KB/" + totalLength / 1024 / 1024 + "M");
        }
        contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_status"), "正在下载");
        contentView.setViewVisibility(ResourceUtil.getId(context, "sig_download_notification_button"), View.VISIBLE);

        manager.notify(task.getId(), builder.build());
    }

    @Override
    public void progress(DownloadTask task, long currentOffset, long totalLength) {
        Log.d("NotificationActivity", "progress " + currentOffset);
        if (currentOffset > 1024 * 1024) {
            contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_speed"), currentOffset / 1024 / 1024 + "M/" + totalLength / 1024 / 1024 + "M");
        } else {
            contentView.setTextViewText(ResourceUtil.getId(context, "sig_download_notification_speed"), currentOffset / 1024 + "KB/" + totalLength / 1024 / 1024 + "M");
        }
        if (totalLength > 0) {
            int progress = (int) (currentOffset * 100 / totalLength);
            contentView.setProgressBar(ResourceUtil.getId(context, "sig_download_notification_progressBar"), 100,progress , false);
        }
        contentView.setViewVisibility(ResourceUtil.getId(context, "sig_download_notification_button"), View.VISIBLE);

        manager.notify(task.getId(), builder.build());

    }
}