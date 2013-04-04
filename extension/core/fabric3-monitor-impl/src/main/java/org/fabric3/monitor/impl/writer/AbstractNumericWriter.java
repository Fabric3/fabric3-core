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
 *
 *------------------------------------------------------------------------------------
 *
 * Based on code from the ultralog project (http://code.google.com/p/ultralog/)
 *
 * Copyright (c) 2012, Mikhail Vladimirov
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the <organization> nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.fabric3.monitor.impl.writer;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Base methods for writing numeric types in a character representation to a ByteBuffer without creating objects on the heap.
 */
public abstract class AbstractNumericWriter {
    protected final static int[] DIGITS = new int[10000];

    static {
        for (int i = 0; i < 10000; i++) {
            DIGITS[i] = (('0' + i / 1000 % 10) << 24) +
                        (('0' + i / 100 % 10) << 16) +
                        (('0' + i / 10 % 10) << 8) +
                        (('0' + i / 1 % 10) << 0);
        }
    }

    protected final static int[] POWER_5 = {1, 5, 25, 125, 625, 3125, 15625, 78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125};

    protected final static int[] POW_10 = new int[258];

    static {
        for (int i = 0; i < 258; i++)
            POW_10[i] = (int) Math.pow(10.0, Math.scalb(i, -8));
    }

    protected static final byte[] NAN = "NaN".getBytes();
    protected static final byte[] INFINITY = "Infinity".getBytes();
    protected static final byte[] NEGATIVE_INFINITY = "-Infinity".getBytes();

    protected final static double EXPONENT_FACTOR = 3.3219280948873623478703194294894;
    protected final static int BUFFER_SIZE = 20;

    protected static final byte[] ZERO_ZERO = "0.0".getBytes();
    protected static final byte[] POINT_ZERO = ".0".getBytes();
    protected static final byte[] ZERO_POINT = "0.".getBytes();

    protected final static ThreadLocal<char[]> CHAR_BUFFER = new ThreadLocal<char[]>() {
        protected char[] initialValue() {
            return new char[BUFFER_SIZE];
        }
    };

    protected final static int BUFFER_LENGTH = 81;

    protected final static ThreadLocal<int[]> BUFFER = new ThreadLocal<int[]>() {
        @Override
        protected int[] initialValue() {
            return new int[BUFFER_LENGTH];
        }

    };

    protected static long getDecimalMantissa(long binaryMantissa, int binaryExponent, int decimalExponent) {
        if (binaryMantissa < 0) {
            throw new IllegalArgumentException("Binary mantissa (" + binaryMantissa + ") < 0");
        }

        if (binaryExponent < -1075) {
            throw new IllegalArgumentException("Binary exponent (" + binaryExponent + ") < -1075");
        }

        if (binaryExponent > 1075) {
            throw new IllegalArgumentException("Binary exponent (" + binaryExponent + ") > 1075");
        }

        if (binaryExponent == 0) {
            return getDecimalMantissa0(binaryMantissa, decimalExponent);
        } else if (binaryExponent > 0) {
            return getDecimalMantissaPlus(binaryMantissa, binaryExponent, decimalExponent);
        } else {
            // binaryExponent < 0
            return getDecimalMantissaMinus(binaryMantissa, binaryExponent, decimalExponent);
        }
    }

    private static long getDecimalMantissa0(long binaryMantissa, int decimalExponent) {
        if (binaryMantissa < 0) {
            throw new IllegalArgumentException("Binary mantissa (" + binaryMantissa + ") < 0");
        }

        if (decimalExponent == 0) {
            return binaryMantissa;
        } else if (decimalExponent > 0) {
            long result = binaryMantissa;
            for (int i = 0; i < decimalExponent; i++)
                result /= 10;
            return result;
        } else {
            // decimalExponent < 0
            long result = binaryMantissa;
            for (int i = 0; i > decimalExponent; i--)
                result *= 10;
            return result;
        }
    }

    private static long getDecimalMantissaPlus(long binaryMantissa, int binaryExponent, int decimalExponent) {
        if (binaryMantissa < 0) {
            throw new IllegalArgumentException("Binary mantissa (" + binaryMantissa + ") < 0");
        }

        if (binaryExponent <= 0) {
            throw new IllegalArgumentException("Binary exponent (" + binaryExponent + ") <= 0");
        }

        if (binaryExponent > 1075) {
            throw new IllegalArgumentException("Binary exponent (" + binaryExponent + ") > 1075");
        }

        int[] buffer = BUFFER.get();
        int pos = binaryExponent / 32;
        int offset = binaryExponent % 32;
        int l;

        if (offset > 0) {
            buffer[pos + 2] = (int) (binaryMantissa >>> (64 - offset));
            l = pos + 3;
        } else {
            l = pos + 2;
        }

        buffer[pos + 1] = (int) (binaryMantissa >>> (32 - offset));
        buffer[pos] = (int) (binaryMantissa << offset);

        Arrays.fill(buffer, 0, pos, 0);

        return normalizeDecimalMantissa(buffer, l, decimalExponent);
    }

    private static long getDecimalMantissaMinus(long binaryMantissa, int binaryExponent, int decimalExponent) {
        if (binaryMantissa < 0) {
            throw new IllegalArgumentException("Binary mantissa (" + binaryMantissa + ") < 0");
        }

        if (binaryExponent < -1075) {
            throw new IllegalArgumentException("Binary exponent (" + binaryExponent + ") < -1075");
        }

        if (binaryExponent >= 0) {
            throw new IllegalArgumentException("Binary exponent (" + binaryExponent + ") >= 0");
        }

        int be = binaryExponent;

        while (binaryMantissa <= Long.MAX_VALUE / 5 && be < 0) {
            binaryMantissa *= 5;
            be += 1;
        }

        int[] buffer = BUFFER.get();
        buffer[0] = (int) binaryMantissa;
        buffer[1] = (int) (binaryMantissa >>> 32);

        int l = multiplyByPower5(buffer, buffer[1] == 0 ? 1 : 2, -be);

        return normalizeDecimalMantissa(buffer, l, decimalExponent - binaryExponent);
    }

    private static int multiplyByPower5(int[] buffer, int length, int power) {
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer is null");
        }

        if (length < 0) {
            throw new IllegalArgumentException("Length (" + length + ") < 0");
        }

        if (power < 0) {
            throw new IllegalArgumentException("Power (" + power + ")< 0");
        }

        int maxStep = POWER_5.length - 1;
        while (power > 0) {
            int step = Math.min(power, maxStep);
            int k = POWER_5[step];

            int carry = 0;
            for (int j = 0; j < length; j++) {
                long chunk = (((long) buffer[j]) & 0xFFFFFFFFL) * k + (((long) carry) & 0xFFFFFFFFL);

                buffer[j] = (int) chunk;
                carry = (int) (chunk >>> 32);
            }

            if (carry != 0) {
                buffer[length++] = carry;
            }

            power -= step;
        }

        return length;
    }

    private static long normalizeDecimalMantissa(int[] buffer, int length, int decimalExponent) {
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer is null");
        }

        if (length < 0) {
            throw new IllegalArgumentException("Length (" + length + ") < 0");
        }

        int bufferLength = buffer.length;
        if (length > buffer.length) {
            throw new IllegalArgumentException("Length (" + length + ") > buffer length (" +
                                               bufferLength + ")");
        }

        if (decimalExponent >= 0) {
            length = shiftRight(buffer, length, decimalExponent);

            return divideByPower5(buffer, length, decimalExponent);
        } else {
            long result;

            if (length == 0) {
                result = 0L;
            } else if (length == 1) {
                result = ((long) buffer[0]) & 0xFFFFFFFFL;
            } else if (length == 2) {
                result = (((long) buffer[1]) << 32 | (((long) buffer[0]) & 0xFFFFFFFFL));
            } else {
                throw new Error("Mantissa does not fit into long");
            }

            for (int i = 0; i > decimalExponent; i--)
                result *= 10;

            return result;
        }
    }

    private static int shiftRight(int[] buffer, int length, int n) {
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer is null");
        }

        if (length < 0) {
            throw new IllegalArgumentException("Length (" + length + ") < 0");
        }

        if (n < 0) {
            throw new IllegalArgumentException("N (" + n + ") is null");
        }

        int bufferLength = buffer.length;
        if (length > bufferLength) {
            throw new IllegalArgumentException("Length (" + length + ") > buffer lenght (" + bufferLength + ")");
        }

        if (n >= length * 32) {
            length = 0;
        } else if (n > 0) {
            int offset = n / 32;
            int bitOffset = n % 32;

            if (bitOffset == 0) {
                length -= offset;
                for (int i = 0; i < length; i++) {
                    buffer[i] = buffer[i + offset];
                }
            } else {
                length -= offset;
                int l1 = length - 1;
                for (int i = 0; i < l1; i++) {
                    buffer[i] = buffer[i + offset] >>> bitOffset | buffer[i + offset + 1] << (32 - bitOffset);
                }

                buffer[l1] = buffer[l1 + offset] >>> bitOffset;
            }
        }

        while (length > 0 && buffer[length - 1] == 0) {
            length -= 1;
        }

        return length;
    }

    private static long divideByPower5(int[] buffer, int length, int power) {
        int maxStep = POWER_5.length - 1;
        while (power > 0 && (length > 2 || (length == 2 && buffer[1] < 0L))) {
            int step = Math.min(power, maxStep);
            int k = POWER_5[step];

            int carry = 0;
            for (int j = length - 1; j >= 0; j--) {
                long chunk = (((long) carry) << 32) + (((long) buffer[j]) & 0xFFFFFFFFL);

                int v = (int) (chunk / k);

                buffer[j] = v;
                carry = (int) (chunk - ((long) v) * k);

                if (v == 0 && j == length - 1) {
                    length -= 1;
                }
            }

            power -= step;
        }

        long result;

        if (length == 0) {
            result = 0L;
        } else if (length == 1) {
            result = ((long) buffer[0]) & 0xFFFFFFFFL;
        } else if (length == 2) {
            result = (((long) buffer[1]) << 32 | (((long) buffer[0]) & 0xFFFFFFFFL));
        } else {
            throw new IllegalArgumentException("Result does not fit into long");
        }

        while (power > 0 && result != 0) {
            int step = Math.min(power, maxStep);
            int k = POWER_5[step];

            result /= k;
            power -= step;
        }

        return result;
    }

    protected static int formatSimple(boolean negative, long decimalMantissa, int decimalExponent, ByteBuffer buffer) {
        int written = 0;
        if (decimalMantissa < 0) {
            throw new IllegalArgumentException("Decimal mantissa (" + decimalMantissa + ") < 0");
        }

        if (buffer == null) {
            throw new IllegalArgumentException("Output is null");
        }

        if (negative) {
            buffer.put((byte) '-');
            written++;
        }

        if (decimalMantissa == 0) {
            buffer.put(ZERO_ZERO);
            written = written + 3;
        } else {
            if (decimalExponent >= 0) {
                written = written + LongWriter.write(decimalMantissa, buffer);
                written = written + writeCharacterNTimes('0', decimalExponent, buffer);
                buffer.put(POINT_ZERO);
                written = written + 2;
            } else {
                // decimalExponent < 0
                char[] charBuffer = CHAR_BUFFER.get();
                int pos = getChars(decimalMantissa, charBuffer);

                int afterLastDigitPos = BUFFER_SIZE;

                for (int i = BUFFER_SIZE - 1; charBuffer[i] == '0'; i--)
                    afterLastDigitPos = i;

                int n = BUFFER_SIZE - pos;

                if (n <= -decimalExponent) {
                    // No significant digits before dot
                    buffer.put(ZERO_POINT);
                    buffer.put(ZERO_POINT);
                    written++;
                    written++;
                    written = written + writeCharacterNTimes('0', -decimalExponent - n, buffer);
                    written = written + write(charBuffer, pos, afterLastDigitPos - pos, buffer);
                } else {
                    int digitsBeforeDot = n + decimalExponent;
                    int digitsAfterDot = afterLastDigitPos - pos - digitsBeforeDot;

                    written = written + write(charBuffer, pos, digitsBeforeDot, buffer);
                    buffer.put((byte) '.');
                    written++;

                    if (digitsAfterDot > 0) {
                        written = written + write(charBuffer, pos + digitsBeforeDot, digitsAfterDot, buffer);
                    } else {
                        buffer.put((byte) '0');
                        written++;
                    }
                }
            }
        }
        return written;
    }

    private static int write(char[] chars, int pos, int length, ByteBuffer buffer) {
        int written = 0;
        int amount = pos + length;
        for (int i = pos; i < amount; i++) {
            buffer.put((byte) chars[i]);
            written++;
        }
        return written;
    }

    private static int writeCharacterNTimes(char ch, int n, ByteBuffer buffer) {
        if (n < 0) {
            throw new IllegalArgumentException("N < 0");
        }

        int written = 0;
        while (n > 0) {
            switch (n) {
                case 1:
                    buffer.put((byte) ch);
                    written++;
                    n -= 1;
                    break;
                case 2:
                    buffer.put((byte) ch);
                    buffer.put((byte) ch);
                    written = written + 2;
                    n -= 2;
                    break;
                case 3:
                    buffer.put((byte) ch);
                    buffer.put((byte) ch);
                    buffer.put((byte) ch);
                    written = written + 3;
                    n -= 3;
                    break;
                default:
                    buffer.put((byte) ch);
                    buffer.put((byte) ch);
                    buffer.put((byte) ch);
                    buffer.put((byte) ch);
                    written = written + 4;
                    n -= 4;
                    break;
            }
        }
        return written;
    }

    private static int getChars(long value, char[] buffer) {
        if (value < 0) {
            throw new IllegalArgumentException("Value (" + value + ") < 0");
        }

        if (buffer == null) {
            throw new IllegalArgumentException("Buffer is null");
        }

        int pos = BUFFER_SIZE;

        while (value > Integer.MAX_VALUE) {
            long v = value / 10000;
            int d = (int) (value - v * 10000);
            value = v;

            int d4 = DIGITS[d];

            buffer[--pos] = (char) ((d4 >>> 0) & 0xFF);
            buffer[--pos] = (char) ((d4 >>> 8) & 0xFF);
            buffer[--pos] = (char) ((d4 >>> 16) & 0xFF);
            buffer[--pos] = (char) ((d4 >>> 24) & 0xFF);
        }

        int intValue = (int) value;

        while (intValue >= 10000) {
            int v = intValue / 10000;
            int d = intValue - v * 10000;
            intValue = v;

            int d4 = DIGITS[d];

            buffer[--pos] = (char) ((d4 >>> 0) & 0xFF);
            buffer[--pos] = (char) ((d4 >>> 8) & 0xFF);
            buffer[--pos] = (char) ((d4 >>> 16) & 0xFF);
            buffer[--pos] = (char) ((d4 >>> 24) & 0xFF);
        }

        int d4 = DIGITS[intValue];

        if (intValue >= 1) {
            buffer[--pos] = (char) ((d4 >>> 0) & 0xFF);
        }

        if (intValue >= 10) {
            buffer[--pos] = (char) ((d4 >>> 8) & 0xFF);
        }

        if (intValue >= 100) {
            buffer[--pos] = (char) ((d4 >>> 16) & 0xFF);
        }

        if (intValue >= 1000) {
            buffer[--pos] = (char) ((d4 >>> 24) & 0xFF);
        }

        return pos;
    }

}
