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
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Formats a timestamp without creating objects and writes it to a ByteBuffer using the following pattern syntax:
 * <pre>
 *     * %a - Abbreviated weekday name ("Sun", "Mon", "Tue")
 *     * %b - Abbreviated month name ("Jan", "Feb", "Mar")
 *     * %c - The Month number
 *     * %d - The day of month in padded two digit form (01..31)
 *     * %e - The day of month in trimmed form (1..31)
 *     * %f - Milliseconds in trimmed form (0..999)
 *     * %F - Milliseconds in padded three digit form (000..999)
 *     * %H - The hour in 24-hour form (00-23)
 *     * %h - The hour in 12-hour form (01-12)
 *     * %i - Minutes
 *     * %j - Day of year (001..366)
 *     * %k - The hour in trimmed 24-hour form (0..23)
 *     * %l - The hour in trimmed 12-hour form (1..12)
 *     * %M - The full month name
 *     * %m - The month number
 *     * %p - AM or PM
 *     * %S - The seconds in trimmed form
 *     * %s - The seconds in padded two digit form
 *     * %W - The full weekday name("Sunday".."Saturday")
 *     * %w - The day of week (1 - Sunday .. 7 - Saturday)
 *     * %Y - The four digit year
 *     * %y - The two digit year
 * </pre>
 * This implementation does not support negative timestamps.
 */
public class TimestampWriter {
    private final static ThreadLocal<ExtractedYearMonth> EXTRACTED_YEAR_MONTH = new ThreadLocal<ExtractedYearMonth>() {

        protected ExtractedYearMonth initialValue() {
            return new ExtractedYearMonth();
        }

    };

    private final static long SECOND = 1000L;
    private final static long MINUTE = SECOND * 60L;
    private final static long HOUR = MINUTE * 60L;
    private final static long DAY = HOUR * 24L;

    private final static long[] MONTH = new long[]{DAY * 31L, DAY * 28L, DAY * 31L, DAY * 30L, DAY * 31L, DAY * 30L, DAY * 31L, DAY * 31L, DAY * 30L, DAY * 31L,
                                                   DAY * 30L, DAY * 31L};

    private final static long[] LEAP_MONTH = new long[]{DAY * 31L, DAY * 29L, DAY * 31L, DAY * 30L, DAY * 31L, DAY * 30L, DAY * 31L, DAY * 31L, DAY * 30L,
                                                        DAY * 31L, DAY * 30L, DAY * 31L};

    private final static long YEAR = DAY * 365L;
    private final static long LEAP_YEAR = DAY * 366L;
    private final static long YEAR_4 = YEAR * 3L + LEAP_YEAR;
    private final static long LEAP_YEAR_4 = YEAR * 4L;
    private final static long YEAR_28 = YEAR_4 * 7L;
    private final static long YEAR_100 = YEAR_4 * 24L + LEAP_YEAR_4;
    private final static long LEAP_YEAR_100 = YEAR_4 * 25L;
    private final static long LEAP_YEAR_200 = LEAP_YEAR_100 + YEAR_100;
    private final static long LEAP_YEAR_300 = LEAP_YEAR_100 + YEAR_100 * 2L;
    private final static long YEAR_400 = YEAR_100 * 3L + LEAP_YEAR_100;

    private final LongFormatter[] chunks;
    private TimeZone tz;

