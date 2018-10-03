package org.jbpm.addons.dialect;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.addons.processor.ShowProcessesProcessor;
import org.jbpm.addons.processor.StartProcessProcessor;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;

public class KieServerDialect extends AbstractDialect {

    public String getPrefix() {
        return "kieserver";
    }

    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new StartProcessProcessor());
        processors.add(new ShowProcessesProcessor());

        return processors;
    }
}
