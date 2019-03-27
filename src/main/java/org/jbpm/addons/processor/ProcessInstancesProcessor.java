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
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.IElementNameProcessorMatcher;
import org.thymeleaf.processor.element.AbstractMarkupSubstitutionElementProcessor;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

import javax.servlet.http.HttpServletRequest;

import static org.jbpm.addons.util.KieServerDialectUtils.getFragmentName;

public class ProcessInstancesProcessor extends AbstractMarkupSubstitutionElementProcessor {

    private static final String ATTR_NAME = "processinstances";
    private static final String DEFAULT_FRAGMENT_NAME = "kieserverdialect :: showprocessinstances";
    private static final String PAGE_PARAM_NAME = "page";
    private static final String PAGE_SIZE_PARAM_NAME = "pageSize";
    private static final int PRECEDENCE = 10000;
    private static final int PAGE_SIZE_DEFAULT = 10;

    public ProcessInstancesProcessor(String elementName) {
        super(elementName);
    }

    public ProcessInstancesProcessor() {
        super(ATTR_NAME);
    }

    public ProcessInstancesProcessor(IElementNameProcessorMatcher matcher) {
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

        SpringWebContext swContext = (SpringWebContext) arguments.getContext();

        ApplicationContext appCtx = swContext.getApplicationContext();
        HttpServletRequest webCtx = swContext.getHttpServletRequest();

        int pageSize = this.getIntParam(webCtx.getParameter(PAGE_SIZE_PARAM_NAME), PAGE_SIZE_DEFAULT);
        int page = this.getIntParam(webCtx.getParameter(PAGE_PARAM_NAME), 1);
        int offSet = pageSize * (page - 1);

        Configuration configuration = arguments.getConfiguration();
        IStandardExpressionParser parser =
                StandardExpressions.getExpressionParser(configuration);

        RuntimeDataService runtimeDataService = (RuntimeDataService) appCtx.getBean("runtimeDataService");
        QueryContext queryContext = new QueryContext(offSet, pageSize);

        Collection<ProcessInstanceDesc> processInstances = runtimeDataService.getProcessInstances(queryContext);

        Map<Long, List<UserTaskInstanceDesc>> createdTasks = new HashMap<Long, List<UserTaskInstanceDesc>>();

        for(ProcessInstanceDesc pdesc : processInstances) {
            List<Long> processinstancetasks = runtimeDataService.getTasksByProcessInstanceId(pdesc.getId());
            for(Long nexttaskid : processinstancetasks) {
                UserTaskInstanceDesc usertaskdesc =  runtimeDataService.getTaskById(nexttaskid);
                if(usertaskdesc.getStatus() != null && (usertaskdesc.getStatus().equals("Created") || usertaskdesc.getStatus().equals("Reserved"))) {
                    if(createdTasks.containsKey(pdesc.getId())) {
                        createdTasks.get(pdesc.getId()).add(usertaskdesc);
                    } else {
                        createdTasks.put(pdesc.getId(), new ArrayList<UserTaskInstanceDesc>());
                        createdTasks.get(pdesc.getId()).add(usertaskdesc);
                    }
                }
            }
        }

        arguments.getContext().getVariables().put("processinstances",
                processInstances);

        arguments.getContext().getVariables().put("createdtasks",
                createdTasks);

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

    private int getIntParam(String strValue, int defaultValue) {
        int value;

        if (strValue != null && !strValue.isEmpty()) {
            try {
                value = Integer.valueOf(strValue);
            } catch (NumberFormatException e) {
                value = defaultValue;
            }
        } else {
            value = defaultValue;
        }

        // No zero values
        value = value == 0 ? 1 : value;
        // No negative values
        value = value < 0 ? value + -1 : value;

        return value;
    }
}