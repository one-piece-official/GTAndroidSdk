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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DownloadListenerBunch implements DownloadListener {

    
    final DownloadListener[] listenerList;

    DownloadListenerBunch( DownloadListener[] listenerList) {
        this.listenerList = listenerList;
    }

    @Override
    public void taskStart( DownloadTask task) {
        for (DownloadListener listener : listenerList) {
            listener.taskStart(task);
        }
    }

    @Override
    public void connectTrialStart( DownloadTask task,
                                   Map<String, List<String>> requestHeaderFields) {
        for (DownloadListener listener : listenerList) {
            listener.connectTrialStart(task, requestHeaderFields);
        }
    }

    @Override
    public void connectTrialEnd( DownloadTask task, int responseCode,
                                 Map<String, List<String>> responseHeaderFields) {
        for (DownloadListener listener : listenerList) {
            listener.connectTrialEnd(task, responseCode, responseHeaderFields);
        }
    }

    @Override
    public void downloadFromBeginning( DownloadTask task,  BreakpointInfo info,
                                       ResumeFailedCause cause) {
        for (DownloadListener listener : listenerList) {
            listener.downloadFromBeginning(task, info, cause);
        }
    }

    @Override
    public void downloadFromBreakpoint( DownloadTask task,  BreakpointInfo info) {
        for (DownloadListener listener : listenerList) {
            listener.downloadFromBreakpoint(task, info);
        }
    }

    @Override
    public void connectStart( DownloadTask task, int blockIndex,
                              Map<String, List<String>> requestHeaderFields) {
        for (DownloadListener listener : listenerList) {
            listener.connectStart(task, blockIndex, requestHeaderFields);
        }
    }

    @Override
    public void connectEnd( DownloadTask task, int blockIndex, int responseCode,
                            Map<String, List<String>> responseHeaderFields) {
        for (DownloadListener listener : listenerList) {
            listener.connectEnd(task, blockIndex, responseCode, responseHeaderFields);
        }
    }

    @Override
    public void fetchStart( DownloadTask task, int blockIndex, long contentLength) {
        for (DownloadListener listener : listenerList) {
            listener.fetchStart(task, blockIndex, contentLength);
        }
    }

    @Override
    public void fetchProgress( DownloadTask task, int blockIndex, long increaseBytes) {
        for (DownloadListener listener : listenerList) {
            listener.fetchProgress(task, blockIndex, increaseBytes);
        }
    }

    @Override
    public void fetchEnd( DownloadTask task, int blockIndex, long contentLength) {
        for (DownloadListener listener : listenerList) {
            listener.fetchEnd(task, blockIndex, contentLength);
        }
    }

    @Override
    public void taskEnd( DownloadTask task,  EndCause cause,
                        Exception realCause) {
        for (DownloadListener listener : listenerList) {
            listener.taskEnd(task, cause, realCause);
        }
    }

    public boolean contain(DownloadListener targetListener) {
        for (DownloadListener listener : listenerList) {
            if (listener == targetListener) return true;
        }

        return false;
    }

    /**
     * Get the index of {@code targetListener}, smaller index, earlier to receive callback.
     *
     * @param targetListener used for compare and get it's index on the bunch.
     * @return {@code -1} if can't find {@code targetListener} on the bunch, otherwise the index of
     * the {@code targetListener} on the bunch.
     */
    public int indexOf(DownloadListener targetListener) {
        for (int index = 0; index < listenerList.length; index++) {
            final DownloadListener listener = listenerList[index];
            if (listener == targetListener) return index;
        }

        return -1;
    }

    public static class Builder {

        private List<DownloadListener> listenerList = new ArrayList<>();

        public DownloadListenerBunch build() {
            return new DownloadListenerBunch(
                    listenerList.toArray(new DownloadListener[listenerList.size()]));
        }

        /**
         * Append {@code listener} to the end of bunch listener list. Then the {@code listener} will
         * listener the callbacks of the host bunch listener attached.
         *
         * @param listener will be appended to the end of bunch listener list. if it's {@code null},
         *                 it will not be appended.
         */
        public Builder append(DownloadListener listener) {
            if (listener != null && !listenerList.contains(listener)) {
                listenerList.add(listener);
            }

            return this;
        }

        public boolean remove(DownloadListener listener) {
            return listenerList.remove(listener);
        }
    }
}
