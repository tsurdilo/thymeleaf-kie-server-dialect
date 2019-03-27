package org.jbpm.addons.dialect;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.addons.processor.AbortProcessProcessor;
import org.jbpm.addons.processor.DeployedUnitProcessor;
import org.jbpm.addons.processor.ProcessInstanceImageProcessor;
import org.jbpm.addons.processor.ProcessInstancesProcessor;
import org.jbpm.addons.processor.ProcessesDefsProcessor;
import org.jbpm.addons.processor.SignalProcessProcessor;
import org.jbpm.addons.processor.StartProcessProcessor;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

public class KieServerDialect extends AbstractProcessorDialect {

    private static final String DIALECT_NAME = "KieServerDialect";

    private final ApplicationContext applicationContext;

    public KieServerDialect(ApplicationContext applicationContext) {
        super(DIALECT_NAME,
              "kieserver",
              StandardDialect.PROCESSOR_PRECEDENCE);
        this.applicationContext = applicationContext;
    }

    @Override
    public Set<IProcessor> getProcessors(final String dialectPrefix) {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new StartProcessProcessor(dialectPrefix,
                                                 applicationContext));
        processors.add(new AbortProcessProcessor(dialectPrefix,
                                                 applicationContext));
        processors.add(new SignalProcessProcessor(dialectPrefix,
                                                  applicationContext));
        processors.add(new ProcessesDefsProcessor(dialectPrefix,
                                                  applicationContext));
        processors.add(new ProcessInstancesProcessor(dialectPrefix,
                                                     applicationContext));
        processors.add(new DeployedUnitProcessor(dialectPrefix,
                                                 applicationContext));
        processors.add(new ProcessInstanceImageProcessor(dialectPrefix,
                                                         applicationContext));

        return processors;
    }
}
