package com.org.education_management.filter.api;

import com.org.education_management.model.User;
import com.org.education_management.model.UserContext;
import com.org.education_management.util.*;
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
        JSONObject failureJSON = new JSONObject();
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        Long userID = null;
        if (excludedUrls.contains(httpServletRequest.getRequestURI())) {
            logger.log(Level.INFO, "url : {0} skipped for authentication filter", httpServletRequest.getRequestURI());
            chain.doFilter(request, response);
        } else {
            logger.log(Level.INFO, "Ensuring cookies values for requestURI : {0}", new Object[]{httpServletRequest.getRequestURI()});
            Cookie[] reqCookie = httpServletRequest.getCookies();
            String tokenFromReq = request.getParameter("token");
            String token = null;
            try {
                if (reqCookie != null || tokenFromReq != null) {
                    if(reqCookie != null) {
                        for (Cookie cookie : reqCookie) {
                            if (cookie.getName().equalsIgnoreCase("token")) {
                                token = cookie.getValue();
                            }
                        }
                    }
                    else if (!tokenFromReq.isEmpty()) {
                        token = tokenFromReq;
                    }
                    if (token != null && !token.isEmpty()) {
                        Claims claims = JWTUtil.validateToken(token);
                        String tokenSub = (String) claims.get("sub");
                        byte[] decodedVal = Base64.getUrlDecoder().decode(tokenSub.getBytes(StandardCharsets.UTF_8));
                        String decodedContent = new String(decodedVal);
                        String[] splitContent = decodedContent.split(",");
                        String schemaName = splitContent[2];
                        userID = Long.parseLong(splitContent[0]);
                        validateSchemaName(splitContent);
                        SchemaUtil.getInstance().setSearchPathForSchema(schemaName);
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
                generateUserData(userID);
                chain.doFilter(request, response);
            } catch (SignatureException sigExp) {
                httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                try {
                    failureJSON.put(StatusConstants.STATUS_CODE, 403);
                    failureJSON.put(StatusConstants.MESSAGE, "Cookie value is not valid! kindly try login to access resource");
                    httpServletResponse.getOutputStream().print(failureJSON.toString());
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
                logger.log(Level.SEVERE, "Exception when validating cookie, signature mismatch!", sigExp);
            } catch (ExpiredJwtException extoken) {
                failureJSON.put(StatusConstants.STATUS_CODE, 403);
                failureJSON.put(StatusConstants.MESSAGE, "Cookie expired! kindly try login");
                httpServletResponse.getOutputStream().print("Cookie expired!");
                httpServletResponse.setStatus(HttpServletResponse.SC_FOUND);
                logger.log(Level.SEVERE, "Token validity expired! try login ", extoken);
            } catch (Exception e) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                try {
                    failureJSON.put(StatusConstants.STATUS_CODE, 403);
                    failureJSON.put(StatusConstants.MESSAGE, "Severe error, when processing request! check logs");
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

    private void generateUserData(Long userID) {
        if(userID != null && userID != -1L) {
            logger.log(Level.INFO, "generating loggedIn user data...");
            Map<String, Object> userDetails = UserMgmtUtil.getInstance().getUsers(userID);
            UserContext.setUser(User.getInstance().setUserData(userID, userDetails));
        }
    }

    private void validateSchemaName(String[] splitContent) {
        String userEmail = splitContent[1];
        if(userEmail != null && !userEmail.isEmpty()) {
            Long userID = OrgUtil.getInstance().getUserIDByEmail(userEmail);
            if(userID != null) {
                String schemaName = OrgUtil.getInstance().getSchemaName(userID);
                if(splitContent[2].equals(schemaName)) {
                    return;
                }
            }
        }
        logger.log(Level.SEVERE, "schema name validation failed!, invalid cookie details");
        throw new SignatureException("Exception when validating cookie value");
    }
}
