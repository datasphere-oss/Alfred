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

package org.apache.alfred.storage.tfs;

public class NextState {

    public static final int NONE = 0;
    public static final int HAS_CURRENT = 1;
    public static final int HAS_MORE = 2;
    public static final int HAS_ALL = 3;

    public static boolean hasMore(int state) {
        return (state & HAS_MORE) == HAS_MORE;
    }

    public static boolean hasCurrent(int state) {
        return (state & HAS_CURRENT) == HAS_CURRENT;
    }
}
