/*
 * Copyright (c) 2018 LingoChamp Inc.
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

package com.sigmob.sdk.downloader.core.download;

import static com.sigmob.sdk.downloader.core.cause.ResumeFailedCause.FILE_NOT_EXIST;
import static com.sigmob.sdk.downloader.core.cause.ResumeFailedCause.INFO_DIRTY;
import static com.sigmob.sdk.downloader.core.cause.ResumeFailedCause.OUTPUT_STREAM_NOT_SUPPORT;

import android.net.Uri;

import com.sigmob.sdk.downloader.DownloadTask;
import com.sigmob.sdk.downloader.FileDownload;
import com.sigmob.sdk.downloader.core.Util;
import com.sigmob.sdk.downloader.core.breakpoint.BlockInfo;
import com.sigmob.sdk.downloader.core.breakpoint.BreakpointInfo;
import com.sigmob.sdk.downloader.core.cause.ResumeFailedCause;

import java.io.File;

/**
 * Check whether the breakpoint is valid to reuse through the local data.
 */
public class BreakpointLocalCheck {

    private boolean dirty;
    boolean fileExist;
    boolean infoRight;
    boolean outputStreamSupport;

    private final DownloadTask task;
    private final BreakpointInfo info;
    private final long responseInstanceLength;

    /**
     * @param responseInstanceLength if the value is larger than {@code 0}, this value will used to
     *                               compare with the old instance-length save on the {@code info}.
     */
    public BreakpointLocalCheck(DownloadTask task, BreakpointInfo info,
                                long responseInstanceLength) {
        this.task = task;
        this.info = info;
        this.responseInstanceLength = responseInstanceLength;
    }

    /**
     * Get whether the local data is dirty. only if one of the {@code fileExist} or
     * {@code infoRight} or {@code outputStreamSupport} is {@code false}, the result will be
     * {@code false}.
     *
     * @return whether the local data is dirty.
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Get cause and if the cause isn't exist  throw the {@link IllegalStateException}.
     *
     * @return the cause of resume failed.
     */

    public ResumeFailedCause getCauseOrThrow() {
        if (!infoRight) {
            return INFO_DIRTY;
        } else if (!fileExist) {
            return FILE_NOT_EXIST;
        } else if (!outputStreamSupport) {
            return OUTPUT_STREAM_NOT_SUPPORT;
        }

        throw new IllegalStateException("No cause find with dirty: " + dirty);
    }

    public boolean isInfoRightToResume() {
        final int blockCount = info.getBlockCount();

        if (blockCount <= 0) return false;
        if (info.isChunked()) return false;
        if (info.getFile() == null) return false;
        final File fileOnTask = task.getTempFile();
        if (!info.getFile().equals(fileOnTask)) return false;
        if (info.getFile().length() > info.getTotalLength()) return false;

        if (responseInstanceLength > 0 && info.getTotalLength() != responseInstanceLength) {
            return false;
        }

        for (int i = 0; i < blockCount; i++) {
            BlockInfo blockInfo = info.getBlock(i);
            if (blockInfo.getContentLength() <= 0) return false;
        }

        return true;
    }

    public boolean isOutputStreamSupportResume() {
        final boolean supportSeek = FileDownload.with().outputStreamFactory().supportSeek();
        if (supportSeek) return true;

        if (info.getBlockCount() != 1) return false;
        if (FileDownload.with().processFileStrategy().isPreAllocateLength(task)) return false;

        return true;
    }

    public boolean isFileExistToResume() {
        final Uri uri = task.getUri();
        if (Util.isUriContentScheme(uri)) {
            return Util.getSizeFromContentUri(uri) > 0;
        } else {
            final File file = task.getTempFile();
            return file != null && file.exists();
        }
    }

    public void check() {
        fileExist = isFileExistToResume();
        infoRight = isInfoRightToResume();
        outputStreamSupport = isOutputStreamSupportResume();
        dirty = !infoRight || !fileExist || !outputStreamSupport;
    }

    @Override
    public String toString() {
        return "fileExist[" + fileExist + "] "
                + "infoRight[" + infoRight + "] "
                + "outputStreamSupport[" + outputStreamSupport + "] "
                + super.toString();
    }
}
