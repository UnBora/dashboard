package com.boraun.dashboard;
import com.boraun.dashboard.admin.authority.AdminAuthorityEntity;
import com.boraun.dashboard.admin.authority.AdminAuthorityService;
import com.boraun.dashboard.admin.config.WebAdminUserSessionListener;
import com.boraun.dashboard.admin.role.AdminRoleService;
import com.boraun.dashboard.admin.user.AdminUserService;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

import java.util.List;
import java.util.TimeZone;

@SpringBootApplication
@Slf4j
public class Application {

    @Value("${spring.profiles.active}")
    private String activeProfile;
    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${project.version}")
    private String applicationVersion;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner initializeApplication(
            @Lazy AdminUserService adminUserService,
            @Lazy AdminRoleService adminRoleService,
            @Lazy AdminAuthorityService adminAuthorityService,
            ConfigurableApplicationContext context) {
        return args -> {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+7"));
            List<AdminAuthorityEntity> list = adminAuthorityService.rescanAuthority(context);
            adminUserService.addDefaultAdminUser(adminRoleService.addDefaultRole(list));
            log.info("Application " + applicationName + " run successfully in " + activeProfile + " version " + applicationVersion);
        };
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionListener> httpSessionListener(
            @Lazy WebAdminUserSessionListener listener) {
        ServletListenerRegistrationBean<HttpSessionListener> listenerRegBean =
                new ServletListenerRegistrationBean<>();
        listenerRegBean.setListener(listener);
        return listenerRegBean;
    }
}