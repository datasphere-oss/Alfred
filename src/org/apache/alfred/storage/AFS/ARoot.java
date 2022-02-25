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

import static org.apache.alfred.core.X.coalesce;
import static org.apache.alfred.core.X.notNull;

public interface ARoot extends ADirectory {
	/**
	 * E.g. https:// file://
	 * 
	 * @return the protocol
	 */
	public String protocol();

	@FunctionalInterface
	public interface Creator {
		public ARoot createRootDirectory(AFileSystem fileSystem, String protocol, String identifier);

		public default ARoot createRootDirectory(final AFileSystem fileSystem, final String identifier) {
			return this.createRootDirectory(fileSystem, coalesce(this.protocol(), fileSystem.defaultProtocol()),
					identifier);
		}

		public default String protocol() {
			return null;
		}
	}

	/**
	 * Creates a new root directory Note: {@code identifier} can be {@literal ""}
	 * since local file paths might start with a "/".
	 * 
	 * @param fileSystem the root's file system
	 * @param protocol   the used protocol
	 * @param identifier the identifier of the root directory
	 * @return the newly created root directory
	 * 
	 */
	public static ARoot New(final AFileSystem fileSystem, final String protocol, final String identifier) {
		return new ARoot.Default(notNull(fileSystem), notNull(protocol), notNull(identifier) // may be ""
		);
	}

	public final class Default extends ADirectory.Abstract implements ARoot {
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final AFileSystem fileSystem;
		private final String protocol;

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final AFileSystem fileSystem, final String protocol, final String identifier) {
			super(identifier);
			this.protocol = protocol;
			this.fileSystem = fileSystem;
		}

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final AFileSystem fileSystem() {
			return this.fileSystem;
		}

		@Override
		public final String protocol() {
			return this.protocol;
		}

		@Override
		public final ADirectory parent() {
			return null;
		}

	}

}
