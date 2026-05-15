package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Pure V35 target difficulty helpers. Mining and validation integration is future work.
 */
public final class V35TargetDifficulty {
    private static final long COMPACT_SIGN_BIT = 0x00800000L;
    private static final long COMPACT_MANTISSA_MASK = 0x007fffffL;

    private V35TargetDifficulty() {
    }

    public static int getTargetWindow() {
        return Seting.V35_TARGET_WINDOW;
    }

    public static int getTargetBlockTimeSeconds() {
        return Seting.V35_TARGET_BLOCK_TIME_SECONDS;
    }

    public static long getEmergencyDelaySeconds() {
        return Seting.V35_EMERGENCY_DELAY_SECONDS;
    }

    public static BigInteger decodeCompactTarget(long compact) {
        long unsignedCompact = compact & 0xffffffffL;
        int size = (int) (unsignedCompact >>> 24);
        long mantissa = unsignedCompact & COMPACT_MANTISSA_MASK;

        if (size <= 0 || mantissa <= 0 || (unsignedCompact & COMPACT_SIGN_BIT) != 0) {
            return BigInteger.ZERO;
        }

        BigInteger target = BigInteger.valueOf(mantissa);
        if (size <= 3) {
            return target.shiftRight(8 * (3 - size));
        }
        return target.shiftLeft(8 * (size - 3));
    }

    public static long encodeCompactTarget(BigInteger target) {
        if (target == null || target.signum() <= 0) {
            return 0L;
        }

        byte[] bytes = toUnsignedBytes(target);
        int size = bytes.length;
        long mantissa;

        if (size <= 3) {
            mantissa = target.longValue() << (8 * (3 - size));
        } else {
            mantissa = target.shiftRight(8 * (size - 3)).longValue();
        }

        if ((mantissa & COMPACT_SIGN_BIT) != 0) {
            mantissa >>= 8;
            size += 1;
        }

        return ((long) size << 24) | (mantissa & COMPACT_MANTISSA_MASK);
    }

    public static BigInteger clampTargetChange(BigInteger previousTarget, BigInteger proposedTarget) {
        if (previousTarget == null || proposedTarget == null || previousTarget.signum() <= 0 || proposedTarget.signum() <= 0) {
            return BigInteger.ZERO;
        }

        BigInteger maxTarget = multiply(previousTarget, Seting.V35_MAX_TARGET_UP);
        BigInteger minTarget = multiply(previousTarget, Seting.V35_MAX_TARGET_DOWN);

        if (proposedTarget.compareTo(maxTarget) > 0) {
            return maxTarget;
        }
        if (proposedTarget.compareTo(minTarget) < 0) {
            return minTarget;
        }
        return proposedTarget;
    }

    public static BigInteger calculateNextTarget(BigInteger previousTarget, long actualTimespanSeconds, long expectedTimespanSeconds) {
        if (previousTarget == null || previousTarget.signum() <= 0 || actualTimespanSeconds <= 0 || expectedTimespanSeconds <= 0) {
            return BigInteger.ZERO;
        }

        BigInteger proposedTarget = BigDecimal.valueOf(actualTimespanSeconds)
                .multiply(new BigDecimal(previousTarget))
                .divide(BigDecimal.valueOf(expectedTimespanSeconds), 0, RoundingMode.DOWN)
                .toBigInteger();
        if (proposedTarget.signum() <= 0) {
            proposedTarget = BigInteger.ONE;
        }
        return clampTargetChange(previousTarget, proposedTarget);
    }

    public static BigInteger applyEmergencyDrop(BigInteger currentTarget, long secondsSinceLastBlock) {
        if (currentTarget == null || currentTarget.signum() <= 0) {
            return BigInteger.ZERO;
        }
        if (secondsSinceLastBlock < Seting.V35_EMERGENCY_DELAY_SECONDS) {
            return currentTarget;
        }
        return clampTargetChange(currentTarget, multiply(currentTarget, Seting.V35_MAX_TARGET_UP));
    }

    private static BigInteger multiply(BigInteger target, double multiplier) {
        BigInteger result = new BigDecimal(target)
                .multiply(BigDecimal.valueOf(multiplier))
                .setScale(0, RoundingMode.DOWN)
                .toBigInteger();
        return result.signum() <= 0 ? BigInteger.ONE : result;
    }

    private static byte[] toUnsignedBytes(BigInteger target) {
        byte[] bytes = target.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] unsigned = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, unsigned, 0, unsigned.length);
            return unsigned;
        }
        return bytes;
    }
}
