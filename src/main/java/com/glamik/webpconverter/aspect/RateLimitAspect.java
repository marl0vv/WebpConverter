package com.glamik.webpconverter.aspect;

import com.glamik.webpconverter.exception.RateLimitException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aspect that enforces rate limiting on API requests based on the client's IP address.
 * It intercepts methods annotated with @WithRateLimitProtection and limits the number of
 * allowed requests within a specified time window.
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    public static final String ERROR_MESSAGE = "Too many requests at endpoint %s from IP %s! Please try again after %d milliseconds!";

    /**
     * List of HTTP headers that may contain the client's real IP address when behind proxies
     */
    protected static final String[] IP_HEADERS = {
            "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
    };

    /**
     * Map to track request timestamps per client IP address
     * <p>
     * Key: IP address, Value: List of timestamps (in millisecond)
     * </p>
     */
    private final ConcurrentHashMap<String, List<Long>> requestsCounts = new ConcurrentHashMap<>();

    /**
     * Maximum number of allowed requests within the rate duration per IP address
     */
    @Value("${rate.limit:3}")
    private int rateLimit;

    /**
     * Time window in milliseconds for rate limiting
     */
    @Value("${rate.duration.millis:60000}")
    private long rateDuration;

    /**
     * Around advice that applies rate limiting to methods annotated with @WithRateLimitProtection
     * It checks if the client IP has exceeded the allowed number of requests within the rate duration.
     *
     * @param pjp the join point representing the method execution
     * @return the result of the method execution
     * @throws Throwable if the method execution throws any exceptions
     */
    @Around("@annotation(com.glamik.webpconverter.annotation.WithRateLimitProtection)")
    public Object rateLimit(ProceedingJoinPoint pjp) throws Throwable {
        // Get the current HTTP request attributes
        final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        /*
         Retrieve the client's IP address
         Request's timestamps from this IP will be recorded in a requestsCounts hashmap
        */
        final String clientIp = getClientIp(request);
        final long currentTime = System.currentTimeMillis();

        // Initialize request counts list for the IP if not already present
        requestsCounts.putIfAbsent(clientIp, new ArrayList<>());
        List<Long> requestCounts = requestsCounts.get(clientIp);

        synchronized (requestCounts) {
            // Remove timestamps that are outside the rate duration window
            requestCounts.removeIf(timestamp -> currentTime - timestamp > rateDuration);

            // If the number of requests exceeds the rate limit, then throw RateLimitException
            if (requestCounts.size() >= rateLimit) {
                throw new RateLimitException(String.format(ERROR_MESSAGE, requestAttributes.getRequest().getRequestURI(), clientIp, rateDuration));
            }
        }

        /*
         Idea here is than when there is an error and not 200 code
         then there is an exception thrown. So, if we don't catch
         any exception then everything is successful, and we should add currentTime timestamp to requestCounts
        */
        Object result;
        try {
            result = pjp.proceed();

            synchronized (requestCounts) {
                requestCounts.add(currentTime);
            }
        } catch (Throwable throwable) {
            log.error("Exception occurred during method execution: {}", pjp.getSignature(), throwable);
            throw throwable;
        }
        return result;
    }

    /**
     * Retrieves the client's IP address from the HTTP request, checking various headers
     * that may contain the real IP when behind proxies or load balancers.
     *
     * @param request the HTTP servlet request
     * @return the client's IP address
     */
    public String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }
        return request.getRemoteAddr();
    }

}
