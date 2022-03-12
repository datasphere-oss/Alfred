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

import org.apache.alfred.util.collections.ByteArray;
import org.apache.alfred.util.io.IOUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public abstract class BlockDecompressor {
    // private Logger logger = LoggerFactory.getLogger("deltix.dtb");

    protected BlockDecompressor() {
    }

    // public BlockDecompressor(ByteArrayHeap heap) {
    // this.heap = heap;
    // }

    public void inflate(InputStream is, int inLength, byte[] out, int outOffset, int outLength) throws IOException {
        if (inLength == 0)
            return;

        ByteArray buffer = new ByteArray(new byte[inLength]);

        assert buffer.getLength() >= inLength;

        IOUtil.readFully(is, buffer.getArray(), buffer.getOffset(), inLength);
        inflate(buffer.getArray(), buffer.getOffset(), inLength, out, outOffset, outLength);
    }

    public abstract void inflate(byte[] src, int srcOffset, int srcLength, byte[] out, int outOffset, int outLength);
}
