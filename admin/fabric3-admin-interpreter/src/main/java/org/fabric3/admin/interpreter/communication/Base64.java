/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.fabric3.admin.interpreter.communication;

/**
 * Provides Base64 encoding and decoding as defined by RFC 2045.
 *
 * @version $Rev$ $Date$
 */


public class Base64 {
    private static final char[] S_BASE64CHAR = {'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '+', '/'};

    private static final char S_BASE64PAD = '=';

    /**
     * Returns base64 representation of the specified byte array.
     *
     * @param data the data
     * @return the base64 representation
     */
    public static String encode(byte[] data) {
        int off = 0;
        int len = data.length;
        if (len <= 0)
            return "";
        char[] out = new char[len / 3 * 4 + 4];
        int rindex = off;
        int windex = 0;
        int rest = len - off;
        while (rest >= 3) {
            int i = ((data[rindex] & 0xff) << 16)
                    + ((data[rindex + 1] & 0xff) << 8)
                    + (data[rindex + 2] & 0xff);
            out[windex++] = S_BASE64CHAR[i >> 18];
            out[windex++] = S_BASE64CHAR[(i >> 12) & 0x3f];
            out[windex++] = S_BASE64CHAR[(i >> 6) & 0x3f];
            out[windex++] = S_BASE64CHAR[i & 0x3f];
            rindex += 3;
            rest -= 3;
        }
        if (rest == 1) {
            int i = data[rindex] & 0xff;
            out[windex++] = S_BASE64CHAR[i >> 2];
            out[windex++] = S_BASE64CHAR[(i << 4) & 0x3f];
            out[windex++] = S_BASE64PAD;
            out[windex++] = S_BASE64PAD;
        } else if (rest == 2) {
            int i = ((data[rindex] & 0xff) << 8) + (data[rindex + 1] & 0xff);
            out[windex++] = S_BASE64CHAR[i >> 10];
            out[windex++] = S_BASE64CHAR[(i >> 4) & 0x3f];
            out[windex++] = S_BASE64CHAR[(i << 2) & 0x3f];
            out[windex++] = S_BASE64PAD;
        }
        return new String(out, 0, windex);
    }

}


