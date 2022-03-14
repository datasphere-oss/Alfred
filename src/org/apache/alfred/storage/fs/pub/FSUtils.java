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

package org.apache.alfred.storage.fs.pub;

import org.apache.alfred.storage.fs.local.LocalFS;
import org.apache.alfred.util.collections.Visitor;
import org.apache.alfred.util.lang.Wrapper;

import java.io.*;

/**
 *
 */
public class FSUtils {
    public static boolean removeRecursive(AbstractPath path, boolean inclusive, Visitor<? super AbstractPath> preDelete)
            throws IOException {
        if (path.isFolder()) {
            for (String name : path.listFolder())
                if (!removeRecursive(path.append(name), true, preDelete))
                    return (false);
        }

        if (inclusive) {
            if (preDelete != null)
                if (!preDelete.visit(path))
                    return (false);

            path.deleteIfExists();
        }

        return (true);
    }

    // public static FileSystem getFileSystem(AbstractPath path) {
    // if (path.getFileSystem() instanceof DistributedFS)
    // return ((DistributedFS)path.getFileSystem()).delegate;
    //
    // return new LocalFileSystem();
    // }

    @SuppressWarnings("unchecked")
    public static boolean isDistributedFS(final AbstractFileSystem fs) {
        AbstractFileSystem unwrappedFS = fs;
        if (fs instanceof Wrapper)
            unwrappedFS = ((Wrapper<AbstractFileSystem>) fs).getNestedInstance();

        return !(unwrappedFS instanceof LocalFS);
    }

}
