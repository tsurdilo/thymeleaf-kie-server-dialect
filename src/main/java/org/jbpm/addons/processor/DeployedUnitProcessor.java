package org.jbpm.addons.processor;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.model.DeployedUnit;
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

import static org.jbpm.addons.util.KieServerDialectUtils.getFragmentName;
import static org.jbpm.addons.util.KieServerDialectUtils.isExpression;

public class DeployedUnitProcessor extends AbstractMarkupSubstitutionElementProcessor {

    private static final String ATTR_NAME = "deployments";
    private static final String DEFAULT_FRAGMENT_NAME = "kieserverdialect :: showdeployedunits";
    private static final int PRECEDENCE = 10000;

    public DeployedUnitProcessor(String elementName) {
        super(elementName);
    }

    public DeployedUnitProcessor() {
        super(ATTR_NAME);
    }

    public DeployedUnitProcessor(IElementNameProcessorMatcher matcher) {
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

        String deploymentUnitNameAttrValue = element.getAttributeValue("deploymentid");
        String deploymentUnitName;

        Configuration configuration = arguments.getConfiguration();
        IStandardExpressionParser parser =
                StandardExpressions.getExpressionParser(configuration);

        DeploymentService deploymentService = (DeploymentService) appCtx.getBean("deploymentService");
        List<DeployedUnit> deployedUnitList = new ArrayList<DeployedUnit>();

        if (deploymentUnitNameAttrValue != null && !deploymentUnitNameAttrValue.isEmpty()) {
            if (isExpression(deploymentUnitNameAttrValue)) {
                IStandardExpression deploymentUnitExpression =
                        parser.parseExpression(configuration,
                                               arguments,
                                               deploymentUnitNameAttrValue);

                deploymentUnitName =
                        (String) deploymentUnitExpression.execute(configuration,
                                                                  arguments);

                if (deploymentUnitName == null) {
                    throw new IllegalArgumentException("Unable to resolve expression for deployment unit: " + deploymentUnitNameAttrValue);
                }
            } else {
                deploymentUnitName = deploymentUnitNameAttrValue;
            }
            deployedUnitList.add(deploymentService.getDeployedUnit(deploymentUnitName));
        } else {
            // get all
            deployedUnitList.addAll(deploymentService.getDeployedUnits());
        }

        arguments.getContext().getVariables().put("deployedunits",
                                                  deployedUnitList);

        Element container = new Element("div");
        container.setAttribute("th:replace",
                               getFragmentName(element.getAttributeValue("fragment"),
                                               DEFAULT_FRAGMENT_NAME,
                                               parser,
                                               configuration,
                                               arguments));

        List<Node> nodes = new ArrayList<Node>();
        nodes.add(container);
        return nodes;
    }
}
