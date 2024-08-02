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
import com.sigmob.sdk.downloader.core.listener.assist.Listener4Assist;
import com.sigmob.sdk.downloader.core.listener.assist.ListenerAssist;
import com.sigmob.sdk.downloader.core.listener.assist.ListenerModelHandler;

import java.util.List;
import java.util.Map;

/**
 * When download from resume:
 * taskStart->infoReady->connectStart->connectEnd->
 * (progressBlock(blockIndex,currentOffset)->progress(currentOffset))
 * <-->
 * (progressBlock(blockIndex,currentOffset)->progress(currentOffset))
 * ->blockEnd->taskEnd
 * </p>
 * When download from beginning:
 * taskStart->connectStart->connectEnd->infoReady->
 * (progress(currentOffset)->progressBlock(blockIndex,currentOffset))
 * <-->
 * (progress(currentOffset)->progressBlock(blockIndex,currentOffset))
 * ->blockEnd->taskEnd
 */
public abstract class DownloadListener4 implements DownloadListener,
        Listener4Assist.Listener4Callback, ListenerAssist {

    final Listener4Assist assist;

    DownloadListener4(Listener4Assist assist) {
        this.assist = assist;
        assist.setCallback(this);
    }

    public DownloadListener4() {
        this(new Listener4Assist<>(new Listener4ModelCreator()));
    }

    public void setAssistExtend( Listener4Assist.AssistExtend assistExtend) {
        this.assist.setAssistExtend(assistExtend);
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
    public void connectTrialStart( DownloadTask task,
                                   Map<String, List<String>> requestHeaderFields) {
    }

    @Override
    public void connectTrialEnd( DownloadTask task, int responseCode,
                                 Map<String, List<String>> responseHeaderFields) {
    }

    @Override
    public final void downloadFromBeginning( DownloadTask task,
                                             BreakpointInfo info,
                                             ResumeFailedCause cause) {
        assist.infoReady(task, info, false);
    }

    @Override
    public final void downloadFromBreakpoint( DownloadTask task,
                                              BreakpointInfo info) {
        assist.infoReady(task, info, true);
    }

    @Override
    public void fetchStart( DownloadTask task, int blockIndex, long contentLength) {
    }

    @Override
    public final void fetchProgress( DownloadTask task, int blockIndex,
                                    long increaseBytes) {
        assist.fetchProgress(task, blockIndex, increaseBytes);
    }

    @Override
    public void fetchEnd( DownloadTask task, int blockIndex, long contentLength) {
        assist.fetchEnd(task, blockIndex);
    }

    @Override
    public final void taskEnd( DownloadTask task,  EndCause cause,
                              Exception realCause) {
        assist.taskEnd(task, cause, realCause);
    }

    static class Listener4ModelCreator implements
            ListenerModelHandler.ModelCreator<Listener4Assist.Listener4Model> {
        @Override
        public Listener4Assist.Listener4Model create(int id) {
            return new Listener4Assist.Listener4Model(id);
        }
    }
}
