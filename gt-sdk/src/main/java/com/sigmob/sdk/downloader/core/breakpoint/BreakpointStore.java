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


import com.sigmob.sdk.downloader.DownloadTask;

import java.io.IOException;

public interface BreakpointStore {


    BreakpointInfo get(int id);


    BreakpointInfo createAndInsert(DownloadTask task) throws IOException;

    int findOrCreateId(DownloadTask task);

    boolean update(BreakpointInfo breakpointInfo) throws IOException;

    void remove(int id);


    String getResponseFilename(String url);


    BreakpointInfo findAnotherInfoFromCompare(DownloadTask task,
                                              BreakpointInfo ignored);

    /**
     * Whether only store breakpoint on memory cache.
     *
     * @return {@code true} if breakpoint on this store is only store on the memory cache.
     */
    boolean isOnlyMemoryCache();

    /**
     * Whether the file relate to the task id {@code id} is dirty, which means the file isn't
     * complete download yet.
     *
     * @param id the task id.
     * @return {@code true} the file relate to {@code id} is dirty
     */
    boolean isFileDirty(int id);
}
