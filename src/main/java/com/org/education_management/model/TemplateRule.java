package com.org.education_management.model;

import java.util.List;

public class TemplateRule {
    private String name; // Template name
    private int minLength;
    private int maxLength;
    private String type; // Expected type (e.g., JSONObject)
    private List<ParamRule> params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ParamRule> getParams() {
        return params;
    }

    public void setParams(List<ParamRule> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "TemplateRule{" +
                "name='" + name + '\'' +
                ", minLength=" + minLength +
                ", maxLength=" + maxLength +
                ", type='" + type + '\'' +
                ", params=" + params +
                '}';
    }
}