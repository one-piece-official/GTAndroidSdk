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

package com.sigmob.sdk.downloader.core.breakpoint;

import android.content.Context;

import com.sigmob.sdk.downloader.DownloadTask;
import com.sigmob.sdk.downloader.core.Util;
import com.sigmob.sdk.downloader.core.cause.EndCause;

import java.io.IOException;

public class BreakpointStoreOnSQLite implements DownloadStore {

    private static final String TAG = "BreakpointStoreOnSQLite";
    protected final BreakpointSQLiteHelper helper;
    protected final BreakpointStoreOnCache onCache;

    BreakpointStoreOnSQLite(BreakpointSQLiteHelper helper, BreakpointStoreOnCache onCache) {
        this.helper = helper;
        this.onCache = onCache;
    }

    public BreakpointStoreOnSQLite(Context context) {
        this.helper = new BreakpointSQLiteHelper(context.getApplicationContext());
        this.onCache = new BreakpointStoreOnCache(helper.loadToCache(),
                helper.loadDirtyFileList(),
                helper.loadResponseFilenameToMap());
    }


    @Override
    public BreakpointInfo get(int id) {
        return onCache.get(id);
    }


    @Override
    public BreakpointInfo createAndInsert(DownloadTask task)
            throws IOException {
        final BreakpointInfo info = onCache.createAndInsert(task);
        helper.insert(info);
        return info;
    }

    @Override
    public void onTaskStart(int id) {
        onCache.onTaskStart(id);
    }

    @Override
    public void onSyncToFilesystemSuccess(BreakpointInfo info, int blockIndex,
                                          long increaseLength) throws IOException {
        onCache.onSyncToFilesystemSuccess(info, blockIndex, increaseLength);
        final long newCurrentOffset = info.getBlock(blockIndex).getCurrentOffset();
        helper.updateBlockIncrease(info, blockIndex, newCurrentOffset);
    }

    @Override
    public boolean update(BreakpointInfo breakpointInfo) throws IOException {
        final boolean result = onCache.update(breakpointInfo);
        helper.updateInfo(breakpointInfo);
        final String filename = breakpointInfo.getFilename();
        Util.d(TAG, "update " + breakpointInfo);
        if (breakpointInfo.isTaskOnlyProvidedParentPath() && filename != null) {
            helper.updateFilename(breakpointInfo.getUrl(), filename);
        }
        return result;
    }

    @Override
    public void onTaskEnd(int id, EndCause cause, Exception exception) {
        onCache.onTaskEnd(id, cause, exception);
        if (cause == EndCause.COMPLETED) {
            helper.removeInfo(id);
        }
    }


    @Override
    public BreakpointInfo getAfterCompleted(int id) {
        return null;
    }

    @Override
    public boolean markFileDirty(int id) {
        if (onCache.markFileDirty(id)) {
            helper.markFileDirty(id);
            return true;
        }

        return false;
    }

    @Override
    public boolean markFileClear(int id) {
        if (onCache.markFileClear(id)) {
            helper.markFileClear(id);
            return true;
        }

        return false;
    }

    @Override
    public void remove(int id) {
        onCache.remove(id);
        helper.removeInfo(id);
    }

    @Override
    public int findOrCreateId(DownloadTask task) {
        return onCache.findOrCreateId(task);
    }


    @Override
    public BreakpointInfo findAnotherInfoFromCompare(DownloadTask task,
                                                     BreakpointInfo ignored) {
        return onCache.findAnotherInfoFromCompare(task, ignored);
    }

    @Override
    public boolean isOnlyMemoryCache() {
        return false;
    }

    @Override
    public boolean isFileDirty(int id) {
        return onCache.isFileDirty(id);
    }


    @Override
    public String getResponseFilename(String url) {
        return onCache.getResponseFilename(url);
    }

    void close() {
        helper.close();
    }


    public DownloadStore createRemitSelf() {
        return new RemitStoreOnSQLite(this);
    }
}
