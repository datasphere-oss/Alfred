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

package org.apache.alfred.storage.codecs;

import org.apache.alfred.util.collections.generated.*;

/**
 *
 */
public abstract class BlockCompressor {

    private final ByteArrayList buffer;

    public BlockCompressor(ByteArrayList buffer) {
        this.buffer = buffer;
    }

    public abstract byte code();

    public ByteArrayList getReusableBuffer() {
        return buffer;
    }

    public abstract int deflate(byte[] src, int offset, int length, ByteArrayList appendTo);
}