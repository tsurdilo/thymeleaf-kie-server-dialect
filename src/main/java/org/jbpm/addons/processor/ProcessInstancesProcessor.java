package org.jbpm.addons.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.kie.api.runtime.query.QueryContext;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import static org.jbpm.addons.util.KieServerDialectUtils.getFragmentName;

public class ProcessInstancesProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "processinstances";
    private static final String DEFAULT_FRAGMENT_NAME = "kieserverdialect :: showprocessinstances";
    private static final int PRECEDENCE = 10000;

    private final ApplicationContext ctx;

    public ProcessInstancesProcessor(final String dialectPrefix, ApplicationContext ctx) {

        super(TemplateMode.HTML, dialectPrefix, TAG_NAME, true, null, false, PRECEDENCE);

        this.ctx = ctx;
    }

    @Override
    protected void doProcess(ITemplateContext templateContext, IProcessableElementTag processInstancesTag,
            IElementTagStructureHandler structureHandler) {

        RuntimeDataService runtimeDataService = (RuntimeDataService) ctx.getBean("runtimeDataService");
        Collection<ProcessInstanceDesc> processInstances = runtimeDataService.getProcessInstances(new QueryContext());

        Map<Long, List<UserTaskInstanceDesc>> createdTasks = new HashMap<Long, List<UserTaskInstanceDesc>>();

        for (ProcessInstanceDesc pdesc : processInstances) {
            List<Long> processinstancetasks = runtimeDataService.getTasksByProcessInstanceId(pdesc.getId());
            for (Long nexttaskid : processinstancetasks) {
                UserTaskInstanceDesc usertaskdesc = runtimeDataService.getTaskById(nexttaskid);
                if (usertaskdesc.getStatus() != null && (usertaskdesc.getStatus().equals("Created")
                        || usertaskdesc.getStatus().equals("Reserved"))) {
                    if (createdTasks.containsKey(pdesc.getId())) {
                        createdTasks.get(pdesc.getId()).add(usertaskdesc);
                    } else {
                        createdTasks.put(pdesc.getId(), new ArrayList<UserTaskInstanceDesc>());
                        createdTasks.get(pdesc.getId()).add(usertaskdesc);
                    }
                }
            }
        }

        final IEngineConfiguration configuration = templateContext.getConfiguration();
        IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);

        structureHandler.setLocalVariable("processinstances", processInstances);

        structureHandler.setLocalVariable("createdtasks", createdTasks);

        final IModelFactory modelFactory = templateContext.getModelFactory();
        final IModel model = modelFactory.createModel();

        model.add(modelFactory.createOpenElementTag("div", "th:replace", getFragmentName(
                processInstancesTag.getAttributeValue("fragment"), DEFAULT_FRAGMENT_NAME, parser, templateContext)));
        model.add(modelFactory.createCloseElementTag("div"));
        structureHandler.replaceWith(model, true);
    }
}
