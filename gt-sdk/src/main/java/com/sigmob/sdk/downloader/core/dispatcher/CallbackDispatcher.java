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

package com.sigmob.sdk.downloader.core.dispatcher;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.sigmob.sdk.downloader.DownloadListener;
import com.sigmob.sdk.downloader.DownloadMonitor;
import com.sigmob.sdk.downloader.DownloadTask;
import com.sigmob.sdk.downloader.FileDownload;
import com.sigmob.sdk.downloader.core.Util;
import com.sigmob.sdk.downloader.core.breakpoint.BreakpointInfo;
import com.sigmob.sdk.downloader.core.cause.EndCause;
import com.sigmob.sdk.downloader.core.cause.ResumeFailedCause;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Dispatch callback to listeners
public class CallbackDispatcher {
    private static final String TAG = "CallbackDispatcher";

    // Just transmit to the main looper.
    private final DownloadListener transmit;

    private final Handler uiHandler;

    CallbackDispatcher(Handler handler, DownloadListener transmit) {
        this.uiHandler = handler;
        this.transmit = transmit;
    }

    public CallbackDispatcher() {
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.transmit = new DefaultTransmitListener(uiHandler);
    }

    public boolean isFetchProcessMoment(DownloadTask task) {
        final long minInterval = task.getMinIntervalMillisCallbackProcess();
        final long now = SystemClock.uptimeMillis();
        return minInterval <= 0
                || now - DownloadTask.TaskHideWrapper
                .getLastCallbackProcessTs(task) >= minInterval;
    }

