package com.org.education_management.filter.api;

import com.org.education_management.util.DynamicSchedulerUtil;
import com.org.education_management.util.JWTUtil;
import com.org.education_management.util.SchemaUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthenticationFilter implements Filter {
    private static final Logger logger = Logger.getLogger(AuthenticationFilter.class.getName());
    private static final List<String> excludedUrls = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String skipUrls = filterConfig.getInitParameter("skip-url");
        excludedUrls.addAll(Arrays.asList(skipUrls.split(",")));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.log(Level.INFO, "Authentication Filter check for API Authentication");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (excludedUrls.contains(httpServletRequest.getRequestURI())) {
            logger.log(Level.INFO, "url : {0} skipped for authentication filter", httpServletRequest.getRequestURI());
            chain.doFilter(request, response);
        } else {
            logger.log(Level.INFO, "Ensuring cookies values for requestURI : {0}", new Object[]{httpServletRequest.getRequestURI()});
            Cookie[] reqCookie = httpServletRequest.getCookies();
            String token = null;
            try {
                if (reqCookie != null) {
                    for (Cookie cookie : reqCookie) {
                        if (cookie.getName().equalsIgnoreCase("token")) {
                            token = cookie.getValue();
                        }
                    }
                    if (token != null && !token.isEmpty()) {
                        Claims claims = JWTUtil.validateToken(token);
                        String tokenSub = (String) claims.get("sub");
                        byte[] decodedVal = Base64.getUrlDecoder().decode(tokenSub.getBytes(StandardCharsets.UTF_8));
                        String decodedContent = new String(decodedVal);
                        String[] splitContent = decodedContent.split(",");
                        SchemaUtil.getInstance().setSearchPathForSchema(splitContent[1]);
                        //Start default scheduler for the user
                        DynamicSchedulerUtil schedulerUtil = new DynamicSchedulerUtil();
                        schedulerUtil.loadDefaultSchedulersFromDatabase();
                    } else {
                        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        try {
                            httpServletResponse.getOutputStream().print(new JSONObject().put("message", "Cookie value is not valid! kindly try login to access resource").toString());
                        } catch (JSONException ex) {
                            throw new RuntimeException(ex);
                        }
                        return;
                    }
                } else {
                    httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    try {
                        httpServletResponse.getOutputStream().print(new JSONObject().put("message", "cookie not found to authenticate user!, kindly pass cookies to proceed").toString());
                    } catch (JSONException ex) {
                        throw new RuntimeException(ex);
                    }
                    return;
                }
                chain.doFilter(request, response);
            } catch (SignatureException sigExp) {
                httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                try {
                    httpServletResponse.getOutputStream().print(new JSONObject().put("message", "Cookie value is not valid! kindly try login to access resource").toString());
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
                logger.log(Level.SEVERE, "Exception when validating cookie, signature mismatch!", sigExp);
            } catch (ExpiredJwtException extoken) {
                httpServletResponse.sendRedirect("/login");
                httpServletResponse.setStatus(HttpServletResponse.SC_FOUND);
                logger.log(Level.SEVERE, "Token validity expired! try login ", extoken);
            } catch (Exception e) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                try {
                    httpServletResponse.getOutputStream().print(new JSONObject().put("message", "Severe error, when processing request! check logs").toString());
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
                logger.log(Level.SEVERE, "Exception when validating user credentials!", e);
            } finally {
                httpServletResponse.getOutputStream().close();
                SchemaUtil.getInstance().setSearchPathToPublic();
            }
        }
    }
}
