/*******************************************************************************
 *
 *
 *  Copyright (c) 2022 Alfred
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package org.apache.alfred.storage.raw;

/**
 *
 */
public class RawEntityIndexEntry {
    private final int entity;
    int firstChildIdx;
    int lastChildIdx;

    public RawEntityIndexEntry(int entity, int firstChildIdx, int lastChildIdx) {
        this.entity = entity;
        this.firstChildIdx = firstChildIdx;
        this.lastChildIdx = lastChildIdx;
    }

    public int getEntity() {
        return entity;
    }

    public int getFirstChildIdx() {
        return firstChildIdx;
    }

    public int getLastChildIdx() {
        return lastChildIdx;
    }
}
