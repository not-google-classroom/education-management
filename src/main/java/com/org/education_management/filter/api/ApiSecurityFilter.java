package com.org.education_management.filter.api;

import com.org.education_management.model.ApiRule;
import com.org.education_management.model.ParamRule;
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

        // Fetch API rules from JSON file
        List<ApiRule> apiRules = ApiSecurityUtil.getInstance().getApiRules();

        boolean isRuleMatched = false;
        // Find matching rule
        for(ApiRule rule : apiRules) {

            if(rule.getPath().equalsIgnoreCase(path) && rule.getMethod().equalsIgnoreCase(method)) {
                isRuleMatched = true;
                // Check roles only if roles are defined and not empty
                if (rule.getRoles() != null && !rule.getRoles().isEmpty() && !rule.getRoles().contains("all")) {
                    String userRole = getUserRoleFromContext(); // Assume this method fetches the user role
                    if (!rule.getRoles().contains(userRole)) {
                        logger.log(Level.SEVERE, "you're not authorized to do this operation");
                        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "you're not authorized to do this operation");
                        return;
                    }
                }

                MultipartHttpServletRequest multipartHttpServletRequest = null;
                if (isMultipartRequest(httpRequest)) {
                    multipartHttpServletRequest = new StandardMultipartHttpServletRequest(httpRequest);
                }

                RequestBodyWrapper wrapper = new RequestBodyWrapper(httpRequest);
                JSONObject requestBody = null;
                try {
                    requestBody = new JSONObject(wrapper.getBody());
                } catch (JSONException e) {
                    logger.log(Level.SEVERE, "Request body is not in expected format");
                }

                // Validate request parameters
                if (rule.getParams() != null) {
                    for (ParamRule paramRule : rule.getParams()) {
                        Object paramValue = httpRequest.getParameter(paramRule.getName());
                        if (paramValue == null) {
                            if (requestBody != null) {
                                paramValue = requestBody.opt(paramRule.getName());
                            }
                            if (requestBody == null && multipartHttpServletRequest != null) {
                                paramValue = multipartHttpServletRequest.getParameter(paramRule.getName());
                            }
                        }
                        if (!validateParam(paramValue, paramRule)) {
                            logger.log(Level.WARNING, "Invalid parameter: {0}", paramRule.getName());
                            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request parameter format found!");
                            return;
                        }
                    }
                }
                break;
            }
        }
        if(isRuleMatched) {
            chain.doFilter(request, response);  // Proceed if validation passes
        } else {
            logger.log(Level.SEVERE,  "Invalid api details or api is not configured");
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid api url");
        }
    }

    private String getUserRoleFromContext() {
        return "admin";
    }

    private boolean validateParam(Object paramValue, ParamRule rule) {
        if (paramValue == null) {
            return rule.getRequired() == null || !rule.getRequired();
        }

        switch (rule.getType()) {
            case "string":
                return rule.getPattern() == null || Pattern.matches(rule.getPattern(), paramValue.toString());
            case "long":
                try {
                    Long.parseLong(paramValue.toString());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
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