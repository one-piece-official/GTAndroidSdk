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


import android.util.SparseArray;

import com.sigmob.sdk.downloader.DownloadTask;

import java.util.HashMap;

public class KeyToIdMap {

    @SuppressWarnings("PMD.AvoidFieldNameMatchingTypeName")
    
    private final HashMap<String, Integer> keyToIdMap;
    
    private final SparseArray<String> idToKeyMap;

    KeyToIdMap() {
        this(new HashMap<String, Integer>(), new SparseArray<String>());
    }

    KeyToIdMap( HashMap<String, Integer> keyToIdMap,
                SparseArray<String> idToKeyMap) {
        this.keyToIdMap = keyToIdMap;
        this.idToKeyMap = idToKeyMap;
    }

    public Integer get( DownloadTask task) {
        final Integer candidate = keyToIdMap.get(generateKey(task));
        if (candidate != null) return candidate;
        return null;
    }

    public void remove(int id) {
        final String key = idToKeyMap.get(id);
        if (key != null) {
            keyToIdMap.remove(key);
            idToKeyMap.remove(id);
        }
    }

    public void add( DownloadTask task, int id) {
        final String key = generateKey(task);
        keyToIdMap.put(key, id);
        idToKeyMap.put(id, key);
    }

    String generateKey( DownloadTask task) {
        return task.getUrl() + task.getUri() + task.getFilename();
    }
}
