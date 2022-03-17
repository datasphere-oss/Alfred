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

package org.apache.alfred.aeron.hf.topic.loader;

import com.google.common.annotations.VisibleForTesting;
import org.apache.alfred.streaming.MessageChannel;
import org.apache.alfred.util.memory.MemoryDataOutput;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * {@link MessageChannel} that accepts {@link MemoryDataOutput} as input and
 * writes it's content to a provided {@link OutputStream}.
 *
 * @author Alexei Osipov
 */
@VisibleForTesting
public class MemoryDataOutputStreamChannel implements MessageChannel<MemoryDataOutput> {
    private final DataOutputStream outputStream;

    public MemoryDataOutputStreamChannel(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(outputStream);
    }

    @Override
    public void send(MemoryDataOutput msg) {
        try {
            int length = msg.getPosition();
            outputStream.writeInt(length);
            outputStream.write(msg.getBuffer(), 0, length);
            outputStream.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        // TODO: Review
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
