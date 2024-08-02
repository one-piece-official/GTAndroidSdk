/*
 * Copyright (c) 2018 LingoChamp Inc.
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


import com.sigmob.sdk.downloader.core.cause.EndCause;

import java.io.IOException;

public interface DownloadStore extends BreakpointStore {

    void onSyncToFilesystemSuccess(BreakpointInfo info, int blockIndex,
                                   long increaseLength) throws IOException;

    void onTaskStart(int id);

    void onTaskEnd(int id, EndCause cause, Exception exception);

    /**
     * Get the breakpoint info after the {@code id} task has been completed, this function is just
     * for ignore real operation and just return {@code null} directly since on some logic model we
     * already delete info from store after task has been completed to enhance performance.
     */

    BreakpointInfo getAfterCompleted(int id);

    /**
     * Mark the file relate to the {@code id} is dirty state.
     *
     * @param id the task id
     */
    boolean markFileDirty(int id);

    /**
     * Mark the file relate to the {@code id} is clear state.
     * <p>
     * Normally, which means the task is completed download.
     *
     * @param id the task id
     */
    boolean markFileClear(int id);
}