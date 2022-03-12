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

import org.apache.alfred.storage.codecs.TSNames;
import org.apache.alfred.storage.impl.*;

/**
 *
 */
public class RawFolderEntry {
    private final boolean isFile;
    private final int id;
    private final long startTimestamp;
    private final int idxInParent;

    public RawFolderEntry(int idxInParent, boolean isFile, int id, long startTimestamp) {
        this.idxInParent = idxInParent;
        this.isFile = isFile;
        this.id = id;
        this.startTimestamp = startTimestamp;
    }

    public int getIdxInParent() {
        return idxInParent;
    }

    public boolean isFile() {
        return isFile;
    }

    public int getId() {
        return id;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public String getName() {
        return (isFile ? TSNames.buildFileName(id) : TSNames.buildFolderName(id));
    }
}
