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


import android.util.SparseArray;

import com.sigmob.sdk.downloader.core.breakpoint.BreakpointInfo;
import com.sigmob.sdk.downloader.core.cause.EndCause;
import com.sigmob.sdk.downloader.core.cause.ResumeFailedCause;
import com.sigmob.sdk.downloader.core.listener.assist.ListenerAssist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnifiedListenerManager {

    final SparseArray<ArrayList<DownloadListener>> realListenerMap;
    final List<Integer> autoRemoveListenerIdList = new ArrayList<>();

    public UnifiedListenerManager() {
        realListenerMap = new SparseArray<>();
    }

    public synchronized void detachListener(int id) {
        realListenerMap.remove(id);
    }

    public synchronized void addAutoRemoveListenersWhenTaskEnd(int id) {
        if (autoRemoveListenerIdList.contains(id)) return;
        autoRemoveListenerIdList.add(id);
    }

    public synchronized void removeAutoRemoveListenersWhenTaskEnd(int id) {
        autoRemoveListenerIdList.remove((Integer) id);
    }

    public synchronized void detachListener(DownloadListener listener) {
        final int count = realListenerMap.size();

        final List<Integer> needRemoveKeyList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final List<DownloadListener> listenerList = realListenerMap.valueAt(i);
            if (listenerList == null) continue;
            listenerList.remove(listener);

            if (listenerList.isEmpty()) needRemoveKeyList.add(realListenerMap.keyAt(i));
        }

        for (int key : needRemoveKeyList) {
            realListenerMap.remove(key);
        }
    }

    public synchronized boolean detachListener(DownloadTask task,
                                               DownloadListener listener) {
        final int id = task.getId();
        final List<DownloadListener> listenerList = realListenerMap.get(id);

        if (listenerList == null) return false;

        boolean result = listenerList.remove(listener);
        if (listenerList.isEmpty()) realListenerMap.remove(id);

        return result;
    }

    public synchronized void attachListener(DownloadTask task,
                                            DownloadListener listener) {
        final int id = task.getId();
        ArrayList<DownloadListener> listenerList = realListenerMap.get(id);
        if (listenerList == null) {
            listenerList = new ArrayList<>();
            realListenerMap.put(id, listenerList);
        }

        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
            if (listener instanceof ListenerAssist) {
                ((ListenerAssist) listener).setAlwaysRecoverAssistModelIfNotSet(true);
            }
        }
    }

    /**
     * Attach the {@code listener} to this manager and enqueue the task if it isn't pending or
     * running.
     *
     * @param task     the task will be enqueue if it isn't running.
     * @param listener the listener will be attach to this manager.
     */
    public synchronized void attachAndEnqueueIfNotRun(DownloadTask task,
                                                      DownloadListener listener) {
        attachListener(task, listener);

        if (!isTaskPendingOrRunning(task)) {
            task.enqueue(hostListener);
        }
    }

    public synchronized void enqueueTaskWithUnifiedListener(DownloadTask task,
                                                            DownloadListener listener) {
        attachListener(task, listener);

        task.enqueue(hostListener);
    }

    /**
     * Attach the {@code listener} to this manager and execute the {@code task}.
     *
     * @param task     the task will be execute.
     * @param listener the listener will be attached to this manager.
     */
    public synchronized void executeTaskWithUnifiedListener(DownloadTask task,
                                                            DownloadListener listener) {
        attachListener(task, listener);

        task.execute(hostListener);
    }


    public DownloadListener getHostListener() {
        return hostListener;
    }

    // convenient for unit-test.
    boolean isTaskPendingOrRunning(DownloadTask task) {
        return StatusUtil.isSameTaskPendingOrRunning(task);
    }

    final DownloadListener hostListener = new DownloadListener() {
        @Override
        public void taskStart(DownloadTask task) {
            final DownloadListener[] listeners = getThreadSafeArray(task, realListenerMap);
            if (listeners == null) return;

            for (final DownloadListener realOne : listeners) {
                if (realOne == null) continue;
                realOne.taskStart(task);
            }
        }

        @Override
        public void connectTrialStart(DownloadTask task,
                                      Map<String, List<String>> requestHeaderFields) {
            final DownloadListener[] listeners = getThreadSafeArray(task, realListenerMap);
            if (listeners == null) return;

            for (final DownloadListener realOne : listeners) {
                if (realOne == null) continue;
                realOne.connectTrialStart(task, requestHeaderFields);
            }
        }

        @Override
        public void connectTrialEnd(DownloadTask task, int responseCode,
                                    Map<String, List<String>> responseHeaderFields) {
            final DownloadListener[] listeners = getThreadSafeArray(task, realListenerMap);
            if (listeners == null) return;

            for (final DownloadListener realOne : listeners) {
                if (realOne == null) continue;
                realOne.connectTrialEnd(task, responseCode, responseHeaderFields);
            }
        }

        @Override
        public void downloadFromBeginning(DownloadTask task, BreakpointInfo info,
                                          ResumeFailedCause cause) {
            final DownloadListener[] listeners = getThreadSafeArray(task, realListenerMap);
            if (listeners == null) return;

            for (final DownloadListener realOne : listeners) {
                if (realOne == null) continue;
                realOne.downloadFromBeginning(task, info, cause);
            }

        }

        @Override
        public void downloadFromBreakpoint(DownloadTask task,
                                           BreakpointInfo info) {
            final DownloadListener[] listeners = getThreadSafeArray(task, realListenerMap);
            if (listeners == null) return;

            for (final DownloadListener realOne : listeners) {
                if (realOne == null) continue;
                realOne.downloadFromBreakpoint(task, info);
            }
        }

        @Override
        public void connectStart(DownloadTask task, int blockIndex,
                                 Map<String, List<String>> requestHeaderFields) {
            final DownloadListener[] listeners = getThreadSafeArray(task, realListenerMap);
            if (listeners == null) return;

            for (final DownloadListener realOne : listeners) {
                if (realOne == null) continue;
                realOne.connectStart(task, blockIndex, requestHeaderFields);
            }
        }

        @Override
        public void connectEnd(DownloadTask task, int blockIndex, int responseCode,
                               Map<String, List<String>> responseHeaderFields) {
            final DownloadListener[] listeners = getThreadSafeArray(task, realListenerMap);
            if (listeners == null) return;

            for (final DownloadListener realOne : listeners) {
                if (realOne == null) continue;
                realOne.connectEnd(task, blockIndex, responseCode, responseHeaderFields);
            }
        }

        @Override
        public void fetchStart(DownloadTask task, int blockIndex, long contentLength) {
            final DownloadListener[] listeners = getThreadSafeArray(task, realListenerMap);
            if (listeners == null) return;

            for (final DownloadListener realOne : listeners) {
                if (realOne == null) continue;
                realOne.fetchStart(task, blockIndex, contentLength);
            }

        }

        @Override
        public void fetchProgress(DownloadTask task, int blockIndex, long increaseBytes) {
            final DownloadListener[] listeners = getThreadSafeArray(task, realListenerMap);
            if (listeners == null) return;

            for (final DownloadListener realOne : listeners) {
                if (realOne == null) continue;
                realOne.fetchProgress(task, blockIndex, increaseBytes);
            }
        }

        @Override
        public void fetchEnd(DownloadTask task, int blockIndex, long contentLength) {
            final DownloadListener[] listeners = getThreadSafeArray(task, realListenerMap);
            if (listeners == null) return;

            for (final DownloadListener realOne : listeners) {
                if (realOne == null) continue;
                realOne.fetchEnd(task, blockIndex, contentLength);
            }
        }

        @Override
        public void taskEnd(DownloadTask task, EndCause cause,
                            Exception realCause) {
            final DownloadListener[] listeners = getThreadSafeArray(task, realListenerMap);
            if (listeners == null) return;

            for (final DownloadListener realOne : listeners) {
                if (realOne == null) continue;
                realOne.taskEnd(task, cause, realCause);
            }

            if (autoRemoveListenerIdList.contains(task.getId())) {
                detachListener(task.getId());
            }
        }
    };

    private static DownloadListener[] getThreadSafeArray(DownloadTask task,
                                                         SparseArray<ArrayList<DownloadListener>>
                                                                 realListenerMap) {
        final ArrayList<DownloadListener> listenerList = realListenerMap.get(task.getId());
        if (listenerList == null || listenerList.size() <= 0) return null;

        final DownloadListener[] copyList = new DownloadListener[listenerList.size()];
        listenerList.toArray(copyList);
        return copyList;
    }
}
