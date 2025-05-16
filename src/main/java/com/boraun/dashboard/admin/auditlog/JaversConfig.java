package com.boraun.dashboard.admin.auditlog;

import lombok.extern.slf4j.Slf4j;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class JaversConfig {
    @Bean
    public Javers javers() {
        log.info("bean javers created.");
        return JaversBuilder.javers().build();
    }
}