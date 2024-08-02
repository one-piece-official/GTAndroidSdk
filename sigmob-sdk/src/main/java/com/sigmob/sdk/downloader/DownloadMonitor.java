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
import com.sigmob.sdk.downloader.core.cause.EndCause;
import com.sigmob.sdk.downloader.core.cause.ResumeFailedCause;

/**
 * If you set monitor to FileDownload, the lifecycle of every task on FileDownload will be caught and
 * callback to this monitor.
 *
 * @see FileDownload#setMonitor(DownloadMonitor)
 * @see FileDownload.Builder#setMonitor(DownloadMonitor)
 */
public interface DownloadMonitor {
    void taskStart(DownloadTask task);

    /**
     * Call this monitor function when the {@code task} just end trial connection, and its
     * {@code info} is ready and also certain this task will resume from the past breakpoint.
     *
     * @param task the target task.
     * @param info has certainly total-length and offset-length now.
     */
    void taskDownloadFromBreakpoint(DownloadTask task, BreakpointInfo info);

    /**
     * Call this monitor function when the {@code task} just end trial connection, and its
     * {@code info} is ready and also certain this task will download from the very beginning.
     *
     * @param task  the target task.
     * @param info  has certainly total-length and offset-length now.
     * @param cause the cause of why download from the very beginning instead of from the past
     *              breakpoint.
     */
    void taskDownloadFromBeginning(DownloadTask task, BreakpointInfo info,
                                   ResumeFailedCause cause);

    void taskEnd(DownloadTask task, EndCause cause, Exception realCause);
}
