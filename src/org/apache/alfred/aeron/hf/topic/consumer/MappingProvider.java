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

import org.apache.alfred.timebase.messages.ConstantIdentityKey;
import org.apache.alfred.util.collections.generated.IntegerToObjectHashMap;

/**
 * @author Alexei Osipov
 */
public interface MappingProvider {
    /**
     * Returns snapshot of current index to InstrumentKey mapping for the topic.
     *
     * Position in the array represents entity index. Position 0 means entity index
     * 0.
     */
    ConstantIdentityKey[] getMappingSnapshot();

    /**
     * Returns snapshot of current index to InstrumentKey mapping for the topic.
     * Result contains a mapping between indexes and entities. May include zero,
     * one, multiple or all temporary mapping entries depending on server
     * heuristics. If the result does not contain needed entry then consumer should
     * stop.
     *
     * @param neededTempEntityIndex entity index that need to be decoded
     * @return mapping
     */
    IntegerToObjectHashMap<ConstantIdentityKey> getTempMappingSnapshot(int neededTempEntityIndex);
}
