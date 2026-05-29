package app.budgetmanager.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServiceExecutionTimeLoggingAspect {

    @Around("execution(* app.budgetmanager.service..*(..))")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedNanos = System.nanoTime() - startedAt;
            double elapsedMs = elapsedNanos / 1_000_000.0;
            if (log.isInfoEnabled()) {
                log.info("{} executed in {} ms", joinPoint.getSignature().toShortString(),
                        String.format("%.3f", elapsedMs));
            }
        }
    }
}
