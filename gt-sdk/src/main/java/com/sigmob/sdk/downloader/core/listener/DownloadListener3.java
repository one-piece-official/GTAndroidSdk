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


import com.sigmob.sdk.downloader.DownloadTask;
import com.sigmob.sdk.downloader.core.Util;
import com.sigmob.sdk.downloader.core.cause.EndCause;
import com.sigmob.sdk.downloader.core.listener.assist.Listener1Assist;

/**
 * started->connected->progress<-->progress(currentOffset)-> completed/canceled/error/warn
 */
public abstract class DownloadListener3 extends DownloadListener1 {
    @Override
    public final void taskStart( DownloadTask task,
                                 Listener1Assist.Listener1Model model) {
        started(task);
    }

    @Override
    public void taskEnd( DownloadTask task,  EndCause cause,
                        Exception realCause,
                         Listener1Assist.Listener1Model model) {
        switch (cause) {
            case COMPLETED:
                completed(task);
                break;
            case CANCELED:
                canceled(task);
                break;
            case ERROR:
            case PRE_ALLOCATE_FAILED:
                error(task, realCause);
                break;
            case FILE_BUSY:
            case SAME_TASK_BUSY:
                warn(task);
                break;
            default:
                Util.w("DownloadListener3", "Don't support " + cause);
        }
    }

    protected abstract void started( DownloadTask task);

    protected abstract void completed( DownloadTask task);

    protected abstract void canceled( DownloadTask task);

    protected abstract void error( DownloadTask task,  Exception e);

    protected abstract void warn( DownloadTask task);
}
