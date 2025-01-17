package com.org.education_management.model;

import java.util.List;
import java.util.Map;

public class ApiRule {
    private String path;
    private String method;
    private List<String> roles;
    private List<ParamRule> params;
    private Map<String, String> bodyValidation; // Field name -> Regex pattern
    private ApiRateLimit rateLimit;
    private String template;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<ParamRule> getParams() {
        return params;
    }

    public void setParams(List<ParamRule> params) {
        this.params = params;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public ApiRateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(ApiRateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    @Override
    public String toString() {
        return "ApiRule{" +
                "path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", roles=" + roles +
                ", params=" + params +
                ", bodyValidation=" + bodyValidation +
                ", apiRateLimit=" + rateLimit +
                ", template=" + template +
                '}';
    }
}


