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

package org.apache.alfred.engine.data.stream;

import org.apache.alfred.engine.streaming.MessageChannel;
import org.apache.alfred.engine.timebase.messages.TimeStampedMessage;

/**
 *
 */
public class TimeSortFilter<T extends TimeStampedMessage> extends MessageFork<T> {
    private long lastTimestamp = Long.MIN_VALUE;

    public TimeSortFilter(MessageChannel<T> acceptedMessageConsumer, MessageChannel<T> rejectedMessageConsumer) {
        super(acceptedMessageConsumer, rejectedMessageConsumer);
    }

    @Override
    protected boolean accept(T message) {
        final long ts = message.getTimeStampMs();
        final boolean ordered = ts >= lastTimestamp;

        if (ordered)
            lastTimestamp = ts;

        return (ordered);
    }
}
