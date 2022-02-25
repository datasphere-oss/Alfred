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

package one.microstream.afs.types;

public interface AResolving {
	// note: no single string parameter resolving here, since this type is
	// separator-agnostic.

	public default AFile resolveFilePath(final String... pathElements) {
		return this.resolveFilePath(pathElements, 0, pathElements.length - 1, pathElements[pathElements.length - 1]);
	}

	public default AFile resolveFilePath(final String[] directoryPathElements, final String fileIdentifier) {
		return this.resolveFilePath(directoryPathElements, 0, directoryPathElements.length, fileIdentifier);
	}

	public default AFile resolveFilePath(final String[] directoryPathElements, final int offset, final int length,
			final String fileIdentifier) {
		final ADirectory directory = this.resolveDirectoryPath(directoryPathElements, offset, length);

		// if the implementation of #resolveDirectoryPath returns null, then conform to
		// this strategy.
		return directory == null ? null : directory.getFile(fileIdentifier);
	}

	public default ADirectory resolveDirectoryPath(final String... pathElements) {
		return this.resolveDirectoryPath(pathElements, 0, pathElements.length);
	}

	public ADirectory resolveDirectoryPath(String[] pathElements, int offset, int length);

}
