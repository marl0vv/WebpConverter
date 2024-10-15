package com.glamik.webpconverter.aspect;

import com.glamik.webpconverter.exception.RateLimitException;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitAspect {

    public static final String ERROR_MESSAGE = "Too many requests at endpoint %s from IP %s! Please try again after %d milliseconds!";
    private final ConcurrentHashMap<String, List<Long>> requestCounts = new ConcurrentHashMap<>();

    @Value("${rate.limit:#{3}}")
    private int rateLimit;

    @Value("${rate.duration.millis:#{60000}}")
    private long rateDuration;

    @Before("@annotation(com.glamik.webpconverter.annotation.WithRateLimitProtection)")
    public void rateLimit() {
        final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        final String remoteAddr = requestAttributes.getRequest().getRemoteAddr();
        final long currentTime = System.currentTimeMillis();
        requestCounts.putIfAbsent(remoteAddr, new ArrayList<>());
        requestCounts.get(remoteAddr).add(currentTime);
        cleanUpRequestCounts(currentTime);
        if (requestCounts.get(remoteAddr).size() > rateLimit) {
            throw new RateLimitException(String.format(ERROR_MESSAGE, requestAttributes.getRequest().getRequestURI(), remoteAddr, rateDuration));
        }
    }

    private void cleanUpRequestCounts(final long currentTime) {
        requestCounts.values().forEach(l -> l.removeIf(t -> timeIsTooOld(currentTime, t)));
    }

    private boolean timeIsTooOld(final long currentTime, final long timeToCheck) {
        return currentTime - timeToCheck > rateDuration;
    }

}
