/*
 * Copyright 2020-2022 Oliver Berg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.epubby.internal.utils

import java.io.OutputStream
import java.nio.BufferOverflowException
import java.nio.ByteBuffer

// from https://gist.github.com/hoijui/7fe8a6d31b20ae7af945

// TODO: can this be removed?
internal class ByteBufferOutputStream(private var buffer: ByteBuffer, private val canGrow: Boolean) : OutputStream() {
    private fun growTo(minCapacity: Int) {
        // overflow-conscious code
        val oldCapacity: Int = buffer.capacity()
        var newCapacity = oldCapacity shl 1

        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity
        }

        if (newCapacity < 0) {
            if (minCapacity < 0) { // overflow
                throw OutOfMemoryError()
            }

            newCapacity = Int.MAX_VALUE
        }

        val oldBuffer: ByteBuffer = buffer
        // create the new buffer
        buffer = if (buffer.isDirect) {
            ByteBuffer.allocateDirect(newCapacity)
        } else {
            ByteBuffer.allocate(newCapacity)
        }

        // copy over the old content into the new buffer
        oldBuffer.flip()
        buffer.put(oldBuffer)
    }

    override fun write(byte: Int) {
        try {
            buffer.put(byte.toByte())
        } catch (e: BufferOverflowException) {
            if (canGrow) {
                val newBufferSize: Int = buffer.capacity() * 2
                growTo(newBufferSize)
                write(byte)
            } else {
                throw e
            }
        }
    }

    override fun write(bytes: ByteArray) {
        var oldPosition = 0
        try {
            oldPosition = buffer.position()
            buffer.put(bytes)
        } catch (e: BufferOverflowException) {
            if (canGrow) {
                val newBufferSize = (buffer.capacity() * 2).coerceAtLeast(oldPosition + bytes.size)
                growTo(newBufferSize)
                write(bytes)
            } else {
                throw e
            }
        }
    }

    override fun write(bytes: ByteArray, offset: Int, length: Int) {
        var oldPosition = 0
        try {
            oldPosition = buffer.position()
            buffer.put(bytes, offset, length)
        } catch (ex: BufferOverflowException) {
            if (canGrow) {
                val newBufferSize = (buffer.capacity() * 2).coerceAtLeast(oldPosition + length)
                growTo(newBufferSize)
                write(bytes, offset, length)
            } else {
                throw ex
            }
        }
    }
}