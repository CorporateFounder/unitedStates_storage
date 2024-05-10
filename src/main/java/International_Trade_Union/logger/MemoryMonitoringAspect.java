package International_Trade_Union.logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MemoryMonitoringAspect {

    @Around("execution(* International_Trade_Union.utils.UtilsResolving.*(..))")
    public Object monitorMemoryUsage(ProceedingJoinPoint joinPoint) throws Throwable {
        long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        MyLogger.saveLog("start method: " + joinPoint.getSignature().getName());
        Object result = joinPoint.proceed(); // Выполнение целевого метода
        long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        MyLogger.saveLog("finish " + joinPoint.getSignature().getName() + ": afterMemory: " + afterMemory + " result: " + (beforeMemory - afterMemory));

        return result;
    }
    @Around("execution(* International_Trade_Union.controllers.BasisController.*(..))")
    public Object monitorMemoryBasisController(ProceedingJoinPoint joinPoint) throws Throwable {
        long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        MyLogger.saveLog("start method: " + joinPoint.getSignature().getName());
        Object result = joinPoint.proceed(); // Выполнение целевого метода
        long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        MyLogger.saveLog("finish " + joinPoint.getSignature().getName() + ": afterMemory: " + afterMemory + " result: " + (beforeMemory - afterMemory));

        return result;
    }
    @Around("execution(* International_Trade_Union.utils.UtilsBalance.*(..))")
    public Object monitorMemoryUtilsBalanse(ProceedingJoinPoint joinPoint) throws Throwable {
        long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        MyLogger.saveLog("start method: " + joinPoint.getSignature().getName());
        Object result = joinPoint.proceed(); // Выполнение целевого метода
        long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        MyLogger.saveLog("finish " + joinPoint.getSignature().getName() + ": afterMemory: " + afterMemory + " result: " + (beforeMemory - afterMemory));

        return result;
    }

    @Around("execution(* International_Trade_Union.utils.UtilUrl.*(..))")
    public Object monitorMemoryUtilUrl(ProceedingJoinPoint joinPoint) throws Throwable {
        long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        MyLogger.saveLog("start method: " + joinPoint.getSignature().getName());
        Object result = joinPoint.proceed(); // Выполнение целевого метода
        long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        MyLogger.saveLog("finish " + joinPoint.getSignature().getName() + ": afterMemory: " + afterMemory + " result: " + (beforeMemory - afterMemory));

        return result;
    }
}

