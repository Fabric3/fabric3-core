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
 * Writes a float value in a character representation to a ByteBuffer without creating objects on the heap.
 */
public class FloatWriter extends AbstractNumericWriter {

    public static int write(float value, ResizableByteBuffer buffer) {
        int written = 0;

        if (Float.isNaN(value)) {
            buffer.put(NAN);
            written = written + 3;
        } else if (Float.isInfinite(value)) {
            if (value > 0.0f) {
                buffer.put(INFINITY);
                written = written + INFINITY.length;
            } else {
                buffer.put(NEGATIVE_INFINITY);
                written = written + NEGATIVE_INFINITY.length;
            }

        } else {
            int bits = Float.floatToRawIntBits(value);

            boolean negative = bits >>> 31 != 0;
            int binaryExponent = ((bits >>> 23) & 0xFF) - 150;
            int binaryMantissa = bits & 0x7FFFFF;

            if (binaryExponent == -150) {
                // subnormal mantissa
                binaryExponent = -149;
            } else {
                // normal mantissa
                binaryMantissa += 0x800000;
            }

            double significantDigits = (binaryExponent - 1) / EXPONENT_FACTOR;
            int decimalExponent = (int) Math.floor(significantDigits);
            int margin = POW_10[(int) Math.scalb(significantDigits - decimalExponent, 8)];

            long decimalMantissa = getDecimalMantissa(binaryMantissa, binaryExponent, decimalExponent);

            int lastDigit = (int) (decimalMantissa % 10);

            if (lastDigit <= margin) {
                decimalMantissa /= 10;
                decimalExponent += 1;
            } else if (lastDigit >= 10 - margin) {
                decimalMantissa = decimalMantissa / 10 + 1;
                decimalExponent += 1;
            }

            written = written + formatSimple(negative, decimalMantissa, decimalMantissa == 0 ? 0 : decimalExponent, buffer);
        }

        return written;
    }

}
