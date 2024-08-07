package com.gt.sdk.base.common;

import com.czhj.sdk.common.network.Networking;
import com.czhj.volley.toolbox.FileDownloader;

public class DownloaderFactory {

    private static FileDownloader fileDownloader = null;

    public static FileDownloader getDownloader() {

        if (fileDownloader == null) {
            synchronized (DownloaderFactory.class) {
                if (fileDownloader == null && Networking.getDownloadRequestQueue() != null) {
                    fileDownloader = new FileDownloader(Networking.getDownloadRequestQueue(), 3);
                }
            }
        }
        return fileDownloader;
    }

}
