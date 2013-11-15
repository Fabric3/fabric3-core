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

import org.fabric3.monitor.spi.buffer.ResizableByteBuffer;

/**
 * Writes a double value in a character representation to a ByteBuffer without creating objects on the heap.
 */
public class DoubleWriter extends AbstractNumericWriter {

    public static int write(double value, ResizableByteBuffer buffer) {
        int written = 0;
        if (Double.isNaN(value)) {
            buffer.put(NAN);
            written = written + 3;
        } else if (Double.isInfinite(value)) {
            if (value > 0.0f) {
                buffer.put(INFINITY);
                written = written + INFINITY.length;
            } else {
                buffer.put(NEGATIVE_INFINITY);
                written = written + NEGATIVE_INFINITY.length;
            }
        } else {
            long bits = Double.doubleToRawLongBits(value);

            boolean negative = bits >>> 63 != 0;
            int binaryExponent = (int) ((bits >>> 52) & 0x7FF) - 1075;
            long binaryMantissa = bits & 0xFFFFFFFFFFFFFL;

            if (binaryExponent == -1075) {
                // subnormal mantissa
                binaryExponent = -1074;
            } else {
                // normal mantissa
                binaryMantissa += 0x10000000000000L;
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
