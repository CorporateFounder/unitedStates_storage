package International_Trade_Union.network;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AllTransactionsV35BurnHardeningTest {
    private static final long SIMULATED_FORK_INDEX = 10L;
    private static final long ACTIVE_CHAIN_HEIGHT = 10L;
    private static final long LEGACY_CHAIN_HEIGHT = 9L;

    @Test
    public void transactionPoolRejectsBurnSenderBeforeVerifyWhenV35IsSimulatedActive() throws Exception {
        TrackingTransaction burnSpend = tracking(Seting.V35_BURN_ADDRESS, "recipient", true);
        AllTransactions pool = activePool();

        pool.addTransaction(burnSpend);

        assertEquals(0, burnSpend.verifyCalls);
        assertTrue(pool.getTransactions().isEmpty());
    }

    @Test
    public void transactionPoolAllowsRecipientBurnAddressAndNormalTransactionsWhenV35IsSimulatedActive() throws Exception {
        TrackingTransaction burnRecipient = tracking(Seting.BASIS_ADDRESS, Seting.V35_BURN_ADDRESS, true);
        TrackingTransaction normal = tracking("normal-sender", "normal-recipient", true);
        AllTransactions pool = activePool();

        pool.addTransaction(burnRecipient);
        pool.addTransaction(normal);
        List<DtoTransaction> accepted = pool.getTransactions();

        assertEquals(2, accepted.size());
        assertTrue(accepted.stream().anyMatch(t -> Seting.V35_BURN_ADDRESS.equals(t.getCustomer())));
        assertTrue(accepted.stream().anyMatch(t -> "normal-recipient".equals(t.getCustomer())));
        assertTrue(burnRecipient.verifyCalls > 0);
        assertTrue(normal.verifyCalls > 0);
    }

    @Test
    public void transactionPoolKeepsLegacyDisabledBehaviorBeforeV35() throws Exception {
        TrackingTransaction burnSpend = tracking(Seting.V35_BURN_ADDRESS, "recipient", false);
        AllTransactions pool = legacyPool();

        pool.addTransaction(burnSpend);

        assertEquals(2, burnSpend.verifyCalls);
        assertTrue(pool.getTransactions().isEmpty());
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
    }

    private static AllTransactions activePool() {
        return new AllTransactions(new FakeBlockService(), () -> ACTIVE_CHAIN_HEIGHT, () -> SIMULATED_FORK_INDEX);
    }

    private static AllTransactions legacyPool() {
        return new AllTransactions(new FakeBlockService(), () -> LEGACY_CHAIN_HEIGHT, () -> SIMULATED_FORK_INDEX);
    }

    private static TrackingTransaction tracking(String sender, String customer, boolean verifyResult) {
        TrackingTransaction transaction = new TrackingTransaction(sender, customer, verifyResult);
        transaction.setSign(new byte[]{(byte) sender.hashCode(), (byte) customer.hashCode()});
        return transaction;
    }

    private static final class TrackingTransaction extends DtoTransaction {
        private final boolean verifyResult;
        private int verifyCalls;

        private TrackingTransaction(String sender, String customer, boolean verifyResult) {
            super(sender, customer, 1.00, 0.0, new Laws(), 0.0, VoteEnum.YES);
            this.verifyResult = verifyResult;
        }

        @Override
        public boolean verify() {
            verifyCalls++;
            return verifyResult;
        }
    }

    private static final class FakeBlockService extends BlockService {
        @Override
        public boolean existsBySign(byte[] sign) {
            return false;
        }

        @Override
        public List<EntityAccount> findByDtoAccounts(List<DtoTransaction> transactions) {
            List<EntityAccount> accounts = new ArrayList<>();
            accounts.add(account(Seting.BASIS_ADDRESS, "0.00"));
            accounts.add(account(Seting.V35_BURN_ADDRESS, "0.00"));
            accounts.add(account("normal-sender", "10.00"));
            accounts.add(account("normal-recipient", "0.00"));
            accounts.add(account("recipient", "0.00"));
            return accounts;
        }

        private static EntityAccount account(String account, String gold) {
            return new EntityAccount(account, new BigDecimal(gold), BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }
}
