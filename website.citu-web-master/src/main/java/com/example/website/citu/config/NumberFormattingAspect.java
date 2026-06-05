package com.example.website.citu.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;
import java.util.regex.Pattern;

@Aspect
@Component
public class NumberFormattingAspect {
    private static final int SCALE = 10;
    private static final Pattern CLEAN_NUMERIC_STRING = Pattern.compile("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?");

    @Around("execution(* com.example.website.citu.controller..*.*(..))")
    public Object formatNumbers(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        return formatObject(result);
    }


    Object formatObject(Object obj) {
        if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).setScale(SCALE, RoundingMode.HALF_UP);
        } else if (obj instanceof BigInteger) {
            return obj;
        } else if (obj instanceof Double) {
            return BigDecimal.valueOf((Double) obj).setScale(SCALE, RoundingMode.HALF_UP).doubleValue();
        } else if (obj instanceof Float) {
            return BigDecimal.valueOf(((Float) obj).doubleValue()).setScale(SCALE, RoundingMode.HALF_UP).floatValue();
        } else if (obj instanceof String) {
            return formatString((String) obj);
        } else if (obj instanceof Map<?, ?>) {
            formatMap(obj);
        } else if (obj instanceof Iterable) {
            ((Iterable<?>) obj).forEach(this::formatObject);
        } else if (obj != null && obj.getClass().isArray()) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(obj); i++) {
                java.lang.reflect.Array.set(obj, i, formatObject(java.lang.reflect.Array.get(obj, i)));
            }
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    private void formatMap(Object obj) {
        ((Map<Object, Object>) obj).replaceAll((key, value) -> formatObject(value));
    }

    String formatString(String value) {
        if (!isCleanNumericString(value)) {
            return value;
        }
        try {
            BigDecimal number = new BigDecimal(value);
            return number.setScale(SCALE, RoundingMode.HALF_UP).toPlainString();
        } catch (NumberFormatException ex) {
            return value;
        }
    }

    boolean isCleanNumericString(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.equals(value) && CLEAN_NUMERIC_STRING.matcher(value).matches();
    }
}
