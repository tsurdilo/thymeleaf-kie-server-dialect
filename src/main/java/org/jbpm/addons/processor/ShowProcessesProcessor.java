package org.jbpm.addons.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.kie.api.runtime.query.QueryContext;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.IElementNameProcessorMatcher;
import org.thymeleaf.processor.element.AbstractMarkupSubstitutionElementProcessor;
import org.thymeleaf.spring4.context.SpringWebContext;

public class ShowProcessesProcessor extends AbstractMarkupSubstitutionElementProcessor {

    private static final String ATTR_NAME = "showprocesses";
    private static final int PRECEDENCE = 10000;

    public ShowProcessesProcessor(String elementName) {
        super(elementName);
    }

    public ShowProcessesProcessor() {
        super(ATTR_NAME);
    }

    public ShowProcessesProcessor(IElementNameProcessorMatcher matcher) {
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


        RuntimeDataService runtimeDataService = (RuntimeDataService) appCtx.getBean("runtimeDataService");
        Collection<ProcessDefinition> processDefinitions = runtimeDataService.getProcesses(new QueryContext());
        arguments.getContext().getVariables().put("processdefs",
                                                  processDefinitions);

        Element container = new Element("div");
        container.setAttribute("th:replace",
                               "kieserverdialect :: showprocesses");

        List<Node> nodes = new ArrayList<Node>();
        nodes.add(container);
        return nodes;
    }
}