    public void endTasksWithError(final Collection<DownloadTask> errorCollection,
                                  final Exception realCause) {
        if (errorCollection.size() <= 0) return;

        Util.d(TAG, "endTasksWithError error[" + errorCollection.size() + "] realCause: "
                + realCause);

        final Iterator<DownloadTask> iterator = errorCollection.iterator();
        while (iterator.hasNext()) {
            final DownloadTask task = iterator.next();
            if (!task.isAutoCallbackToUIThread()) {
                DownloadListener listener = task.getListener();
                if (listener != null){
                    listener.taskEnd(task, EndCause.ERROR, realCause);
                }
                iterator.remove();
            }
        }

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (DownloadTask task : errorCollection) {
                    DownloadListener listener = task.getListener();
                    if (listener != null){
                        listener.taskEnd(task, EndCause.ERROR, realCause);
                    }
                }
            }
        });
    }

    public void endTasks(final Collection<DownloadTask> completedTaskCollection,
                         final Collection<DownloadTask> sameTaskConflictCollection,
                         final Collection<DownloadTask> fileBusyCollection) {
        if (completedTaskCollection.size() == 0 && sameTaskConflictCollection.size() == 0
                && fileBusyCollection.size() == 0) {
            return;
        }

        Util.d(TAG, "endTasks completed[" + completedTaskCollection.size()
                + "] sameTask[" + sameTaskConflictCollection.size()
                + "] fileBusy[" + fileBusyCollection.size() + "]");

        if (completedTaskCollection.size() > 0) {
            final Iterator<DownloadTask> iterator = completedTaskCollection.iterator();
            while (iterator.hasNext()) {
                final DownloadTask task = iterator.next();
                if (!task.isAutoCallbackToUIThread()) {
                    DownloadListener listener = task.getListener();
                    if (listener != null){
                        listener.taskEnd(task, EndCause.COMPLETED, null);
                    }
                    iterator.remove();
                }
            }
        }


        if (sameTaskConflictCollection.size() > 0) {
            final Iterator<DownloadTask> iterator = sameTaskConflictCollection.iterator();
            while (iterator.hasNext()) {
                final DownloadTask task = iterator.next();
                if (!task.isAutoCallbackToUIThread()) {
                    DownloadListener listener = task.getListener();
                    if (listener != null){
                        listener.taskEnd(task, EndCause.SAME_TASK_BUSY, null);
                    }
                    iterator.remove();
                }
            }
        }

        if (fileBusyCollection.size() > 0) {
            final Iterator<DownloadTask> iterator = fileBusyCollection.iterator();
            while (iterator.hasNext()) {
                final DownloadTask task = iterator.next();
                if (!task.isAutoCallbackToUIThread()) {
                    DownloadListener listener = task.getListener();
                    if (listener != null){
                        listener.taskEnd(task, EndCause.FILE_BUSY, null);
                    }
                    iterator.remove();
                }
            }
        }

        if (completedTaskCollection.size() == 0 && sameTaskConflictCollection.size() == 0
                && fileBusyCollection.size() == 0) {
            return;
        }

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (DownloadTask task : completedTaskCollection) {
                    DownloadListener listener = task.getListener();
                    if (listener != null){
                        listener.taskEnd(task, EndCause.COMPLETED, null);
                    }
                }
                for (DownloadTask task : sameTaskConflictCollection) {
                    DownloadListener listener = task.getListener();
                    if (listener != null) {
                        listener.taskEnd(task, EndCause.SAME_TASK_BUSY, null);
                    }
                }
                for (DownloadTask task : fileBusyCollection) {
                    DownloadListener listener = task.getListener();
                    if (listener != null){
                        listener.taskEnd(task, EndCause.FILE_BUSY, null);
                    }
                }
            }
        });
    }

    public void endTasksWithCanceled(final Collection<DownloadTask> canceledCollection) {
        if (canceledCollection.size() <= 0) return;

        Util.d(TAG, "endTasksWithCanceled canceled[" + canceledCollection.size() + "]");

        final Iterator<DownloadTask> iterator = canceledCollection.iterator();
        while (iterator.hasNext()) {
            final DownloadTask task = iterator.next();
            if (!task.isAutoCallbackToUIThread()) {
                DownloadListener listener = task.getListener();
                if (listener != null){
                    listener.taskEnd(task, EndCause.CANCELED, null);
                }
                iterator.remove();
            }
        }

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (DownloadTask task : canceledCollection) {
                    DownloadListener listener = task.getListener();
                    if (listener != null) {
                        listener.taskEnd(task, EndCause.CANCELED, null);
                    }
                }
            }
        });
    }

    public DownloadListener dispatch() {
        return transmit;
    }

    static class DefaultTransmitListener implements DownloadListener {

        private final Handler uiHandler;

        DefaultTransmitListener(Handler uiHandler) {
            this.uiHandler = uiHandler;
        }

        @Override
        public void taskStart(final DownloadTask task) {
            Util.d(TAG, "taskStart: " + task.getId());
            inspectTaskStart(task);
            if (task.isAutoCallbackToUIThread()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DownloadListener listener = task.getListener();
                        if (listener != null){
                            listener.taskStart(task);
                        }
                    }
                });
            } else {
                DownloadListener listener = task.getListener();
                if (listener != null) {
                    listener.taskStart(task);
                }
            }
        }

        @Override
        public void connectTrialStart(final DownloadTask task,
                                      final Map<String, List<String>> headerFields) {
            Util.d(TAG, "-----> start trial task(" + task.getId() + ") " + headerFields);
            if (task.isAutoCallbackToUIThread()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DownloadListener listener = task.getListener();
                        if (listener != null) {
                            listener.connectTrialStart(task, headerFields);
                        }
                    }
                });
            } else {
                DownloadListener listener = task.getListener();
                if (listener != null) {
                    listener.connectTrialStart(task, headerFields);
                }
            }
        }

        @Override
        public void connectTrialEnd(final DownloadTask task, final int responseCode,
                                    final Map<String, List<String>> headerFields) {
            Util.d(TAG, "<----- finish trial task(" + task.getId()
                    + ") code[" + responseCode + "]" + headerFields);
            if (task.isAutoCallbackToUIThread()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DownloadListener listener = task.getListener();
                        if (listener != null) {
                            listener.connectTrialEnd(task, responseCode, headerFields);
                        }
                    }
                });
            } else {
                DownloadListener listener = task.getListener();
                if (listener != null) {
                    listener.connectTrialEnd(task, responseCode, headerFields);
                }
            }
        }

        @Override
        public void downloadFromBeginning(final DownloadTask task,
                                          final BreakpointInfo info,
                                          final ResumeFailedCause cause) {
            Util.d(TAG, "downloadFromBeginning: " + task.getId());
            inspectDownloadFromBeginning(task, info, cause);
            if (task.isAutoCallbackToUIThread()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DownloadListener listener = task.getListener();
                        if (listener != null) {
                            listener.downloadFromBeginning(task, info, cause);
                        }
                    }
                });
            } else {
                DownloadListener listener = task.getListener();
                if (listener != null) {
                    listener.downloadFromBeginning(task, info, cause);
                }
            }
        }

        @Override
        public void downloadFromBreakpoint(final DownloadTask task,
                                           final BreakpointInfo info) {
            Util.d(TAG, "downloadFromBreakpoint: " + task.getId());
            inspectDownloadFromBreakpoint(task, info);
            if (task.isAutoCallbackToUIThread()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DownloadListener listener = task.getListener();
                        if (listener != null) {
                            listener.downloadFromBreakpoint(task, info);
                        }
                    }
                });
            } else {
                DownloadListener listener = task.getListener();
                if (listener != null) {
                    listener.downloadFromBreakpoint(task, info);
                }
            }
        }

        @Override
        public void connectStart(final DownloadTask task, final int blockIndex,
                                 final Map<String, List<String>> requestHeaderFields) {
            Util.d(TAG, "-----> start connection task(" + task.getId()
                    + ") block(" + blockIndex + ") " + requestHeaderFields);
            if (task.isAutoCallbackToUIThread()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DownloadListener listener = task.getListener();
                        if (listener != null) {
                            listener.connectStart(task, blockIndex, requestHeaderFields);
                        }
                    }
                });
            } else {
                DownloadListener listener = task.getListener();
                if (listener != null) {
                    listener.connectStart(task, blockIndex, requestHeaderFields);
                }
            }
        }

        @Override
        public void connectEnd(final DownloadTask task, final int blockIndex,
                               final int responseCode,
                               final Map<String, List<String>> requestHeaderFields) {
            Util.d(TAG, "<----- finish connection task(" + task.getId() + ") block("
                    + blockIndex + ") code[" + responseCode + "]" + requestHeaderFields);
            if (task.isAutoCallbackToUIThread()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DownloadListener listener = task.getListener();
                        if (listener != null) {
                            listener.connectEnd(task, blockIndex, responseCode,
                                    requestHeaderFields);
                        }
                    }
                });
            } else {
                DownloadListener listener = task.getListener();
                if (listener != null) {
                    listener.connectEnd(task, blockIndex, responseCode,
                            requestHeaderFields);
                }
            }
        }

        @Override
        public void fetchStart(final DownloadTask task, final int blockIndex,
                               final long contentLength) {
            Util.d(TAG, "fetchStart: " + task.getId());
            if (task.isAutoCallbackToUIThread()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DownloadListener listener = task.getListener();
                        if (listener != null) {
                            listener.fetchStart(task, blockIndex, contentLength);
                        }
                    }
                });
            } else {
                DownloadListener listener = task.getListener();
                if (listener != null) {
                    listener.fetchStart(task, blockIndex, contentLength);
                }
            }
        }

        @Override
        public void fetchProgress(final DownloadTask task, final int blockIndex,
                                  final long increaseBytes) {
            if (task.getMinIntervalMillisCallbackProcess() > 0) {
                DownloadTask.TaskHideWrapper
                        .setLastCallbackProcessTs(task, SystemClock.uptimeMillis());
            }

            if (task.isAutoCallbackToUIThread()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DownloadListener listener = task.getListener();
                        if (listener != null) {
                            listener.fetchProgress(task, blockIndex, increaseBytes);
                        }
                    }
                });
            } else {
                DownloadListener listener = task.getListener();
                if (listener != null) {
                    listener.fetchProgress(task, blockIndex, increaseBytes);
                }
            }
        }

        @Override
        public void fetchEnd(final DownloadTask task, final int blockIndex,
                             final long contentLength) {
            Util.d(TAG, "fetchEnd: " + task.getId());
            if (task.isAutoCallbackToUIThread()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DownloadListener listener = task.getListener();
                        if (listener != null) {
                            listener.fetchEnd(task, blockIndex, contentLength);
                        }
                    }
                });
            } else {
                DownloadListener listener = task.getListener();
                if (listener != null) {
                    listener.fetchEnd(task, blockIndex, contentLength);
                }
            }
        }

        @Override
        public void taskEnd(final DownloadTask task, final EndCause cause,
                            final Exception realCause) {
            if (cause == EndCause.ERROR) {
                // only care about error.
                Util.d(TAG, "taskEnd: " + task.getId() + " " + cause + " " + realCause);
            }
            inspectTaskEnd(task, cause, realCause);
            if (task.isAutoCallbackToUIThread()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DownloadListener listener = task.getListener();
                        if (listener != null){
                            listener.taskEnd(task, cause, realCause);
                        }
                    }
                });
            } else {
                DownloadListener listener = task.getListener();
                if (listener != null){
                    listener.taskEnd(task, cause, realCause);
                }
            }
        }


        void inspectDownloadFromBreakpoint(DownloadTask task,
                                           BreakpointInfo info) {
            final DownloadMonitor monitor = FileDownload.with().getMonitor();
            if (monitor != null) monitor.taskDownloadFromBreakpoint(task, info);
        }

        void inspectDownloadFromBeginning(DownloadTask task,
                                          BreakpointInfo info,
                                          ResumeFailedCause cause) {
            final DownloadMonitor monitor = FileDownload.with().getMonitor();
            if (monitor != null) monitor.taskDownloadFromBeginning(task, info, cause);
        }

        void inspectTaskStart(DownloadTask task) {
            final DownloadMonitor monitor = FileDownload.with().getMonitor();
            if (monitor != null) monitor.taskStart(task);
        }

        void inspectTaskEnd(final DownloadTask task, final EndCause cause,
                            final Exception realCause) {
            final DownloadMonitor monitor = FileDownload.with().getMonitor();
            if (monitor != null) monitor.taskEnd(task, cause, realCause);
        }

    }
}
