package org.jbpm.addons.processor;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.model.DeployedUnit;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import static org.jbpm.addons.util.KieServerDialectUtils.getFragmentName;
import static org.jbpm.addons.util.KieServerDialectUtils.isExpression;

public class DeployedUnitProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "deployments";
    private static final String DEFAULT_FRAGMENT_NAME = "kieserverdialect :: showdeployedunits";
    private static final int PRECEDENCE = 10000;

    private final ApplicationContext ctx;

    public DeployedUnitProcessor(final String dialectPrefix, ApplicationContext ctx) {

        super(TemplateMode.HTML, dialectPrefix, TAG_NAME, true, null, false, PRECEDENCE);

        this.ctx = ctx;
    }

    @Override
    protected void doProcess(ITemplateContext templateContext, IProcessableElementTag deployedUnitTag,
            IElementTagStructureHandler structureHandler) {

        String deploymentUnitNameAttrValue = deployedUnitTag.getAttributeValue("deploymentid");
        String deploymentUnitName;

        DeploymentService deploymentService = (DeploymentService) ctx.getBean("deploymentService");
        List<DeployedUnit> deployedUnitList = new ArrayList<DeployedUnit>();

        final IEngineConfiguration configuration = templateContext.getConfiguration();
        IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);

        if (deploymentUnitNameAttrValue != null && !deploymentUnitNameAttrValue.isEmpty()) {
            if (isExpression(deploymentUnitNameAttrValue)) {
                IStandardExpression deploymentUnitExpression = parser.parseExpression(templateContext,
                        deploymentUnitNameAttrValue);

                deploymentUnitName = (String) deploymentUnitExpression.execute(templateContext);

                if (deploymentUnitName == null) {
                    throw new IllegalArgumentException(
                            "Unable to resolve expression for deployment unit: " + deploymentUnitNameAttrValue);
                }
            } else {
                deploymentUnitName = deploymentUnitNameAttrValue;
            }
            deployedUnitList.add(deploymentService.getDeployedUnit(deploymentUnitName));
        } else {
            // get all
            deployedUnitList.addAll(deploymentService.getDeployedUnits());
        }

        structureHandler.setLocalVariable("deployedunits", deployedUnitList);

        final IModelFactory modelFactory = templateContext.getModelFactory();
        final IModel model = modelFactory.createModel();

        model.add(modelFactory.createOpenElementTag("div", "th:replace", getFragmentName(
                deployedUnitTag.getAttributeValue("fragment"), DEFAULT_FRAGMENT_NAME, parser, templateContext)));
        model.add(modelFactory.createCloseElementTag("div"));
        structureHandler.replaceWith(model, true);
    }
}
