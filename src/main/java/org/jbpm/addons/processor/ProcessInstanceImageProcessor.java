package org.jbpm.addons.processor;

import java.util.Collection;

import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
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

public class ProcessInstanceImageProcessor extends AbstractElementTagProcessor {

        private static final String TAG_NAME = "processimages";
        private static final String DEFAULT_FRAGMENT_NAME = "kieserverdialect :: showprocessimages";
        private static final int PRECEDENCE = 10000;

        private final ApplicationContext ctx;

        public ProcessInstanceImageProcessor(final String dialectPrefix, ApplicationContext ctx) {

                super(TemplateMode.HTML, dialectPrefix, TAG_NAME, true, null, false, PRECEDENCE);

                this.ctx = ctx;
        }

        @Override
        protected void doProcess(ITemplateContext templateContext, IProcessableElementTag processInstanceImageTag,
                        IElementTagStructureHandler structureHandler) {

                RuntimeDataService runtimeDataService = (RuntimeDataService) ctx.getBean("runtimeDataService");
                Collection<ProcessInstanceDesc> processInstances = runtimeDataService
                                .getProcessInstances(new QueryContext());

                structureHandler.setLocalVariable("processinstances", processInstances);

                final IModelFactory modelFactory = templateContext.getModelFactory();
                final IModel model = modelFactory.createModel();

                final IEngineConfiguration configuration = templateContext.getConfiguration();
                IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);

                model.add(modelFactory.createOpenElementTag("div", "th:replace",
                                getFragmentName(processInstanceImageTag.getAttributeValue("fragment"),
                                                DEFAULT_FRAGMENT_NAME, parser, templateContext)));
                model.add(modelFactory.createCloseElementTag("div"));
                structureHandler.replaceWith(model, true);
        }
}
