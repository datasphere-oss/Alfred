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

public interface AResolver<D, F> {
	public AFileSystem fileSystem();

	public String[] resolveDirectoryToPath(D directory);

	public String[] resolveFileToPath(F file);

	public D resolve(ADirectory directory);

	public F resolve(AFile file);

	public default ADirectory resolveDirectory(final D directory) {
		final String[] path = this.resolveDirectoryToPath(directory);

		return this.fileSystem().resolveDirectoryPath(path);
	}

	public default AFile resolveFile(final F file) {
		final String[] path = this.resolveFileToPath(file);

		return this.fileSystem().resolveFilePath(path);
	}

	// (13.05.2020 TM)TODO: priv#49: does ensure~ really belong here?

	public default ADirectory ensureDirectory(final D directory) {
		final String[] path = this.resolveDirectoryToPath(directory);

		return this.fileSystem().ensureDirectoryPath(path);
	}

	public default AFile ensureFile(final F file) {
		final String[] path = this.resolveFileToPath(file);

		return this.fileSystem().ensureFilePath(path);
	}

}
