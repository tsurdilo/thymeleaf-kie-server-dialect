package org.jbpm.addons.util;

public class KieServerDialectUtils {

    public static boolean isExpression(String value) {
        return value != null && value.startsWith("${") && value.endsWith("}");
    }
}
