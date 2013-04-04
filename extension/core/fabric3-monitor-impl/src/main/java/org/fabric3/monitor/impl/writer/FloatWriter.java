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
package org.fabric3.monitor.impl.writer;

import java.nio.ByteBuffer;

/**
 * Writes a float value in a character representation to a ByteBuffer without creating objects on the heap.
 */
public class FloatWriter extends AbstractNumericWriter {

    public static int write(float value, ByteBuffer buffer) {
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
