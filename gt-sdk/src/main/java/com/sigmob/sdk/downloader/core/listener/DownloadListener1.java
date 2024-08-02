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
import com.sigmob.sdk.downloader.core.cause.EndCause;
import com.sigmob.sdk.downloader.core.cause.ResumeFailedCause;
import com.sigmob.sdk.downloader.core.listener.assist.Listener1Assist;
import com.sigmob.sdk.downloader.core.listener.assist.ListenerAssist;

import java.util.List;
import java.util.Map;

/**
 * taskStart->(retry)->connect->progress<-->progress(currentOffset)->taskEnd
 */
public abstract class DownloadListener1 implements DownloadListener,
        Listener1Assist.Listener1Callback, ListenerAssist {
    final Listener1Assist assist;

    DownloadListener1(Listener1Assist assist) {
        this.assist = assist;
        assist.setCallback(this);
    }

    public DownloadListener1() {
        this(new Listener1Assist());
    }

    @Override
    public boolean isAlwaysRecoverAssistModel() {
        return assist.isAlwaysRecoverAssistModel();
    }

    @Override
    public void setAlwaysRecoverAssistModel(boolean isAlwaysRecoverAssistModel) {
        assist.setAlwaysRecoverAssistModel(isAlwaysRecoverAssistModel);
    }

    @Override
    public void setAlwaysRecoverAssistModelIfNotSet(boolean isAlwaysRecoverAssistModel) {
        assist.setAlwaysRecoverAssistModelIfNotSet(isAlwaysRecoverAssistModel);
    }

    @Override
    public final void taskStart( DownloadTask task) {
        assist.taskStart(task);
    }

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
        assist.downloadFromBeginning(task, info, cause);
    }

    @Override
    public void downloadFromBreakpoint( DownloadTask task,  BreakpointInfo info) {
        assist.downloadFromBreakpoint(task, info);
    }

    @Override
    public void connectStart( DownloadTask task, int blockIndex,
                              Map<String, List<String>> requestHeaderFields) {
    }

    @Override
    public void connectEnd( DownloadTask task, int blockIndex, int responseCode,
                            Map<String, List<String>> responseHeaderFields) {
        assist.connectEnd(task);
    }

    @Override
    public void fetchStart( DownloadTask task, int blockIndex, long contentLength) {
    }

    @Override
    public void fetchProgress( DownloadTask task, int blockIndex, long increaseBytes) {
        assist.fetchProgress(task, increaseBytes);
    }

    @Override
    public void fetchEnd( DownloadTask task, int blockIndex, long contentLength) {
    }

    @Override
    public final void taskEnd( DownloadTask task,  EndCause cause,
                              Exception realCause) {
        assist.taskEnd(task, cause, realCause);
    }
}

