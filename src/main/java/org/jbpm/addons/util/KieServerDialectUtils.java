package org.jbpm.addons.util;

import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;

public class KieServerDialectUtils {

    public static boolean isExpression(String value) {
        return value != null && value.startsWith("${") && value.endsWith("}");
    }

    public static String getFragmentName(String value,
                                         String defaulValue,
                                         IStandardExpressionParser parser,
                                         Configuration configuration,
                                         Arguments arguments) {

        if (value == null || value.trim().length() < 1) {
            return defaulValue;
        } else {
            if (isExpression(value)) {
                IStandardExpression deploymentIdExpression =
                        parser.parseExpression(configuration,
                                               arguments,
                                               value);

                return (String) deploymentIdExpression.execute(configuration,
                                                               arguments);
            } else {
                return value;
            }
        }
    }
}
