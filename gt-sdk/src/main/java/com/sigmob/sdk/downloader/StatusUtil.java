/*
 * Copyright (c) 2017 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sigmob.sdk.downloader;


import com.sigmob.sdk.downloader.core.breakpoint.BreakpointInfo;
import com.sigmob.sdk.downloader.core.breakpoint.BreakpointStore;
import com.sigmob.sdk.downloader.core.dispatcher.DownloadDispatcher;

import java.io.File;

public class StatusUtil {

    public static boolean isSameTaskPendingOrRunning(DownloadTask task) {
        return FileDownload.with().downloadDispatcher().findSameTask(task) != null;
    }

    public static Status getStatus(DownloadTask task) {

        final Status status = isCompletedOrUnknown(task);
        if (status == Status.COMPLETED) return Status.COMPLETED;

        final DownloadDispatcher dispatcher = FileDownload.with().downloadDispatcher();

        if (dispatcher.isPending(task)) return Status.PENDING;
        if (dispatcher.isRunning(task)) return Status.RUNNING;
        if (dispatcher.isCanceled(task)) return Status.CANCELED;

        return status;
    }

    public static Status getStatus(String url, String parentPath,
                                   String filename) {
        return getStatus(createFinder(url, parentPath, filename));
    }

    public static boolean isCompleted(DownloadTask task) {
        return isCompletedOrUnknown(task) == Status.COMPLETED;
    }

    public static Status isCompletedOrUnknown(DownloadTask task) {
        final BreakpointStore store = FileDownload.with().breakpointStore();
        final BreakpointInfo info = store.get(task.getId());

        String filename = task.getFilename();
        final File parentFile = task.getParentFile();
        final File targetFile = task.getFile();

        if (info != null) {
            if (!info.isChunked() && info.getTotalLength() <= 0) {
                return Status.UNKNOWN;
            } else if ((targetFile != null && targetFile.equals(info.getFile()))
                    && targetFile.exists()
                    && info.getTotalOffset() == info.getTotalLength()) {
                return Status.COMPLETED;
            } else if (filename == null && info.getFile() != null
                    && info.getFile().exists()) {
                return Status.IDLE;
            } else if (targetFile != null && targetFile.equals(info.getFile())
                    && targetFile.exists()) {
                return Status.IDLE;
            }
        } else if (store.isOnlyMemoryCache() || store.isFileDirty(task.getId())) {
            return Status.UNKNOWN;
        } else if (targetFile != null && targetFile.exists()) {
            return Status.COMPLETED;
        } else {
            filename = store.getResponseFilename(task.getUrl());
            if (filename != null && new File(parentFile, filename).exists()) {
                return Status.COMPLETED;
            }
        }

        return Status.UNKNOWN;
    }

    public static boolean isCompleted(String url, String parentPath,
                                      String filename) {
        return isCompleted(createFinder(url, parentPath, filename));
    }

    public static BreakpointInfo getCurrentInfo(String url,
                                                String parentPath,
                                                String filename) {
        return getCurrentInfo(createFinder(url, parentPath, filename));
    }

    public static BreakpointInfo getCurrentInfo(DownloadTask task) {
        final BreakpointStore store = FileDownload.with().breakpointStore();
        final int id = store.findOrCreateId(task);

        final BreakpointInfo info = store.get(id);

        return info == null ? null : info.copy();
    }


    public static DownloadTask createFinder(String url,
                                     String parentPath,
                                     String filename) {
        return new DownloadTask.Builder(url, parentPath, filename)
                .build();
    }

    public enum Status {
        PENDING,
        RUNNING,
        COMPLETED,
        IDLE,
        CANCELED,
        // may completed, but no filename can't ensure.
        UNKNOWN;
    }
}
