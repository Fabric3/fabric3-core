/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fabric3.monitor.spi.buffer;

import java.nio.ByteBuffer;

/**
 * A wrapper that enables a ByteBuffer to be re-sized.
 */
public class ResizableByteBuffer {
    private static final int SIZE = 1024;

    private ByteBuffer buffer;
    private ResizableByteBufferMonitor monitor;

    public ResizableByteBuffer(ByteBuffer buffer, ResizableByteBufferMonitor monitor) {
        this.buffer = buffer;
        this.monitor = monitor;
    }

    public ResizableByteBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
        this.monitor = new ResizableByteBufferMonitor() {
            public void bufferResize() {
            }
        };
    }

    public void put(int b) {
        resize(1);
        buffer.put((byte) b);
    }

    public void put(byte[] bytes) {
        resize(bytes.length);
        buffer.put(bytes);
    }

    public void put(byte[] bytes, int offset, int length) {
        resize(length);
        buffer.put(bytes, offset, length);
    }

    public void put(ByteBuffer from) {
        resize(from.capacity() - from.remaining());
        buffer.put(from);
    }

    public void put(int index, byte b) {
        resize(1);
        buffer.put(index, b);

    }

    public void putCharacter(char c) {
        resize(2);
        buffer.putChar(c);
    }

    public void putShort(short s) {
        resize(2);
        buffer.putShort(s);
    }

    public void putInteger(int i) {
        resize(4);
        buffer.putInt(i);
    }

    public void putFloat(float f) {
        resize(4);
        buffer.putFloat(f);
    }

    public void putDouble(double d) {
        resize(8);
        buffer.putDouble(d);
    }

    public void putLong(long l) {
        resize(8);
        buffer.putLong(l);
    }

    public void limit(int limit) {
        buffer.limit(limit);
    }

    public int limit() {
        return buffer.limit();
    }

    public void position(int position) {
        buffer.position(position);
    }

    public int position() {
        return buffer.position();
    }

    public int capacity() {
        return buffer.capacity();
    }

    public ByteBuffer getByteBuffer() {
        return buffer;
    }

    public void clear() {
        buffer.clear();
    }

    public void flip() {
        buffer.flip();
    }

    public void get(byte[] bytes) {
        buffer.get(bytes);
    }

    public byte[] array() {
        return buffer.array();
    }

    private void resize(int amount) {
        if (buffer.remaining() >= amount) {
            return;
        }
        monitor.bufferResize();
        ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + amount + SIZE);
        int currentPosition = buffer.position();
        buffer.position(0);
        newBuffer.put(buffer);
        newBuffer.position(currentPosition);
        buffer = newBuffer;
    }

}
