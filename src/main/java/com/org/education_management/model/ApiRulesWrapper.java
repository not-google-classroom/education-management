package com.org.education_management.model;

import java.util.List;

public class ApiRulesWrapper {
    private List<ApiRule> urls;
    private List<TemplateRule> templates;

    public List<ApiRule> getUrls() {
        return urls;
    }

    public void setUrls(List<ApiRule> urls) {
        this.urls = urls;
    }

    public List<TemplateRule> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateRule> templates) {
        this.templates = templates;
    }

    @Override
    public String toString() {
        return "ApiRulesWrapper{" +
                "urls=" + urls +
                ", templates=" + templates +
                '}';
    }
}