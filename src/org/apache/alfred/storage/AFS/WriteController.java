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

package org.apache.alfred.storage.afs.types;

import org.apache.alfred.storage.afs.exceptions.AfsExceptionReadOnly;

@FunctionalInterface
public interface WriteController {
	public default void validateIsWritable() {
		if (this.isWritable()) {
			return;
		}

		throw new AfsExceptionReadOnly("Writing is not enabled.");
	}

	public boolean isWritable();

	public static WriteController Enabled() {
		// Singleton is (usually) an anti pattern.
		return new WriteController.Enabled();
	}

	public final class Enabled implements WriteController {
		Enabled() {
			super();
		}

		@Override
		public final void validateIsWritable() {
			// no-op
		}

		@Override
		public final boolean isWritable() {
			return true;
		}

	}

	public static WriteController Disabled() {
		// Singleton is (usually) an anti pattern.
		return new WriteController.Disabled();
	}

	public final class Disabled implements WriteController {
		Disabled() {
			super();
		}

		@Override
		public final boolean isWritable() {
			return false;
		}

	}

}
