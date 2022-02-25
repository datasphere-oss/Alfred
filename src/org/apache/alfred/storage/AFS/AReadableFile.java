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

import java.nio.ByteBuffer;

import org.apache.alfred.core.io.BufferProvider;

public interface AReadableFile extends AFile.Wrapper {
	/*
	 * (31.05.2020 TM)NOTE: shortcut implementations for useReading? But beware: -
	 * Default user is defined in the accessmanager instance, so it must be used,
	 * anyway! - retired usage/wrapper instances might be used to create new, active
	 * ones. May not be suppressed! - More special cases? Thus: worth it?
	 */

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean open() {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().openReading(this);
	}

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean isOpen() {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().isOpen(this);
	}

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean close() {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().close(this);
	}

	// implicitely #close PLUS the AFS-management-level aspect
	public default boolean release() {
		final boolean wasClosed = this.close();

		this.fileSystem().accessManager().unregister(this);

		return wasClosed;
	}

	@Override
	public default long size() {
		// synchronization handled by IoHandler.
		return this.fileSystem().ioHandler().size(this);
	}

	public default ByteBuffer readBytes() {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this);
	}

	public default ByteBuffer readBytes(final long position) {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, position);
	}

	public default ByteBuffer readBytes(final long position, final long length) {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, position, length);
	}

	public default long readBytes(final ByteBuffer targetBuffer) {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer);
	}

	public default long readBytes(final ByteBuffer targetBuffer, final long position) {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer, position);
	}

	public default long readBytes(final ByteBuffer targetBuffer, final long position, final long length) {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer, position, length);
	}

	public default long readBytes(final BufferProvider bufferProvider) {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider);
	}

	public default long readBytes(final BufferProvider bufferProvider, final long position) {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider, position);
	}

	public default long readBytes(final BufferProvider bufferProvider, final long position, final long length) {
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider, position, length);
	}

	public default long copyTo(final AWritableFile target) {
		return this.actual().fileSystem().ioHandler().copyTo(this, target);
	}

	public default long copyTo(final AWritableFile target, final long sourcePosition) {
		return this.actual().fileSystem().ioHandler().copyTo(this, sourcePosition, target);
	}

	public default long copyTo(final AWritableFile target, final long sourcePosition, final long length) {
		return this.actual().fileSystem().ioHandler().copyTo(this, sourcePosition, length, target);
	}

	public boolean retire();

	public boolean isRetired();

	public void validateIsNotRetired();

}
