package com.glamik.webpconverter.aspect;

import com.glamik.webpconverter.exception.RateLimitException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    public static final String ERROR_MESSAGE = "Too many requests at endpoint %s from IP %s! Please try again after %d milliseconds!";
    public static final String[] IP_HEADERS = {
            "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
    };
    private final ConcurrentHashMap<String, List<Long>> requestsCounts = new ConcurrentHashMap<>();

    @Value("${rate.limit:3}")
    private int rateLimit;

    @Value("${rate.duration.millis:60000}")
    private long rateDuration;

    @Around("@annotation(com.glamik.webpconverter.annotation.WithRateLimitProtection)")
    public Object rateLimit(ProceedingJoinPoint pjp) throws Throwable {
        final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        final String clientIp = getClientIp(request);
        final long currentTime = System.currentTimeMillis();

        requestsCounts.putIfAbsent(clientIp, new ArrayList<>());
        List<Long> requestCounts = requestsCounts.get(clientIp);

        Object result;
        synchronized (requestCounts) {
            requestCounts.removeIf(timestamp -> currentTime - timestamp > rateDuration);

            if (requestCounts.size() >= rateLimit) {
                throw new RateLimitException(String.format(ERROR_MESSAGE, requestAttributes.getRequest().getRequestURI(), clientIp, rateDuration));
            }

            try {
                result = pjp.proceed();
                requestCounts.add(currentTime);
            } catch (Throwable throwable) {
                log.error("Exception occurred during method execution: {}", pjp.getSignature(), throwable);
                throw throwable;
            }

        }
        return result;
    }

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
