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

package com.sigmob.sdk.downloader.core.listener;



import com.sigmob.sdk.downloader.DownloadListener;
import com.sigmob.sdk.downloader.DownloadTask;
import com.sigmob.sdk.downloader.core.breakpoint.BreakpointInfo;
import com.sigmob.sdk.downloader.core.cause.ResumeFailedCause;

import java.util.List;
import java.util.Map;

/**
 * taskStart->taskEnd
 */
public abstract class DownloadListener2 implements DownloadListener {
    @Override
    public void connectTrialStart( DownloadTask task,
                                   Map<String, List<String>> requestHeaderFields) {
    }

    @Override
    public void connectTrialEnd( DownloadTask task, int responseCode,
                                 Map<String, List<String>> responseHeaderFields) {
    }

    @Override
    public void downloadFromBeginning( DownloadTask task,  BreakpointInfo info,
                                       ResumeFailedCause cause) {
    }

    @Override
    public void downloadFromBreakpoint( DownloadTask task,  BreakpointInfo info) {
    }

    @Override
    public void connectStart( DownloadTask task, int blockIndex,
                              Map<String, List<String>> requestHeaderFields) {
    }

    @Override
    public void connectEnd( DownloadTask task, int blockIndex, int responseCode,
                            Map<String, List<String>> responseHeaderFields) {
    }

    @Override
    public void fetchStart( DownloadTask task, int blockIndex, long contentLength) {
    }

    @Override
    public void fetchProgress( DownloadTask task, int blockIndex, long increaseBytes) {
    }

    @Override
    public void fetchEnd( DownloadTask task, int blockIndex, long contentLength) {
    }
}
