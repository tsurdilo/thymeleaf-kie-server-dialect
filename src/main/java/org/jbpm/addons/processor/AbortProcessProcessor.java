package org.jbpm.addons.processor;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.services.api.ProcessService;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.IElementNameProcessorMatcher;
import org.thymeleaf.processor.element.AbstractMarkupSubstitutionElementProcessor;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

import static org.jbpm.addons.util.KieServerDialectUtils.isExpression;

public class AbortProcessProcessor extends AbstractMarkupSubstitutionElementProcessor {

    private static final String ATTR_NAME = "abortprocess";
    private static final int PRECEDENCE = 10000;

    public AbortProcessProcessor(String elementName) {
        super(elementName);
    }

    public AbortProcessProcessor() {
        super(ATTR_NAME);
    }

    public AbortProcessProcessor(IElementNameProcessorMatcher matcher) {
        super(matcher);
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE;
    }

    @Override
    protected List<Node> getMarkupSubstitutes(
            final Arguments arguments,
            final Element element) {

        ApplicationContext appCtx =
                ((SpringWebContext) arguments.getContext()).getApplicationContext();

        ProcessService processService = (ProcessService) appCtx.getBean("processService");

        String deploymentIdAttrValue = element.getAttributeValue("deploymentid");
        String processInstanceIdAttrValue = element.getAttributeValue("processinstanceid");

        String deploymentId;
        Long processInstanceId;

        Configuration configuration = arguments.getConfiguration();
        IStandardExpressionParser parser =
                StandardExpressions.getExpressionParser(configuration);

        if (isExpression(deploymentIdAttrValue)) {
            IStandardExpression deploymentIdExpression =
                    parser.parseExpression(configuration,
                                           arguments,
                                           deploymentIdAttrValue);

            deploymentId =
                    (String) deploymentIdExpression.execute(configuration,
                                                            arguments);

            if (deploymentId == null) {
                throw new IllegalArgumentException("Unable to resolve expression for deployment id: " + deploymentIdAttrValue);
            }
        } else {
            deploymentId = deploymentIdAttrValue;
        }

        if (isExpression(processInstanceIdAttrValue)) {
            IStandardExpression processInstanceIdExpression =
                    parser.parseExpression(configuration,
                                           arguments,
                                           processInstanceIdAttrValue);

            processInstanceId =
                    (Long) processInstanceIdExpression.execute(configuration,
                                                               arguments);
        } else {
            processInstanceId = Long.parseLong(processInstanceIdAttrValue);
        }

        try {
            processService.abortProcessInstance(deploymentId,
                                                processInstanceId);

            arguments.getContext().getVariables().put("abortedpid",
                                                      processInstanceId);
        } catch (Exception e) {
            arguments.getContext().getVariables().put("abortprocesserror",
                                                      e.getMessage());
        }

        Element container = new Element("div");
        container.setAttribute("th:replace",
                               "kieserverdialect :: abortprocess");

        List<Node> nodes = new ArrayList<Node>();
        nodes.add(container);
        return nodes;
    }
}
