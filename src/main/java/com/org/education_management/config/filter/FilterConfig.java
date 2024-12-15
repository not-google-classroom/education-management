package com.org.education_management.config.filter;

import com.org.education_management.filter.api.ApiRateLimitingFilter;
import com.org.education_management.filter.api.ApiSecurityFilter;
import com.org.education_management.filter.api.AuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ApiSecurityFilter> apiSecurityFilter() {
        FilterRegistrationBean<ApiSecurityFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiSecurityFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(3);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<ApiRateLimitingFilter> apiRateLimitingFilterRegistrationBean() {
        FilterRegistrationBean<ApiRateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiRateLimitingFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterFilterRegistrationBean() {
        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthenticationFilter());
        registrationBean.addInitParameter("skip-url", "/api/org/createOrg,/api/org/getOrgDetails,/api/auth/login");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}

