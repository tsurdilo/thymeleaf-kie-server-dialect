package org.jbpm.addons.processor;

import org.jbpm.services.api.ProcessService;
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

public class AbortProcessProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "abortprocess";
    private static final String DEFAULT_FRAGMENT_NAME = "kieserverdialect :: abortprocess";
    private static final int PRECEDENCE = 10000;

    private final ApplicationContext ctx;

    public AbortProcessProcessor(final String dialectPrefix, ApplicationContext ctx) {

        super(TemplateMode.HTML, dialectPrefix, TAG_NAME, true, null, false, PRECEDENCE);

        this.ctx = ctx;
    }

    @Override
    protected void doProcess(ITemplateContext templateContext, IProcessableElementTag abortProcessTag,
            IElementTagStructureHandler structureHandler) {

        ProcessService processService = (ProcessService) ctx.getBean("processService");

        String deploymentIdAttrValue = abortProcessTag.getAttributeValue("deploymentid");
        String processInstanceIdAttrValue = abortProcessTag.getAttributeValue("processinstanceid");

        String deploymentId;
        Long processInstanceId;

        final IEngineConfiguration configuration = templateContext.getConfiguration();
        IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);

        if (isExpression(deploymentIdAttrValue)) {
            IStandardExpression deploymentIdExpression = parser.parseExpression(templateContext, deploymentIdAttrValue);

            deploymentId = (String) deploymentIdExpression.execute(templateContext);

            if (deploymentId == null) {
                throw new IllegalArgumentException(
                        "Unable to resolve expression for deployment id: " + deploymentIdAttrValue);
            }
        } else {
            deploymentId = deploymentIdAttrValue;
        }

        if (isExpression(processInstanceIdAttrValue)) {
            IStandardExpression processInstanceIdExpression = parser.parseExpression(templateContext,
                    processInstanceIdAttrValue);

            processInstanceId = (Long) processInstanceIdExpression.execute(templateContext);
        } else {
            processInstanceId = Long.parseLong(processInstanceIdAttrValue);
        }

        try {
            processService.abortProcessInstance(deploymentId, processInstanceId);

            structureHandler.setLocalVariable("abortedpid", processInstanceId);
        } catch (Exception e) {
            structureHandler.setLocalVariable("abortprocesserror", e.getMessage());
        }

        final IModelFactory modelFactory = templateContext.getModelFactory();
        final IModel model = modelFactory.createModel();

        model.add(modelFactory.createOpenElementTag("div", "th:replace", getFragmentName(
                abortProcessTag.getAttributeValue("fragment"), DEFAULT_FRAGMENT_NAME, parser, templateContext)));
        model.add(modelFactory.createCloseElementTag("div"));
        structureHandler.replaceWith(model, true);
    }
}
