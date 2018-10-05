package org.jbpm.addons.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class StartProcessProcessor extends AbstractMarkupSubstitutionElementProcessor {

    private static final String ATTR_NAME = "startprocess";
    private static final int PRECEDENCE = 10000;

    public StartProcessProcessor(String elementName) {
        super(elementName);
    }

    public StartProcessProcessor() {
        super(ATTR_NAME);
    }

    public StartProcessProcessor(IElementNameProcessorMatcher matcher) {
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

        String containerIdAttrValue = element.getAttributeValue("containerid");
        String processIdAttrValue = element.getAttributeValue("processid");
        String processInputsAttrValue = element.getAttributeValue("processinputs");

        String containerId;
        String processId;
        Map<String, Object> processInputs = null;
        long processInstanceId;

        Configuration configuration = arguments.getConfiguration();
        IStandardExpressionParser parser =
                StandardExpressions.getExpressionParser(configuration);



        if (isExpression(containerIdAttrValue)) {
            IStandardExpression containerIdExpression =
                    parser.parseExpression(configuration,
                                           arguments,
                                           containerIdAttrValue);

            containerId =
                    (String) containerIdExpression.execute(configuration,
                                                           arguments);

            if (containerId == null) {
                throw new IllegalArgumentException("Unable to resolve expression for containerid: " + containerIdAttrValue);
            }
        } else {
            containerId = containerIdAttrValue;
        }

        if (isExpression(processIdAttrValue)) {
            IStandardExpression processIdExpression =
                    parser.parseExpression(configuration,
                                           arguments,
                                           processIdAttrValue);

            processId =
                    (String) processIdExpression.execute(configuration,
                                                         arguments);

            if (processId == null) {
                throw new IllegalArgumentException("Unable to resolve expression for processid: " + processIdAttrValue);
            }
        } else {
            processId = processIdAttrValue;
        }

        if (isExpression(processInputsAttrValue)) {
            IStandardExpression processInputsExpression =
                    parser.parseExpression(configuration,
                                           arguments,
                                           processInputsAttrValue);

            processInputs =
                    (Map<String, Object>) processInputsExpression.execute(configuration,
                                                                          arguments);
        }

        try {
            if (processInputs == null) {
                processInstanceId = processService.startProcess(containerId,
                                                                processId);
            } else {
                processInstanceId = processService.startProcess(containerId,
                                                                processId,
                                                                processInputs);
            }

            arguments.getContext().getVariables().put("startedpid",
                                                      processInstanceId);
        } catch (Exception e) {
            arguments.getContext().getVariables().put("startprocesserror",
                                                      e.getMessage());
        }

        Element container = new Element("div");
        container.setAttribute("th:replace",
                               "kieserverdialect :: startprocess");

        List<Node> nodes = new ArrayList<Node>();
        nodes.add(container);
        return nodes;
    }
}
