package com.boraun.dashboard.admin.config;

import com.boraun.dashboard.admin.authority.AdminAuthorityEntity;
import com.boraun.dashboard.admin.authority.AdminAuthorityService;
import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.admin.user.AdminUserService;
import com.boraun.dashboard.common.CoreConstants;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


import java.util.Comparator;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class WebAdminSecurityConfig {

    private final AdminUserService adminUserService;
    private final WebAdminAccessDeniedHandler webAdminAccessDeniedHandler;
    private final ConfigurationService configurationService;
    private final AdminAuthorityService adminAuthorityService;

    @Autowired
    public WebAdminSecurityConfig(@Lazy AdminUserService adminUserService, WebAdminAccessDeniedHandler webAdminAccessDeniedHandler, @Lazy ConfigurationService configurationService, AdminAuthorityService adminAuthorityService) {
        this.adminUserService = adminUserService;
        this.webAdminAccessDeniedHandler = webAdminAccessDeniedHandler;
        this.configurationService = configurationService;
        this.adminAuthorityService = adminAuthorityService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        List<AdminAuthorityEntity> authorityEntities = adminAuthorityService.findAllByStatus(CoreConstants.Status.Enabled, Comparator.comparing(AdminAuthorityEntity::getAuthorityOrder));
        http
                .formLogin(form -> form
                        .loginPage(CoreConstants.WEB_ADMIN_LOGIN)
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl(CoreConstants.WEB_ADMIN_HOME, true)
                        .permitAll()
                )
                .userDetailsService(adminUserService)
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authReq -> {
                    // 1. Publicly accessible paths
                    authReq.requestMatchers(
                            "/health",
                            "admin/authentication/**",
                            "admin/unauthorized",
                            "/assets/**",
                            "/webjars/**",
                            "/error/**",
                            "/css/**",
                            "/fonts/**",
                            "/js/**",
                            "/images/**",
                            "/favicon.ico",
                            "/.well-known/**"
                    ).permitAll();

                    // 2. Authenticated paths that do not require specific roles
                    authReq.requestMatchers("/admin", "/permit/**", "/resources/attachment/**",
                            "/assets/libs/**" ).authenticated();

                    // 3. Paths requiring specific authorities
                    authorityEntities.forEach(authorityEntity -> {
                        authReq.requestMatchers(authorityEntity.getEndPointUrl())
                                .hasAnyAuthority(authorityEntity.getAuthorityKey());
                    });

                    // 4. Optional: Catch-all rule to restrict access to any other endpoints
                    authReq.anyRequest().denyAll();
                })

                .logout(logout -> logout
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutRequestMatcher(new AntPathRequestMatcher(CoreConstants.WEB_ADMIN_LOGOUT))
                        .logoutSuccessUrl(CoreConstants.WEB_ADMIN_LOGOUT_SUCCESS)
                        .clearAuthentication(true)
                )
                .sessionManagement(session -> session
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::migrateSession)
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .maximumSessions(configurationService.getMaximumSessionAllowance())
                        .expiredUrl(CoreConstants.WEB_ADMIN_SESSION_EXPIRED)
                        .maxSessionsPreventsLogin(true)
                )
                .exceptionHandling(ex -> ex.accessDeniedHandler(webAdminAccessDeniedHandler));

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        // Change session timeout need restart application
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                servletContext.setSessionTimeout(configurationService.getConfiguration().getMaximumSessionTimeout() / 60);
            }
        };
    }
}
