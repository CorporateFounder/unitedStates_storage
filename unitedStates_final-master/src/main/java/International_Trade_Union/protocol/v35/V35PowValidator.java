package International_Trade_Union.protocol.v35;

import java.math.BigInteger;

/**
 * Pure target comparison helper. It is not connected to block validation yet.
 */
public final class V35PowValidator {
    private V35PowValidator() {
    }

    public static boolean isHashUnderTarget(String hashHex, BigInteger target) {
        if (hashHex == null || hashHex.isBlank() || target == null || target.signum() < 0) {
            return false;
        }
        if (!hashHex.matches("[0-9a-fA-F]+")) {
            return false;
        }
        BigInteger hashNumber = new BigInteger(hashHex, 16);
        return hashNumber.compareTo(target) <= 0;
    }
}
