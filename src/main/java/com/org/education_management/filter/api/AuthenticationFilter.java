package com.org.education_management.filter.api;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
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
                    } else {
                        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        httpServletResponse.getOutputStream().println("Cookie value is not valid! kindly try login to access resource");
                        return;
                    }
                } else {
                    logger.log(Level.SEVERE, "cookie not found to authenticate user!, kindly pass cookies to proceed");
                    httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    //httpServletResponse.getOutputStream().println("Insufficient user permission!");
                    return;
                }
                chain.doFilter(request, response);
            } catch (SignatureException sigExp) {
                httpServletResponse.sendRedirect("/api/auth/login");
                httpServletResponse.setStatus(HttpServletResponse.SC_FOUND);
                logger.log(Level.SEVERE, "Exception when validating cookie, signature mismatch!", sigExp);
                return;
            } catch (ExpiredJwtException extoken) {
                httpServletResponse.sendRedirect("/login");
                httpServletResponse.setStatus(HttpServletResponse.SC_FOUND);
                logger.log(Level.SEVERE, "Token validity expired! try login ", extoken);
                return;
            } catch (Exception e) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpServletResponse.getOutputStream().print("Severe error, when processing request! check logs");
                logger.log(Level.SEVERE, "Exception when validating user credentials!", e);
                return;
            } finally {
                httpServletResponse.getOutputStream().close();
                SchemaUtil.getInstance().setSearchPathToPublic();
            }
        }
    }
}
