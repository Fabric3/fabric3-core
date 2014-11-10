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
package org.fabric3.monitor.impl.writer;

import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;

/**
 * Writes an int value in a character representation to a ByteBuffer without creating objects on the heap.
 */
public final class IntWriter {
    private static final byte[] INT_MIN = "-2147483648".getBytes();

    private final static int[] SIZE_TABLE = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

    private final static char[] DIGIT_TENS = {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2', '2',
                                              '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4',
                                              '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6',
                                              '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9',
                                              '9', '9', '9', '9', '9', '9', '9', '9',};

    private final static char[] DIGIT_ONES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2',
                                              '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5',
                                              '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8',
                                              '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1',
                                              '2', '3', '4', '5', '6', '7', '8', '9',};

    private final static char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                                          'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private IntWriter() {
    }

    public static int write(int value, ResizableByteBuffer buffer) {
        if (value == Integer.MIN_VALUE) {
            buffer.put(INT_MIN, 0, INT_MIN.length);
            return INT_MIN.length;
        }

        return writeIntChars(value, buffer);

    }

    private static int stringSize(int x) {
        for (int i = 0; ; i++)
            if (x <= SIZE_TABLE[i]) {
                return i + 1;
            }
    }

    private static int writeIntChars(int value, ResizableByteBuffer buffer) {
        int start = buffer.position();
        int size = (value < 0) ? stringSize(-value) + 1 : stringSize(value);
        int index = size + start;

        int q, r;
        int charPos = index;
        char sign = 0;

        if (value < 0) {
            sign = '-';
            value = -value;
        }

        // Generate two digits per iteration
        while (value >= 65536) {
            q = value / 100;
            // really: r = i - (q * 100);
            r = value - ((q << 6) + (q << 5) + (q << 2));
            value = q;
            buffer.put(--charPos, (byte) DIGIT_ONES[r]);
            buffer.put(--charPos, (byte) DIGIT_TENS[r]);
        }

        // fast mode for smaller numbers
        for (; ; ) {
            q = (value * 52429) >>> (16 + 3);
            r = value - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            buffer.put(--charPos, (byte) DIGITS[r]);
            value = q;
            if (value == 0) {
                break;
            }
        }
        if (sign != 0) {
            buffer.put(--charPos, (byte) sign);
        }
        buffer.position(index);
        return size;

    }

}
