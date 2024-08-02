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

import java.util.List;
import java.util.Map;

/**
 * @see com.sigmob.sdk.downloader.core.listener.DownloadListener1
 * @see com.sigmob.sdk.downloader.core.listener.DownloadListener2
 * @see com.sigmob.sdk.downloader.core.listener.DownloadListener3
 * @see com.sigmob.sdk.downloader.core.listener.DownloadListener4
 * @see com.sigmob.sdk.downloader.core.listener.DownloadListener4WithSpeed
 */
public interface DownloadListener {
    void taskStart(DownloadTask task);

    /**
     * On start trial connect state.
     * <p/>
     * The trial connection is used for:
     * 1. check whether the local info is valid to resume downloading
     * 2. get the instance length of this resource.
     * 3. check whether the resource support accept range.
     *
     * @param task                the host task.
     * @param requestHeaderFields the request header fields for this connection.
     */
    void connectTrialStart(DownloadTask task,
                           Map<String, List<String>> requestHeaderFields);

    /**
     * On end trial connect state.
     * <p/>
     * The trial connection is used for:
     * 1. check whether the local info is valid to resume downloading
     * 2. get the instance length of this resource.
     * 3. check whether the resource support accept range.
     *
     * @param task                 the host task.
     * @param responseCode         the response code of this trial connection.
     * @param responseHeaderFields the response header fields for this trial connection.
     */
    void connectTrialEnd(DownloadTask task,
                         int responseCode,
                         Map<String, List<String>> responseHeaderFields);

    void downloadFromBeginning(DownloadTask task, BreakpointInfo info,
                               ResumeFailedCause cause);

    void downloadFromBreakpoint(DownloadTask task, BreakpointInfo info);

    void connectStart(DownloadTask task, int blockIndex,
                      Map<String, List<String>> requestHeaderFields);

    void connectEnd(DownloadTask task, int blockIndex,
                    int responseCode,
                    Map<String, List<String>> responseHeaderFields);

    void fetchStart(DownloadTask task, int blockIndex,
                    long contentLength);

    void fetchProgress(DownloadTask task, int blockIndex,
                       long increaseBytes);

    void fetchEnd(DownloadTask task, int blockIndex,
                  long contentLength);

    void taskEnd(DownloadTask task, EndCause cause,
                 Exception realCause);
}
