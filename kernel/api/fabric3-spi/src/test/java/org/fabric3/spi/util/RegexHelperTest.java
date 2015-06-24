package org.fabric3.spi.util;

import junit.framework.TestCase;

public class RegexHelperTest extends TestCase {

    public void testExpandEntireExpression() throws Exception {
        String result = RegexHelper.expandExpression("${bar}", (s -> "foo"));
        assertEquals("foo", result);
    }

    public void testExpandPartialEndExpression() throws Exception {
        String result = RegexHelper.expandExpression("foo/${bar}", (s -> "foo"));
        assertEquals("foo/foo", result);
    }

    public void testExpandPartialStartExpression() throws Exception {
        String result = RegexHelper.expandExpression("${bar}/foo", (s -> "foo"));
        assertEquals("foo/foo", result);
    }

    public void testNoExpand() throws Exception {
        String result = RegexHelper.expandExpression("{bar}/foo", (s -> "foo"));
        assertEquals("{bar}/foo", result);
    }

    public void testNoExpandClosing() throws Exception {
        String result = RegexHelper.expandExpression("${bar/foo", (s -> "foo"));
        assertEquals("${bar/foo", result);
    }

}