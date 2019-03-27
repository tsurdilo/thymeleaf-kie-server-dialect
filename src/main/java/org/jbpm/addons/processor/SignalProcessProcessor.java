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

public class SignalProcessProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "signalprocess";
    private static final String DEFAULT_FRAGMENT_NAME = "kieserverdialect :: signalprocess";
    private static final int PRECEDENCE = 10000;

    private final ApplicationContext ctx;

    public SignalProcessProcessor(final String dialectPrefix, ApplicationContext ctx) {

        super(TemplateMode.HTML, dialectPrefix, TAG_NAME, true, null, false, PRECEDENCE);

        this.ctx = ctx;
    }

    @Override
    protected void doProcess(ITemplateContext templateContext, IProcessableElementTag signalProcessTag,
            IElementTagStructureHandler structureHandler) {

        ProcessService processService = (ProcessService) ctx.getBean("processService");

        String deploymentIdAttrValue = signalProcessTag.getAttributeValue("deploymentid");
        String processInstanceIdAttrValue = signalProcessTag.getAttributeValue("processinstanceid");
        String signalNameAttrValue = signalProcessTag.getAttributeValue("signalname");
        String eventAttrValue = signalProcessTag.getAttributeValue("event");

        String deploymentId;
        Long processInstanceId;
        String signalName;
        Object event = null;

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

        if (isExpression(signalNameAttrValue)) {
            IStandardExpression signalNameExpression = parser.parseExpression(templateContext, signalNameAttrValue);

            signalName = (String) signalNameExpression.execute(templateContext);
            if (signalName == null) {
                throw new IllegalArgumentException(
                        "Unable to resolve expression for signal name: " + signalNameAttrValue);
            }
        } else {
            signalName = signalNameAttrValue;
        }

        if (isExpression(eventAttrValue)) {
            IStandardExpression eventExpression = parser.parseExpression(templateContext, eventAttrValue);

            event = eventExpression.execute(templateContext);
        }

        try {

            processService.signalProcessInstance(deploymentId, processInstanceId, signalName, event);

            structureHandler.setLocalVariable("signalledpid", processInstanceId);
        } catch (Exception e) {
            structureHandler.setLocalVariable("signalprocesserror", e.getMessage());
        }

        final IModelFactory modelFactory = templateContext.getModelFactory();
        final IModel model = modelFactory.createModel();

        model.add(modelFactory.createOpenElementTag("div", "th:replace", getFragmentName(
                signalProcessTag.getAttributeValue("fragment"), DEFAULT_FRAGMENT_NAME, parser, templateContext)));
        model.add(modelFactory.createCloseElementTag("div"));
        structureHandler.replaceWith(model, true);
    }
}
