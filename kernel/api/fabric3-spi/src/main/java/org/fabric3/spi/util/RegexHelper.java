package org.fabric3.spi.util;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class RegexHelper {
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    /**
     * Expands the given expression containing a variable of the form ${..} using the provided function.
     *
     * @param expression the expression
     * @param function   the function
     * @return the expanded expression
     */
    public static String expandExpression(String expression, Function<String, String> function) {
        Matcher matcher = VAR_PATTERN.matcher(expression);
        if (matcher.find()) {
            String var = matcher.group();
            String expanded = function.apply(var);
            return matcher.replaceFirst(expanded);

        }
        return expression;

    }

    private RegexHelper() {
    }
}
