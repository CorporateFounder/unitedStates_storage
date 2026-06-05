package com.example.website.citu.config;

import org.junit.jupiter.api.Test;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberFormattingAspectTest {
    private final NumberFormattingAspect aspect = new NumberFormattingAspect();

    @Test
    void formatsCleanIntegerString() {
        assertEquals("12345.0000000000", aspect.formatString("12345"));
    }

    @Test
    void formatsCleanDecimalString() {
        assertEquals("12345.6789000000", aspect.formatString("12345.6789"));
    }

    @Test
    void formatsNegativeNumericString() {
        assertEquals("-123.4500000000", aspect.formatString("-123.45"));
    }

    @Test
    void leavesNullAndEmptyValuesUnchanged() {
        assertEquals(null, aspect.formatObject(null));
        assertEquals("", aspect.formatString(""));
    }

    @Test
    void leavesNonNumericTextUnchanged() {
        assertEquals("V35 activation is scheduled", aspect.formatString("V35 activation is scheduled"));
        assertEquals("height 514291", aspect.formatString("height 514291"));
    }

    @Test
    void leavesUrlsAndIpAddressesUnchanged() {
        assertEquals("http://194.87.236.238:82/size", aspect.formatString("http://194.87.236.238:82/size"));
        assertEquals("https://citucorp.com", aspect.formatString("https://citucorp.com"));
        assertEquals("194.87.236.238", aspect.formatString("194.87.236.238"));
    }

    @Test
    void leavesDateAndSlashSeparatedStringsUnchanged() {
        assertEquals("2026-06-05", aspect.formatString("2026-06-05"));
        assertEquals("v35/protocol", aspect.formatString("v35/protocol"));
        assertEquals("514291/432", aspect.formatString("514291/432"));
    }

    @Test
    void leavesLabelsAndAlreadyFormattedStringsUnchanged() {
        assertEquals("Fork height: 514291", aspect.formatString("Fork height: 514291"));
        assertEquals("1,234.00", aspect.formatString("1,234.00"));
        assertEquals(" 1234", aspect.formatString(" 1234"));
    }

    @Test
    void formatsOnlyCleanNumericMapValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("height", "514291");
        values.put("url", "http://194.87.236.238:82/size");

        aspect.formatObject(values);

        assertEquals("514291.0000000000", values.get("height"));
        assertEquals("http://194.87.236.238:82/size", values.get("url"));
    }

    @Test
    void pageStyleMixedValuesDoNotThrow() {
        assertDoesNotThrow(() -> {
            aspect.formatString("http://194.87.236.238:82/size");
            aspect.formatString("V35 activation at block 514291");
            aspect.formatString("2026-06-05");
            aspect.formatString("BURN_ADDRESS is internal placeholder");
            aspect.formatString("1,261,440 GOLD/year");
        });
    }

    @Test
    void aspectIsDisabledByDefaultForControllerResults() throws Throwable {
        NumberFormattingAspect disabled = new NumberFormattingAspect();
        ProceedingJoinPoint joinPoint = new StaticResultJoinPoint("514291");

        assertEquals("514291", disabled.formatNumbers(joinPoint));
    }

    @Test
    void aspectCanBeEnabledExplicitly() throws Throwable {
        NumberFormattingAspect enabled = new NumberFormattingAspect(true);
        ProceedingJoinPoint joinPoint = new StaticResultJoinPoint("514291");

        assertEquals("514291.0000000000", enabled.formatNumbers(joinPoint));
    }

    private record StaticResultJoinPoint(Object value) implements ProceedingJoinPoint {
        @Override public Object proceed() { return value; }
        @Override public Object proceed(Object[] args) { return value; }
        @Override public String toShortString() { return "static"; }
        @Override public String toLongString() { return "static"; }
        @Override public Object getThis() { return null; }
        @Override public Object getTarget() { return null; }
        @Override public Object[] getArgs() { return new Object[0]; }
        @Override public org.aspectj.lang.Signature getSignature() { return null; }
        @Override public org.aspectj.lang.reflect.SourceLocation getSourceLocation() { return null; }
        @Override public String getKind() { return "method-execution"; }
        @Override public org.aspectj.lang.JoinPoint.StaticPart getStaticPart() { return null; }
        @Override public void set$AroundClosure(org.aspectj.runtime.internal.AroundClosure aroundClosure) {}
    }
}
