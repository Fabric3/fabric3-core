/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
