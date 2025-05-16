package com.boraun.dashboard.filter;

import com.boraun.dashboard.common.Utils;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@WebFilter(urlPatterns = "/*")
@Component
public class GlobalFilter extends OncePerRequestFilter {

    @Value("#{'${global.filter.exclude.extension}'.split(',')}")
    private List<String> globalFilterExcludeExtension;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Filter model = new Filter();
        String requestId = Utils.getHeaderParameter(request, "x-request-id");
        MDC.put("LOG_REQUEST_UUID", requestId);
        model.setStart(System.currentTimeMillis());

        model.setMethod(request.getMethod());
        model.setRequestHeaders(Utils.getHeaders(request));
        model.setResponseId(org.springframework.util.StringUtils.replace(String.valueOf(UUID.randomUUID()), "-", ""));

        model.setRequestId(requestId);
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        model.setRequestUrl(url + (Utils.nonNull(queryString) ? "?" + queryString : ""));
        model.setClientIP(Utils.getClientIP(request));
        model.setClientTimestamp(Utils.getHeaderParameter(request, "timestamp"));
        model.setServerTimestamp(Utils.getTimestamp());
        response.addHeader("x-request-id", model.getRequestId());
        response.addHeader("x-response-id", model.getResponseId());

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(req, resp);

        model.setRequestBody(new String(req.getContentAsByteArray(), StandardCharsets.UTF_8));
        model.setResponseBody(new String(resp.getContentAsByteArray(), StandardCharsets.UTF_8));

        model.setStatus(resp.getStatus());
        resp.copyBodyToResponse();
        model.setResponseHeaders(Utils.getHeaders(response));
        model.setEnd(System.currentTimeMillis());
        if(globalFilterExcludeExtension.stream().noneMatch(url::endsWith)){
            log.info("Request " + Utils.toJsonString(model));
        }
    }

//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//        return !request.getServletPath().startsWith("/web");
//    }

}