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

import org.apache.alfred.engine.messages.InstrumentMessage;
import org.apache.alfred.storage.hf.tickdb.lang.compiler.sem.PredicateCompiler;
import org.apache.alfred.storage.hf.tickdb.lang.pub.CompilerUtil;
import org.apache.alfred.storage.hf.tickdb.lang.runtime.MessagePredicate;
import org.apache.alfred.storage.hf.tickdb.pub.TickStream;
import org.apache.alfred.util.memory.MemoryDataOutput;

/**
 *
 */
public class FilteredEncoder<T extends InstrumentMessage> implements MessageEncoder<T> {
    private MessagePredicate filter;
    private MessageEncoder<T> encoder;

    public FilteredEncoder(TickStream stream, String filterExpression, MessageEncoder<T> encoder) {
        if (filterExpression != null) {
            PredicateCompiler pc = stream.isPolymorphic() ? new PredicateCompiler(stream.getPolymorphicDescriptors())
                    : new PredicateCompiler(stream.getFixedType());

            this.filter = pc.compile(CompilerUtil.parseExpression(filterExpression));
        }
        this.encoder = encoder;
    }

    @Override
    public boolean encode(T message, MemoryDataOutput out) {
        encoder.encode(message, out);

        if (filter != null)
            return filter.accept(message, getTypeIndex(), out.getBuffer(), encoder.getContentOffset(),
                    out.getSize() - getContentOffset());

        return true;
    }

    @Override
    public int getContentOffset() {
        return encoder.getContentOffset();
    }

    @Override
    public int getTypeIndex() {
        return encoder.getTypeIndex();
    }
}
