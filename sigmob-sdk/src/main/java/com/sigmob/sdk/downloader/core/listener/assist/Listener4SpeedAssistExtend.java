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

package com.sigmob.sdk.downloader.core.listener.assist;


import android.util.SparseArray;

import com.sigmob.sdk.downloader.DownloadTask;
import com.sigmob.sdk.downloader.SpeedCalculator;
import com.sigmob.sdk.downloader.core.breakpoint.BlockInfo;
import com.sigmob.sdk.downloader.core.breakpoint.BreakpointInfo;
import com.sigmob.sdk.downloader.core.cause.EndCause;

//@SuppressFBWarnings(value = "BC")
public class Listener4SpeedAssistExtend implements Listener4Assist.AssistExtend,
        ListenerModelHandler.ModelCreator<Listener4SpeedAssistExtend.Listener4SpeedModel> {

    private Listener4SpeedCallback callback;

    public void setCallback(Listener4SpeedCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean dispatchInfoReady(DownloadTask task, BreakpointInfo info,
                                     boolean fromBreakpoint,
                                     Listener4Assist.Listener4Model model) {
        if (callback != null) {
            callback.infoReady(task, info, fromBreakpoint, (Listener4SpeedModel) model);
        }
        return true;
    }

    @Override
    public boolean dispatchFetchProgress(DownloadTask task, int blockIndex,
                                         long increaseBytes,
                                         Listener4Assist.Listener4Model model) {
        final Listener4SpeedModel speedModel = (Listener4SpeedModel) model;

        speedModel.blockSpeeds.get(blockIndex).downloading(increaseBytes);
        speedModel.taskSpeed.downloading(increaseBytes);

        if (callback != null) {
            callback.progressBlock(task, blockIndex, model.blockCurrentOffsetMap.get(blockIndex),
                    speedModel.getBlockSpeed(blockIndex));
            callback.progress(task, model.currentOffset, speedModel.taskSpeed);
        }

        return true;
    }

    @Override
    public boolean dispatchBlockEnd(DownloadTask task, int blockIndex,
                                    Listener4Assist.Listener4Model model) {
        final Listener4SpeedModel speedModel = (Listener4SpeedModel) model;

        speedModel.blockSpeeds.get(blockIndex).endTask();

        if (callback != null) {
            callback.blockEnd(task, blockIndex, model.info.getBlock(blockIndex),
                    speedModel.getBlockSpeed(blockIndex));
        }

        return true;
    }

    @Override
    public boolean dispatchTaskEnd(DownloadTask task, EndCause cause, Exception realCause,
                                   Listener4Assist.Listener4Model model) {
        final Listener4SpeedModel speedModel = (Listener4SpeedModel) model;
        final SpeedCalculator speedCalculator;
        if (speedModel.taskSpeed != null) {
            speedCalculator = speedModel.taskSpeed;
            speedCalculator.endTask();
        } else {
            speedCalculator = new SpeedCalculator();
        }

        if (callback != null) {
            callback.taskEnd(task, cause, realCause, speedCalculator);
        }

        return true;
    }

    @Override
    public Listener4SpeedModel create(int id) {
        return new Listener4SpeedModel(id);
    }

    public static class Listener4SpeedModel extends Listener4Assist.Listener4Model {
        SpeedCalculator taskSpeed;
        SparseArray<SpeedCalculator> blockSpeeds;

        public SpeedCalculator getTaskSpeed() {
            return taskSpeed;
        }

        public SpeedCalculator getBlockSpeed(int blockIndex) {
            return blockSpeeds.get(blockIndex);
        }

        public Listener4SpeedModel(int id) {
            super(id);
        }

        @Override
        public void onInfoValid(BreakpointInfo info) {
            super.onInfoValid(info);
            this.taskSpeed = new SpeedCalculator();
            this.blockSpeeds = new SparseArray<>();

            final int blockCount = info.getBlockCount();
            for (int i = 0; i < blockCount; i++) {
                blockSpeeds.put(i, new SpeedCalculator());
            }
        }
    }

    public interface Listener4SpeedCallback {
        void infoReady(DownloadTask task, BreakpointInfo info,
                       boolean fromBreakpoint,
                       Listener4SpeedModel model);

        void progressBlock(DownloadTask task, int blockIndex, long currentBlockOffset,
                           SpeedCalculator blockSpeed);

        void progress(DownloadTask task, long currentOffset,
                      SpeedCalculator taskSpeed);

        void blockEnd(DownloadTask task, int blockIndex, BlockInfo info,
                      SpeedCalculator blockSpeed);

        void taskEnd(DownloadTask task, EndCause cause,
                     Exception realCause,
                     SpeedCalculator taskSpeed);
    }
}
