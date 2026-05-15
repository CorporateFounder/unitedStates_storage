package International_Trade_Union.protocol.v35;

import java.math.BigInteger;

/**
 * Future cumulative-work helper. This placeholder has no chain-selection integration.
 */
public final class V35WorkCalculator {
    private V35WorkCalculator() {
    }

    public static BigInteger zeroWork() {
        return BigInteger.ZERO;
    }

    public static BigInteger addWork(BigInteger currentWork, BigInteger additionalWork) {
        BigInteger safeCurrent = currentWork == null ? BigInteger.ZERO : currentWork;
        BigInteger safeAdditional = additionalWork == null ? BigInteger.ZERO : additionalWork;
        return safeCurrent.add(safeAdditional);
    }

    public static BigInteger workFromTarget(BigInteger target) {
        if (target == null || target.signum() <= 0) {
            return BigInteger.ZERO;
        }
        return BigInteger.ONE.shiftLeft(256).divide(target.add(BigInteger.ONE));
    }
}
