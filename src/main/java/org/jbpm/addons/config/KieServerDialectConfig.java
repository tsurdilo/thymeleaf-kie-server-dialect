package org.jbpm.addons.config;

import org.jbpm.addons.dialect.KieServerDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:kieserverdialect.properties")
public class KieServerDialectConfig {

    @Autowired
    private ApplicationContext context;

    @Bean
    public KieServerDialect kieServerDialect() {
        return new KieServerDialect(context);
    }
}
