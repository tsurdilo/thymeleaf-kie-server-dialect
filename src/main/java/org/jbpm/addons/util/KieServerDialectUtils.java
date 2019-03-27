package org.jbpm.addons.util;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;

public class KieServerDialectUtils {

    public static boolean isExpression(String value) {
        return value != null && value.startsWith("${") && value.endsWith("}");
    }

    public static String getFragmentName(String value,
                                         String defaulValue,
                                         IStandardExpressionParser parser,
                                         ITemplateContext templateContext) {

        if (value == null || value.trim().length() < 1) {
            return defaulValue;
        } else {
            if (isExpression(value)) {
                IStandardExpression expression = parser.parseExpression(templateContext,
                                                                        value);

                return (String) expression.execute(templateContext);
            } else {
                return value;
            }
        }
    }
}
