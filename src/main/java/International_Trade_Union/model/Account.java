package International_Trade_Union.model;


import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.utils.UtilsSecurity;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import lombok.Data;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/**Класс Аккаунт, хранит данные такие данные.
 * account - public key ECDSA
 * digitalDollarBalance - цифровой доллар (деньги).
 * digitalStockBalance - акции, используется для голосования.
 * digitalStakingBalance - сумма долларов которые зарезервированы для staking (pos).
 **/
@Data
public class Account implements Cloneable {
    private String account;
    private double digitalDollarBalance;
    private double digitalStockBalance;
    private double digitalStakingBalance;



    public Account(String account, double digitalDollarBalance) {
        this(account, digitalDollarBalance, 0.0, 0);

    }

    public Account(String account, double digitalDollarBalance, double digitalStockBalance, double digitalStakingBalance) {
        this.account = account;
        this.digitalDollarBalance = digitalDollarBalance;
        this.digitalStockBalance = digitalStockBalance;
        this.digitalStakingBalance = digitalStakingBalance;

    }

    public Account() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account1 = (Account) o;
        return getAccount().equals(account1.getAccount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount());
    }

    private DtoTransaction sendMoney(String recipient, String privatekey, double digitalDollar, double digitalStock, Laws laws, double minerRewards, VoteEnum voteEnum) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, SignatureException, IOException, InvalidKeyException {

        DtoTransaction transaction = null;
        if (account.equals(recipient)){
            System.out.println("sender %s, recipient %s cannot be equals! Error!".format(account,recipient));
            return transaction;
        }

            if(digitalDollarBalance < digitalDollar + minerRewards  ){
                System.out.println("sender don't have dollar");
                return transaction;
            }
            if(digitalStockBalance < digitalStock){
                System.out.println("sender don't have stock");
                return transaction;
            }
            if(digitalStakingBalance < digitalDollar){
                System.out.println("sender don't have staking");
                return transaction;
            }
            else{
                Base base = new Base58();
                PrivateKey privateKey = UtilsSecurity.privateBytToPrivateKey(base.decode(privatekey));
                 transaction = new DtoTransaction(this.getAccount(), recipient, digitalDollar, digitalStock, laws, minerRewards, voteEnum);
                byte[] signGold = UtilsSecurity.sign(privateKey, transaction.toSign());
                transaction.setSign(signGold);
            }

       return transaction;
    }

//      recipient - получатель
//      gold сумма отправки, last Block - это послдний блок.
    public DtoTransaction send(String recipient, String privateKey, double digitalDollar, double digitalReputation, Laws laws,  double minerRewards, VoteEnum voteEnum) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, IOException, NoSuchProviderException, InvalidKeyException {
         return sendMoney(recipient,privateKey, digitalDollar, digitalReputation, laws, minerRewards, voteEnum);
    }

    @Override
    public Account clone() throws CloneNotSupportedException {
        return new Account(account, digitalDollarBalance, digitalStockBalance, digitalStakingBalance);
    }
}
