package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.model.Account;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsBlock;
import International_Trade_Union.utils.UtilsSecurity;
import International_Trade_Union.utils.UtilsUse;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BurnAddressPolicyTest {
    private static final long SIMULATED_FORK_INDEX = 10L;
    private static final String NORMAL_ADDRESS = "normal-address";

    @Test
    public void exposesSentinelBurnAddressAndKeepsNoSpendInactiveWhileV35Disabled() {
        String burnAddress = Seting.V35_BURN_ADDRESS;

        assertEquals("BURN_ADDRESS", burnAddress);
        assertEquals(burnAddress, BurnAddressPolicy.getBurnAddress());
        assertFalse(BurnAddressPolicy.isPlaceholderBurnAddress());
        assertTrue(BurnAddressPolicy.isBurnAddress(burnAddress));
        assertFalse(BurnAddressPolicy.isBurnAddress(NORMAL_ADDRESS));
        assertTrue(BurnAddressPolicy.canSpendFromAddress(0, NORMAL_ADDRESS));
        assertTrue(BurnAddressPolicy.canSpendFromAddress(International_Trade_Union.protocol.v35.V35TestMode.preForkIndex(), burnAddress));
        assertFalse(BurnAddressPolicy.isNoSpendActive(International_Trade_Union.protocol.v35.V35TestMode.preForkIndex()));
        assertFalse(BurnAddressPolicy.isSpendForbidden(International_Trade_Union.protocol.v35.V35TestMode.preForkIndex(), burnAddress));
    }

    @Test
    public void rejectsBurnAddressSpendOnlyWhenExplicitForkIsActive() {
        String burnAddress = Seting.V35_BURN_ADDRESS;

        assertTrue(BurnAddressPolicy.canSpendFromAddress(9L, burnAddress, SIMULATED_FORK_INDEX));
        assertFalse(BurnAddressPolicy.canSpendFromAddress(10L, burnAddress, SIMULATED_FORK_INDEX));
        assertTrue(BurnAddressPolicy.canSpendFromAddress(10L, NORMAL_ADDRESS, SIMULATED_FORK_INDEX));
        assertTrue(BurnAddressPolicy.isSpendForbidden(10L, burnAddress, SIMULATED_FORK_INDEX));
        assertFalse(BurnAddressPolicy.isSpendForbidden(9L, burnAddress, SIMULATED_FORK_INDEX));
        assertFalse(BurnAddressPolicy.isSpendForbidden(10L, NORMAL_ADDRESS, SIMULATED_FORK_INDEX));
    }

    @Test
    public void signedTransactionToBurnAddressVerifiesRecipientIsNotPublicKeyDecoded() throws Exception {
        DtoTransaction transaction = new DtoTransaction(
                Seting.BASIS_ADDRESS,
                Seting.V35_BURN_ADDRESS,
                1.00,
                0.0,
                new Laws(),
                0.0,
                VoteEnum.YES);
        transaction.setSign(UtilsSecurity.sign(basisPrivateKey(), transaction.toSign()));

        assertTrue(BurnAddressPolicy.isBurnAddress(transaction.getCustomer()));
        assertFalse(BurnAddressPolicy.isSpendForbidden(SIMULATED_FORK_INDEX, transaction.getSender(), SIMULATED_FORK_INDEX));
        assertTrue(transaction.verify());
    }

    @Test
    public void utilsUsePathDoesNotCrashForBurnSenderWhileV35Disabled() {
        DtoTransaction transaction = burnSenderTransaction();

        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertDoesNotThrow(() -> {
            List<DtoTransaction> accepted = UtilsUse.balanceTransaction(
                    Collections.singletonList(transaction),
                    new HashMap<>(),
                    0L);
            assertTrue(accepted.isEmpty());
        });
    }

    @Test
    public void blockValidationRejectsBurnSenderBeforeDecodeWhenForkIsActive() {
        Block previous = block(9L, Collections.emptyList());
        Block current = block(10L, Collections.singletonList(burnSenderTransaction()));
        Map<String, Account> balances = new HashMap<>();
        balances.put(Seting.V35_BURN_ADDRESS, new Account(Seting.V35_BURN_ADDRESS, BigDecimal.TEN));

        Boolean valid = assertDoesNotThrow(() -> UtilsBlock.validationOneBlockWithCanonicalContext(
                "founder",
                previous,
                current,
                Collections.singletonList(previous),
                null,
                balances,
                new ArrayList<>(),
                new ArrayList<>(),
                Collections.singletonList(previous),
                SIMULATED_FORK_INDEX));

        assertFalse(valid);
    }

    private static DtoTransaction burnSenderTransaction() {
        DtoTransaction transaction = new DtoTransaction(
                Seting.V35_BURN_ADDRESS,
                NORMAL_ADDRESS,
                1.00,
                0.0,
                new Laws(),
                0.0,
                VoteEnum.YES);
        transaction.setSign(new byte[]{1, 2, 3});
        return transaction;
    }

    private static Block block(long index, List<DtoTransaction> transactions) {
        return new Block(new ArrayList<>(transactions), "previous-" + index, "miner", "founder",
                0L, 0.0, 1L, new Timestamp(index * 1000L), index, hash(index));
    }

    private static String hash(long index) {
        return String.format("%064x", index + 1L);
    }

    private static PrivateKey basisPrivateKey() throws Exception {
        Base base = new Base58();
        return UtilsSecurity.privateBytToPrivateKey(base.decode(Seting.BASIS_PASSWORD));
    }
}