    /**
     * Constructor.
     *
     * @param pattern  pattern to use for formatting timestamps
     * @param timeZone time zone to use or <code>null</code> to use UTC time zone.
     */
    public TimestampWriter(String pattern, TimeZone timeZone) {
        this.tz = timeZone;

        List<LongFormatter> chunks = new ArrayList<LongFormatter>();

        int length = pattern.length();
        int state = 0;
        StringBuilder literal = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char ch = pattern.charAt(i);

            switch (state) {
                case 0:
                    switch (ch) {
                        case '%':
                            state = 1;
                            break;
                        default:
                            literal.append(ch);
                    }
                    break;
                case 1:
                    switch (ch) {
                        case 'a':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(AbbreviatedWeekdayName.INSTANCE);
                            break;
                        case 'b':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(AbbreviatedMonthName.INSTANCE);
                            break;
                        case 'c':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(MonthNumeric.INSTANCE);
                            break;
                        case 'd':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(DayOfMonthNumeric2.INSTANCE);
                            break;
                        case 'e':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(DayOfMonthNumeric.INSTANCE);
                            break;
                        case 'f':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(Milliseconds.INSTANCE);
                            break;
                        case 'F':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(Milliseconds3.INSTANCE);
                            break;
                        case 'H':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(Hour24_2.INSTANCE);
                            break;
                        case 'h':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(Hour12_2.INSTANCE);
                            break;
                        case 'i':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(Minutes2.INSTANCE);
                            break;
                        case 'j':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(DayOfYear3.INSTANCE);
                            break;
                        case 'k':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(Hour24.INSTANCE);
                            break;
                        case 'l':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(Hour12.INSTANCE);
                            break;
                        case 'M':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(MonthName.INSTANCE);
                            break;
                        case 'm':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(MonthNumeric2.INSTANCE);
                            break;
                        case 'p':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(AMPM.INSTANCE);
                            break;
                        case 'S':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(Seconds.INSTANCE);
                            break;
                        case 's':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(Seconds2.INSTANCE);
                            break;
                        case 'W':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(WeekdayName.INSTANCE);
                            break;
                        case 'w':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(DayOfWeek.INSTANCE);
                            break;
                        case 'Y':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(Year4.INSTANCE);
                            break;
                        case 'y':
                            if (literal.length() > 0) {
                                chunks.add(new Literal(literal.toString()));
                                literal.setLength(0);
                            }
                            chunks.add(Year2.INSTANCE);
                            break;
                        default:
                            literal.append(ch);
                    }
                    state = 0;
                    break;
                default:
                    throw new Error("Unknown state: " + state);
            }
        }

        if (state != 0) {
            throw new IllegalArgumentException("Unexpected end of pattern: " + pattern);
        }

        if (literal.length() > 0) {
            chunks.add(new Literal(literal.toString()));
        }

