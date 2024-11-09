package com.org.education_management.config.filter;

import com.org.education_management.filter.api.ApiSecurityFilter;
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
        registrationBean.setOrder(1);
        return registrationBean;
    }
}

