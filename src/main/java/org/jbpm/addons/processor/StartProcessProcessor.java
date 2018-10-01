/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.addons.processor;

import java.util.ArrayList;
import java.util.List;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.IElementNameProcessorMatcher;
import org.thymeleaf.processor.element.AbstractMarkupSubstitutionElementProcessor;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.dom.Text;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.ProcessService;
import org.kie.server.springboot.jbpm.ContainerAliasResolver;
import org.thymeleaf.Configuration;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.standard.expression.IStandardExpression;
import java.util.Map;

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

        final ApplicationContext appCtx =
               ((SpringWebContext) arguments.getContext()).getApplicationContext();

        //DeploymentService deploymentService = (DeploymentService) appCtx.getBean("deploymentService");
        //RuntimeDataService runtimeDataService = (RuntimeDataService) appCtx.getBean("runtimeDataService");
        ProcessService processService = (ProcessService) appCtx.getBean("processService");
        //ContainerAliasResolver aliasResolver = (ContainerAliasResolver) appCtx.getBean("aliasResolver");

        //System.out.println("DEPLOYMENTSERVICE: " + deploymentService.toString());
        //System.out.println("RUNTIMEDATASERVICE: " + runtimeDataService.toString());
        //System.out.println("PROCESSSERVICE: " + processService.toString());

        String containerIdAttrValue = element.getAttributeValue("containerid");
        String processIdAttrValue = element.getAttributeValue("processid");
        String processInputsAttrValue = element.getAttributeValue("processinputs");

        Configuration configuration = arguments.getConfiguration();
        IStandardExpressionParser parser =
                StandardExpressions.getExpressionParser(configuration);

        IStandardExpression containerIdExpression =
                parser.parseExpression(configuration, arguments, containerIdAttrValue);

        String containerId =
                (String) containerIdExpression.execute(configuration, arguments);

        IStandardExpression processIdExpression =
                parser.parseExpression(configuration, arguments, processIdAttrValue);

        String processId =
                (String) processIdExpression.execute(configuration, arguments);

        IStandardExpression processInputsExpression =
                parser.parseExpression(configuration, arguments, processInputsAttrValue);

        Map<String, Object> processInputs =
                (Map<String, Object>) processInputsExpression.execute(configuration, arguments);

        if(processId == null) {
            throw new IllegalArgumentException("Unable to resolve expression for processid: " + processId);
        }

        if(containerId == null) {
            throw new IllegalArgumentException("Unable to resolve expression for containerid: " + containerId);
        }

        long processInstanceId;

        if(processId == null) {
            processInstanceId = processService.startProcess(containerId, processId);
        } else {
            processInstanceId = processService.startProcess(containerId, processId, processInputs);
        }

        final Element container = new Element("div");


        final Text text = new Text("process instance id: " + processInstanceId);
        container.addChild(text);


        final List<Node> nodes = new ArrayList<Node>();
        nodes.add(container);
        return nodes;
    }
}