        this.chunks = chunks.toArray(new LongFormatter[chunks.size()]);
    }

    public int write(long value, ByteBuffer buffer) {
        int written = 0;
        if (tz != null) {
            value += tz.getOffset(value);
        }

        if (value < 0) {
            written = written + LongWriter.write(value, buffer);
        } else {
            for (int count = chunks.length, i = 0; i < count; i++) {
                written = written + chunks[i].format(value, buffer);
            }
        }
        return written;
    }

    private static int getMillisecond(long timestamp) {
        if (timestamp < 0L) {
            throw new IllegalArgumentException("Timestamp (" + timestamp + ") < 0");
        }

        return (int) (timestamp % SECOND);
    }

    private static int getSecond(long timestamp) {
        if (timestamp < 0L) {
            throw new IllegalArgumentException("Timestamp (" + timestamp + ") < 0");
        }

        return (int) (timestamp % MINUTE / SECOND);
    }

    private static int getMinute(long timestamp) {
        if (timestamp < 0L) {
            throw new IllegalArgumentException("Timestamp (" + timestamp + ") < 0");
        }

        return (int) (timestamp % HOUR / MINUTE);
    }

    private static int getHour(long timestamp) {
        if (timestamp < 0L) {
            throw new IllegalArgumentException("Timestamp (" + timestamp + ") < 0");
        }

        return (int) (timestamp % DAY / HOUR);
    }

    private static int getDayOfWeek(long timestamp) {
        if (timestamp < 0L) {
            throw new IllegalArgumentException("Timestamp (" + timestamp + ") < 0");
        }

        return (int) ((timestamp / DAY + 4L) % 7L);
    }

    private static int getDayOfMonth(long timestamp) {
        if (timestamp < 0L) {
            throw new IllegalArgumentException("Timestamp (" + timestamp + ") < 0");
        }

        long m = extractMonth(timestamp);

        return (int) m;
    }

    private static int getDayOfYear(long timestamp) {
        if (timestamp < 0L) {
            throw new IllegalArgumentException("Timestamp (" + timestamp + ") < 0");
        }

        long e = extractYear(timestamp);

        return (int) e;
    }

    private static int getMonth(long timestamp) {
        if (timestamp < 0L) {
            throw new IllegalArgumentException("Timestamp (" + timestamp + ") < 0");
        }

        long m = extractMonth(timestamp);

        return (int) (m >>> 32);
    }

    private static int getYear(long timestamp) {
        if (timestamp < 0L) {
            throw new IllegalArgumentException("Timestamp (" + timestamp + ") < 0");
        }

        return (int) (extractYear(timestamp) >>> 32);
    }

    private static long extractMonth(long timestamp) {
        if (timestamp < 0L) {
            throw new IllegalArgumentException("Timestamp (" + timestamp + ") < 0");
        }

        ExtractedYearMonth eym = EXTRACTED_YEAR_MONTH.get();

        if (eym.extractedMonthTimestamp == timestamp) {
            return eym.extractedMonth;
        }

        long e = extractYear(timestamp);

        int year = (int) (e >>> 32);
        long offset = ((int) e) * DAY;

        boolean leap = isLeapYear(year);

        long[] month = leap ? LEAP_MONTH : MONTH;

        for (int i = 0; i < 12; i++) {
            long m = month[i];

            if (offset >= m) {
                offset -= m;
            } else {
                long result = (((long) i) << 32) + (offset / DAY);

                eym.extractedMonthTimestamp = timestamp;
                eym.extractedMonth = result;

                return result;
            }
        }

        throw new Error("Impossible");
    }

    private static long extractYear(long timestamp) {
        if (timestamp < 0L) {
            throw new IllegalArgumentException("Timestamp (" + timestamp + ") < 0");
        }

        ExtractedYearMonth eym = EXTRACTED_YEAR_MONTH.get();

        if (eym.extractedYearTimestamp == timestamp) {
            return eym.extractedYear;
        }

        int year = 1970 + (int) (timestamp / YEAR_400) * 400;
        timestamp %= YEAR_400;

        if (timestamp < LEAP_YEAR_100) {
            year += (int) (timestamp / YEAR_4) * 4;
            timestamp %= YEAR_4;
        } else {
            if (timestamp < LEAP_YEAR_200) {
                timestamp -= LEAP_YEAR_100;
                year += 100;
            } else if (timestamp < LEAP_YEAR_300) {
                timestamp -= LEAP_YEAR_200;
                year += 200;
            } else {
                timestamp -= LEAP_YEAR_300;
                year += 300;
            }

            if (timestamp < YEAR_28) {
                year += (int) (timestamp / YEAR_4) * 4;
                timestamp %= YEAR_4;
            } else {
                year += 28;
                timestamp -= YEAR_28;
                if (timestamp < LEAP_YEAR_4) {
                    // no-op
                } else {
                    year += 4;
                    timestamp -= LEAP_YEAR_4;

                    year += (int) (timestamp / YEAR_4) * 4;
                    timestamp %= YEAR_4;
                }
            }
        }

        while (true) {
            long yearLength = isLeapYear(year) ? LEAP_YEAR : YEAR;

            if (timestamp >= yearLength) {
                timestamp -= yearLength;
                year += 1;
            } else {
                break;
            }
        }

        long result = (((long) year) << 32) + (timestamp / DAY);

        eym.extractedYearTimestamp = timestamp;
        eym.extractedYear = result;

        return result;
    }

    private static boolean isLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    private static int writeIntegerWidth(int value, int width, ByteBuffer buffer) {
        if (value < 0) {
            throw new IllegalArgumentException("Value (" + value + ") < 0");
        }

        if (width < 0) {
            throw new IllegalArgumentException("Width (" + width + ") < 0");
        }

        int written = 0;
        int v = value;
        int n = 0;

        while (v != 0) {
            v /= 10;
            n += 1;
        }

        if (n == 0) {
            n = 1;
        }

        if (n < width) {
            int count = width - n;

            while (count >= 4) {
                buffer.put((byte) '0');
                buffer.put((byte) '0');
                buffer.put((byte) '0');
                buffer.put((byte) '0');
                count -= 4;
                written = written + 4;
            }

            switch (count) {
                case 3:
                    buffer.put((byte) '0');
                    buffer.put((byte) '0');
                    buffer.put((byte) '0');
                    written = written + 3;
                    break;
                case 2:
                    buffer.put((byte) '0');
                    buffer.put((byte) '0');
                    written = written + 2;
                    break;
                case 1:
                    buffer.put((byte) '0');
                    written++;
                    break;
                case 0:
                    // no-op
                    break;
                default:
                    throw new Error("Impossible");
            }
        }

        written = written + IntWriter.write(value, buffer);
        return written;
    }

    private class Literal implements LongFormatter {
        private final String literal;

        public Literal(String literal) {
            this.literal = literal;
        }

        public int format(long value, ByteBuffer buffer) {
            return CharSequenceWriter.write(literal, buffer);
        }
    }

    private static class AbbreviatedMonthName implements LongFormatter {
        private final static AbbreviatedMonthName INSTANCE = new AbbreviatedMonthName();

        private final static String[] MONTH_NAMES = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        private AbbreviatedMonthName() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            String monthName = MONTH_NAMES[getMonth(value)];
            return CharSequenceWriter.write(monthName, buffer);
        }
    }

    private static class AbbreviatedWeekdayName implements LongFormatter {
        private final static AbbreviatedWeekdayName INSTANCE = new AbbreviatedWeekdayName();

        private final static String[] WEEKDAY_NAMES = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        private AbbreviatedWeekdayName() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            String weekdayName = WEEKDAY_NAMES[getDayOfWeek(value)];
            return CharSequenceWriter.write(weekdayName, buffer);
        }
    }

    private static class AMPM implements LongFormatter {
        private final static AMPM INSTANCE = new AMPM();

        private AMPM() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            int hour = getHour(value);

            String ampm = hour < 12 ? "AM" : "PM";
            return CharSequenceWriter.write(ampm, buffer);
        }
    }

    private static class DayOfMonthNumeric implements LongFormatter {
        private final static DayOfMonthNumeric INSTANCE = new DayOfMonthNumeric();

        private DayOfMonthNumeric() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            int dayOfMonth = getDayOfMonth(value);
            return IntWriter.write(dayOfMonth + 1, buffer);
        }
    }

    private static class DayOfMonthNumeric2 implements LongFormatter {
        private final static DayOfMonthNumeric2 INSTANCE = new DayOfMonthNumeric2();

        private DayOfMonthNumeric2() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            if (buffer == null) {
                throw new IllegalArgumentException("buffer is null");
            }

            int dayOfMonth = getDayOfMonth(value);

            return writeIntegerWidth(dayOfMonth + 1, 2, buffer);
        }
    }

    private static class DayOfWeek implements LongFormatter {
        private final static DayOfWeek INSTANCE = new DayOfWeek();

        private DayOfWeek() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            int dayOfWeek = getDayOfWeek(value);
            return IntWriter.write(dayOfWeek + 1, buffer);
        }
    }

    private static class DayOfYear3 implements LongFormatter {
        private final static DayOfYear3 INSTANCE = new DayOfYear3();

        private DayOfYear3() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            int dayOfYear = getDayOfYear(value);
            return writeIntegerWidth(dayOfYear + 1, 3, buffer);
        }
    }

    private static class Hour12 implements LongFormatter {
        private final static Hour12 INSTANCE = new Hour12();

        private Hour12() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            int hour = getHour(value) % 12;

            if (hour == 0) {
                hour = 12;
            }

            return IntWriter.write(hour, buffer);
        }
    }

    private static class Hour12_2 implements LongFormatter {
        private final static Hour12_2 INSTANCE = new Hour12_2();

        private Hour12_2() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            int hour = getHour(value) % 12;
            if (hour == 0) {
                hour = 12;
            }
            return writeIntegerWidth(hour, 2, buffer);
        }
    }

    private static class Hour24 implements LongFormatter {
        private final static Hour24 INSTANCE = new Hour24();

        private Hour24() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            int hour = getHour(value);
            return IntWriter.write(hour, buffer);
        }
    }

    private static class Hour24_2 implements LongFormatter {
        private final static Hour24_2 INSTANCE = new Hour24_2();

        private Hour24_2() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            int hour = getHour(value);
            return writeIntegerWidth(hour, 2, buffer);
        }
    }

    private static class Milliseconds implements LongFormatter {
        private final static Milliseconds INSTANCE = new Milliseconds();

        private Milliseconds() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            int milliseconds = getMillisecond(value);
            return IntWriter.write(milliseconds, buffer);
        }
    }

    private static class Milliseconds3 implements LongFormatter {
        private final static Milliseconds3 INSTANCE = new Milliseconds3();

        private Milliseconds3() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            int milliseconds = getMillisecond(value);
            return writeIntegerWidth(milliseconds, 3, buffer);
        }
    }

    private static class Minutes2 implements LongFormatter {
        private final static Minutes2 INSTANCE = new Minutes2();

        private Minutes2() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            int minutes = getMinute(value);
            return writeIntegerWidth(minutes, 2, buffer);
        }
    }

    private static class MonthName implements LongFormatter {
        private final static MonthName INSTANCE = new MonthName();

        private final static String[] MONTH_NAMES = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September",
                                                                 "October", "November", "December"};

        private MonthName() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            String monthName = MONTH_NAMES[getMonth(value)];
            return CharSequenceWriter.write(monthName, buffer);
        }
    }

    private static class MonthNumeric implements LongFormatter {
        private final static MonthNumeric INSTANCE = new MonthNumeric();

        private MonthNumeric() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            return IntWriter.write(getMonth(value) + 1, buffer);
        }
    }

    private static class MonthNumeric2 implements LongFormatter {
        private final static MonthNumeric2 INSTANCE = new MonthNumeric2();

        private MonthNumeric2() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            return writeIntegerWidth(getMonth(value) + 1, 2, buffer);
        }
    }

    private static class Seconds implements LongFormatter {
        private final static Seconds INSTANCE = new Seconds();

        private Seconds() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            return IntWriter.write(getSecond(value), buffer);
        }
    }

    private static class Seconds2 implements LongFormatter {
        private final static Seconds2 INSTANCE = new Seconds2();

        private Seconds2() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            return writeIntegerWidth(getSecond(value), 2, buffer);
        }
    }

    private static class WeekdayName implements LongFormatter {
        private final static WeekdayName INSTANCE = new WeekdayName();

        private final static String[] WEEKDAY_NAMES = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        private WeekdayName() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            String weekdayName = WEEKDAY_NAMES[getDayOfWeek(value)];
            return CharSequenceWriter.write(weekdayName, buffer);
        }
    }

    private static class Year2 implements LongFormatter {
        private final static Year2 INSTANCE = new Year2();

        private Year2() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            return writeIntegerWidth(getYear(value) % 100, 2, buffer);
        }
    }

    private static class Year4 implements LongFormatter {
        private final static Year4 INSTANCE = new Year4();

        private Year4() {
            // no-op
        }

        public int format(long value, ByteBuffer buffer) {
            return writeIntegerWidth(getYear(value), 4, buffer);
        }
    }

    private interface LongFormatter {
        public int format(long value, ByteBuffer buffer);
    }

    private static class ExtractedYearMonth {
        public long extractedYearTimestamp = Long.MIN_VALUE;
        public long extractedYear = Long.MIN_VALUE;
        public long extractedMonthTimestamp = Long.MIN_VALUE;
        public long extractedMonth = Long.MIN_VALUE;
    }
}


