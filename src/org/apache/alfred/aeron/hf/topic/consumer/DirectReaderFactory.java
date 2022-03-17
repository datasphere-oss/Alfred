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

package org.apache.alfred.aeron.hf.topic.consumer;

import org.apache.alfred.streaming.MessageSource;
import org.apache.alfred.timebase.messages.InstrumentMessage;
import org.apache.alfred.aeron.hf.pub.TypeLoader;
import org.apache.alfred.aeron.hf.pub.TypeLoaderImpl;
import org.apache.alfred.aeron.hf.pub.codec.CodecFactory;
import org.apache.alfred.aeron.hf.pub.md.RecordClassDescriptor;
import org.apache.alfred.aeron.hf.tickdb.pub.topic.MessagePoller;
import org.apache.alfred.aeron.hf.tickdb.pub.topic.MessageProcessor;
import org.apache.alfred.util.io.idlestrat.IdleStrategy;
import org.apache.alfred.util.io.idlestrat.YieldingIdleStrategy;
import io.aeron.Aeron;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Alexei Osipov
 */
@ParametersAreNonnullByDefault
public class DirectReaderFactory {
    private final CodecFactory codecFactory;
    private final TypeLoader typeLoader;

    public DirectReaderFactory() {
        this(CodecFactory.newCompiledCachingFactory());
    }

    public DirectReaderFactory(CodecFactory codecFactory) {
        this(codecFactory, TypeLoaderImpl.DEFAULT_INSTANCE);
    }

    public DirectReaderFactory(CodecFactory codecFactory, TypeLoader typeLoader) {
        this.codecFactory = codecFactory;
        this.typeLoader = typeLoader;
    }

    public SubscriptionWorker createListener(Aeron aeron, boolean raw, String channel, int dataStreamId,
            List<RecordClassDescriptor> types, MessageProcessor processor, @Nullable IdleStrategy idleStrategy,
            MappingProvider mappingProvider) {
        if (idleStrategy == null) {
            idleStrategy = new YieldingIdleStrategy();
        }
        return new DirectMessageListenerProcessor(processor, aeron, raw, channel, dataStreamId, codecFactory,
                typeLoader, types, idleStrategy, mappingProvider);
    }

    public MessagePoller createPoller(Aeron aeron, boolean raw, String channel, int dataStreamId,
            List<RecordClassDescriptor> types, MappingProvider mappingProvider) {
        return new DirectMessageNonblockingPoller(aeron, raw, channel, dataStreamId, types, codecFactory, typeLoader,
                mappingProvider);
    }

    public MessageSource<InstrumentMessage> createMessageSource(Aeron aeron, boolean raw, String channel,
            int dataStreamId, List<RecordClassDescriptor> types, @Nullable IdleStrategy idleStrategy,
            MappingProvider mappingProvider) {
        if (idleStrategy == null) {
            idleStrategy = new YieldingIdleStrategy();
        }
        return new DirectMessageSource(aeron, raw, channel, dataStreamId, codecFactory, typeLoader, types, idleStrategy,
                mappingProvider);
    }
}
