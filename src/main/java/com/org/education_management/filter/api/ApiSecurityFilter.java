package com.org.education_management.filter.api;

import com.org.education_management.model.ApiRule;
import com.org.education_management.model.ParamRule;
import com.org.education_management.model.TemplateRule;
import com.org.education_management.util.api.ApiSecurityUtil;
import com.org.education_management.util.api.RequestBodyWrapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.logging.Logger;

@MultipartConfig
@WebFilter(urlPatterns = "/api/*")
public class ApiSecurityFilter implements Filter {

    private static final Logger logger = Logger.getLogger(ApiSecurityFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        logger.log(Level.INFO, "Api with path : {0} and method : {1} is requested for validation", new Object[]{path, method});

        // Fetch API and Template rules from JSON file
        List<ApiRule> apiRules = ApiSecurityUtil.getInstance().getApiRules();
        List<TemplateRule> templateRules = ApiSecurityUtil.getInstance().getTemplateRules();

        boolean isRuleMatched = false;
        RequestBodyWrapper wrapper = new RequestBodyWrapper(httpRequest);

        for (ApiRule rule : apiRules) {
            if (rule.getPath().equalsIgnoreCase(path) && rule.getMethod().equalsIgnoreCase(method)) {
                isRuleMatched = true;

                // Validate roles
                if (rule.getRoles() != null && !rule.getRoles().isEmpty() && !rule.getRoles().contains("all")) {
                    String userRole = getUserRoleFromContext();
                    if (!rule.getRoles().contains(userRole)) {
                        logger.log(Level.SEVERE, "You're not authorized to do this operation");
                        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You're not authorized to do this operation");
                        return;
                    }
                }

                MultipartHttpServletRequest multipartHttpRequest = null;
                if (isMultipartRequest(httpRequest)) {
                    multipartHttpRequest = new StandardMultipartHttpServletRequest(httpRequest);
                }

                JSONObject requestBody = null;
                if (rule.getTemplate() != null) {
                    try {
                        requestBody = new JSONObject(wrapper.getBody());
                    } catch (JSONException e) {
                        logger.log(Level.SEVERE, "Request body is not in expected format");
                        httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request body format");
                        return;
                    }
                }

                // Validate parameters from ApiRule
                if (rule.getParams() != null) {
                    if (!validateParams(rule.getParams(), httpRequest, requestBody, multipartHttpRequest)) {
                        logger.log(Level.WARNING, "Invalid parameters in ApiRule");
                        httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request parameter format found!");
                        return;
                    }
                }

                // Validate parameters from TemplateRule
                if (rule.getTemplate() != null) {
                    TemplateRule templateRule = templateRules.stream()
                            .filter(t -> t.getName().equals(rule.getTemplate()))
                            .findFirst()
                            .orElse(null);

                    if (templateRule != null && templateRule.getParams() != null) {
                        if(!validateLength(templateRule.getMinLength(), requestBody, true)) {
                            logger.log(Level.WARNING, "RequestBody doesn't meet minimum requestBody value");
                            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "RequestBody doesn't meet minimum requestBody value");
                            return;
                        }
                        if(!validateLength(templateRule.getMaxLength(), requestBody, false)) {
                            logger.log(Level.WARNING, "RequestBody has more requestBody value");
                            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "RequestBody has more requestBody value");
                            return;
                        }
                        if (!validateParams(templateRule.getParams(), httpRequest, requestBody, multipartHttpRequest)) {
                            logger.log(Level.WARNING, "Invalid parameters in TemplateRule: {0}", templateRule.getName());
                            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request parameter format found in template!");
                            return;
                        }
                    } else {
                        logger.log(Level.SEVERE, "Template not found: {0}", rule.getTemplate());
                        httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Template not found");
                        return;
                    }
                }

                break;
            }
        }

        if (isRuleMatched) {
            chain.doFilter(wrapper, response);  // Proceed if validation passes
        } else {
            logger.log(Level.SEVERE, "Invalid API details or API is not configured");
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid API URL");
        }
    }

    private boolean validateLength(int length, JSONObject requestBody, boolean isMinimum) {
        int size = requestBody.toString().length();
        if(isMinimum && size < length){
            return false;
        }
        if(!isMinimum && size > length) {
            return false;
        }
        return true;
    }

    private boolean validateParams(List<ParamRule> paramRules, HttpServletRequest httpRequest, JSONObject requestBody, MultipartHttpServletRequest multipartHttpRequest) {
        for (ParamRule paramRule : paramRules) {
            Object paramValue = httpRequest.getParameter(paramRule.getName());
            if (paramValue == null) {
                if (requestBody != null) {
                    paramValue = requestBody.opt(paramRule.getName());
                }
                if (requestBody == null && multipartHttpRequest != null) {
                    paramValue = multipartHttpRequest.getParameter(paramRule.getName());
                }
            }
            if (!validateParam(paramValue, paramRule)) {
                logger.log(Level.WARNING, "Parameter : {0}, not found in the API request", paramRule.getName());
                return false;
            }
        }
        return true;
    }

    private String getUserRoleFromContext() {
        return "admin";
    }

    private boolean validateParam(Object paramValue, ParamRule rule) {
        if (paramValue == null) {
            return rule.getRequired() == null || !rule.getRequired();
        }

        String type = rule.getType().toLowerCase();
        type = type.replaceAll("integer", "int");
        switch (type) {
            case "string":
                return rule.getPattern() == null || Pattern.matches(rule.getPattern(), paramValue.toString());
            case "long":
                try {
                    Long.parseLong(paramValue.toString());
                    return true;
                } catch (NumberFormatException e) {
                    logger.log(Level.SEVERE, "Invalid parameter format found! for param : {0}, paramvalue : {1}", new Object[]{rule.getName(), paramValue});
                    return false;
                }
            case "int":
                try{
                    Integer.parseInt(paramValue.toString());
                    return true;
                } catch (NumberFormatException e) {
                    logger.log(Level.SEVERE, "Invalid parameter format found! for param : {0}, paramvalue : {1}", new Object[]{rule.getName(), paramValue});
                    return false;
                }
            case "boolean":
                return rule.getPattern() == null || Pattern.matches(rule.getPattern(), paramValue.toString());
            default:
                return false;
        }
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().startsWith("multipart/");
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization if required
    }

    @Override
    public void destroy() {
        // Cleanup if required
    }
}