package org.jbpm.addons.processor;

import java.util.Map;

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

public class StartProcessProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "startprocess";
    private static final String DEFAULT_FRAGMENT_NAME = "kieserverdialect :: startprocess";
    private static final int PRECEDENCE = 10000;

    private final ApplicationContext ctx;

    public StartProcessProcessor(final String dialectPrefix, ApplicationContext ctx) {

        super(TemplateMode.HTML, dialectPrefix, TAG_NAME, true, null, false, PRECEDENCE);

        this.ctx = ctx;
    }

    @Override
    protected void doProcess(ITemplateContext templateContext, IProcessableElementTag startProcessTag,
            IElementTagStructureHandler structureHandler) {

        ProcessService processService = (ProcessService) ctx.getBean("processService");

        String containerIdAttrValue = startProcessTag.getAttributeValue("containerid");
        String processIdAttrValue = startProcessTag.getAttributeValue("processid");
        String processInputsAttrValue = startProcessTag.getAttributeValue("processinputs");

        String containerId;
        String processId;
        Map<String, Object> processInputs = null;
        long processInstanceId;

        final IEngineConfiguration configuration = templateContext.getConfiguration();
        IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);

        if (isExpression(containerIdAttrValue)) {
            IStandardExpression containerIdExpression = parser.parseExpression(templateContext, containerIdAttrValue);

            containerId = (String) containerIdExpression.execute(templateContext);

            if (containerId == null) {
                throw new IllegalArgumentException(
                        "Unable to resolve expression for containerid: " + containerIdAttrValue);
            }
        } else {
            containerId = containerIdAttrValue;
        }

        if (isExpression(processIdAttrValue)) {
            IStandardExpression processIdExpression = parser.parseExpression(templateContext, processIdAttrValue);

            processId = (String) processIdExpression.execute(templateContext);

            if (processId == null) {
                throw new IllegalArgumentException("Unable to resolve expression for processid: " + processIdAttrValue);
            }
        } else {
            processId = processIdAttrValue;
        }

        if (isExpression(processInputsAttrValue)) {
            IStandardExpression processInputsExpression = parser.parseExpression(templateContext,
                    processInputsAttrValue);
            processInputs = (Map<String, Object>) processInputsExpression.execute(templateContext);
        }

        try {
            if (processInputs == null) {
                processInstanceId = processService.startProcess(containerId, processId);
            } else {
                processInstanceId = processService.startProcess(containerId, processId, processInputs);
            }

            structureHandler.setLocalVariable("startedpid", processInstanceId);
        } catch (Exception e) {
            structureHandler.setLocalVariable("startprocesserror", e.getMessage());
        }

        final IModelFactory modelFactory = templateContext.getModelFactory();
        final IModel model = modelFactory.createModel();

        model.add(modelFactory.createOpenElementTag("div", "th:replace", getFragmentName(
                startProcessTag.getAttributeValue("fragment"), DEFAULT_FRAGMENT_NAME, parser, templateContext)));
        model.add(modelFactory.createCloseElementTag("div"));
        structureHandler.replaceWith(model, true);
    }
}
