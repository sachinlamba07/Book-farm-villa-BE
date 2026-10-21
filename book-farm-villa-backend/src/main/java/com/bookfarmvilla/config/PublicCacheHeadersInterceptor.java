package com.bookfarmvilla.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Adds short browser/CDN cache headers only to public read-only API paths. */
@Component
public class PublicCacheHeadersInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        boolean publicRead = path.startsWith("/api/properties")
                || path.startsWith("/api/vendors")
                || path.startsWith("/api/amenities")
                || path.startsWith("/api/blogs")
                || path.startsWith("/api/reviews");
        boolean authenticated = request.getHeader("Authorization") != null;
        if (publicRead && !authenticated && !path.startsWith("/api/admin") && !path.contains("/admin/")) {
            response.setHeader("Cache-Control", "public, max-age=30, s-maxage=60, stale-while-revalidate=30");
        }
        return true;
    }
}
