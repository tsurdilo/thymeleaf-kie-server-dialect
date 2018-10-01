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
import java.util.Map;

import org.jbpm.services.api.ProcessService;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;
import org.thymeleaf.processor.IElementNameProcessorMatcher;
import org.thymeleaf.processor.element.AbstractMarkupSubstitutionElementProcessor;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

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
        //ContainerAliasResolver aliasResolver = (ContainerAliasResolver) appCtx.getBean("aliasResolver");
        ProcessService processService = (ProcessService) appCtx.getBean("processService");

        String containerIdAttrValue = element.getAttributeValue("containerid");
        String processIdAttrValue = element.getAttributeValue("processid");
        String processInputsAttrValue = element.getAttributeValue("processinputs");

        String containerId;
        String processId;
        Map<String, Object> processInputs = null;
        long processInstanceId;

        Configuration configuration = arguments.getConfiguration();
        IStandardExpressionParser parser =
                StandardExpressions.getExpressionParser(configuration);

        if (containerIdAttrValue != null && containerIdAttrValue.startsWith("${") && containerIdAttrValue.endsWith("}")) {
            IStandardExpression containerIdExpression =
                    parser.parseExpression(configuration,
                                           arguments,
                                           containerIdAttrValue);

            containerId =
                    (String) containerIdExpression.execute(configuration,
                                                           arguments);

            if (containerId == null) {
                throw new IllegalArgumentException("Unable to resolve expression for containerid: " + containerId);
            }
        } else {
            containerId = containerIdAttrValue;
        }

        if (processIdAttrValue != null && processIdAttrValue.startsWith("${") && processIdAttrValue.endsWith("}")) {
            IStandardExpression processIdExpression =
                    parser.parseExpression(configuration,
                                           arguments,
                                           processIdAttrValue);

            processId =
                    (String) processIdExpression.execute(configuration,
                                                         arguments);

            if (processId == null) {
                throw new IllegalArgumentException("Unable to resolve expression for processid: " + processId);
            }
        } else {
            processId = processIdAttrValue;
        }

        if (processInputsAttrValue != null && processInputsAttrValue.startsWith("${") && processInputsAttrValue.endsWith("}")) {
            IStandardExpression processInputsExpression =
                    parser.parseExpression(configuration,
                                           arguments,
                                           processInputsAttrValue);

            processInputs =
                    (Map<String, Object>) processInputsExpression.execute(configuration,
                                                                          arguments);
        }

        if (processInputs == null) {
            processInstanceId = processService.startProcess(containerId,
                                                            processId);
        } else {
            processInstanceId = processService.startProcess(containerId,
                                                            processId,
                                                            processInputs);
        }

        final Element container = new Element("div");

        final Text text = new Text("process instance id: " + processInstanceId);
        container.addChild(text);

        final List<Node> nodes = new ArrayList<Node>();
        nodes.add(container);
        return nodes;
    }
}
